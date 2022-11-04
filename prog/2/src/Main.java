package src;

import ru.ifmo.se.pokemon.Battle;

import src.pokemons.*;

public class Main {
    public static void main(String args[]) {
        Battle b = new Battle();

        Bellsprout p1 = new Bellsprout("1");
        Nincada p2 = new Nincada("2");
        Ninjask p3 = new Ninjask("3");

        Regice p4 = new Regice("4");
        Victreebel p5 = new Victreebel("5");
        Weepinbell p6 = new Weepinbell("6");

        b.addAlly(p1);
        b.addAlly(p2);
        b.addAlly(p3);

        b.addFoe(p4);
        b.addFoe(p5);
        b.addFoe(p6);

        b.go();
    }
}
