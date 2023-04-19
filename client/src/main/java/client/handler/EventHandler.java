package client.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import core.handler.ChannelType;

/**
 * Class for handling application events via other handlers.
 *
 * @author ivatolm
 */
public class EventHandler {

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

        HashMap<ChannelType, SelectableChannel> shellIC = this.shellHandler.getInputChannels();
        for (HashMap.Entry<ChannelType, SelectableChannel> item : shellIC.entrySet()) {
            SelectableChannel channel = item.getValue();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ, item.getKey());
        }

        HashMap<ChannelType, SelectableChannel> comIC = this.comHandler.getInputChannels();
        for (HashMap.Entry<ChannelType, SelectableChannel> item : comIC.entrySet()) {
            SelectableChannel channel = item.getValue();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ, item.getKey());
        }
    }

    /**
     *
     */
    public void run() {
        while (true) {
            try {
                this.selector.select();
                Set<SelectionKey> selectedKeys = this.selector.selectedKeys();

                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    ChannelType channelType = (ChannelType) key.attachment();
                    switch (channelType) {
                        case Shell:
                            if (key.isReadable()) {
                                this.shellHandler.process(channelType);
                            }

                        case Com:
                            if (key.isReadable()) {
                                this.comHandler.process(channelType);
                            }

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
