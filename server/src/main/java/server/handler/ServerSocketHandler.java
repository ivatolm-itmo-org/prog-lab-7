package server.handler;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Pipe.SinkChannel;
import java.nio.channels.Pipe.SourceChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.event.Event;
import core.handler.ChannelType;
import core.handler.SocketHandler;
import core.net.Com;
import core.net.packet.Packet;
import core.utils.NBChannelController;

enum ServerSocketHandlerState {
    Waiting,
    NewEvent,
    NewNetworkEvent,
    NewNetworkPacket,
    CompletedMessage,
    NewComEvent,
    Error
}

public class ServerSocketHandler extends SocketHandler<DatagramChannel, ServerSocketHandlerState> {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger("SocketHandler");

    // Currently processed channel
    private ChannelType channelType;
    private SelectableChannel channel;

    // Sessions
    private HashSet<SocketAddress> knownClients = new HashSet<>();
    private HashMap<SocketAddress, SelectableChannel> clientAC = new HashMap<>();
    private HashMap<SelectableChannel, SocketAddress> clientCA = new HashMap<>();
    private HashMap<SocketAddress, Pair<Integer, LinkedList<Packet>>> messages = new HashMap<>();

    // State data
    private Object stateData;

    // Communicaton Handler
    private Pair<Pipe, Pipe> clientPipe;

    /**
     * Constructs new {@code ServerComHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param networkCom network communicator
     */
    public ServerSocketHandler(LinkedList<Pair<ChannelType, SelectableChannel>> inputChannels,
                               LinkedList<Pair<ChannelType, SelectableChannel>> outputChannels,
                               Com networkCom) {
        super(inputChannels, outputChannels, ServerSocketHandlerState.Waiting, networkCom);

        this.stateData = null;
        this.clientPipe = null;
    }

    @Override
    public void process(ChannelType type, SelectableChannel channel) {
        logger.trace("New event from " + type);

        switch (type) {
            case Com:
            case Network:
                this.readyChannels = new LinkedList<ChannelType>() {{ add(type); }};

                // TODO: REMOVE FROM HERE
                this.channel = channel;
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
            ServerSocketHandlerState stState = this.getState();

            switch (this.getState()) {
                case Waiting:
                    this.handleWaitingState();
                    break;
                case NewEvent:
                    this.handleNewEvent();
                    break;
                case NewNetworkEvent:
                    this.handleNewNetworkEvent();
                    break;
                case NewNetworkPacket:
                    this.handleNewNetworkPacket();
                    break;
                case CompletedMessage:
                    this.handleCompletedMessage();
                    break;
                case NewComEvent:
                    this.handleNewComEvent();
                    break;
                case Error:
                    this.handleError();
                    break;
            }

            logger.trace("State: " + stState + " -> " + this.getState());
        } while (this.getState() != ServerSocketHandlerState.Waiting);
    }

    private void handleWaitingState() {
        if (this.readyChannels.isEmpty()) {
            return;
        }

        if (this.readyChannels.contains(ChannelType.Network) ||
            this.readyChannels.contains(ChannelType.Com)) {

            ChannelType[] channels = this.readyChannels.toArray(new ChannelType[0]);
            this.channelType = channels[0];

            this.nextState(ServerSocketHandlerState.NewEvent);
        }
    }

    private void handleNewEvent() {
        switch (this.channelType) {
            case Network:
                this.nextState(ServerSocketHandlerState.NewNetworkEvent);
                break;
            case Com:
                this.nextState(ServerSocketHandlerState.NewComEvent);
                break;
            default:
                this.nextState(ServerSocketHandlerState.Error);
                break;
        }
    }

    private void handleNewNetworkEvent() {
        Pair<SocketAddress, Packet> data = this.networkCom.receive();
        this.stateData = data;

        SocketAddress address = data.getKey();
        if (this.knownClients.contains(address)) {
            this.nextState(ServerSocketHandlerState.NewNetworkPacket);
        } else {
            Pipe client_socket, socket_client;
            try {
                client_socket = Pipe.open();
                socket_client = Pipe.open();
            } catch (IOException e) {
                System.err.println("Cannot write to the channel.");
                this.nextState(ServerSocketHandlerState.Error);
                return;
            }

            this.knownClients.add(address);
            this.clientAC.put(address, socket_client.sink());
            this.clientCA.put(client_socket.source(), address);
            this.messages.put(address, new ImmutablePair<>(1, new LinkedList<>()));

            this.clientPipe = new ImmutablePair<>(socket_client, client_socket);

            this.nextState(ServerSocketHandlerState.NewNetworkPacket);
        }
    }

    private void handleNewNetworkPacket() {
        @SuppressWarnings("unchecked")
        Pair<SocketAddress, Packet> data = (Pair<SocketAddress, Packet>) this.stateData;

        SocketAddress client = data.getKey();
        Packet packet = data.getValue();

        if (this.messages.get(client) == null) {
            this.messages.put(client, new ImmutablePair<>(1, new LinkedList<>()));
        }

        Pair<Integer, LinkedList<Packet>> message = this.messages.get(client);
        Integer requiredLength = message.getLeft();
        LinkedList<Packet> receivedPackets = message.getRight();

        receivedPackets.push(packet);

        if (requiredLength == receivedPackets.size()) {
            this.stateData = client;

            this.nextState(ServerSocketHandlerState.CompletedMessage);
        } else {
            this.nextState(ServerSocketHandlerState.Waiting);
        }
    }

    private void handleCompletedMessage() {
        SocketAddress address = (SocketAddress) this.stateData;

        Pair<Integer, LinkedList<Packet>> message = this.messages.get(address);
        this.messages.remove(address);

        Event event = (Event) message.getValue().getLast().getData();
        // logger.info("Event type: " + event.getType());

        SelectableChannel outputChannel = this.clientAC.get(address);
        SinkChannel channel = (SinkChannel) outputChannel;
        try {
            NBChannelController.write(channel, event);
        } catch (IOException e) {
            System.err.println("Cannot write to the channel.");
            this.nextState(ServerSocketHandlerState.Error);
            return;
        }

        this.nextState(ServerSocketHandlerState.Waiting);
    }

    private void handleNewComEvent() {
        SourceChannel inputChannel = (SourceChannel) this.channel;

        Event reqNC;
        try {
            reqNC = (Event) NBChannelController.read(inputChannel);
        } catch (IOException e) {
            System.err.println("Cannot read from the channel.");
            this.nextState(ServerSocketHandlerState.Waiting);
            return;
        }

        SocketAddress address = this.clientCA.get(this.channel);

        Packet packet = new Packet(reqNC.getType(), reqNC);
        this.networkCom.send(packet, address);

        this.nextState(ServerSocketHandlerState.Waiting);
    }

    private void handleError() {
        // TODO: close connection

        logger.warn("Error occured while processing the last state. Resetting...");
    }

    public boolean hasClientPipe() {
        return this.clientPipe != null;
    }

    public Pair<Pipe, Pipe> getClientPipe() {
        Pair<Pipe, Pipe> result = this.clientPipe;
        this.clientPipe = null;
        return result;
    }

}
