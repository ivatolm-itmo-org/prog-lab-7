package com.ivatolm.app.humanBeing;

import java.time.LocalDate;
import java.util.LinkedList;

import com.ivatolm.app.database.IDatabaseObject;
import com.ivatolm.app.database.ISerializable;
import com.ivatolm.app.utils.SimpleParseException;

public class HumanBeing implements ISerializable, IDatabaseObject {

    private Long id;
    private String name;
    private Coordinates coordinates;
    private LocalDate creationDate;
    private boolean realHero;
    private Boolean hasToothpick;
    private Long impactSpeed;
    private String soundtrackName;
    private int minutesOfWaiting;
    private Mood mood;
    private Car car;

    public HumanBeing() {}

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

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public boolean isRealHero() {
        return realHero;
    }

    public Boolean getHasToothpick() {
        return hasToothpick;
    }

    public Long getImpactSpeed() {
        return impactSpeed;
    }

    public String getSoundtrackName() {
        return soundtrackName;
    }

    public int getMinutesOfWaiting() {
        return minutesOfWaiting;
    }

    public Mood getMood() {
        return mood;
    }

    public Car getCar() {
        return car;
    }

}
