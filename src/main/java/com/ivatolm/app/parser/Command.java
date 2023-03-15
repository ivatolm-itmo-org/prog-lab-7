package com.ivatolm.app.parser;

import java.util.LinkedList;

import com.ivatolm.app.models.mood.Mood;
import com.ivatolm.app.parser.arguments.Argument;
import com.ivatolm.app.parser.arguments.BooleanArgument;
import com.ivatolm.app.parser.arguments.FloatArgument;
import com.ivatolm.app.parser.arguments.IntegerArgument;
import com.ivatolm.app.parser.arguments.LongArgument;
import com.ivatolm.app.parser.arguments.MoodArgument;
import com.ivatolm.app.parser.arguments.StringArgument;

/**
 * Functional interface for extending command arguments with arguments
 * of other command.
 *
 * @author ivatolm
 */
interface ArgsExtention {

    /**
     * Extends arguments of the command with arguments of other command.
     *
     * @param args arguments of other command
     * @return final command arguments
     */
    Argument[] extend(Argument[] args);

}

/**
 * This enum represents all avaliable commands.
 * Each command is constructed with validator and all messages required
 * for its utilization.
 *
 * @author ivatolm
 */
public enum Command {

    NOOP(
        new String[] {
            "noop",
            "does nothing"
        },
        null
    ),
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
            new StringArgument(
                "name",
                (x) -> x != null && !x.isEmpty(),
                "name",
                "This argument cannot be empty"),
            new IntegerArgument(
                "xCoordinate",
                (x) -> {
                    int y; try { y = Integer.parseInt(x); } catch (Exception e) { return false; }
                    return x != null && y > -58;
                },
                "x coordinate",
                "This argument must be greater than -58 (integer)"),
            new FloatArgument(
                "yCoordinate",
                (x) -> {
                    float y; try { y = Float.parseFloat(x); } catch (Exception e) { return false; }
                    return x != null && y <= 414;
                },
                "y coordinate",
                "This argument must be less or equal than 414 (float)"),
            new BooleanArgument(
                "realHero",
                (x) -> {
                    if (x == null) return true;
                    boolean y = ("true".equalsIgnoreCase(x) || "false".equalsIgnoreCase(x));
                    return y;
                },
                "real hero",
                "This argument must be true, false (or null)"),
            new BooleanArgument(
                "hasToothpick",
                (x) -> {
                    boolean y = ("true".equalsIgnoreCase(x) || "false".equalsIgnoreCase(x));
                    return x != null && y;
                },
                "has toothpick",
                "This argument must be true or false"),
            new LongArgument(
                "impactSpeed",
                (x) -> {
                    try { Long.parseLong(x); } catch (Exception e) { return false; }
                    return x != null;
                },
                "impact speed",
                "This argument must be long integer"),
            new StringArgument(
                "soundtrackName",
                (x) -> x != null,
                "soundtrack name",
                "This argument cannot be empty"),
            new IntegerArgument(
                "minutesOfWaiting",
                (x) -> {
                    if (x == null) return true;
                    try { Integer.parseInt(x); } catch (Exception e) { return false; }
                    return true;
                },
                "minutes of waiting",
                "This argument must be integer (or null)"),
            new MoodArgument(
                "mood",
                (x) -> {
                    try { Mood.parseMood(x); } catch (Exception e) { return false; }
                    return x != null;
                },
                "mood",
                "This argument must be equal to 'LONGING', 'GLOOM', 'APATHY' or 'RAGE'"),
            new StringArgument(
                "carName",
                (x) -> x != null,
                "car name",
                "This argument cannot be empty"),
            new BooleanArgument(
                "carCool",
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

            args[0] = new LongArgument(
                "id",
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
            new LongArgument(
                "id",
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
            new StringArgument(
                "filename",
                (x) -> x != null,
                "filename",
                "This argument cannot be empty")
        }
    ),
    EXIT(
        new String[] {
            "exit",
            "exit from the program (without saving)"
        },
        null
    ),
    REMOVE_FIRST(
        new String[] {
            "remove_first",
            "remove first element from the collection"
        },
        null
    ),
    HEAD(
        new String[] {
            "head",
            "print first element of the collection"
        },
        null
    ),
    HISTORY(
        new String[] {
            "history",
            "print last 12 commands without arguments"
        },
        null
    ),
    COUNT_GREATER_THAN_MINUTES_OF_WAITING(
        new String[] {
            "count_greater_than_minutes_of_waiting {minutesOfWaiting}",
            "print number of elements, which 'minutesOfWaiting' property is greater than {minutesOfWaiting}"
        },
        new Argument[] {
            new IntegerArgument(
                "minutesOfWaiting",
                (x) -> {
                    if (x == null) return true;
                    try { Integer.parseInt(x); } catch (Exception e) { return false; }
                    return true;
                },
                "minutes of waiting",
                "This argument must be integer (or null)"),
        }
    ),
    FILTER_STARTS_WITH_NAME(
        new String[] {
            "filter_starts_with_name {name}",
            "print elemenets, which 'name' property starts with substring of {name}"
        },
        new Argument[] {
            new StringArgument(
                "name",
                (x) -> x != null,
                "name",
                "This argument cannot be empty")
        }
    ),
    PRINT_FIELD_DESCENDING_MINUTES_OF_WAITING(
        new String[] {
            "print_field_descending_minutes_of_waiting",
            "print elements sorted by 'minutesOfWaiting' property in the descending order"
        },
        null
    )
    ;

    /** Description of the command */
    private String[] description;

    /** Arguments (validator, etc.) for the command */
    private Argument[] args;

    /** Argument values for the command */
    private LinkedList<Argument> argsValues;

    /**
     * Constructs new command with provided arguments.
     *
     * @param description description of the command
     * @param args arguments for the command
     */
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

    /**
     * @return {@code description} field of the object
     */
    public String[] getDescription() {
        return this.description;
    }

    /**
     * @param index index of the argument
     * @return argument at position of {@code index} from {@code args}
     */
    public Argument getArgument(int index) {
        return this.args[index];
    }

    /**
     * @return {@code argsValues} field of the object
     */
    public LinkedList<Argument> getArgsValues() {
        return this.argsValues;
    }

    /**
     * @return number of arguments required for the command
     */
    int getArgsCount() {
        return this.args.length;
    }

    /**
     * @return {@code args} field of the object
     */
    Argument[] getArgs() {
        return this.args;
    }

    /**
     * Set values of the arguments.
     * Overrides argument values with new values.
     *
     * @param values new values for arguments
     */
    void setArgs(LinkedList<Argument> values) {
        this.argsValues = values;
    }

}
