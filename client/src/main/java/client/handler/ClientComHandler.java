package client.handler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.util.LinkedList;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.shell.ContentManager;
import core.command.Command;
import core.event.Event;
import core.event.EventType;
import core.handler.ChannelType;
import core.handler.ComHandler;
import core.utils.NBChannelController;

enum ClientComHandlerState {
    Waiting,
    NewEvent,
    NewRequest,
    IVStart,
    LVStart,
    NCStart,
    ExistingRequest,
    IVProcessing,
    LVProcessing,
    NCProcessingSR,
    NCProcessingOR,
    FinishRequest,
    AuthError,
    Error
}

/**
 * Class for handling client side communication between client and server.
 *
 * @author ivatolm
 */
public class ClientComHandler extends ComHandler<ClientComHandlerState> {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger("ComHandler");

    // Content Manager
    private ContentManager contentManager;

    // State communication data
    private Object stateData;

    // Currently processed event
    private Event event;

    // Session token
    private String token;

    /**
     * Constructs new {@code ClientComHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param contentManager content manager
     */
    public ClientComHandler(LinkedList<Pair<ChannelType, SelectableChannel>> inputChannels,
                            LinkedList<Pair<ChannelType, SelectableChannel>> outputChannels,
                            ContentManager contentManager) {
        super(inputChannels, outputChannels, ClientComHandlerState.Waiting);

        this.contentManager = contentManager;
    }

    @Override
    public void process(ChannelType type, SelectableChannel channel) {
        logger.trace("New event from " + type);

        switch (type) {
            case Shell:
            case Network:
                this.readyChannels = new LinkedList<ChannelType>() {{ add(type); }};
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
            ClientComHandlerState stState = this.getState();

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
                case LVStart:
                    this.handleLVStart();
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
                case LVProcessing:
                    this.handleLVProcessing();
                    break;
                case NCProcessingSR:
                    this.handleNCProcessingSR();
                    break;
                case NCProcessingOR:
                    this.handleNCProcessingOR();
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
            }

            logger.trace("State: " + stState + " -> " + this.getState());
        } while (this.getState() != ClientComHandlerState.Waiting);
    }

    private void handleWaitingState() {
        if (this.readyChannels.isEmpty()) {
            return;
        }

        if (this.readyChannels.contains(ChannelType.Shell)) {
            this.nextState(ClientComHandlerState.NewEvent);
        } else if (this.readyChannels.contains(ChannelType.Network)) {
            this.nextState(ClientComHandlerState.NewEvent);
        }
    }

    private void handleNewEvent() {
        if (this.event == null) {
            try {
                this.filterSubscriptions(ChannelType.Network);
            } catch (IOException e) {
                this.nextState(ClientComHandlerState.Error);
                return;
            }

            this.nextState(ClientComHandlerState.NewRequest);
        } else {
            this.nextState(ClientComHandlerState.ExistingRequest);
        }
    }

    private void handleNewRequest() {
        ChannelType type = ChannelType.Shell;
        Optional<SelectableChannel> ic = this.getFirstInputChannel(type);
        if (!ic.isPresent()) {
            logger.warn("Input channel " + type + " was not found.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        SourceChannel channel = (SourceChannel) ic.get();
        try {
            this.event = (Event) NBChannelController.read(channel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        switch (this.event.getType()) {
            case LoginValidation:
                this.nextState(ClientComHandlerState.LVStart);
                break;
            case IdValidation:
                this.nextState(ClientComHandlerState.IVStart);
                break;
            case NewCommands:
                this.nextState(ClientComHandlerState.NCStart);
                break;
            case AuthError:
                this.nextState(ClientComHandlerState.AuthError);
                break;
            default:
                this.nextState(ClientComHandlerState.Error);
                break;
        }
    }

    private void handleIVStart() {
        Event reqIV = new Event(EventType.IdValidation, this.event.getData(), this.token);

        ChannelType type = ChannelType.Network;
        Optional<SelectableChannel> oc = this.getFirstOutputChannel(type);
        if (!oc.isPresent()) {
            logger.warn("Output channel " + type + " was not found.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        SinkChannel channel = (SinkChannel) oc.get();
        try {
            NBChannelController.write(channel, reqIV);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        this.nextState(ClientComHandlerState.Waiting);
    }

    private void handleLVStart() {
        Event reqLV = new Event(EventType.LoginValidation, this.event.getData(), this.token);

        ChannelType type = ChannelType.Network;
        Optional<SelectableChannel> oc = this.getFirstOutputChannel(type);
        if (!oc.isPresent()) {
            logger.warn("Output channel " + type + " was not found.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        SinkChannel channel = (SinkChannel) oc.get();
        try {
            NBChannelController.write(channel, reqLV);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        this.nextState(ClientComHandlerState.Waiting);
    }

    private void handleNCStart() {
        Event reqNC = new Event(EventType.NewCommands, this.event.getData(), this.token);

        ChannelType type = ChannelType.Network;
        Optional<SelectableChannel> oc = this.getFirstOutputChannel(type);
        if (!oc.isPresent()) {
            logger.warn("Output channel " + type + " was not found.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        SinkChannel channel = (SinkChannel) oc.get();
        try {
            NBChannelController.write(channel, reqNC);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        this.nextState(ClientComHandlerState.Waiting);
    }

    private void handleExistingRequest() {
        ChannelType type = ChannelType.Network;
        Optional<SelectableChannel> ic = this.getFirstInputChannel(type);
        if (!ic.isPresent()) {
            logger.warn("Input channel " + type + " was not found.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        SourceChannel channel = (SourceChannel) ic.get();
        Event response = null;
        try {
            response = (Event) NBChannelController.read(channel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        this.stateData = response;

        switch (response.getType()) {
            case LoginValidation:
                this.nextState(ClientComHandlerState.LVProcessing);
                break;
            case IdValidation:
                this.nextState(ClientComHandlerState.IVProcessing);
                break;
            case ScriptRequest:
                this.nextState(ClientComHandlerState.NCProcessingSR);
                break;
            case OutputResponse:
                this.nextState(ClientComHandlerState.NCProcessingOR);
                break;
            case AuthError:
                this.nextState(ClientComHandlerState.AuthError);
                break;
            default:
                this.nextState(ClientComHandlerState.Error);
                break;
        }
    }

    private void handleIVProcessing() {
        Event data = (Event) this.stateData;
        boolean result = (boolean) data.getData();

        Event respIV = new Event(EventType.IdValidation, result);

        ChannelType type = ChannelType.Shell;
        Optional<SelectableChannel> oc = this.getFirstOutputChannel(type);
        if (!oc.isPresent()) {
            logger.warn("Output channel " + type + " was not found.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        SinkChannel channel = (SinkChannel) oc.get();
        try {
            NBChannelController.write(channel, respIV);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        this.nextState(ClientComHandlerState.FinishRequest);
    }

    private void handleLVProcessing() {
        Event data = (Event) this.stateData;
        String response = (String) data.getData();

        boolean result = response != null;
        if (result) {
            this.token = response;
            logger.debug("Logged in. Token: {}", this.token);
        }

        Event respLV = new Event(this.event.getType(), result);

        ChannelType type = ChannelType.Shell;
        Optional<SelectableChannel> oc = this.getFirstOutputChannel(type);
        if (!oc.isPresent()) {
            logger.warn("Output channel " + type + " was not found.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        SinkChannel channel = (SinkChannel) oc.get();
        try {
            NBChannelController.write(channel, respLV);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        this.nextState(ClientComHandlerState.FinishRequest);
    }

    private void handleNCProcessingSR() {
        Event data = (Event) this.stateData;
        @SuppressWarnings("unchecked")
        LinkedList<String> filename = (LinkedList<String>) data.getData();

        LinkedList<Command> commands = null;
        try {
            commands = this.contentManager.get(filename.getFirst());
        } catch (FileNotFoundException e) {
            logger.info("Resource was not found: " + filename.getFirst());
        }

        Event respNCSR = new Event(EventType.ScriptRequest, commands);

        ChannelType type = ChannelType.Network;
        Optional<SelectableChannel> oc = this.getFirstOutputChannel(type);
        if (!oc.isPresent()) {
            logger.warn("Output channel " + type + " was not found.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        SinkChannel channel = (SinkChannel) oc.get();
        try {
            NBChannelController.write(channel, respNCSR);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        this.nextState(ClientComHandlerState.Waiting);
    }

    private void handleNCProcessingOR() {
        Event data = (Event) this.stateData;
        @SuppressWarnings("unchecked")
        LinkedList<String> output = (LinkedList<String>) data.getData();

        Event respIV = new Event(EventType.OutputResponse, output);

        ChannelType type = ChannelType.Shell;
        Optional<SelectableChannel> oc = this.getFirstOutputChannel(type);
        if (!oc.isPresent()) {
            logger.warn("Output channel " + type + " was not found.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        SinkChannel channel = (SinkChannel) oc.get();
        try {
            NBChannelController.write(channel, respIV);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        this.nextState(ClientComHandlerState.FinishRequest);
    }

    private void handleFinishRequest() {
        this.event = null;
        this.stateData = null;

        this.filterSubscriptions();

        this.nextState(ClientComHandlerState.Waiting);
    }

    private void handleAuthError() {
        logger.debug("Authentication error.");

        Event respLV = new Event(EventType.AuthError, null);

        ChannelType type = ChannelType.Shell;
        Optional<SelectableChannel> oc = this.getFirstOutputChannel(type);
        if (!oc.isPresent()) {
            logger.warn("Output channel " + type + " was not found.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        SinkChannel channel = (SinkChannel) oc.get();
        try {
            NBChannelController.write(channel, respLV);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        this.nextState(ClientComHandlerState.FinishRequest);
    }

    private void handleError() {
        logger.warn("Error occured while processing the last state. Resetting...");

        this.nextState(ClientComHandlerState.FinishRequest);
    }
}
