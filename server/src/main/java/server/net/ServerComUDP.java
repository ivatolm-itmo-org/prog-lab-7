package server.net;

import java.util.LinkedList;

import core.command.Command;
import core.net.ServerCom;

public class ServerComUDP implements ServerCom {

    /**
     * Implements {@code isAlive} method of {@code ServerCom}.
     */
    @Override
    public boolean isAlive() {
        throw new UnsupportedOperationException("Unimplemented method 'isAlive'");
    }

    /**
     * Implements {@code send} method of {@code ServerCom}.
     */
    @Override
    public void send(String output) {
        throw new UnsupportedOperationException("Unimplemented method 'send'");
    }

    /**
     * Implements {@code receive} method of {@code ServerCom}.
     */
    @Override
    public Command receive() {
        throw new UnsupportedOperationException("Unimplemented method 'receive'");
    }

    /**
     * Implements {@code receiveScript} method of {@code ServerCom}.
     */
    @Override
    public LinkedList<Command> receiveScript(String filename) {
        throw new UnsupportedOperationException("Unimplemented method 'receiveScript'");
    }
    
}
