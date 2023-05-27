package core.handler;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.util.Scanner;

import core.utils.NBChannelController;

/**
 * Class providing asyncronous input from System.in
 *
 * @author ivatolm
 */
public class InputHandler implements Runnable {

    // Output channel
    private Pipe.SinkChannel outputChannel;

    // Scanner
    private Scanner scanner;

    // Should close flag
    private boolean shouldClose;

    /**
     * Constructs new {@code InputHandler} with provided arguments.
     */
    public InputHandler(Pipe.SinkChannel outputChannel) {
        this.outputChannel = outputChannel;
        this.scanner = new Scanner(System.in);
        this.shouldClose = false;
    }

    /**
     * Implements {@code run} for {@code Runnable}.
     * Sends received input from stdin to {@code outputChannel}
     */
    @Override
    public void run() {
        while (!shouldClose) {
            String value = this.scanner.nextLine();

            try {
                NBChannelController.write(this.outputChannel, value);
            } catch (IOException e) {
                // Ignoring errors
            }
        }
    }

    public void close() {
        this.shouldClose = true;
    }

}
