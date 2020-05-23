package hr.fer.zemris.ppj.lab02;

/**
 * Represents the SHIFT actions. Contains the next state to be put on
 * LR parser stack.
 */
public class Shift extends Action{

    private static final long serialVersionUID = -3603686683883489319L;

    int toState;

    public Shift(int toState) {
        super(ActionType.SHIFT);
        this.toState = toState;
    }

    public int getToState() {
        return toState;
    }

    @Override
    public String toString() {
        return "SHIFT " + toState;
    }

}
