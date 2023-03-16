package com.ivatolm.app.models.car;

import com.ivatolm.app.parser.arguments.ArgCheck;
import com.ivatolm.app.parser.arguments.BooleanArgument;

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

        BooleanArgument arg = new BooleanArgument(null, null, null, null);
        arg.parse(value);

        return ((Boolean) arg.getValue()) instanceof Boolean;
    }

}
