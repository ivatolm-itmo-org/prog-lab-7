package com.ivatolm.app.humanBeing;

import com.ivatolm.app.database.ISerializable;
import com.ivatolm.app.utils.NameNotFoundException;
import com.ivatolm.app.utils.SimpleParseException;

/**
 * Data structure for Mood described in the task
 *
 * @author ivatolm
 */
public enum Mood implements ISerializable {

    LONGING("longing"),
    GLOOM("gloom"),
    APATHY("apathy"),
    RAGE("rage")
    ;

    // String value of the enum
    private String value;

    /**
     * Constructs new instance from the string argument.
     *
     * @param value enum string representation
     */
    Mood(String value) {
        this.value = value;
    }

    /**
     * Parses {@code value} into {@code Mood}.
     *
     * @param value enum string representation
     * @return Enum with the provided name
     * @throws NameNotFoundException if there is no instance with such name
     */
    public static Mood parseMood(String value) throws NameNotFoundException {
        for (Mood mood : Mood.values()) {
            if (value.equalsIgnoreCase(mood.value)) {
                return mood;
            }
        }

        throw new NameNotFoundException("'" + value + "'" + " " + "cannot be converted into Mood.");
    }

    /**
     * Implements {@code serialize} for {@code ISerializable}.
     * Serializes fields into {@code String} array.
     *
     * @return serialized object
     */
    @Override
    public String[] serialize() {
        return new String[] { this.value };
    }

    /**
     * Implements {@code deserialize} for {@code ISerializable}.
     * Casts input value to target type. Overrides internal value with a new one.
     *
     * @param string serialized object
     * @throws SimpleParseException if input is invalid
     */
    @Override
    public void deserialize(String[] string) throws SimpleParseException {
        String value = string[0];

        try {
            this.value = Mood.parseMood(value).value;
        } catch(NameNotFoundException e) {
            throw new SimpleParseException("Cannot parse Mood from: " + value);
        }
    }

}
