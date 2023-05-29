package server.handler;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.event.EventHandler;
import core.event.SelectorKeyNotFoundException;
import core.handler.ChannelType;
import core.handler.Handler;
import core.handler.HandlerChannel;
import core.handler.HandlerChannels;
import server.balancer.Balancer;
import server.balancer.BalancerTask;
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

    // Load balancer
    private Balancer loadBalancer;

    /**
     * Constructs new {@code EventHandler} with provided arguments.
     *
     * @param shellHandler handler of shell
     * @throws IOException if cannot setup {@code Selector}
     */
    public ServerEventHandler(ServerShellHandler shellHandler,
                            ServerComHandler shellComHandler,
                            ServerSocketHandler socketHandler,
                            Runner runner,
                            Balancer loadBalancer) throws IOException {
        super();

        this.shellHandler = shellHandler;
        this.shellComHandler = shellComHandler;
        this.socketHandler = socketHandler;
        this.runner = runner;
        this.loadBalancer = loadBalancer;

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

    public void updateSubscriptions() {
        logger.trace("Updating subscriptions...");

        try {
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

        } catch (SelectorKeyNotFoundException e) {
            e.printStackTrace();
        }

        logger.trace("Updating subscriptions done");
    }

    /**
     *
     */
    public void run() {
        while (true) {
            try {
                this.updateSubscriptions();

                logger.trace("Selecting channels...");
                this.selector.select(10);
                Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
                logger.trace("" + selectedKeys);
                logger.trace("Selected channels count: {}", selectedKeys.size());

                if (this.socketHandler.hasNewClient()) {
                    this.addClient();
                }

                if (!this.runner.isRunning()) {
                    logger.info("Exiting...");
                    return;
                }

                for (Handler<ChannelType, ?> handler : this.comHandlers) {
                    if (!handler.isRunning()) {
                        this.removeHandler(handler);
                    }
                }

                logger.trace("Connected clients count: {}", this.comHandlers.size());

                Iterator<SelectionKey> iter = selectedKeys.iterator();
                if (!iter.hasNext()) {
                    continue;
                }

                SelectionKey key = iter.next();

                Object[] attachments = (Object[]) key.attachment();

                @SuppressWarnings("unchecked")
                Handler<ChannelType, ?> handler = (Handler<ChannelType, ?>) attachments[0];
                ChannelType channelType = (ChannelType) attachments[1];
                SelectableChannel channel = key.channel();

                logger.debug("Event on {} for {}", channelType, handler);

                if (key.isReadable()) {
                    logger.trace("Preprocessing handler...");

                    handler.preProcessing();
                    this.updateSubscriptions();

                    logger.trace("Sending to load balancer...");
                    BalancerTask task = new BalancerTask(handler, channelType, channel);
                    this.loadBalancer.process(task);
                }

                selectedKeys.clear();

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void addClient() throws IOException {
        logger.debug("Adding new client...");

        Pair<Pipe, Pipe> clientPipes = this.socketHandler.getNewClientPipe();
        Pipe network_com = clientPipes.getLeft();
        Pipe com_network = clientPipes.getRight();

        ServerComHandler comHandler = new ServerComHandler(
            new HandlerChannels() {{
                add(new HandlerChannel(ChannelType.Network, network_com.source()));
            }},
            new HandlerChannels() {{
                add(new HandlerChannel(ChannelType.Network, com_network.sink()));
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
            new HandlerChannels() {{
                add(new HandlerChannel(ChannelType.Com, com_network.source()));
            }},
            this.socketHandler
        );

        this.socketHandler.addInputChannel(new HandlerChannel(ChannelType.Com, com_network.source()));
        this.socketHandler.addOutputChannel(new HandlerChannel(ChannelType.Com, network_com.sink()));

        logger.debug("Adding new client done");
    }

    void removeHandler(Handler<ChannelType, ?> handler) {
        logger.debug("Shutting down handler...");

        this.unsubscribeChannels(handler.getInputChannels());

        // TODO: Handle situation of main handler failure
        if (this.comHandlers.contains(handler)) {
            this.comHandlers.remove(handler);
        }

        logger.debug("Handler was shut down");
    }

}
