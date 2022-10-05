package src.pokemons;

import ru.ifmo.se.pokemon.Type;

import src.moves.EnergyBall;

public class Victreebel extends Weepinbell {
    public Victreebel(String name) {
        super(name);
        super.setType(Type.GRASS, Type.POISON);

        super.addMove(
            new EnergyBall()
        );

        // Levels:
        //  * DoubleTeam -- 32
        //  * SludgeBomb -- 36
        //  * StunSpore -- 17
        //  * EnergyBall -- 53


        super.setLevel(53);
        super.setStats(80, 105, 65, 100, 70, 70);
    }
}
