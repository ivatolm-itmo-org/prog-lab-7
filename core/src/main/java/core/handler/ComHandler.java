package core.handler;

import java.nio.channels.SelectableChannel;

import core.net.Com;

/**
 * Class for handling one side of communication between client and server.
 *
 * @author ivatolm
 */
public abstract class ComHandler {

    // Communicator
    protected Com com;

    /**
     * Constructs new {@code ComHandler} with provided arguments.
     *
     * @param com communicator
     */
    protected ComHandler(Com com) {
        this.com = com;
    }

    /**
     * Processes all incoming and outgoing messages.
     */
    public abstract void process();

    /**
     * Returns communication channel of the communicator.
     *
     * @return communicator's channel
     */
    public SelectableChannel getComChannel() {
        return this.com.getChannel();
    }

}
