package client.shell;

import java.util.LinkedList;

import core.command.Command;
import core.net.Com;
import core.net.packet.Packet;
import core.net.packet.PacketType;

/**
 * Class for handling client side communication of client and server.
 *
 * @author ivatolm
 */
public class ComHandler {

    // Communicator for talking to server
    private Com com;

    // Content Manager
    private ContentManager contentManager;

    /**
     * Constructs new {@code ComHandler} with provided arguments.
     *
     * @param com communicator for talking to server
     */
    public ComHandler(Com com, ContentManager contentManager) {
        this.com = com;
        this.contentManager = contentManager;
    }

    /**
     * Sends commands to server for processing.
     * Sends commands one-by-one and collects received
     * output. If command requires other commands to be
     * executed, then sends commands to server.
     *
     * @param commands commands for processing
     * @return output of each command
     */
    public String[] processCommands(LinkedList<Command> commands) {
        LinkedList<String> result = new LinkedList<>();

        for (Command command : commands) {
            Packet request = new Packet(PacketType.CommandReq, command);
            this.com.send(request);

            String output = null;
            boolean finished = false;
            while (!finished) {
                Packet response = this.com.receive();
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
                    finished = true;
                }
            }
        }

        return result.toArray(new String[0]);
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
