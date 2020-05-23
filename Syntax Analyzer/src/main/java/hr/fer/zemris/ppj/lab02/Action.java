package hr.fer.zemris.ppj.lab02;

import java.io.Serializable;

/**
 * Action class is used for modelling LR parser table actions. In its abstract version
 * Action has only one attribute - type which is defined by ActionType enumeration.
 * @see ActionType
 */

public abstract class Action implements Serializable {
	
	private static final long serialVersionUID = 6320678778683757897L;
	
	private ActionType type;

    public Action(ActionType type) {
        this.type = type;
    }

    public ActionType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
