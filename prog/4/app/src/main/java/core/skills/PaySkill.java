package core.skills;

import core.AdultHuman;

public interface PaySkill {
    void giveCash(AdultHuman human, int cash);
    void receiveCash(int cash);
}
