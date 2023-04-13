package server.handler;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import core.command.Command;
import core.command.arguments.Argument;
import core.handler.ShellHandler;
import core.models.IdValidator;
import server.interpreter.Interpreter;
import server.runner.RecursionFoundException;
import server.runner.Runner;

/**
 * Class providing user interactive shell.
 *
 * @author ivatolm
 */
public class ServerShellHandler extends ShellHandler {

    /** User program runner */
    private Runner runner;

    /**
     * Constructs new {@code Shell} with provided arguments.
     */
    public ServerShellHandler(Runner runner) {
        super();

        this.runner = runner;

        IdValidator idValidator = (Argument arg) -> {
            return Interpreter.HasItemWithId(arg);
        };

        this.setup(idValidator);
    }

    /**
     * Runs interactive shell until EOF.
     * Work cycle:
     * 1. get user input
     * 2. send command to the pipe
     */
    @Override
    public void _run() {
        try {
            LinkedList<Command> commands = this.parseCommands(null);

            byte[] data = new byte[] { 1 };
            ByteBuffer buffer = ByteBuffer.wrap(data);
            this.syncWait(buffer);

            try {
                this.runner.addSubroutine(commands);
            } catch (RecursionFoundException e) {
                System.err.println("Recursion detected. Skipping...");
                return;
            }

            this.runner.run();

            LinkedList<String> output = this.runner.getProgramOutput();
            for (String commandOutput : output) {
                System.out.println(commandOutput);
            }
            LinkedList<String> result = this.runner.getProgramResult();
            if (result != null) {
                System.out.println("Cannot resolve dependency: " + result.getFirst());
            }

            this.syncNotify();
        } catch (NoSuchElementException e) {
            System.out.println("\nExiting by Ctrl-D (EOF)");
            this.running = false;
        }
    }

}
