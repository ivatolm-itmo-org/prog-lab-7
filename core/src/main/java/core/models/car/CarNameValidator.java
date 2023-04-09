package core.models.car;

import core.command.arguments.ArgCheck;

/**
 * Validator for {@code name} field of the {@code Car}.
 *
 * @author ivatolm
 */
public class CarNameValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        return value != null;
    }

}
