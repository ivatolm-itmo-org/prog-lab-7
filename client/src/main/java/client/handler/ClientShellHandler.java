package client.handler;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.util.Arrays;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.command.Command;
import core.command.CommandType;
import core.command.arguments.Argument;
import core.event.Event;
import core.event.EventType;
import core.handler.ChannelType;
import core.handler.HandlerChannels;
import core.handler.ShellHandler;
import core.utils.ChannelNotFoundException;
import core.utils.NBChannelController;

enum ClientShellHandlerState {
    Waiting(true),
    InputParsingStart(false),
    InputParsingProcessing(false),
    InputParsingFinish(false),
    ComIdValidationStart(false),
    ComIdValidationWaiting(true),
    ComIdValidationFinish(false),
    ComReceiveOutput(false),
    ComLgValidationStart(false),
    ComLgValidationWaiting(true),
    ComLgValidationFinish(false),
    AuthError(false),
    Close(true)
    ;

    private boolean isWaiting = false;

    ClientShellHandlerState(boolean isWaiting) {
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
public class ClientShellHandler extends ShellHandler<ClientShellHandlerState> {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger("ShellHandler");

    // Input from the System.in
    private String input;

    // Id for validation
    private Argument idArgForValidation;

    // Should close flag
    private boolean shouldClose;

    // State data
    private Object stateData;

    /**
     * Constructs new {@code ClientShellHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     */
    public ClientShellHandler(HandlerChannels inputChannels,
                              HandlerChannels outputChannels) {
        super(inputChannels, outputChannels, ClientShellHandlerState.Waiting);

        this.input = null;
        this.idArgForValidation = null;
        this.shouldClose = false;

        this.showInputGreeting();
    }

    @Override
    public void process(ChannelType type, SelectableChannel channel) {
        logger.trace("New event from {}", type);

        switch (type) {
            case Input:
            case Com:
                this.readyChannels = new LinkedList<ChannelType>() {{ add(type); }};
                break;
            default:
                logger.warn("Unexpected channel.");
                break;
        }

        this.handleEvents();
    }

    @Override
    protected void handleEvents() {
        logger.trace("State: {}", this.getState());
        do {
            ClientShellHandlerState stState = this.getState();

            try {
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
                    case ComLgValidationStart:
                        this.handleComLgValidationStart();
                        break;
                    case ComLgValidationWaiting:
                        this.handleComLgValidationWaiting();
                        break;
                    case ComLgValidationFinish:
                        this.handleComLgValidationFinish();
                        break;
                    case ComReceiveOutput:
                        this.handleComReceiveOutput();
                        break;
                    case AuthError:
                        this.handleAuthError();
                        break;
                    case Close:
                        this.handleClose();
                        break;
                }
            } catch(IOException | ChannelNotFoundException e) {
                logger.warn(e.getMessage());
                this.nextState(ClientShellHandlerState.Close);
            }

            logger.trace("State: {} -> {}", stState, this.getState());
        } while (!this.getState().isWaiting());
    }

    private void handleWaitingState() {
        logger.debug("Ready channels count: {}", this.readyChannels.size());
        if (this.readyChannels.isEmpty()) {
            return;
        }

        logger.debug("Ready channels: {}", this.readyChannels);
        if (this.readyChannels.contains(ChannelType.Input)) {
            this.nextState(ClientShellHandlerState.InputParsingStart);
        } else if (this.readyChannels.contains(ChannelType.Com)) {
            this.nextState(ClientShellHandlerState.ComReceiveOutput);
        }
    }

    private void handleInputParsingStart()
        throws IOException, ChannelNotFoundException
    {
        ChannelType type = ChannelType.Input;
        SourceChannel ic = (SourceChannel) this.getFirstInputChannel(type);

        this.input = (String) NBChannelController.read(ic);

        this.nextState(ClientShellHandlerState.InputParsingProcessing);
    }

    private void handleInputParsingProcessing() {
        logger.debug("Parsing command...");
        this.parseCommands(new LinkedList<>(Arrays.asList(input)));
        this.input = null;
        logger.debug("Parsing completed");

        logger.debug("Has argument for id validation: {}", this.hasArgForIdValidation());
        if (this.hasArgForIdValidation()) {
            Argument arg = this.getArgForIdValidation();
            this.idArgForValidation = arg;
            this.nextState(ClientShellHandlerState.ComIdValidationStart);
        } else {
            this.nextState(ClientShellHandlerState.InputParsingFinish);
        }
    }

    private void handleInputParsingFinish()
        throws IOException, ChannelNotFoundException
    {
        if (this.hasParsingResult()) {
            ChannelType type = ChannelType.Com;
            SinkChannel oc = (SinkChannel) this.getFirstOutputChannel(type);

            LinkedList<Command> commands = this.getParsingResult();
            for (Command cmd : commands) {
                if (cmd.getType() == CommandType.LOGIN) {
                    this.stateData = cmd.getArgsValues();
                    this.nextState(ClientShellHandlerState.ComLgValidationStart);
                    return;
                }

                if (cmd.getType() == CommandType.EXIT) {
                    this.nextState(ClientShellHandlerState.Close);
                    return;
                }
            }

            Event event = new Event(EventType.NewCommands, commands);
            NBChannelController.write(oc, event);
        }

        this.nextState(ClientShellHandlerState.Waiting);
    }

    private void handleComIdValidationStart()
        throws IOException, ChannelNotFoundException
    {
        if (this.idArgForValidation != null) {
            ChannelType type = ChannelType.Com;
            SinkChannel oc = (SinkChannel) this.getFirstOutputChannel(type);

            Event event = new Event(EventType.IdValidation, this.idArgForValidation);
            NBChannelController.write(oc, event);

            this.filterSubscriptions(ChannelType.Com);

            this.nextState(ClientShellHandlerState.ComIdValidationWaiting);
        } else {
            this.nextState(ClientShellHandlerState.InputParsingProcessing);
        }
    }

    private void handleComIdValidationWaiting() {
        if (this.readyChannels.isEmpty()) {
            return;
        }

        if (this.readyChannels.contains(ChannelType.Com)) {
            this.nextState(ClientShellHandlerState.ComIdValidationFinish);
        }
    }

    private void handleComIdValidationFinish()
        throws IOException, ChannelNotFoundException
    {
        ChannelType type = ChannelType.Com;
        SourceChannel ic = (SourceChannel) this.getFirstInputChannel(type);

        Event event = (Event) NBChannelController.read(ic);

        if (event.getType() == EventType.IdValidation) {
            boolean result = (boolean) event.getData();
            this.setArgIdValidationResult(result);

            this.filterSubscriptions();
            this.nextState(ClientShellHandlerState.InputParsingProcessing);
        } else if (event.getType() == EventType.AuthError) {
            this.nextState(ClientShellHandlerState.AuthError);
        }
    }

    private void handleComLgValidationStart()
        throws IOException, ChannelNotFoundException
    {
        @SuppressWarnings("unchecked")
        LinkedList<Argument> arguments = (LinkedList<Argument>) this.stateData;
        Pair<String, String> credentials = new ImmutablePair<>(
            (String) arguments.get(0).getValue(),
            (String) arguments.get(1).getValue()
        );

        ChannelType type = ChannelType.Com;
        SinkChannel oc = (SinkChannel) this.getFirstOutputChannel(type);

        Event event = new Event(EventType.LoginValidation, credentials);
        NBChannelController.write(oc, event);

        this.filterSubscriptions(ChannelType.Com);

        this.nextState(ClientShellHandlerState.ComLgValidationWaiting);
    }

    private void handleComLgValidationWaiting() {
        if (this.readyChannels.isEmpty()) {
            return;
        }

        if (this.readyChannels.contains(ChannelType.Com)) {
            this.nextState(ClientShellHandlerState.ComLgValidationFinish);
        }
    }

    private void handleComLgValidationFinish()
        throws IOException, ChannelNotFoundException
    {
        ChannelType type = ChannelType.Com;
        SourceChannel ic = (SourceChannel) this.getFirstInputChannel(type);

        Event event = (Event) NBChannelController.read(ic);

        if (event.getType() == EventType.LoginValidation) {
            boolean result = (boolean) event.getData();
            if (result) {
                System.out.println("Logged in.");
            } else {
                System.out.println("Invalid credentials.");
            }

            this.showInputGreeting();

            this.filterSubscriptions();
        } else if (event.getType() == EventType.AuthError) {
            this.nextState(ClientShellHandlerState.AuthError);
            return;
        }

        this.stateData = null;

        this.nextState(ClientShellHandlerState.Waiting);
    }

    private void handleComReceiveOutput()
        throws IOException, ChannelNotFoundException
    {
        ChannelType type = ChannelType.Com;
        SourceChannel ic = (SourceChannel) this.getFirstInputChannel(type);

        Event event = (Event) NBChannelController.read(ic);

        if (event.getType() == EventType.OutputResponse) {
            @SuppressWarnings("unchecked")
            LinkedList<String> result = (LinkedList<String>) event.getData();

            for (String line : result) {
                System.out.println(line);
            }

            this.showInputGreeting();

            this.nextState(ClientShellHandlerState.Waiting);
        } else if (event.getType() == EventType.AuthError) {
            this.nextState(ClientShellHandlerState.AuthError);
        } else {
            this.nextState(ClientShellHandlerState.Waiting);
        }
    }

    private void handleAuthError() {
        System.out.println("Invalid credentials.");
        this.showInputGreeting();

        this.nextState(ClientShellHandlerState.Waiting);
    }

    private void handleClose() {
        this.shouldClose = true;
    }

    public boolean shouldClose() {
        return this.shouldClose;
    }

}
