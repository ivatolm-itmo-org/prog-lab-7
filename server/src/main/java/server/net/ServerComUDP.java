package server.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.lang3.SerializationUtils;

import core.net.Com;
import core.net.packet.Packet;

public class ServerComUDP implements Com {
    // Server ip
    private String ip;

    // Server port
    private Integer port;

    // Socket
    private DatagramSocket socket;

    // Client address
    private InetAddress clientAddress;

    // Client port
    private Integer clientPort;

    /**
     * Constructs new {@code ClientComUDP} with provided arguments.
     *
     * @param ip server ip
     * @param port server port
     */
    public ServerComUDP(String ip, Integer port) throws SocketException, UnknownHostException {
        this.ip = ip;
        this.port = port;

        this.socket = new DatagramSocket(this.port);
        this.clientAddress = null;
        this.clientPort = null;
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

        if (this.clientAddress == null || this.clientPort == null) {
            System.err.println("Unknown destination");
            return;
        }

        DatagramPacket pkt = new DatagramPacket(
            data, data.length,
            this.clientAddress, this.clientPort
        );
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

        this.clientAddress = pkt.getAddress();
        this.clientPort = pkt.getPort();

        return SerializationUtils.deserialize(data);
    }

}
