package hr.fer.zemris.ppj.lab04;

import java.util.HashMap;
import java.util.Map;

/**
 * This class models a single node in {@link SymbolTableTree}.
 * <p>
 * Each node in the tree represents a different scope inside some ppjC program. Inside each node (scope),
 * there is a table of local declarations of that scope and also each node contains a reference to the parent scope.
 * <p>
 * That way, it is possible to easily check if variables or functions are declared in any scope, from the narrowest to widest.
 * 
 * @author Stena
 */
public class SymbolTableNode {
	
	/**
	 * This map is actually the table of local declarations in this node (scope).
	 * <p>
	 * It's keys are local variable and function names and the values are those variable's and function's data nodes.
	 */
    private Map<String, NodeData> declarationsTable = new HashMap<>();
    
    /**
     * A reference to the parent scope.
     */
    private SymbolTableNode parentBlock;
    
    /**
     * Constructs a new {@link SymbolTableNode} object with an empty local variables table.
     */
    private String parentFunctionName;
    private String parentFunctionType;
    private boolean returnTypeIsArray;
    private boolean inLoop;
    private long functionCounter = 0;

    public SymbolTableNode() {
	}
    
    /**
     * Constructs a new {@link SymbolTableNode} object, but with prefilled variables table and a reference
     * to the parent scope.
     * 
     * @param declarationsTable - prefilled local declarations table (variables and functions)
     * @param parentBlock - a reference to the parent scope
     */
    public SymbolTableNode(Map<String, NodeData> declarationsTable, SymbolTableNode parentBlock) {
        this.declarationsTable = declarationsTable;
        this.parentBlock = parentBlock;
    }

    public void addLocalDeclaration(String name, NodeData data) {
    	declarationsTable.put(name, data);
    }

    public NodeData getLocalDeclarationData(String name) {
        return declarationsTable.get(name);
    }

    public void setLocalDeclarationData(String name, NodeData data) {
    	declarationsTable.put(name, data);
    }
    
    public boolean hasLocalDeclaration(String name) {
    	return declarationsTable.containsKey(name);
    }

    public SymbolTableNode getParent() {
        return parentBlock;
    }

    public void setParent(SymbolTableNode parentBlock) {
        this.parentBlock = parentBlock;
    }

    public String getParentFunctionName() {
    	return parentFunctionName;
    }

    public boolean isInLoop() {
        return inLoop;
    }

    public void setInLoop(boolean inLoop) {
        this.inLoop = inLoop;
    }

    public void setParentFunctionName(String parentFunctionName) {
        this.parentFunctionName = parentFunctionName;
    }

    public String getParentFunctionType() {
        return parentFunctionType;
    }

    public void setParentFunctionType(String parentFunctionType) {
        this.parentFunctionType = parentFunctionType;
    }

    public boolean isReturnTypeArray() {
        return returnTypeIsArray;
    }

    public void setReturnTypeIsArray(boolean returnTypeIsArray) {
        this.returnTypeIsArray = returnTypeIsArray;
    }

    public long getFunctionCounter() {
        return functionCounter++;
    }
    
}
