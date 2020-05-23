package hr.fer.zemris.ppj.lab01;

public class PairOfStates {

    private int leftState;
    private int rightState;

    public PairOfStates(int leftState, int rightState) {
        this.leftState = leftState;
        this.rightState = rightState;
    }

    public int getLeftState() {
        return leftState;
    }

    public void setLeftState(int leftState) {
        this.leftState = leftState;
    }

    public int getRightState() {
        return rightState;
    }

    public void setRightState(int rightState) {
        this.rightState = rightState;
    }
}
