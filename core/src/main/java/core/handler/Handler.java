package core.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.Pair;

import core.fsm.FSM;

/**
 * Class for handling events.
 *
 * @author ivatolm
 */
public abstract class Handler<E extends Enum<?>, S extends Enum<?>> extends FSM<S> {

    /** Input channels */
    protected LinkedList<Pair<E, SelectableChannel>> inputChannels;

    /** Output channels */
    protected LinkedList<Pair<E, SelectableChannel>> outputChannels;

    /** Input channels subscriptions */
    protected LinkedList<Pair<E, SelectableChannel>> subscriptions;

    /** Ready channels */
    protected LinkedList<E> readyChannels;

    /**
     * Constructs new {@code Handler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param initState initial state of FSM
     */
    protected Handler(LinkedList<Pair<E, SelectableChannel>> inputChannels,
                      LinkedList<Pair<E, SelectableChannel>> outputChannels,
                      S initState) {
        super(initState);
        this.inputChannels = inputChannels;
        this.outputChannels = outputChannels;
        this.subscriptions = this.inputChannels;
        this.readyChannels = new LinkedList<>();
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
            this.subscriptions = new LinkedList<>();

            for (Pair<E, SelectableChannel> ic : this.inputChannels) {
                if (ic.getKey() == type) {
                    this.subscriptions.push(ic);
                }
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
            this.subscriptions = new LinkedList<>();
            for (E type : filter) {
                for (Pair<E, SelectableChannel> ic : this.inputChannels) {
                    if (ic.getKey() == type) {
                        this.subscriptions.push(ic);
                    }
                }
            }
        }
    }

    /**
     * Returns first input channel of type {@code type}.
     *
     * @param type type of the channel
     * @return input channel if exists, else null
     */
    protected SelectableChannel getFirstInputChannel(ChannelType type) {
        for (Pair<E, SelectableChannel> ic : this.inputChannels) {
            if (ic.getKey() == type) {
                return ic.getValue();
            }
        }

        return null;
    }

    /**
     * Returns first output channel of type {@code type}.
     *
     * @param type type of the channel
     * @return output channel if exists, else null
     */
    protected SelectableChannel getFirstOutputChannel(ChannelType type) {
        for (Pair<E, SelectableChannel> oc : this.outputChannels) {
            if (oc.getKey() == type) {
                return oc.getValue();
            }
        }

        return null;
    }

    /**
     * Returns all input channels that of the handler.
     *
     * @return input channels of the handler
     */
    public LinkedList<Pair<E, SelectableChannel>> getInputChannels() {
        return this.inputChannels;
    }

    /**
     * Returns input channels that {@code Selector} will
     * catch events on.
     *
     * @return subscriptions of the handler
     */
    public LinkedList<Pair<E, SelectableChannel>> getSubscriptions() {
        return this.subscriptions;
    }

}
