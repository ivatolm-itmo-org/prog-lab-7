package core;
import java.util.Random;

public abstract class Cat extends Animal {
    private int lifesCnt;

    public Cat(Position position, String name, int age) {
        super(position, name, age);

        this.lifesCnt = 9;
    }

    public boolean die() {
        this.lifesCnt--;

        if (this.lifesCnt == 0) {
            System.out.println("Meeeeoww");
            System.out.println("Oh no! Kitty is dead!");
        }

        return this.lifesCnt == 0;
    }

    public void react() {
        Random random = new Random();
        switch (random.nextInt() % 4) {
            case 0:
                System.out.println("Meow?");
                break;
            case 1:
                System.out.println("Meeoow!");
                break;
            case 2:
                System.out.println("MEOW!?");
                break;
            default:
                // no reaction
        }
    }
}
