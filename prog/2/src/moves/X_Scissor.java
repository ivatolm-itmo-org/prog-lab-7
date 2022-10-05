package src.moves;

import ru.ifmo.se.pokemon.Type;
import ru.ifmo.se.pokemon.PhysicalMove;

public class X_Scissor extends PhysicalMove {
    public X_Scissor() {
        super(Type.BUG, 80, 100);
    }

    protected String describe() {
        return "uses X_Scissor";
    }
}
