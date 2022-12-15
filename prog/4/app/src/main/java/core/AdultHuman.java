package core;

import core.skills.DrinkSkill;
import core.skills.PaySkill;

import java.util.ArrayList;

public abstract class AdultHuman extends ChildHuman implements DrinkSkill, PaySkill {
    protected ArrayList<Business> businesses;
    protected Business job;

    private boolean isSober;

    public AdultHuman(Position position, String name, int age, int cash) {
        super(position, name, age, cash);
        this.businesses = new ArrayList<>();
        this.isSober = true;
    }

    public ArrayList<Business> getBusinesses() {
        return this.businesses;
    }

    public Business getBusiness(String name) {
        for (Business business : this.businesses) {
            if (business.getName().equals(name)) {
                return business;
            }
        }

        return null;
    }

    public Business getJob() {
        return this.job;
    }

    public int getCash() {
        return this.cash;
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

    public boolean isSober() {
        // TODO: Lazy update here

        return this.isSober;
    }

    public String toString() {
        String result = "";

        result += this.name + ": ";
        result += '\n';
        result += "  " + "position: " + this.position;
        result += '\n';
        result += "  " + "sitting: " + this.sitting;
        result += '\n';
        result += "  " + "sober: " + this.isSober;

        return result;
    }
}
