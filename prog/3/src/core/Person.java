package core;

public class Person extends Entity {
    private int age;
    private int cash;
    private Business job;
    private Business[] businesses;

    public Person(String name, Position position) {
        super(name, position, true);
    }

    int getAge() {
        return this.age;
    }

    int getCash() {
        return this.cash;
    }

    Business getJob() {
        return this.job;
    }

    Business[] getBusinesses() {
        return this.businesses;
    }
}
