package hr.fer.zemris.ppj.lab03;

import java.util.List;
import java.util.Objects;

public class Function {
    private String type;
    private boolean isArray;
    private String name;
    private List<NodeData> argumentTypes;

    public Function(String type, boolean isArray, String name, List<NodeData> argumentTypes) {
        this.type = type;
        this.isArray = isArray;
        this.name = name;
        this.argumentTypes = argumentTypes;
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
            if (!SemantickiAnalizator.canBeCast(a1.getType(), a2.getType())) return false;
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
}
