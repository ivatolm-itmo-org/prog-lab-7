import java.util.Random;

import cargoCompany.CargoCompanyEmployee;
import cargoCompany.CargoCompanyOwner;
import cargoCompany.CargoVehicle;
import core.Place;
import core.Position;
import core.Thing;
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
            Math.abs(random.nextInt() % 1000)
        );
        cargoCompanyOwner.startBusiness("EasyShipping", 100);
        cargoCompanyOwner.withdrawFromBusiness("EasyShipping");

        // Pumping money into business
        System.out.println("Owner's cash: " + cargoCompanyOwner.getCash());
        System.out.println("Company stock: " + cargoCompanyOwner.getBusiness("EasyShipping").getStock());
        System.out.println();
        for (int i = 0; i < 10; i++) {
            int investmentAmount = cargoCompanyOwner.getCash() / 2;
            System.out.println("Owner is investing " + investmentAmount);
            cargoCompanyOwner.investInBusiness("EasyShipping", investmentAmount);

            // timeout
            int result = 0;
            for (int j = 0; j < 100000000; j++) {
                // Yea, I know it's really bad.
                // For demonstration purposes only!
                result += random.nextInt() % 1;
            }

            cargoCompanyOwner.withdrawFromBusiness("EasyShipping");
            System.out.println("Company stock: " + cargoCompanyOwner.getBusiness("EasyShipping").getStock());
            System.out.println("Owner's cash: " + cargoCompanyOwner.getCash());
        }

        // Choose two workers in that business
        //   Order them to drive to the house of Louis and Rechel
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
            "Gazelle",
            2,
            10
        );

        // load vehicle
        for (int i = 0; i < vehicle.getStorageCapacity() / 2; i++) {
            vehicle.addStorageUnit(new Thing(
                new Position(random.nextDouble(), random.nextDouble(), random.nextDouble()),
                "Thing " + Math.abs(random.nextInt() % 100)
            ));
        }

        System.out.println("Vehicle was loaded. Current vehicle storage:");
        for (Thing storageUnit : vehicle.getStorage()) {
            System.out.println("  " + storageUnit);
        }

        DriveSkill driver = (DriveSkill) vehicle.getDriver();
        if (driver != null) {
            driver.driveTo(vehicle, Place.HOME.getPosition());
        }

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

        ellie.sleep(Place.CHILDREN_ROOM);
        gedj.sleep(Place.CHILDREN_ROOM);
        cherch.sleep(Place.CHERCH_BED);

        // Lous and Rechel
        //   After children and cat went to bed
        //   Talk for sometime
        //   Go to the kitchen table
        //   Sit
        //   Lous notices blue eyes

        Thing table = new Thing(Place.TABLE, "Kitchen table") {
            // public String clean() {
            //     return "cleaned";
            // }
        };
        Thing chair1 = new Thing(Place.CHAIR_1, "Kitchen chair 1");
        Thing chair2 = new Thing(Place.CHAIR_2, "Kitchen chair 2");

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

        louis.talk(rechel);

        System.out.println(louis);
        System.out.println(rechel);

        louis.move(Place.CHAIR_1);
        rechel.move(Place.CHAIR_2);

        // table.clean();
        louis.sitOn(chair1);
        rechel.sitOn(chair2);

        System.out.println(louis);
        System.out.println(rechel);

        louis.notice();
    }
}
