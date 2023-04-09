package core.command;

/**
 * Class for storing information about command.
 * Created for command definition to be compile-checked.
 *
 * @author ivatolm
 */
public class CommandInfo {

    /** Name of the command */
    private String name;

    /** Description of the command */
    private String description;

    /**
     * Constructs new instance with provided arguments.
     *
     * @param name name of the command
     * @param description description of the command
     */
    CommandInfo(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * @return {@code name} of the command
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return {@code description} of the command
     */
    public String getDescription() {
        return this.description;
    }

}
