package core.models.coordinates;

import core.command.arguments.ArgCheck;
import core.utils.SimpleParseException;

/**
 * Validator for {@code Coordinates}.
 *
 * @author ivatolm
 */
public class CoordinatesValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        Coordinates coordinates = new Coordinates();

        String[] string = new String[] { value };
        try {
            coordinates.deserialize(string);
        } catch (SimpleParseException e) {
            System.err.println(e);
            return false;
        }

        CoordinatesXValidator xValidator = new CoordinatesXValidator();
        CoordinatesYValidator yValidator = new CoordinatesYValidator();

        return xValidator.check("" + coordinates.getX()) &&
               yValidator.check("" + coordinates.getY());
    }

}
