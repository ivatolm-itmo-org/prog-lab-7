package server.balancer;

import java.nio.channels.SelectableChannel;

import core.handler.ChannelType;
import core.handler.Handler;

/**
 * Class representing a task spreadable by the balancer.
 *
 * @author ivatolm
 */
public class BalancerTask implements Runnable {

    // Handler to process
    private Handler<ChannelType, ?> handler;

    // Channel type from which event was received
    private ChannelType channelType;

    // Channel from which event was received
    private SelectableChannel channel;

    /**
     * Constructs new {@code BalancerTask} with provided arguments.
     *
     * @param handler handler to process
     * @param channelType channel type of channel with event
     * @param channel channel with event
     */
    public BalancerTask(Handler<ChannelType, ?> handler,
                        ChannelType channelType,
                        SelectableChannel channel) {
        this.handler = handler;
        this.channelType = channelType;
        this.channel = channel;
    }

    /**
     * Executes an actual task.
     */
    @Override
    public void run() {
        this.handler.process(this.channelType, this.channel);
        this.handler.postProcessing();
    }

}
