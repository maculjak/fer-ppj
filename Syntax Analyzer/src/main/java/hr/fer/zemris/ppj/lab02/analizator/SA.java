package hr.fer.zemris.ppj.lab02.analizator;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.stream.Collectors;

import hr.fer.zemris.ppj.lab02.*;
import hr.fer.zemris.ppj.lab02.analizator.Tree.Node;

/**
 * 
 * 
 * @author Pajser
 */
public class SA {
	
	private static HashMap<Key<Integer>, Action> actions;
	private static HashMap<Key<Integer>, Integer> GOTO;
	
	private static Integer currentState;
	private static Integer startingState;
	
	private static String startingNonTerminal;
	private static ArrayList<String> synchronizingTerminals;
	
    @SuppressWarnings("unchecked")
	public static void main(String[] args) {
    	String code = "";
    	
    	try {
    		// Reading the items created by syntax analyzer generator
            HashMap<Key<Integer>, Action> actionTable = (HashMap<Key<Integer>, Action>) deSerializeObject("action_table.txt");
			HashMap<Key<Integer>, Integer> GOTOTable = (HashMap<Key<Integer>, Integer>) deSerializeObject("goto_table.txt");
            int startState = (int) deSerializeObject("start_state.txt");
            String startNonTerminal = (String) deSerializeObject("starting_non_terminal.txt");
            ArrayList<String> synchronizingSymbols = (ArrayList<String>) deSerializeObject("synchronizing_terminals.txt");
            
            actions = actionTable;
            GOTO = GOTOTable;
            startingState = startState;
            startingNonTerminal = startNonTerminal;
            synchronizingTerminals = synchronizingSymbols;
            
            // Reading the input code from the console
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            StringBuilder codeBuilder = new StringBuilder();
            
            while(true) {
                String line = br.readLine();
                if (line == null) break;
                codeBuilder.append(line).append("\n");
            }
            
            code = codeBuilder.toString();
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	currentState = startingState;
    	
    	List<String> codeLines = Arrays.stream(code.split("\n")).collect(Collectors.toList());
    	Stack<Node> parserStack = new Stack<>();
    	
    	Tree generativeTree = new Tree(currentState, startingNonTerminal);
    	parserStack.push(generativeTree.getRoot());

    	int i = 0;

    	while (true) {
    	    int lineNumber;
    	    String lexem;
    	    String lexicalUnit;

    	    if (i < codeLines.size()) {
                String[] lineTokens = codeLines.get(i).split(" ");
                lexem = lineTokens[0];
                lineNumber = Integer.parseInt(lineTokens[1]);
                lexicalUnit = "";
                for(int j = 2; j < lineTokens.length; j++) {
                	lexicalUnit += lineTokens[j];
                	if(j + 1 < lineTokens.length) {
                		lexicalUnit += " ";
                	}
                }
            } else {
    	        lineNumber = -1;
    	        lexem = "$";
    	        lexicalUnit = "$";
            }
    	    
    		// LR parser behavior simulator
    		while(true) {
    			// let s be the state on top of the stack
    			int s = parserStack.peek().getData().getState();
    			Key<Integer> currentActionKey = new Key<>(s, lexem);
    			Action currentAction = actions.get(currentActionKey);

    			// Error recovery
    			if(currentAction == null) {
    				System.err.println("Error at line: " + lineNumber);
    				
    				ArrayList<String> lexemsForS = (ArrayList<String>) actions.keySet().stream()
    						.filter(k -> k.getState().equals(s))
    						.map(k -> k.getCharacter())
    						.collect(Collectors.toList());
    				
    				System.err.print("The error wouldn't have been caused by: ");
    				
    				for(String listLexem : lexemsForS) {
    					Key<Integer> key = new Key<>(s, listLexem);
    					if(actions.get(key) != null) {
    						System.err.print(listLexem + " ");
    					}
    				}
    				
    				System.err.println();
    				System.err.print("Error at character: " + lexicalUnit);
    				
    				boolean synchFound = false;
    				String synchUnit = "";
    				for(int k = i; k < codeLines.size(); k++) {
    					String[] codeLineTokens = codeLines.get(k).split(" ");
    					String symbol = codeLineTokens[0];
    					
    					if(synchronizingTerminals.contains(symbol)) {
    						synchFound = true;
    						synchUnit = symbol;
    						break;
    					}
    					
    					i++;
    				}
    				
    				if(!synchFound) {
    					System.err.println("Unable to recover from the error");
    					return;
    				}
    				
    				while(!parserStack.isEmpty()) {
    					Key<Integer> tempKey = new Key<>(parserStack.peek().getData().getState(), synchUnit);
    					Action synchroAction = actions.get(tempKey);
    					
    					if(synchroAction != null) {
    						generativeTree.setRoot(parserStack.peek());
    						break;
    					}
    					
    					parserStack.pop();
    				}
    				
    				if(parserStack.isEmpty()) {
    					System.err.println("Unable to recover from the error");
    					return;
    				}
    				
    				break;
                } else if(currentAction.getType() == ActionType.SHIFT) {
    				Shift moveAction = (Shift) currentAction;
    				int toState = moveAction.getToState();
    				
    				Node newNode = new Node(new NodeData(toState, lexem, lineNumber, lexicalUnit));
    				parserStack.push(newNode);
    				i++;
    				break;
    			} else if(currentAction.getType() == ActionType.REDUCE) {
    				Reduce reduceAction = (Reduce) currentAction;
    				Production production = reduceAction.getProduction();
    				ArrayList<String> rightSide = production.getRightSide();
    				
    				ArrayList<Node> reducedNodes = new ArrayList<>();

    				if (!rightSide.get(0).equals("$")) {
    					for(int j = 0; j < rightSide.size() && !rightSide.get(0).equals("$"); j++) {
						    reducedNodes.add(parserStack.pop());
					    }
				    }

    				Node topNode = parserStack.peek();
    				String leftSide = production.getLeftSide();
    				
    				Key<Integer> currentNewStateKey = new Key<>(topNode.getData().getState(), leftSide);
    				int newState = GOTO.get(currentNewStateKey);
    				
    				Node newNode = new Node(new NodeData(newState, leftSide));
    				parserStack.push(newNode);

    				if(!reducedNodes.isEmpty()) {
    					newNode.getChildren().addAll(reducedNodes);
    				} else {
    					newNode.getChildren().add(new Node(new NodeData(newState, "", -1, "$")));
    				}
    				
    				generativeTree.setRoot(newNode);
    			} else if(currentAction.getType() == ActionType.ACCEPT) {
    				generativeTree.printTree();
    				return;
    			}
    		}
    	}
    }
    
    private static Object deSerializeObject(String path) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path))) {
            return in.readObject();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
