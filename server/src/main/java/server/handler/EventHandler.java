package server.handler;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.handler.ChannelType;
import core.handler.Handler;
import server.runner.Runner;

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

    // Communication handler for shell
    private ServerComHandler shellComHandler;

    // Socket handler
    private ServerSocketHandler socketHandler;

    // Client handlers
    private LinkedList<ServerComHandler> comHandlers;

    // Program runner
    private Runner runner;

    /**
     * Constructs new {@code EventHandler} with provided arguments.
     *
     * @param shellHandler handler of shell
     * @throws IOException if cannot setup {@code Selector}
     */
    public EventHandler(ServerShellHandler shellHandler,
                        ServerComHandler shellComHandler,
                        ServerSocketHandler socketHandler,
                        Runner runner) throws IOException {
        this.shellHandler = shellHandler;
        this.shellComHandler = shellComHandler;
        this.socketHandler = socketHandler;
        this.runner = runner;

        this.comHandlers = new LinkedList<>();

        this.selector = Selector.open();

        logger.debug("Registering channels:");
        logger.debug("  " + "Shell:");
        LinkedList<Pair<ChannelType, SelectableChannel>> shellIC = this.shellHandler.getInputChannels();
        this.registerChannels(ChannelType.Shell, this.shellHandler, shellIC);

        logger.debug("  " + "ShellCom:");
        LinkedList<Pair<ChannelType, SelectableChannel>> comShellIC = this.shellComHandler.getInputChannels();
        this.registerChannels(ChannelType.Com, this.shellComHandler, comShellIC);

        logger.debug("  " + "Socket:");
        LinkedList<Pair<ChannelType, SelectableChannel>> socketIC = this.socketHandler.getInputChannels();
        this.registerChannels(ChannelType.Network, this.socketHandler, socketIC);
    }

    public void updateSubscriptions() {
        logger.debug("Updating subscriptions...");

        logger.debug("  " + "Shell:");
        LinkedList<Pair<ChannelType, SelectableChannel>> shellIC = this.shellHandler.getInputChannels();
        LinkedList<Pair<ChannelType, SelectableChannel>> shellSubsIC = this.shellHandler.getSubscriptions();
        this.updateChannelSubscriptions(ChannelType.Shell, shellIC, shellSubsIC);

        logger.debug("  " + "ShellCom:");
        LinkedList<Pair<ChannelType, SelectableChannel>> shellComIC = this.shellHandler.getInputChannels();
        LinkedList<Pair<ChannelType, SelectableChannel>> shellComSubsIC = this.shellHandler.getSubscriptions();
        this.updateChannelSubscriptions(ChannelType.Com, shellComIC, shellComSubsIC);

        logger.debug("  " + "Socket:");
        LinkedList<Pair<ChannelType, SelectableChannel>> socketIC = this.socketHandler.getInputChannels();
        LinkedList<Pair<ChannelType, SelectableChannel>> socketSubsIC = this.socketHandler.getSubscriptions();
        this.updateChannelSubscriptions(ChannelType.Network, socketIC, socketSubsIC);

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

                    if (this.socketHandler.hasClientPipe()) {
                        Pair<Pipe, Pipe> clientPipes = this.socketHandler.getClientPipe();
                        Pipe network_com = clientPipes.getLeft();
                        Pipe com_network = clientPipes.getRight();

                        ServerComHandler comHandler = new ServerComHandler(
                            new LinkedList<Pair<ChannelType, SelectableChannel>>() {{
                                add(new ImmutablePair<>(ChannelType.Network, network_com.source()));
                            }},
                            new LinkedList<Pair<ChannelType, SelectableChannel>>() {{
                                add(new ImmutablePair<>(ChannelType.Network, com_network.sink()));
                            }},
                            this.runner,
                            ChannelType.Network
                        );

                        logger.debug("Registering channels for new comHandler:");
                        logger.debug("  " + "Com:");
                        LinkedList<Pair<ChannelType, SelectableChannel>> comIC = comHandler.getInputChannels();
                        this.registerChannels(ChannelType.Com, comHandler, comIC);
                        this.comHandlers.push(comHandler);

                        Pipe.SourceChannel com_network_channel = com_network.source();
                        com_network_channel.configureBlocking(false);
                        com_network_channel.register(
                            selector,
                            SelectionKey.OP_READ,
                            new Object[] { this.socketHandler, ChannelType.Com }
                        );
                        logger.debug("    " + ChannelType.Network + " <== " + ChannelType.Com);

                        this.socketHandler.addInputChannel(new ImmutablePair<>(ChannelType.Com, com_network.source()));
                        this.socketHandler.addOutputChannel(new ImmutablePair<>(ChannelType.Com, network_com.sink()));
                    }

                    if (!this.runner.isRunning()) {
                        logger.info("Exiting...");
                        return;
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
                    new Object[] { handler, item.getKey() }
                );

                logger.debug("    " + type + " <== " + item.getKey());
            } catch (IOException e) {
                logger.warn("Cannot subscribe " + type + " to channel " + item.getKey() + ": " + e);
            }
        }
    }

}
