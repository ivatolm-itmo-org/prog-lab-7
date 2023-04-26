package client.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.event.ClientEvent;
import client.event.ClientEventType;
import client.shell.ContentManager;
import core.command.Command;
import core.handler.ChannelType;
import core.handler.ComHandler;
import core.utils.NBChannelController;

enum ClientComHandlerState {
    Waiting,
    NewEvent,
    NewRequest,
    IVStart,
    NCStart,
    ExistingRequest,
    IVProcessing,
    NCProcessing,
    NCProcessingSR,
    NCProcessingOR,
    FinishRequest,
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
    private ClientEvent event;

    /**
     * Constructs new {@code ClientComHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param contentManager content manager
     */
    public ClientComHandler(HashMap<ChannelType, SelectableChannel> inputChannels,
                            HashMap<ChannelType, SelectableChannel> outputChannels,
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
                case NCProcessingSR:
                    this.handleNCProcessingSR();
                    break;
                case NCProcessingOR:
                    this.handleNCProcessingOR();
                    break;
                case FinishRequest:
                    this.handleFinishRequest();
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
        SourceChannel channel = (SourceChannel) this.inputChannels.get(ChannelType.Shell);
        try {
            this.event = (ClientEvent) NBChannelController.read(channel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        switch (this.event.getType()) {
            case IdValidation:
                this.nextState(ClientComHandlerState.IVStart);
                break;
            case NewCommands:
                this.nextState(ClientComHandlerState.NCStart);
                break;
            default:
                this.nextState(ClientComHandlerState.Error);
                break;
        }
    }

    private void handleIVStart() {
        ClientEvent reqIV = new ClientEvent(ClientEventType.IdValidation, this.event.getData());

        SinkChannel channel = (SinkChannel) this.outputChannels.get(ChannelType.Network);
        try {
            NBChannelController.write(channel, reqIV);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        this.nextState(ClientComHandlerState.Waiting);
    }

    private void handleNCStart() {
        ClientEvent reqNC = new ClientEvent(ClientEventType.NewCommands, this.event.getData());

        SinkChannel channel = (SinkChannel) this.outputChannels.get(ChannelType.Network);
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
        SourceChannel channel = (SourceChannel) this.inputChannels.get(ChannelType.Network);
        ClientEvent response = null;
        try {
            response = (ClientEvent) NBChannelController.read(channel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        this.stateData = response;

        switch (response.getType()) {
            case IdValidation:
                this.nextState(ClientComHandlerState.IVProcessing);
                break;
            case NewCommands:
                this.nextState(ClientComHandlerState.NCProcessing);
                break;
            default:
                this.nextState(ClientComHandlerState.Error);
                break;
        }
    }

    private void handleIVProcessing() {
        boolean result = (boolean) this.stateData;

        ClientEvent respIV = new ClientEvent(ClientEventType.IdValidation, result);

        SinkChannel channel = (SinkChannel) this.outputChannels.get(ChannelType.Shell);
        try {
            NBChannelController.write(channel, respIV);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ClientComHandlerState.Error);
            return;
        }

        this.nextState(ClientComHandlerState.FinishRequest);
    }

    private void handleNCProcessing() {
        ClientEvent response = (ClientEvent) this.stateData;

        this.stateData = response.getData();

        switch (response.getType()) {
            case ScriptRequest:
                this.nextState(ClientComHandlerState.NCProcessingSR);
                break;
            case OutputResponse:
                this.nextState(ClientComHandlerState.NCProcessingOR);
                break;
            default:
                this.nextState(ClientComHandlerState.Error);
                break;
        }
    }

    private void handleNCProcessingSR() {
        String filename = (String) this.stateData;
        LinkedList<Command> commands = this.contentManager.get(filename);

        ClientEvent respNCSR = new ClientEvent(ClientEventType.ScriptRequest, commands);

        SinkChannel channel = (SinkChannel) this.outputChannels.get(ChannelType.Network);
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
        @SuppressWarnings("unchecked")
        LinkedList<String> output = (LinkedList<String>) this.stateData;

        ClientEvent respIV = new ClientEvent(ClientEventType.NewCommands, output);

        SinkChannel channel = (SinkChannel) this.outputChannels.get(ChannelType.Shell);
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

    private void handleError() {
        logger.warn("Error occured while processing the last state. Resetting...");

        this.nextState(ClientComHandlerState.FinishRequest);
    }

}
