package hr.fer.zemris.ppj.lab04;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class reads the input file for semantic analyzer. The input file contains a valid
 * generative tree created by the syntactic analyzer. However, we need the tree in non-textual form
 * to be able to work with it more naturally (e.g. convert it to a custom Java object by parsing the file).
 * <p>
 * This class also implements mentioned functionality.
 * 
 * @author Pajser, Stena
 */
public class EntryFileParser {
	
	private GenerativeTree generativeTree;
	private Map<Integer, List<GenerativeTreeNode>> generativeTreeDepthTable = new HashMap<>();
	private Map<Integer, GenerativeTreeNode> lastNodeOnDepthTable = new HashMap<>();
	
	/**
	 * Constructs a new {@link EntryFileParser} object. The constructor reads the input file
	 * and updates the depth table after each line read. Afterwards, when the depth table is properly
	 * filled, the {@link GenerativeTree} object is constructed based on it.
	 */
	public EntryFileParser() {
		try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new BufferedInputStream(System.in), "UTF-8"));
            
            String line = br.readLine();
            while(line != null && !line.equals("")) {
            	updateDepthTable(line);
            	line = br.readLine();
            }
        } catch(IOException e){
            e.printStackTrace();
            return;
        }
		
		createGenerativeTree();
	}
	
	private void createGenerativeTree() {
		int currentDepth = 0;
		List<GenerativeTreeNode> nodesOnCurrentDepth = generativeTreeDepthTable.get(currentDepth);
		
		generativeTree = new GenerativeTree(nodesOnCurrentDepth.get(0));
		currentDepth++;
		
		while(true) {
			nodesOnCurrentDepth = generativeTreeDepthTable.get(currentDepth);
			if(nodesOnCurrentDepth != null)
				for(GenerativeTreeNode node : nodesOnCurrentDepth)
					node.getParent().getChildren().add(node);
			else break;
			currentDepth++;
		}
	}
	
	private void updateDepthTable(String line) {
		int depth = 0;
		while(depth < line.length() && line.charAt(depth) == ' ') {
			depth++;
		}
		
		String lexem = "";
		int lineNumber = -1;
		String lexicalUnit = "";
		if(line.charAt(depth) == '<') {
			lexem = line.substring(depth);
		} else {
			String[] lexemInfo = line.substring(depth).split(" ");
			lexem = lexemInfo[0];
			lineNumber = Integer.parseInt(lexemInfo[1]);
			if (lexemInfo[2].startsWith("\"")) lexicalUnit = line.substring(line.indexOf("\""), line.length());
			else lexicalUnit = lexemInfo[2];
		}
		
		GenerativeTreeNode newNode = new GenerativeTreeNode((depth == 0) ? null : lastNodeOnDepthTable.get(depth - 1), new NodeData(lexem, lineNumber, lexicalUnit));
		lastNodeOnDepthTable.put(depth, newNode);
		List<GenerativeTreeNode> nodeList = generativeTreeDepthTable.get(depth);
		if(nodeList == null) {
			List<GenerativeTreeNode> singleNodeList = new ArrayList<>();
			singleNodeList.add(newNode);
			generativeTreeDepthTable.put(depth, singleNodeList);
		} else {
			nodeList.add(newNode);
		}
	}
	
	/**
	 * Getter for the generative tree constructed from the input file.
	 * 
	 * @return a {@link GenerativeTree} object constructed from the input file
	 */
	public GenerativeTree getGenerativeTree() {
		return generativeTree;
	}
	
}
