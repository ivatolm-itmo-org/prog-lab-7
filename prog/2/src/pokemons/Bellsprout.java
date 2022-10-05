package src.pokemons;

import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Type;
import src.moves.DoubleTeam;
import src.moves.SludgeBomb;

public class Bellsprout extends Pokemon {
    public Bellsprout(String name) {
        super(name, 1);
        super.setType(Type.GRASS, Type.POISON);

        super.setMove(
            new DoubleTeam(),
            new SludgeBomb()
        );

        // Levels:
        //  * DoubleTeam -- 32
        //  * SludgeBomb -- 36

        super.setLevel(36);
        super.setStats(50, 75, 35, 70, 30, 40);
    }
}
