package com.ivatolm.app.parser.arguments;

/**
 * This class represents long command line argument.
 *
 * @author ivatolm
 */
public class LongArgument extends Argument {

    /**
     * Constructs new instance with provided arguments.
     *
     * @param check check
     * @param greeingMsg greeting message
     * @param errorMsg error message
     */
    public LongArgument(ArgCheck check, String greeingMsg, String errorMsg) {
        super(check, greeingMsg, errorMsg);
    }

    /**
     * Implements {@code parse} for {@code IParsable}.
     *
     * @param value value to parse
     */
    @Override
    public void parse(String value) {
        this.value = Long.parseLong(value);
    }

}
