package server.runner;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

import core.command.Command;
import core.command.arguments.Argument;
import server.interpreter.Interpreter;

/**
 * Class for running programs.
 *
 * @author ivatolm
 */
public class Runner {

    /** Command interpreter */
    private Interpreter interpreter;

    /** Program's call stack */
    private Stack<Integer> callstack;

    /** Program's subroutines */
    private Hashtable<Integer, LinkedList<Command>> subroutines;

    /** Program's output */
    private LinkedList<String> programOutput;

    /** Program's result */
    private LinkedList<String> programResult;

    /** Running flag */
    private Boolean isRunning = true;

    /**
     * Constructs new {@code Runner} with provided arguments.
     *
     * @param interpreter command interpreter with attached database
     */
    public Runner(Interpreter interpreter) {
        this.interpreter = interpreter;

        // TODO: Check for database to be attached to interpreter.

        this.callstack = new Stack<>();
        this.subroutines = new Hashtable<>();

        this.programOutput = null;
        this.programResult = null;
    }

    /**
     * Runs subroutines until required to create new one.
     * If new one have to be created, then saves current state
     * and returns new inputs to parse.
     * Returns null if halted by finish of {@code Interpreter}. Also sets
     * {@code isRunning} flag positive.
     *
     * @return new inputs to parse or null, if nothing to parse or halted
     */
    public LinkedList<String> run() {
        if (this.programOutput == null) {
            this.programOutput = new LinkedList<>();
        }

        while (!this.callstack.isEmpty()) {
            Integer subroutineId = this.callstack.pop();
            LinkedList<Command> subroutine = this.subroutines.get(subroutineId);
            this.subroutines.remove(subroutineId);

            while (!subroutine.isEmpty()) {
                Command command = subroutine.pop();

                this.interpreter.exec(command);
                if (!this.interpreter.isRunning()) {
                    this.isRunning = false;
                    return null;
                }

                String commandOutput = this.interpreter.getCommandOutput();
                if (commandOutput != null) {
                    this.programOutput.add(commandOutput);
                }

                String commandResult = this.interpreter.getCommandResult();
                if (commandResult != null) {
                    // Saving current state
                    this.subroutines.put(subroutineId, subroutine);
                    this.callstack.push(subroutineId);

                    if (this.programResult == null) {
                        this.programResult = new LinkedList<>();
                    }

                    this.programResult.add(commandResult);
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Creates new subroutine from {@code command} and puts it
     * on the top of the {@code callstack}.
     *
     * @param command to create subroutine from
     */
    public void addCommand(Command command) throws RecursionFoundException {
        LinkedList<Command> commands = new LinkedList<>(Arrays.asList(command));
        this.addSubroutine(commands);
    }

    /**
     * Creates new subroutine from {@code commands} and puts it
     * on the top of the {@code callstack}.
     *
     * @param commands to create subroutine from
     */
    public void addSubroutine(LinkedList<Command> commands) throws RecursionFoundException {
        Integer newSubroutineId = this.getSubroutineHash(commands);

        if (this.subroutines.containsKey(newSubroutineId)) {
            this.callstack = new Stack<>();
            this.subroutines = new Hashtable<>();
            throw new RecursionFoundException("Recursion detected. Erasing callstack...");
        }

        this.subroutines.put(newSubroutineId, commands);
        this.callstack.push(newSubroutineId);
    }

    /**
     * Returns hash of the subroutine.
     * Uses hash implementation of {@code AbstractList}.
     *
     * @param subroutine subroutine to get hash from
     * @return int hash value
     */
    private int getSubroutineHash(LinkedList<Command> subroutine) {
        int hashCode = 1;

        for (Command command : subroutine) {
            int cmdHashCode = 1;

            for (Argument argument : command.getArgsValues()) {
                Object value = argument.getValue();
                cmdHashCode = 31 * cmdHashCode + (value == null ? 0 : value.hashCode());
            }

            hashCode = 31 * hashCode + (command == null ? 0 : cmdHashCode);
        }

        return hashCode;
    }

    /**
     * Returns output produced by the last execution of the program.
     *
     * @return output of the last program execution
     */
    public LinkedList<String> getProgramOutput() {
        LinkedList<String> result = this.programOutput;
        this.programOutput = null;
        return result;
    }

    /**
     * Returns result produced by the last execution of the program.
     *
     * @return result of the last program execution
     */
    public LinkedList<String> getProgramResult() {
        LinkedList<String> result = this.programResult;
        this.programResult = null;
        return result;
    }

    /**
     * Returns {@code isRunning} flag, that shows whether {@code Runner}
     * is still running or halted by finish of {@code Interpreter}.
     *
     * @return {@code isRunning} flag
     */
    public Boolean isRunning() {
        return this.isRunning;
    }

}
