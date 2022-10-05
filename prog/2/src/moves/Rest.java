package src.moves;

import ru.ifmo.se.pokemon.Effect;
import ru.ifmo.se.pokemon.Pokemon;
import ru.ifmo.se.pokemon.Stat;
import ru.ifmo.se.pokemon.Status;
import ru.ifmo.se.pokemon.StatusMove;
import ru.ifmo.se.pokemon.Type;

public class Rest extends StatusMove {
    private final int MAX_POKEMON_HP;

    public Rest(int max_pokemon_hp) {
        super(Type.PSYCHIC, 0, 100);

        this.MAX_POKEMON_HP = max_pokemon_hp;
    }

    protected void applySelfEffects(Pokemon p) {
        Effect e1 = new Effect().turns(2).condition(Status.SLEEP);
        p.addEffect(e1);

        Effect e2 = new Effect().stat(Stat.HP, this.MAX_POKEMON_HP);
        p.addEffect(e2);
    }

    protected boolean checkAccuracy(Pokemon att, Pokemon def) {
        return true;
    }

    protected String describe() {
        return "uses Rest";
    }
}
