package server.handler;

import java.util.LinkedList;

import core.command.Command;
import core.command.arguments.Argument;
import core.handler.ComHandler;
import core.net.Com;
import core.net.packet.Packet;
import core.net.packet.PacketType;
import server.interpreter.Interpreter;
import server.runner.RecursionFoundException;
import server.runner.Runner;

/**
 * Class for handling server side communication between client and server.
 *
 * @author ivatolm
 */
public class ServerComHandler extends ComHandler {

    // Runner
    private Runner runner;

    /**
     * Constructs new {@code ServerComHandler} with provided arguments.
     *
     * @param com communicator for talking to client
     */
    public ServerComHandler(Com com, Runner runner) {
        super(com);
        this.runner = runner;
    }

    /**
     * Receives and processes commands from the client.
     * Runs received command via {@code Runner} and sends
     * output and result of it back to the client.
     */
    @Override
    public void process() {
        Packet response = this.com.receive();
        switch (response.getType()) {
            case CommandReq:
                this.handleCommandReq(response);
                break;
            case ScriptResp:
                this.handleScriptResp(response);
                break;
            case ValidateIdReq:
                boolean result = this.handleValidateReq(response);
                Packet request = new Packet(PacketType.ValidateIdResp, result);
                this.com.send(request);
                return;
            default:
                System.err.println("Unknown response type: " + response.getType());
                return;
        }

        this.runner.run();

        // If some dependency is required, request it
        LinkedList<String> dependencies = this.runner.getProgramResult();
        if (dependencies != null) {
            String dependency = dependencies.getFirst();
            Packet request = new Packet(PacketType.ScriptReq, dependency);
            this.com.send(request);
        }

        LinkedList<String> output = this.runner.getProgramOutput();
        if (output != null) {
            Packet request = new Packet(PacketType.CommandResp, output);
            this.com.send(request);
        }
    }

    /**
     * Adds received command to the execution queue.
     *
     * @param packet packet to process
     */
    private void handleCommandReq(Packet packet) {
        Command command = (Command) packet.getData();

        if (command == null) {
            System.err.println("Received empty command. Skipping...");
            return;
        }

        try {
            this.runner.addCommand(command);
        } catch (RecursionFoundException e) {
            System.err.println("Recursion detected. Skipping...");
            return;
        }
    }

    /**
     * Adds received script to the execution queue.
     *
     * @param packet packet to process
     */
    private void handleScriptResp(Packet packet) {
        @SuppressWarnings("unchecked")
        LinkedList<Command> commands = (LinkedList<Command>) packet.getData();

        if (commands == null) {
            System.err.println("Received empty command-list. Skipping...");
            return;
        }

        try {
            runner.addSubroutine(commands);
        } catch (RecursionFoundException e) {
            System.err.println("Recursion detected. Skipping...");
            return;
        }
    }

    /**
     * Checks whether collection has item with received id.
     *
     * @param packet packet to process
     * @return result of validation
     */
    private boolean handleValidateReq(Packet packet) {
        Argument argument = (Argument) packet.getData();

        if (argument == null) {
            System.err.println("Received empty validation request. Skipping...");
            return false;
        }

        return Interpreter.HasItemWithId(argument);
    }

}