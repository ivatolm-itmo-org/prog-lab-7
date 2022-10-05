package src.moves;

import ru.ifmo.se.pokemon.Effect;
import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Stat;
import ru.ifmo.se.pokemon.StatusMove;
import ru.ifmo.se.pokemon.Type;

public class Screech extends StatusMove {
    public Screech() {
        super(Type.NORMAL, 0, 85);
    }

    protected void applyOppEffects(Pokemon p) {
        Effect e = new Effect().stat(Stat.DEFENSE, -2);
        p.addEffect(e);
    }

    protected String describe() {
        return "uses Screech";
    }
}
