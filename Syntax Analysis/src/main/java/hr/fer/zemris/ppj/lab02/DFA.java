package hr.fer.zemris.ppj.lab02;

import java.util.HashSet;

/**
 * Represents a simple deterministic finite state machine.
 * @param <T> state of the automaton
 * @see Automaton
 */

public class DFA<T> extends Automaton<T> {

    public DFA(HashSet<T> finalStates, T startState) {
        super(finalStates, startState);
    }
    
    public DFA(T startState) {
        super(startState);
    }
    
    public T getNextState(Key<T> key) {
        return getTransitionFunction().get(key).stream().findFirst().get();
    }
    
}
