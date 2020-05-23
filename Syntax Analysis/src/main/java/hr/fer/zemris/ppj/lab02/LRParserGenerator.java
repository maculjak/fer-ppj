package hr.fer.zemris.ppj.lab02;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class contains all the methods needed for creation of LR parser table. The table
 * consists of two separate hash maps: actions and GOTO. Rows of actions table are states
 * of the DFA that is generated from grammar productions and columns are the characters.
 * Characters and states are combined in Key class. The second table is GOTO. That table
 * is used when REDUCE action is performed in order to determine the next state that will
 * to be put on the LR parser stack. Rows and colums are states and characters, respectively.
 * @see Key
 */
public class LRParserGenerator {

	private Grammar grammar;

	private HashMap<Key<Integer>, Action> actions = new HashMap<>();
	private HashMap<Key<Integer>, Integer> GOTO = new HashMap<>();

	private HashSet<HashSet<Item>> canonicalCollection = new HashSet<>();
	private DFA<HashSet<Item>> dfa;

	private HashSet<HashSet<Item>> canBeReduced = new HashSet<>();
	private HashSet<HashSet<Item>> canBeAccepted = new HashSet<>();

	private HashMap<HashSet<Item>, Integer> stateIndexMap = new HashMap<>();

    private enum ItemForm {
	    AAlphaDOTaBeta, AAlphaDOT, GinnungagapSDOT;
    }

	public LRParserGenerator(Grammar grammar) {
		this.grammar = grammar;
		
		createCanonicalCollection();
		createTable();
	}

	private void createCanonicalCollection() {
		Item start = new Item(0, grammar.getGinnungagapProduction());
		start.addEmptyLookahead();
		this.dfa = new DFA<>(closure(start));
		canonicalCollection.add(closure(start));

		int index = 0;
		stateIndexMap.put(closure(start), index++);

		HashSet<HashSet<Item>> currentStates = new HashSet<>(canonicalCollection);
		boolean change = true;
        while(change) {
			change = false;
			HashSet<HashSet<Item>> nextStates = new HashSet<>();

			for (HashSet<Item> state : currentStates) {
				for (String symbol : grammar.getSymbols()) {
                    HashSet<Item> stateToAdd = GOTO(state, symbol);
                    
					if (!stateToAdd.isEmpty()) {
						dfa.addTransition(state, stateToAdd, symbol);
					}

                    if (!canonicalCollection.contains(stateToAdd) && !stateToAdd.isEmpty()) {
                    	dfa.addToFinalStates(stateToAdd);
                        nextStates.add(stateToAdd);

                        if (!stateIndexMap.containsKey(stateToAdd)) stateIndexMap.put(stateToAdd, index++);
                        change = true;
                    }
				}
			}

			canonicalCollection.addAll(nextStates);
			currentStates = nextStates;
		}
	}

	private void createTable() {
	    dfa.getTransitionFunction().forEach((k,v) -> {
	        if (!grammar.isNonTerminal(k.getCharacter()))
	        	actions.put(new Key<>(stateIndexMap.get(k.getState()), k.getCharacter())
			        , new Shift(stateIndexMap.get(dfa.getNextState(k))));

	        else GOTO.put(new Key<>(stateIndexMap.get(k.getState()), k.getCharacter())
			        , stateIndexMap.get(dfa.getNextState(k)));
	    });

	    canBeReduced.forEach(state -> {
	        for (Item i : state) {
	        	if (itemForm(i) != ItemForm.AAlphaDOT) continue;
	            HashSet<String> lookaheads = i.getLookaheads();

	            for (String s : lookaheads) {
	                Key<Integer> key = new Key<>(stateIndexMap.get(state), s);

	                if (!actions.containsKey(key)) actions.put(key, new Reduce(i.getProduction()));
	                else if (actions.get(key).getType() == ActionType.REDUCE) {
	                    Reduce r = (Reduce) actions.get(key);
	                    if (i.getProduction().getPriority() < r.getProduction().getPriority()) {
	                        actions.remove(key);
	                        actions.put(key, new Reduce(i.getProduction()));
					    }
				    }
			    }
		    }
	    });

	    canBeAccepted.forEach(state -> {
	        actions.put(new Key<>(stateIndexMap.get(state), "$"), new Accept());
	    });
	}

    private ItemForm itemForm(Item item) {
        if (item.getDotIndex() == item.getRightSide().size() || item.getRightSide().get(0).equals("$")){
        	if (item.getLeftSide().equals("<Ginnungagap>")) return ItemForm.GinnungagapSDOT;
        	return ItemForm.AAlphaDOT;
	    }

        else return ItemForm.AAlphaDOTaBeta;
    }

	private HashSet<Item> closure(HashSet<Item> item) {
		boolean change = true;
		boolean reduceable = false;
		boolean acceptable = false;

		HashSet<Item> closure = new HashSet<>(item);
		HashSet<Item> current = new HashSet<>(closure);

		while(change) {
			change = false;
			HashSet<Item> nextItems = new HashSet<>();

			for (Item i : current) {
				Production production = i.getProduction();

				if (itemForm(i) == ItemForm.AAlphaDOT) reduceable = true;
				else if (itemForm(i) == ItemForm.GinnungagapSDOT) acceptable = true;

				int dotIndex = i.getDotIndex();
				if (dotIndex >= production.getRightSide().size()) continue;

				String symbol = i.getSymbolAtDot();

				if (grammar.isNonTerminal(symbol)) {
					ArrayList<Production> productions = grammar.getProductions(symbol);

					for (Production p : productions) {
						ArrayList<String> string;

						if (dotIndex < production.getRightSide().size()) {
							string = new ArrayList<>(production.getRightSide()
									.subList(dotIndex + 1, production.getRightSide().size()));
						} else string = new ArrayList<>();

						HashSet<String> newLookaheads = new HashSet<>(grammar.FIRST(string));

						if (string.isEmpty() || grammar.generatesEmptySequence(string))
							newLookaheads.addAll(i.getLookaheads());

						Item next = new Item(0, p, newLookaheads);

						if (!closure.contains(next)) {
                            nextItems.add(next);
							change = true;
						}
					}
				}
			}

			closure.addAll(nextItems);
			current.clear();
			current.addAll(nextItems);
		}
		if (reduceable) canBeReduced.add(closure);
		else if (acceptable) canBeAccepted.add(closure);

		return closure;
	}

	private HashSet<Item> closure(Item item) {
		HashSet<Item> temp = new HashSet<>();
		temp.add(item);

		return closure(temp);
	}

	private HashSet<Item> GOTO(HashSet<Item> items, String symbol) {
		HashSet<Item> j = new HashSet<>();

		items.stream().filter(i -> i.getDotIndex() < i.getRightSide().size() && i.getSymbolAtDot().equals(symbol))
				.forEach(i -> j.add(i.increaseDotIndex()));

		return closure(j);
	}

	public HashMap<Key<Integer>, Action> getActions() {
		return actions;
	}

	public HashMap<Key<Integer>, Integer> getGOTO() {
		return GOTO;
	}

	public HashMap<HashSet<Item>, Integer> getStateIndexMap() {
		return stateIndexMap;
	}
	
	public int getStartState() {
		return stateIndexMap.get(dfa.getStartState());
	}
	
}
