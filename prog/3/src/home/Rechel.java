package home;

import core.Human;
import core.Position;
import core.Thing;
import core.skills.TalkSkill;

public class Rechel extends Human implements TalkSkill {
    private boolean sitting;

    public Rechel(Position position, int age, int cash) {
        super(position, "Rechel", age, cash);

        this.sitting = false;
    }

    public void talk(Human human) {
        for (int i = 0; i < 25; i++) {
            this.say();
        }

        TalkSkill other = (TalkSkill) human;
        for (int i = 0; i < 25; i++) {
            other.say();
        }
    }

    public String say() {
        return "blo-blo-blo";
    }

    public void sitOn(Thing thing) {
        this.sitting = true;
    }

    public void getUp() {
        this.sitting = false;
    }
}
