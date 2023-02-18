package com.ivatolm.app.parser;

import java.util.LinkedList;

import com.ivatolm.app.humanBeing.Mood;
import com.ivatolm.app.parser.arguments.Argument;
import com.ivatolm.app.parser.arguments.BooleanArgument;
import com.ivatolm.app.parser.arguments.FloatArgument;
import com.ivatolm.app.parser.arguments.IntegerArgument;
import com.ivatolm.app.parser.arguments.LongArgument;
import com.ivatolm.app.parser.arguments.MoodArgument;
import com.ivatolm.app.parser.arguments.StringArgument;

public enum Command {

    UNKNOWN("", null),
    HELP("help", null),
    INFO("info", null),
    SHOW("show", null),
    ADD("add", new Argument[] {
        new StringArgument( // Name
            (x) -> x != null && !x.isEmpty(),
            "name",
            "This argument cannot be empty"),
        new IntegerArgument( // X coordinate
            (x) -> {
                int y; try { y = Integer.parseInt(x); } catch (Exception e) { return false; }
                return x != null && y > -58; 
            },
            "x coordinate",
            "This argument must be greater than -58 (integer)"),
        new FloatArgument( // Y coordinate
            (x) -> {
                float y; try { y = Float.parseFloat(x); } catch (Exception e) { return false; }
                return x != null && y <= 414;
            },
            "y coordinate",
            "This argument must be less or equal than 414 (float)"),
        new BooleanArgument( // Real hero
            (x) -> {
                if (x == null) return true;
                boolean y = ("true".equalsIgnoreCase(x) || "false".equalsIgnoreCase(x));
                return y;
            },
            "real hero",
            "This argument must be true, false (or null)"),
        new BooleanArgument( // Has toothpick
            (x) -> {
                boolean y = ("true".equalsIgnoreCase(x) || "false".equalsIgnoreCase(x));
                return x != null && y;
            },
            "has toothpick",
            "This argument must be true or false"),
        new LongArgument( // Impact speed
            (x) -> {
                try { Long.parseLong(x); } catch (Exception e) { return false; }
                return x != null;
            },
            "impact speed",
            "This argument must be long integer"),
        new StringArgument( // Soundtrack name
            (x) -> x != null,
            "soundtrack name",
            "This argument cannot be empty"),
        new IntegerArgument( // Minutes of waiting
            (x) -> {
                if (x == null) return true;
                try { Integer.parseInt(x); } catch (Exception e) { return false; }
                return true;
            },
            "minutes of waiting",
            "This argument must be integer (or null)"),
        new MoodArgument( // Mood
            (x) -> {
                try { Mood.parseMood(x); } catch (Exception e) { return false; }
                return x != null;
            },
            "mood",
            "This argument must be equal to 'LONGING', 'GLOOM', 'APATHY' or 'RAGE'"),
        new StringArgument( // Car name
            (x) -> x != null,
            "car name",
            "This argument cannot be empty"),
        new BooleanArgument( // Car cool
            (x) -> {
                if (x == null) return true;
                boolean y = ("true".equalsIgnoreCase(x) || "false".equalsIgnoreCase(x));
                return y;
            },
            "coolness of the car",
            "This argument must be true or false (or null)"),
    }),
    SAVE("save", null),
    ;

    private String name;
    private Argument[] args;
    private LinkedList<Argument> argsValues;

    Command(String name, Argument[] args) {
        this.name = name;
        this.args = args;

        if (args == null)
            this.args = new Argument[] {};
    }

    public Argument getArgument(int index) {
        return this.args[index];
    }

    public String getName() {
        return this.name;
    }

    public LinkedList<Argument> getArgsValues() {
        return this.argsValues;
    }

    int getArgsCount() {
        return this.args.length;
    }

    void setArgs(LinkedList<Argument> values) {
        this.argsValues = values;
    }

}
