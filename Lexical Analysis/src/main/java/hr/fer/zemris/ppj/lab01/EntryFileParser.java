package hr.fer.zemris.ppj.lab01;

import java.io.*;
import java.util.*;

/**
 * A class for parsing the file which contains information about lexical analyzer. The format of lexical analyzer
 * description file is as follows:
 * Regular definitions
 * %X Lexical analyzer states
 * %L Lexemes
 * Lexical analyzer rules
 *
 * The format of a regular definition is as follows:
 * {RegularDefinitionName} RegularExpression
 *
 * The format of lexical analyzer rules is as follows
 * <lexical_analyzer_state>RegularExpression
 * {
 * ACTIONS
 * }
 *
 * @author Charlie
 */
public class EntryFileParser {

	private String[] lexicalAnalyzerStatesArray;
    private String[] lexicalUnitNamesArray;
    private ArrayList<RegularDefinition> regularDefinitions = new ArrayList<>();
    private ArrayList<Rule> lexicalAnalyzerRules = new ArrayList<>();

    public EntryFileParser() {
        // This collection will be used to store all regular definitions that
        // cannot be immediately converted to an automaton.
        ArrayList<RegularDefinition> invalidRegularDefinitions = new ArrayList<>();
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new BufferedInputStream(System.in), "UTF-8"));

            String line;
            line = br.readLine();

            // Parsing regular definitions
            while (!line.startsWith("%X")) {
                String[] pair = line.split(" ");
                RegularDefinition regDef = new RegularDefinition(pair[0], pair[1]);

                if (regDef.getExpression().matches(".*\\{[A-Za-z]+\\}.*")) {
                	invalidRegularDefinitions.add(regDef);
                } else {
                	regularDefinitions.add(regDef);
                }

                line = br.readLine();
            }
            
            // We use this variable to store regular definitions because
            // we will now iterate through all regular definitions and
            // will possibly add elements to it later on.
            ArrayList<RegularDefinition> temp = regularDefinitions;

            for (RegularDefinition ird : invalidRegularDefinitions) {
                regularDefinitions = temp;
                if (!ird.getExpression().matches(".*\\{[A-Za-z]+\\}.*")) break;

                for (RegularDefinition rd : regularDefinitions) {
                	String rdName = rd.getName();
                    String rdExpression = rd.getExpression();

                    if (ird.getExpression().contains(rdName)) {
                    	ird.setExpression(ird.getExpression().replace(rdName, "(" + rdExpression + ")"));
                    }
                    
                    if (!ird.getExpression().matches(".*\\{[A-Za-z]+\\}.*")) {
                        temp.add(ird);
                        break;
                    }
                }
            }

            regularDefinitions = temp;

            lexicalAnalyzerStatesArray = line.substring(3).split(" ");
            lexicalUnitNamesArray = br.readLine().substring(3).split(" ");

            // Parsing lexical analyzer rules
            line = br.readLine();
            while (line != null && !line.equals("")) {
                String state = line.substring(1, line.indexOf('>'));
                String regex = line.substring(line.indexOf('>') + 1);

                if (regex.matches(".*\\{[A-Za-z]+\\}.*")) {
                    for (RegularDefinition rd : regularDefinitions) {
                        if (!regex.matches(".*\\{[A-Za-z]+\\}.*")) break;
                        
                        if (regex.contains(rd.getName())) {
                        	regex = regex.replace(rd.getName(), "(" + rd.getExpression() + ")");
                        }
                    }
                }
                
                // This reads the "{" character.
                br.readLine();
                
                // In the Rule constructor the rest of the work happens.
                Rule rule = new Rule(state, regex);

                while (!(line = br.readLine()).startsWith("}")) {
                    // Action is a String until we make a class to model actions
                    rule.addAction(line);
                }

                lexicalAnalyzerRules.add(rule);
                line = br.readLine();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<RegularDefinition> getRegularDefinitions() {
        return regularDefinitions;
    }

    public String[] getLexicalAnalyzerStatesArray() {
        return lexicalAnalyzerStatesArray;
    }

    public String[] getLexicalUnitNamesArray() {
        return lexicalUnitNamesArray;
    }

    public ArrayList<Rule> getLexicalAnalyzerRules() {
        return lexicalAnalyzerRules;
    }
}
