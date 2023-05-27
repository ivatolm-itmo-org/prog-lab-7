package core.event;

import java.io.Serializable;

/**
 * Class representing communication event used by client and server.
 *
 * @author ivatolm
 */
public class Event implements Serializable {

    // Type of event
    private EventType type;

    // Data of event
    private Object data;

    // Credentials
    private String token;

    /**
     * Constructs new {@code Event} with provided arguments.
     *
     * @param type type of the event
     * @param data data of the event
     */
    public Event(EventType type, Object data) {
        this.type = type;
        this.data = data;
        this.token = null;
    }

    /**
     * Constructs new {@code Event} with provided arguments.
     *
     * @param type type of the event
     * @param data data of the event
     * @param token access credentials
     */
    public Event(EventType type, Object data, String token) {
        this.type = type;
        this.data = data;
        this.token = token;
    }

    /**
     * @return type of the event
     */
    public EventType getType() {
        return this.type;
    }

    /**
     * @return data of the event
     */
    public Object getData() {
        return this.data;
    }

    /**
     * @return access credentials
     */
    public Object getToken() {
        return this.token;
    }

    @Override
    public String toString() {
        return String.format(
            "(type: %s, data: %s, token: %s)",
            this.type, this.data.hashCode(), this.token
        );
    }

}
