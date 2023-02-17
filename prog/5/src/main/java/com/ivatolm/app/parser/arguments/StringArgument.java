package com.ivatolm.app.parser.arguments;

public class StringArgument extends Argument {

    public StringArgument(ArgCheck check, String greeingMsg, String errorMsg) {
        super(check, greeingMsg, errorMsg);
    }

    @Override
    public void parse(String value) {
        this.value = value;
    }

}
