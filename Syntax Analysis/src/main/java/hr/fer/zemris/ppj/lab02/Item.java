package hr.fer.zemris.ppj.lab02;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents LR(1) item. LR(1) items consist of a grammar production, index of
 * currently processed character and set of lookahead characters. Lookahead characters
 * are used for filling the action table with REDUCE actions.
 */
public class Item implements Serializable {
	
	private static final long serialVersionUID = -7520219232448397496L;
	
	private int dotIndex;
    private Production production;
    private HashSet<String> lookaheads = new HashSet<>();

    public Item(int dotIndex, Production production) {
        this.dotIndex = dotIndex;
        this.production = production;
    }

    public Item(int dotIndex, Production production, HashSet<String> lookaheads) {
        this(dotIndex, production);
        this.lookaheads = lookaheads;
    }

    public int getDotIndex() {
        return dotIndex;
    }

    public Production getStringRep() {
        return production;
    }

    public HashSet<String> getLookaheads() {
        return lookaheads;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        
        return dotIndex == item.dotIndex && Objects.equals(production, item.production) && Objects.equals(lookaheads, item.lookaheads);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dotIndex, production, lookaheads);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(production.getLeftSide()).append(" -> ");
        ArrayList<String> rightSide = production.getRightSide();
        for(int i = 0; i < dotIndex; i++) sb.append(production.getRightSide().get(i)).append(" ");
        sb.append("♥");
        for(int i = dotIndex; i < rightSide.size(); i++) sb.append(rightSide.get(i)).append(" ");

        return sb.append(lookaheads).toString();
    }

    public ArrayList<String> getRightSide() {
        return new ArrayList<>(production.getRightSide());
    }

    public Item increaseDotIndex() {
        return new Item(this.dotIndex + 1, production, lookaheads);
    }

    public void addToLookaheads(String symbol) {
        lookaheads.add(symbol);
    }

    public void setLookaheads(HashSet<String> lookaheads) {
        this.lookaheads = lookaheads;
    }

    public void addEmptyLookahead() {
        addToLookaheads("$");
    }

    public String getLeftSide() {
        return production.getLeftSide();
    }

    public Production getProduction() {
        return production;
    }

    public String getSymbolAtDot() {
        return production.getRightSide().get(dotIndex);
    }
}
