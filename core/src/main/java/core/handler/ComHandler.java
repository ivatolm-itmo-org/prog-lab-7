package core.handler;

import java.nio.channels.SelectableChannel;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Class for handling network communication logic of the application.
 *
 * @author ivatolm
 */
public abstract class ComHandler<S extends Enum<?>> extends Handler<ChannelType, S> {

    /**
     * Constructs new {@code ComHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param initState initial state of FSM
     */
    protected ComHandler(LinkedList<Pair<ChannelType, SelectableChannel>> inputChannels,
                         LinkedList<Pair<ChannelType, SelectableChannel>> outputChannels,
                         S initState) {
        super(inputChannels, outputChannels, initState);
    }

}
