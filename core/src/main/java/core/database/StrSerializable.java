package core.database;

import core.utils.SimpleParseException;

/**
 * Interface for serializing and deserializing database objects.
 *
 * @author ivatolm
 */
public interface StrSerializable {

    /**
     * Serialize internal field into {@code String} array.
     *
     * @return serialized object
     */
    String[] serialize();

    /**
     * Deserializes data from {@code string} and override internal values.
     *
     * @param string serialized object
     * @throws SimpleParseException if error occures
     */
    void deserialize(String[] string) throws SimpleParseException;

}
