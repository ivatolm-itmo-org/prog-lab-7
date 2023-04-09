package client.net;

import java.util.LinkedList;

import core.command.Command;
import core.net.ClientCom;

public class ClientComUDP implements ClientCom {

    // Server ip
    private String ip;

    // Server port
    private Integer port;

    /**
     * Constructs new {@code ClientComUDP} with provided arguments.
     * 
     * @param ip server ip
     * @param port server port
     */
    public ClientComUDP(String ip, Integer port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * Implements {@code isAlive} method of {@code ClientCom}.
     */
    @Override
    public boolean isAlive() {
        throw new UnsupportedOperationException("Unimplemented method 'isAlive'");
    }

    /**
     * Implements {@code send} method of {@code ClientCom}.
     */
    @Override
    public void send(Command command) {
        throw new UnsupportedOperationException("Unimplemented method 'send'");
    }

    /**
     * Implements {@code sendScript} method of {@code ClientCom}.
     */
    @Override
    public void sendScript(LinkedList<Command> commands) {
        throw new UnsupportedOperationException("Unimplemented method 'sendScript'");
    }

    /**
     * Implements {@code receive} method of {@code ClientCom}.
     */
    @Override
    public String receive() {
        throw new UnsupportedOperationException("Unimplemented method 'receive'");
    }
   
}
