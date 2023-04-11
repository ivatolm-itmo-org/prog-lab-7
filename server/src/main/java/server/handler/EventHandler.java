package server.handler;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * Class for handling application events via other handlers.
 *
 * @author ivatolm
 */
public class EventHandler {

    enum ChannelType {
        Server,
        Shell
    };

    // Channel selector
    private Selector selector;

    // Communication handler
    private ComHandler comHandler;

    // Shell
    private Shell shell;

    /**
     * Constructs new {@code EventHandler} with provided arguments.
     *
     * @param comHandler handler of communicator
     * @param shell handler of shell
     * @throws IOException if cannot setup {@code Selector}
     */
    public EventHandler(ComHandler comHandler, Shell shell) throws IOException {
        this.comHandler = comHandler;
        this.shell = shell;

        this.selector = Selector.open();

        // Server
        SelectableChannel serverChannel = comHandler.getComChannel();
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_READ, ChannelType.Server);

        // Shell
        Pipe pipe = Pipe.open();
        shell.setPipe(pipe);

        SelectableChannel shellChannel = pipe.source();
        shellChannel.configureBlocking(false);
        shellChannel.register(selector, SelectionKey.OP_READ, ChannelType.Shell);
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
                        case Server:
                            if (key.isReadable()) {
                                this.onReadServer();
                            }

                            break;

                        case Shell:
                            if (key.isReadable()) {
                                this.onReadShell();
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

    /**
     * Tells {@code comHandler} to process incoming data.
     */
    private void onReadServer() {
        this.comHandler.process();
    }

    /**
     * Tells {@code comHandler} to process incoming data.
     */
    private void onReadShell() {
        this.shell.process();
    }

}
