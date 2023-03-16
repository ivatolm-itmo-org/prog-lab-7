package com.ivatolm.app.models.humanBeing;

import com.ivatolm.app.models.car.CarValidator;
import com.ivatolm.app.models.coordinates.CoordinatesValidator;
import com.ivatolm.app.models.mood.MoodValidator;
import com.ivatolm.app.parser.arguments.ArgCheck;
import com.ivatolm.app.utils.SimpleParseException;

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
               realHeroValidator.check("" + hb.isRealHero()) &&
               hasToothpickValidator.check("" + hb.getHasToothpick()) &&
               impactSpeedValidator.check("" + hb.getImpactSpeed()) &&
               minutesOfWaitingValidator.check("" + hb.getMinutesOfWaiting()) &&
               moodValidator.check("" + hb.getMood().serialize()[0]) &&
               carValidator.check("" + hb.getCar().serialize()[0]);
    }

}
