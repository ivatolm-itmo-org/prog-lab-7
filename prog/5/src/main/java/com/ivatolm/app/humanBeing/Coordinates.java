package com.ivatolm.app.humanBeing;

import com.ivatolm.app.database.ISerializable;
import com.ivatolm.app.utils.SimpleParseException;

public class Coordinates implements ISerializable {

    private Integer x;
    private Float y;

    public Coordinates() {}

    public Coordinates(Object x, Object y) {
        this.x = (Integer) x;
        this.y = (Float) y;
    }

    @Override
    public String[] serialize() {
        return new String[] { "(" + this.x + "," + this.y +  ")" };
    }

    @Override
    public void deserialize(String[] string) throws SimpleParseException {
        String value = string[0];

        String internal = value.substring(1, value.length() - 1);
        String[] data = internal.split(",");

        if (data.length != 2) {
            throw new SimpleParseException(value + " must contain 2 values.");
        }

        this.x = Integer.parseInt(data[0]);
        this.y = Float.parseFloat(data[1]);
    }

}
