package hr.fer.zemris.ppj.lab03;

/**
 * This class models the code scope hierarchy in ppjC programs as a tree like data structure.
 * <p>
 * The tree contains a reference to its root node, which represents the global scope.
 * 
 * @author Stena
 */
public class SymbolTableTree {
	
	/**
	 * Root node of this tree, represents global scope in a program.
	 */
	private SymbolTableNode root;
	
	/**
	 * Constructs a new {@link SymbolTableTree} object.
	 * 
	 * @param root - root node for this tree which will represent the program's global scope
	 */
	public SymbolTableTree(SymbolTableNode root) {
		this.root = root;
	}
	
	public SymbolTableNode getRoot() {
		return root;
	}

	public void setRoot(SymbolTableNode node) {
		this.root = node;
	}
	
}
