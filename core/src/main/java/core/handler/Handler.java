package core.handler;

import java.nio.channels.SelectableChannel;
import java.util.HashMap;

import core.fsm.FSM;

/**
 * Class for handling events.
 *
 * @author ivatolm
 */
public abstract class Handler<E extends Enum<?>, S extends Enum<?>> extends FSM<S> {

    /** Input channels */
    protected HashMap<E, SelectableChannel> inputChannels;

    /** Output channels */
    protected HashMap<E, SelectableChannel> outputChannels;

    /**
     * Constructs new {@code Handler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param initState initial state of FSM
     */
    protected Handler(HashMap<E, SelectableChannel> inputChannels,
                      HashMap<E, SelectableChannel> outputChannels,
                      S initState) {
        super(initState);
        this.inputChannels = inputChannels;
        this.outputChannels = outputChannels;
    }

    /**
     * Method that is run by {@code Selector} when
     * some event occures on one input channels.
     *
     * @param channel ready channel identificator
     */
    public abstract void process(E channel);

    /**
     * Returns input channels that {@code Selector} will
     * catch events on.
     *
     * @return input channels of the handler
     */
    public HashMap<E, SelectableChannel> getInputChannels() {
        return this.inputChannels;
    }

}
