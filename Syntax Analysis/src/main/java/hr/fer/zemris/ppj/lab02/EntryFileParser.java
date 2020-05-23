package hr.fer.zemris.ppj.lab02;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class is used for parsing language specification file
 * @author Bruno
 */
public class EntryFileParser {

    private ArrayList<String> nonTerminals;
    private ArrayList<String> terminals;
    private ArrayList<String> synchronisingTerminals;
    
    private String startingNonTerminal;
    
    private Grammar grammar;

    public EntryFileParser() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new BufferedInputStream(System.in), "UTF-8"));

            nonTerminals = new ArrayList<>(Arrays.asList(br.readLine().substring(3).split(" ")));
            startingNonTerminal = nonTerminals.get(0);
            terminals = new ArrayList<>(Arrays.asList(br.readLine().substring(3).split(" ")));
            synchronisingTerminals = new ArrayList<>(Arrays.asList(br.readLine().substring(5).split(" ")));
            grammar = new Grammar(nonTerminals, terminals);
            
            String leftSide = "";
            String line = br.readLine();
            while(line != null && !line.equals("")) {
                leftSide = line;
                line = br.readLine();

                while(line.startsWith(" ")) {
                    ArrayList<String> singleRightSide = new ArrayList<>(Arrays.asList(line.substring(1).split(" ")));
                    grammar.add(leftSide, singleRightSide);
                    line = br.readLine();
                    if (line == null) break;
                }
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public ArrayList<String> getNonTerminals() {
        return nonTerminals;
    }

    public ArrayList<String> getTerminals() {
        return terminals;
    }

    public ArrayList<String> getSynchronisingTerminals() {
        return synchronisingTerminals;
    }

    public String getStartingNonTerminal() {
        return startingNonTerminal;
    }

    public Grammar getGrammar() {
        return grammar;
    }
}
