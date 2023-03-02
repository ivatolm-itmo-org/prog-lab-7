package com.ivatolm.app.shell;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import com.ivatolm.app.database.CSVDatabase;
import com.ivatolm.app.database.DataBase;
import com.ivatolm.app.interpreter.Interpreter;
import com.ivatolm.app.models.HumanBeing;
import com.ivatolm.app.parser.Command;
import com.ivatolm.app.parser.Parser;
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

    /** Command interpreter */
    private Interpreter interpreter;

    /**
     * Constructs new shell with provided arguments.
     *
     * @param filename database filename
     */
    public Shell(String filename) {
        DataBase<HumanBeing> database = new CSVDatabase<HumanBeing>(filename);

        this.scanner = new Scanner(System.in);
        this.parser = new Parser();
        this.interpreter = new Interpreter(database);
    }

    /**
     * Runs interactive shell until EOF.
     * Work cycle:
     *  - get user input
     *  - parse command
     *    continue if parsed, else go to previous step
     *  - execute command
     *    print output to user
     */
    public void run() {
        try {
            while (true) {
                LinkedList<Command[]> commandQueue = new LinkedList<>();

                while (true) {
                    System.out.print(": ");
                    String input = this.scanner.nextLine();

                    boolean result = false;
                    try {
                        result = this.parser.parse(input);
                    } catch (SimpleParseException e) {
                        System.err.println(e.getMessage());
                        continue;
                    }

                    if (result == false) {
                        Command cmd = this.parser.getCurrentCommand();
                        int argCnt = this.parser.getCurrentArgumentsCnt();

                        String greeting = "Enter" + " " + "'" + cmd.getArgument(argCnt).getGreetingMsg() + "'";
                        System.out.print(greeting);
                    } else {
                        Command cmd = this.parser.getCurrentCommand();
                        Command[] subQueue = new Command[] { cmd };
                        commandQueue.add(subQueue);
                        break;
                    }
                }

                // TODO: Sending to the server for remote processing
                while (commandQueue.size() > 0) {
                    Command[] subQueue = commandQueue.pop();

                    for (Command cmd : subQueue) {
                        String[] subInput = this.interpreter.exec(cmd);

                        if (subInput == null) {
                            continue;
                        }

                        try {
                            LinkedList<Command> newSubQueue = new LinkedList<>();

                            for (String input : subInput) {
                                boolean result = this.parser.parse(input);

                                if (!result) {
                                    throw new SimpleParseException("Incomplete command inside subInput.");
                                }

                                Command newCmd = this.parser.getCurrentCommand();
                                newSubQueue.add(newCmd);
                            }

                            commandQueue.add(newSubQueue.toArray(new Command[0]));
                        } catch (SimpleParseException e) {
                            System.out.println("Cannot parse command from script. Skipping...");
                            break;
                        }
                    }
                }

            }
        } catch(NoSuchElementException e) {
            System.out.println("\nExiting by Ctrl-D (EOF)");
        }
    }

    /**
     * Close shell.
     * Closes internal connections.
     */
    public void close() {
        this.scanner.close();
    }

}
