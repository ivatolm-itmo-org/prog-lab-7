package server.handler;

import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.io.IOException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import core.parser.ArgumentCheckFailedException;
import core.parser.Parser;
import core.command.Command;
import core.command.CommandType;
import core.utils.SimpleParseException;
import server.runner.RecursionFoundException;
import server.runner.Runner;

/**
 * Class providing user interactive shell.
 *
 * @author ivatolm
 */
public class Shell implements Runnable {

    /** Command line scanner */
    private Scanner scanner;

    /** Command parser */
    private Parser parser;

    /** User program runner */
    private Runner runner;

    /** Pipe for communication */
    private Pipe pipe;

    /** Syncronization */
    private boolean firstWaiting;
    private boolean secondWaiting;
    private Lock syncLock;

    /**
     * Constructs new {@code Shell} with provided arguments.
     *
     * @param filename database filename
     */
    public Shell(Runner runner) {
        this.scanner = new Scanner(System.in);
        this.parser = new Parser();
        this.runner = runner;
        this.pipe = null;
        this.firstWaiting = false;
        this.secondWaiting = false;
        this.syncLock = new ReentrantLock();
    }

    /**
     * Runs interactive shell until EOF.
     * Work cycle:
     * 1. get user input
     * 2. send command to the pipe
     */
    @Override
    public void run() {
        if (this.pipe == null) {
            System.err.println("Sink channel was not set. Exiting...");
            return;
        }

        try {
            while (true) {
                LinkedList<Command> commands = this.parseCommands(null);

                synchronized (this.syncLock) {

                    byte[] data = new byte[] { 1 };
                    ByteBuffer buffer = ByteBuffer.wrap(data);

                    try {
                        this.pipe.sink().write(buffer);
                    } catch (IOException e) {
                        System.err.println("Cannot send commands to the pipe: " + e);
                        continue;
                    }

                    try {
                        firstWaiting = true;
                        while (firstWaiting) {
                            this.syncLock.wait();
                        }
                    } catch (InterruptedException e) {
                        System.err.println("Thread was interrupted while waiting: " + e);
                        continue;
                    }

                    buffer.clear();
                    try {
                        this.pipe.source().read(buffer);
                    } catch (IOException e) {
                        System.err.println("Cannot read from the pipe: " + e);
                    }

                    try {
                        this.runner.addSubroutine(commands);
                    } catch (RecursionFoundException e) {
                        System.err.println("Recursion detected. Skipping...");
                        continue;
                    }

                    this.runner.run();

                    System.out.println("P1 Notifying");
                    secondWaiting = false;
                    this.syncLock.notify();

                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nExiting by Ctrl-D (EOF)");
        }
    }

    /**
     *
     */
    public void setPipe(Pipe pipe) {
        this.pipe = pipe;
    }

    /**
     *
     */
    public void process() {
        synchronized (this.syncLock) {

            firstWaiting = false;
            this.syncLock.notify();

            try {
                secondWaiting = true;
                while (secondWaiting) {
                    this.syncLock.wait();
                }
            } catch (InterruptedException e) {
                System.err.println("Thread was interrupted while waiting: " + e);
            }

        }
    }

    /**
     * Parses one or multiple {@code Command}-s from {@code inputs}.
     * If input is null, greets user and parses the command.
     * If parsing fails, then asks user to correct the input.
     *
     * @param inputs strings to parse {@code Command} from or null
     * @return parsed {@code Command}-s
     */
    private LinkedList<Command> parseCommands(LinkedList<String> inputs) {
        LinkedList<Command> result = new LinkedList<>();

        boolean promptRequired = inputs == null || inputs.isEmpty();
        while (true) {
            String input;
            if (promptRequired) {
                System.out.print(": ");
                input = this.scanner.nextLine();
            } else {
                input = inputs.pop();
            }

            promptRequired = inputs == null || inputs.isEmpty();

            try {
                boolean hasParsedCommands = this.parser.parse(input);
                if (hasParsedCommands == true) {
                    result.addAll(this.parser.getResult());

                    if (promptRequired) {
                        return result;
                    }
                }

            } catch (SimpleParseException e) {
                System.err.println(e.getMessage());
                promptRequired = true;
                continue;

            } catch (ArgumentCheckFailedException e) {
                System.err.println(e.getMessage());
                promptRequired = true;
            }

            if (promptRequired) {
                CommandType cmdType = this.parser.getCurrentCommandType();
                int argCnt = this.parser.getCurrentArgumentsCnt();

                String greeting = "Enter" + " " + "'" + cmdType.getArgument(argCnt).getGreetingMsg() + "'";
                System.out.print(greeting);
            }
        }
    }

    /**
     * Closes shell.
     * Closes internal connections.
     */
    public void close() {
        this.scanner.close();
    }

}
