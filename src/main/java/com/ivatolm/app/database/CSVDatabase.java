package com.ivatolm.app.database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import com.ivatolm.app.parser.SimpleParseException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * Class for interacting with a CSV file (database).
 *
 * @author ivatolm
 */
public class CSVDatabase<T extends Serializable & DataBaseObject> implements DataBase<T> {

    /**
     * Read note in {@code DataBase} interface for explanation.
     */
    private T dummyObject = null;

    /** filename field */
    private String filename;

    /**
     * Constructs new instance for working with {@code filename}.
     *
     * @param filename database filename
     */
    public CSVDatabase(String filename) {
        this.filename = filename;
    }

    /**
     * Implements {@code setDummyObject} for {@code DataBase}.
     *
     * @param dummyObject dummy instance of class T
     */
    @Override
    public void setDummyObject(T dummyObject) {
        this.dummyObject = dummyObject;
    }

    /**
     * Implements {@code write} for {@code DataBase}.
     * Serializes data via {@code Serializable} interface and writes it to file.
     * Prints error if such occures, but does not throw exception.
     *
     * TODO: throw exception if failure occures.
     *
     * @param data data to write
     */
    @Override
    public void write(LinkedList<T> data) {
        if (this.dummyObject == null) {
            System.err.println("Dummy object wasn't initialized.");
            return;
        }

        try {
            FileOutputStream fstream = new FileOutputStream(this.filename);
            OutputStreamWriter istream = new OutputStreamWriter(fstream);
            CSVWriter writer = new CSVWriter(istream);

            // Writing attributes
            String[] attributes = this.dummyObject.getAttributesList();
            writer.writeNext(attributes, false);

            // Writing records
            for (T item : data) {
                String[] serializedItem = item.serialize();
                writer.writeNext(serializedItem, false);
            }

            writer.close();
        } catch (FileNotFoundException e) {
            System.err.println("Cannot open file.");
        } catch (IOException e) {
            System.err.println("Cannot read file.");
        }
    }

    /**
     * Implements {@code read} for {@code DataBase}.
     * Uses reflection to create instances of class T via {@code dummyObject}.
     * Deserializes read data into them via {@code Serializable} interface.
     * Prints error if such occures, but does not throw exception.
     *
     * TODO: throw exception if failure occures.
     *
     * @return list of read objects of class T or null if error occured
     */
    @Override
    public LinkedList<T> read() {
        if (this.dummyObject == null) {
            System.err.println("Dummy object wasn't initialized.");
            return null;
        }

        try {
            FileInputStream fstream = new FileInputStream(filename);
            InputStreamReader istream = new InputStreamReader(fstream);
            CSVReader reader = new CSVReader(istream);

            // Verifying attributes
            String[] attributes = this.dummyObject.getAttributesList();
            String[] readAttributes = reader.readNext();
            for (int i = 0; i < Math.min(attributes.length, readAttributes.length); i++) {
                if (!attributes[i].equals(readAttributes[i])) {
                    System.err.println("Attributes doesn't match: " +
                                       "'" + attributes[i] + "'" +
                                       " != "
                                       + "'" + readAttributes[i] + "'" + ".");
                    reader.close();
                    return null;
                }
            }

            // Using reflection to get constructors of database object
            Constructor<?> constructor = null;
            try {
                Constructor<?>[] constructors = this.dummyObject.getClass().getConstructors();
                for (Constructor<?> c : constructors) {
                    if (c.getParameterTypes().length == 0) {
                        constructor = c;
                        break;
                    }
                }
            } catch (SecurityException e) {
                System.err.println("Cannot create new instance of database object.");
                reader.close();
                return null;
            }

            LinkedList<T> result = new LinkedList<>();

            // Reading records
            try {
                String[] record;
                while ((record = reader.readNext()) != null) {
                    // Using reflection to create new instace of database object
                    @SuppressWarnings("unchecked")
                    T t = (T) constructor.newInstance();
                    t.deserialize(record);

                    result.add(t);
                }
            } catch (InstantiationException | IllegalAccessException |
                     IllegalArgumentException | InvocationTargetException e) {
                System.err.println("Cannot create an instance of database object.");
                reader.close();
                return null;
            } catch (SimpleParseException e) {
                System.err.println("Cannot parse record.");
                reader.close();
                return null;
            }

            reader.close();

            return result;
        } catch (FileNotFoundException e) {
            System.err.println("Cannot open file.");
        } catch (IOException e) {
            System.err.println("Cannot read file.");
        }

        return null;
    }

}
