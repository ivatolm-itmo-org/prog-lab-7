package core.handler;

import java.nio.channels.SelectableChannel;
import java.util.HashMap;

/**
 * Class for handling network communication logic of the application.
 *
 * @author ivatolm
 */
public abstract class ComHandler extends Handler<ChannelType> {

    /**
     * Constructs new {@code ComHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     */
    protected ComHandler(HashMap<ChannelType, SelectableChannel> inputChannels,
                         HashMap<ChannelType, SelectableChannel> outputChannels) {
        super(inputChannels, outputChannels);
    }

}
