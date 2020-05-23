package hr.fer.zemris.ppj.lab04;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NodeData {

	private String lexeme;
	private int lineNumber;
	private String lexicalUnit;

	private String type;
	private String name;
	private boolean lValue;
	private boolean constant;
	private boolean array;
	private boolean function;
	private boolean isDefined;
	private boolean isGlobal;
	private int numberOfElements;
	
	private List<NodeData> functionArguments = new ArrayList<>();

	public NodeData(String lexeme, int lineNumber, String lexicalUnit) {
		this.lexeme = lexeme;
		this.lineNumber = lineNumber;
		this.lexicalUnit = lexicalUnit;
	}

	public NodeData() {
	}

	public String getLexeme() {
		return lexeme;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getLexicalUnit() {
		return lexicalUnit;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean islValue() {
		return lValue;
	}

	public void setlValue(boolean lValue) {
		this.lValue = lValue;
	}

	public boolean isConstant() {
		return constant;
	}

	public void setConstant(boolean constant) {
		this.constant = constant;
	}

	public boolean isArray() {
		return array;
	}

	public void setArray(boolean array) {
		this.array = array;
	}

	public boolean isFunction() {
		return function;
	}

	public void setFunction(boolean function) {
		this.function = function;
	}

	public boolean isDefined() {
		return isDefined;
	}

	public void setDefined(boolean isDefined) {
		this.isDefined = isDefined;
	}
	
	public List<NodeData> getFunctionArguments() {
		return functionArguments;
	}

	public void setFunctionArguments(List<NodeData> functionArguments) {
		this.functionArguments = functionArguments;
	}
	
	public void addFunctionArgument(NodeData functionArgument) {
		functionArguments.add(functionArgument);
	}
	
	public boolean containsFunctionArgument(NodeData functionArgument) {
		return functionArguments.contains(functionArgument);
	}
	
	public List<String> getFunctionArgumentTypes() {
		return functionArguments.stream()
				.map(nd -> nd.getType())
				.collect(Collectors.toList());
	}
	
	public List<String> getFunctionArgumentNames(){
		return functionArguments.stream()
				.map(nd -> nd.getName())
				.collect(Collectors.toList());
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getNumberOfElements() {
		return numberOfElements;
	}

	public void setNumberOfElements(int numberOfElements) {
		this.numberOfElements = numberOfElements;
	}

	public void setLexeme(String lexeme) {
		this.lexeme = lexeme;
	}

	public void setLexicalUnit(String lexicalUnit) {
		this.lexicalUnit = lexicalUnit;
	}

	@Override
	public int hashCode() {
		return Objects.hash(lexeme, lexicalUnit, lineNumber);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof NodeData))
			return false;
		NodeData other = (NodeData) obj;
		return Objects.equals(lexeme, other.lexeme) && Objects.equals(lexicalUnit, other.lexicalUnit)
				&& lineNumber == other.lineNumber;
	}

	@Override
	public String toString() {
		if(lexicalUnit.isEmpty()) {
			return lexeme;
		} else if(lexicalUnit.equals("$")) {
			return lexicalUnit;
		} else {
			return lexeme + " " + lineNumber + " " + lexicalUnit;
		}
	}

	public boolean isGlobal() {
		return isGlobal;
	}

	public void setGlobal(boolean global) {
		isGlobal = global;
	}
}
