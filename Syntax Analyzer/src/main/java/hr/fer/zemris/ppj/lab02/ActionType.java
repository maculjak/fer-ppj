package hr.fer.zemris.ppj.lab02;

/**
 * This enumeration holds three values SHIFT, ACCEPT and REDUCE, corresponding to three out of four possible
 * LR parser action types (it is not necessary to cover ERROR type as in our table fields corresponding to
 * ERROR actions are empty).
 */
public enum ActionType {
    SHIFT, ACCEPT, REDUCE
}
