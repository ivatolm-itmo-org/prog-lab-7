package com.ivatolm.app.shell;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.ivatolm.app.database.CSVDatabase;
import com.ivatolm.app.database.DataBase;
import com.ivatolm.app.interpreter.Interpreter;
import com.ivatolm.app.models.humanBeing.HumanBeing;
import com.ivatolm.app.parser.ArgumentCheckFailedException;
import com.ivatolm.app.parser.Command;
import com.ivatolm.app.parser.CommandType;
import com.ivatolm.app.parser.Parser;
import com.ivatolm.app.parser.SimpleParseException;

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

    /** Program runner */
    private Runner runner;

    /**
     * Constructs new {@code Shell} with provided arguments.
     *
     * @param filename database filename
     */
    public Shell(String filename) {
        DataBase<HumanBeing> database = new CSVDatabase<HumanBeing>(filename);

        this.scanner = new Scanner(System.in);
        this.parser = new Parser();

        Interpreter interpreter = new Interpreter(database);
        this.runner = new Runner(interpreter);
    }

    /**
     * Runs interactive shell until EOF.
     * Work cycle:
     * 1. get user input
     * 2. parse user input
     * 3. run command
     * 4. if command require more commands to be run,
     * then go to step 3
     */
    public void run() {
        try {
            while (true) {
                LinkedList<Command> commands = this.parseCommands(null);
                try {
                    this.runner.addSubroutine(commands);
                } catch (RecursionFoundException e) {
                    System.err.println(e.getMessage());
                    continue;
                }

                LinkedList<String> inputs;
                while ((inputs = this.runner.run()) != null) {
                    commands = this.parseCommands(inputs);

                    try {
                        this.runner.addSubroutine(commands);
                    } catch (RecursionFoundException e) {
                        System.err.println(e.getMessage());
                        break;
                    }
                }

                if (!this.runner.isRunning()) {
                    System.out.println("Exiting by 'Exit' command");
                    break;
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
