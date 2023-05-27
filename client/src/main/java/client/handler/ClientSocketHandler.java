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
import core.handler.HandlerChannel;
import core.handler.HandlerChannels;
import core.handler.SocketHandler;
import core.net.Com;
import core.net.packet.Packet;
import core.utils.ChannelNotFoundException;
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
    public ClientSocketHandler(HandlerChannels inputChannels,
                               HandlerChannels outputChannels,
                               Com networkCom) throws IOException {
        super(inputChannels, outputChannels, ClientSocketHandlerState.Waiting, networkCom);

        this.disconnectionPipe = Pipe.open();

        this.timer = new Timer();
        this.timerTask = new PingTask(this.disconnectionPipe.sink());

        this.timer.scheduleAtFixedRate(timerTask, 0, PING_DELAY);

        this.inputChannels.add(new HandlerChannel(ChannelType.Internal, disconnectionPipe.source()));
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

            try {
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
            } catch(IOException | ChannelNotFoundException e) {
                logger.warn(e.getMessage());
                this.nextState(ClientSocketHandlerState.Error);
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

    private void handlePingTimeout()
        throws IOException, ChannelNotFoundException
    {
        ChannelType type = ChannelType.Internal;
        SourceChannel ic = (SourceChannel) this.getFirstInputChannel(type);

        @SuppressWarnings("unused") // dummy event
        Event event = (Event) NBChannelController.read(ic);

        Event reqPG = new Event(EventType.Ping, null);

        Packet packet = new Packet(EventType.Ping, reqPG);
        this.networkCom.send(packet, null);

        this.nextState(ClientSocketHandlerState.Waiting);
    }

    private void handleNewComEvent()
        throws IOException, ChannelNotFoundException
    {
        ChannelType type = ChannelType.Com;
        SourceChannel ic = (SourceChannel) this.getFirstInputChannel(type);

        Event req = (Event) NBChannelController.read(ic);

        // Packet type of NewCommands doesn't matter
        // TODO: maybe remove?
        System.out.println("Event: " + req);
        Packet packet = new Packet(EventType.NewCommands, req);
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

    private void handleCompletedMessage()
        throws IOException, ChannelNotFoundException
    {
        Event event = (Event) this.message.getValue().getLast().getData();
        this.message = null;

        ChannelType type = ChannelType.Com;
        SinkChannel oc = (SinkChannel) this.getFirstOutputChannel(type);

        NBChannelController.write(oc, event);

        this.nextState(ClientSocketHandlerState.Waiting);
    }

    private void handleError() {
        // TODO: close connection

        logger.warn("Error occured while processing the last state. Resetting...");
    }

}
