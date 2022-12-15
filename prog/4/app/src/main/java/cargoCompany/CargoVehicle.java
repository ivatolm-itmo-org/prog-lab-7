package cargoCompany;

import core.Position;
import core.Vehicle;

public class CargoVehicle extends Vehicle {
    public CargoVehicle(Position position, String name, int seatsNum, int storageCapacity) {
        super(position, name, seatsNum, storageCapacity);
    }
}
