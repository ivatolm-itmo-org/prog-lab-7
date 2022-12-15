package core;

import java.util.ArrayList;

public class Street extends Area {
    private ArrayList<Thing> things;

    public Street(ArrayList<Position> points) {
        super(points);

        this.things = new ArrayList<>();
    }

    public ArrayList<Thing> getThings() {
        return this.things;
    }

    public void setThings(ArrayList<Thing> things) {
        this.things = things;
    }
}
