package com.ivatolm.app.humanBeing;

import com.ivatolm.app.database.ISerializable;
import com.ivatolm.app.utils.NameNotFoundException;
import com.ivatolm.app.utils.SimpleParseException;

public enum Mood implements ISerializable {

    LONGING("longing"),
    GLOOM("gloom"),
    APATHY("apathy"),
    RAGE("rage")
    ;

    private String value;

    Mood(String value) {
        this.value = value;
    }

    public static Mood parseMood(String value) throws NameNotFoundException {
        for (Mood mood : Mood.values()) {
            if (value.equalsIgnoreCase(mood.value)) {
                return mood;
            }
        }

        throw new NameNotFoundException("'" + value + "'" + " " + "cannot be converted into Mood.");
    }

    @Override
    public String[] serialize() {
        return new String[] { this.value };
    }

    @Override
    public void deserialize(String[] string) throws SimpleParseException {
        String value = string[0];        

        try {
            this.value = Mood.parseMood(value).value;
        } catch(NameNotFoundException e) {
            throw new SimpleParseException("Cannot parse Mood from: " + value);
        }
    }

}
