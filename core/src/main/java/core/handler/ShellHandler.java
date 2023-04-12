package core.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import core.command.Command;
import core.command.CommandType;
import core.parser.ArgumentCheckFailedException;
import core.parser.Parser;
import core.utils.SimpleParseException;

/**
 * Class providing user interactive shell.
 *
 * @author ivatolm
 */
public abstract class ShellHandler implements Runnable {

    /** Command line scanner */
    protected Scanner scanner;

    /** Command parser */
    protected Parser parser;

    /** Communication channel */
    protected Pipe pipe;

    /** Thread running flag */
    protected boolean running;

    /** Syncronization */
    private Lock syncLock;
    private boolean firstWaiting;
    private boolean secondWaiting;

    /**
     * Constructs new {@code ShellHandler} with provided arguments.
     *
     * @param filename database filename
     */
    protected ShellHandler() {
        this.scanner = new Scanner(System.in);
        this.parser = new Parser();

        this.running = true;

        this.syncLock = new ReentrantLock();
        this.firstWaiting = false;
        this.secondWaiting = false;
    }

    /**
     * Runs logic specific to application.
     */
    protected abstract void _run();

    /**
     * Implements {@code run} for {@code Runnable}.
     */
    @Override
    public final void run() {
        if (this.pipe == null) {
            System.err.println("Sink channel was not set. Exiting...");
            return;
        }

        while (running) {
            synchronized (this.syncLock) {

                this._run();

            }
        }
    }

    /**
     * Sends buffer and waits to be awakened (sync p.1).
     *
     * @param buffer buffer to send to the pipe
     */
    protected final void syncWait(ByteBuffer buffer) {
        try {
            this.pipe.sink().write(buffer);
        } catch (IOException e) {
            System.err.println("Cannot send commands to the pipe: " + e);
            return;
        }

        try {
            firstWaiting = true;
            while (firstWaiting) {
                this.syncLock.wait();
            }
        } catch (InterruptedException e) {
            System.err.println("Thread was interrupted while waiting: " + e);
            return;
        }

        buffer.clear();
        try {
            this.pipe.source().read(buffer);
        } catch (IOException e) {
            System.err.println("Cannot read from the pipe: " + e);
        }
    }

    /**
     * Notifies other thread (sync p.2).
     */
    protected final void syncNotify() {
        secondWaiting = false;
        this.syncLock.notify();
    }

    /**
     * Syncronizes with other thread.
     * This method is invoked from {@code Selector} and
     * syncronizes with {@code syncRun}.
     */
    public final void process() {
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
     * Set communication pipe to {@code pipe}.
     *
     * @param pipe communication pipe
     */
    public void setPipe(Pipe pipe) {
        this.pipe = pipe;
    }

    /**
     * Parses one or multiple {@code Command}-s from {@code inputs}.
     * If input is null, greets user and parses the command.
     * If parsing fails, then asks user to correct the input.
     *
     * @param inputs strings to parse {@code Command} from or null
     * @return parsed {@code Command}-s
     */
    protected LinkedList<Command> parseCommands(LinkedList<String> inputs) {
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

}
