package core;

class Entity {
    private String name;
    private Position position;
    private boolean isMovable;

    public Entity(String name, Position position, boolean isMovable) {
        this.name = name;
        this.position = position;
        this.isMovable = isMovable;
    }

    String getName() {
        return this.name;
    }

    Position getPosition() {
        return this.position;
    }

    void move(Position position) {
        if (this.isMovable) {
            System.out.println("Moving!");
        } else {
            System.out.println("Cannot move!");
        }
    }
}
