package server.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.auth.AuthManager;
import server.interpreter.Interpreter;
import server.runner.RecursionFoundException;
import server.runner.Runner;
import core.command.Command;
import core.command.arguments.Argument;
import core.event.Event;
import core.event.EventType;
import core.handler.ChannelType;
import core.handler.ComHandler;
import core.handler.HandlerChannels;
import core.utils.ChannelNotFoundException;
import core.utils.NBChannelController;

enum ServerComHandlerState {
    Waiting,
    NewEvent,
    NewRequest,
    LVStart,
    IVStart,
    NCStart,
    ExistingRequest,
    LVProcessing,
    IVProcessing,
    NCProcessing,
    FinishRequest,
    Error,
    AuthError,
    Close,
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

    // Session token
    private String token;

    /**
     * Constructs new {@code ServerComHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param runner program runner
     * @param type communication channel type
     */
    public ServerComHandler(HandlerChannels inputChannels,
                            HandlerChannels outputChannels,
                            Runner runner,
                            ChannelType type) {
        super(inputChannels, outputChannels, ServerComHandlerState.Waiting);

        this.runner = runner;
        this.channelType = type;
        this.token = null;
    }

    @Override
    public void process(ChannelType type, SelectableChannel channel) {
        logger.trace("New event from {}", type);

        if (this.channelType == type) {
            this.readyChannels = new LinkedList<ChannelType>() {{ add(type); }};
        } else {
            logger.warn("Unexpected channel.");
        }

        this.handleEvents();
    }

    @Override
    protected void handleEvents() {
        logger.trace("State: {}", this.getState());
        do {
            ServerComHandlerState stState = this.getState();

            try {
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
                    case LVStart:
                        this.handleLVStart();
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
                    case LVProcessing:
                        this.handleLVProcessing();
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
                    case AuthError:
                        this.handleAuthError();
                        break;
                    case Error:
                        this.handleError();
                        break;
                    case Close:
                        this.handleClose();
                        break;
                }
            } catch(IOException | ChannelNotFoundException e) {
                logger.warn(e.getMessage());
                this.nextState(ServerComHandlerState.Error);
            }

            logger.trace("State: {} -> {}", stState, this.getState());
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

    private void handleNewRequest()
        throws IOException, ChannelNotFoundException
    {
        SourceChannel ic = (SourceChannel) this.getFirstInputChannel(this.channelType);

        this.event = (Event) NBChannelController.read(ic);

        if (!AuthManager.auth(this.event, this.token)) {
            this.nextState(ServerComHandlerState.AuthError);
            return;
        }

        logger.debug("Request type: {}", this.event.getType());
        switch (this.event.getType()) {
            case LoginValidation:
                this.nextState(ServerComHandlerState.LVStart);
                break;
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

    private void handleLVStart() {
        @SuppressWarnings("unchecked")
        Pair<String, String> credentials = (Pair<String, String>) this.event.getData();

        this.stateData = credentials;

        this.nextState(ServerComHandlerState.LVProcessing);
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

    private void handleExistingRequest()
        throws IOException, ChannelNotFoundException
    {
        SourceChannel ic = (SourceChannel) this.getFirstInputChannel(this.channelType);

        Event response = (Event) NBChannelController.read(ic);

        this.stateData = response;

        switch (response.getType()) {
            case NewCommands:
                this.nextState(ServerComHandlerState.NCProcessing);
                break;
            case ScriptRequest:
                this.nextState(ServerComHandlerState.NCProcessing);
                break;
            default:
                this.nextState(ServerComHandlerState.Error);
                break;
        }
    }

    private void handleLVProcessing()
        throws IOException, ChannelNotFoundException
    {
        @SuppressWarnings("unchecked")
        Pair<String, String> credentials = (Pair<String, String>) this.event.getData();

        this.token = AuthManager.login(credentials.getLeft(), credentials.getRight());
        if (this.token.equals("")) {
            this.nextState(ServerComHandlerState.AuthError);
            return;
        }

        Event respIV = new Event(EventType.LoginValidation, this.token);

        SinkChannel oc = (SinkChannel) this.getFirstOutputChannel(this.channelType);
        NBChannelController.write(oc, respIV);

        this.nextState(ServerComHandlerState.FinishRequest);
    }

    private void handleIVProcessing()
        throws IOException, ChannelNotFoundException
    {
        Argument id = (Argument) this.stateData;

        boolean result = Interpreter.HasItemWithId(id);
        logger.debug("Id validation result: {}", result);

        Event respIV = new Event(EventType.IdValidation, result);

        SinkChannel oc = (SinkChannel) this.getFirstOutputChannel(this.channelType);
        NBChannelController.write(oc, respIV);

        this.nextState(ServerComHandlerState.FinishRequest);
    }

    private void handleNCProcessing()
        throws IOException, ChannelNotFoundException
    {
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

            SinkChannel oc = (SinkChannel) this.getFirstOutputChannel(this.channelType);
            NBChannelController.write(oc, respNC);

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

        SinkChannel oc = (SinkChannel) this.getFirstOutputChannel(this.channelType);
        NBChannelController.write(oc, respNC);
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

    private void handleAuthError()
        throws IOException, ChannelNotFoundException
    {
        SinkChannel oc = (SinkChannel) this.getFirstOutputChannel(this.channelType);

        Event respAE = new Event(EventType.AuthError, null);
        NBChannelController.write(oc, respAE);

        this.nextState(ServerComHandlerState.FinishRequest);
    }

    private void handleClose() {
        this.setNotRunning();

        this.nextState(ServerComHandlerState.Waiting);
    }

}
