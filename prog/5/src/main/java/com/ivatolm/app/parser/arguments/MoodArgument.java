package com.ivatolm.app.parser.arguments;

import com.ivatolm.app.humanBeing.Mood;

public class MoodArgument extends Argument {

    public MoodArgument(ArgCheck check, String greeingMsg, String errorMsg) {
        super(check, greeingMsg, errorMsg);
    }

    @Override
    public void parse(String value) {
        this.value = Mood.parseMood(value);
    }

}
