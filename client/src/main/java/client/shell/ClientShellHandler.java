package client.shell;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.SerializationUtils;

import core.command.Command;
import core.handler.ShellHandler;

/**
 * Class providing user interactive shell.
 *
 * @author ivatolm
 */
public class ClientShellHandler extends ShellHandler {

    /** Communication channel with {@code Com} */
    private Pipe comPipe;

    /**
     * Constructs new {@code Shell} with provided arguments.
     */
    public ClientShellHandler(Pipe comPipe) {
        super();
        this.comPipe = comPipe;
    }

    /**
     * Runs interactive shell until EOF.
     * Work cycle:
     * 1. get user input
     * 2. send command to the pipe
     */
    @Override
    public void _run() {
        ByteBuffer buffer;
        try {
            LinkedList<Command> commands = this.parseCommands(null);

            // Sending commands to ComHandler
            byte[] commandsBytes = SerializationUtils.serialize(commands);
            buffer = ByteBuffer.wrap(commandsBytes);
            try {
                this.comPipe.sink().write(buffer);
            } catch (IOException e) {
                System.err.println("Cannot send commands to the pipe: " + e);
                return;
            }

            // Sending notification to selector
            this.syncWait(ByteBuffer.wrap(new byte[] { 1 }));

            // Reading received command output
            buffer.clear();
            try {
                this.comPipe.source().read(buffer);
            } catch (IOException e) {
                System.err.println("Cannot read from the pipe: " + e);
                return;
            }

            LinkedList<String> output = SerializationUtils.deserialize(buffer.array());
            for (String commandOutput : output) {
                System.out.println(commandOutput);
            }

            // Notifying selector that we are done
            this.syncNotify();

        } catch (NoSuchElementException e) {
            System.out.println("\nExiting by Ctrl-D (EOF)");
            this.running = false;
        }
    }

}
