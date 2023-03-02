package com.ivatolm.app.parser.arguments;

import com.ivatolm.app.models.Mood;

/**
 * This class represents mood command line argument.
 *
 * @author ivatolm
 */
public class MoodArgument extends Argument {

    /**
     * Constructs new instance with provided arguments.
     *
     * @param check check
     * @param greeingMsg greeting message
     * @param errorMsg error message
     */
    public MoodArgument(ArgCheck check, String greeingMsg, String errorMsg) {
        super(check, greeingMsg, errorMsg);
    }

    /**
     * Implements {@code parse} for {@code IParsable}.
     *
     * @param value value to parse
     */
    @Override
    public void parse(String value) {
        this.value = Mood.parseMood(value);
    }

}
