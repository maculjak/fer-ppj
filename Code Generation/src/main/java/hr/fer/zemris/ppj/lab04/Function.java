package hr.fer.zemris.ppj.lab04;

import java.util.*;

public class Function {
	
    private String type;
    private boolean isArray;
    private String name;
    private List<NodeData> argumentTypes;
    private StringBuilder functionBuilder;
    private String firstInstruction;
    private Map<String, Integer> parameterOffsetMap = new HashMap<>();
    private Map<String, Integer> localVariablesOffsetMap = new HashMap<>();
    private int stackPointerOffset = 4;
    private String label = null;
    private static int mulIndex = 0;
    private static int divIndex = 0;
    private static int modIndex = 0;
    private List<String> memoryLocalVariables = new ArrayList<>();
    
    public Function(String type, boolean isArray, String name, List<NodeData> argumentTypes) {
        this.type = type;
        this.isArray = isArray;
        this.name = name;
        this.argumentTypes = argumentTypes;
        this.functionBuilder = new StringBuilder();
    }

    public Function() {
        this.functionBuilder = new StringBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Function function = (Function) o;
        int args = argumentTypes.size();
        if (args != function.argumentTypes.size()) return false;
        for (int i = 0; i < args; i++) {
            NodeData a1 = argumentTypes.get(i);
            if (function.argumentTypes.size() == 0) return false;
            NodeData a2 = function.argumentTypes.get(i);
            if (!GeneratorKoda.canBeCast(a1.getType(), a2.getType())) return false;
            if (a1.isArray() != a2.isArray()) return false;
        }
        return isArray == function.isArray &&
                Objects.equals(type, function.type) &&
                Objects.equals(name, function.name) &&
                Objects.equals(argumentTypes, function.argumentTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, isArray, name, argumentTypes);
    }

    public String getCode() {
        return functionBuilder.toString();
    }

    public void appendCode(String code) {
        if (firstInstruction == null) firstInstruction = code;
        else {
            if (label == null) functionBuilder.append(formatLine(code));
            else {
                functionBuilder.append(formatLine(code, label));
                label = null;
            }
        }
    }

    public String getType() {
        return type;
    }

    public boolean isArray() {
        return isArray;
    }

    public String getName() {
        return name;
    }

    public List<NodeData> getArgumentTypes() {
        return argumentTypes;
    }

    public StringBuilder getFunctionBuilder() {
        return functionBuilder;
    }

    public String getFirstInstruction() {
        return firstInstruction;
    }

    /**
     * Appends <code>PUSH R{index}</code>
     * @param index index of register to push to the stack
     */
    public void pushR(int index) {
        appendCode("PUSH R" + index);
        stackPointerOffset += 4;
    }

    /**
     * Appends <code>LOAD R{index}, ({label})</code>
     * @param index index of register to move the value to
     * @param label label to load from
     */
    public void loadR(int index, String label) {
        appendCode("LOAD R" + index + ", " + addBracketsToLabel(label));
    }
    
    /**
     * Appends <code>MOVE %D {value}, R{index}</code>
     * @param value value to be moved to register of given index
     * @param index index of register to move the value to
     */
    public void move(int value, int index) {
        if (label == null) appendCode("MOVE %D " + value + ", R" + index);
        else {
            functionBuilder.append(formatLine("MOVE %D " + value + ", R" + index, label));
            label = null;
        }
    }

    public void moveR(int i1, int i2) {
        appendCode("MOVE R" + i1 + ", " + "R" + i2);
    }

    public void shlR(int index, int value) {
        appendCode("SHL R" + index + ", " + value + ", R" + index);
    }

    public void moveLabel(String label, int index) {
        appendCode("MOVE " + label + ", R" + index);
    }

    /**
     * Appends <code>POP R{index}</code>
     * @param index index of register to put the value on top of the stack
     */
    public void popR(int index) {
        appendCode("POP R" + index);
        stackPointerOffset -= 4;
    }

    /**
     * Appends <code>STORE R{index}, ({label})</code>
     * @param index index of register whose value is to be stored
     * @param label label of memory location to store the value to
     */
    public void storeR(int index, String label) {
        if (this.label == null) appendCode("STORE R" + index + ", " + addBracketsToLabel(label));
        else {
            functionBuilder.append(formatLine("STORE R" + index + ", " + addBracketsToLabel(label), label));
            this.label = null;
        }
    }

    public void ALU(String OP, int index, int number) {
        if (label == null) appendCode(OP + " R" + index + ", %D " + number + ", R" + index);
        else {
            functionBuilder.append(formatLine(OP + " R" + index + ", %D " + number + ", R" + index, label));
            label = null;
        }
    }

    public void ALU(String OP, int i1, int i2, int i3) {
        if (label == null) appendCode(OP + " R" + i1 + ", R" + i2 + ", R" + i3);
        else {
            functionBuilder.append(formatLine(OP + " R" + i1 + ", R" + i2 + ", R" + i3, label));
            label = null;
        }

    }

    public void xorR(int index, int number) {
        ALU("XOR", index, number);
    }

    public void addR(int index, int number) {
        ALU("ADD", index, number);
    }

    public void andR(int index, int number) {
    	ALU("AND", index, number);
    }

    public void orrR(int index, int number) {
    	ALU("OR", index, number);
    }

    public void subR(int index, int number) {
    	ALU("SUB", index, number);
    }

    public void addR(int i1, int i2, int i3) {
        ALU("ADD", i1, i2, i3);
    }

    public void subR(int i1, int i2, int i3) {
        ALU("SUB", i1, i2, i3);
    }

    public void orR(int i1, int i2, int i3) {
        ALU("OR", i1, i2, i3);
    }

    public void xorR(int i1, int i2, int i3) {
        ALU("XOR", i1, i2, i3);
    }

    public void andR(int i1, int i2, int i3) {
        ALU("AND", i1, i2, i3);
    }

    public void loadR(int index, int offset) {
        memory("LOAD", index, offset);
    }

    public void storeR(int index, int offset) {
        memory("STORE", index, offset);
    }

    public void cmp0(int index) {
        if (label == null) appendCode("CMP " + "R" + index + ", 0");
        else {
            formatLine("CMP " + "R" + index + ", 0", label);
            label = null;
        }
    }

    public void cmp1(int index) {
        if (label == null) appendCode("CMP " + "R" + index + ", 1");
        else {
            formatLine("CMP " + "R" + index + ", 1", label);
            label = null;
        }
    }

    public void mulR() {
        andR(2, 0);
        cmp0(0);
        j("==", "END");
        cmp0(1);
        j("==", "END");

        setLabel("MUL" + mulIndex);
        addR(0, 2, 2);
        subR(1, 1);
        cmp0(1);
        j("!=", "MUL" + mulIndex);
        setLabel("END" + mulIndex);
        moveR(2, 0);
        mulIndex++;
    }

    public void modR() {
        move(-1, 2);
        setLabel("MOD" + modIndex);
        addR(2, 1, 2);
        subR(1, 0, 1);
        j("+", "MOD" + modIndex);
        addR(1, 0, 1);
        moveR(1, 0);
        modIndex++;
    }

    public void divR() {
        moveR(0, 2);
        setLabel("DIV" + divIndex);
        subR(1, 0, 1);
        j("<", "END" + divIndex);
        addR(2, 1);
        jp("DIV" + divIndex);
        setLabel("END" + divIndex);
        moveR(2, 0);
        divIndex++;
    }

    public void cmpR(int i1, int i2) {
        appendCode("CMP " + "R" + i1 + ", " + "R" + i2);
    }

    public void j(String condition, String label) {
        switch (condition) {
            case "<":
                condition = "SLT";
                break;
            case ">":
                condition = "SGT";
                break;
            case "<=":
                condition = "SLE";
                break;
            case ">=":
                condition = "SGE";
                break;
            case "==":
                condition = "EQ";
                break;
            case "!=":
                condition = "NE";
                break;
            case "+":
                condition = "P";
                break;
        }
        appendCode("JP_" + condition + " " + label);
    }

    public void jeq(String label) {appendCode("JP_EQ " + label);}

    private void memory(String OP, int index, int offset) {
        String hexVal = getHexVal(stackPointerOffset - offset);
        if (label == null) appendCode(OP + " R" + index + ", " + addBracketsToLabel("R7 + " + hexVal));
        else {
            functionBuilder.append(formatLine(OP + " R" + index + ", " + addBracketsToLabel("R7 + " + hexVal), label));
            label = null;
        }
    }

    public void jp(String label) {appendCode("JP " + label);}

    public String getHexVal(int number) {
        String hexVal = Integer.toHexString(number).toUpperCase();
        switch (hexVal) {
            case "A":
            case "B":
            case "C":
            case "D":
            case "E":
            case "F":
                hexVal = "0" + hexVal;
        }
        return hexVal;
    }

    public void call(String function) {
        if (label == null) appendCode("CALL F_" + function.toUpperCase());
        else {
            functionBuilder.append(formatLine("CALL F_" + function.toUpperCase(), label));
            label = null;
        }
        stackPointerOffset -= 4;
    }

    public void ret() {
        if (localVariablesOffsetMap.size() > 0) {
            addR(7, stackPointerOffset - 4 + memoryLocalVariables.size() * 4);
            stackPointerOffset = 4;
        }

        appendCode("RET");
    }

    private String formatLine(String instruction) {
        return formatLine(instruction, null);
    }

    private static String addBracketsToLabel(String label) {
        return "(" + label + ")";
    }

    private String formatLine(String instruction, String label){
        StringBuilder spaceBuilder = new StringBuilder();

        int labelSize = label == null ? 0 : label.length();

        for (int i = 0; i < 16 - labelSize; i++) spaceBuilder.append(" ");

        return (label == null ? "\t\t\t\t" : label + spaceBuilder.toString()) + instruction + "\n";
    }

    public int getStackPointerOffset(String variable) {
        if (localVariablesOffsetMap.containsKey(variable)) return localVariablesOffsetMap.get(variable);
        else if (parameterOffsetMap.containsKey(variable)) return parameterOffsetMap.get(variable);
        return -1;
    }

    public void addStackPointerOffset(String variable) {
        parameterOffsetMap.put(variable, stackPointerOffset);
        stackPointerOffset += 4;
    }

    public boolean isLocalVariableOrParameter(String variable) {
        return parameterOffsetMap.containsKey(variable) || localVariablesOffsetMap.containsKey(variable);
    }

    public void addLocalVariable(String variable) {
        localVariablesOffsetMap.put(variable, stackPointerOffset);
    }

    public int localVariablesMapSize() {
        return parameterOffsetMap.size();
    }

    public void updateStackPointerOffset(int value) {
        stackPointerOffset += value;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getStackPointerOffset() {
        return stackPointerOffset;
    }

    public void setStackPointerOffset(int stackPointerOffset) {
        this.stackPointerOffset = stackPointerOffset;
    }

    public void addLocalVariableToMemory(String variable) {
        memoryLocalVariables.add(variable);
    }

    public boolean isVariableInMemory(String variable) {
        return memoryLocalVariables.contains(variable);
    }

    public boolean isParameter(String variable) {
        return parameterOffsetMap.containsKey(variable);
    }
}
