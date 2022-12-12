package core;

import core.skills.*;

import java.util.Random;

public abstract class ChildHuman extends Human implements PassengerSkill, SeeSkill, TalkSkill, SitSkill {
    private boolean sitting;

    public ChildHuman(Position position, String name, int age, int cash) {
        super(position, name, age, cash);
        this.sitting = false;
    }

    public void sitInVehicle(Vehicle vehicle) {
        this.sitting = true;
        this.position = vehicle.getPosition();
    }

    public void leaveFromVehicle(Vehicle vehicle) {
        this.sitting = false;
        this.position = vehicle.getPosition();
    }

    public String notice() {
        return "Oh!";
    }

    public void wonder() {
        System.out.println("Wondering around...");
    }

    public void talk(Human human) {
        Random random = new Random();

        int dialog_length = random.nextInt() % 100;

        for (int i = 0; i < dialog_length; i++) {
            this.say();
        }

        TalkSkill other = (TalkSkill) human;
        for (int i = 0; i < dialog_length; i++) {
            other.say();
        }
    }

    public String say() {
        return "Gibberish";
    }

    public void sitOn(Thing thing) {
        this.sitting = true;
        this.position = thing.getPosition();
    }

    public void getUp() {
        if (this.sitting) {
            this.sitting = false;
        }
    }
}
