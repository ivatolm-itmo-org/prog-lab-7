package server.load;

import java.nio.channels.SelectableChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import core.handler.ChannelType;
import core.handler.Handler;

/**
 * Class for execution of the handler processing.
 *
 * @author ivatolm
 */
public class Worker implements Runnable {

    // Is running flag
    private volatile Boolean isRunning;

    // Request queues
    private Queue<Handler<ChannelType, ?>> handlersQueue;
    private Queue<ChannelType> channelTypesQueue;
    private Queue<SelectableChannel> channelsQueue;

    /**
     * Constructs new {@code Worker} with provided arguments.
     */
    public Worker() {
        this.isRunning = true;

        this.handlersQueue = new LinkedBlockingQueue<>();
        this.channelTypesQueue = new LinkedBlockingQueue<>();
        this.channelsQueue = new LinkedBlockingQueue<>();
    }

    /**
     * Receives handler and starts it's processing.
     */
    @Override
    public void run() {
        while (this.isRunning) {
            if (!this.handlersQueue.isEmpty() &&
                !this.channelTypesQueue.isEmpty() &&
                !this.channelsQueue.isEmpty())
            {
                Handler<ChannelType, ?> handler = this.handlersQueue.poll();
                ChannelType channelType = this.channelTypesQueue.poll();
                SelectableChannel channel = this.channelsQueue.poll();

                handler.process(channelType, channel);
                handler.postProcessing();
            }

            else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    System.err.println("Thread sleep was interrupted.");
                }
            }
        }
    }

    /**
     * Adds request parameters to the corresponding queues.
     *
     * @param handler handler to process
     * @param channelType handler processing param #1
     * @param channel handler processing param #2
     */
    public void addRequest(Handler<ChannelType, ?> handler, ChannelType channelType, SelectableChannel channel) {
        this.handlersQueue.add(handler);
        this.channelTypesQueue.add(channelType);
        this.channelsQueue.add(channel);
    }

    /**
     * Closes worker.
     */
    public void close() {
        this.isRunning = false;
    }

}
