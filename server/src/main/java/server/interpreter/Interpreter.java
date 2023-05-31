package server.interpreter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import core.command.Command;
import core.command.CommandInfo;
import core.command.CommandType;
import core.command.arguments.Argument;
import core.database.DataBase;
import core.models.IdValidator;
import core.models.Validatable;
import core.models.car.Car;
import core.models.coordinates.Coordinates;
import core.models.humanBeing.HumanBeing;
import core.models.mood.Mood;
import server.database.HibernateUtil;

/**
 * Standalone class for interpreting commands.
 *
 * @author ivatolm
 */
public class Interpreter {

    /** Link to database */
    private DataBase<HumanBeing> database;

    /** Collection of objects (described in the task) */
    static private LinkedList<HumanBeing> collection;

    /** Was database read or it was created? */
    private final boolean wasRead;

    /** History of interpreted commands */
    private LinkedList<Command> history;

    /** Output produces by the command */
    private String commandOutput;

    /** Result of the command */
    private String commandResult;

    /** Running flag */
    private Boolean isRunning = true;

    /** Id validator */
    private IdValidator idValidator;

    /** Id validator dummy */
    private IdValidator IdValidatorDummy;

    /**
     * Constructs instance of the class.
     *
     * @param database link to database
     */
    public Interpreter(DataBase<HumanBeing> database) {
        this.database = database;
        this.database.setDummyObject(new HumanBeing());

        LinkedList<HumanBeing> data = this.database.read();
        if (data != null) {
            Interpreter.collection = data;
            this.wasRead = true;
        } else {
            Interpreter.collection = new LinkedList<>();
            this.wasRead = false;
        }

        this.history = new LinkedList<>();

        this.idValidator = (Argument arg) -> {
            return Interpreter.HasItemWithId(arg);
        };

        this.IdValidatorDummy = (Argument arg) -> {
            return true;
        };
    }

    /**
     * Executes {@code cmd}.
     * Returns list of commands to execute later. It's required for execution not
     * to happen outside of interpreter (otherwise some commands have to run other
     * commands inside themselves).
     *
     * @param cmd command for interpretation
     * @return list of commands for later interpretation or null
     */
    public String[] exec(Command cmd) {
        LinkedList<Argument> args = cmd.getArgsValues();

        if (this.history.size() > 12) {
            this.history.removeFirst();
        }

        this.history.add(cmd);

        switch (cmd.getType()) {
            case NOOP:
                return this.noop(args);

            case HELP:
                return this.help(args);

            case INFO:
                return this.info(args);

            case SHOW:
                return this.show(args);

            case ADD:
                return this.add(args);

            case UPDATE:
                return this.update(args);

            case REMOVE_BY_ID:
                return this.removeById(args);

            case CLEAR:
                return this.clear(args);

            case SAVE:
                return this.save(args);

            case EXECUTE_SCRIPT:
                return this.executeScript(args);

            case EXIT:
                return this.exit(args);

            case REMOVE_FIRST:
                return this.removeFirst(args);

            case HEAD:
                return this.head(args);

            case HISTORY:
                return this.history(args);

            case COUNT_GREATER_THAN_MINUTES_OF_WAITING:
                return this.countGreaterThanMinutesOfWaiting(args);

            case FILTER_STARTS_WITH_NAME:
                return this.filterStartsWithName(args);

            case PRINT_FIELD_DESCENDING_MINUTES_OF_WAITING:
                return this.printFieldDescendingMinutesOfWaiting(args);

            case LOGIN:
                return this.login(args);

            case REGISTER:
                return this.register(args);

            default:
                System.err.println("Unknown command.");
        }

        return new String[] {};
    }

    /**
     * NOOP command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] noop(LinkedList<Argument> args) {
        return null;
    }

    /**
     * HELP command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] help(LinkedList<Argument> args) {
        String result = "";

        // Finding length of the longest name-description
        final int gap = 4;
        int length = 0;

        for (CommandType c : CommandType.values()) {
            CommandInfo info = c.getInfo();
            int nameDescriptionLength = info.getName().length();
            if (length < nameDescriptionLength) {
                length = nameDescriptionLength;
            }
        }

        for (CommandType c : CommandType.values()) {
            CommandInfo info = c.getInfo();
            result += info.getName();

            // Adding whitespaces
            for (int i = 0; i < length - info.getName().length() + gap; i++) {
                result += " ";
            }

            result += info.getDescription();
            result += "\n";
        }

        this.commandOutput = result;

        return null;
    }

    /**
     * INFO command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] info(LinkedList<Argument> args) {
        String result = "";
        result += "Type: " + Interpreter.collection.getClass() + "\n";
        result += "Creation date: " + (this.wasRead ? "unknown" : "recently") + "\n";
        result += "Size: " + Interpreter.collection.size() + "\n";

        this.commandOutput = result;

        return null;
    }

    /**
     * SHOW command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] show(LinkedList<Argument> args) {
        String result = "";

        result = Interpreter.collection.stream()
            .map(hb -> hb.toString() + '\n' + '\n')
            .collect(Collectors.joining());

        // for (HumanBeing hb : Interpreter.collection) {
        //     result += hb.toString() + '\n' + '\n';
        // }

        this.commandOutput = result;

        return null;
    }

    /**
     * ADD command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] add(LinkedList<Argument> args) {
        Coordinates coordinates = new Coordinates();
        coordinates.setX((Integer) args.get(1).getValue());
        coordinates.setY((Float) args.get(2).getValue());

        Car car = new Car();
        car.setName((String) args.get(9).getValue());
        car.setCool((Boolean) args.get(10).getValue());

        HumanBeing instance = new HumanBeing();
        instance.setId(-1L); // Will be replaced by db
        instance.setName((String) args.get(0).getValue());
        instance.setCoordinates(coordinates);
        instance.setCreationDate(System.currentTimeMillis());
        instance.setRealHero((Boolean) args.get(3).getValue());
        instance.setHasToothpick((Boolean) args.get(4).getValue());
        instance.setImpactSpeed((Long) args.get(5).getValue());
        instance.setSoundtrackName((String) args.get(6).getValue());
        instance.setMinutesOfWaiting((Integer) args.get(7).getValue());
        instance.setMood((Mood) args.get(8).getValue());
        instance.setCar(car);

        if (!Validatable.validate(instance, this.IdValidatorDummy)) {
            System.err.println("Instance validation failed.");
            return null;
        }

        // Adding item to database
        System.out.println("Using hibernate...");
        try {
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            session.beginTransaction();
            session.save(instance);
            session.getTransaction().commit();
            HibernateUtil.getSessionFactory().close();
        } catch (HibernateException e) {
            System.err.println("Error occured while committing transaction: " + e);
            return null;
        }

        Interpreter.collection.add(instance);

        return null;
    }

    /**
     * UPDATE command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    public String[] update(LinkedList<Argument> args) {
        // Checking if object with given id exists
        Long id = (Long) args.get(0).getValue();
        int index = -1;
        for (int i = 0; i < Interpreter.collection.size(); i++) {
            if (id.equals(Interpreter.collection.get(i).getId())) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            System.err.println("There is no element with given id: " + id);
            return null;
        }

        LinkedList<Object> res = new LinkedList<>();

        HumanBeing prevInstance = Interpreter.collection.get(index);
        res.add(prevInstance.getId());                      // id
        res.add(args.get(1).getValue());                    // name
        res.add(new Coordinates(args.get(2).getValue(),
                                args.get(3).getValue()));   // coordinates
        res.add(prevInstance.getCreationDate());            // creationDate
        res.add(args.get(4).getValue());                    // realHero
        res.add(args.get(5).getValue());                    // hasToothpick
        res.add(args.get(6).getValue());                    // impactSpeed
        res.add(args.get(7).getValue());                    // soundtrackName
        res.add(args.get(8).getValue());                    // minutesOfWaiting
        res.add(args.get(9).getValue());                    // mood
        res.add(new Car(args.get(10).getValue(),
                        args.get(11).getValue()));          // car

        HumanBeing instance = new HumanBeing(res);
        if (Validatable.validate(instance, this.idValidator)) {
            Interpreter.collection.set(index, instance);
        } else {
            System.err.println("Instance validation failed.");
        }

        return null;
    }

    /**
     * REMOVE_BY_ID command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] removeById(LinkedList<Argument> args) {
        // Checking if object with given id exists
        Long id = (Long) args.get(0).getValue();
        int index = -1;
        for (int i = 0; i < Interpreter.collection.size(); i++) {
            if (id.equals(Interpreter.collection.get(i).getId())) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            System.err.println("There is no element with given id: " + id);
            return null;
        }

        Interpreter.collection.remove(index);

        return null;
    }

    /**
     * CLEAR command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] clear(LinkedList<Argument> args) {
        Interpreter.collection.clear();

        return null;
    }

    /**
     * SAVE command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] save(LinkedList<Argument> args) {
        this.database.write(Interpreter.collection);

        return null;
    }

    /**
     * EXECUTE_SCRIPT command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] executeScript(LinkedList<Argument> args) {
        String filename = (String) args.get(0).getValue();

        this.commandResult = filename;

        return new String[] { filename };
    }

    /**
     * EXIT command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] exit(LinkedList<Argument> args) {
        this.isRunning = false;

        return null;
    }

    /**
     * REMOVE_FIRST command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] removeFirst(LinkedList<Argument> args) {
        if (Interpreter.collection.isEmpty()) {
            System.err.println("Cannot remove first element, collection is empty.");
            return null;
        }

        Interpreter.collection.removeFirst();
        return null;
    }

    /**
     * HEAD command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] head(LinkedList<Argument> args) {
        String result = "";

        if (Interpreter.collection.isEmpty()) {
            System.err.println("Cannot show first element, collection is empty.");
            return null;
        }

        result += Interpreter.collection.getFirst().toString() + '\n';
        this.commandOutput = result;

        return null;
    }

    /**
     * HISTORY command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] history(LinkedList<Argument> args) {
        String result = "";

        // for (int i = 0; i < Math.min(12, this.history.size()); i++) {
        //     Command cmd = this.history.get(i);

        //     result += cmd.getType().name() + '\n';
        // }

        result = this.history.stream()
            .map(h -> h.getType().name() + '\n')
            .collect(Collectors.joining());

        this.commandOutput = result;

        return null;
    }

    /**
     * COUNT_GREATER_THAN_MINUTES_OF_WAITING command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] countGreaterThanMinutesOfWaiting(LinkedList<Argument> args) {
        String result = "";

        Integer minutesOfWaiting = (Integer) args.get(0).getValue();
        minutesOfWaiting = minutesOfWaiting == null ? 0 : minutesOfWaiting;

        int counter = 0;
        for (HumanBeing hb : Interpreter.collection) {
            Integer MOF = hb.getMinutesOfWaiting();
            MOF = MOF == null ? 0 : MOF;

            if (MOF > minutesOfWaiting) {
                counter++;
            }
        }

        result += counter + '\n';

        this.commandOutput = result;

        return null;
    }

    /**
     * FILTER_STARTS_WITH_NAME command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] filterStartsWithName(LinkedList<Argument> args) {
        String result = "";

        String substring = (String) args.get(0).getValue();
        for (HumanBeing hb : Interpreter.collection) {
            if (hb.getName().indexOf(substring) >= 0) {
                result += hb.toString() + '\n' + '\n';
            }
        }

        this.commandOutput = result;

        return null;
    }

    /**
     * PRINT_FIELD_DESCENDING_MINUTES_OF_WAITING command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] printFieldDescendingMinutesOfWaiting(LinkedList<Argument> args) {
        String result = "";

        class SortByMinutesOfWaiting implements Comparator<HumanBeing> {
            public int compare(HumanBeing a, HumanBeing b)
            {
                Integer aMOF = a.getMinutesOfWaiting();
                Integer bMOF = b.getMinutesOfWaiting();

                return (bMOF == null ? 0 : bMOF) -
                       (aMOF == null ? 0 : aMOF);
            }
        }

        HumanBeing[] hbs = Interpreter.collection.toArray(new HumanBeing[0]);
        Arrays.sort(hbs, new SortByMinutesOfWaiting());

        for (HumanBeing hb : hbs) {
            result += hb.toString() + '\n' + '\n';
        }

        this.commandOutput = result;

        return null;
    }

    /**
     * LOGIN command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] login(LinkedList<Argument> args) {
        return null;
    }

    /**
     * REGISTER command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] register(LinkedList<Argument> args) {
        String username = (String) args.get(0).getValue();
        String password = (String) args.get(1).getValue();

        // this.database.

        return null;
    }

    /**
     * Checks if there is element with {@code id} in collection.
     * Deprecated in favor of static method {@code HasItemWithId}.
     *
     * @param arg id of the element to check
     * @return true if exists, else false
     */
    @Deprecated
    public boolean hasItemWithId(Argument arg) {
        Long id = (Long) arg.getValue();

        for (HumanBeing hb : Interpreter.collection) {
            if (id.equals(hb.getId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Statically checks if there is an element with {@code id} in the collection.
     *
     * @param arg id of the element to check
     * @return true if exists, else false
     */
    public static boolean HasItemWithId(Argument arg) {
        Long id = (Long) arg.getValue();

        for (HumanBeing hb : Interpreter.collection) {
            if (id.equals(hb.getId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns output produced by the last command.
     *
     * @return output of the last command
     */
    public String getCommandOutput() {
        String result = this.commandOutput;
        this.commandOutput = null;
        return result;
    }

    /**
     * Returns some data that is not output produced
     * by the last command.
     *
     * @return result of the last command
     */
    public String getCommandResult() {
        String result = this.commandResult;
        this.commandResult = null;
        return result;
    }

    /**
     * Returns {@code isRunning} flag, that shows whether {@code Interpreter}
     * is still running or halted by an {@code exit} command.
     *
     * @return {@code isRunning} flag
     */
    public Boolean isRunning() {
        return this.isRunning;
    }

}
