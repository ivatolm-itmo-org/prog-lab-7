package com.ivatolm.app;

import com.ivatolm.app.shell.Shell;

/**
 * Program for running interactive shell.
 * Accepts one environment argument -- filename of database.
 * If the argument count is not equals to one, then finishes execution.
 *
 * @author ivatolm
 */
public class App  {

    /**
     * This method is a start of the program.
     * Checks number of argument passed to program and runs interactive shell.
     *
     * @param args command line argumets
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Wrong number of input arguments.");
            return;
        }

        Shell shell = new Shell(args[0]);
        shell.run();
        shell.close();
    }

}
