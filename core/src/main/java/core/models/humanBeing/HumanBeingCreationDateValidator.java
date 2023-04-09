package core.models.humanBeing;

import java.time.LocalDate;

import core.command.arguments.ArgCheck;

/**
 * Validator for {@code creationDate} field of the {@code HumanBeing}.
 *
 * @author ivatolm
 */
public class HumanBeingCreationDateValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        try {
            LocalDate.parse(value);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

}
