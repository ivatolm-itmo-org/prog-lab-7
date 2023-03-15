package com.ivatolm.app.models.coordinates;

import com.ivatolm.app.parser.arguments.ArgCheck;

/**
 * Validator for {@code y} field of the {@code Coordinates}.
 *
 * @author ivatolm
 */
public class CoordinatesYValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        float y;

        try {
            y = Float.parseFloat(value);
        } catch (Exception e) {
            return false;
        }

        return value != null && y <= 414;
    }

}
