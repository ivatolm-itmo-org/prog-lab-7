package core;

import core.skills.SleepSkill;

public abstract class Animal implements SleepSkill {
    protected Position position;
    protected String name;
    protected int age;

    protected boolean sleeping;

    public Animal(Position position, String name, int age) {
        this.position = position;
        this.name = name;
        this.age = age;

        this.sleeping = false;
    }

    public final void move(Place place) {
        this.position = place.getPosition();
    }

    void react() { }

    Position getPosition() {
        return this.position;
    }

    String getName() {
        return this.name;
    }

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
