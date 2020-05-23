package hr.fer.zemris.ppj.lab02;

import java.io.Serializable;
import java.util.Objects;

/**
 * LR parser table is modelled as a hash map. Key class represents a key to that
 * hash map. Key consists of a state and character.
 * @param <T> LR parser state type
 */
public class Key<T> implements Serializable {
	
	private static final long serialVersionUID = 5642243865718029497L;
	
	private T state;
    private String character;

    public Key(T state, String character) {
        this.state = state;
        this.character = character;
    }

    public T getState() {
        return state;
    }

    public void setState(T state) {
        this.state = state;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    @Override
    public String toString() {
        return state + ", " + character;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key<?> key = (Key<?>) o;
        
        return Objects.equals(state, key.state) && Objects.equals(character, key.character);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, character);
    }
}
