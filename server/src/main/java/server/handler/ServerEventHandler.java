package server.handler;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.event.EventHandler;
import core.event.SelectorKeyNotFoundException;
import core.handler.ChannelType;
import core.handler.Handler;
import server.runner.Runner;

/**
 * Class for handling application events via other handlers.
 *
 * @author ivatolm
 */
public class ServerEventHandler extends EventHandler<ChannelType> {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger("EventHandler");

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
    public ServerEventHandler(ServerShellHandler shellHandler,
                        ServerComHandler shellComHandler,
                        ServerSocketHandler socketHandler,
                        Runner runner) throws IOException {
        super();

        this.shellHandler = shellHandler;
        this.shellComHandler = shellComHandler;
        this.socketHandler = socketHandler;
        this.runner = runner;

        this.comHandlers = new LinkedList<>();

        this.subscribeChannelsRead(
            this.shellHandler.getInputChannels(),
            this.shellHandler
        );

        this.subscribeChannelsRead(
            this.shellComHandler.getInputChannels(),
            this.shellComHandler
        );

        this.subscribeChannelsRead(
            this.socketHandler.getInputChannels(),
            this.socketHandler
        );
    }

    public void updateSubscriptions() throws SelectorKeyNotFoundException {
        this.updateChannelsSubscriptionRead(
            this.shellHandler.getInputChannels(),
            this.shellHandler.getSubscriptions()
        );

        this.updateChannelsSubscriptionRead(
            this.shellComHandler.getInputChannels(),
            this.shellComHandler.getSubscriptions()
        );

        this.updateChannelsSubscriptionRead(
            this.socketHandler.getInputChannels(),
            this.socketHandler.getSubscriptions()
        );

        for (ServerComHandler comHandler : this.comHandlers) {
            this.updateChannelsSubscriptionRead(
                comHandler.getInputChannels(),
                comHandler.getSubscriptions()
            );
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
                    Handler<ChannelType, ?> handler = (Handler<ChannelType,?>) attachments[0];
                    ChannelType channelType = (ChannelType) attachments[1];

                    logger.trace("Event on " + channelType + " for " + handler);

                    if (key.isReadable()) {
                        handler.process(channelType, key.channel());
                    }

                    if (!handler.isRunning()) {
                        logger.debug("Shutting down handler...");
                        this.removeHandler(handler);
                        logger.debug("Handler was shut down");
                    } else if (this.socketHandler.hasNewClient()) {
                        logger.debug("Adding new client...");
                        this.addClient();
                        logger.debug("Adding new client done");
                    }

                    if (!this.runner.isRunning()) {
                        logger.info("Exiting...");
                        return;
                    }

                    logger.debug("Updating subscriptions...");
                    try {
                        this.updateSubscriptions();
                    } catch (SelectorKeyNotFoundException e) {
                        logger.warn("Cannot update subscriptions. Exiting...");
                        return;
                    }
                    logger.debug("Updating subscriptions done");

                    logger.debug("Connected clients count: " + this.comHandlers.size());

                    break;
                }

                selectedKeys.clear();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addClient() throws IOException {
        Pair<Pipe, Pipe> clientPipes = this.socketHandler.getNewClientPipe();
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

        this.subscribeChannelsRead(
            comHandler.getInputChannels(),
            comHandler
        );
        this.comHandlers.push(comHandler);

        this.subscribeChannelsRead(
            new LinkedList<Pair<ChannelType, SelectableChannel>>() {{
                add(new ImmutablePair<>(ChannelType.Com, com_network.source()));
            }},
            this.socketHandler
        );

        this.socketHandler.addInputChannel(new ImmutablePair<>(ChannelType.Com, com_network.source()));
        this.socketHandler.addOutputChannel(new ImmutablePair<>(ChannelType.Com, network_com.sink()));
    }

    void removeHandler(Handler<ChannelType, ?> handler) {
        this.unsubscribeChannels(handler.getInputChannels());

        // TODO: Handle situation of main handler failure
        if (this.comHandlers.contains(handler)) {
            this.comHandlers.remove(handler);
        }
    }

}
