package core.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import core.command.Command;
import core.command.CommandType;
import core.command.arguments.ArgCheck;
import core.command.arguments.Argument;
import core.utils.SimpleParseException;

/**
 * Class for parsing commands.
 *
 * @author ivatolm
 */
public class Parser {

    /** Partially parsed command type */
    private CommandType cmdType = null;

    /** Partially parsed arguments */
    private LinkedList<Argument> args = null;

    /** List of parsed commands */
    private LinkedList<Command> result = new LinkedList<>();

    /** Argument for id validation */
    private Argument argForIdValidation = null;

    private LinkedList<String> remainingSlices = null;

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

        // Feeding in slices from before
        if (this.remainingSlices != null) {
            slices.addAll(0, this.remainingSlices);
        }

        // Allowing empty args
        if (slices.isEmpty()) {
            slices.add(null);
        }

        // New command
        if (this.cmdType == null) {
            this.cmdType = this.parseCommandType(slices);
            this.args = new LinkedList<>();
        }

        // New arguments
        if (this.cmdType.getArgsCount() - this.args.size() > 0) {
            this.remainingSlices = this.parseArguments(slices);
        }

        // Checking if there are no args left to wait for
        if (this.cmdType.getArgsCount() - this.args.size() == 0) {
            Command command = new Command(this.cmdType, this.args);

            this.cmdType = null;
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
    public String slim(String input) throws SimpleParseException {
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
    public LinkedList<String> split(String input) throws SimpleParseException {
        if (input == null) {
            throw new SimpleParseException("Cannot split null value.");
        }

        LinkedList<String> result = new LinkedList<>();

        int substringStartPtr = 0;
        String substringBuffer = new String();

        boolean escaping = false;
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);

            if (current == '\\') {
                escaping = true;
                continue;
            }

            if (("" + current).isBlank() && !escaping) {
                result.add(substringBuffer);
                substringBuffer = new String();
                substringStartPtr = i + 1;
            }

            substringBuffer += current;
            escaping = false;
        }

        if (substringStartPtr != input.length()) {
            result.add(input.substring(substringStartPtr, input.length()));
        }

        return result;
    }

    /**
     * Tries to match first argument to existing command types. If command type
     * was found returns it, else throws SimpleParseException.
     * If found null as first argument returns NOOP command type.
     * Also, if command type was found removes first argument from the provided list.
     *
     * @param slices input arguments
     * @return parsed command type
     * @throws SimpleParseException if command type wasn't found or slices is null
     */
    private CommandType parseCommandType(LinkedList<String> slices) throws SimpleParseException {
        CommandType result = null;

        if (slices != null && !slices.isEmpty()) {
            String label = slices.get(0);

            if (label == null) {
                result = CommandType.NOOP;
            }

            else {
                for (CommandType command : CommandType.values()) {
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
    private LinkedList<String> parseArguments(LinkedList<String> slices) throws ArgumentCheckFailedException {
        int argCnt = Math.min(this.cmdType.getArgsCount() - this.args.size(),
                              slices.size());

        for (int i = 0; i < argCnt; i++) {
            int argId = this.args.size();
            Argument argType = this.cmdType.getArgument(argId);

            // Using reflection to get constructor of the argument
            Constructor<?> constructor = null;
            try {
                Constructor<?>[] constructors = argType.getClass().getConstructors();
                for (Constructor<?> c : constructors) {
                    if (c.getParameterTypes().length == 0) {
                        constructor = c;
                        break;
                    }
                }
            } catch (SecurityException e) {
                throw new ArgumentCheckFailedException("Cannot create new instance of argument object.");
            }

            // Using reflection to create new instace of database object
            Argument arg = null;
            try {
                arg = (Argument) constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new ArgumentCheckFailedException("Cannot create new instance of argument object.");
            }

            ArgCheck f = argType.getCheck();

            String inputArg = slices.get(i);
            if (f.check(inputArg)) {
                try {
                    arg.parse(inputArg);
                } catch (Exception e) {
                    arg.setValue(null);
                }

                if (argType.getName().equalsIgnoreCase("id")) {
                    this.argForIdValidation = arg;
                    return new LinkedList<>(slices.subList(i + 1, slices.size()));
                } else {
                    this.args.add(arg);
                }
            } else {
                throw new ArgumentCheckFailedException(argType.getErrorMsg());
            }
        }

        return null;
    }

    /**
     * Sets {@code idValidationResult} to {@code status}.
     * If check passed, then adds argument to the list of parsed.
     *
     * @param result result of validation
     * @throws ArgumentCheckFailedException if argument check failed
     */
    public void setIdValidationResult(boolean result) throws ArgumentCheckFailedException {
        if (result) {
            this.args.add(this.argForIdValidation);
        } else {
            int argId = this.args.size();
            Argument argType = this.cmdType.getArgument(argId);
            throw new ArgumentCheckFailedException(argType.getErrorMsg());
        }

        this.argForIdValidation = null;
    }

    /**
     * Returns argument for id validation.
     *
     * @return argument if there is one, else null
     */
    public Argument getArgForIdValidation() {
        return this.argForIdValidation;
    }

    /**
     * Returns id validation state.
     *
     * @return true if validation needed, else false
     */
    public boolean needIdValidation() {
        return this.argForIdValidation != null;
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
     * @return partially parsed command type
     */
    public CommandType getCurrentCommandType() {
        return this.cmdType;
    }

    /**
     * @return number of parsed arguments
     */
    public int getCurrentArgumentsCnt() {
        return this.args.size();
    }

}
