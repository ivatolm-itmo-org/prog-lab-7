package com.ivatolm.app.parser.arguments;

/**
 * Interface for parsing arguments.
 *
 * @author ivatolm
 */
interface IParsable {

    /**
     * Parses {@code value} and overrides internal one with new one.
     *
     * @param value
     */
    void parse(String value);

}

/**
 * Class that represents general command line argument.
 *
 * @author ivatolm
 */
public abstract class Argument implements IParsable {

    /** Value validator */
    private ArgCheck check;

    /** Greeting message */
    private String greetingMsg;

    /** Error message */
    private String errorMsg;

    /** Internal value of the argument */
    protected Object value;

    /**
     * Constructs new instance with given arguments.
     *
     * @param check value validator
     * @param greeingMsg greeting message
     * @param errorMsg error message
     */
    public Argument(ArgCheck check, String greeingMsg, String errorMsg) {
        this.check = check;
        this.greetingMsg = greeingMsg;
        this.errorMsg = errorMsg;
    }

    /**
     * @return {@code check} field of the object
     */
    public ArgCheck getCheck() {
        return this.check;
    }

    /**
     * @return {@code greetingMsg} field of the object
     */
    public String getGreetingMsg() {
        return this.greetingMsg;
    }

    /**
     * @return {@code errorMsg} field of the object
     */
    public String getErrorMsg() {
        return this.errorMsg;
    }

    /**
     * @return {@code value} field of the object
     */
    public Object getValue() {
        return this.value;
    }

}
