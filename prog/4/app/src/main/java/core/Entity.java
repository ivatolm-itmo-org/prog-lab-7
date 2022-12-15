package core;

public abstract class Entity {
    protected Position position;
    protected String name;

    public Entity(Position position, String name) {
        this.position = position;
        this.name = name;
    }

    Position getPosition() {
        return this.position;
    }

    String getName() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }
}
