package com.ivatolm.app.parser;

/**
 * Exception reperesenting situation of error occuring
 * while parsing user's command.
 *
 * @author ivatolm
 */
public class SimpleParseException extends Exception {

    /**
     * Constructs new instance with message of {@code message}
     *
     * @param message message of the exception
     */
    public SimpleParseException(String message) {
        super(message);
    }

}
