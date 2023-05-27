package core.utils;

/**
 * Exception reperesenting situation of internal channel
 * not being found.
 *
 * @author ivatolm
 */
public class ChannelNotFoundException extends RuntimeException {

    /**
     * Constructs new instance with message of {@code message}
     *
     * @param message message of the exception
     */
    public ChannelNotFoundException(String message) {
        super(message);
    }

}
