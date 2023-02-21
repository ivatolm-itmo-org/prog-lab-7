package com.ivatolm.app.humanBeing;

import com.ivatolm.app.database.ISerializable;
import com.ivatolm.app.utils.SimpleParseException;

public class Car implements ISerializable {

    private String name;
    private boolean cool;

    public Car() {}

    public Car(Object name, Object cool) {
        this.name = (String) name;
        this.cool = (boolean) cool;
    }

    @Override
    public String[] serialize() {
        return new String[] { "(" + this.name + "," + this.cool +  ")" };
    }

    @Override
    public void deserialize(String[] string) throws SimpleParseException {
        String value = string[0];

        String internal = value.substring(1, value.length() - 1);
        String[] data = internal.split(",");

        if (data.length != 2) {
            throw new SimpleParseException(value + " must contain 2 values.");
        }

        this.name = data[0];
        this.cool = Boolean.parseBoolean(data[1]);
    }

}
