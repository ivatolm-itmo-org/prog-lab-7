package core;

public class Business {
    private String name;
    private int income;
    private int stock;
    private Person owner;
    private Person[] employees;
    private int employeeSalary;

    String getName() {
        return this.name;
    }

    int getIncome() {
        return this.income;
    }

    int getStock() {
        return this.stock;
    }

    void payEmployees() {
        System.out.println("Paying employees...");
    }
}
