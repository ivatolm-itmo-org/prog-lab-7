package com.ivatolm.app.models.humanBeing;

import com.ivatolm.app.parser.arguments.ArgCheck;

/**
 * Validator for {@code name} field of the {@code HumanBeing}.
 *
 * @author ivatolm
 */
public class HumanBeingNameValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        return value != null && !value.isEmpty();
    }

}
