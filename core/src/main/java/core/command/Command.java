package core.command;

import java.io.Serializable;
import java.util.LinkedList;

import core.command.arguments.Argument;

/**
 * This class represents a command with values of the arguments.
 *
 * @author ivatolm
 */
public class Command implements Serializable {

    /** Type of the command */
    private CommandType type;

    /** Values of the arguments */
    private LinkedList<Argument> argsValues;

    /** Signature */
    private String signature;

    /**
     * Constructs new command with provided arguments.
     *
     * @param type type of the command
     * @param argsValues values of the arguments for the command
     */
    public Command(CommandType type, LinkedList<Argument> argsValues) {
        this.type = type;
        this.argsValues = argsValues;
    }

    /**
     * Sets signature of the command to {@code signature}.
     */
    public void setSignature(String signature) {
        this.signature = signature;
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
     * @return {@code signature} field of the object
     */
    public String getSignature() {
        return this.signature;
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
