package core.handler;

import java.nio.channels.SelectableChannel;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.Pair;

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
    protected SocketHandler(LinkedList<Pair<ChannelType, SelectableChannel>> inputChannels,
                            LinkedList<Pair<ChannelType, SelectableChannel>> outputChannels,
                            S initState,
                            Com networkCom) {
        super(inputChannels, outputChannels, initState);

        this.networkCom = networkCom;
    }

}
