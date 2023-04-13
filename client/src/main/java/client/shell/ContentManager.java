package client.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;

import core.command.Command;
import core.command.arguments.Argument;
import core.parser.ArgumentCheckFailedException;
import core.parser.Parser;
import core.utils.SimpleParseException;

/**
 * Class representing manager of static content (scripts).
 *
 * @author ivatolm
 */
public class ContentManager {

    // Table of resources
    private HashMap<String, LinkedList<Command>> resources;

    /**
     * Constructs new {@code ContentManager} with provided arguments.
     *
     * @param pathToResources path to directory with resources
     */
    public ContentManager(String pathToResources) {
        File dir = new File(pathToResources);
        File[] resources = dir.listFiles();

        System.out.println("Processing resources:");
        this.resources = new HashMap<>();
        for (File resource : resources) {
            if (resource.isFile()) {
                System.out.print(resource.getName() + ": " + "...");
                LinkedList<Command> resourceContent = this.getResourceContent(resource);
                if (resourceContent != null) {
                    this.resources.put(resource.getAbsolutePath(), resourceContent);
                    System.out.print("\r" + resource.getName() + ": " + "added");
                } else {
                    System.out.print("\r" + resource.getName() + ": " + "skipped (validation failed)");
                }
                System.out.println();
            }
        }
    }

    /**
     * Returns content of resource located at {@code filename}.
     *
     * @param filename filename of the resource
     * @return content of the resource or null if it doesnt exist
     */
    public LinkedList<Command> get(String filename) {
        String path = Path.of(filename).toAbsolutePath().toString();
        return this.resources.get(path);
    }

    /**
     * Returns content of the resource {@code file}.
     *
     * @return content of the resource or null if it doesnt exist
     */
    private LinkedList<Command> getResourceContent(File file) {
        String path = file.getAbsolutePath();

        String content = this.getFileContent(path);
        if (content == null) {
            return null;
        }

        if (content.isBlank()) {
            return new LinkedList<>();
        } else {
            Parser parser = new Parser(((Argument arg) -> {return false;}));

            // intentionally not slimming input
            LinkedList<String> splittedContent;
            try {
                splittedContent = parser.split(content);
                for (String line : splittedContent) {
                    parser.parse(line);
                }
            } catch (ArgumentCheckFailedException | SimpleParseException e) {
                return null;
            }

            // Checking if script is valid
            if (parser.getCurrentCommandType() == null) {
                return parser.getResult();
            } else {
                return null;
            }
        }
    }

    /**
     * Returns content of the file at {@code filename}.
     *
     * @return content of the file or null if file doesnt exist
     */
    private String getFileContent(String filename) {
        try {
            FileInputStream fstream = new FileInputStream(filename);
            InputStreamReader istream = new InputStreamReader(fstream);

            String content = "";
            int data;
            while ((data = istream.read()) != -1) {
                content += (char) data;
            }

            istream.close();
            return content;

        } catch (IOException e) {
            return null;
        }
    }

}
