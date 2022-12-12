import java.util.Random;

import cargoCompany.CargoCompanyEmployee;
import cargoCompany.CargoCompanyOwner;
import cargoCompany.CargoVehicle;
import core.Place;
import core.Position;
import core.skills.DriveSkill;
import home.*;

public class Main {
    public static void main(String[] args) {
        Random random = new Random();

        // Create unknown person [done]
        //   Create business
        //   Hire people and invest chaotically
        //   Sometimes withdraw money

        CargoCompanyOwner cargoCompanyOwner = new CargoCompanyOwner(
            new Position(random.nextDouble(), random.nextDouble(), random.nextDouble()),
            "Ivan Ivanovich",
            25 + random.nextInt() % 25,
            random.nextInt()
        );
        cargoCompanyOwner.startBusiness("EasyShipping", 100);
        cargoCompanyOwner.withdrawFromBusiness("EasyShipping");

        // Choose two workers in that business
        //   Order them to drive to the house of Lous and Rechel
        //   After some time leave

        CargoCompanyEmployee cargoCompanyEmployee1 = new CargoCompanyEmployee(
            new Position(random.nextDouble(), random.nextDouble(), random.nextDouble()),
            "Petr Sergeevich",
            20 + random.nextInt() % 25,
            random.nextInt() % 1000
        );
        CargoCompanyEmployee cargoCompanyEmployee2 = new CargoCompanyEmployee(
            new Position(random.nextDouble(), random.nextDouble(), random.nextDouble()),
            "Sergey Petrovich",
            20 + random.nextInt() % 25,
            random.nextInt() % 1000
        );

        cargoCompanyOwner.hireForBusiness("EasyShipping", cargoCompanyEmployee1);
        cargoCompanyOwner.hireForBusiness("EasyShipping", cargoCompanyEmployee2);

        CargoVehicle vehicle = new CargoVehicle(
            Place.CARGO_COMPANY_HEADQUATERS.getPosition(),
            2,
            100
        );

        // load vehicle

        DriveSkill driver = (DriveSkill) vehicle.getDriver();
        if (driver != null) {
            driver.driveTo(vehicle, Place.HOME.getPosition());
        }

        Table table = new Table(Place.TABLE.getPosition(), "Kitchen table");
        Chair chair1 = new Chair(Place.CHAIR_1.getPosition(), "Kitchen chair 1");
        Chair chair2 = new Chair(Place.CHAIR_2.getPosition(), "Kitchen chair 2");

        Louis louis = new Louis(
            new Position(
                random.nextDouble(), random.nextDouble(), random.nextDouble()
            ), 25 + random.nextInt() % 15, 1000
        );

        Rechel rechel = new Rechel(
            new Position(
                random.nextDouble(), random.nextDouble(), random.nextDouble()
            ), 25 + random.nextInt() % 15, 1000
        );

        louis.sitOn(chair1);
        rechel.sitOn(chair2);

        louis.talk(rechel);
        louis.notice();

        // Children and cat go to bed
        //   They sleep in different places

        Ellie ellie = new Ellie(new Position(
            random.nextDouble(), random.nextDouble(), random.nextDouble()
        ), 3, 0);

        Gedj gedj = new Gedj(new Position(
            random.nextDouble(), random.nextDouble(), random.nextDouble()
        ), 5, 0);

        Cherch cherch = new Cherch(new Position(
            random.nextDouble(), random.nextDouble(), random.nextDouble()
        ), 2);

        ellie.sleep(Place.ELLIE_BED);
        gedj.sleep(Place.GEDJ_BED);
        cherch.sleep(Place.CHERCH_BED);

        // Lous and Rechel
        //   After children and cat went to bed
        //   Talk for sometime
        //   Go to the kitchen table
        //   Sit
        //   Lous notices blue eyes

        louis.talk(rechel);

        louis.move(Place.CHAIR_1);
        rechel.move(Place.CHAIR_2);

        louis.sitOn(chair1);
        rechel.sitOn(chair2);

        louis.notice();
    }
}
