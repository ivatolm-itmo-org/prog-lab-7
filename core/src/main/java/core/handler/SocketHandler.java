package core.handler;

import java.nio.channels.SelectableChannel;
import java.util.HashMap;

/**
 * Class for handling events of network socket.
 *
 * @author ivatolm
 */
public abstract class SocketHandler extends Handler<ChannelType> {

    /** Socket channel */
    protected SelectableChannel channel;

    /**
     * Constructs new {@code SocketHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     */
    protected SocketHandler(HashMap<ChannelType, SelectableChannel> inputChannels,
                            HashMap<ChannelType, SelectableChannel> outputChannels) {
        super(inputChannels, outputChannels);

        this.inputChannels.put(ChannelType.Socket, channel);
    }

    /**
     * Implements {@code process} for {@code Handler}.
     */
    @Override
    public abstract void process(ChannelType channel);

    protected abstract void send();

    protected abstract void receive();

}
