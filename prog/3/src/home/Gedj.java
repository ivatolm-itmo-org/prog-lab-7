package home;

import core.Human;
import core.Position;
import core.skills.SleepSkill;

public class Gedj extends Human implements SleepSkill {
    private boolean sleepting;

    public Gedj(Position position, int age, int cash) {
        super(position, "Gedj", age, cash);

        this.sleepting = false;
    }

    public void sleep(Position position) {
        this.position = position;

        this.sleepting = true;
    }
}
