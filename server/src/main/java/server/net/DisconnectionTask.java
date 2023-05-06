package server.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.Pipe;
import java.util.TimerTask;

import core.event.Event;
import core.event.EventType;
import core.utils.NBChannelController;

/**
 * Timer task for disconnecting clients if timeout was exceeded.
 *
 * @author ivatolm
 */
public class DisconnectionTask extends TimerTask {

    // Socket handler pipe
    private Pipe.SinkChannel sinkPipe;

    // Client's address
    private SocketAddress address;

    /**
     * Constructs new {@code DisconnectionTask} with provided arguments.
     *
     * @param sinkPipe socket handler pipe
     * @param address client's address
     */
    public DisconnectionTask(Pipe.SinkChannel sinkPipe, SocketAddress address) {
        this.sinkPipe = sinkPipe;
        this.address = address;
    }

    @Override
    public void run() {
        Event event = new Event(EventType.ConnectionTimeout, address);

        try {
            NBChannelController.write(this.sinkPipe, event);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            return;
        }
    }

}
