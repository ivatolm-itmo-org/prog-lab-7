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

    /**
     * Constructs new {@code Event} with provided arguments.
     *
     * @param type type of the event
     * @param data data of the event
     */
    public Event(EventType type, Object data) {
        this.type = type;
        this.data = data;
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

}
