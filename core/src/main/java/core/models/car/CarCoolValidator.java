package core.models.car;

import core.command.arguments.ArgCheck;
import core.command.arguments.BooleanArgument;

/**
 * Validator for {@code cool} field of the {@code Car}.
 *
 * @author ivatolm
 */
public class CarCoolValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        if (value == null)
            return true;

        BooleanArgument arg = new BooleanArgument();
        arg.parse(value);

        return ((Boolean) arg.getValue()) instanceof Boolean;
    }

}
