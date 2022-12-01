package cargoCompany;

import java.util.Random;

import core.Business;
import core.Human;
import core.Position;
import core.skills.EmployeeSkill;
import core.skills.EntrepreneurSkill;

public class CargoCompanyOwner extends Human implements EntrepreneurSkill {
    public CargoCompanyOwner(Position position, String name, int age, int cash) {
        super(position, name, age, cash);
    }

    public void startBusiness(String name, int employeeSalary) {
        Business business = new CargoBusiness(name, this, employeeSalary);
        this.businesses.add(business);
    }

    public void investInBusiness(String name) {
        Random random = new Random();

        int index = findBusiness(name);
        if (index != -1) {
            int amount = random.nextInt() % this.cash;
            this.businesses.get(index).invest(amount);
            this.cash -= amount;
        } else {
            System.out.println("Business not found!");
        }
    }

    public void closeBusiness(String name) {
        int index = findBusiness(name);
        if (index != -1) {
            this.businesses.remove(index);
        } else {
            System.out.println("Business not found!");
        }
    }

    public void hireForBusiness(String name, Human human) {
        int index = findBusiness(name);
        if (index != -1) {
            if (human instanceof EmployeeSkill) {
                boolean response = ((EmployeeSkill) human).receiveOffer(150);
                if (response == true) {
                    this.businesses.get(index).addEmployee(human);
                } else {
                    System.out.println("Human declined job");
                }
            }
        } else {
            System.out.println("Business not found!");
        }
    }

    public void withdrawFromBusiness(String name) {
        int index = findBusiness(name);
        if (index != -1) {
            this.businesses.get(index).withdrawCash(this);
        } else {
            System.out.println("Business not found!");
        }
    }

    public void receivePayment(int cash) {
        this.cash += cash;
    }

    private int findBusiness(String name) {
        int index = -1;

        for (int i = 0; i < this.businesses.size(); i++) {
            if (this.businesses.get(i).getName() == name) {
                return i;
            }
        }

        return index;
    }
}
