package client;

import java.io.IOException;
import java.nio.channels.Pipe;

import client.handler.ClientComHandler;
import client.handler.ClientShellHandler;
import client.handler.ClientSocketHandler;
import client.handler.EventHandler;
import client.net.ClientComUDP;
import client.shell.ContentManager;
import core.handler.ChannelType;
import core.handler.HandlerChannel;
import core.handler.HandlerChannels;
import core.handler.InputHandler;
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
        Thread.currentThread().setName("client");

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

        Pipe input_shell, shell_com, com_shell, com_socket, socket_com;
        try {
            input_shell = Pipe.open();
            shell_com = Pipe.open();
            com_shell = Pipe.open();
            com_socket = Pipe.open();
            socket_com = Pipe.open();
        } catch (IOException e) {
            System.err.println("Cannot open pipe: " + e);
            return;
        }

        InputHandler inputHandler = new InputHandler(
            input_shell.sink()
        );

        ClientComHandler comHandler = new ClientComHandler(
            new HandlerChannels() {{
                add(new HandlerChannel(ChannelType.Shell, shell_com.source()));
                add(new HandlerChannel(ChannelType.Network, socket_com.source()));
            }},
            new HandlerChannels() {{
                add(new HandlerChannel(ChannelType.Shell, com_shell.sink()));
                add(new HandlerChannel(ChannelType.Network, com_socket.sink()));
            }},
            new ContentManager("../client/src/main/resources/scripts")
        );

        ClientShellHandler shellHandler = new ClientShellHandler(
            new HandlerChannels() {{
                add(new HandlerChannel(ChannelType.Input, input_shell.source()));
                add(new HandlerChannel(ChannelType.Com, com_shell.source()));
            }},
            new HandlerChannels() {{
                add(new HandlerChannel(ChannelType.Com, shell_com.sink()));
            }}
        );

        ClientSocketHandler socketHandler;
        try {
            socketHandler = new ClientSocketHandler(
                new HandlerChannels() {{
                    add(new HandlerChannel(ChannelType.Com, com_socket.source()));
                }},
                new HandlerChannels() {{
                    add(new HandlerChannel(ChannelType.Com, socket_com.sink()));
                }},
                com
            );
        } catch (IOException e) {
            System.err.println("Cannot create socket handler: " + e);
            return;
        }

        EventHandler eventHandler = null;
        try {
            eventHandler = new EventHandler(shellHandler, comHandler, socketHandler);
        } catch (IOException e) {
            System.err.println("Error occured while starting event handler: " + e);
            return;
        }

        Thread inputHandlerThread = new Thread(inputHandler);
        inputHandlerThread.start();

        eventHandler.run();

        inputHandler.close();

        try {
            inputHandlerThread.join();
        } catch (InterruptedException e) {
            System.err.println("Input thread failed to join.");
        }
    }

}
