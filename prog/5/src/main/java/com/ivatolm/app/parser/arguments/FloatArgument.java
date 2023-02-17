package com.ivatolm.app.parser.arguments;

public class FloatArgument extends Argument {

    public FloatArgument(ArgCheck check, String greeingMsg, String errorMsg) {
        super(check, greeingMsg, errorMsg);
    }

    @Override
    public void parse(String value) {
        this.value = Float.parseFloat(value);
    }

}
