package client;

import java.net.SocketException;
import java.net.UnknownHostException;

import client.net.ClientComUDP;
import client.shell.ContentManager;
import client.shell.Shell;
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
        } catch (SocketException e) {
            System.err.println("Cannot create socket: " + e);
            return;
        } catch (UnknownHostException e) {
            System.err.println("Cannot create socket. Unknown host: " + e);
            return;
        }

        ContentManager contentManager = new ContentManager("../res");

        Shell shell = new Shell(com, contentManager);
        shell.run();
        shell.close();
    }

}
