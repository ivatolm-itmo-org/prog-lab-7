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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.event.Event;
import core.event.EventType;
import core.handler.ChannelType;
import core.handler.HandlerChannel;
import core.handler.HandlerChannels;
import core.handler.SocketHandler;
import core.net.Com;
import core.net.packet.Packet;
import core.utils.NBChannelController;
import server.net.DisconnectionTask;

enum ServerSocketHandlerState {
    Waiting,
    NewEvent,
    ConnectionTimeout,
    NewNetworkEvent,
    NewNetworkPacket,
    CompletedMessage,
    NewComEvent,
    Error
}

public class ServerSocketHandler extends SocketHandler<DatagramChannel, ServerSocketHandlerState> {

    private static final int DISCONNECTION_DELAY = 1000;

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
    private HashMap<SocketAddress, Pair<Timer, TimerTask>> timers = new HashMap<>();

    // State data
    private Object stateData;

    // Communicaton Handler
    private Pair<Pipe, Pipe> newClientPipe;

    // Disconnection task
    private Lock disconnectionLock;
    private Pipe disconnectionPipe;

    /**
     * Constructs new {@code ServerComHandler} with provided arguments.
     *
     * @param inputChannels input channels of the handler
     * @param outputChannels output channels of the handler
     * @param networkCom network communicator
     * @throws IOException if cannot open a pipe
     */
    public ServerSocketHandler(HandlerChannels inputChannels,
                               HandlerChannels outputChannels,
                               Com networkCom) throws IOException {
        super(inputChannels, outputChannels, ServerSocketHandlerState.Waiting, networkCom);

        this.stateData = null;
        this.newClientPipe = null;

        this.disconnectionLock = new ReentrantLock();
        this.disconnectionPipe = Pipe.open();
        this.inputChannels.add(new HandlerChannel(ChannelType.Internal, disconnectionPipe.source()));
    }

    @Override
    public void process(ChannelType type, SelectableChannel channel) {
        logger.trace("New event from {}", type);

        switch (type) {
            case Internal:
            case Com:
            case Network:
                this.readyChannels = new LinkedList<ChannelType>() {{ add(type); }};

                // TODO: REMOVE FROM HERE
                this.channel = channel;
                break;
            default:
                logger.warn("Unexpected channel.");
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
                case ConnectionTimeout:
                    this.handleConnectionTimeout();
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

            logger.trace("State: {} -> {}", stState, this.getState());
        } while (this.getState() != ServerSocketHandlerState.Waiting);
    }

    private void handleWaitingState() {
        if (this.readyChannels.isEmpty()) {
            return;
        }

        if (this.readyChannels.contains(ChannelType.Internal) ||
            this.readyChannels.contains(ChannelType.Network) ||
            this.readyChannels.contains(ChannelType.Com)) {

            ChannelType[] channels = this.readyChannels.toArray(new ChannelType[0]);
            this.channelType = channels[0];

            this.nextState(ServerSocketHandlerState.NewEvent);
        }
    }

    private void handleNewEvent() {
        switch (this.channelType) {
            case Internal:
                this.nextState(ServerSocketHandlerState.ConnectionTimeout);
                break;
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

    private void handleConnectionTimeout() {
        SourceChannel inputChannel = (SourceChannel) this.channel;

        Event reqDC;
        try {
            reqDC = (Event) NBChannelController.read(inputChannel);
        } catch (IOException e) {
            logger.error("Cannot read from the channel.");
            this.nextState(ServerSocketHandlerState.Error);
            return;
        }

        SocketAddress address = (SocketAddress) reqDC.getData();
        SelectableChannel clientChannel = this.clientAC.get(address);

        Event reqCL = new Event(EventType.Close, null);

        SinkChannel clientChannelOutput = (SinkChannel) clientChannel;
        try {
            NBChannelController.write(clientChannelOutput, reqCL);
        } catch (IOException | NullPointerException e) {
            logger.error("Cannot write to the channel.");
            this.nextState(ServerSocketHandlerState.Error);
        }

        this.knownClients.remove(address);
        this.clientAC.remove(address);
        this.clientCA.remove(channel);
        this.messages.remove(address);
        this.timers.remove(address);

        this.nextState(ServerSocketHandlerState.Waiting);
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
                logger.error("Cannot write to the channel.");
                this.nextState(ServerSocketHandlerState.Error);
                return;
            }

            this.knownClients.add(address);
            this.clientAC.put(address, socket_client.sink());
            this.clientCA.put(client_socket.source(), address);
            this.messages.put(address, new ImmutablePair<>(1, new LinkedList<>()));

            Timer timer = new Timer();
            this.timers.put(address, new ImmutablePair<>(timer, null));

            this.newClientPipe = new ImmutablePair<>(socket_client, client_socket);

            this.nextState(ServerSocketHandlerState.NewNetworkPacket);
        }
    }

    private void handleNewNetworkPacket() {
        @SuppressWarnings("unchecked")
        Pair<SocketAddress, Packet> data = (Pair<SocketAddress, Packet>) this.stateData;

        SocketAddress client = data.getKey();

        Pair<Timer, TimerTask> item = this.timers.get(client);
        Timer timer = item.getLeft();
        TimerTask task = item.getRight();

        if (task != null) {
            task.cancel();
        }

        TimerTask newTask = new DisconnectionTask(
            this.disconnectionLock,
            this.disconnectionPipe.sink(),
            client
        );
        timer.schedule(newTask, DISCONNECTION_DELAY);
        this.timers.put(client, new ImmutablePair<>(timer, newTask));

        Packet packet = data.getValue();
        if (packet.getType() == EventType.Ping) {
            this.nextState(ServerSocketHandlerState.Waiting);
            return;
        }

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
            logger.error("Cannot write to the channel.");
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
            logger.error("Cannot read from the channel.");
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

    public boolean hasNewClient() {
        return this.newClientPipe != null;
    }

    public Pair<Pipe, Pipe> getNewClientPipe() {
        Pair<Pipe, Pipe> result = this.newClientPipe;
        this.newClientPipe = null;
        return result;
    }

}
