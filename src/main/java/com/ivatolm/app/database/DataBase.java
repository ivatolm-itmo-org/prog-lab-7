package com.ivatolm.app.database;

import java.util.LinkedList;

/**
 * Interface for interacting with a file (database).
 *
 * Note:
 * Scalability of the interface is limited without generic T. While reading from the
 * file its necessary to create new instances of T. Because of type erasure it's
 * impossible to create instance of class T at runtime. Dummy object is a
 * workaround for that.
 *
 * @author ivatolm
 */
public interface DataBase<T extends Serializable & DataBaseObject> {

    /**
     * Register dummy instance of class T.
     *
     * @param dummyObject dummy instance of class T
     */
    void setDummyObject(T dummyObject);

    /**
     * Writes {@code data} to {@code filename}.
     *
     * @param data to write
     */
    void write(LinkedList<T> data);

    /**
     * Reads data from {@code filename}.
     *
     * @return list of read objects of class T or null if error occured
     */
    LinkedList<T> read();

}
