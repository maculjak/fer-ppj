package hr.fer.zemris.ppj.lab02.analizator;

import java.util.HashSet;

import hr.fer.zemris.ppj.lab02.Item;

public class NodeData {
	
	private int state;
	private String lexem;
	private int lineNumber;
	private String lexicalUnit;
	
	public NodeData(int state, String lexem, int lineNumber, String lexicalUnit) {
		this.state = state;
		this.lexem = lexem;
		this.lineNumber = lineNumber;
		this.lexicalUnit = lexicalUnit;
	}
	
	public NodeData(int state, String lexem) {
		this(state, lexem, -1, "");
	}
	
	public NodeData(int state) {
		this(state, "");
	}
	
	public int getState() {
		return state;
	}

	public String getLexem() {
		return lexem;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public String getLexicalUnit() {
		return lexicalUnit;
	}

	@Override
	public String toString() {
		if(lexicalUnit.isEmpty()) {
			return lexem;
		} else if(lexicalUnit.equals("$")) {
			return lexicalUnit;
		} else {
			return lexem + " " + lineNumber + " " + lexicalUnit;
		}
	}
}
