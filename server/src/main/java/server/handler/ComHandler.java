package server.handler;

import java.nio.channels.SelectableChannel;
import java.util.LinkedList;

import core.command.Command;
import core.net.Com;
import core.net.packet.Packet;
import core.net.packet.PacketType;
import server.runner.RecursionFoundException;
import server.runner.Runner;

/**
 * Class for handling server side communication of client and server.
 *
 * @author ivatolm
 */
public class ComHandler {

    // Communicator for talking to client
    private Com com;

    // Client program runner
    private Runner runner;

    /**
     * Constructs new {@code ComHandler} with provided arguments.
     *
     * @param com communicator for talking to client
     */
    public ComHandler(Com com, Runner runner) {
        this.com = com;
        this.runner = runner;
    }

    /**
     * TODO: Rewrite description.
     * Sends commands to server for processing.
     * Sends commands one-by-one and collects received
     * output. If command requires other commands to be
     * executed, then sends commands to server.
     *
     * @param commands commands for processing
     * @return output of each command
     */
    public String[] process() {
        Packet response = this.com.receive();
        switch (response.getType()) {
            case CommandReq:
                this.handleCommandReq(response);
                break;
            case CommandResp:
                break;
            case ScriptReq:
                break;
            case ScriptResp:
                this.handleScriptResp(response);
                break;
        }

        LinkedList<String> dependencies = this.runner.run();
        if (dependencies != null) {
            String dependency = dependencies.getFirst();
            Packet request = new Packet(PacketType.ScriptReq, dependency);
            this.com.send(request);
        } else {
            Packet request = new Packet(PacketType.CommandResp, "Success!");
            this.com.send(request);
        }

        return new String[] {};
    }

    /**
     * Returns communication channel of the communicator.
     *
     * @return communicator's channel
     */
    public SelectableChannel getComChannel() {
        return this.com.getChannel();
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

}
