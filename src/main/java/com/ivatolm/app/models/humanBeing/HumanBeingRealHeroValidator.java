package com.ivatolm.app.models.humanBeing;

import com.ivatolm.app.parser.arguments.ArgCheck;
import com.ivatolm.app.parser.arguments.BooleanArgument;

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

        BooleanArgument arg = new BooleanArgument(null, null, null, null);
        arg.parse(value);

        return ((Boolean) arg.getValue()) instanceof Boolean;
    }

}
