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

interface ArgsExtention {

    Argument[] extend(Argument[] args);

}

public enum Command {

    HELP(
        new String[] {
            "help",
            "print information about avaliable commands"
        },
        null
    ),
    INFO(
        new String[] {
            "info",
            "print information about collection"
        },
        null
    ),
    SHOW(
        new String[] {
            "show",
            "print collection"
        },
        null
    ),
    ADD(
        new String[] {
            "add",
            "add new element to the collection"
        },
        new Argument[] {
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
                "This argument must be true or false (or null)")
        }
    ),
    UPDATE(
        new String[] {
            "update {id} {element}",
            "replace element with id of {id} for {element}"
        },
        ((ArgsExtention) ((a) -> {
            Argument[] args = new Argument[1 + a.length];

            args[0] = new LongArgument( // Id
                (x) -> {
                    try { Long.parseLong(x); } catch (Exception e) { return false; }
                    return x != null;
                },
                "id",
                "This argument must be long integer"
            );

            for (int i = 0; i < a.length; i++) {
                args[i + 1] = a[i];
            }

            return args;
        })).extend(ADD.getArgs())
    ),
    REMOVE_BY_ID(
        new String[] {
            "remove_by_id {id}",
            "remove element with id of {id} from collection"
        },
        new Argument[] {
            new LongArgument( // Id
                (x) -> {
                    try { Long.parseLong(x); } catch (Exception e) { return false; }
                    return x != null;
                },
                "id",
                "This argument must be long integer"),
        }
    ),
    CLEAR(
        new String[] {
            "clear",
            "remove all elements from collection"
        },
        null
    ),
    SAVE(
        new String[] {
            "save",
            "save collection to the storage"
        },
        null
    ),
    EXECUTE_SCRIPT(
        new String[] {
            "execute_script",
            "execute script from the file"
        },
        new Argument[] {
            new StringArgument( // Filename
                (x) -> x != null,
                "filename",
                "This argument cannot be empty")
        }
    )
    ;

    private String[] description;
    private Argument[] args;
    private LinkedList<Argument> argsValues;

    Command(String[] description, Argument[] args) {
        this.description = description;
        this.args = args;

        if (this.description == null || this.description.length != 2) {
            System.err.println("Command cannot be registred. Description must contain 2 values");
            System.exit(-1);
        }

        if (args == null) {
            this.args = new Argument[] {};
        }
    }

    public String[] getDescription() {
        return this.description;
    }

    public Argument getArgument(int index) {
        return this.args[index];
    }

    public LinkedList<Argument> getArgsValues() {
        return this.argsValues;
    }

    int getArgsCount() {
        return this.args.length;
    }

    Argument[] getArgs() {
        return this.args;
    }

    void setArgs(LinkedList<Argument> values) {
        this.argsValues = values;
    }

}
