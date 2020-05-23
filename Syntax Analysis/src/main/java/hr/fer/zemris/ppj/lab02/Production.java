package hr.fer.zemris.ppj.lab02;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Represents a grammar production. Apart from left and right side of the grammar production
 * this class also contains priority attribute which is used to resolve REDUCE/REDUCE conflict.
 * Smaller priority means that the production is defined earlier in the language specification
 * file.
 */
public class Production implements Serializable {
	
	private static final long serialVersionUID = 9065101540301779994L;
	
	private String leftSide;
    private ArrayList<String> rightSide;
    private int priority;

    public Production(String leftSide, ArrayList<String> rightSide, int priority) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
        this.priority = priority;
    }

    public String getLeftSide() {
        return leftSide;
    }

    public ArrayList<String> getRightSide() {
        return rightSide;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Production that = (Production) o;
        return leftSide.equals(that.leftSide) &&
                rightSide.equals(that.rightSide);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftSide, rightSide);
    }

    public String toString() {
        return priority + " " + leftSide + " -> " + rightSide;
    }
}
