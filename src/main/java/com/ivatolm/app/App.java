package com.ivatolm.app;

import com.ivatolm.app.models.coordinates.Coordinates;
import com.ivatolm.app.shell.Shell;

public class App  {

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
