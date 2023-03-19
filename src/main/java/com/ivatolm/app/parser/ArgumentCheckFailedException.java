package com.ivatolm.app.parser;

/**
 * Exception reperesenting situation of error occuring
 * while parsing command arguments.
 *
 * @author ivatolm
 */
public class ArgumentCheckFailedException extends Exception {

    /**
     * Constructs new instance with message of {@code message}
     *
     * @param message message of the exception
     */
    public ArgumentCheckFailedException(String message) {
        super(message);
    }

}
