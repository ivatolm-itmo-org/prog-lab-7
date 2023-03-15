package com.ivatolm.app.models.humanBeing;

import com.ivatolm.app.parser.arguments.ArgCheck;

/**
 * Validator for {@code realHero} field of the {@code HumanBeing}.
 *
 * @author ivatolm
 */
public class HumanBeingRealHeroValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        if (value == null)
            return true;

        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

}
