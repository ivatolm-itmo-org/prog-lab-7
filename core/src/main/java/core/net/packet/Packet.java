package core.net.packet;

import java.io.Serializable;

import core.event.EventType;

/**
 * Class representing communication packet used
 * by client and server.
 *
 * @author ivatolm
 */
public class Packet implements Serializable {

    // Type of packet
    private EventType type;

    // Data of packet
    private Object data;

    /**
     * Constructs new {@code Packet} with provided arguments.
     *
     * @param type type of the packet
     * @param data data of the packet
     */
    public Packet(EventType type, Object data) {
        this.type = type;
        this.data = data;
    }

    /**
     * @return type of the packet
     */
    public EventType getType() {
        return this.type;
    }

    /**
     * @return data of the packet
     */
    public Object getData() {
        return this.data;
    }

}
