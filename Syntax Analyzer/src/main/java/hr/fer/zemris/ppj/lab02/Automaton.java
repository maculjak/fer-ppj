package hr.fer.zemris.ppj.lab02;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a simple finite state machine (automaton). In order to define an
 * automaton we have to create a transition function and specify start state and final states
 * @param <T> this parameter is used to specify data type of the automaton states
 */

public abstract class Automaton<T> {

    private HashMap<Key<T>, HashSet<T>> transitionFunction = new HashMap<>();
    private HashSet<T> finalStates = new HashSet<>();
    private T startState;
    private HashSet<T> states;

    public Automaton(HashSet<T> finalStates, T startState) {
        this.finalStates.addAll(finalStates);
        this.startState = startState;
        this.states = new HashSet<>(finalStates);
    }

    public Automaton(T startState) {
        this.finalStates.add(startState);
        this.startState = startState;
        this.states = new HashSet<>();
    }

    public void addTransition(T fromState, T toState, String character) {
        Key<T> transitionKey = new Key<>(fromState, character);

        if (transitionFunction.containsKey(transitionKey)
                && !transitionFunction.get(transitionKey).contains(toState)) {
        	transitionFunction.get(transitionKey).add(toState);
        }
        else {
            transitionFunction.put(transitionKey, new HashSet<>());
            transitionFunction.get(transitionKey).add(toState);
        }

        states.add(fromState);
        states.add(toState);
    }

    public HashMap<Key<T>, HashSet<T>> getTransitionFunction() {
        return transitionFunction;
    }

    public HashSet<T> getFinalStates() {
        return finalStates;
    }

    public T getStartState() {
        return startState;
    }

    public boolean transitionExists(Key<T> key) {
        return transitionFunction.containsKey(key);
    }

    public HashSet<T> getResultingStates(Key<T> key) {
        return transitionFunction.get(key);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        transitionFunction.forEach((k,v) -> sb.append("For state   ").append(k.getState())
                .append("   and character   ").append(k.getCharacter()).append("   next state is   ")
                .append(v).append("\n"));
        
        return sb.toString();
    }

    public Set<Key<T>> getKeySet() {
        return transitionFunction.keySet();
    }

    public void addToFinalStates(T finalStates) {
        this.finalStates.add(finalStates);
    }

    public int getNumberOfStates() {
        return states.size();
    }

    public HashSet<T> getStates() {
        return states;
    }
}
