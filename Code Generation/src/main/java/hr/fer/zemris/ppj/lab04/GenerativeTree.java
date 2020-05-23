package hr.fer.zemris.ppj.lab04;

public class GenerativeTree {
	
	private GenerativeTreeNode root;
	
	public GenerativeTree(GenerativeTreeNode root) {
		this.root = root;
	}

	public void printTree() {
		dfsTreeTraversal(root, 0);
	}

	public void dfsTreeTraversal(GenerativeTreeNode node, int level) {
		for (int i = 0; i < level; i++) System.out.print(" ");

		System.out.println(node.getData());

		for (int i = 0; i < node.getChildren().size(); i++) {
			dfsTreeTraversal(node.getChildren().get(i), level + 1);
		}
	}
	
	public GenerativeTreeNode getRoot() {
		return root;
	}

	public void setRoot(GenerativeTreeNode node) {
		this.root = node;
	}

}
