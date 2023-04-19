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
import core.command.arguments.Argument;
import core.handler.ChannelType;
import core.handler.ComHandler;
import core.utils.NBChannelController;

enum ClientComHandlerState {
    Waiting(true),
    ShellRequest(false),
    NewCommandsReqStart(false),
    NewCommandsReqProcessing(false),
    NewCommandsReqFinish(false),
    IdValidationReqStart(false),
    IdValidationReqFinish(false),
    SocketResponse(false),
    NewCommandsResp(false),
    ScriptReq(false),
    SocketReq(false),
    SocketRespWaiting(true),
    SocketResp(false)
    ;

    private boolean isWaiting = false;

    ClientComHandlerState(boolean isWaiting) {
        this.isWaiting = isWaiting;
    }

    boolean isWaiting() {
        return this.isWaiting;
    }
}

/**
 * Class for handling client side communication between client and server.
 *
 * @author ivatolm
 */
public class ClientComHandler extends ComHandler<ClientComHandlerState> {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(ClientComHandler.class);

    // Content Manager
    private ContentManager contentManager;

    // Currently processed event
    private ClientEvent event;

    // Currently processed commands
    private LinkedList<Command> commands;

    // State management for subroutines
    private ClientComHandlerState fromState;
    private ClientComHandlerState toState;


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
    public void process(ChannelType channel) {
        logger.trace("New event from " + channel);

        switch (channel) {
            case Shell:
            case Socket:
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
        logger.trace("State " + this.getState());
        do {
            ClientComHandlerState stState = this.getState();

            switch (this.getState()) {
                case Waiting:
                    this.handleWaitingState();
                    break;
                case ShellRequest:
                    this.handleShellRequest();
                    break;
                case NewCommandsReqStart:
                    this.handleNewCommandsReqStart();
                    break;
                case NewCommandsReqProcessing:
                    this.handleNewCommandsReqProcessing();
                    break;
                case NewCommandsReqFinish:
                    this.handleNewCommandsReqFinish();
                    break;
                case IdValidationReqStart:
                    this.handleIdValidationReqStart();
                    break;
                case IdValidationReqFinish:
                    this.handleIdValidationReqFinish();
                    break;
                case SocketResponse:
                    this.handleSocketResponse();
                    break;
                case NewCommandsResp:
                    this.handleNewCommandsResp();
                    break;
                case ScriptReq:
                    this.handleScriptReq();
                    break;
                case SocketReq:
                    this.handleSocketReq();
                    break;
                case SocketRespWaiting:
                    this.handleSocketRespWaiting();
                    break;
                case SocketResp:
                    this.handleSocketResp();
                    break;
            }

            logger.trace("State: " + stState + " -> " + this.getState());
        } while (!this.getState().isWaiting());
    }

    private void handleWaitingState() {
        if (this.readyChannels.isEmpty()) {
            return;
        }

        if (this.readyChannels.contains(ChannelType.Shell)) {
            this.nextState(ClientComHandlerState.ShellRequest);
        }
    }

    private void handleShellRequest() {
        SourceChannel shellChannel = (SourceChannel) this.inputChannels.get(ChannelType.Shell);

        try {
            this.event = (ClientEvent) NBChannelController.read(shellChannel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ClientComHandlerState.Waiting);
            return;
        }

        switch (this.event.getType()) {
            case NewCommandsReq:
                this.nextState(ClientComHandlerState.NewCommandsReqStart);
                return;
            case IdValidationReq:
                this.nextState(ClientComHandlerState.IdValidationReqStart);
                return;
            default:
                break;
        }

        this.nextState(ClientComHandlerState.Waiting);
    }

    private void handleNewCommandsReqStart() {
        @SuppressWarnings("unchecked")
        LinkedList<Command> commands = (LinkedList<Command>) this.event.getData();

        this.commands = commands;
        this.fromState = this.getState();
        this.nextState(ClientComHandlerState.NewCommandsReqProcessing);
    }

    private void handleNewCommandsReqProcessing() {
        if (this.fromState == this.toState) {
            this.fromState = this.getState();
            this.toState = this.getState();
            this.nextState(ClientComHandlerState.SocketResponse);
            return;
        }

        if (this.commands.isEmpty()) {
            this.fromState = null;
            this.toState = null;
            this.nextState(ClientComHandlerState.NewCommandsReqFinish);
            return;
        }

        Command command = this.commands.pop();
        this.event = new ClientEvent(ClientEventType.SendDataReq, command);

        this.fromState = this.getState();
        this.toState = ClientComHandlerState.NewCommandsReqProcessing;
        this.nextState(ClientComHandlerState.SocketReq);
    }

    private void handleNewCommandsReqFinish() {
        this.nextState(ClientComHandlerState.Waiting);
    }

    private void handleIdValidationReqStart() {
        Argument argument = (Argument) this.event.getData();

        this.event = new ClientEvent(ClientEventType.SendDataReq, argument);

        this.fromState = this.getState();
        this.toState = ClientComHandlerState.IdValidationReqFinish;
        this.nextState(ClientComHandlerState.SocketReq);
    }

    private void handleIdValidationReqFinish() {
        SinkChannel shellChannel = (SinkChannel) this.outputChannels.get(ChannelType.Shell);

        boolean result = (boolean) this.event.getData();
        this.event = new ClientEvent(ClientEventType.IdValidationResp, result);

        try {
            NBChannelController.write(shellChannel, this.event);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ClientComHandlerState.Waiting);
            return;
        }

        this.nextState(ClientComHandlerState.Waiting);
    }

    private void handleSocketResponse() {
        this.fromState = this.getState();

        switch (this.event.getType()) {
            case NewCommandsResp:
                this.nextState(ClientComHandlerState.NewCommandsResp);
                break;
            case ScritpReq:
                this.nextState(ClientComHandlerState.ScriptReq);
                break;
            default:
                this.nextState(this.toState);
                break;
        }
    }

    private void handleNewCommandsResp() {
        SinkChannel shellChannel = (SinkChannel) this.outputChannels.get(ChannelType.Shell);

        @SuppressWarnings("unchecked")
        LinkedList<String> output = (LinkedList<String>) this.event.getData();
        this.event = new ClientEvent(ClientEventType.NewCommandsResp, output);

        try {
            NBChannelController.write(shellChannel, this.event);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ClientComHandlerState.Waiting);
            return;
        }

        this.nextState(this.toState);
    }

    private void handleScriptReq() {
        String filename = (String) this.event.getData();

        LinkedList<Command> commands = this.contentManager.get(filename);

        this.event = new ClientEvent(ClientEventType.SendDataReq, commands);
        this.nextState(ClientComHandlerState.SocketReq);
    }

    private void handleSocketReq() {
        SinkChannel socketChannel = (SinkChannel) this.outputChannels.get(ChannelType.Socket);

        try {
            NBChannelController.write(socketChannel, this.event);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ClientComHandlerState.Waiting);
            return;
        }

        this.nextState(ClientComHandlerState.SocketRespWaiting);
    }

    private void handleSocketRespWaiting() {
        if (this.readyChannels.isEmpty()) {
            return;
        }

        if (this.readyChannels.contains(ChannelType.Socket)) {
            this.nextState(ClientComHandlerState.SocketResp);
        }
    }

    private void handleSocketResp() {
        SourceChannel socketChannel = (SourceChannel) this.inputChannels.get(ChannelType.Socket);

        try {
            this.event = (ClientEvent) NBChannelController.read(socketChannel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ClientComHandlerState.Waiting);
            return;
        }

        this.nextState(this.toState);
    }

}
