package core.handler;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SelectableChannel;
import java.util.LinkedList;

import core.fsm.FSM;
import core.utils.ChannelNotFoundException;

/**
 * Class for handling events.
 *
 * @author ivatolm
 */
public abstract class Handler<E extends Enum<?>, S extends Enum<?>> extends FSM<S> implements Serializable {

    /** Input channels */
    protected HandlerChannels inputChannels;

    /** Output channels */
    protected HandlerChannels outputChannels;

    /** Input channels subscriptions */
    protected HandlerChannels subscriptions;
    protected HandlerChannels subscriptionsBuffer;

    /** Ready channels */
    protected LinkedList<E> readyChannels;

    // Is running
    private boolean running;

    /**
     * Constructs new {@code Handler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param initState initial state of FSM
     */
    protected Handler(HandlerChannels inputChannels,
                      HandlerChannels outputChannels,
                      S initState) {
        super(initState);
        this.inputChannels = inputChannels;
        this.outputChannels = outputChannels;
        this.subscriptions = this.inputChannels;
        this.subscriptionsBuffer = null;
        this.readyChannels = new LinkedList<>();
        this.running = true;
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
     * Removes and saves all handler subscriptions.
     */
    public void preProcessing() {
        this.subscriptionsBuffer = this.subscriptions;
        this.subscriptions = new HandlerChannels();
    }

    /**
     * Restores all handler subscriptions.
     */
    public void postProcessing() {
        this.subscriptions = this.subscriptionsBuffer;
        this.subscriptionsBuffer = new HandlerChannels();
    }

    /**
     * Filters {@code inputChannels} to all avaliable
     * input channels.
     */
    protected void filterSubscriptions() {
        this.subscriptionsBuffer = this.inputChannels;
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
            this.subscriptionsBuffer = this.inputChannels;
        } else {
            this.subscriptionsBuffer = new HandlerChannels();

            for (HandlerChannel ic : this.inputChannels) {
                if (ic.getKey() == type) {
                    this.subscriptionsBuffer.push(ic);
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
            this.subscriptionsBuffer = this.inputChannels;
        } else {
            this.subscriptionsBuffer = new HandlerChannels();
            for (E type : filter) {
                for (HandlerChannel ic : this.inputChannels) {
                    if (ic.getKey() == type) {
                        this.subscriptionsBuffer.push(ic);
                    }
                }
            }
        }
    }

    /**
     * Returns first input channel of type {@code type}.
     *
     * @param type type of the channel
     * @return input channel
     * @throws ChannelNotFoundException if channel wasn't found
     */
    protected SelectableChannel getFirstInputChannel(ChannelType type)
        throws ChannelNotFoundException
    {
        for (HandlerChannel ic : this.inputChannels) {
            if (ic.getKey() == type) {
                return ic.getValue();
            }
        }

        throw new ChannelNotFoundException("Input channel wasn't found.");
    }

    /**
     * Returns first output channel of type {@code type}.
     *
     * @param type type of the channel
     * @return output channel
     * @throws ChannelNotFoundException if channel wasn't found
     */
    protected SelectableChannel getFirstOutputChannel(ChannelType type)
        throws ChannelNotFoundException
    {
        for (HandlerChannel oc : this.outputChannels) {
            if (oc.getKey() == type) {
                return oc.getValue();
            }
        }

        throw new ChannelNotFoundException("Output channel wasn't found.");
    }

    /**
     * Sets running flag to false.
     */
    protected void setNotRunning() {
        this.running = false;
    }

    /**
     * Returns all input channels that of the handler.
     *
     * @return input channels of the handler
     */
    public HandlerChannels getInputChannels() {
        return this.inputChannels;
    }

    /**
     * Returns input channels that {@code Selector} will
     * catch events on.
     *
     * @return subscriptions of the handler
     */
    public HandlerChannels getSubscriptions() {
        return this.subscriptions;
    }

    /**
     * Adds {@code ic} to input channels.
     *
     * @param ic channel to add
     */
    public void addInputChannel(HandlerChannel ic) {
        this.inputChannels.add(ic);
    }

    /**
     * Adds {@code oc} to output channels.
     *
     * @param oc channel to add
     */
    public void addOutputChannel(HandlerChannel oc) {
        this.outputChannels.add(oc);
    }

    /**
     * Returns running state of the Handler.
     *
     * @return true if running, else false
     */
    public boolean isRunning() {
        return this.running;
    }

}
