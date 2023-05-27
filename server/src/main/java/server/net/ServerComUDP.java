package server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.net.Com;
import core.net.packet.Packet;

public class ServerComUDP implements Com {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger("ComUDP");

    // Channel
    private DatagramChannel channel;

    /**
     * Constructs new {@code ClientComUDP} with provided arguments.
     *
     * @param ip server ip
     * @param port server port
     * @throws IOException if cannot open datagram channel
     */
    public ServerComUDP(String ip, Integer port) throws IOException {
        InetSocketAddress address = new InetSocketAddress(ip, port);
        this.channel = DatagramChannel.open();
        this.channel.bind(address);
    }

    /**
     * Implements {@code send} method of {@code Com}.
     */
    @Override
    public void send(Packet packet, SocketAddress address) {
        byte[] data = SerializationUtils.serialize(packet);
        ByteBuffer buffer = ByteBuffer.wrap(data);

        try {
            this.channel.send(buffer, address);
        } catch (IOException e) {
            logger.warn("Cannot send packet: " + e);
        }
    }

    /**
     * Implements {@code receive} method of {@code Com}.
     */
    @Override
    public Pair<SocketAddress, Packet> receive() {
        byte[] data = new byte[16384];
        ByteBuffer buffer = ByteBuffer.wrap(data);

        SocketAddress address;
        try {
            address = this.channel.receive(buffer);
        } catch (IOException e) {
            logger.warn("Cannot receive packet: " + e);
            return null;
        }

        return new ImmutablePair<>(address, SerializationUtils.deserialize(data));
    }

    /**
     * Implements {@code getChannel} method of {@code Com}.
     */
    @Override
    public SelectableChannel getChannel() {
        return this.channel;
    }

}
