package core.event;

/**
 * Exception reperesenting situation of selector key being non
 * existant.
 *
 * @author ivatolm
 */
public class SelectorKeyNotFoundException extends Exception {

    /**
     * Constructs new instance with message of {@code message}
     *
     * @param message message of the exception
     */
    public SelectorKeyNotFoundException(String message) {
        super(message);
    }

}