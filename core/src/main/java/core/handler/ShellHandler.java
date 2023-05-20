package core.handler;

import java.nio.channels.SelectableChannel;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.Pair;

import core.command.Command;
import core.command.CommandType;
import core.command.arguments.Argument;
import core.parser.ArgumentCheckFailedException;
import core.parser.Parser;
import core.utils.SimpleParseException;

/**
 * Class providing user interactive shell.
 *
 * @author ivatolm
 */
public abstract class ShellHandler<S extends Enum<?>> extends Handler<ChannelType, S> {

    // Command parser
    private Parser parser;

    // Accumulated result of parsing
    private LinkedList<Command> parsingResult;

    // Argument for id validation
    private Argument argForIdValidation;

    // Id validation result
    private Boolean argIdValidationResult;

    /**
     * Constructs new {@code ShellHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param initState initial state of FSM
     */
    protected ShellHandler(LinkedList<Pair<ChannelType, SelectableChannel>> inputChannels,
                           LinkedList<Pair<ChannelType, SelectableChannel>> outputChannels,
                           S initState) {
        super(inputChannels, outputChannels, initState);
        this.parser = new Parser();
        this.parsingResult = null;
        this.argForIdValidation = null;
        this.argIdValidationResult = null;
    }

    /**
     * Implements {@code process} for {@code Handler}.
     */
    @Override
    public abstract void process(ChannelType type, SelectableChannel channel);

    /**
     * Prints initial greeting.
     */
    protected void showInputGreeting() {
        System.out.print(": ");
    }

    /**
     * Parses one or multiple {@code Command}-s from {@code inputs}.
     * If input is null or parsing fails, waits for being called again
     * with further user input.
     * Accumulated result of parsing is stored in {@code parsingResult}.
     * If id argument validation is required than sets {@code argForIdValidation} to
     * argument to be validated, which can be accessed via {@code getArgForIdValidation}.
     *
     * @param inputs strings to parse {@code Command} from or null
     */
    protected void parseCommands(LinkedList<String> inputs) {
        if (this.argForIdValidation != null) {
            if (this.argIdValidationResult != null) {
                this.argForIdValidation = null;

                try {
                    this.parser.setIdValidationResult(this.argIdValidationResult);
                } catch (ArgumentCheckFailedException e) {
                    // error will be printed later
                }
            } else {
                return;
            }
        }

        boolean promptRequired = inputs == null || inputs.isEmpty();
        while (true) {
            String input;
            if (promptRequired) {
                System.out.print(": ");
                return;
            } else {
                input = inputs.pop();
            }

            promptRequired = inputs == null || inputs.isEmpty();

            try {
                boolean hasParsedCommands = this.parser.parse(input);
                if (this.parser.needIdValidation()) {
                    Argument arg = this.parser.getArgForIdValidation().get();
                    this.argForIdValidation = arg;
                    this.argIdValidationResult = null;
                    return;
                }

                if (hasParsedCommands == true) {
                    if (this.parsingResult == null) {
                        this.parsingResult = new LinkedList<>();
                    }

                    this.parsingResult.addAll(this.parser.getResult().get());

                    if (promptRequired) {
                        return;
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
     * Sets argument id validation result.
     *
     * @param result of argument id validation
     */
    protected void setArgIdValidationResult(boolean result) {
        this.argIdValidationResult = result;
    }

    /**
     * Returns argument for id validation.
     *
     * @return argument to be validated
     */
    protected Argument getArgForIdValidation() {
        return this.argForIdValidation;
    }

    /**
     * Returns need of validating argument id.
     *
     * @return true if needed, else false
     */
    protected boolean hasArgForIdValidation() {
        return this.argForIdValidation != null;
    }

    /**
     * Returns status of parsing result.
     *
     * @return true if has result, else false
     */
    protected boolean hasParsingResult() {
        if (this.parsingResult == null) {
            return false;
        }

        return !this.parsingResult.isEmpty();
    }

    /**
     * Returns accumulated parsing result or null if there isn't one.
     * Consumes result.
     *
     * @return parsed commands
     */
    protected LinkedList<Command> getParsingResult() {
        LinkedList<Command> result = this.parsingResult;
        this.parsingResult = null;
        return result;
    }

}
