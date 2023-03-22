package com.ivatolm.app.parser;

import java.util.LinkedList;

import com.ivatolm.app.parser.arguments.Argument;

/**
 * This class represents a command with values of the arguments.
 *
 * @author ivatolm
 */
public class Command {

    /** Type of the command */
    private CommandType type;

    /** Values of the arguments */
    private LinkedList<Argument> argsValues;

    /**
     * Constructs new command with provided arguments.
     *
     * @param type type of the command
     * @param argsValues values of the arguments for the command
     */
    Command(CommandType type, LinkedList<Argument> argsValues) {
        this.type = type;
        this.argsValues = argsValues;
    }

    /**
     * @return type of the command
     */
    public CommandType getType() {
        return this.type;
    }

    /**
     * @return {@code argsValues} field of the object
     */
    public LinkedList<Argument> getArgsValues() {
        return this.argsValues;
    }

    /**
     * @return {@code Argument} argument at {@code index}
     */
    public Argument getArgValue(int index) {
        return this.argsValues.get(index);
    }

    /**
     * Overrides {@code toString} of {@code Object}.
     *
     * @return string representation of the object
     */
    @Override
    public String toString() {
        String result = "";

        result += this.type.name() + "\n";
        for (Argument arg : this.getArgsValues()) {
            result += arg.getValue() + "\n";
        }

        return result;
    }

}
