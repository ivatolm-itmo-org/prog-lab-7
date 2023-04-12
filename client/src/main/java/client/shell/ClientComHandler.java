package client.shell;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.LinkedList;

import org.apache.commons.lang3.SerializationUtils;

import core.command.Command;
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
    private Pipe shellPipe;

    /**
     * Constructs new {@code ComHandler} with provided arguments.
     *
     * @param com communicator for talking to server
     */
    public ClientComHandler(Com com, ContentManager contentManager, Pipe shellPipe) {
        super(com);
        this.contentManager = contentManager;
        this.shellPipe = shellPipe;
    }

    /**
     * Sends commands to server for processing.
     * Collects received output and result of the command.
     */
    @Override
    public void process() {
        ByteBuffer buffer;

        // Reading commands from ShellHandler
        buffer = ByteBuffer.wrap(new byte[1024]);
        try {
            this.shellPipe.source().read(buffer);
        } catch (IOException e) {
            System.err.println("Cannot read from the pipe: " + e);
            return;
        }

        LinkedList<Command> commands = SerializationUtils.deserialize(buffer.array());

        LinkedList<String> result = new LinkedList<>();
        for (Command command : commands) {
            Packet request = new Packet(PacketType.CommandReq, command);
            this.com.send(request);

            String output = null;
            boolean finished = false;
            while (!finished) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
                Packet response = this.com.receive();
                System.out.println(response);
                if (response == null) {
                    finished = true;
                    break;
                }

                switch (response.getType()) {
                    case CommandReq:
                        break;
                    case CommandResp:
                        output = this.handleCommandResp(response);
                        break;
                    case ScriptReq:
                        output = this.handleScriptReq(response);
                        break;
                    case ScriptResp:
                        break;
                }

                if (output != null) {
                    result.add(output);
                }
            }
        }

        // Sending output to ShellHandler
        byte[] data = SerializationUtils.serialize(result);
        buffer = ByteBuffer.wrap(data);
        try {
            this.shellPipe.sink().write(buffer);
        } catch (IOException e) {
            System.err.println("Cannot send commands to the pipe: " + e);
            return;
        }
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

}
