package server.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.util.LinkedList;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.interpreter.Interpreter;
import server.runner.RecursionFoundException;
import server.runner.Runner;
import core.command.Command;
import core.command.arguments.Argument;
import core.event.Event;
import core.event.EventType;
import core.handler.ChannelType;
import core.handler.ComHandler;
import core.utils.NBChannelController;

enum ServerComHandlerState {
    Waiting,
    NewEvent,
    NewRequest,
    IVStart,
    NCStart,
    ExistingRequest,
    IVProcessing,
    NCProcessing,
    FinishRequest,
    Error,
    Close
}

/**
 * Class for handling server side communication between client and server.
 *
 * @author ivatolm
 */
public class ServerComHandler extends ComHandler<ServerComHandlerState> {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger("ComHandler");

    // State communication data
    private Object stateData;

    // Currently processed event
    private Event event;

    // Program runner
    private Runner runner;

    // Communication channel type
    private ChannelType channelType;

    /**
     * Constructs new {@code ServerComHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param runner program runner
     * @param type communication channel type
     */
    public ServerComHandler(LinkedList<Pair<ChannelType, SelectableChannel>> inputChannels,
                            LinkedList<Pair<ChannelType, SelectableChannel>> outputChannels,
                            Runner runner,
                            ChannelType type) {
        super(inputChannels, outputChannels, ServerComHandlerState.Waiting);

        this.runner = runner;
        this.channelType = type;
    }

    @Override
    public void process(ChannelType type, SelectableChannel channel) {
        logger.trace("New event from " + type);

        if (this.channelType == type) {
            this.readyChannels = new LinkedList<ChannelType>() {{ add(type); }};
        } else {
            System.err.println("Unexpected channel.");
        }

        this.handleEvents();
    }

    @Override
    protected void handleEvents() {
        logger.trace("State: " + this.getState());
        do {
            ServerComHandlerState stState = this.getState();

            switch (this.getState()) {
                case Waiting:
                    this.handleWaitingState();
                    break;
                case NewEvent:
                    this.handleNewEvent();
                    break;
                case NewRequest:
                    this.handleNewRequest();
                    break;
                case IVStart:
                    this.handleIVStart();
                    break;
                case NCStart:
                    this.handleNCStart();
                    break;
                case ExistingRequest:
                    this.handleExistingRequest();
                    break;
                case IVProcessing:
                    this.handleIVProcessing();
                    break;
                case NCProcessing:
                    this.handleNCProcessing();
                    break;
                case FinishRequest:
                    this.handleFinishRequest();
                    break;
                case Error:
                    this.handleError();
                    break;
                case Close:
                    this.handleClose();
                    break;
            }

            logger.trace("State: " + stState + " -> " + this.getState());
        } while (this.getState() != ServerComHandlerState.Waiting);
    }

    private void handleWaitingState() {
        if (this.readyChannels.isEmpty()) {
            return;
        }

        if (this.readyChannels.contains(this.channelType)) {
            this.nextState(ServerComHandlerState.NewEvent);
        }
    }

    private void handleNewEvent() {
        if (this.event == null) {
            try {
                this.filterSubscriptions(this.channelType);
            } catch (IOException e) {
                this.nextState(ServerComHandlerState.Error);
                return;
            }

            this.nextState(ServerComHandlerState.NewRequest);
        } else {
            this.nextState(ServerComHandlerState.ExistingRequest);
        }
    }

    private void handleNewRequest() {
        Optional<SelectableChannel> ic = this.getFirstInputChannel(this.channelType);
        if (!ic.isPresent()) {
            logger.warn("Input channel " + this.channelType + " was not found.");
            this.nextState(ServerComHandlerState.Error);
            return;
        }

        SourceChannel channel = (SourceChannel) ic.get();
        try {
            this.event = (Event) NBChannelController.read(channel);
        } catch (IOException e) {
            logger.warn("Cannot read from the channel.");
            this.nextState(ServerComHandlerState.Error);
            return;
        }

        switch (this.event.getType()) {
            case IdValidation:
                this.nextState(ServerComHandlerState.IVStart);
                break;
            case NewCommands:
                this.nextState(ServerComHandlerState.NCStart);
                break;
            case Close:
                this.nextState(ServerComHandlerState.Close);
                break;
            default:
                this.nextState(ServerComHandlerState.Error);
                break;
        }
    }

    private void handleIVStart() {
        Argument id = (Argument) this.event.getData();

        this.stateData = id;

        this.nextState(ServerComHandlerState.IVProcessing);
    }

    private void handleNCStart() {
        @SuppressWarnings("unchecked")
        LinkedList<Command> commands = (LinkedList<Command>) this.event.getData();

        // Sending commands to server as it was response to the script request
        this.stateData = new Event(EventType.ScriptRequest, commands);

        this.nextState(ServerComHandlerState.NCProcessing);
    }

    private void handleExistingRequest() {
        Optional<SelectableChannel> ic = this.getFirstInputChannel(this.channelType);
        if (!ic.isPresent()) {
            logger.warn("Input channel " + this.channelType + " was not found.");
            this.nextState(ServerComHandlerState.Error);
            return;
        }

        SourceChannel channel = (SourceChannel) ic.get();
        Event response = null;
        try {
            response = (Event) NBChannelController.read(channel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ServerComHandlerState.Error);
            return;
        }

        this.stateData = response;

        // logger.info("Response type: " + response.getType());
        switch (response.getType()) {
            case NewCommands:
                this.nextState(ServerComHandlerState.NCProcessing);
                break;
            case ScriptRequest:
                // logger.info("" + response.getData());
                this.nextState(ServerComHandlerState.NCProcessing);
                break;
            default:
                this.nextState(ServerComHandlerState.Error);
                break;
        }
    }

    private void handleIVProcessing() {
        Argument id = (Argument) this.stateData;

        boolean result = Interpreter.HasItemWithId(id);
        logger.debug("Id validation result: " + result);

        Event respIV = new Event(EventType.IdValidation, result);

        Optional<SelectableChannel> oc = this.getFirstOutputChannel(this.channelType);
        if (!oc.isPresent()) {
            logger.warn("Output channel " + this.channelType + " was not found.");
            this.nextState(ServerComHandlerState.Error);
            return;
        }

        SinkChannel channel = (SinkChannel) oc.get();
        try {
            NBChannelController.write(channel, respIV);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ServerComHandlerState.Error);
            return;
        }

        this.nextState(ServerComHandlerState.FinishRequest);
    }

    private void handleNCProcessing() {
        Event response = (Event) this.stateData;

        @SuppressWarnings("unchecked")
        LinkedList<Command> commands = (LinkedList<Command>) response.getData();

        if (commands == null) {
            logger.info("File wasn't found on client. Sending output...");
            LinkedList<String> programOutput = this.runner.getProgramOutput();
            if (programOutput == null) {
                programOutput = new LinkedList<>();
            }

            Event respNC = new Event(EventType.OutputResponse, programOutput);

            Optional<SelectableChannel> oc = this.getFirstOutputChannel(this.channelType);
            if (!oc.isPresent()) {
                logger.warn("Output channel " + this.channelType + " was not found.");
                this.nextState(ServerComHandlerState.Error);
                return;
            }

            SinkChannel channel = (SinkChannel) oc.get();
            try {
                NBChannelController.write(channel, respNC);
            } catch (IOException e) {
                System.err.println("Cannot write to the channel.");
                this.nextState(ServerComHandlerState.Error);
                return;
            }
            this.nextState(ServerComHandlerState.FinishRequest);
            return;
        }

        boolean errorOccured = false;
        String errorMessage = null;
        try {
            this.runner.addSubroutine(commands);
            this.runner.run();
        } catch (RecursionFoundException e) {
            logger.info(e.getMessage());
            errorOccured = true;
            errorMessage = e.getMessage();
        }

        Event respNC = null;
        if (errorOccured) {
            LinkedList<String> programOutput = this.runner.getProgramOutput();
            if (programOutput == null) {
                programOutput = new LinkedList<>();
            }

            programOutput.push(errorMessage);
            respNC = new Event(EventType.OutputResponse, programOutput);
            this.nextState(ServerComHandlerState.FinishRequest);
        } else {
            LinkedList<String> programResult = this.runner.getProgramResult();
            if (programResult != null) {
                respNC = new Event(EventType.ScriptRequest, programResult);
                this.nextState(ServerComHandlerState.Waiting);
            } else {
                LinkedList<String> programOutput = this.runner.getProgramOutput();
                if (programOutput == null) {
                    programOutput = new LinkedList<>();
                }

                respNC = new Event(EventType.OutputResponse, programOutput);
                this.nextState(ServerComHandlerState.FinishRequest);
            }
        }

        Optional<SelectableChannel> oc = this.getFirstOutputChannel(this.channelType);
        if (!oc.isPresent()) {
            logger.warn("Output channel " + this.channelType + " was not found.");
            this.nextState(ServerComHandlerState.Error);
            return;
        }

        SinkChannel channel = (SinkChannel) oc.get();
        try {
            NBChannelController.write(channel, respNC);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ServerComHandlerState.Error);
            return;
        }
    }

    private void handleFinishRequest() {
        this.event = null;
        this.stateData = null;

        this.filterSubscriptions();

        this.nextState(ServerComHandlerState.Waiting);
    }

    private void handleError() {
        // TODO: close connection

        logger.warn("Error occured while processing the last state. Resetting...");

        this.nextState(ServerComHandlerState.FinishRequest);
    }

    private void handleClose() {
        this.setNotRunning();

        this.nextState(ServerComHandlerState.Waiting);
    }

}
