package com.ivatolm.app.interpreter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import com.ivatolm.app.database.IDatabase;
import com.ivatolm.app.humanBeing.Car;
import com.ivatolm.app.humanBeing.Coordinates;
import com.ivatolm.app.humanBeing.HumanBeing;
import com.ivatolm.app.parser.Command;
import com.ivatolm.app.parser.arguments.Argument;

/**
 * Class for interpreting commands.
 *
 * @author ivatolm
 */
public class Interpreter {

    /** Link to database */
    private IDatabase<HumanBeing> database;

    /** Collection of objects (described in the task) */
    private LinkedList<HumanBeing> collection;

    /** Was database read or it was created? */
    private final boolean wasRead;

    /** History of interpreted commands */
    private LinkedList<Command> history;

    /**
     * Constructs instance of the class.
     *
     * @param database link to database
     */
    public Interpreter(IDatabase<HumanBeing> database) {
        this.database = database;
        this.database.setDummyObject(new HumanBeing());

        LinkedList<HumanBeing> data = this.database.read();
        if (data != null) {
            this.collection = data;
            this.wasRead = true;
        } else {
            this.collection = new LinkedList<>();
            this.wasRead = false;
        }

        this.history = new LinkedList<>();
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

        if (this.history.size() > 100) {
            this.history.removeFirst();
        }

        this.history.add(cmd);

        switch (cmd) {
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
                return this.remove_by_id(args);

            case CLEAR:
                return this.clear(args);

            case SAVE:
                return this.save(args);

            case EXECUTE_SCRIPT:
                return this.execute_script(args);

            case EXIT:
                return this.exit(args);

            case REMOVE_FIRST:
                return this.remove_first(args);

            case HEAD:
                return this.head(args);

            case HISTORY:
                return this.history(args);

            case COUNT_GREATER_THAN_MINUTES_OF_WAITING:
                return this.count_greater_than_minutes_of_waiting(args);

            case FILTER_STARTS_WITH_NAME:
                return this.filter_starts_with_name(args);

            case PRINT_FIELD_DESCENDING_MINUTES_OF_WAITING:
                return this.print_field_descending_minutes_of_waiting(args);

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
        for (Command c : Command.values()) {
            String[] description = c.getDescription();
            int nameDescriptionLength = description[0].length();
            if (length < nameDescriptionLength) {
                length = nameDescriptionLength;
            }
        }

        for (Command c : Command.values()) {
            String[] description = c.getDescription();
            result += description[0];

            // Adding whitespaces
            for (int i = 0; i < length - description[0].length() + gap; i++) {
                result += " ";
            }

            result += description[1];
            result += "\n";
        }

        System.out.println(result);

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
        result += "Type: " + this.collection.getClass() + "\n";
        result += "Creation date: " + (this.wasRead ? "unknown" : "recently") + "\n";
        result += "Size: " + this.collection.size() + "\n";

        System.out.println(result);

        return null;
    }

    /**
     * SHOW command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] show(LinkedList<Argument> args) {
        for (HumanBeing hb : this.collection) {
            System.out.println(hb);
            System.out.println();
        }

        return null;
    }

    /**
     * ADD command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] add(LinkedList<Argument> args) {
        LinkedList<Object> res = new LinkedList<>();

        res.add(System.currentTimeMillis());                // id
        res.add(args.get(0).getValue());                    // name
        res.add(new Coordinates(args.get(1).getValue(),
                                args.get(2).getValue()));   // coordinates
        res.add(LocalDate.now());                           // creationDate
        res.add(args.get(3).getValue());                    // realHero
        res.add(args.get(4).getValue());                    // hasToothpick
        res.add(args.get(5).getValue());                    // impactSpeed
        res.add(args.get(6).getValue());                    // soundtrackName
        res.add(args.get(7).getValue());                    // minutesOfWaiting
        res.add(args.get(8).getValue());                    // mood
        res.add(new Car(args.get(9).getValue(),
                        args.get(10).getValue()));          // car

        HumanBeing instance = new HumanBeing(res);
        this.collection.add(instance);

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
        for (int i = 0; i < this.collection.size(); i++) {
            if (id.equals(this.collection.get(i).getId())) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            System.err.println("There is no element with given id: " + id);
            return null;
        }

        LinkedList<Object> res = new LinkedList<>();

        HumanBeing prevInstance = this.collection.get(index);
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
        this.collection.set(index, instance);

        return null;
    }

    /**
     * REMOVE_BY_ID command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] remove_by_id(LinkedList<Argument> args) {
        // Checking if object with given id exists
        Long id = (Long) args.get(0).getValue();
        int index = -1;
        for (int i = 0; i < this.collection.size(); i++) {
            if (id.equals(this.collection.get(i).getId())) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            System.err.println("There is no element with given id: " + id);
            return null;
        }

        this.collection.remove(index);

        return null;
    }

    /**
     * CLEAR command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] clear(LinkedList<Argument> args) {
        this.collection.clear();

        return null;
    }

    /**
     * SAVE command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] save(LinkedList<Argument> args) {
        this.database.write(this.collection);

        return null;
    }

    /**
     * EXECUTE_SCRIPT command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] execute_script(LinkedList<Argument> args) {
        String filename = (String) args.get(0).getValue();

        try {
            LinkedList<String> source = new LinkedList<>();

            FileInputStream fstream = new FileInputStream(filename);
            InputStreamReader istream = new InputStreamReader(fstream);

            String current = "";
            int data;
            while ((data = istream.read()) != -1) {
                char c = (char) data;

                if (source.isEmpty()) {
                    source.add("");
                }

                if (c == '\n') {
                    source.set(source.size() - 1, current);
                    source.add("");
                    current = "";
                } else {
                    current += c;
                }
            }

            if (!current.isEmpty()) {
                source.set(source.size() - 1, current);
            }

            istream.close();

            return source.toArray(new String[0]);

        } catch (FileNotFoundException e) {
            System.err.println("Cannot open file.");
            return null;
        } catch (IOException e) {
            System.err.println("Cannot read file.");
            return null;
        }
    }

    /**
     * EXIT command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] exit(LinkedList<Argument> args) {
        System.exit(0);

        return null;
    }

    /**
     * REMOVE_FIRST command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] remove_first(LinkedList<Argument> args) {
        if (this.collection.isEmpty()) {
            System.err.println("Cannot remove first element, collection is empty.");
            return null;
        }

        this.collection.removeFirst();
        return null;
    }

    /**
     * HEAD command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] head(LinkedList<Argument> args) {
        if (this.collection.isEmpty()) {
            System.err.println("Cannot show first element, collection is empty.");
            return null;
        }

        System.out.println(this.collection.getFirst());

        return null;
    }

    /**
     * HISTORY command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] history(LinkedList<Argument> args) {
        for (int i = 0; i < Math.min(12, this.history.size()); i++) {
            Command cmd = this.history.get(i);

            System.out.println(cmd.name());
        }

        return null;
    }

    /**
     * COUNT_GREATER_THAN_MINUTES_OF_WAITING command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] count_greater_than_minutes_of_waiting(LinkedList<Argument> args) {
        int minutesOfWaiting = (int) args.get(0).getValue();

        int counter = 0;
        for (HumanBeing hb : this.collection) {
            if (hb.getMinutesOfWaiting() > minutesOfWaiting) {
                counter++;
            }
        }

        System.out.println("Result: " + counter);

        return null;
    }

    /**
     * FILTER_STARTS_WITH_NAME command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] filter_starts_with_name(LinkedList<Argument> args) {
        String substring = (String) args.get(0).getValue();

        for (HumanBeing hb : this.collection) {
            if (hb.getName().indexOf(substring) > 0) {
                System.out.println(hb);
                System.out.println();
            }
        }

        return null;
    }

    /**
     * PRINT_FIELD_DESCENDING_MINUTES_OF_WAITING command, description is provided in {@code Command}.
     *
     * @param args arguments for the command
     * @return list of commands for later interpretation or null
     */
    private String[] print_field_descending_minutes_of_waiting(LinkedList<Argument> args) {
        class SortByMinutesOfWaiting implements Comparator<HumanBeing> {
            public int compare(HumanBeing a, HumanBeing b)
            {
                return a.getMinutesOfWaiting() - b.getMinutesOfWaiting();
            }
        }

        HumanBeing[] hbs = this.collection.toArray(new HumanBeing[0]);
        Arrays.sort(hbs, new SortByMinutesOfWaiting());

        for (HumanBeing hb : hbs) {
            System.out.println(hb);
            System.out.println();
        }

        return null;
    }

}
