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

    /**
     * Constructs new {@code InputHandler} with provided arguments.
     */
    public InputHandler(Pipe.SinkChannel outputChannel) {
        this.outputChannel = outputChannel;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Implements {@code run} for {@code Runnable}.
     * Sends received input from stdin to {@code outputChannel}
     */
    @Override
    public void run() {
        while (true) {
            String value = this.scanner.nextLine();

            try {
                NBChannelController.write(this.outputChannel, value);
            } catch (IOException e) {
                System.err.println("Cannot write to buffer: " + e);
            }
        }
    }

}
