package server.event;

import java.io.Serializable;

/**
 * Class representing communication event used
 * by server.
 *
 * @author ivatolm
 */
public class ServerEvent implements Serializable {

    // Type of event
    private ServerEventType type;

    // Data of event
    private Object data;

    /**
     * Constructs new {@code Packet} with provided arguments.
     *
     * @param type type of the event
     * @param data data of the event
     */
    public ServerEvent(ServerEventType type, Object data) {
        this.type = type;
        this.data = data;
    }

    /**
     * @return type of the event
     */
    public ServerEventType getType() {
        return this.type;
    }

    /**
     * @return data of the event
     */
    public Object getData() {
        return this.data;
    }

}
