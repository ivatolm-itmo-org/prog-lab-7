package server.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.Pipe;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import core.event.Event;
import core.event.EventType;
import core.utils.NBChannelController;

/**
 * Timer task for disconnecting clients if timeout was exceeded.
 *
 * @author ivatolm
 */
public class DisconnectionTask extends TimerTask {

    // Syncronization
    private Lock lock;

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
    public DisconnectionTask(Lock lock, Pipe.SinkChannel sinkPipe, SocketAddress address) {
        this.lock = lock;
        this.sinkPipe = sinkPipe;
        this.address = address;
    }

    @Override
    public void run() {
        this.lock.lock();

        Event event = new Event(EventType.ConnectionTimeout, address);

        try {
            NBChannelController.write(this.sinkPipe, event);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
        }

        this.lock.unlock();
    }

}
