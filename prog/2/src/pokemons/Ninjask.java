package src.pokemons;

import ru.ifmo.se.pokemon.Type;

import src.moves.Screech;

public class Ninjask extends Nincada {
    public Ninjask(String name) {
        super(name);
        super.setType(Type.BUG, Type.FLYING);

        super.addMove(
            new Screech()
        );

        // Levels:
        //  * Harden -- 0
        //  * X_Scissor -- 81
        //  * Scratch -- 0
        //  * Screech -- 0

        super.setLevel(81);
        super.setStats(61, 90, 45, 50, 50, 160);
    }
}
