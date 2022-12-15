package core;

public abstract class Human extends Animal {
    public Human(Position position, String name, int age) {
        super(position, name, age);
    }

    public boolean equals(Object object) {
        if (object instanceof Human human) {
            return this.name.equals(human.name);
        }
        return false;
    }
}
