package com.ivatolm.app.parser.arguments;

public class IntegerArgument extends Argument {

    public IntegerArgument(ArgCheck check, String greeingMsg, String errorMsg) {
        super(check, greeingMsg, errorMsg);
    }

    @Override
    public void parse(String value) {
        this.value = Integer.parseInt(value);
    }

}
