package core.models.humanBeing;

import core.command.arguments.ArgCheck;
import core.command.arguments.BooleanArgument;

/**
 * Validator for {@code hasToothpick} field of the {@code HumanBeing}.
 *
 * @author ivatolm
 */
public class HumanBeingHasToothpickValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        BooleanArgument arg = new BooleanArgument();
        arg.parse(value);

        return value != null && ((Boolean) arg.getValue()) instanceof Boolean;
    }

}
