package src.pokemons;

import ru.ifmo.se.pokemon.Type;

import src.moves.StunSpore;

public class Weepinbell extends Bellsprout {
    public Weepinbell(String name) {
        super(name);
        super.setType(Type.GRASS, Type.POISON);

        super.addMove(
            new StunSpore()
        );

        // Levels:
        //  * DoubleTeam -- 32
        //  * SludgeBomb -- 36
        //  * StunSpore -- 17

        super.setLevel(36);
        super.setStats(65, 90, 50, 85, 45, 55);
    }
}
