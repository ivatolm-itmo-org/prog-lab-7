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

import com.ivatolm.app.utils.SimpleParseException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class CSVDatabase<T extends ISerializable & IDatabaseObject> implements IDatabase<T> {

    private T dummyObject = null;
    private String filename;

    public CSVDatabase(String filename) {
        this.filename = filename;
    }

    @Override
    public void setDummyObject(T dummyObject) {
        this.dummyObject = dummyObject;
    }

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
            writer.writeNext(attributes);

            // Writing records
            for (T item : data) {
                String[] serializedItem = item.serialize();
                writer.writeNext(serializedItem);
            }

            writer.close();
        } catch (FileNotFoundException e) {
            System.err.println("Cannot open file.");
        } catch (IOException e) {
            System.err.println("Cannot read file.");
        }
    }

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
