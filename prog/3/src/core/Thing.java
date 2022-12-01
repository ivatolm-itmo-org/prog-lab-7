package core;

public abstract class Thing {
    private Position position;
    private String name;

    public Thing(Position position, String name) {
        this.position = position;
        this.name = name;
    }

    public Position gePosition() {
        return this.position;
    }

    public String getName() {
        return this.name;
    }
}
