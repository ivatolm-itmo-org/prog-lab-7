package com.ivatolm.app.humanBeing;

import java.time.LocalDate;
import java.util.LinkedList;

import com.ivatolm.app.database.IDatabaseObject;
import com.ivatolm.app.database.ISerializable;
import com.ivatolm.app.utils.SimpleParseException;

/**
 * Data structure for HumanBeing described in the task.
 *
 * @author ivatolm
 */
public class HumanBeing implements ISerializable, IDatabaseObject, Comparable<HumanBeing> {

    /** Id field */
    private Long id;

    /** Name field */
    private String name;

    /** Coordinates field */
    private Coordinates coordinates;

    /** Creation date field */
    private LocalDate creationDate;

    /** Real hero field */
    private boolean realHero;

    /** Has toothpick field */
    private Boolean hasToothpick;

    /** Impact speed field */
    private Long impactSpeed;

    /** Soundtrack name field */
    private String soundtrackName;

    /** Minutes of waiting field */
    private int minutesOfWaiting;

    /** Mood field */
    private Mood mood;

    /** Car field */
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
     * Implements {@code serialize} for {@code ISerializable}.
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
     * Implements {@code deserialize} for {@code ISerializable}.
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
     * Implements {@code getAttributesList} for {@code IDatabaseObject}.
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
