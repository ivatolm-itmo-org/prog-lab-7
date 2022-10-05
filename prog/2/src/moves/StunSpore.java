package src.moves;

import ru.ifmo.se.pokemon.Effect;
import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.StatusMove;
import ru.ifmo.se.pokemon.Type;

public class StunSpore extends StatusMove {
    public StunSpore() {
        super(Type.GRASS, 0, 75);
    }

    protected void applyOppEffects(Pokemon p) {
        Effect.paralyze(p);
    }

    protected String describe() {
        return "uses StunSpore";
    }
}
