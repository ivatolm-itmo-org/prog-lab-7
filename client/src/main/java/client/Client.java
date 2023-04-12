package client;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.Pipe;

import client.net.ClientComUDP;
import client.shell.ClientComHandler;
import client.shell.ClientShellHandler;
import client.shell.ContentManager;
import core.handler.ComHandler;
import core.handler.ShellHandler;
import core.net.Com;

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

        String ip = args[0];
        Integer port = null;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Cannot parse port from argument: " + args[1]);
            return;
        }


        Com com;
        try {
            com = new ClientComUDP(ip, port);
        } catch (IOException e) {
            System.err.println("Cannot create socket: " + e);
            return;
        }

        ContentManager contentManager = new ContentManager("../res");

        Pipe com_shell_pipe;
        try {
            com_shell_pipe = Pipe.open();
        } catch (IOException e) {
            System.err.println("Cannot open pipe: " + e);
            return;
        }
        ComHandler comHandler = new ClientComHandler(com, contentManager, com_shell_pipe);
        ShellHandler shellHandler = new ClientShellHandler(com_shell_pipe);

        Thread shellThread = new Thread(shellHandler);
        shellThread.start();

        comHandler.process();

        try {
            shellThread.join();
        } catch (InterruptedException e) {
            System.err.println("Shell thread failed to join.");
        }
    }

}
