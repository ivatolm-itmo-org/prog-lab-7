package core.models.car;

import core.command.arguments.ArgCheck;
import core.utils.SimpleParseException;

/**
 * Validator for {@code Car}.
 *
 * @author ivatolm
 */
public class CarValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        Car car = new Car();

        String[] string = new String[] { value };
        try {
            car.deserialize(string);
        } catch (SimpleParseException e) {
            System.err.println(e);
            return false;
        }

        CarNameValidator nameValidator = new CarNameValidator();
        CarCoolValidator coolValidator = new CarCoolValidator();

        return nameValidator.check("" + car.getName()) &&
               coolValidator.check("" + car.getCool());
    }

}
