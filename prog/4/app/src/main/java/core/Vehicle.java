package core;

import java.util.ArrayList;

import core.skills.DriveSkill;

public abstract class Vehicle {
    private Position position;
    private String name;

    private int seatsNum;
    private int storageCapacity;

    private ArrayList<Human> passengers;
    private ArrayList<Thing> storage;

    public Vehicle(Position position, String name, int seatsNum, int storageCapacity) {
        this.position = position;
        this.name = name;
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
        return this.seatsNum > this.passengers.size();
    }

    public boolean hasEmptyStorage() {
        return this.storageCapacity > this.storage.size();
    }

    public Human getDriver() {
        for (Human passenger : this.passengers) {
            if (passenger instanceof DriveSkill) {
                return passenger;
            }
        }

        return null;
    }

    public void move(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return this.position;
    }

    public String getName() {
        return this.name;
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

    public String toString() {
        return this.name;
    }
}
