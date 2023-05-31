package core.models.humanBeing;

import java.util.LinkedList;

import core.database.DataBaseObject;
import core.database.StrSerializable;
import core.models.Validatable;
import core.models.ValidateAsId;
import core.models.Validator;
import core.models.car.Car;
import core.models.car.CarValidator;
import core.models.coordinates.Coordinates;
import core.models.coordinates.CoordinatesValidator;
import core.models.mood.Mood;
import core.models.mood.MoodValidator;
import core.utils.SimpleParseException;
import lombok.Getter;
import lombok.Setter;

/**
 * Data structure for HumanBeing described in the task.
 *
 * @author ivatolm
 */
public class HumanBeing implements StrSerializable, DataBaseObject, Comparable<HumanBeing>, Validatable {

    /** Id field */
    @Setter
    @Getter
    @ValidateAsId
    @Validator(validator = HumanBeingIdValidator.class)
    private Long id;

    /** Name field */
    @Setter
    @Getter
    @Validator(validator = HumanBeingNameValidator.class)
    private String name;

    /** Coordinates field */
    @Setter
    @Getter
    @Validator(validator = CoordinatesValidator.class)
    private Coordinates coordinates;

    /** Creation date field */
    @Setter
    @Getter
    @Validator(validator = HumanBeingCreationDateValidator.class)
    private Long creationDate;

    /** Real hero field */
    @Setter
    @Getter
    @Validator(validator = HumanBeingRealHeroValidator.class)
    private Boolean realHero;

    /** Has toothpick field */
    @Setter
    @Getter
    @Validator(validator = HumanBeingHasToothpickValidator.class)
    private Boolean hasToothpick;

    /** Impact speed field */
    @Setter
    @Getter
    @Validator(validator = HumanBeingImpactSpeedValidator.class)
    private Long impactSpeed;

    /** Soundtrack name field */
    @Setter
    @Getter
    @Validator(validator = HumanBeingSoundtrackNameValidator.class)
    private String soundtrackName;

    /** Minutes of waiting field */
    @Setter
    @Getter
    @Validator(validator = HumanBeingMinutesOfWaitingValidator.class)
    private Integer minutesOfWaiting;

    /** Mood field */
    @Setter
    @Getter
    @Validator(validator = MoodValidator.class)
    private Mood mood;

    /** Car field */
    @Setter
    @Getter
    @Validator(validator = CarValidator.class)
    private Car car;

    /**
     * Constructs dummy-instance of the class.
     * Used to create dummy-instances of the class that will be instantly overriden.
     * Must not be used in typical case.
     */
    public HumanBeing() {}

    /**
     * Constructs new instance from the passed agruments.
     * Extracts and casts provided arguments to target types.
     *
     * @param args validated arguments from the command line
     */
    public HumanBeing(LinkedList<Object> args) {
        this.id               = (Long) args.get(0);
        this.name             = (String) args.get(1);
        this.coordinates      = (Coordinates) args.get(2);
        this.creationDate     = (Long) args.get(3);
        this.realHero         = (Boolean) args.get(4);
        this.hasToothpick     = (Boolean) args.get(5);
        this.impactSpeed      = (Long) args.get(6);
        this.soundtrackName   = (String) args.get(7);
        this.minutesOfWaiting = (Integer) args.get(8);
        this.mood             = (Mood) args.get(9);
        this.car              = (Car) args.get(10);
    }

    /**
     * Implements {@code serialize} for {@code StrSerializable}.
     * Sequentially serializes fiels into {@code String} array. If field is
     * complex serializes it first.
     *
     * @return serialized object
     */
    @Override
    public String[] serialize() {
        return new String[] {
            this.id               == null ? null : "" + this.id,
            this.name             == null ? null : "" + this.name,
            this.coordinates      == null ? null : "" + this.coordinates.serialize()[0],
            this.creationDate     == null ? null : "" + this.creationDate,
            this.realHero         == null ? null : "" + this.realHero,
            this.hasToothpick     == null ? null : "" + this.hasToothpick,
            this.impactSpeed      == null ? null : "" + this.impactSpeed,
            this.soundtrackName   == null ? null : "" + this.soundtrackName,
            this.minutesOfWaiting == null ? null : "" + this.minutesOfWaiting,
            this.mood             == null ? null : "" + this.mood.serialize()[0],
            this.car              == null ? null : "" + this.car.serialize()[0]
        };
    }

    /**
     * Implements {@code deserialize} for {@code StrSerializable}.
     * Casts input values to target types. Overrides internal values with new ones.
     *
     * @param value serialized object
     * @throws SimpleParseException if input is invalid
     */
    @Override
    public void deserialize(String[] value) throws SimpleParseException {
        if (value.length != 11) {
            throw new SimpleParseException(value + " must contain 11 values.");
        }

        this.id               = value[0] == "" ? null : Long.parseLong(value[0]);
        this.name             = value[1] == "" ? null : value[1];
        this.coordinates      = new Coordinates();
        this.coordinates.deserialize(new String[] { value[2] });
        this.creationDate     = value[3] == "" ? null : Long.parseLong(value[3]);
        this.realHero         = value[4] == "" ? null : Boolean.parseBoolean(value[4]);
        this.hasToothpick     = value[5] == "" ? null : Boolean.parseBoolean(value[5]);
        this.impactSpeed      = value[6] == "" ? null : Long.parseLong(value[6]);
        this.soundtrackName   = value[7] == "" ? null : value[7];
        this.minutesOfWaiting = value[8] == "" ? null : Integer.parseInt(value[8]);
        this.mood             = value[9] == "" ? null : Mood.parseMood(value[9]);
        this.car              = new Car();
        this.car.deserialize(new String[] { value[10] });
    }

    /**
     * Implements {@code getAttributesList} for {@code DataBaseObject}.
     *
     * @return list of class fields
     */
    public String[] getAttributesList() {
        return new String[] {
            "id",
            "name",
            "coordinates",
            "creationDate",
            "realHero",
            "hasToothpick",
            "impactSpeed",
            "soundtrackName",
            "minutesOfWaiting",
            "mood",
            "car"
        };
    }

    /**
     * Overrides {@code toString} of {@code Object}
     *
     * @return string representation of the object
     */
    @Override
    public String toString() {
        String result = "";

        result += "id: "               + this.id               + "\n";
        result += "name: "             + this.name             + "\n";
        result += "coordinates: "      + this.coordinates      + "\n";
        result += "creationDate: "     + this.creationDate     + "\n";
        result += "realHero: "         + this.realHero         + "\n";
        result += "hasToothpick: "     + this.hasToothpick     + "\n";
        result += "impactSpeed: "      + this.impactSpeed      + "\n";
        result += "soundtrackName: "   + this.soundtrackName   + "\n";
        result += "minutesOfWaiting: " + this.minutesOfWaiting + "\n";
        result += "mood: "             + this.mood             + "\n";
        result += "car: "              + this.car;

        return result;
    }

    /**
     * Implements {@code compareTo} for {@code Comparable}.
     * Compares objects by id:
     *  0 -- if equal;
     *  1 -- if this one is less than other;
     * -1 -- if other is greater then this.
     *
     * @param o object for comparison
     * @return position relative to other object
     */
    @Override
    public int compareTo(HumanBeing o) {
        int value = (int) (o.getId() - this.getId());

        return value == 0 ? 0 : value / value;
    }

}
