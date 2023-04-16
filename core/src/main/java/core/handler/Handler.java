package core.handler;

import java.nio.channels.SelectableChannel;
import java.util.HashMap;

/**
 * Class for handling events.
 *
 * @author ivatolm
 */
public abstract class Handler<E extends Enum<?>> {

    // Input channels
    protected HashMap<E, SelectableChannel> inputChannels;

    // Output channels
    protected HashMap<E, SelectableChannel> outputChannels;

    /**
     * Constructs new {@code Handler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     */
    protected Handler(HashMap<E, SelectableChannel> inputChannels,
                      HashMap<E, SelectableChannel> outputChannels) {
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
