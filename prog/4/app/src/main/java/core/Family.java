package core;

import java.util.ArrayList;

public class Family {
    private ArrayList<Human> humans;
    private Place place;

    public Family(ArrayList<Human> humans, Place place) {
        this.humans = humans;
        this.place = place;
    }

    public ArrayList<Human> getHumans() {
        return this.humans;
    }

    public Place getPlace() {
        return this.place;
    }

    public void setHumans(ArrayList<Human> humans) {
        this.humans = humans;
    }

    public void setPlace(Place place) {
        this.place = place;

        for (Human human : this.humans) {
            human.move(place);
        }
    }
}
