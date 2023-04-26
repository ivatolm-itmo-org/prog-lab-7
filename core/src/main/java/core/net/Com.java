package core.net;

import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;

import org.apache.commons.lang3.tuple.Pair;

import core.net.packet.Packet;

/**
 * Interface for network communication.
 *
 * @author ivatolm
 */
public interface Com {

    /**
     * Checks for connection being alive.
     *
     * @return true if connection is alive, else false
     */
    boolean isAlive();

    /**
     * Sends packet to other peer.
     *
     * @param packet packet to be sent
     * @param address destination address
     */
    void send(Packet packet, SocketAddress address);

    /**
     * Receives packet from other peer.
     *
     * @return pair of ip and received packet
     */
    Pair<SocketAddress, Packet> receive();

    /**
     * Returns {@code Channel} object.
     *
     * @return channel of communication
     */
    SelectableChannel getChannel();

}
