package core.models.car;

import core.database.StrSerializable;
import core.models.Validatable;
import core.models.Validator;
import core.utils.SimpleParseException;
import lombok.Getter;
import lombok.Setter;

/**
 * Data structure for Car described in the task.
 *
 * @author ivatolm
 */
public class Car implements StrSerializable, Validatable {

    @Setter
    @Getter
    private Long id;

    /** Name field */
    @Setter
    @Getter
    @Validator(validator = CarNameValidator.class)
    private String name;

    /** Cool field */
    @Setter
    @Getter
    @Validator(validator = CarCoolValidator.class)
    private Boolean cool;

    /**
     * Constructs dummy-instance of the class.
     * Used to create dummy-instances of the class that will be instantly overriden.
     * Must not be used in typical case.
     */
    public Car() {}

    /**
     * Constructs new instance from the passed agruments.
     * Extracts and casts provided arguments to target types.
     *
     * @param name valid {@code name} argument from command line
     * @param cool valid {@code cool} argument from command line
     */
    public Car(Object name, Object cool) {
        this.name = (String) name;
        this.cool = (Boolean) cool;
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
            "(" + (this.name == null ? null : this.name) + "," +
                  (this.cool == null ? null : this.cool) +  ")"
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

        this.name = data[0] == "" ? null : data[0];
        this.cool = data[1] == "" ? null : Boolean.parseBoolean(data[1]);
    }

    /**
     * Overrides {@code toString} of {@code Object}
     *
     * @return string representation of the object
     */
    @Override
    public String toString() {
        return "(" + "name: " + this.name + ", " + "cool: " + this.cool + ")";
    }

}
