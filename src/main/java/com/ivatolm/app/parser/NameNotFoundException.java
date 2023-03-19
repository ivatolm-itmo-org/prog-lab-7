package com.ivatolm.app.parser;

/**
 * Exception reperesenting situation of object with provided
 * name not being found.
 *
 * @author ivatolm
 */
public class NameNotFoundException extends RuntimeException {

    /**
     * Constructs new instance with message of {@code message}
     *
     * @param message message of the exception
     */
    public NameNotFoundException(String message) {
        super(message);
    }

}
