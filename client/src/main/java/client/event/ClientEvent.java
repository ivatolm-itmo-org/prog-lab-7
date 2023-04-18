package client.event;

import java.io.Serializable;

/**
 * Class representing communication event used
 * by client.
 *
 * @author ivatolm
 */
public class ClientEvent implements Serializable {

    // Type of client event
    private ClientEventType type;

    // Data of client event
    private Object data;

    /**
     * Constructs new {@code Packet} with provided arguments.
     *
     * @param type type of the client event
     * @param data data of the client event
     */
    public ClientEvent(ClientEventType type, Object data) {
        this.type = type;
        this.data = data;
    }

    /**
     * @return type of the client event
     */
    public ClientEventType getType() {
        return this.type;
    }

    /**
     * @return data of the client event
     */
    public Object getData() {
        return this.data;
    }

}
