package core;

public class Thing extends Entity {
    public Thing(Position position, String name) {
        super(position, name);
    }

    public Thing(Place place, String name) {
        super(place.getPosition(), name);
    }
}
