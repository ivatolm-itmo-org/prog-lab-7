package core.models.humanBeing;

import core.command.arguments.ArgCheck;

/**
 * Validator for {@code name} field of the {@code HumanBeing}.
 *
 * @author ivatolm
 */
public class HumanBeingNameValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        return value != null && !value.isEmpty();
    }

}
