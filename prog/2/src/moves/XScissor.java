package src.moves;

import ru.ifmo.se.pokemon.Type;
import ru.ifmo.se.pokemon.PhysicalMove;

public class XScissor extends PhysicalMove {
    public XScissor() {
        super(Type.BUG, 80, 100);
    }

    protected String describe() {
        return "uses XScissor";
    }
}
