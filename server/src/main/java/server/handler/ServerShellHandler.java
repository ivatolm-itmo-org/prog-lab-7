package server.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.command.Command;
import core.command.arguments.Argument;
import core.handler.ChannelType;
import core.handler.ShellHandler;
import core.utils.NBChannelController;
import server.event.ServerEvent;
import server.event.ServerEventType;

enum ServerShellHandlerState {
    Waiting(true),
    InputParsingStart(false),
    InputParsingProcessing(false),
    InputParsingFinish(false),
    ComIdValidationStart(false),
    ComIdValidationWaiting(true),
    ComIdValidationFinish(false),
    ComReceiveOutput(false)
    ;

    private boolean isWaiting = false;

    ServerShellHandlerState(boolean isWaiting) {
        this.isWaiting = isWaiting;
    }

    boolean isWaiting() {
        return this.isWaiting;
    }
}

/**
 * Class providing user interactive shell.
 *
 * @author ivatolm
 */
public class ServerShellHandler extends ShellHandler<ServerShellHandlerState> {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger("ShellHandler");

    // Input from the System.in
    private String input;

    // Id for validation
    private Argument idArgForValidation;

    /**
     * Constructs new {@code ServerShellHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     */
    public ServerShellHandler(HashMap<ChannelType, SelectableChannel> inputChannels,
                              HashMap<ChannelType, SelectableChannel> outputChannels) {
        super(inputChannels, outputChannels, ServerShellHandlerState.Waiting);

        this.input = null;
        this.idArgForValidation = null;
    }

    @Override
    public void process(ChannelType channel) {
        logger.trace("New event from " + channel);

        switch (channel) {
            case Input:
            case Com:
                this.readyChannels.add(channel);
                break;
            default:
                System.err.println("Unexpected channel.");
                break;
            }

        this.handleEvents();
    }

    @Override
    protected void handleEvents() {
        logger.trace("State: " + this.getState());
        do {
            ServerShellHandlerState stState = this.getState();

            switch (this.getState()) {
                case Waiting:
                    this.handleWaitingState();
                    break;
                case InputParsingStart:
                    this.handleInputParsingStart();
                    break;
                case InputParsingProcessing:
                    this.handleInputParsingProcessing();
                    break;
                case InputParsingFinish:
                    this.handleInputParsingFinish();
                    break;
                case ComIdValidationStart:
                    this.handleComIdValidationStart();
                    break;
                case ComIdValidationWaiting:
                    this.handleComIdValidationWaiting();
                    break;
                case ComIdValidationFinish:
                    this.handleComIdValidationFinish();
                    break;
                case ComReceiveOutput:
                    this.handleComReceiveOutput();
                    break;
            }

            logger.trace("State: " + stState + " -> " + this.getState());
        } while (!this.getState().isWaiting());
    }

    private void handleWaitingState() {
        logger.debug("Ready channels count: " + this.readyChannels.size());
        if (this.readyChannels.isEmpty()) {
            return;
        }

        logger.debug("Ready channels: " + this.readyChannels);
        if (this.readyChannels.contains(ChannelType.Input)) {
            this.nextState(ServerShellHandlerState.InputParsingStart);
        }
    }

    private void handleInputParsingStart() {
        SourceChannel inputChannel = (SourceChannel) this.inputChannels.get(ChannelType.Input);
        try {
            this.input = (String) NBChannelController.read(inputChannel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ServerShellHandlerState.Waiting);
            return;
        }

        this.nextState(ServerShellHandlerState.InputParsingProcessing);
    }

    private void handleInputParsingProcessing() {
        logger.debug("Parsing command...");
        this.parseCommands(new LinkedList<>(Arrays.asList(input)));
        logger.debug("Parsing completed");

        logger.debug("Has argument for id validation: " + this.hasArgForIdValidation());
        if (this.hasArgForIdValidation()) {
            Argument arg = this.getArgForIdValidation();
            this.idArgForValidation = arg;
            this.nextState(ServerShellHandlerState.ComIdValidationStart);
        } else {
            this.nextState(ServerShellHandlerState.InputParsingFinish);
        }
    }

    private void handleInputParsingFinish() {
        if (this.hasParsingResult()) {
            SinkChannel comChannel = (SinkChannel) this.outputChannels.get(ChannelType.Com);
            LinkedList<Command> commands = this.getParsingResult();
            ServerEvent event = new ServerEvent(ServerEventType.NewCommandsReq, commands);

            try {
                NBChannelController.write(comChannel, event);
            } catch (IOException e) {
                System.err.println("Cannot write to the channel.");
                this.nextState(ServerShellHandlerState.Waiting);
                return;
            }
        }

        this.nextState(ServerShellHandlerState.Waiting);
    }

    private void handleComIdValidationStart() {
        if (this.idArgForValidation != null) {
            SinkChannel comChannel = (SinkChannel) this.outputChannels.get(ChannelType.Com);
            ServerEvent event = new ServerEvent(ServerEventType.IdValidationReq, this.idArgForValidation);

            try {
                NBChannelController.write(comChannel, event);
            } catch (IOException e) {
                System.err.println("Cannot write to the channel.");
                this.nextState(ServerShellHandlerState.Waiting);
                return;
            }

            try {
                this.filterSubscriptions(ChannelType.Com);
            } catch (IOException e) {
                logger.warn(e.getMessage());
            }

            this.nextState(ServerShellHandlerState.ComIdValidationWaiting);
        } else {
            this.nextState(ServerShellHandlerState.InputParsingProcessing);
        }
    }

    private void handleComIdValidationWaiting() {
        if (this.readyChannels.isEmpty()) {
            return;
        }

        if (!this.readyChannels.contains(ChannelType.Com)) {
            this.nextState(ServerShellHandlerState.ComIdValidationFinish);
        }
    }

    private void handleComIdValidationFinish() {
        SourceChannel comChannel = (SourceChannel) this.inputChannels.get(ChannelType.Com);
        ServerEvent event;
        try {
            event = (ServerEvent) NBChannelController.read(comChannel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ServerShellHandlerState.Waiting);
            return;
        }

        if (event.getType() == ServerEventType.IdValidationResp) {
            boolean result = (boolean) event.getData();
            this.setArgIdValidationResult(result);

            this.filterSubscriptions();
        } else {
            return;
        }

        this.nextState(ServerShellHandlerState.InputParsingProcessing);
    }

    private void handleComReceiveOutput() {
        SourceChannel comChannel = (SourceChannel) this.inputChannels.get(ChannelType.Com);
        ServerEvent event;
        try {
            event = (ServerEvent) NBChannelController.read(comChannel);
        } catch (IOException e) {
            // System.err.println("Cannot read from the channel.");
            this.nextState(ServerShellHandlerState.Waiting);
            return;
        }

        if (event.getType() == ServerEventType.NewCommandsResp) {
            @SuppressWarnings("unchecked")
            LinkedList<String> result = (LinkedList<String>) event.getData();

            for (String line : result) {
                System.out.println(line);
            }

            this.nextState(ServerShellHandlerState.ComReceiveOutput);
        } else {
            this.nextState(ServerShellHandlerState.Waiting);
        }
    }

}
