package com.ivatolm.app.models.coordinates;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import com.ivatolm.app.database.Serializable;
import com.ivatolm.app.models.Validatable;
import com.ivatolm.app.models.Validator;
import com.ivatolm.app.parser.arguments.ArgCheck;
import com.ivatolm.app.utils.SimpleParseException;

/**
 * Data structure for Coordinates described in the task.
 *
 * @author ivatolm
 */
public class Coordinates implements Serializable, Validatable {

    /** x field */
    @Validator(validator = CoordinatesXValidator.class)
    private Integer x;

    /** y field */
    @Validator(validator = CoordinatesYValidator.class)
    private Float y;

    /**
     * Constructs dummy-instance of the class.
     * Used to create dummy-instances of the class that will be instantly overriden.
     * Must not be used in typical case.
     */
    public Coordinates() {}

    /**
     * Constructs new instance from the passed agruments.
     * Extracts and casts provided arguments to target types.
     *
     * @param x validated {@code x} argument from the command line
     * @param y validated {@code y} argument from the command line
     */
    public Coordinates(Object x, Object y) {
        this.x = (Integer) x;
        this.y = (Float) y;
    }

    /**
     * Implements {@code serialize} for {@code Serializable}.
     * Serializes fields into {@code String} array.
     *
     * @return serialized object
     */
    @Override
    public String[] serialize() {
        return new String[] {
            "(" + (this.x == null ? null : this.x) + "," +
                  (this.y == null ? null : this.y) +  ")"
        };
    }

    /**
     * Implements {@code deserialize} for {@code Serializable}.
     * Casts input values to target types. Overrides internal values with new ones.
     *
     * @param string serialized object
     * @throws SimpleParseException if input is invalid
     */
    @Override
    public void deserialize(String[] string) throws SimpleParseException {
        String value = string[0];

        String internal = value.substring(1, value.length() - 1);
        String[] data = internal.split(",");

        if (data.length != 2) {
            throw new SimpleParseException(value + " must contain 2 values.");
        }

        this.x = data[0] == "" ? null : Integer.parseInt(data[0]);
        this.y = data[1] == "" ? null : Float.parseFloat(data[1]);
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
                        result = check.check(field.get(this) == null ? null : "" + field.get(this));
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
        return "(" + "x: " + this.x + ", " + "y: " + this.y + ")";
    }

    /**
     * @return 'x' field of the object
     */
    public Integer getX() {
        return this.x;
    }

    /**
     * @return 'y' field of the object
     */
    public Float getY() {
        return this.y;
    }

}
