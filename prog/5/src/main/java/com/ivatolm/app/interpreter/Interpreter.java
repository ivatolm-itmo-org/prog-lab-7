package com.ivatolm.app.interpreter;

import com.ivatolm.app.parser.Command;
import com.ivatolm.app.parser.arguments.Argument;

public class Interpreter {

    public void exec(Command cmd) {
        switch (cmd.getName()) {
            case "help":
                System.out.println("HELP");
                break;
            case "info":
                System.out.println("INFO");
                break;
            case "show":
                System.out.println("SHOW");
                break;
            case "add":
                System.out.println("ADD");
                for (Argument arg : cmd.getArgsValues()) {
                    System.out.println(arg.getValue());
                }
                break;
            default:
                System.err.println("Unknown command.");
        }
    }

}
