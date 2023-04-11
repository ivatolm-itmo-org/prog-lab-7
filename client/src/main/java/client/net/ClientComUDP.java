package client.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SelectableChannel;

import org.apache.commons.lang3.SerializationUtils;

import core.net.Com;
import core.net.packet.Packet;

public class ClientComUDP implements Com {
    // Server ip
    private String ip;

    // Server port
    private Integer port;

    // Socket
    private DatagramSocket socket;

    // Address
    private InetAddress address;

    /**
     * Constructs new {@code ClientComUDP} with provided arguments.
     *
     * @param ip server ip
     * @param port server port
     */
    public ClientComUDP(String ip, Integer port) throws SocketException, UnknownHostException {
        this.ip = ip;
        this.port = port;

        this.socket = new DatagramSocket();
        this.address = InetAddress.getByName(this.ip);
    }

    /**
     * Implements {@code isAlive} method of {@code Com}.
     */
    @Override
    public boolean isAlive() {
        throw new UnsupportedOperationException("Unimplemented method 'isAlive'");
    }

    /**
     * Implements {@code send} method of {@code Com}.
     */
    @Override
    public void send(Packet packet) {
        byte[] data = SerializationUtils.serialize(packet);

        DatagramPacket pkt = new DatagramPacket(data, data.length, this.address, this.port);
        try {
            this.socket.send(pkt);
        } catch (IOException e) {
            System.err.println("Cannot send packet: " + e);
        }
    }

    /**
     * Implements {@code receive} method of {@code Com}.
     */
    @Override
    public Packet receive() {
        byte[] data = new byte[1024];

        DatagramPacket pkt = new DatagramPacket(data, data.length);
        try {
            this.socket.receive(pkt);
        } catch (IOException e) {
            System.err.println("Cannot receive packet: " + e);
            return null;
        }

        return SerializationUtils.deserialize(data);
    }

    /**
     * Implements {@code getChannel} method of {@code Com}.
     */
    @Override
    public SelectableChannel getChannel() {
        return this.socket.getChannel();
    }

}
