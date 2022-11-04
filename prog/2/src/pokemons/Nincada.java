package src.pokemons;

import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Type;

import src.moves.Harden;
import src.moves.Scratch;
import src.moves.XScissor;

public class Nincada extends Pokemon {
    public Nincada(String name) {
        super(name, 1);
        super.setType(Type.BUG, Type.GROUND);

        super.setMove(
            new Harden(),
            new XScissor(),
            new Scratch()
        );

        // Levels:
        //  * Harden -- 0
        //  * X_Scissor -- 81
        //  * Scratch -- 0

        super.setLevel(81);
        super.setStats(31, 45, 90, 30, 30, 40);
    }
}
