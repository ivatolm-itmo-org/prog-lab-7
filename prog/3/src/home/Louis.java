package home;

import core.Human;
import core.Position;
import core.Thing;
import core.skills.SeeSkill;
import core.skills.TalkSkill;

public class Louis extends Human implements TalkSkill, SeeSkill {
    private boolean sitting;

    public Louis(Position position, int age, int cash) {
        super(position, "Louis", age, cash);

        this.sitting = false;
    }

    public void talk(Human human) {
        for (int i = 0; i < 100; i++) {
            this.say();
        }

        TalkSkill other = (TalkSkill) human;
        for (int i = 0; i < 100; i++) {
            other.say();
        }
    }

    public String say() {
        return "bla-bla-bla";
    }

    public void wonder() {}

    public String notice() {
        return "Oh, are you okay?";
    }

    public void sitOn(Thing thing) {
        this.sitting = true;
    }

    public void getUp() {
        this.sitting = false;
    }
}
