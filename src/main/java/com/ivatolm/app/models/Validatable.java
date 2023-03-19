package com.ivatolm.app.models;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import com.ivatolm.app.database.Serializable;
import com.ivatolm.app.parser.arguments.ArgCheck;

/**
 * Interface for validating objects.
 *
 * @author ivatolm
 */
public interface Validatable {

    /**
     * Check object for validity.
     *
     * @return true if object is valid, else false
     */
    static boolean validate(Object obj) {
        Class<?> clazz = obj.getClass();

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
                    if (field.get(obj) instanceof Serializable) {
                        Serializable f = (Serializable) field.get(obj);
                        result = check.check(f.serialize()[0]);
                    } else {
                        result = check.check(field.get(obj) == null ? null : "" + field.get(obj));
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
    };

}
