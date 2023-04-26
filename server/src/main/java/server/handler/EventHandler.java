package server.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.handler.ChannelType;
import core.handler.Handler;

/**
 * Class for handling application events via other handlers.
 *
 * @author ivatolm
 */
public class EventHandler {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger("EventHandler");

    // Channel selector
    private Selector selector;

    // Shell handler
    private ServerShellHandler shellHandler;

    // Client handlers
    private LinkedList<ServerComHandler> comHandlers;

    /**
     * Constructs new {@code EventHandler} with provided arguments.
     *
     * @param shellHandler handler of shell
     * @throws IOException if cannot setup {@code Selector}
     */
    public EventHandler(ServerShellHandler shellHandler) throws IOException {
        this.shellHandler = shellHandler;
        this.comHandlers = new LinkedList<>();

        this.selector = Selector.open();

        logger.debug("Registering channels:");
        logger.debug("  " + "Shell:");
        LinkedList<Pair<ChannelType, SelectableChannel>> shellIC = this.shellHandler.getInputChannels();
        this.registerChannels(ChannelType.Shell, this.shellHandler, shellIC);

        for (ServerComHandler comHandler : this.comHandlers) {
            logger.debug("  " + "Com:");
            LinkedList<Pair<ChannelType, SelectableChannel>> comIC = comHandler.getInputChannels();
            this.registerChannels(ChannelType.Com, comHandler, comIC);
        }
    }

    public void updateSubscriptions() {
        logger.debug("Updating subscriptions...");

        logger.debug("  " + "Shell:");
        LinkedList<Pair<ChannelType, SelectableChannel>> shellIC = this.shellHandler.getInputChannels();
        LinkedList<Pair<ChannelType, SelectableChannel>> shellSubsIC = this.shellHandler.getSubscriptions();
        this.updateChannelSubscriptions(ChannelType.Shell, shellIC, shellSubsIC);

        for (ServerComHandler comHandler : this.comHandlers) {
            logger.debug("  " + "Com:");
            LinkedList<Pair<ChannelType, SelectableChannel>> comIC = comHandler.getInputChannels();
            LinkedList<Pair<ChannelType, SelectableChannel>> comSubsIC = comHandler.getSubscriptions();
            this.updateChannelSubscriptions(ChannelType.Com, comIC, comSubsIC);
        }
    }

    /**
     *
     */
    public void run() {
        while (true) {
            try {
                logger.trace("Selecting channels...");
                this.selector.select();
                Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
                logger.debug("Selected channels count: " + selectedKeys.size());

                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    Object[] attachments = (Object[]) key.attachment();

                    @SuppressWarnings("unchecked")
                    Handler<ChannelType,?> handler = (Handler<ChannelType,?>) attachments[0];
                    ChannelType channelType = (ChannelType) attachments[1];

                    logger.trace("Event on " + channelType + " for " + handler);

                    if (key.isReadable()) {
                        handler.process(channelType, key.channel());
                    }

                    this.updateSubscriptions();
                    break;
                }

                selectedKeys.clear();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateChannelSubscriptions(ChannelType type,
                                            LinkedList<Pair<ChannelType, SelectableChannel>> ic,
                                            LinkedList<Pair<ChannelType, SelectableChannel>> subs) {
        for (HashMap.Entry<ChannelType, SelectableChannel> item : ic) {
            SelectableChannel channel = item.getValue();
            SelectionKey key = channel.keyFor(this.selector);

            if (key == null) {
                logger.warn("Cannot subscribe " + type + " to channel: " + item.getKey());
                continue;
            }

            if (subs.contains(item)) {
                if ((key.interestOps() & SelectionKey.OP_READ) != SelectionKey.OP_READ) {
                    logger.debug("    " + type + " === " + item.getKey());
                    key.interestOps(SelectionKey.OP_READ);
                }
            } else {
                if ((key.interestOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                    logger.debug("    " + type + " =!= " + item.getKey());
                    key.interestOps(0);
                }
            }
        }
    }

    private void registerChannels(ChannelType type,
                                  Handler<ChannelType,?> handler,
                                  LinkedList<Pair<ChannelType, SelectableChannel>> ic) {
        for (Pair<ChannelType, SelectableChannel> item : ic) {
            SelectableChannel channel = item.getValue();

            try {
                channel.configureBlocking(false);
                channel.register(
                    selector,
                    SelectionKey.OP_READ,
                    new Object[] { handler, type }
                );

                logger.debug("    " + type + " <== " + item.getKey());
            } catch (IOException e) {
                logger.warn("Cannot subscribe " + type + " to channel: " + item.getKey());
            }
        }
    }


}
