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

    /** Partially parsed command */
    private Command cmd = null;

    /** Partially parsed arguments */
    private LinkedList<Argument> args = null;

    /** List of parsed commands */
    private LinkedList<Command> result = new LinkedList<>();

    /**
     * Parsing input string containing command or its arguments.
     * If required input is abscent, then {@code SimpleParseException} is thrown.
     *
     * TODO: Detailed explanation of the process.
     *
     * @param input string containing command or its arguments
     * @return true if command parsing completed, else false
     * @throws SimpleParseException if error occured in parsing or command not found
     * @throws ArgumentCheckFailedException if argument check failed
     */
    public boolean parse(String input) throws SimpleParseException, ArgumentCheckFailedException {
        String slimmedString = this.slim(input);
        LinkedList<String> slices = this.split(slimmedString);

        // Allowing empty args
        if (slices.isEmpty()) {
            slices.add(null);
        }

        // New command
        if (this.cmd == null) {
            this.cmd = this.parseCommand(slices);
            this.args = new LinkedList<>();
        }

        // New arguments
        if (this.cmd.getArgsCount() - this.args.size() > 0) {
            LinkedList<Argument> arguments = this.parseArguments(slices);

            for (Argument arg : arguments) {
                this.args.add(arg);
            }
        }

        // Checking if there are no args left to wait for
        if (this.cmd.getArgsCount() - this.args.size() == 0) {
            Command command = this.cmd;
            command.setArgs(this.args);

            this.cmd = null;
            this.args = null;

            this.result.add(command);
        }

        return !this.result.isEmpty();
    }

    /**
     * Replaces all '\n' chatacter with spaces and replaces multiple
     * spaces with just one.
     *
     * @return slimmed string
     * @throws SimpleParseException if input is null
     */
    private String slim(String input) throws SimpleParseException {
        if (input == null) {
            throw new SimpleParseException("Cannot slim null value.");
        }

        String result = input.strip();

        result = result.replaceAll("\n", " ");
        result = result.replaceAll("\\s+", " ");

        return result;
    }

    /**
     * Splits slimmed string by spaces with support of escaping space
     * with a backslash.
     *
     * @param input slimmed string
     * @return splitted string
     * @throws SimpleParseException if input is null
     */
    private LinkedList<String> split(String input) throws SimpleParseException {
        if (input == null) {
            throw new SimpleParseException("Cannot split null value.");
        }

        LinkedList<String> result = new LinkedList<>();

        int substringStartPtr = 0;
        boolean escaping = false;
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);

            if (current == '\\') {
                escaping = true;
                continue;
            }

            if (current == ' ' && !escaping) {
                result.add(input.substring(substringStartPtr, i));
                substringStartPtr = i + 1;
            }

            escaping = false;
        }

        if (substringStartPtr != input.length()) {
            result.add(input.substring(substringStartPtr, input.length()));
        }

        return result;
    }

    /**
     * Tries to match first argument to existing commands. If command
     * was found returns actual command else throws SimpleParseException.
     * If found null as first argument returns NOOP command.
     * Also, if command was found removes first argument from the provided list.
     *
     * @param slices input arguments
     * @return parsed command
     * @throws SimpleParseException if command wasn't found or slices is null
     */
    private Command parseCommand(LinkedList<String> slices) throws SimpleParseException {
        Command result = null;

        if (slices != null && !slices.isEmpty()) {
            String label = slices.get(0);

            if (label == null) {
                result = Command.NOOP;
            }

            else {
                for (Command command : Command.values()) {
                    String commandLabel = command.name().toLowerCase();

                    if (commandLabel.equals(label)) {
                        result = command;
                        break;
                    }
                }
            }
        }

        if (result == null) {
            throw new SimpleParseException("Provided command doesn't exist.");
        }

        // Removing command label from args
        slices.pop();

        return result;
    }

    /**
     * Tries to parse arguments from provided input args. If it failes,
     * then throws ArgumentCheckFailedException. Parser will wait for next input to
     * try again.
     *
     * @param slices input arguments
     * @return parsed arguments
     * @throws ArgumentCheckFailedException if argument check failed
     */
    private LinkedList<Argument> parseArguments(LinkedList<String> slices) throws ArgumentCheckFailedException {
        LinkedList<Argument> result = new LinkedList<>();

        int argCnt = Math.min(this.cmd.getArgsCount() - this.args.size(),
                              slices.size());

        for (int i = 0; i < argCnt; i++) {
            int argId = this.args.size();
            Argument arg = this.cmd.getArgument(argId);

            ArgCheck f = arg.getCheck();

            String inputArg = slices.get(i);
            if (f.check(inputArg)) {
                try {
                    arg.parse(inputArg);
                } catch (Exception e) {
                    arg.setValue(null);
                }

                result.add(arg);
            } else {
                throw new ArgumentCheckFailedException(arg.getErrorMsg());
            }
        }

        return result;
    }

    /**
     * Returns parsed commands with removal from parser.
     *
     * @return parsed commands or null, if no command was parsed yet
     */
    public LinkedList<Command> getResult() {
        LinkedList<Command> result = this.result;
        this.result = new LinkedList<>();

        return result.isEmpty() ? null : result;
    }

    /**
     * @return partially parsed command
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

}
