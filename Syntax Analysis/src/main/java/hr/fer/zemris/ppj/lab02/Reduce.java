package hr.fer.zemris.ppj.lab02;

/**
 * Represents the reduce action. Contains the production which determines what
 * items will be removed from the LR parser stack.
 */
public class Reduce extends Action {
	
	private static final long serialVersionUID = 2341690754993702584L;
	
	private Production production;

    public Reduce(Production production) {
        super(ActionType.REDUCE);
        this.production = production;
    }

    public Production getProduction() {
        return production;
    }

    @Override
    public String toString() {
        return "REDUCE " + production.getPriority();
    }
    
}
