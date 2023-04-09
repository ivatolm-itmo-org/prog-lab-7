package core.net.packet;

import java.io.Serializable;

/**
 * Class representing communication packet used
 * by client and server.
 *
 * @author ivatolm
 */
public class Packet implements Serializable {

    // Type of packet
    private PacketType type;

    // Data of packet
    private Object data;

    /**
     * Constructs new {@code Packet} with provided arguments.
     *
     * @param type type of the packet
     * @param data data of the packet
     */
    public Packet(PacketType type, Object data) {
        this.type = type;
    }

    /**
     * @return type of the packet
     */
    public PacketType getType() {
        return this.type;
    }

    /**
     * @return data of the packet
     */
    public Object getData() {
        return this.data;
    }

}
