package com.ivatolm.app.shell;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.ivatolm.app.database.CSVDatabase;
import com.ivatolm.app.database.DataBase;
import com.ivatolm.app.interpreter.Interpreter;
import com.ivatolm.app.models.humanBeing.HumanBeing;
import com.ivatolm.app.parser.Command;
import com.ivatolm.app.parser.Parser;
import com.ivatolm.app.parser.arguments.Argument;
import com.ivatolm.app.utils.SimpleParseException;

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
     *  1. get user input
     *  2. parse user input
     *  3. run command
     *  4. if command require more commands to be run,
     *     then go to step 3
     */
    public void run() {
        try {
            while (true) {
                Command command = this.parseCommand(null);
                try {
                    this.runner.addCommand(command);
                } catch (RecursionFoundException e) {
                    System.err.println(e.getMessage());
                    continue;
                }

                String[] inputs;
                while ((inputs = this.runner.run()) != null) {
                    LinkedList<Command> commands = this.parseCommands(inputs);

                    try {
                        this.runner.addSubroutine(commands);
                    } catch(RecursionFoundException e) {
                        System.err.println(e.getMessage());
                        break;
                    }
                }
            }
        } catch(NoSuchElementException e) {
            System.out.println("\nExiting by Ctrl-D (EOF)");
        }
    }

    /**
     * Parses {@code Command} from {@code input}.
     * If input is null, greets user and parses the command.
     * If parsing fails, then asks user to correct the input.
     *
     * @param input string to parse {@code Command} from or null
     * @return parsed {@code Command}
     */
    private Command parseCommand(String input) {
        while (true) {
            if (input == null) {
                System.out.print(": ");
                input = this.scanner.nextLine();
            }

            boolean result = false;
            try {
                result = this.parser.parse(input);

            } catch (SimpleParseException e) {
                System.err.println(e.getMessage());
                input = null;
                continue;
            }

            if (result == true) {
                return this.parser.getCurrentCommand();
            }

            Command cmd = this.parser.getCurrentCommand();
            int argCnt = this.parser.getCurrentArgumentsCnt();

            Argument id = this.parser.getIdArgument();
            if (id != null) {
                if (!Interpreter.HasItemWithId(id)) {
                    this.parser.raiseNoSuchId();
                    argCnt--;
                }
            }

            String greeting = "Enter" + " " + "'" + cmd.getArgument(argCnt).getGreetingMsg() + "'";
            System.out.print(greeting);
            input = null;
        }
    }

    /**
     * Parses multiple commands sequentially using {@code parseCommand}.
     *
     * @param inputs strings to parse {@code Command}-s from
     * @return list of parsed commands
     */
    private LinkedList<Command> parseCommands(String[] inputs) {
        LinkedList<Command> result = new LinkedList<>();

        for (String input : inputs) {
            Command command = this.parseCommand(input);
            result.add(command);
        }

        return result;
    }

    /**
     * Closes shell.
     * Closes internal connections.
     */
    public void close() {
        this.scanner.close();
    }

}
