package com.ivatolm.app.parser.arguments;

/**
 * This class represents float command line argument.
 *
 * @author ivatolm
 */
public class FloatArgument extends Argument {

    /**
     * Constructs new instance with provided arguments.
     *
     * @param name name
     * @param check check
     * @param greeingMsg greeting message
     * @param errorMsg error message
     */
    public FloatArgument(String name, ArgCheck check, String greeingMsg, String errorMsg) {
        super(name, check, greeingMsg, errorMsg);
    }

    /**
     * Implements {@code parse} for {@code Parsable}.
     *
     * @param value value to parse
     * @throws NumberFormatException if number format is invalid
     */
    @Override
    public void parse(String value) throws NumberFormatException {
        this.value = Float.parseFloat(value);
    }

}
