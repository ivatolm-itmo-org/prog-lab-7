package core.command;

import core.models.car.CarCoolValidator;
import core.models.car.CarNameValidator;
import core.models.coordinates.CoordinatesXValidator;
import core.models.coordinates.CoordinatesYValidator;
import core.models.humanBeing.HumanBeingHasToothpickValidator;
import core.models.humanBeing.HumanBeingIdValidator;
import core.models.humanBeing.HumanBeingImpactSpeedValidator;
import core.models.humanBeing.HumanBeingMinutesOfWaitingValidator;
import core.models.humanBeing.HumanBeingNameValidator;
import core.models.humanBeing.HumanBeingRealHeroValidator;
import core.models.humanBeing.HumanBeingSoundtrackNameValidator;
import core.models.mood.MoodValidator;

import core.command.arguments.Argument;
import core.command.arguments.BooleanArgument;
import core.command.arguments.FloatArgument;
import core.command.arguments.IntegerArgument;
import core.command.arguments.LongArgument;
import core.command.arguments.MoodArgument;
import core.command.arguments.StringArgument;

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
public enum CommandType {

    NOOP(
        new CommandInfo(
            "noop",
            "does nothing"
        ),
        null
    ),
    HELP(
        new CommandInfo(
            "help",
            "print information about avaliable commands"
        ),
        null
    ),
    INFO(
        new CommandInfo(
            "info",
            "print information about collection"
        ),
        null
    ),
    SHOW(
        new CommandInfo(
            "show",
            "print collection"
        ),
        null
    ),
    ADD(
        new CommandInfo(
            "add",
            "add new element to the collection"
        ),
        new Argument[] {
            new StringArgument(
                "name",
                new HumanBeingNameValidator(),
                "name (string, cannot be empty)",
                "This argument cannot be empty"),
            new IntegerArgument(
                "xCoordinate",
                new CoordinatesXValidator(),
                "x coordinate (integer, value greater than -58)",
                "This argument must be greater than -58"),
            new FloatArgument(
                "yCoordinate",
                new CoordinatesYValidator(),
                "y coordinate (float, value less or equal than 414)",
                "This argument must be less or equal than 414"),
            new BooleanArgument(
                "realHero",
                new HumanBeingRealHeroValidator(),
                "real hero (boolean, null)",
                "This argument must be boolean or null"),
            new BooleanArgument(
                "hasToothpick",
                new HumanBeingHasToothpickValidator(),
                "has toothpick (boolean)",
                "This argument must be boolean"),
            new LongArgument(
                "impactSpeed",
                new HumanBeingImpactSpeedValidator(),
                "impact speed (long integer)",
                "This argument must be long integer"),
            new StringArgument(
                "soundtrackName",
                new HumanBeingSoundtrackNameValidator(),
                "soundtrack name (string, cannot be empty)",
                "This argument cannot be empty"),
            new IntegerArgument(
                "minutesOfWaiting",
                new HumanBeingMinutesOfWaitingValidator(),
                "minutes of waiting (integer, null)",
                "This argument must be integer or null"),
            new MoodArgument(
                "mood",
                new MoodValidator(),
                "mood (Mood {'longin' - 0, 'gloom' - 1, 'apathy' - 2, 'rage' - 3})",
                "This argument must be Mood"),
            new StringArgument(
                "carName",
                new CarNameValidator(),
                "car name (string, cannot be empty)",
                "This argument cannot be empty"),
            new BooleanArgument(
                "carCool",
                new CarCoolValidator(),
                "coolness of the car (boolean, null)",
                "This argument must be boolean or null")
        }
    ),
    UPDATE(
        new CommandInfo(
            "update {id} {element}",
            "replace element with id of {id} for {element}"
        ),
        ((ArgsExtention) ((a) -> {
            Argument[] args = new Argument[1 + a.length];

            args[0] = new LongArgument(
                "id",
                new HumanBeingIdValidator(),
                "id (long integer)",
                "This argument must be long integer and exist on the server"
            );

            for (int i = 0; i < a.length; i++) {
                args[i + 1] = a[i];
            }

            return args;
        })).extend(ADD.getArgs())
    ),
    REMOVE_BY_ID(
        new CommandInfo(
            "remove_by_id {id}",
            "remove element with id of {id} from collection"
        ),
        new Argument[] {
            new LongArgument(
                "id",
                new HumanBeingIdValidator(),
                "id (long integer)",
           "This argument must be long integer"),
        }
    ),
    CLEAR(
        new CommandInfo(
            "clear",
            "remove all elements from collection"
        ),
        null
    ),
    SAVE(
        new CommandInfo(
            "save",
            "save collection to the storage"
        ),
        null
    ),
    EXECUTE_SCRIPT(
        new CommandInfo(
            "execute_script",
            "execute script from the file"
        ),
        new Argument[] {
            new StringArgument(
                "filename",
                (x) -> x != null,
                "filename (string, cannot be empty)",
                "This argument cannot be empty")
        }
    ),
    EXIT(
        new CommandInfo(
            "exit",
            "exit from the program (without saving)"
        ),
        null
    ),
    REMOVE_FIRST(
        new CommandInfo(
            "remove_first",
            "remove first element from the collection"
        ),
        null
    ),
    HEAD(
        new CommandInfo(
            "head",
            "print first element of the collection"
        ),
        null
    ),
    HISTORY(
        new CommandInfo(
            "history",
            "print last 12 commands without arguments"
        ),
        null
    ),
    COUNT_GREATER_THAN_MINUTES_OF_WAITING(
        new CommandInfo(
            "count_greater_than_minutes_of_waiting {minutesOfWaiting}",
            "print number of elements, which 'minutesOfWaiting' property is greater than {minutesOfWaiting}"
        ),
        new Argument[] {
            new IntegerArgument(
                "minutesOfWaiting",
                new HumanBeingMinutesOfWaitingValidator(),
                "minutes of waiting (integer, null)",
                "This argument must be integer or null"),
        }
    ),
    FILTER_STARTS_WITH_NAME(
        new CommandInfo(
            "filter_starts_with_name {name}",
            "print elemenets, which 'name' property starts with substring of {name}"
        ),
        new Argument[] {
            new StringArgument(
                "name",
                (x) -> x != null,
                "name (string, cannot be empty)",
                "This argument cannot be empty")
        }
    ),
    PRINT_FIELD_DESCENDING_MINUTES_OF_WAITING(
        new CommandInfo(
            "print_field_descending_minutes_of_waiting",
            "print elements sorted by 'minutesOfWaiting' property in the descending order"
        ),
        null
    ),
    LOGIN(
        new CommandInfo(
            "login",
            "login into account on the server"
        ),
        new Argument[] {
            new StringArgument(
                "username",
                (x) -> x != null && !x.isBlank(),
                "username (string, cannot be empty)",
                "This argument cannot be empty"),
            new StringArgument(
                "password",
                (x) -> x != null && !x.isBlank(),
                "password (string, cannot be empty)",
                "This argument cannot be empty"),
        }
    ),
    REGISTER(
        new CommandInfo(
            "register",
            "register a new account on the server"
        ),
        new Argument[] {
            new StringArgument(
                "username",
                (x) -> x != null && !x.isBlank(),
                "username (string, cannot be empty)",
                "This argument cannot be empty"),
            new StringArgument(
                "password",
                (x) -> x != null && !x.isBlank(),
                "password (string, cannot be empty)",
                "This argument cannot be empty"),
        }
    )
    ;

    /** Information about the command */
    private CommandInfo info;

    /** Arguments (validator, etc.) for the command */
    private Argument[] args;

    /**
     * Constructs new command with provided arguments.
     *
     * @param info information about the command
     * @param args arguments for the command
     */
    CommandType(CommandInfo info, Argument[] args) {
        this.info = info;
        this.args = args;

        if (args == null) {
            this.args = new Argument[] {};
        }
    }

    /**
     * @return {@code info} field of the object
     */
    public CommandInfo getInfo() {
        return this.info;
    }

    /**
     * @param index index of the argument
     * @return argument at position of {@code index} from {@code args}
     */
    public Argument getArgument(int index) {
        return this.args[index];
    }

    /**
     * @return number of arguments required for the command
     */
    public int getArgsCount() {
        return this.args.length;
    }

    /**
     * @return {@code args} field of the object
     */
    Argument[] getArgs() {
        return this.args;
    }

}
