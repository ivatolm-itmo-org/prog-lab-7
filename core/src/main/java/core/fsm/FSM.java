package core.fsm;

/**
 * Class representing Finite State Machine.
 *
 * @author ivatolm
 */
public abstract class FSM<E extends Enum<?>> {

    // Current state
    private E state;

    /**
     * Constructs new {@code FSM} with provided arguments.
     *
     * @param initState initial state
     */
    protected FSM(E initState) {
        this.state = initState;
    }

    /**
     * Change current state to {@code state}.
     *
     * @param state next state
     */
    protected final void nextState(E state) {
        this.state = state;
    }

    /**
     * Returns current state
     *
     * @return current state
     */
    protected E getState() {
        return this.state;
    }

    /**
     * Goes through states while not handled all events.
     */
    protected abstract void handleEvents();

}
