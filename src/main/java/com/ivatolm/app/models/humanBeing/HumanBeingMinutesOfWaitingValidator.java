package com.ivatolm.app.models.humanBeing;

import com.ivatolm.app.parser.arguments.ArgCheck;

/**
 * Validator for {@code minutesOfWaiting} field of the {@code HumanBeing}.
 *
 * @author ivatolm
 */
public class HumanBeingMinutesOfWaitingValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        if (value == null)
            return true;

        try {
            Integer.parseInt(value);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

}
