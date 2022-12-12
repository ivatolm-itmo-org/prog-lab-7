package core;
import java.util.ArrayList;
import java.util.Date;

import core.skills.EmployeeSkill;
import core.skills.EntrepreneurSkill;

public abstract class Business {
    private String name;
    private int cash;
    private double stock;

    private Human owner;
    private ArrayList<Human> employees;

    private int employeeSalary;

    private Date lastUpdate;

    public Business(String name, Human owner, int employeeSalary) {
        this.name = name;
        this.cash = 0;
        this.stock = 0;
        this.owner = owner;
        this.employees = new ArrayList<>();
        this.employeeSalary = employeeSalary;

        this.lastUpdate = new Date();
    }

    public String getName() {
        return this.name;
    }

    public double getStock() {
        return this.stock;
    }

    public Human getOwner() {
        return this.owner;
    }

    public void withdrawCash(Human human) {
        // this method is designed such only owner could withdraw

        if (this.owner.equals(human)) {
            this.calcCash();

            this.payEmployees();

            int amount = this.cash;
            this.cash = 0;

            if (human instanceof EntrepreneurSkill) {
                ((EntrepreneurSkill) human).receivePayment(amount);
            }
        }
    }

    public void invest(int amount) {
        if (amount > 0) {
            this.stock += (double) amount / 100;
        } else {
            System.out.println("You cannot steal! Ha, thought you smart?");
        }
    }

    public void payEmployees() {
        int requiredAmount = this.employeeSalary * this.employees.size();

        if (this.cash < requiredAmount) {
            System.out.println("Transaction failed. Insufficient cash");
            return;
        }

        for (Human employee : this.employees) {
            if (employee instanceof EmployeeSkill) {
                ((EmployeeSkill) employee).receivePaycheck(this.employeeSalary);
            }
        }

        this.cash -= requiredAmount;
    }

    public void addEmployee(Human human) {
        this.employees.add(human);
    }

    public void removeEmployee(Human human) {
        this.employees.remove(human);
    }

    private void calcCash() {
        Date date = new Date();
        if (this.lastUpdate.getTime() == date.getTime()) {
            return;
        }

        this.cash += this.stock * (int) (this.lastUpdate.getTime() - date.getTime()) / 10000;

        this.lastUpdate = date;
    }
}
