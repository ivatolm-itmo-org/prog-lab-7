package src.moves;

import ru.ifmo.se.pokemon.Type;
import ru.ifmo.se.pokemon.Effect;
import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.SpecialMove;
import ru.ifmo.se.pokemon.Status;

public class SludgeBomb extends SpecialMove {
    public SludgeBomb() {
        super(Type.POISON, 90, 100);
    }

    protected void applyOppEffects(Pokemon p) {
        Effect e = new Effect().chance(0.3).condition(Status.POISON);
        p.addEffect(e);
    }

    protected String describe() {
        return "uses SludgeBomb";
    }
}
