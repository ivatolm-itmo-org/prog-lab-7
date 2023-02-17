package com.ivatolm.app.humanBeing;

public enum Mood {
    LONGING("longing"),
    GLOOM("gloom"),
    APATHY("apathy"),
    RAGE("rage");

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
}
