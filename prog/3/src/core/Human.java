package core;

import java.util.ArrayList;

public abstract class Human extends Animal {
    protected ArrayList<Business> businesses;
    protected Business job;
    protected int cash;

    public Human(Position position, String name, int age, int cash) {
        super(position, name, age);
    }

    public ArrayList<Business> getBusinesses() {
        return this.businesses;
    }

    public Business getBusiness() {
        return this.job;
    }

    public int getCash() {
        return this.cash;
    }

    public boolean equals(Object object) {
        if (object instanceof Human) {
            Human human = (Human) object;
            return this.name == human.name;
        }
        return false;
    }
}
