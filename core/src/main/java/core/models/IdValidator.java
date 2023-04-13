package core.models;

import core.command.arguments.Argument;

/**
 * Functional interface for validating id command arguments.
 *
 * @author ivatolm
 */
public interface IdValidator {

    /**
     * Validates provided id command argument.
     *
     * @param value argument to check
     * @return true if argument is valid, else false
     */
    boolean check(Argument value);

}
