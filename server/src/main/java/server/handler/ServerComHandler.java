package server.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.event.ServerEvent;
import server.event.ServerEventType;
import server.interpreter.Interpreter;
import server.runner.RecursionFoundException;
import server.runner.Runner;
import core.command.Command;
import core.command.arguments.Argument;
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
    Error
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
    private ServerEvent event;

    // Program runner
    private Runner runner;

    /**
     * Constructs new {@code ServerComHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param runner program runner
     */
    public ServerComHandler(HashMap<ChannelType, SelectableChannel> inputChannels,
                            HashMap<ChannelType, SelectableChannel> outputChannels,
                            Runner runner) {
        super(inputChannels, outputChannels, ServerComHandlerState.Waiting);

        this.runner = runner;
    }

    @Override
    public void process(ChannelType type, SelectableChannel channel) {
        logger.trace("New event from " + type);

        switch (type) {
            case Network:
                this.readyChannels.add(type);
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
            }

            logger.trace("State: " + stState + " -> " + this.getState());
        } while (this.getState() != ServerComHandlerState.Waiting);
    }

    private void handleWaitingState() {
        if (this.readyChannels.isEmpty()) {
            return;
        }

        if (this.readyChannels.contains(ChannelType.Network)) {
            this.nextState(ServerComHandlerState.NewEvent);
        }
    }

    private void handleNewEvent() {
        if (this.event == null) {
            try {
                this.filterSubscriptions(ChannelType.Network);
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
        SourceChannel channel = (SourceChannel) this.inputChannels.get(ChannelType.Network);
        try {
            this.event = (ServerEvent) NBChannelController.read(channel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
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
        this.stateData = new ServerEvent(ServerEventType.ScriptRequest, commands);

        this.nextState(ServerComHandlerState.NCProcessing);
    }

    private void handleExistingRequest() {
        SourceChannel channel = (SourceChannel) this.inputChannels.get(ChannelType.Network);
        ServerEvent response = null;
        try {
            response = (ServerEvent) NBChannelController.read(channel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ServerComHandlerState.Error);
            return;
        }

        this.stateData = response;

        switch (response.getType()) {
            case NewCommands:
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
        ServerEvent respIV = new ServerEvent(ServerEventType.IdValidation, result);

        SinkChannel channel = (SinkChannel) this.outputChannels.get(ChannelType.Network);
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
        ServerEvent response = (ServerEvent) this.stateData;

        @SuppressWarnings("unchecked")
        LinkedList<Command> commands = (LinkedList<Command>) response.getData();

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

        ServerEvent respNC = null;
        if (errorOccured) {
            LinkedList<String> programOutput = this.runner.getProgramOutput();
            if (programOutput == null) {
                programOutput = new LinkedList<>();
            }

            programOutput.push(errorMessage);
            respNC = new ServerEvent(ServerEventType.OutputResponse, programOutput);
            this.nextState(ServerComHandlerState.FinishRequest);
        } else {
            LinkedList<String> programResult = this.runner.getProgramResult();
            if (programResult != null) {
                respNC = new ServerEvent(ServerEventType.ScriptRequest, programResult);
                this.nextState(ServerComHandlerState.Waiting);
            } else {
                LinkedList<String> programOutput = this.runner.getProgramOutput();
                if (programOutput == null) {
                    programOutput = new LinkedList<>();
                }

                respNC = new ServerEvent(ServerEventType.OutputResponse, programOutput);
                this.nextState(ServerComHandlerState.FinishRequest);
            }
        }

        SinkChannel channel = (SinkChannel) this.outputChannels.get(ChannelType.Network);
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

}
