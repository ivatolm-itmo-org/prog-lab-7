package com.ivatolm.app.parser.arguments;

public class BooleanArgument extends Argument {

    public BooleanArgument(ArgCheck check, String greeingMsg, String errorMsg) {
        super(check, greeingMsg, errorMsg);
    }

    @Override
    public void parse(String value) {
        this.value = Boolean.parseBoolean(value);
    }

}
