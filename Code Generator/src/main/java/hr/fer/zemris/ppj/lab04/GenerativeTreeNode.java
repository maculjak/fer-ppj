package hr.fer.zemris.ppj.lab04;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenerativeTreeNode {
	
    private NodeData data;
    private GenerativeTreeNode parent;
    private List<GenerativeTreeNode> children = new ArrayList<>();
    private boolean isInLoop;

    public GenerativeTreeNode(GenerativeTreeNode parent, NodeData data) {
        this.parent = parent;
        this.data = data;
    }

    public GenerativeTreeNode getParent() {
        return parent;
    }

    public NodeData getData() {
        return data;
    }
    
    public void setData(NodeData data) {
        this.data = data;
    }

    public List<GenerativeTreeNode> getChildren() {
        return children;
    }

    public String getType() {
        return data.getType();
    }

    public void setType(String type) {
        data.setType(type);
    }

    public boolean islValue() {
        return data.islValue();
    }
    public void setlValue(boolean lValue) {
        data.setlValue(lValue);
    }

    public boolean isConstant() {
        return data.isConstant();
    }

    public void setConstant(boolean constant) {
        data.setConstant(constant);
    }

    public boolean isArray() {
        return data.isArray();
    }

    public void setArray(boolean array) {
        data.setArray(array);
    }

    public boolean isFunction() {
        return data.isFunction();
    }

    public void setFunction(boolean function) {
        data.setFunction(function);
    }
    
    public boolean isDefined() {
    	return data.isDefined();
    }
    
    public void setDefined(boolean defined) {
    	data.setDefined(defined);
    }

    public boolean isInLoop() {
        return isInLoop;
    }

    public void setInLoop(boolean inLoop) {
        isInLoop = inLoop;
    }

    public String getLexeme() {
        return data.getLexeme();
    }

    public int getLineNumber() {
        return data.getLineNumber();
    }

    public String getLexicalUnit() {
        return data.getLexicalUnit();
    }

    public void setLexeme(String lexeme) {
        this.data.setLexeme(lexeme);
    }

    public void setLexicalUnit(String lexicalUnit) {
        this.data.setLexicalUnit(lexicalUnit);
    }
    
    public String getName() {
        return data.getName();
    }

    public void setName(String name) {
        data.setName(name);
    }
    
    public int getNumberOfElements() {
    	return data.getNumberOfElements();
    }
    
    public void setNumberOfElements(int numberOfElements) {
    	data.setNumberOfElements(numberOfElements);
    }
    
    public List<NodeData> getFunctionArguments() {
    	return data.getFunctionArguments();
    }
    
    public void setFunctionArguments(List<NodeData> functionArguments) {
    	data.setFunctionArguments(functionArguments);
    }
    
    public void addFunctionArgument(NodeData functionArgument) {
    	data.addFunctionArgument(functionArgument);
    }
    
    public boolean containsFunctionArgument(NodeData functionArgument) {
		return data.containsFunctionArgument(functionArgument);
	}
    
    public List<String> getFunctionArgumentTypes() {
		return data.getFunctionArgumentTypes();
	}
	
	public List<String> getFunctionArgumentNames(){
		return data.getFunctionArgumentNames();
	}
    
    @Override
    public int hashCode() {
        return Objects.hash(children, data);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof GenerativeTreeNode))
            return false;
        GenerativeTreeNode other = (GenerativeTreeNode) obj;
        return Objects.equals(children, other.children) && Objects.equals(data, other.data);
    }
    
    @Override
    public String toString() {
        return data.getLexeme();
    }
    
}
