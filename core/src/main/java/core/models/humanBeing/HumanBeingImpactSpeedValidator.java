package core.models.humanBeing;

import core.command.arguments.ArgCheck;

/**
 * Validator for {@code impactSpeed} field of the {@code HumanBeing}.
 *
 * @author ivatolm
 */
public class HumanBeingImpactSpeedValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        try {
            Long.parseLong(value);
        } catch (NumberFormatException e) {
            return false;
        }

        return value != null;
    }

}
