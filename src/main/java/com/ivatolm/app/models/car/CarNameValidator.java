package com.ivatolm.app.models.car;

import com.ivatolm.app.parser.arguments.ArgCheck;

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
