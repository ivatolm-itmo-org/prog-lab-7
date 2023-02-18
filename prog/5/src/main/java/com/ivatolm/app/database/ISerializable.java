package com.ivatolm.app.database;

import com.ivatolm.app.utils.SimpleParseException;

public interface ISerializable {

    String[] serialize();

    void deserialize(String[] string) throws SimpleParseException;

}
