package core;
public abstract class Animal {
    protected Position position;
    protected String name;
    protected int age;

    public Animal(Position position, String name, int age) {
        this.position = position;
        this.name = name;
        this.age = age;
    }

    final void move(Place place) {
        this.position = place.getPosition();
    }

    void react() { }

    Position getPosition() {
        return this.position;
    }

    String getName() {
        return this.name;
    }

    int getAge() {
        return this.age;
    }
}
