package client.shell;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import client.parser.ArgumentCheckFailedException;
import client.parser.Parser;
import core.command.Command;
import core.command.CommandType;
import core.net.Com;
import core.net.packet.Packet;
import core.net.packet.PacketType;
import core.utils.SimpleParseException;

/**
 * Class providing user interactive shell.
 *
 * @author ivatolm
 */
public class Shell {

    /** Command line scanner */
    private Scanner scanner;

    /** Command parser */
    private Parser parser;

    /** Communicator */
    private Com com;

    /**
     * Constructs new {@code Shell} with provided arguments.
     *
     * @param filename database filename
     */
    public Shell(Com com) {
        this.scanner = new Scanner(System.in);
        this.parser = new Parser();
        this.com = com;
    }

    /**
     * Runs interactive shell until EOF.
     * Work cycle:
     * 1. get user input
     * 2. parse user input
     * 3. run command
     * 4. if command require more commands to be run,
     * then go to step 3
     */
    public void run() {
        try {
            Packet request, response;
            while (true) {
                Command command = this.parseCommands(null).getFirst();

                request = new Packet(PacketType.CommandReq, command);
                this.com.send(request);

                boolean isRunning = true;
                while (isRunning) {
                    response = this.com.receive();
                    switch (response.getType()) {
                        case CommandResp:
                            String output = (String) response.getData();
                            System.out.println(output);

                            isRunning = false;
                            break;
                        case ScriptReq:
                            String filename = (String) response.getData();
                            System.out.println("Server requested file: " + filename);
                            LinkedList<Command> commands = this.parseScript(filename);

                            if (commands == null) {
                                // TODO: notify server about error
                                commands = new LinkedList<>();
                            }

                            request = new Packet(PacketType.ScriptResp, commands);
                            this.com.send(request);

                            System.out.println("Sending script...");
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nExiting by Ctrl-D (EOF)");
        }
    }

    /**
     * Parses one or multiple {@code Command}-s from {@code inputs}.
     * If input is null, greets user and parses the command.
     * If parsing fails, then asks user to correct the input.
     *
     * @param inputs strings to parse {@code Command} from or null
     * @return parsed {@code Command}-s
     */
    private LinkedList<Command> parseCommands(LinkedList<String> inputs) {
        LinkedList<Command> result = new LinkedList<>();

        boolean promptRequired = inputs == null || inputs.isEmpty();
        while (true) {
            String input;
            if (promptRequired) {
                System.out.print(": ");
                input = this.scanner.nextLine();
            } else {
                input = inputs.pop();
            }

            promptRequired = inputs == null || inputs.isEmpty();

            try {
                boolean hasParsedCommands = this.parser.parse(input);
                if (hasParsedCommands == true) {
                    result.addAll(this.parser.getResult());

                    if (promptRequired) {
                        return result;
                    }
                }

            } catch (SimpleParseException e) {
                System.err.println(e.getMessage());
                promptRequired = true;
                continue;

            } catch (ArgumentCheckFailedException e) {
                System.err.println(e.getMessage());
                promptRequired = true;
            }

            if (promptRequired) {
                CommandType cmdType = this.parser.getCurrentCommandType();
                int argCnt = this.parser.getCurrentArgumentsCnt();

                String greeting = "Enter" + " " + "'" + cmdType.getArgument(argCnt).getGreetingMsg() + "'";
                System.out.print(greeting);
            }
        }
    }

    /**
     * Parses script into list of {@code Command}-s.
     * If input is null, greets user and parses the command.
     * If parsing fails, then asks user to correct the input.
     *
     * @param filename filename of the script
     * @return parsed {@code Command}-s
     */
    private LinkedList<Command> parseScript(String filename) {
        LinkedList<String> source = new LinkedList<>();

        try {
            FileInputStream fstream = new FileInputStream(filename);
            InputStreamReader istream = new InputStreamReader(fstream);

            String input = "";
            int data;
            while ((data = istream.read()) != -1) {
                input += (char) data;
            }

            Parser parser = new Parser();
            try {
                if (input.isBlank()) {
                    return null;
                } else {
                    // intentionally not slimming input
                    source = parser.split(input);
                }
            } catch (SimpleParseException e) {
                System.err.println("Cannot parse script file.");
                return null;
            } finally {
                istream.close();
            }

            return this.parseCommands(source);

        } catch (FileNotFoundException e) {
            System.err.println("Cannot open file.");
            return null;
        } catch (IOException e) {
            System.err.println("Cannot read file.");
            return null;
        }
    }

    /**
     * Closes shell.
     * Closes internal connections.
     */
    public void close() {
        this.scanner.close();
    }

}
