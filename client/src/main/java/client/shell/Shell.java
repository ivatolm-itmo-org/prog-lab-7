package client.shell;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import client.parser.ArgumentCheckFailedException;
import client.parser.Parser;
import core.command.Command;
import core.command.CommandType;
import core.net.Com;
import core.utils.SimpleParseException;

/**
 * Class providing user interactive shell.
 *
 * @author ivatolm
 */
public class Shell {

    /** Command line scanner */
    private Scanner scanner;

    /** Command parser */
    private Parser parser;

    /** Communication handler */
    private ComHandler comHandler;

    /**
     * Constructs new {@code Shell} with provided arguments.
     *
     * @param filename database filename
     */
    public Shell(Com com, ContentManager contentManager) {
        this.scanner = new Scanner(System.in);
        this.parser = new Parser();
        this.comHandler = new ComHandler(com, contentManager);
    }

    /**
     * Runs interactive shell until EOF.
     * Work cycle:
     * 1. get user input
     * 2. process commands on server
     * 3. print output to user
     */
    public void run() {
        try {
            while (true) {
                LinkedList<Command> commands = this.parseCommands(null);
                String[] output = this.comHandler.processCommands(commands);
                for (String commandOutput : output) {
                    System.out.println(commandOutput);
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nExiting by Ctrl-D (EOF)");
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
