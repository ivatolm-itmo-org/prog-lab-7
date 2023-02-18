package com.ivatolm.app.interpreter;

import java.time.LocalDate;
import java.util.LinkedList;

import com.ivatolm.app.database.IDatabase;
import com.ivatolm.app.humanBeing.Car;
import com.ivatolm.app.humanBeing.Coordinates;
import com.ivatolm.app.humanBeing.HumanBeing;
import com.ivatolm.app.parser.Command;
import com.ivatolm.app.parser.arguments.Argument;

public class Interpreter {

    private IDatabase<HumanBeing> database;
    private LinkedList<HumanBeing> collection;

    public Interpreter(IDatabase<HumanBeing> database) {
        this.database = database;

        this.database.setDummyObject(new HumanBeing());

        LinkedList<HumanBeing> data = this.database.read();
        if (data != null) {
            this.collection = data;
        } else {
            this.collection = new LinkedList<>();
        }
    }

    public void exec(Command cmd) {
        System.out.println("Executing command...");
        System.out.println("  CMD: " + cmd.getName());
        System.out.println("  Args: ");
        LinkedList<Argument> args = cmd.getArgsValues();
        for (int i = 0; i < args.size(); i++) {
            System.out.println("    " + i + ": " + args.get(i).getValue());
        }
        System.out.println();

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

                LinkedList<Object> res = new LinkedList<>();
                
                res.add(System.currentTimeMillis()); // id
                res.add(args.get(0).getValue()); // name
                res.add(new Coordinates(args.get(1).getValue(),
                                        args.get(2).getValue())); // coordinates
                res.add(LocalDate.now()); // creationDate
                res.add(args.get(3).getValue()); // realHero
                res.add(args.get(4).getValue()); // hasToothpick
                res.add(args.get(5).getValue()); // impactSpeed
                res.add(args.get(6).getValue()); // soundtrackName
                res.add(args.get(7).getValue()); // minutesOfWaiting
                res.add(args.get(8).getValue()); // mood
                res.add(new Car(args.get(9).getValue(),
                                args.get(10).getValue())); // car

                HumanBeing instance = new HumanBeing(res);
                this.collection.add(instance);

                break;
            case "save":
                System.out.println("SAVE");
                this.database.write(this.collection);
                break;
            default:
                System.err.println("Unknown command.");
        }
    }

}
