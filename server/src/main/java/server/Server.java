package server;

import java.io.IOException;

import core.models.humanBeing.HumanBeing;
import core.net.Com;
import server.database.CSVDatabase;
import server.handler.ComHandler;
import server.handler.EventHandler;
import server.handler.Shell;
import server.interpreter.Interpreter;
import server.net.ServerComUDP;
import server.runner.Runner;

/**
 * Program for running server application.
 * This program includes
 * - starting a server
 * - getting input from the client
 * - processing command
 * - sending output to the client.
 * Takes three mandatory input arguments:
 *  filename of the database, ip and port of the server.
 *
 * @author ivatolm
 */
public class Server
{

    /**
     * This method is a start of the program.
     * Checks number of argument passed to program and runs interactive shell.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Wrong number of input arguments.");
            return;
        }

        String databaseFilename = args[0];

        String ip = args[1];
        Integer port = null;
        try {
            port = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("Cannot parse port from argument: " + args[2]);
            return;
        }

        CSVDatabase<HumanBeing> database = new CSVDatabase<>(databaseFilename);
        Interpreter interpreter = new Interpreter(database);
        Runner runner = new Runner(interpreter);

        Com com;
        try {
            com = new ServerComUDP(ip, port);
        } catch (IOException e) {
            System.err.println("Cannot create socket: " + e);
            return;
        }

        ComHandler comHandler = new ComHandler(com, runner);
        Shell shell = new Shell(runner);

        EventHandler eventHandler = null;
        try {
            eventHandler = new EventHandler(comHandler, shell);
        } catch (IOException e) {
            System.err.println("Error occured while starting event handler: " + e);
            return;
        }

        Thread shellThread = new Thread(shell);
        shellThread.start();

        eventHandler.run();

        try {
            shellThread.join();
        } catch (InterruptedException e) {
            System.err.println("Shell thread failed to join.");
        }
    }

}
