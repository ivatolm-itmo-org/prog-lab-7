package core.models.coordinates;

import core.command.arguments.ArgCheck;

/**
 * Validator for {@code x} field of the {@code Coordinates}.
 *
 * @author ivatolm
 */
public class CoordinatesXValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        int y;

        try {
            y = Integer.parseInt(value);
        } catch (Exception e) {
            return false;
        }

        return value != null && y > -58;
    }

}
