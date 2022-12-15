package core;

import java.util.ArrayList;
import java.util.Arrays;

public class Room extends Area {
    private ArrayList<Human> humans;
    private ArrayList<Thing> things;

    public Room(ArrayList<Position> points) {
        super(points);

        this.humans = new ArrayList<>();
        this.things = new ArrayList<>();
    }

    public Room(Position point0, Position point1, Position point2, Position point3) {
        super(new ArrayList<>(Arrays.asList(point0, point1, point2, point3)));
    }

    public ArrayList<Human> getHumans() {
        return this.humans;
    }

    public ArrayList<Thing> getThings() {
        return this.things;
    }

    public void setHumans(ArrayList<Human> humans) {
        this.humans = humans;
    }

    public void setThings(ArrayList<Thing> things) {
        this.things = things;
    }
}
