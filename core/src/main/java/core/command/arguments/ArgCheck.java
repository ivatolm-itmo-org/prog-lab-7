package core.command.arguments;

/**
 * Functional interface for validating command arguments.
 *
 * @author ivatolm
 */
public interface ArgCheck {

    /**
     * Validates argument by prodived (by task) specification.
     *
     * @param value argument to check
     * @return true if argument is valid, else false
     */
    boolean check(String value);

}
