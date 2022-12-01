package core.skills;

import core.Position;
import core.Vehicle;

public interface DriveSkill {
    void driveTo(Vehicle vehicle, Position position);
}
