package com.ivatolm.app.parser.arguments;

public class LongArgument extends Argument {

    public LongArgument(ArgCheck check, String greeingMsg, String errorMsg) {
        super(check, greeingMsg, errorMsg);
    }

    @Override
    public void parse(String value) {
        this.value = Long.parseLong(value);
    }

}
