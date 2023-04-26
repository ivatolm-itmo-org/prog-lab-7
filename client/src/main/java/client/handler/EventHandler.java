package client.handler;

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

    // Network handler
    private ClientComHandler comHandler;

    // Shell handler
    private ClientShellHandler shellHandler;

    /**
     * Constructs new {@code EventHandler} with provided arguments.
     *
     * @param comHandler handler of communicator
     * @param shellHandler handler of shell
     * @throws IOException if cannot setup {@code Selector}
     */
    public EventHandler(ClientShellHandler shellHandler,
                        ClientComHandler comHandler) throws IOException {
        this.shellHandler = shellHandler;
        this.comHandler = comHandler;

        this.selector = Selector.open();

        logger.debug("Registering channels:");
        logger.debug("  " + "Shell:");
        LinkedList<Pair<ChannelType, SelectableChannel>> shellIC = this.shellHandler.getInputChannels();
        this.registerChannels(ChannelType.Shell, shellIC);

        logger.debug("  " + "Com:");
        LinkedList<Pair<ChannelType, SelectableChannel>> comIC = this.comHandler.getInputChannels();
        this.registerChannels(ChannelType.Com, comIC);
    }

    public void updateSubscriptions() {
        logger.debug("Updating subscriptions...");

        logger.debug("  " + "Shell:");
        LinkedList<Pair<ChannelType, SelectableChannel>> shellIC = this.shellHandler.getInputChannels();
        LinkedList<Pair<ChannelType, SelectableChannel>> shellSubsIC = this.shellHandler.getSubscriptions();
        this.updateChannelSubscriptions(ChannelType.Shell, shellIC, shellSubsIC);

        logger.debug("  " + "Com:");
        LinkedList<Pair<ChannelType, SelectableChannel>> comIC = this.comHandler.getInputChannels();
        LinkedList<Pair<ChannelType, SelectableChannel>> comSubsIC = this.comHandler.getSubscriptions();
        this.updateChannelSubscriptions(ChannelType.Com, comIC, comSubsIC);
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
                    ChannelType handler = (ChannelType) attachments[0];
                    ChannelType channelType = (ChannelType) attachments[1];
                    logger.trace("Event on " + channelType + " for " + handler);

                    switch (handler) {
                        case Shell:
                            if (key.isReadable()) {
                                this.shellHandler.process(channelType, key.channel());
                            }
                            break;

                        case Com:
                            if (key.isReadable()) {
                                this.comHandler.process(channelType, key.channel());
                            }
                            break;

                        default:
                            break;
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

    private void registerChannels(ChannelType type, LinkedList<Pair<ChannelType, SelectableChannel>> ic) {
        for (Pair<ChannelType, SelectableChannel> item : ic) {
            SelectableChannel channel = item.getValue();

            try {
                channel.configureBlocking(false);
                channel.register(
                    selector,
                    SelectionKey.OP_READ,
                    new Object[] { type, item.getKey() }
                );

                logger.debug("    " + type + " <== " + item.getKey());
            } catch (IOException e) {
                logger.warn("Cannot subscribe " + type + " to channel: " + item.getKey());
            }
        }
    }

}
