package core;

public class Vehicle extends Entity {
    private int tankSize;
    private int seatsCnt;
    private int storageSize;
    private Thing[] storage;
    private Person[] persons;

    public Vehicle(String name, Position position) {
        super(name, position, true);
    }

    void start() {

    }

    void stop() {

    }

    void load(Thing[] things) {

    }

    Thing[] unload() {
        return this.storage;
    }
}
