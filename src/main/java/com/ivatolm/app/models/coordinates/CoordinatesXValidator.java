package com.ivatolm.app.models.coordinates;

import com.ivatolm.app.parser.arguments.ArgCheck;

/**
 * Validator for {@code x} field of the {@code Coordinates}.
 *
 * @author ivatolm
 */
public class CoordinatesXValidator implements ArgCheck {

    @Override
    public boolean check(String value) {
        int y;

        try {
            y = Integer.parseInt(value);
        } catch (Exception e) {
            return false;
        }

        return value != null && y > -58;
    }

}
