package core.handler;

import java.nio.channels.SelectableChannel;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Alias-class representing handler's channels.
 *
 * @author ivatolm
 */
public class HandlerChannel extends Pair<ChannelType, SelectableChannel> {

    private Pair<ChannelType, SelectableChannel> pair;

    public HandlerChannel(ChannelType type, SelectableChannel channel) {
        this.pair = new ImmutablePair<>(type, channel);
    }

    @Override
    public SelectableChannel setValue(SelectableChannel value) {
        return this.pair.setValue(value);
    }

    @Override
    public ChannelType getLeft() {
        return this.pair.getLeft();
    }

    @Override
    public SelectableChannel getRight() {
        return this.pair.getRight();
    }

}
