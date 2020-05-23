package hr.fer.zemris.ppj.lab02.analizator;

import java.util.ArrayList;
import java.util.HashSet;

import hr.fer.zemris.ppj.lab02.Item;

public class Tree {
	
	private Node root;
	
	public Tree(int startingState, String startingNonTerminal) {
		NodeData data = new NodeData(startingState, startingNonTerminal);
		this.root = new Node(data);
	}

	public void printTree() {
		dfsTreeTraversal(root, 0);
	}

	public void dfsTreeTraversal(Node node, int level) {
		for (int i = 0; i < level; i++) System.out.print(" ");

		System.out.println(node.data);

		for (int i = node.children.size() - 1; i >= 0; i--) {
			dfsTreeTraversal(node.children.get(i), level + 1);
		}
	}
	
	public Node getRoot() {
		return root;
	}

	public void setRoot(Node node) {
		this.root = node;
	}

	public static class Node {
		private NodeData data;
		private ArrayList<Node> children = new ArrayList<>();
		
		public Node(NodeData data) {
			this.data = data;
		}
		
		public NodeData getData() {
			return data;
		}

		public ArrayList<Node> getChildren() {
			return children;
		}

		@Override
		public String toString() {
			return data.getLexem();
		}
		
	}
	
}
