package com.ivatolm.app.models.coordinates;

import java.lang.reflect.Field;
import java.util.LinkedList;

import com.ivatolm.app.database.Serializable;
import com.ivatolm.app.models.Validatable;
import com.ivatolm.app.models.Validator;
import com.ivatolm.app.models.ValidatorException;
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
        return new String[] { "(" + this.x + "," + this.y +  ")" };
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

        this.x = Integer.parseInt(data[0]);
        this.y = Float.parseFloat(data[1]);
    }

    @Override
    public boolean validate() {
        Class<?> clazz = this.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(Validator.class)) {
                Validator validator = field.getAnnotation(Validator.class);
                Class<? extends ArgCheck> validatorClass = validator.validator();
                ArgCheck a = (ArgCheck) validatorClass;

                try {
                    boolean result = check.check("" + field.get(clazz));

                    if (!result) {
                        return false;
                    }

                } catch (IllegalArgumentException | IllegalAccessException e) {
                    System.err.println(e);
                }
            }
        }

        return true;
    }

    /**
     * @return 'x' field of the object
     */
    public int getX() {
        return this.x;
    }

    /**
     * @return 'y' field of the object
     */
    public Float getY() {
        return this.y;
    }

}
