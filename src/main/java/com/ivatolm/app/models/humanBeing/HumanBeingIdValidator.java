package com.ivatolm.app.models.humanBeing;

import com.ivatolm.app.parser.arguments.ArgCheck;

/**
 * Validator for {@code id} field of the {@code HumanBeing}.
 *
 * @author ivatolm
 */
public class HumanBeingIdValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        float y;

        try {
            y = Float.parseFloat(value);
        } catch (Exception e) {
            return false;
        }

        return value != null && y <= 414;
    }

}
