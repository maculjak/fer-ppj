package hr.fer.zemris.ppj.lab02;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

/**
 * Represents the grammar. To define a grammar we need to specify terminals, nonterminals and
 * grammar productions. This class also contains the methods that provide other objects with
 * the information about the grammar.
 * 
 * @author Bruno
 */
public class Grammar {
	
    private ArrayList<String> nonTerminals;
    private ArrayList<String> terminals;
    private ArrayList<String> symbols = new ArrayList<>();

    private ArrayList<Production> productions = new ArrayList<>();

    private int priority;
    private boolean hasEpsilonProductions = false;
    
    private Production ginnungagapProduction;
    
    public HashMap<String, HashSet<String>> firstSets = new HashMap<>();

    public Grammar(ArrayList<String> nonTerminals, ArrayList<String> terminals) {
    	this.nonTerminals = nonTerminals;
        this.terminals = terminals;
        
        symbols.addAll(nonTerminals);
        symbols.addAll(terminals);
    }
    
    public void add(String leftSide, ArrayList<String> rightSide) {
        if(rightSide.contains("$")) {
        	hasEpsilonProductions = true;
        }
        
        Production production = new Production(leftSide, rightSide, priority++);
        productions.add(production);
    }

    private HashSet<String> calculateFirst(ArrayList<String> characters, int index) {
        HashSet<String> FIRST = new HashSet<>();

        if (index == characters.size()) return FIRST;

        String currentCharacter = characters.get(index);

        if (terminals.contains(currentCharacter)) {
            FIRST.add(currentCharacter);
            return FIRST;
        }

        if (nonTerminals.contains(currentCharacter)) FIRST.addAll(firstSets.get(currentCharacter));
        if (generatesEmptySequence(currentCharacter)) FIRST.addAll(calculateFirst(characters, index + 1));

        return FIRST;
    }

    /**
     * Generates FIRST sets for each symbol of the grammar.
     */
    public void calculateFirstSets() {
        firstSets = new HashMap<>();

        for (String s : nonTerminals) {
            HashSet<String> temp = new HashSet<>();
            firstSets.put(s, temp);
        }

        boolean change;
        do {
            change = false;
            for (String nonTerminal : nonTerminals) {
                HashSet<String> FIRST = new HashSet<>();
                for (Production rule : productions) {
                    if (rule.getLeftSide().equals(nonTerminal)) {
                        FIRST.addAll(calculateFirst(rule.getRightSide(), 0));
                    }
                }

                if (!firstSets.get(nonTerminal).containsAll(FIRST)) {
                    change = true;
                    firstSets.get(nonTerminal).addAll(FIRST);
                }

            }
        } while (change);
    }

    /**
     *
     * @return all nonterminals whose reflexive-transitive environment is empty string
     */
    public ArrayList<String> emptyNonTerminals() {
        if(!hasEpsilonProductions) return null;
        HashSet<String> emptyCharacters = new HashSet<>();

        boolean noChangeFlag;
        emptyCharacters.add("$");

        do {
            noChangeFlag = true;

            for(Production production : productions) {
                if (production.getRightSide().contains("$")) {
                    emptyCharacters.add(production.getLeftSide());
                    noChangeFlag = false;
                }

                boolean emptyFlag = true;
                for(String character : production.getRightSide()) {
                    if(!emptyCharacters.contains(character)) {
                        emptyFlag = false;
                        break;
                    }
                }

                if(emptyFlag) {
                    emptyCharacters.add(production.getLeftSide());
                    noChangeFlag = false;
                }
            }

        } while(noChangeFlag);

        emptyCharacters.remove("$");
        return new ArrayList<>(emptyCharacters);
    }

    /**
     *
     * @param symbol
     * @return first set of a symbol
     */

    public HashSet<String> FIRST(String symbol) {
        HashSet<String> FIRST = new HashSet<>();

        if(terminals.contains(symbol)) {
            FIRST.add(symbol);
            return FIRST;
        }
        
        FIRST.addAll(firstSets.get(symbol));
        return FIRST;
    }

    /**
     *
     * @param rightSide
     * @return first set of a string of characters at the right side of a grammar production
     */

    public HashSet<String> FIRST(ArrayList<String> rightSide) {
        HashSet<String> FIRST = new HashSet<>();

        for(int i = 0; i < rightSide.size(); i++) {
            String symbol = rightSide.get(i);

            if(!hasEpsilonProductions || !generatesEmptySequence(symbol)){
                    FIRST.addAll(FIRST(symbol));
                    return FIRST;
            }

            FIRST.addAll(FIRST(symbol));
        }

        return FIRST;
    }

    public ArrayList<String> getSymbols() {
        return symbols;
    }

    public ArrayList<Production> getProductions() {
        return productions;
    }

    public boolean isNonTerminal(String symbol) {
        return nonTerminals.contains(symbol);
    }

    public boolean generatesEmptySequence(ArrayList<String> stringArray) {
        if(stringArray.isEmpty()) return true;

        ArrayList<String> emptyNonTerminals = emptyNonTerminals();

        if(emptyNonTerminals == null) return false;
        for(String symbol : stringArray) {
        	if(!emptyNonTerminals.contains(symbol)) return false;
        }

        return true;
    }

    public boolean generatesEmptySequence(String character) {
        ArrayList<String> temp = new ArrayList<>();
        temp.add(character);

        return generatesEmptySequence(temp);
    }

    public ArrayList<Production> getProductions(String nonTerminal) {
        ArrayList<Production> temp = new ArrayList<>();

        for(Production production : productions) if(production.getLeftSide().equals(nonTerminal)) temp.add(production);

        return temp;
    }

    public Production getGinnungagapProduction() {
        return ginnungagapProduction;
    }

    public void setGinnungagapProduction(Production ginnungagapProduction) {
        this.ginnungagapProduction = ginnungagapProduction;
    }
}
