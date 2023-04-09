package core.models.mood;

import core.command.arguments.ArgCheck;

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
