package core.skills;

import core.Human;

public interface EntrepreneurSkill {
    void startBusiness(String name, int employeeSalary);
    void investInBusiness(String name);
    void closeBusiness(String name);
    void hireForBusiness(String name, Human human);
    void withdrawFromBusiness(String name);
    void receivePayment(int cash);
}
