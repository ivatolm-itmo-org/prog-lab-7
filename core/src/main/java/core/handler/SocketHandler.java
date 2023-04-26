package core.handler;

import java.nio.channels.SelectableChannel;
import java.util.HashMap;

import core.net.Com;

/**
 * Class for handling events of network socket.
 *
 * @author ivatolm
 */
public abstract class SocketHandler<C extends SelectableChannel, S extends Enum<?>> extends Handler<ChannelType, S> {

    /** Network comminicator */
    protected Com networkCom;

    /**
     * Constructs new {@code SocketHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param initState initial state of FSM
     * @param networkCom network communicator
     */
    protected SocketHandler(HashMap<ChannelType, SelectableChannel> inputChannels,
                            HashMap<ChannelType, SelectableChannel> outputChannels,
                            S initState,
                            Com networkCom) {
        super(inputChannels, outputChannels, initState);

        this.networkCom = networkCom;
    }

}
