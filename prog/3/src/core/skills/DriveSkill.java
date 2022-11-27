package core.skills;

import core.Position;
import core.Vehicle;

interface DriveSkill {
    void driveTo(Vehicle vehicle, Position position);
}
