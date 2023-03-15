package com.ivatolm.app.models.mood;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import com.ivatolm.app.database.Serializable;
import com.ivatolm.app.models.Validatable;
import com.ivatolm.app.models.Validator;
import com.ivatolm.app.parser.arguments.ArgCheck;
import com.ivatolm.app.utils.NameNotFoundException;
import com.ivatolm.app.utils.SimpleParseException;

/**
 * Data structure for Mood described in the task.
 *
 * @author ivatolm
 */
public enum Mood implements Serializable, Validatable {

    LONGING("longing"),
    GLOOM("gloom"),
    APATHY("apathy"),
    RAGE("rage")
    ;

    // String value of the enum
    @Validator(validator = MoodValueValidator.class)
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
     * Implements {@code serialize} for {@code Serializable}.
     * Serializes fields into {@code String} array.
     *
     * @return serialized object
     */
    @Override
    public String[] serialize() {
        return new String[] { this.value };
    }

    /**
     * Implements {@code deserialize} for {@code Serializable}.
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
                    boolean result = check.check("" + field.get(this));
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

}
