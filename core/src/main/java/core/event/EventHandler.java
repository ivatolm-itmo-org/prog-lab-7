package core.event;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.Pair;

import core.handler.Handler;

/**
 * Class for handing occuring events via other handlers.
 * This class uses {@code Selector} to catch events for
 * handlers on input channels provided by them. Then calls
 * {@code process} method of the corresponding handler.
 *
 * @author ivatolm
 */
public abstract class EventHandler<E extends Enum<?>> {

    /**
     * Selector of the channels
     */
    protected Selector selector;

    /**
     * Constructs new {@code EventHandler} with provided arguments.
     *
     * @throws IOException if cannot open new selector
     */
    protected EventHandler() throws IOException {
        this.selector = Selector.open();
    }

    /**
     * Subscribe channel to {@code READ} operations.
     *
     * @param channel channel to subscribe
     * @param attachment attachment to the subscription entry
     * @throws IOException if cannot configure channel non-blocking
     */
    protected void subscribeChannelRead(SelectableChannel channel, Object[] attachment) throws IOException {
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ, attachment);
    }

    /**
     * Subscribe channels to {@code READ} operations.
     *
     * @param channels input channels to subscribe
     * @param handler handler of the events
     * @throws IOException if cannot configure channel non-blocking
     */
    protected void subscribeChannelsRead(
        LinkedList<Pair<E, SelectableChannel>> channels,
        Handler<E, ?> handler
    ) throws IOException {
        for (Pair<E, SelectableChannel> item : channels) {
            this.subscribeChannelRead(
                item.getValue(),
                new Object[] { handler, item.getKey() }
            );
        }
    }

    /**
     * Updates {@code channel}-s {@code READ} subsciption via {@code subsciptions} list.
     * If channel is on the list then subscribe to it, if not already.
     * If channel is not on the list then unsubscribe from it, if not already.
     *
     * @param channel channel to update subscription at
     * @param subscriptions list of current subscriptions
     * @return true if state was changed, else false
     * @throws SelectorKeyNotFoundException if key of {@code channel} is unknown
     */
    protected boolean updateSubscriptionRead(SelectableChannel channel,
                                             LinkedList<Pair<E, SelectableChannel>> subscriptions)
        throws SelectorKeyNotFoundException
    {
        SelectionKey key = channel.keyFor(this.selector);
        if (key == null) {
            throw new SelectorKeyNotFoundException("Selector key wasn't found for: " + channel);
        }

        boolean found = false;
        for (Pair<E, SelectableChannel> subscription : subscriptions) {
            SelectableChannel subsciptionChannel = subscription.getRight();
            if (subsciptionChannel.equals(channel)) {
                found = true;
                break;
            }
        }

        int interestOps = key.interestOps();
        boolean subscribed = (interestOps & SelectionKey.OP_READ) == SelectionKey.OP_READ;

        if (found && !subscribed) {
            key.interestOps(interestOps | SelectionKey.OP_READ);
            return true;
        }

        if (!found && subscribed) {
            key.interestOps(interestOps ^ SelectionKey.OP_READ);
            return true;
        }

        return false;
    }

    /**
     * Updates {@code READ} subsciption of each {@code channel} via {@code subsciptions} list.
     *
     * @param channels channels to update subscription at
     * @param subscriptions list of current subscriptions
     * @throws SelectorKeyNotFoundException if key of {@code channel} is unknown
     */
    protected void updateChannelsSubscriptionRead(
        LinkedList<Pair<E, SelectableChannel>> channels,
        LinkedList<Pair<E, SelectableChannel>> subscriptions
    ) throws SelectorKeyNotFoundException {
        for (Pair<E, SelectableChannel> channel : channels) {
            this.updateSubscriptionRead(channel.getValue(), subscriptions);
        }
    }

    /**
     * Unsubscribe channel from all operations.
     *
     * @param channel channel to unsubscribe
     */
    protected void unsubscribeChannel(SelectableChannel channel) {
        channel.keyFor(this.selector).cancel();
    }

    /**
     * Unsubscribe channels from all operations.
     *
     * @param channels channels to unsubscribe
     */
    protected void unsubscribeChannels(LinkedList<Pair<E, SelectableChannel>> channels) {
        for (Pair<E, SelectableChannel> item : channels) {
            this.unsubscribeChannel(item.getValue());
        }
    }

}
