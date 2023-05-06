package client.handler;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import client.net.PingTask;
import core.event.Event;
import core.event.EventType;
import core.handler.ChannelType;
import core.handler.SocketHandler;
import core.net.Com;
import core.net.packet.Packet;
import core.utils.NBChannelController;

enum ClientSocketHandlerState {
    Waiting,
    PingTimeout,
    NewComEvent,
    NewNetworkEvent,
    CompletedMessage,
    Error
}

public class ClientSocketHandler extends SocketHandler<DatagramChannel, ClientSocketHandlerState> {

    private static final int PING_DELAY = 100;

    // Logger
    private static final Logger logger = LoggerFactory.getLogger("SocketHandler");

    // Message
    private Pair<Integer, LinkedList<Packet>> message;

    // Disconnection task
    private Timer timer;
    private TimerTask timerTask;
    private Pipe disconnectionPipe;

    /**
     * Constructs new {@code ClientSocketHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param networkCom network communicator
     * @throws IOException if cannot open a pipe
     */
    public ClientSocketHandler(LinkedList<Pair<ChannelType, SelectableChannel>> inputChannels,
                               LinkedList<Pair<ChannelType, SelectableChannel>> outputChannels,
                               Com networkCom) throws IOException {
        super(inputChannels, outputChannels, ClientSocketHandlerState.Waiting, networkCom);

        this.disconnectionPipe = Pipe.open();

        this.timer = new Timer();
        this.timerTask = new PingTask(this.disconnectionPipe.sink());

        this.timer.scheduleAtFixedRate(timerTask, 0, PING_DELAY);

        this.inputChannels.add(new ImmutablePair<>(ChannelType.Internal, disconnectionPipe.source()));
    }

    @Override
    public void process(ChannelType type, SelectableChannel channel) {
        logger.trace("New event from " + type);

        switch (type) {
            case Internal:
            case Com:
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
            ClientSocketHandlerState stState = this.getState();

            switch (this.getState()) {
                case Waiting:
                    this.handleWaitingState();
                    break;
                case PingTimeout:
                    this.handlePingTimeout();
                    break;
                case NewComEvent:
                    this.handleNewComEvent();
                    break;
                case NewNetworkEvent:
                    this.handleNewNetworkEvent();
                    break;
                case CompletedMessage:
                    this.handleCompletedMessage();
                    break;
                case Error:
                    this.handleError();
                    break;
            }

            logger.trace("State: " + stState + " -> " + this.getState());
        } while (this.getState() != ClientSocketHandlerState.Waiting);
    }

    private void handleWaitingState() {
        if (this.readyChannels.isEmpty()) {
            return;
        }

        if (this.readyChannels.contains(ChannelType.Internal)) {
            this.nextState(ClientSocketHandlerState.PingTimeout);
        } else if (this.readyChannels.contains(ChannelType.Com)) {
            this.nextState(ClientSocketHandlerState.NewComEvent);
        } else if (this.readyChannels.contains(ChannelType.Network)) {
            this.nextState(ClientSocketHandlerState.NewNetworkEvent);
        }
    }

    private void handlePingTimeout() {
        SourceChannel inputChannel = (SourceChannel) this.getFirstInputChannel(ChannelType.Internal);

        @SuppressWarnings("unused")
        Event event;
        try {
            event = (Event) NBChannelController.read(inputChannel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ClientSocketHandlerState.Waiting);
            return;
        }

        Event reqPG = new Event(EventType.Ping, null);

        Packet packet = new Packet(EventType.Ping, reqPG);
        this.networkCom.send(packet, null);

        this.nextState(ClientSocketHandlerState.Waiting);
    }

    private void handleNewComEvent() {
        SourceChannel inputChannel = (SourceChannel) this.getFirstInputChannel(ChannelType.Com);

        Event reqNC;
        try {
            reqNC = (Event) NBChannelController.read(inputChannel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ClientSocketHandlerState.Waiting);
            return;
        }

        Packet packet = new Packet(EventType.NewCommands, reqNC);
        this.networkCom.send(packet, null);

        this.nextState(ClientSocketHandlerState.Waiting);
    }

    private void handleNewNetworkEvent() {
        Pair<SocketAddress, Packet> data = this.networkCom.receive();
        Packet packet = data.getValue();

        if (this.message == null) {
            this.message = new ImmutablePair<>(1, new LinkedList<>());
        }

        Integer requiredLength = this.message.getLeft();
        LinkedList<Packet> receivedPackets = this.message.getRight();

        receivedPackets.push(packet);

        if (requiredLength == receivedPackets.size()) {
            this.nextState(ClientSocketHandlerState.CompletedMessage);
        } else {
            this.nextState(ClientSocketHandlerState.Waiting);
        }
    }

    private void handleCompletedMessage() {
        Event event = (Event) this.message.getValue().getLast().getData();
        this.message = null;

        SelectableChannel outputChannel = this.getFirstOutputChannel(ChannelType.Com);
        SinkChannel channel = (SinkChannel) outputChannel;
        try {
            NBChannelController.write(channel, event);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ClientSocketHandlerState.Error);
            return;
        }

        this.nextState(ClientSocketHandlerState.Waiting);
    }

    private void handleError() {
        // TODO: close connection

        logger.warn("Error occured while processing the last state. Resetting...");
    }

}
