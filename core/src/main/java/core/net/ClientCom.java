package core.net;

import java.util.LinkedList;

import core.command.Command;

/**
 * Interface for client side network communication.
 * 
 * @author ivatolm
 */
public interface ClientCom {

    /**
     * Checks for connection being alive.
     * 
     * @return true if connection is alive, else false
     */
    boolean isAlive();

    /**
     * Sends command to the server for processing.
     * 
     * @param command command to execute by the server
     */
    void send(Command command);

    /**
     * Sends script commands to the server for processing.
     * 
     * @param commands script commands to execute by the server
     */
    void sendScript(LinkedList<Command> commands);

    /**
     * Receive command output from the server.
     * 
     * @return output of the command produced by the server
     */
    String receive();

}
