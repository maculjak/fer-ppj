package hr.fer.zemris.ppj.lab01;

import java.io.Serializable;
import java.util.ArrayList;

public class Rule implements Serializable {

    private static final long serialVersionUID = 123456789L;
    
    private String lexicalAnalyzerState;
    private String regex;
    private Automaton automaton = new Automaton();
    private ArrayList<String> actions = new ArrayList<>();
    
    public Rule(String lexicalAnalyzerState, String regex) {
        this.lexicalAnalyzerState = lexicalAnalyzerState;
        this.regex = regex;
        PairOfStates startAndFinal = convert(regex, automaton);
        automaton.setStartState(startAndFinal.getLeftState());
        automaton.setFinalState(startAndFinal.getRightState());
    }

    public boolean addAction(String action) {
        return this.actions.add(action);
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    private static int findMatchingParenthesis(String expression, int index) {
        int numberOfParentheses = 1;
        while (numberOfParentheses > 0) {
            index++;
            if (expression.charAt(index) == '(') numberOfParentheses++;
            else if (expression.charAt(index) == ')') numberOfParentheses--;
        }
        
        return index;
    }

    /**
     * Function that checks if a character at index i is an operator.<br>
     * The pseudo-code is given in the instructions.
     * 
     * @param expression
     * @param i - index of an operator to check
     * @return <code>true</code> if a character is an operator, <code>false</code> otherwise
     */
    private boolean isOperator(String expression, int i) {
        int br = 0;
        while (i > 0 && expression.charAt(i-1)=='\\') {
            br++;
            i--;
        }
        
        return br % 2 == 0;
    }

    /**
     * Function that converts a regular expression to e-NFA.<br>
     * The pseudo-code is given in the instructions.
     * 
     * @param expression - regular expression that will be converted into automaton
     * @param automaton - resulting automaton
     * @return returns left and right states which are start and final states of the resulting automaton
     */
    private PairOfStates convert(String expression, Automaton automaton) {
        ArrayList<String> expressionChoices = new ArrayList<>();
        int numberOfParentheses = 0;
        int startOfLastExpression = 0;

        for (int i = 0; i < expression.length(); i++) {
            if (expression.charAt(i) == '(' && isOperator(expression, i)) numberOfParentheses++;
            else if (expression.charAt(i) == ')' && isOperator(expression, i)) numberOfParentheses--;
            else if (numberOfParentheses == 0 && expression.charAt(i) == '|' && isOperator(expression, i)) {
                expressionChoices.add(expression.substring(startOfLastExpression, i));
                startOfLastExpression = i + 1;
            }
        }
        
        if (expressionChoices.size() > 0) expressionChoices.add(expression.substring(startOfLastExpression));

        int leftState = automaton.newState();
        int rightState = automaton.newState();

        if(expressionChoices.size() > 0) {
            for(String ex : expressionChoices) {
                PairOfStates temp = convert(ex, automaton);
                automaton.addEpsilonTransition(leftState, temp.getLeftState());
                automaton.addEpsilonTransition(temp.getRightState(), rightState);
            }
        } else {
            boolean prefixed = false;
            int lastState = leftState;

            for(int i = 0; i < expression.length(); i++) {
                int a, b;

                if(prefixed) {
                    prefixed = false;
                    char transitionCharacter;

                    if(expression.charAt(i) == 't') transitionCharacter = '\t';
                    else if(expression.charAt(i) == 'n') transitionCharacter = '\n';
                    else if(expression.charAt(i) == '_') transitionCharacter = ' ';
                    else transitionCharacter = expression.charAt(i);

                    a = automaton.newState();
                    b = automaton.newState();

                    automaton.addTransition(a, b, String.valueOf(transitionCharacter));
                } else {
                    if(expression.charAt(i) == '\\') {
                        prefixed = true;
                        continue;
                    }
                    
                    if(expression.charAt(i) != '(') {
                        a = automaton.newState();
                        b = automaton.newState();

                        if(expression.charAt(i) == '$') automaton.addEpsilonTransition(a, b);
                        else automaton.addTransition(a, b, String.valueOf(expression.charAt(i)));
                    } else {
                        int j = findMatchingParenthesis(expression, i);
                        PairOfStates temp = convert(expression.substring(i + 1, j), automaton);

                        a = temp.getLeftState();
                        b = temp.getRightState();
                        i = j;
                    }
                }
                
                if((i < expression.length() - 1) && (expression.charAt(i + 1) == '*')) {
                    int x = a;
                    int y = b;

                    a = automaton.newState();
                    b = automaton.newState();

                    automaton.addEpsilonTransition(a, x);
                    automaton.addEpsilonTransition(y, b);
                    automaton.addEpsilonTransition(a, b);
                    automaton.addEpsilonTransition(y, x);

                    i++;
                }
                
                automaton.addEpsilonTransition(lastState, a);
                lastState = b;
            }
            
            automaton.addEpsilonTransition(lastState, rightState);
        }
        
        return new PairOfStates(leftState, rightState);
    }
    
    public Automaton getAutomaton() {
        return automaton;
    }

    public String getLexicalAnalyzerState() {
        return lexicalAnalyzerState;
    }

    public ArrayList<String> getActions() {
        return actions;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("State: ").append(lexicalAnalyzerState).append("\n");
        sb.append("Regex: ").append(regex).append("\n");
        sb.append("Actions:");
        
        for(String action : actions) {
        	sb.append(" ").append(action);
        }
        
        sb.append("\n");
        return sb.toString();
    }
}
