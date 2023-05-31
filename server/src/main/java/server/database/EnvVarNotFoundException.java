package server.database;

/**
 * Exception reperesenting situation of environment
 * variable not being found.
 *
 * @author ivatolm
 */
public class EnvVarNotFoundException extends Exception {

    /**
     * Constructs new instance with message of {@code message}
     *
     * @param message message of the exception
     */
    public EnvVarNotFoundException(String message) {
        super(message);
    }

}
