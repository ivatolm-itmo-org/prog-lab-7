package core.net;

import java.nio.channels.SelectableChannel;

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
     */
    void send(Packet packet);

    /**
     * Receives packet from other peer.
     *
     * @return received packet
     */
    Packet receive();

    /**
     * Returns {@code Channel} object.
     *
     * @return channel of communication
     */
    SelectableChannel getChannel();

}
