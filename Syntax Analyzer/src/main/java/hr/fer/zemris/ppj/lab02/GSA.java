package hr.fer.zemris.ppj.lab02;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * GSA is the entry point of syntax analyzer generator. GSA by itself calls the methods that
 * generate LR parser table and serializes necessary items for the syntax analyzer.
 * @author Bruno
 */
public class GSA {

	public static void main(String[] args) {
		EntryFileParser fileParser = new EntryFileParser();

		ArrayList<String> nonTerminals = fileParser.getNonTerminals();
		Grammar grammar = fileParser.getGrammar();
		String startingNonTerminal = fileParser.getStartingNonTerminal();

		String newStartingNonTerminal = "<Ginnungagap>";
		nonTerminals.add(newStartingNonTerminal);

		ArrayList<String> oldStartingNonTerminal = new ArrayList<>();
		oldStartingNonTerminal.add(startingNonTerminal);

		grammar.add(newStartingNonTerminal, oldStartingNonTerminal);
		grammar.setGinnungagapProduction(new Production(newStartingNonTerminal, oldStartingNonTerminal, -1));
		grammar.calculateFirstSets();

		LRParserGenerator lrParserGenerator = new LRParserGenerator(grammar);

		try {
			//Serializing the action table for LR Parser
			FileOutputStream fileOut = new FileOutputStream("action_table.txt");
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(lrParserGenerator.getActions());
			objectOut.flush();
			objectOut.close();

			//Serializing the GOTO table for LR Parser
			fileOut = new FileOutputStream("goto_table.txt");
			objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(lrParserGenerator.getGOTO());
			objectOut.flush();
			objectOut.close();

			// Serializing the start state of DFA for LR Parser
			fileOut = new FileOutputStream("start_state.txt");
			objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(lrParserGenerator.getStartState());
			objectOut.flush();
			objectOut.close();

			// Serializing the starting non terminal for LR Parser
			fileOut = new FileOutputStream("starting_non_terminal.txt");
			objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(startingNonTerminal);
			objectOut.flush();
			objectOut.close();
			
			// Serializing the synchronizing symbols for LR parser
			fileOut = new FileOutputStream("synchronizing_terminals.txt");
			objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(fileParser.getSynchronisingTerminals());
			objectOut.flush();
			objectOut.close();
		} catch(Exception e){
			System.err.println(e);
		}
	}

}
