package core.models.humanBeing;

import core.command.arguments.ArgCheck;
import core.command.arguments.LongArgument;

/**
 * Validator for {@code id} field of the {@code HumanBeing}.
 *
 * @author ivatolm
 */
public class HumanBeingIdValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        LongArgument id = new LongArgument("id", null, null, null);

        try {
            id.parse(value);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

}
