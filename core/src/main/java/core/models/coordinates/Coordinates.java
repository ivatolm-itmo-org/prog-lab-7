package core.models.coordinates;

import core.database.StrSerializable;
import core.models.Validatable;
import core.models.Validator;
import core.utils.SimpleParseException;
import lombok.Getter;
import lombok.Setter;

/**
 * Data structure for Coordinates described in the task.
 *
 * @author ivatolm
 */
public class Coordinates implements StrSerializable, Validatable {

    /** id field */
    @Setter
    @Getter
    private Long id;

    /** x field */
    @Setter
    @Getter
    @Validator(validator = CoordinatesXValidator.class)
    private Integer x;

    /** y field */
    @Setter
    @Getter
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
     * Implements {@code serialize} for {@code StrSerializable}.
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
     * Implements {@code deserialize} for {@code StrSerializable}.
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
     * Overrides {@code toString} of {@code Object}
     *
     * @return string representation of the object
     */
    @Override
    public String toString() {
        return "(" + "x: " + this.x + ", " + "y: " + this.y + ")";
    }

}
