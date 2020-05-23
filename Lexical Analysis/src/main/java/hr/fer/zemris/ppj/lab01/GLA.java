package hr.fer.zemris.ppj.lab01;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * GLA is the entry point of the lexical analyzer generator. Goal of generator is obviously to create needed items for
 * lexical analyzer. Firstly GLA creates an instance of EntryFileParser. When EntryFileParser finishes his job, GLA
 * serializes the items for lexical analyzer.
 * 
 * @author Pajser
 */
public class GLA {

	public static void main(String[] args) {
		EntryFileParser efp = new EntryFileParser();
        ArrayList<Rule> rules = efp.getLexicalAnalyzerRules();
        String[] lexicalAnalyzerStates = efp.getLexicalAnalyzerStatesArray();

		try {
		    //Serializing lexical analyzer rules
			FileOutputStream fileOut = new FileOutputStream("rules_serialized.txt");
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(rules);
			objectOut.flush();
			objectOut.close();

			//Serializing lexical analyzer states
			fileOut = new FileOutputStream("states_serialized.txt");
			objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(lexicalAnalyzerStates);
			objectOut.flush();
			objectOut.close();

		} catch(Exception e){
			System.err.println(e);
		}
	}
}
