package client.handler;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import core.handler.ComHandler;
import core.handler.ShellHandler;

/**
 * Class for handling application events via other handlers.
 *
 * @author ivatolm
 */
public class EventHandler {

    enum ChannelType {
        Network,
        Shell,
        Com_Shell,
        Shell_Com
    };

    // Channel selector
    private Selector selector;

    // Network handler
    private ComHandler comHandler;

    // Shell handler
    private ShellHandler shellHandler;

    /**
     * Constructs new {@code EventHandler} with provided arguments.
     *
     * @param comHandler handler of communicator
     * @param shell handler of shell
     * @throws IOException if cannot setup {@code Selector}
     */
    public EventHandler(ComHandler comHandler,
                        ShellHandler shell,
                        Pipe.SourceChannel com_shell_pipe,
                        Pipe.SourceChannel shell_com_pipe) throws IOException {
        this.comHandler = comHandler;
        this.shellHandler = shell;

        this.selector = Selector.open();

        // Shell
        Pipe pipe = Pipe.open();
        this.shellHandler.setPipe(pipe);

        // Com => Shell channel
        SelectableChannel com_shell_channel = com_shell_pipe;
        com_shell_channel.configureBlocking(false);
        com_shell_channel.register(selector, SelectionKey.OP_READ, ChannelType.Com_Shell);

        // Shell => Com channel
        SelectableChannel shell_com_channel = shell_com_pipe;
        shell_com_channel.configureBlocking(false);
        shell_com_channel.register(selector, SelectionKey.OP_READ, ChannelType.Shell_Com);
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
                    System.out.println(channelType);
                    switch (channelType) {
                        case Com_Shell:
                            if (key.isReadable()) {
                                this.onReadShell();
                            }

                        case Shell_Com:
                            if (key.isReadable()) {
                                this.onReadNetwork();
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

    /**
     * Tells {@code comHandler} to process incoming data.
     */
    private void onReadNetwork() {
        this.comHandler.process();
    }

    /**
     * Tells {@code shellHandler} to process incoming data.
     */
    private void onReadShell() {
        this.shellHandler.process();
    }

}
