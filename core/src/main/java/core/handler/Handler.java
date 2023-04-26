package core.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.HashMap;
import java.util.HashSet;

import core.fsm.FSM;

/**
 * Class for handling events.
 *
 * @author ivatolm
 */
public abstract class Handler<E extends Enum<?>, S extends Enum<?>> extends FSM<S> {

    /** Input channels */
    protected HashMap<E, SelectableChannel> inputChannels;

    /** Output channels */
    protected HashMap<E, SelectableChannel> outputChannels;

    /** Input channels subscriptions */
    protected HashMap<E, SelectableChannel> subscriptions;

    /** Ready channels */
    protected HashSet<E> readyChannels;

    /**
     * Constructs new {@code Handler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param initState initial state of FSM
     */
    protected Handler(HashMap<E, SelectableChannel> inputChannels,
                      HashMap<E, SelectableChannel> outputChannels,
                      S initState) {
        super(initState);
        this.inputChannels = inputChannels;
        this.outputChannels = outputChannels;
        this.subscriptions = this.inputChannels;
        this.readyChannels = new HashSet<>();
    }

    /**
     * Method that is run by {@code Selector} when
     * some event occures on one input channels.
     *
     * @param type ready channel identificator
     * @param channel ready channel
     */
    public abstract void process(E type, SelectableChannel channel);

    /**
     * Filters {@code inputChannels} to all avaliable
     * input channels.
     */
    protected void filterSubscriptions() {
        this.subscriptions = this.inputChannels;
    }

    /**
     * Filters {@code inputChannels} to only use {@code type}
     * input channel.
     *
     * @param filter new subscription
     * @throws IOException if input channel doesn't exist
     */
    protected void filterSubscriptions(E type) throws IOException {
        if (type == null) {
            this.subscriptions = this.inputChannels;
        } else {
            this.subscriptions = new HashMap<>();
            SelectableChannel ic = this.inputChannels.get(type);
            if (ic == null) {
                throw new IOException("Cannot subscribe to channel. Channel doesn't exist: " + type);
            } else {
                this.subscriptions.put(type, ic);
            }
        }
    }

    /**
     * Filters {@code inputChannels} to only use {@code filter}
     * input channels.
     *
     * @param filter new subscriptions
     * @throws IOException if input channel doesn't exist
     */
    protected void filterSubscriptions(E[] filter) throws IOException {
        if (filter == null) {
            this.subscriptions = this.inputChannels;
        } else {
            this.subscriptions = new HashMap<>();
            for (E type : filter) {
                SelectableChannel ic = this.inputChannels.get(type);
                if (ic == null) {
                    throw new IOException("Cannot subscribe to channel. Channel doesn't exist: " + type);
                } else {
                    this.subscriptions.put(type, ic);
                }
            }
        }
    }

    /**
     * Returns all input channels that of the handler.
     *
     * @return input channels of the handler
     */
    public HashMap<E, SelectableChannel> getInputChannels() {
        return this.inputChannels;
    }

    /**
     * Returns input channels that {@code Selector} will
     * catch events on.
     *
     * @return subscriptions of the handler
     */
    public HashMap<E, SelectableChannel> getSubscriptions() {
        return this.subscriptions;
    }

}
