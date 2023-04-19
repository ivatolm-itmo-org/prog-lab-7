package client.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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
    private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);

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

        logger.debug("Registering shell input channels:");
        HashMap<ChannelType, SelectableChannel> shellIC = this.shellHandler.getInputChannels();
        for (HashMap.Entry<ChannelType, SelectableChannel> item : shellIC.entrySet()) {
            SelectableChannel channel = item.getValue();
            channel.configureBlocking(false);
            channel.register(
                selector,
                SelectionKey.OP_READ,
                new Object[] { ChannelType.Shell, item.getKey() }
            );
            logger.debug(ChannelType.Shell + " <- " + item.getKey());
        }

        logger.debug("Registering com input channels:");
        HashMap<ChannelType, SelectableChannel> comIC = this.comHandler.getInputChannels();
        for (HashMap.Entry<ChannelType, SelectableChannel> item : comIC.entrySet()) {
            SelectableChannel channel = item.getValue();
            channel.configureBlocking(false);
            channel.register(
                selector,
                SelectionKey.OP_READ,
                new Object[] { ChannelType.Com, item.getKey() }
            );
            logger.debug(ChannelType.Com + " <- " + item.getKey());
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
                    ChannelType handler = (ChannelType) attachments[0];
                    ChannelType channelType = (ChannelType) attachments[1];
                    logger.trace("Event on " + channelType + " for " + handler);

                    switch (handler) {
                        case Shell:
                            if (key.isReadable()) {
                                this.shellHandler.process(channelType);
                            }
                            break;

                        case Com:
                            if (key.isReadable()) {
                                this.comHandler.process(channelType);
                            }
                            break;

                        default:
                            break;
                    }
                }

                selectedKeys.clear();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
