package com.ivatolm.app.net;

import java.util.LinkedList;

import com.ivatolm.app.parser.Command;

/**
 * Interface for server side network communication.
 * 
 * @author ivatolm
 */
public interface ServerCom {

    /**
     * Checks for connection being alive.
     * 
     * @return true if connection is alive, else false
     */
    boolean isAlive();

    /**
     * Sends command output to the client.
     * 
     * @param output output of the command produced by the server
     */
    void send(String output);

    /**
     * Receive command for processing from the client.
     * 
     * @return command to execute by the server
     */
    Command receive();

    /**
     * Receive script commands for processing from the client.
     * 
     * @param filename filename of the script
     * @return commands of the script
     */
    LinkedList<Command> receiveScript(String filename);

}
