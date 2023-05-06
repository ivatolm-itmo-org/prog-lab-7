package client.net;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.util.TimerTask;

import core.event.Event;
import core.event.EventType;
import core.utils.NBChannelController;

/**
 * Timer task for pinging server.
 *
 * @author ivatolm
 */
public class PingTask extends TimerTask {

    // Socket handler pipe
    private Pipe.SinkChannel sinkPipe;

    /**
     * Constructs new {@code PingTask} with provided arguments.
     *
     * @param sinkPipe socket handler pipe
     */
    public PingTask(Pipe.SinkChannel sinkPipe) {
        this.sinkPipe = sinkPipe;
    }

    @Override
    public void run() {
        Event event = new Event(EventType.PingTimeout, null);

        try {
            NBChannelController.write(this.sinkPipe, event);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            return;
        }
    }

}
