package server;

import java.io.IOException;
import java.nio.channels.Pipe;

import core.handler.ChannelType;
import core.handler.HandlerChannel;
import core.handler.HandlerChannels;
import core.handler.InputHandler;
import core.models.humanBeing.HumanBeing;
import core.net.Com;
import server.balancer.Balancer;
import server.database.CSVDatabase;
import server.handler.ServerComHandler;
import server.handler.ServerEventHandler;
import server.handler.ServerShellHandler;
import server.handler.ServerSocketHandler;
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
        Thread.currentThread().setName("server");

        if (args.length != 3) {
            System.out.println("Wrong number of input arguments.");
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
        Balancer loadBalancer = new Balancer(4);

        Com com;
        try {
            com = new ServerComUDP(ip, port);
        } catch (IOException e) {
            System.out.println("Cannot create socket: " + e);
            return;
        }

        Pipe input_shell, shell_com, com_shell;
        try {
            input_shell = Pipe.open();
            shell_com = Pipe.open();
            com_shell = Pipe.open();
        } catch (IOException e) {
            System.out.println("Cannot open pipe: " + e);
            return;
        }

        InputHandler inputHandler = new InputHandler(
            input_shell.sink()
        );

        ServerShellHandler shellHandler = new ServerShellHandler(
            new HandlerChannels() {{
                add(new HandlerChannel(ChannelType.Input, input_shell.source()));
                add(new HandlerChannel(ChannelType.Com, com_shell.source()));
            }},
            new HandlerChannels() {{
                add(new HandlerChannel(ChannelType.Com, shell_com.sink()));
            }}
        );

        ServerComHandler shellComHandler = new ServerComHandler(
            new HandlerChannels() {{
                add(new HandlerChannel(ChannelType.Shell, shell_com.source()));
            }},
            new HandlerChannels() {{
                add(new HandlerChannel(ChannelType.Shell, com_shell.sink()));
            }},
            runner,
            ChannelType.Shell
        );

        ServerSocketHandler socketHandler;
        try {
            socketHandler = new ServerSocketHandler(
                new HandlerChannels() {{

                }},
                new HandlerChannels() {{

                }},
                com
            );
        } catch (IOException e) {
            System.out.println("Cannot create socket handler: " + e);
            return;
        }

        ServerEventHandler eventHandler = null;
        try {
            eventHandler = new ServerEventHandler(
                shellHandler, shellComHandler, socketHandler,
                runner, loadBalancer
            );
        } catch (IOException e) {
            System.out.println("Error occured while starting event handler: " + e);
            return;
        }

        Thread inputHandlerThread = new Thread(inputHandler);
        inputHandlerThread.start();

        eventHandler.run();

        inputHandler.close();
        loadBalancer.close();

        try {
            inputHandlerThread.join();
        } catch (InterruptedException e) {
            System.out.println("Shell thread failed to join.");
        }
    }

}
