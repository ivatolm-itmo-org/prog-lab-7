package com.ivatolm.app.parser;

import java.util.LinkedList;

import com.ivatolm.app.parser.arguments.ArgCheck;
import com.ivatolm.app.parser.arguments.Argument;
import com.ivatolm.app.utils.SimpleParseException;

/**
 * Class for parsing commands.
 *
 * @author ivatolm
 */
public class Parser {

    /** Do we parse command or its arguments? */
    private boolean waitingArgs = false;


    /** Partially parsed command */
    private Command cmd;

    /** Partially parsed arguments */
    private LinkedList<Argument> args;

    /** Id argument (urgent validation) */
    private Argument idArg = null;

    /** Parsed command */
    private Command result;

    /**
     * Parsing input string containing command or its arguments.
     * If required input is abscent, then {@code SimpleParseException} is thrown.
     *
     * TODO: Detailed explanation of the process.
     *
     * @param input string containing command or its arguments
     * @return true if command parsing completed, else false
     * @throws SimpleParseException if error occures
     */
    public boolean parse(String input) throws SimpleParseException {
        LinkedList<String> inputArgs = this.split(input);

        // Received empty command
        if (inputArgs.size() == 0 && !this.waitingArgs) {
            this.cmd = Command.NOOP;
            this.args = new LinkedList<>();

            return true;
        }

        // Allowing empty args
        if (inputArgs.size() == 0) {
            inputArgs.add(null);
        }

        // New command
        if (!this.waitingArgs) {
            this.cmd = this.parseCommand(inputArgs);

            this.waitingArgs = true;
            this.args = new LinkedList<>();
        }

        // New arguments
        if (this.waitingArgs) {
            LinkedList<Argument> arguments = this.parseArguments(inputArgs);

            for (Argument arg : arguments) {
                this.args.add(arg);
            }
        }

        // Checking if there any args left to wait
        if (this.cmd.getArgsCount() - this.args.size() == 0) {
            this.waitingArgs = false;

            // Create command
            this.result = this.cmd;
            this.result.setArgs(this.args);

            return true;
        } else {
            return false;
        }
    }

    /**
     * Splits string separated by one or more spaces.
     *
     * @param input input string
     * @return splitted string
     */
    private LinkedList<String> split(String input) {
        String strippedInput = input.strip();

        LinkedList<String> inputArgs = new LinkedList<>();
        boolean escaping = false;
        for (int i = 0; i < strippedInput.length(); i++) {
            if (strippedInput.charAt(i) == '\\') {
                escaping = true;
                continue;
            }

            if (strippedInput.charAt(i) == ' ' && !escaping) {
                if (inputArgs.getLast() != "") {
                    inputArgs.add("");
                }
            } else {
                if (inputArgs.size() == 0) {
                    inputArgs.add("");
                }

                inputArgs.set(inputArgs.size() - 1, inputArgs.getLast() + strippedInput.charAt(i));
            }

            escaping = false;
        }

        return inputArgs;
    }

    /**
     * Tries to match first argument to existing commands. If command
     * was found returns actual command, else throws SimpleParseException.
     * Also, if command was found removes first argument from the provided list.
     *
     * @param args input arguments
     * @return parsed command
     * @throws SimpleParseException if command wasn't found
     */
    private Command parseCommand(LinkedList<String> args) throws SimpleParseException {
        // Checking if given command exists
        Command result = null;

        String cmdLabel = args.get(0);
        for (Command cmd : Command.values()) {
            if (cmdLabel.equals(cmd.name().toLowerCase())) {
                result = cmd;
                break;
            }
        }

        if (result == null) {
            throw new SimpleParseException("Provided command doesn't exist.");
        }

        // Removing command label from args
        args.pop();

        return result;
    }

    /**
     * Tries to parse arguments from provided input args. If it failes,
     * then nothing special happens. Parser will wait for next input to
     * try again.
     *
     * @param args input arguments
     * @return parsed arguments
     */
    private LinkedList<Argument> parseArguments(LinkedList<String> args) {
        LinkedList<Argument> result = new LinkedList<>();

        // Validating arguments
        int argCnt = Math.min(this.cmd.getArgsCount() - this.args.size(),
                              args.size());

        for (int i = 0; i < argCnt; i++) {
            String inputArg = args.get(i);
            int argId = this.args.size();

            Argument arg = this.cmd.getArgument(argId);

            ArgCheck f = arg.getCheck();
            if (f.check(inputArg)) {
                try {
                    arg.parse(inputArg);
                } catch (Exception e) {
                    arg.setValue(null);
                }

                if (arg.getName().equalsIgnoreCase("id")) {
                    this.idArg = arg;
                }

                result.add(arg);
            } else {
                System.err.println(arg.getErrorMsg());
            }
        }

        return result;
    }

    /**
     * Removes last parsed argument.
     */
    public void raiseNoSuchId() {
        this.args.removeLast();
        System.out.println("There is no element with such id in collection.");
    }

    /**
     * @return parsed command or null, if command was not parsed yet
     */
    public Command getResult() {
        return this.result;
    }

    /**
     * @return parsed command or partially parsed command
     */
    public Command getCurrentCommand() {
        return this.cmd;
    }

    /**
     * @return number of parsed arguments
     */
    public int getCurrentArgumentsCnt() {
        return this.args.size();
    }

    /**
     * @return id argument or null, if no such argument
     */
    public Argument getIdArgument() {
        Argument arg = this.idArg;
        this.idArg = null;
        return arg;
    }

}
