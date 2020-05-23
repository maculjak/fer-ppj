package hr.fer.zemris.ppj.lab01.analizator;

import hr.fer.zemris.ppj.lab01.Automaton;
import hr.fer.zemris.ppj.lab01.Rule;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * LA class is used as a main class that captures the functionality of a lexical analyzer that uses resources generated
 * by GLA. The analyzer first gets all the generated resources as well as the input code. After that the analyzer starts
 * iterating through the input code, grouping the code in the lexemes and performing actions described in the description
 * of a lexical analyzer which is passed to GLA.
 * 
 * @author Charlie
 **/
public class LA {

    private static ArrayList<Rule> rules = new ArrayList<>();

    public static void main (String[] args){
        String code = "";
        String[] lexicalAnalyzerStates = {""};
        
        try {
            // Reading the items created by lexical analyzer generator
            @SuppressWarnings("unchecked")
            ArrayList<Rule> lexicalAnalyzerRules = (ArrayList<Rule>) deSerializeObject("rules_serialized.txt");
            lexicalAnalyzerStates = (String[]) deSerializeObject("states_serialized.txt");

            rules = lexicalAnalyzerRules;

            // Reading the input code from the console
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            StringBuilder codeBuilder = new StringBuilder();

            while(true) {
                String line = br.readLine();
                if (line == null) break;
                codeBuilder.append(line).append("\n");
            }
            
            code = codeBuilder.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }

        HashMap<String, ArrayList<Rule>> rulesAtStates = new HashMap<>();

        if(lexicalAnalyzerStates == null) {
            System.err.println("Lexical analyzer states not initialized correctly. Closing the program...");
            System.exit(1);
        }

        for(String state : lexicalAnalyzerStates) {
            ArrayList<Rule> rules = getRulesFromState(state);
            rulesAtStates.put(state, rules);
        }

        int codeIndex = 0;
        int lineCounter = 1;

        String state = lexicalAnalyzerStates[0];

        // Lexical analysis
        while(codeIndex < code.length()) {
            int lengthOfTheLongestSequence = 0;
            Rule THERule = null;
            boolean matches = false;

            for(Rule r : rulesAtStates.get(state)) {
                Automaton belongingAutomaton = r.getAutomaton();

                int endIndex = belongingAutomaton.getLongestMatchingPrefix(code.substring(codeIndex));
                if(endIndex >= 0) {
                	matches = true;
                }
                int lengthOfCurrentMatchingSubsequence = endIndex + 1;

                if (lengthOfCurrentMatchingSubsequence > lengthOfTheLongestSequence) {
                    lengthOfTheLongestSequence = lengthOfCurrentMatchingSubsequence;
                    THERule = r;
                }
            }
            
            // If no automaton accepts the prefix starting with the current character, we can move on to the next character
            if(!matches) {
                codeIndex++;
                continue;
            }

            ArrayList<String> actions = THERule.getActions();
            boolean isLexicalUnit = true;

            StringBuilder output = new StringBuilder();

            for (String action : actions) {
                if (action.startsWith("-")) isLexicalUnit = false;
                else if (action.startsWith("UDJI_U_STANJE")) state = action.split(" ")[1];
                else if (action.startsWith("NOVI_REDAK")) lineCounter++;
                else if (action.startsWith("VRATI_SE")) lengthOfTheLongestSequence = Integer.parseInt(action.split(" ")[1]);
                else output.append(action).append(" ");
            }

            if (isLexicalUnit) {
                output.append(lineCounter).append(" ");
                output.append(code, codeIndex, codeIndex + lengthOfTheLongestSequence);
                System.out.println(output);
            }

            codeIndex += lengthOfTheLongestSequence;
        }
    }

    /**
     * 
     * 
     * @param lexicalAnalyzerState - state of lexical analyzer
     * @return ArrayList of rules that can be used from a given lexical analyzer state
     */
    private static ArrayList<Rule> getRulesFromState (String lexicalAnalyzerState) {
        ArrayList<Rule> availableRules = new ArrayList<>();
        rules.forEach(r -> {
            if (r.getLexicalAnalyzerState().equals(lexicalAnalyzerState)) {
            	availableRules.add(r);
            }
        });
        
        return availableRules;
    }

    private static Object deSerializeObject(String path) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path))) {
            return in.readObject();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
