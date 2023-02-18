package com.ivatolm.app.parser;

import java.util.Arrays;
import java.util.LinkedList;

import com.ivatolm.app.parser.arguments.ArgCheck;
import com.ivatolm.app.parser.arguments.Argument;
import com.ivatolm.app.utils.CommandParserException;

public class Parser {

    private boolean waitingArgs = false;
    
    private Command cmd;
    private LinkedList<Argument> args;

    private Command result;

    public boolean parse(String input) throws CommandParserException {
        // Splitting received input
        LinkedList<String> inputArgs = new LinkedList<>(Arrays.asList(input.split("\n", 0)));

        if (inputArgs.size() == 0) {
            throw new CommandParserException("Nothing to parse.");
        }

        // New command
        if (!this.waitingArgs) {
            // Resetting data from previous cmd
            this.cmd = Command.UNKNOWN;
            this.args = new LinkedList<>();

            // Checking if given command exists
            String cmdLabel = inputArgs.get(0);
            for (Command cmd : Command.values()) {
                if (cmdLabel.equals(cmd.getName())) {
                    this.cmd = cmd;
                    break;
                }
            }

            if (this.cmd.equals(Command.UNKNOWN)) {
                throw new CommandParserException("Provided command doesn't exist.");
            }

            // Removing command label from splitted input
            inputArgs.pop();

            // Setting flag for argument-parsing
            this.waitingArgs = true;
        }

        if (this.waitingArgs) {
            // Validating arguments
            int argCnt = Math.min(this.cmd.getArgsCount() - this.args.size(),
                                  inputArgs.size());

            for (int i = 0; i < argCnt; i++) {
                String inputArg = inputArgs.get(i);
                int argId = this.args.size();

                System.out.println("You passed: " + inputArg);

                Argument arg = this.cmd.getArgument(argId);

                ArgCheck f = arg.getCheck();
                if (f.check(inputArg)) {
                    arg.parse(inputArg);
                    this.args.add(arg);
                } else {
                    throw new CommandParserException(arg.getErrorMsg());
                }
            }

            if (this.cmd.getArgsCount() - this.args.size() == 0) {
                // Setting flag for new command
                this.waitingArgs = false;

                // Create command
                this.result = this.cmd;
                this.result.setArgs(this.args);

                return true;
            }
        }

        return false;
    }

    public Command getResult() {
        return this.result;
    }

    public Command getCurrentCommand() {
        return this.cmd;
    }

    public int getCurrentArgumentsCnt() {
        return this.args.size();
    }

}
