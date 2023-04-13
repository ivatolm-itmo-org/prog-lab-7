package client.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.LinkedList;

import org.apache.commons.lang3.SerializationUtils;

import client.shell.ContentManager;
import core.command.Command;
import core.command.arguments.Argument;
import core.handler.ComHandler;
import core.net.Com;
import core.net.packet.Packet;
import core.net.packet.PacketType;

/**
 * Class for handling client side communication between client and server.
 *
 * @author ivatolm
 */
public class ClientComHandler extends ComHandler {

    // Content Manager
    private ContentManager contentManager;

    // Communication channel with Shell
    private Pipe.SourceChannel sourceChannel;
    private Pipe.SinkChannel sinkChannel;

    /**
     * Constructs new {@code ComHandler} with provided arguments.
     *
     * @param com communicator for talking to server
     */
    public ClientComHandler(Com com, ContentManager contentManager,
                            Pipe.SourceChannel sourceChannel,
                            Pipe.SinkChannel sinkChannel) {
        super(com);
        this.contentManager = contentManager;
        this.sourceChannel = sourceChannel;
        this.sinkChannel = sinkChannel;
    }

    /**
     * Sends commands to server for processing.
     * Collects received output and result of the command.
     */
    @Override
    public void process() {
        ByteBuffer buffer;

        // Reading commands from ShellHandler
        buffer = ByteBuffer.wrap(new byte[16384]);
        try {
            this.sourceChannel.read(buffer);
        } catch (IOException e) {
            System.err.println("Cannot read from the pipe: " + e);
            return;
        }

        byte[] result = new byte[] { 1 };
        ClientEvent event = SerializationUtils.deserialize(buffer.array());
        switch (event.getType()) {
            case NewCommands:
                result = this.handleEventNewCommands(event);
                break;
            case ValidateId:
                result = this.handleValidateId(event);
                break;
        }

        // Sending output to ShellHandler
        buffer = ByteBuffer.wrap(result);
        try {
            this.sinkChannel.write(buffer);
        } catch (IOException e) {
            System.err.println("Cannot send commands to the pipe: " + e);
            return;
        }
    }

    /**
     * Sends new commands to the server and returns output.
     *
     * @param event event to process
     * @return result of the handling
     */
    private byte[] handleEventNewCommands(ClientEvent event) {
        @SuppressWarnings("unchecked")
        LinkedList<Command> commands = (LinkedList<Command>) event.getData();

        LinkedList<String> result = new LinkedList<>();
        for (Command command : commands) {
            Packet request = new Packet(PacketType.CommandReq, command);
            this.com.send(request);

            String output = null;
            boolean finished = false;
            while (!finished) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {}
                Packet response = this.com.receive();
                if (response == null) {
                    finished = true;
                    break;
                }

                switch (response.getType()) {
                    case CommandResp:
                        output = this.handleCommandResp(response);
                        break;
                    case ScriptReq:
                        output = this.handleScriptReq(response);
                        break;
                    default:
                        System.err.println("Unknown response type: " + response.getType());
                        break;
                }

                if (output != null) {
                    result.add(output);
                }
            }
        }

        return SerializationUtils.serialize(result);
    }

    /**
     * Sends validation request to server and returns result.
     *
     * @param event event to process
     * @return result of the handling
     */
    private byte[] handleValidateId(ClientEvent event) {
        Argument argument = (Argument) event.getData();
        Packet request = new Packet(PacketType.ValidateIdReq, argument);
        this.com.send(request);

        boolean result = false;
        boolean finished = false;
        while (!finished) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
            Packet response = this.com.receive();
            if (response == null) {
                finished = true;
                break;
            }

            switch (response.getType()) {
                case ValidateIdResp:
                    result = this.handleValidateIdResp(response);
                    break;
                default:
                    System.err.println("Unknown reposnse type: " + response.getType());
                    break;
            }
        }

        return SerializationUtils.serialize(result);
    }

    /**
     * Prints received output of the command.
     *
     * @param packet packet to process
     * @return output, as processing finished
     */
    private String handleCommandResp(Packet packet) {
        String result = "";

        @SuppressWarnings("unchecked")
        LinkedList<String> output = (LinkedList<String>) packet.getData();

        for (String line : output) {
            result += line + '\n';
        }

        return result;
    }

    /**
     * Sends requested script to the server.
     *
     * @param packet packet to process
     * @return null, as processing is not finished
     */
    private String handleScriptReq(Packet packet) {
        String filename = (String) packet.getData();

        LinkedList<Command> cmds = this.contentManager.get(filename);

        Packet response = new Packet(PacketType.ScriptResp, cmds);
        this.com.send(response);

        return null;
    }

    /**
     * Returns result of server id validation.
     *
     * @param packet packet to process
     * @return result of validation
     */
    private boolean handleValidateIdResp(Packet packet) {
        boolean result = (boolean) packet.getData();

        return result;
    }

}
