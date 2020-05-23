package hr.fer.zemris.ppj.lab01;

public class RegularDefinition {

    private String name;
    private String expression;

    public RegularDefinition(String name, String expression) {
        this.name = name;
        this.expression = expression;
    }

    public String getName() {
        return name;
    }

    public String getExpression() {
        return expression;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return name + " " + expression;
    }
}
