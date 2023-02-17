package com.ivatolm.app.shell;

import java.util.Scanner;

import com.ivatolm.app.interpreter.Interpreter;
import com.ivatolm.app.parser.Command;
import com.ivatolm.app.parser.CommandParserException;
import com.ivatolm.app.parser.Parser;

public class Shell {

    public void run() {
        Scanner scanner = new Scanner(System.in);
        Parser parser = new Parser();

        Interpreter interpreter = new Interpreter();

        while (true) {
            System.out.print(": ");
            String input = scanner.nextLine();

            boolean result = false;
            try {
                result = parser.parse(input);                
            } catch (CommandParserException e) {
                System.err.println(e.getMessage());
                continue;
            }

            if (result == false) {
                Command cmd = parser.getCurrentCommand();
                int argCnt = parser.getCurrentArgumentsCnt();

                String greeting = "Enter" + " " + "'" + cmd.getArgument(argCnt).getGreetingMsg() + "'";
                System.out.print(greeting);
            } else {
                Command cmd = parser.getCurrentCommand();

                interpreter.exec(cmd);
                // Sending to server for processing
                System.out.println("Command completed.");
            }
        }

        // scanner.close();
    }

}
