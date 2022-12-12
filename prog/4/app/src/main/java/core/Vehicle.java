package core;

import java.util.ArrayList;

public abstract class Vehicle {
    private Position position;

    private int seatsNum;
    private int storageCapacity;

    private ArrayList<Human> passengers;
    private ArrayList<Thing> storage;

    public Vehicle(Position position, int seatsNum, int storageCapacity) {
        this.position = position;
        this.seatsNum = seatsNum;
        this.storageCapacity = storageCapacity;
        this.passengers = new ArrayList<Human>();
        this.storage = new ArrayList<Thing>();
    }

    public void addPassenger(Human human) {
        if (this.hasEmptySeats()) {
            this.passengers.add(human);
        }
    }

    public void removePassenger(Human human) {
        this.passengers.remove(human);
    }

    public void addStorageUnit(Thing thing) {
        if (this.hasEmptyStorage()) {
            this.storage.add(thing);
        }
    }

    public void removeStorageUnit(Thing thing) {
        this.storage.remove(thing);
    }

    public boolean hasEmptySeats() {
        return this.seatsNum == this.passengers.size();
    }

    public boolean hasEmptyStorage() {
        return this.storageCapacity == this.storage.size();
    }

    public Human getDriver() {
        if (this.passengers.size() > 0) {
            return this.passengers.get(0);
        } else {
            return null;
        }
    }

    public void move(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return this.position;
    }

    public int getSeatsNum() {
        return this.seatsNum;
    }

    public int getStorageCapacity() {
        return this.storageCapacity;
    }

    public ArrayList<Human> getPassengers() {
        return this.passengers;
    }

    public ArrayList<Thing> getStorage() {
        return this.storage;
    }
}
