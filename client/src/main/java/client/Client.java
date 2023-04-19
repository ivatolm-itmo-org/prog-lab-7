package client;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.util.HashMap;

import client.handler.ClientComHandler;
import client.handler.ClientShellHandler;
import client.handler.EventHandler;
import client.shell.ContentManager;
import core.handler.ChannelType;
import core.handler.InputHandler;

/**
 * Program for running client application.
 * This program includes
 * - connecting to the server
 * - getting input from the user
 * - processing command on the server
 * - printing output.
 * Takes two mandatory input arguments:
 *  ip and port of the server.
 *
 * @author ivatolm
 */
public class Client {

    /**
     * This method is a start of the program.
     * Checks number of argument passed to program and runs interactive shell.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Wrong number of input arguments.");
            return;
        }

        // String ip = args[0];
        // Integer port = null;
        // try {
        //     port = Integer.parseInt(args[1]);
        // } catch (NumberFormatException e) {
        //     System.out.println("Cannot parse port from argument: " + args[1]);
        //     return;
        // }

        // Com com;
        // try {
        //     com = new ClientSocketHandler(ip, port);
        // } catch (IOException e) {
        //     System.err.println("Cannot create socket: " + e);
        //     return;
        // }

        Pipe input_shell, shell_com, com_shell, com_socket;
        try {
            input_shell = Pipe.open();
            shell_com = Pipe.open();
            com_shell = Pipe.open();
            com_socket = Pipe.open();
        } catch (IOException e) {
            System.err.println("Cannot open pipe: " + e);
            return;
        }

        InputHandler inputHandler = new InputHandler(
            input_shell.sink()
        );

        ClientShellHandler shellHandler = new ClientShellHandler(
            new HashMap<ChannelType, SelectableChannel>() {{
                put(ChannelType.Input, input_shell.source());
                put(ChannelType.Com, com_shell.source());
            }},
            new HashMap<ChannelType, SelectableChannel>() {{
                put(ChannelType.Com, shell_com.sink());
            }}
        );

        ClientComHandler comHandler = new ClientComHandler(
            new HashMap<ChannelType, SelectableChannel>() {{
                put(ChannelType.Shell, shell_com.source());
            }},
            new HashMap<ChannelType, SelectableChannel>() {{
                put(ChannelType.Shell, com_shell.sink());
                put(ChannelType.Socket, com_socket.sink());
            }},
            new ContentManager("../res")
        );

        EventHandler eventHandler = null;
        try {
            eventHandler = new EventHandler(shellHandler, comHandler);
        } catch (IOException e) {
            System.err.println("Error occured while starting event handler: " + e);
            return;
        }

        Thread inputHandlerThread = new Thread(inputHandler);
        inputHandlerThread.start();

        eventHandler.run();

        try {
            inputHandlerThread.join();
        } catch (InterruptedException e) {
            System.err.println("Input thread failed to join.");
        }
    }

}
