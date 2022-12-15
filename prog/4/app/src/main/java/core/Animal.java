package core;

import core.skills.SleepSkill;

public abstract class Animal extends Entity implements SleepSkill {
    protected int age;

    protected boolean sleeping;

    public Animal(Position position, String name, int age) {
        super(position, name);
        this.age = age;

        this.sleeping = false;
    }

    public final void move(Place place) {
        this.position = place.getPosition();
    }

    void react() { }

    int getAge() {
        return this.age;
    }

    public void sleep(Place place) {
        this.position = place.getPosition();
        this.sleeping = true;
    }

    public void wakeUp() {
        if (this.sleeping) {
            this.sleeping = false;
        }
    }
}
