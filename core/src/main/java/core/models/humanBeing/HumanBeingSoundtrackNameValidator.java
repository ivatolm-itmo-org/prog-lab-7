package core.models.humanBeing;

import core.command.arguments.ArgCheck;

/**
 * Validator for {@code soundtrackName} field of the {@code HumanBeing}.
 *
 * @author ivatolm
 */
public class HumanBeingSoundtrackNameValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        return value != null;
    }

}
