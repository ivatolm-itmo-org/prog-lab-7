package server.interpreter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import core.command.Command;
import core.command.CommandInfo;
import core.command.CommandType;
import core.command.arguments.Argument;
import core.models.IdValidator;
import core.models.Validatable;
import core.models.car.Car;
import core.models.coordinates.Coordinates;
import core.models.humanBeing.HumanBeing;
import core.models.mood.Mood;
import core.models.user.User;
import server.auth.AuthManager;
import server.database.HibernateUtil;

/**
 * Standalone class for interpreting commands.
 *
 * @author ivatolm
 */
public class Interpreter {

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
    public Interpreter() {
        this.wasRead = true;

        try {
            SessionFactory factory = HibernateUtil.getSessionFactory();
            Session session = factory.openSession();
            Criteria criteria = session.createCriteria(HumanBeing.class);

            @SuppressWarnings("unchecked")
            LinkedList<HumanBeing> tmp = new LinkedList<HumanBeing>(criteria.list());
            Interpreter.collection = tmp;

            session.close();
        } catch (HibernateException e) {
            System.err.println("Error occured while committing transaction: " + e);
            return;
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
        String signature = cmd.getSignature();

        if (this.history.size() > 12) {
            this.history.removeFirst();
        }

        this.history.add(cmd);

        switch (cmd.getType()) {
            case NOOP:
                return this.noop(args, signature);

            case HELP:
                return this.help(args, signature);

            case INFO:
                return this.info(args, signature);

            case SHOW:
                return this.show(args, signature);

            case ADD:
                return this.add(args, signature);

            case UPDATE:
                return this.update(args, signature);

            case REMOVE_BY_ID:
                return this.removeById(args, signature);

            case CLEAR:
                return this.clear(args, signature);

            case SAVE:
                return this.save(args, signature);

            case EXECUTE_SCRIPT:
                return this.executeScript(args, signature);

            case EXIT:
                return this.exit(args, signature);

            case REMOVE_FIRST:
                return this.removeFirst(args, signature);

            case HEAD:
                return this.head(args, signature);

            case HISTORY:
                return this.history(args, signature);

            case COUNT_GREATER_THAN_MINUTES_OF_WAITING:
                return this.countGreaterThanMinutesOfWaiting(args, signature);

            case FILTER_STARTS_WITH_NAME:
                return this.filterStartsWithName(args, signature);

            case PRINT_FIELD_DESCENDING_MINUTES_OF_WAITING:
                return this.printFieldDescendingMinutesOfWaiting(args, signature);

            case LOGIN:
                return this.login(args, signature);

            case REGISTER:
                return this.register(args, signature);

            default:
                System.err.println("Unknown command.");
        }

        return new String[] {};
    }

    /**
     * NOOP command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] noop(LinkedList<Argument> args, String signature) {
        return null;
    }

    /**
     * HELP command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] help(LinkedList<Argument> args, String signature) {
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
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] info(LinkedList<Argument> args, String signature) {
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
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] show(LinkedList<Argument> args, String signature) {
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
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] add(LinkedList<Argument> args, String signature) {
        Coordinates coordinates = new Coordinates();
        coordinates.setX((Integer) args.get(1).getValue());
        coordinates.setY((Float) args.get(2).getValue());

        Car car = new Car();
        car.setName((String) args.get(9).getValue());
        car.setCool((Boolean) args.get(10).getValue());

        User user;
        try {
            SessionFactory factory = HibernateUtil.getSessionFactory();
            Session session = factory.openSession();
            Criteria criteria = session.createCriteria(User.class)
                .add(Restrictions.idEq(signature));

            user = (User) criteria.uniqueResult();

            session.close();
        } catch (HibernateException e) {
            System.err.println("Error occured while committing transaction: " + e);
            return null;
        }

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
        instance.setOwner(user);

        if (!Validatable.validate(instance, this.IdValidatorDummy)) {
            System.err.println("Instance validation failed.");
            return null;
        }

        try {
            SessionFactory factory = HibernateUtil.getSessionFactory();
            Session session = factory.openSession();
            session.beginTransaction();
            session.save(instance);
            session.getTransaction().commit();
            session.close();
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
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    public String[] update(LinkedList<Argument> args, String signature) {
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

        HumanBeing prevInstance = Interpreter.collection.get(index);

        if (!prevInstance.getOwner().getUsername().equals(signature)) {
            this.commandOutput = "Permisson denied.";
            return null;
        }

        Coordinates coordinates = new Coordinates();
        coordinates.setX((Integer) args.get(2).getValue());
        coordinates.setY((Float) args.get(3).getValue());

        Car car = new Car();
        car.setName((String) args.get(10).getValue());
        car.setCool((Boolean) args.get(11).getValue());

        HumanBeing instance = new HumanBeing();
        instance.setId(id);
        instance.setName((String) args.get(1).getValue());
        instance.setCoordinates(coordinates);
        instance.setCreationDate(System.currentTimeMillis());
        instance.setRealHero((Boolean) args.get(4).getValue());
        instance.setHasToothpick((Boolean) args.get(5).getValue());
        instance.setImpactSpeed((Long) args.get(6).getValue());
        instance.setSoundtrackName((String) args.get(7).getValue());
        instance.setMinutesOfWaiting((Integer) args.get(8).getValue());
        instance.setMood((Mood) args.get(9).getValue());
        instance.setCar(car);
        instance.setOwner(prevInstance.getOwner());

        if (!Validatable.validate(instance, this.idValidator)) {
            System.err.println("Instance validation failed.");
        }

        try {
            SessionFactory factory = HibernateUtil.getSessionFactory();
            Session session = factory.openSession();
            session.beginTransaction();
            session.update(instance);
            session.getTransaction().commit();
            session.close();
        } catch (HibernateException e) {
            System.err.println("Error occured while committing transaction: " + e);
            return null;
        }

        Interpreter.collection.set(index, instance);

        return null;
    }

    /**
     * REMOVE_BY_ID command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] removeById(LinkedList<Argument> args, String signature) {
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

        HumanBeing instance = Interpreter.collection.get(index);

        try {
            SessionFactory factory = HibernateUtil.getSessionFactory();
            Session session = factory.openSession();
            session.beginTransaction();
            session.delete(instance);
            session.getTransaction().commit();
            session.close();
        } catch (HibernateException e) {
            System.err.println("Error occured while committing transaction: " + e);
            return null;
        }

        Interpreter.collection.remove(index);

        return null;
    }

    /**
     * CLEAR command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] clear(LinkedList<Argument> args, String signature) {
        try {
            SessionFactory factory = HibernateUtil.getSessionFactory();
            Session session = factory.openSession();
            session.beginTransaction();

            for (HumanBeing hb : Interpreter.collection) {
                session.delete(hb);
            }

            session.getTransaction().commit();
            session.close();
        } catch (HibernateException e) {
            System.err.println("Error occured while committing transaction: " + e);
            return null;
        }

        Interpreter.collection.clear();

        return null;
    }

    /**
     * SAVE command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] save(LinkedList<Argument> args, String signature) {
        // this.database.write(Interpreter.collection);

        return null;
    }

    /**
     * EXECUTE_SCRIPT command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] executeScript(LinkedList<Argument> args, String signature) {
        String filename = (String) args.get(0).getValue();

        this.commandResult = filename;

        return new String[] { filename };
    }

    /**
     * EXIT command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] exit(LinkedList<Argument> args, String signature) {
        this.isRunning = false;

        return null;
    }

    /**
     * REMOVE_FIRST command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] removeFirst(LinkedList<Argument> args, String signature) {
        if (Interpreter.collection.isEmpty()) {
            System.err.println("Cannot remove first element, collection is empty.");
            return null;
        }

        HumanBeing instance = Interpreter.collection.getFirst();

        try {
            SessionFactory factory = HibernateUtil.getSessionFactory();
            Session session = factory.openSession();
            session.beginTransaction();
            session.delete(instance);
            session.getTransaction().commit();
            session.close();
        } catch (HibernateException e) {
            System.err.println("Error occured while committing transaction: " + e);
            return null;
        }

        Interpreter.collection.removeFirst();
        return null;
    }

    /**
     * HEAD command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] head(LinkedList<Argument> args, String signature) {
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
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] history(LinkedList<Argument> args, String signature) {
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
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] countGreaterThanMinutesOfWaiting(LinkedList<Argument> args, String signature) {
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
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] filterStartsWithName(LinkedList<Argument> args, String signature) {
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
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] printFieldDescendingMinutesOfWaiting(LinkedList<Argument> args, String signature) {
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
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] login(LinkedList<Argument> args, String signature) {
        return null;
    }

    /**
     * REGISTER command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @param signature signature of the user
     * @return list of commands for later interpretation or null
     */
    private String[] register(LinkedList<Argument> args, String signature) {
        String username = (String) args.get(0).getValue();
        String password = (String) args.get(1).getValue();

        String passwordHash = AuthManager.getCryptoHash(password);

        User instance = new User();
        instance.setUsername(username);
        instance.setPassword(passwordHash);

        try {
            SessionFactory factory = HibernateUtil.getSessionFactory();
            Session session = factory.openSession();
            session.beginTransaction();
            session.save(instance);
            session.getTransaction().commit();
            session.close();
            this.commandOutput = "Successfully registered";
        } catch (HibernateException e) {
            this.commandOutput = "Failed to register.";
            System.err.println("Error occured while committing transaction: " + e);
            return null;
        }

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
