package client.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import client.event.ClientEvent;
import client.event.ClientEventType;
import core.command.Command;
import core.command.arguments.Argument;
import core.handler.ChannelType;
import core.handler.ShellHandler;
import core.utils.NBChannelController;

enum ClientShellHandlerState {
    Waiting,
    InputParsingStart,
    InputParsingProcessing,
    InputParsingFinish,
    ComIdValidationStart,
    ComIdValidationWaiting,
    ComIdValidationFinish,
    ComReceiveOutput
}

/**
 * Class providing user interactive shell.
 *
 * @author ivatolm
 */
public class ClientShellHandler extends ShellHandler<ClientShellHandlerState> {

    // Input from the System.in
    private String input;

    // Id for validation
    private Argument idArgForValidation;

    /**
     * Constructs new {@code ClientShellHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     */
    public ClientShellHandler(HashMap<ChannelType, SelectableChannel> inputChannels,
                              HashMap<ChannelType, SelectableChannel> outputChannels) {
        super(inputChannels, outputChannels, ClientShellHandlerState.Waiting);

        this.input = null;
        this.idArgForValidation = null;
    }

    @Override
    public void process(ChannelType channel) {
        switch (channel) {
            case Input:
                // receive input from the channel
                // parse input for commands
                // if parsing requires id validation,
                //   then send command handler a request
                // else
                //   send commands to com handler
            case Com:
                // receive input from the channel
                // if received output
                //   print output
                // else
                //   continue parsing with received response
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
    }

    private void handleWaitingState() {
        if (this.readyChannels.isEmpty()) {
            return;
        }

        if (!this.readyChannels.contains(ChannelType.Input)) {
            this.nextState(ClientShellHandlerState.InputParsingStart);
        }
    }

    private void handleInputParsingStart() {
        SourceChannel inputChannel = (SourceChannel) this.inputChannels.get(ChannelType.Input);
        try {
            this.input = (String) NBChannelController.read(inputChannel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ClientShellHandlerState.Waiting);
            return;
        }

        this.nextState(ClientShellHandlerState.InputParsingProcessing);
    }

    private void handleInputParsingProcessing() {
        this.parseCommands(new LinkedList<>(Arrays.asList(input)));

        if (this.hasArgForIdValidation()) {
            Argument arg = this.getArgForIdValidation();
            this.idArgForValidation = arg;
            this.nextState(ClientShellHandlerState.ComIdValidationStart);
        } else {
            this.nextState(ClientShellHandlerState.InputParsingFinish);
        }
    }

    private void handleInputParsingFinish() {
        if (this.hasParsingResult()) {
            SinkChannel comChannel = (SinkChannel) this.outputChannels.get(ChannelType.Com);
            LinkedList<Command> commands = this.getParsingResult();
            ClientEvent event = new ClientEvent(ClientEventType.NewCommandsReq, commands);

            try {
                NBChannelController.write(comChannel, event);
            } catch (IOException e) {
                System.err.println("Cannot write to the channel.");
                this.nextState(ClientShellHandlerState.Waiting);
                return;
            }

            this.nextState(ClientShellHandlerState.ComReceiveOutput);
        } else {
            this.nextState(ClientShellHandlerState.Waiting);
        }
    }

    private void handleComIdValidationStart() {
        if (this.idArgForValidation != null) {
            SinkChannel comChannel = (SinkChannel) this.outputChannels.get(ChannelType.Com);
            ClientEvent event = new ClientEvent(ClientEventType.IdValidationReq, this.idArgForValidation);

            try {
                NBChannelController.write(comChannel, event);
            } catch (IOException e) {
                System.err.println("Cannot write to the channel.");
                this.nextState(ClientShellHandlerState.Waiting);
                return;
            }

            this.nextState(ClientShellHandlerState.ComIdValidationWaiting);
        } else {
            this.nextState(ClientShellHandlerState.InputParsingProcessing);
        }
    }

    private void handleComIdValidationWaiting() {
        if (this.readyChannels.isEmpty()) {
            return;
        }

        if (!this.readyChannels.contains(ChannelType.Com)) {
            this.nextState(ClientShellHandlerState.ComIdValidationFinish);
        }
    }

    private void handleComIdValidationFinish() {
        SourceChannel comChannel = (SourceChannel) this.inputChannels.get(ChannelType.Com);
        ClientEvent event;
        try {
            event = (ClientEvent) NBChannelController.read(comChannel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ClientShellHandlerState.Waiting);
            return;
        }

        if (event.getType() == ClientEventType.IdValidationResp) {
            boolean result = (boolean) event.getData();
            this.setArgIdValidationResult(result);
        } else {
            this.nextState(ClientShellHandlerState.Waiting);
        }

        this.nextState(ClientShellHandlerState.InputParsingProcessing);
    }

    private void handleComReceiveOutput() {
        SourceChannel comChannel = (SourceChannel) this.inputChannels.get(ChannelType.Com);
        ClientEvent event;
        try {
            event = (ClientEvent) NBChannelController.read(comChannel);
        } catch (IOException e) {
            // System.err.println("Cannot read from the channel.");
            this.nextState(ClientShellHandlerState.Waiting);
            return;
        }

        if (event.getType() == ClientEventType.NewCommandsResp) {
            @SuppressWarnings("unchecked")
            LinkedList<String> result = (LinkedList<String>) event.getData();

            for (String line : result) {
                System.out.println(line);
            }

            this.nextState(ClientShellHandlerState.ComReceiveOutput);
        } else {
            this.nextState(ClientShellHandlerState.Waiting);
        }
    }

}
