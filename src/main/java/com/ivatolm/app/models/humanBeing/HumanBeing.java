package com.ivatolm.app.models.humanBeing;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.LinkedList;

import com.ivatolm.app.database.DataBaseObject;
import com.ivatolm.app.database.Serializable;
import com.ivatolm.app.models.Validatable;
import com.ivatolm.app.models.Validator;
import com.ivatolm.app.models.car.Car;
import com.ivatolm.app.models.car.CarValidator;
import com.ivatolm.app.models.coordinates.Coordinates;
import com.ivatolm.app.models.coordinates.CoordinatesValidator;
import com.ivatolm.app.models.mood.Mood;
import com.ivatolm.app.models.mood.MoodValidator;
import com.ivatolm.app.parser.arguments.ArgCheck;
import com.ivatolm.app.utils.SimpleParseException;

/**
 * Data structure for HumanBeing described in the task.
 *
 * @author ivatolm
 */
public class HumanBeing implements Serializable, DataBaseObject, Comparable<HumanBeing>, Validatable {

    /** Id field */
    private Long id;

    /** Name field */
    @Validator(validator = HumanBeingNameValidator.class)
    private String name;

    /** Coordinates field */
    @Validator(validator = CoordinatesValidator.class)
    private Coordinates coordinates;

    /** Creation date field */
    private LocalDate creationDate;

    /** Real hero field */
    @Validator(validator = HumanBeingRealHeroValidator.class)
    private boolean realHero;

    /** Has toothpick field */
    @Validator(validator = HumanBeingHasToothpickValidator.class)
    private Boolean hasToothpick;

    /** Impact speed field */
    @Validator(validator = HumanBeingImpactSpeedValidator.class)
    private Long impactSpeed;

    /** Soundtrack name field */
    @Validator(validator = HumanBeingSoundtrackNameValidator.class)
    private String soundtrackName;

    /** Minutes of waiting field */
    @Validator(validator = HumanBeingMinutesOfWaitingValidator.class)
    private int minutesOfWaiting;

    /** Mood field */
    @Validator(validator = MoodValidator.class)
    private Mood mood;

    /** Car field */
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
        this.creationDate     = (LocalDate) args.get(3);
        this.realHero         = (boolean) args.get(4);
        this.hasToothpick     = (Boolean) args.get(5);
        this.impactSpeed      = (Long) args.get(6);
        this.soundtrackName   = (String) args.get(7);
        this.minutesOfWaiting = (int) args.get(8);
        this.mood             = (Mood) args.get(9);
        this.car              = (Car) args.get(10);
    }

    /**
     * Implements {@code serialize} for {@code Serializable}.
     * Sequentially serializes fiels into {@code String} array. If field is
     * complex serializes it first.
     *
     * @return serialized object
     */
    @Override
    public String[] serialize() {
        return new String[] {
            "" + this.id,
            this.name,
            this.coordinates.serialize()[0],
            "" + this.creationDate,
            "" + this.realHero,
            "" + this.hasToothpick,
            "" + this.impactSpeed,
            this.soundtrackName,
            "" + this.minutesOfWaiting,
            this.mood.serialize()[0],
            this.car.serialize()[0]
        };
    }

    /**
     * Implements {@code deserialize} for {@code Serializable}.
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

        this.id               = Long.parseLong(value[0]);
        this.name             = value[1];
        this.coordinates      = new Coordinates();
        this.coordinates.deserialize(new String[] { value[2] });
        this.creationDate     = java.time.LocalDate.parse(value[3]);
        this.realHero         = Boolean.parseBoolean(value[4]);
        this.hasToothpick     = Boolean.parseBoolean(value[5]);
        this.impactSpeed      = Long.parseLong(value[6]);
        this.soundtrackName   = value[7];
        this.minutesOfWaiting = Integer.parseUnsignedInt(value[8]);
        this.mood             = Mood.parseMood(value[9]);
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
     * Implements {@code validate} for {@code Validatable}.
     * Checks each field value for being valid via provided {@code Validator}.
     *
     * @return true if whole object is valid, else false
     */
    @Override
    public boolean validate() {
        Class<?> clazz = this.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            // Checking if field has an annotaion
            if (field.isAnnotationPresent(Validator.class)) {
                // Getting annotation from the field
                Validator validator = field.getAnnotation(Validator.class);

                // Extracting validator class from the annotation
                Class<? extends ArgCheck> validatorClass = validator.validator();

                // Instantinating validator
                try {
                    ArgCheck check = validatorClass.getDeclaredConstructor().newInstance();

                    // Checking field value
                    boolean result;
                    if (field.get(this) instanceof Serializable) {
                        Serializable f = (Serializable) field.get(this);
                        result = check.check(f.serialize()[0]);
                    } else {
                        result = check.check("" + field.get(this));
                    }

                    if (!result) {
                        return false;
                    }

                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    System.err.println(e);
                    return false;
                }
            }
        }

        return true;
    }


    /**
     * Overrides {@code toString} of {@code Object}
     *
     * @return string representation of the object
     */
    @Override
    public String toString() {
        String result = "";

        String[] serialized = this.serialize();
        String[] attributes = this.getAttributesList();

        int min = Math.min(serialized.length, attributes.length);
        for (int i = 0; i < min; i++) {
            result += attributes[i] + ": " + serialized[i];
            if (i < min - 1) {
                result += "\n";
            }
        }

        return result;
    }

    /**
     * @return 'id' field of the object
     */
    public Long getId() {
        return id;
    }

    /**
     * @return {@code name} field of the object
     */
    public String getName() {
        return name;
    }

    /**
     * @return {@code coordinates} field of the object
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * @return {@code creationDate} field of the object
     */
    public LocalDate getCreationDate() {
        return creationDate;
    }

    /**
     * @return {@code realHero} field of the object
     */
    public boolean isRealHero() {
        return realHero;
    }

    /**
     * @return {@code hasToothpick} field of the object
     */
    public Boolean getHasToothpick() {
        return hasToothpick;
    }

    /**
     * @return {@code impactSpeed} field of the object
     */
    public Long getImpactSpeed() {
        return impactSpeed;
    }

    /**
     * @return {@code soundtrackName} field of the object
     */
    public String getSoundtrackName() {
        return soundtrackName;
    }

    /**
     * @return {@code minutesOfWaiting} field of the object
     */
    public int getMinutesOfWaiting() {
        return minutesOfWaiting;
    }

    /**
     * @return {@code mood} field of the object
     */
    public Mood getMood() {
        return mood;
    }

    /**
     * @return {@code car} field of the object
     */
    public Car getCar() {
        return car;
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
