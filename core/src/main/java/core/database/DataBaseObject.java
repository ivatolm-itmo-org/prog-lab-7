package core.database;

/**
 * Interface for interacting with database object.
 *
 * Note:
 * Object attributes are used to validate database tables and create new ones
 * for this object.
 *
 * @author ivatolm
 */
public interface DataBaseObject {

    /**
     * @return list of object attributes
     */
    String[] getAttributesList();

}
