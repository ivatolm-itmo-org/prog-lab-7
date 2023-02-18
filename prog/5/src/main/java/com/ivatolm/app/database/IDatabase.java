package com.ivatolm.app.database;

import java.util.LinkedList;

public interface IDatabase<T extends ISerializable & IDatabaseObject> {

    void setDummyObject(T dummyObject);

    void write(LinkedList<T> data);

    LinkedList<T> read();

}
