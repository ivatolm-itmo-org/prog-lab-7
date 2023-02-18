package com.ivatolm.app.shell;

import java.util.NoSuchElementException;
import java.util.Scanner;

import com.ivatolm.app.database.CSVDatabase;
import com.ivatolm.app.database.IDatabase;
import com.ivatolm.app.humanBeing.HumanBeing;
import com.ivatolm.app.interpreter.Interpreter;
import com.ivatolm.app.parser.Command;
import com.ivatolm.app.parser.Parser;
import com.ivatolm.app.utils.CommandParserException;

public class Shell {

    private Scanner scanner;
    private Parser parser;
    private Interpreter interpreter;

    public Shell(String filename) {
        IDatabase<HumanBeing> database = new CSVDatabase<HumanBeing>(filename);

        this.scanner = new Scanner(System.in);
        this.parser = new Parser();
        this.interpreter = new Interpreter(database);
    }

    public void run() {
        try {
            while (true) {
                System.out.print(": ");
                String input = this.scanner.nextLine();

                boolean result = false;
                try {
                    result = this.parser.parse(input);                
                } catch (CommandParserException e) {
                    System.err.println(e.getMessage());
                    continue;
                }

                if (result == false) {
                    Command cmd = this.parser.getCurrentCommand();
                    int argCnt = this.parser.getCurrentArgumentsCnt();

                    String greeting = "Enter" + " " + "'" + cmd.getArgument(argCnt).getGreetingMsg() + "'";
                    System.out.print(greeting);
                } else {
                    Command cmd = this.parser.getCurrentCommand();

                    // Sending to server for processing
                    this.interpreter.exec(cmd);
                    System.out.println("Command completed.");
                }
            }
        } catch(NoSuchElementException e) {
            System.out.println("\nExiting by Ctrl-D (EOF)");
        }
    }

    public void close() {
        this.scanner.close();
    }

}
