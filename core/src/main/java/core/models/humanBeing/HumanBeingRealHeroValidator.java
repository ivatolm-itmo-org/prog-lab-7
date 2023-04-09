package core.models.humanBeing;

import core.command.arguments.ArgCheck;
import core.command.arguments.BooleanArgument;

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

        BooleanArgument arg = new BooleanArgument();
        arg.parse(value);

        return ((Boolean) arg.getValue()) instanceof Boolean;
    }

}
