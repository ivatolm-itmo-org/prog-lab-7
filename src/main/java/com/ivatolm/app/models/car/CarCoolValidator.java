package com.ivatolm.app.models.car;

import com.ivatolm.app.parser.arguments.ArgCheck;

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

        boolean y = ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value));
        return y;
    }

}
