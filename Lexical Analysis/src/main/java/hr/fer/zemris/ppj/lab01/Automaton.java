package hr.fer.zemris.ppj.lab01;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

/**
 * The automaton class is used to model a non-deterministic final automaton with epsilon transitions. Transition function
 * is modeled by a {@link HashMap} in which the key is a {@link String} (of the format STATE,CHARACTER) and the value of each key is
 * a state which in our case is modeled as an integer. The automaton start and final state are of course modeled by
 * integers.
 */
public class Automaton implements Serializable {

    private static final long serialVersionUID = 123456789L;

    private int numberOfStates = 0;
    private int startState;
    private int finalState;
    private HashMap<String, ArrayList<Integer>> transitionFunction = new HashMap<>();

    /**
     * This method adds a new state to the automaton, hence the name.
     * 
     * @return number of states before increment, or an ID of the state that was just added
     */
    public int newState() {
        return numberOfStates++;
    }

    /**
     * This method adds an epsilon transition from the state leftState to the state rightState.
     * 
     * @param leftState
     * @param rightState
     */
    public void addEpsilonTransition(int leftState, int rightState) {
        addTransition(leftState, rightState, "");
    }

    /**
     * Adds a transition from the state leftState to the state rightState at the occurrence of a given character.
     * The method first checks if there is a transition at a given state with a given character. In that case the
     * rightState is added to the ArrayList containing resulting states. Otherwise, new ArrayList is created.
     * 
     * @param leftState
     * @param rightState
     * @param character
     */
    public void addTransition(int leftState, int rightState, String character) {
        String transitionKey = leftState + "," + character;
        if(transitionFunction.containsKey(transitionKey)
                && !transitionFunction.get(transitionKey).contains(rightState)) {
        	transitionFunction.get(transitionKey).add(rightState);
        } else if(!transitionFunction.containsKey(transitionKey)) {
            transitionFunction.put(transitionKey, new ArrayList<>());
            transitionFunction.get(transitionKey).add(rightState);
        }
    }
    
    /**
     * This method is used to get the longest prefix of a given sequence of characters that is accepted by the automaton.
     * First step of the method is to get the epsilon closure of the start state. After that we start iterating through
     * the given sequence and in every iteration we try to obtain the next set of states based on the current state
     * and the current character. At the and of every iteration we check if the set of next states is empty. That means
     * that the automaton has come to an end and we have to break out of the loop. We also check if the set of next states
     * contains the final state of the automaton. In that case we have a matching prefix, so we can update our end index.
     * 
     * @param sequence
     * @return end index of the longest prefix that matches the regex which defines this automaton. If there is no
     * sequence accepted by the automaton, the method returns -1.
     */
    public int getLongestMatchingPrefix(String sequence) {
        ArrayList<Integer> currentStates = new ArrayList<>();
        currentStates.add(startState);
        currentStates = epsilonClosure(currentStates); // Getting the epsilon closure of start states

        int index = -1; // Index is initially at -1 which means that no subsequence is matched
        
        for(int i = 0; i < sequence.length(); i++) {
            char currentCharacter = sequence.charAt(i); // Current character that is being processed
            ArrayList<Integer> nextStates = new ArrayList<>();

            // Getting the entire set of next states
            for(int state : currentStates) {
                String transitionKey = state + "," + currentCharacter;
                if(transitionFunction.containsKey(transitionKey)) {
                	nextStates.addAll(epsilonClosure(transitionFunction.get(transitionKey)));
                }
            }

            currentStates = nextStates;

            if(currentStates.isEmpty()) break; // If there is no more states to be in that means that we have reached the end of matching subsequence
            if(currentStates.contains(finalState)) index = i; // If we are in final state we can update the index of the end of a subsequence
        }
        
        return index;
    }

    /**
     * This method is used to get the epsilon closure of a set of states. The algorithm uses a {@link Stack} as an auxiliary
     * data structure. The method first initializes a {@link Stack} and a {@link HashSet} and adds all the states from a given set of
     * states to them. While the stack is not empty, the algorithm takes one state from the stack at a time and adds
     * all the states resulting from the transition from the current state to the stack as well as to the {@link HashSet} if
     * it doesn't contain them.
     * 
     * @param state
     * @return an epsilon closure of a given {@link ArrayList} of states in form of an {@link ArrayList}
     */
    public ArrayList<Integer> epsilonClosure(ArrayList<Integer> state) {
    	HashSet<Integer> closure = new HashSet<>(state);
        Stack<Integer> stack = new Stack<>();
        stack.addAll(state);
        
        while(!stack.isEmpty()) {
            int s = stack.pop();
            if(!transitionFunction.containsKey(s + ",")) continue;
            
            for(int r : transitionFunction.get(s + ",")) {
            	if(!closure.contains(r)) {
                    stack.push(r);
                    closure.add(r);
                }
            }
        }
        
        return new ArrayList<>(closure);
    }
    
    public int getNumberOfStates() {
        return numberOfStates;
    }

    public HashMap<String, ArrayList<Integer>> getTransitionFunction() {
        return transitionFunction;
    }

    public int getStartState() {
        return startState;
    }

    public void setStartState(int startState) {
        this.startState = startState;
    }

    public int getFinalState() {
        return finalState;
    }

    public void setFinalState(int finalState) {
        this.finalState = finalState;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Start state: ").append(startState).append("\n");
        sb.append("Final state: ").append(finalState).append("\n");

        for(String key : transitionFunction.keySet()) {
            sb.append(key);
            sb.append("->");
            for(int i : transitionFunction.get(key)) {
                sb.append(i);
                sb.append(',');
            }
            sb.deleteCharAt(sb.lastIndexOf(","));
            sb.append("\n");
        }
        
        return sb.toString();
    }
}
