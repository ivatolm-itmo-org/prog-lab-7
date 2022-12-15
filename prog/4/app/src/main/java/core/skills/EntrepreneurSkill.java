package core.skills;

import core.AdultHuman;

public interface EntrepreneurSkill {
    void startBusiness(String name, int employeeSalary);
    void investInBusiness(String name, int amount);
    void closeBusiness(String name);
    void hireForBusiness(String name, AdultHuman human);
    void withdrawFromBusiness(String name);
    void receivePayment(int cash);
}
