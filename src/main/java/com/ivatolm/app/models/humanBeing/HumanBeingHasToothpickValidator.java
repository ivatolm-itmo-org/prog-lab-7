package com.ivatolm.app.models.humanBeing;

import com.ivatolm.app.parser.arguments.ArgCheck;

/**
 * Validator for {@code hasToothpick} field of the {@code HumanBeing}.
 *
 * @author ivatolm
 */
public class HumanBeingHasToothpickValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        return value != null && ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value));
    }

}
