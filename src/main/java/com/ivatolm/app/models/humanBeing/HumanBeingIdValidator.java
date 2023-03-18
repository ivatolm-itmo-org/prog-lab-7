package com.ivatolm.app.models.humanBeing;

import com.ivatolm.app.interpreter.Interpreter;
import com.ivatolm.app.parser.arguments.ArgCheck;
import com.ivatolm.app.parser.arguments.LongArgument;

/**
 * Validator for {@code id} field of the {@code HumanBeing}.
 *
 * @author ivatolm
 */
public class HumanBeingIdValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        LongArgument id = new LongArgument("id", null, null, null);

        try {
            id.parse(value);
        } catch (NumberFormatException e) {
            return false;
        }

        return Interpreter.HasItemWithId(id);
    }

}
