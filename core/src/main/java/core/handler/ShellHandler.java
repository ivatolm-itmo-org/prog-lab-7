package core.handler;

import java.nio.channels.SelectableChannel;
import java.util.HashMap;
import java.util.LinkedList;

import core.command.Command;
import core.command.CommandType;
import core.models.IdValidator;
import core.parser.ArgumentCheckFailedException;
import core.parser.Parser;
import core.utils.SimpleParseException;

/**
 * Class providing user interactive shell.
 *
 * @author ivatolm
 */
public abstract class ShellHandler extends Handler<ChannelType> {

    // Command parser
    private Parser parser;

    // Accumulated result of parsing
    private LinkedList<Command> parsingResult;

    /**
     * Constructs new {@code ShellHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     */
    protected ShellHandler(HashMap<ChannelType, SelectableChannel> inputChannels,
                           HashMap<ChannelType, SelectableChannel> outputChannels) {
        super(inputChannels, outputChannels);
    }

    /**
     * Creates parser via {@idValidator}.
     *
     * @param idValidator validator for an id argument
     */
    public void setup(IdValidator idValidator) {
        this.parser = new Parser(idValidator);
    }

    /**
     * Implements {@code process} for {@code Handler}.
     */
    @Override
    public abstract void process(ChannelType channel);

    /**
     * Parses one or multiple {@code Command}-s from {@code inputs}.
     * If input is null or parsing fails, waits for being called again
     * with further user input.
     * Accumulated result of parsing is stored in {@code parsingResult}
     *
     * @param inputs strings to parse {@code Command} from or null
     */
    protected void parseCommands(LinkedList<String> inputs) {
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
                if (hasParsedCommands == true) {
                    this.parsingResult.addAll(this.parser.getResult());

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
