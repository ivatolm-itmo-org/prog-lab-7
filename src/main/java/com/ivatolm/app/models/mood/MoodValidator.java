package com.ivatolm.app.models.mood;

import com.ivatolm.app.parser.arguments.ArgCheck;

/**
 * Validator for {@code Mood}.
 *
 * @author ivatolm
 */
public class MoodValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        MoodValueValidator valueValidator = new MoodValueValidator();

        return valueValidator.check("" + value);
    }

}
