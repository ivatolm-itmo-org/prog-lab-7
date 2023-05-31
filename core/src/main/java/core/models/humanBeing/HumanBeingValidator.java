package core.models.humanBeing;

import core.command.arguments.ArgCheck;
import core.models.car.CarValidator;
import core.models.coordinates.CoordinatesValidator;
import core.models.mood.MoodValidator;
import core.utils.SimpleParseException;

/**
 * Validator for {@code HumanBeing}.
 *
 * @author ivatolm
 */
public class HumanBeingValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        HumanBeing hb = new HumanBeing();

        String[] string = new String[] { value };
        try {
            hb.deserialize(string);
        } catch (SimpleParseException e) {
            System.err.println(e);
            return false;
        }

        HumanBeingIdValidator idValidator = new HumanBeingIdValidator();
        HumanBeingCreationDateValidator creationDateValidator = new HumanBeingCreationDateValidator();
        HumanBeingNameValidator nameValidator = new HumanBeingNameValidator();
        CoordinatesValidator coordinateValidator = new CoordinatesValidator();
        HumanBeingRealHeroValidator realHeroValidator = new HumanBeingRealHeroValidator();
        HumanBeingHasToothpickValidator hasToothpickValidator = new HumanBeingHasToothpickValidator();
        HumanBeingImpactSpeedValidator impactSpeedValidator = new HumanBeingImpactSpeedValidator();
        HumanBeingMinutesOfWaitingValidator minutesOfWaitingValidator = new HumanBeingMinutesOfWaitingValidator();
        MoodValidator moodValidator = new MoodValidator();
        CarValidator carValidator = new CarValidator();

        return idValidator.check("" + hb.getId()) &&
               creationDateValidator.check("" + hb.getCreationDate()) &&
               nameValidator.check("" + hb.getName()) &&
               coordinateValidator.check(hb.getCoordinates().serialize()[0]) &&
               realHeroValidator.check("" + hb.getRealHero()) &&
               hasToothpickValidator.check("" + hb.getHasToothpick()) &&
               impactSpeedValidator.check("" + hb.getImpactSpeed()) &&
               minutesOfWaitingValidator.check("" + hb.getMinutesOfWaiting()) &&
               moodValidator.check("" + hb.getMood().serialize()[0]) &&
               carValidator.check("" + hb.getCar().serialize()[0]);
    }

}
