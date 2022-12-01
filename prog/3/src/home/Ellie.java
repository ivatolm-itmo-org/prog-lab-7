package home;

import core.Human;
import core.Position;
import core.skills.SleepSkill;

public class Ellie extends Human implements SleepSkill {
    private boolean sleeping;

    public Ellie(Position position, int age, int cash) {
        super(position, "Ellie", age, cash);

        this.sleeping = false;
    }

    public void sleep(Position position) {
        this.position = position;
        this.sleeping = true;
    }
}
