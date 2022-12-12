package core;

import core.skills.DrinkSkill;
import core.skills.PaySkill;

public abstract class AdultHuman extends ChildHuman implements DrinkSkill, PaySkill {
    private boolean isSober;

    public AdultHuman(Position position, String name, int age, int cash) {
        super(position, name, age, cash);
        this.isSober = true;
    }

    public void drink() {
        this.isSober = false;
    }

    public void drinkWith(AdultHuman human) {
        if (this.position.equals(human.position)) {
            this.isSober = false;
            this.talk(human);
        } else {
            System.out.println("Ugh, wish I could drink with " + human.name);
        }
    }

    public void giveCash(AdultHuman human, int cash) {
        if (this.cash >= cash) {
            this.cash -= cash;
            human.receiveCash(cash);
        } else {
            System.out.println("Bruh, no money. I'll run to ATM now...");
        }
    }

    public void receiveCash(int cash) {
        this.cash += cash;
    }
}
