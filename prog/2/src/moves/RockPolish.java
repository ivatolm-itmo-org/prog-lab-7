package src.moves;

import ru.ifmo.se.pokemon.Type;
import ru.ifmo.se.pokemon.Effect;
import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Stat;
import ru.ifmo.se.pokemon.StatusMove;

public class RockPolish extends StatusMove {
    public RockPolish() {
        super(Type.ROCK, 0, 100);
    }

    protected void applySelfEffects(Pokemon p) {
        Effect e = new Effect().stat(Stat.SPEED, +2);
        p.addEffect(e);
    }

    protected boolean checkAccuracy(Pokemon att, Pokemon def) {
        return true;
    }

    protected String describe() {
        return "uses RockPolish";
    }
}
