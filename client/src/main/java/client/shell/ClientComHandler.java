package client.shell;

import java.util.LinkedList;

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

    // Commands to process
    private LinkedList<Command> commands;

    // Accumulated output from the commands
    private LinkedList<String> accumulatedOutput;

    /**
     * Constructs new {@code ComHandler} with provided arguments.
     *
     * @param com communicator for talking to server
     */
    public ClientComHandler(Com com, ContentManager contentManager) {
        super(com);
        this.contentManager = contentManager;
        this.accumulatedOutput = null;
    }

    /**
     * Sends commands to server for processing.
     * Collects received output and result of the command.
     */
    @Override
    public void process() {
        if (this.accumulatedOutput == null) {
            this.accumulatedOutput = new LinkedList<>();
        }

        for (Command command : this.commands) {
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
                    this.accumulatedOutput.add(output);
                }
            }
        }
    }

    /**
     * Sets commands for processing to {@code commands}.
     */
    public void setCommands(LinkedList<Command> commands) {
        this.commands = commands;
    }

    /**
     * Returns output of the processed commands.
     *
     * @return output of the commands
     */
    public LinkedList<String> getAccumulatedOutput() {
        LinkedList<String> result = this.accumulatedOutput;
        this.accumulatedOutput = null;
        return result;
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
