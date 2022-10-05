package src.pokemons;

import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Type;

import src.moves.FocusBlast;
import src.moves.Rest;
import src.moves.RockPolish;
import src.moves.Swagger;

public class Regice extends Pokemon {
    public Regice(String name) {
        super(name, 1);
        super.setType(Type.ICE);

        super.setMove(
            new FocusBlast(),
            new RockPolish(),
            new Rest(80),
            new Swagger()
        );

        // Levels:
        //  * FocusBlast -- 52
        //  * RockPolush -- 69
        //  * Rest -- 44
        //  * Swagger -- 87

        super.setLevel(87);
        super.setStats(80, 50, 100, 100, 200, 50);
    }
}
