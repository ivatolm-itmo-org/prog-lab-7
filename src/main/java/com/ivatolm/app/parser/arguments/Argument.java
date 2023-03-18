package com.ivatolm.app.parser.arguments;

/**
 * Interface for parsing arguments.
 *
 * @author ivatolm
 */
interface Parsable {

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
public abstract class Argument implements Parsable {

    /** Name of the argument */
    private String name;

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
     * @param name name of the argument
     * @param check value validator
     * @param greeingMsg greeting message
     * @param errorMsg error message
     */
    public Argument(String name, ArgCheck check, String greeingMsg, String errorMsg) {
        this.name = name;
        this.check = check;
        this.greetingMsg = greeingMsg;
        this.errorMsg = errorMsg;
    }

    /**
     * @return {@code name} field of the object
     */
    public String getName() {
        return this.name;
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

    /**
     * @param value new value for {@code value}
     */
    public void setValue(Object value) {
        this.value = value;
    }

}
