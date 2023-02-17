package com.ivatolm.app.parser.arguments;

interface Parsable {

    void parse(String value);

}

public abstract class Argument implements Parsable {

    private ArgCheck check;
    private String greetingMsg;
    private String errorMsg;

    protected Object value;

    public Argument(ArgCheck check, String greeingMsg, String errorMsg) {
        this.check = check;
        this.greetingMsg = greeingMsg;
        this.errorMsg = errorMsg;
    }

    public ArgCheck getCheck() {
        return this.check;
    }

    public String getGreetingMsg() {
        return this.greetingMsg;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }

    public Object getValue() {
        return this.value;
    }

}
