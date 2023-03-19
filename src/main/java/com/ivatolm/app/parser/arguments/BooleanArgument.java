package com.ivatolm.app.parser.arguments;

/**
 * This class represents boolean command line argument.
 *
 * @author ivatolm
 */
public class BooleanArgument extends Argument {

    /**
     * Constructs new instance without arguments.
     */
    public BooleanArgument() {
        super(null, null, null, null);
    }

    /**
     * Constructs new instance with provided arguments.
     *
     * @param name name
     * @param check check
     * @param greeingMsg greeting message
     * @param errorMsg error message
     */
    public BooleanArgument(String name, ArgCheck check, String greeingMsg, String errorMsg) {
        super(name, check, greeingMsg, errorMsg);
    }

    /**
     * Implements {@code parse} for {@code Parsable}.
     *
     * @param value value to parse
     */
    @Override
    public void parse(String value) {
        try {
            int intValue = Integer.parseInt(value);

            if (intValue == 0) {
                this.value = false;
            }

            if (intValue == 1) {
                this.value = true;
            }

            return;
        } catch (Exception e) {
            /*
                Boolean value is not represented by integer, maybe it's
                represented by Boolean. So, skipping...
             */
        }

        this.value = Boolean.parseBoolean(value);
    }

}
