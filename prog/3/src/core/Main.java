package core;
import java.util.Random;

import cargoCompany.CargoCompanyEmployee;
import cargoCompany.CargoCompanyOwner;
import cargoCompany.CargoVehicle;
import core.skills.DriveSkill;
import home.Chair;
import home.Ellie;
import home.Gedj;
import home.Louis;
import home.Rechel;
import home.Table;

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

        Ellie ellie = new Ellie(new Position(
            random.nextDouble(), random.nextDouble(), random.nextDouble()
        ), 3, 0);

        ellie.sleep(Place.ELLIE_BED.getPosition());

        Gedj gedj = new Gedj(new Position(
            random.nextDouble(), random.nextDouble(), random.nextDouble()
        ), 3, 0);

        gedj.sleep(Place.GEDJ_BED.getPosition());

        // Children and cat go to bed
        //   They sleep in different places

        // Lous and Rechel
        //   After children and cat went to bed
        //   Talk for sometime
        //   Go to the kitchen table
        //   Sit
        //   Lous notices blue eyes

        // Animal (abstract): Human, Cat | Common: name, age, react() [done]
        // Business (abstract): name, workers, ..., . [done]
        // Place (enumerate).

        // Human has many flags or sayings.
    }
}
