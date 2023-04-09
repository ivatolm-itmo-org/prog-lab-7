package server.runner;

/**
 * Exception reperesenting situation of recursion being
 * detected while running user provided script.
 *
 * @author ivatolm
 */
public class RecursionFoundException extends Exception {

    /**
     * Constructs new instance with message of {@code message}
     *
     * @param message message of the exception
     */
    public RecursionFoundException(String message) {
        super(message);
    }

}
