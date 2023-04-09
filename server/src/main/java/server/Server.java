package server;

import java.net.SocketException;
import java.net.UnknownHostException;

import core.command.Command;
import core.models.humanBeing.HumanBeing;
import core.net.Com;
import core.net.packet.Packet;
import server.database.CSVDatabase;
import server.interpreter.Interpreter;
import server.net.ServerComUDP;
import server.runner.RecursionFoundException;
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
        } catch (SocketException e) {
            System.err.println("Cannot create socket: " + e);
            return;
        } catch (UnknownHostException e) {
            System.err.println("Cannot create socket. Unknown host: " + e);
            return;
        }

        System.out.println("Waiting for commands...");
        while (true) {
            Packet packet = com.receive();
            System.out.println("Command received!");

            switch (packet.getType()) {
                case Command:
                    try {
                        runner.addCommand((Command) packet.getData());
                        runner.run();
                    } catch (RecursionFoundException e) {
                        System.err.println("Recursion detected...");
                    }
                    break;
                case ScriptRequest:
                    break;
                case ScriptResponse:
                    break;
                default:
                    break;
            }
        }
    }

}
