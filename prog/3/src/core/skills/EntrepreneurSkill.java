package core.skills;

import core.Human;

public interface EntrepreneurSkill {
    public void startBusiness(String name, int employeeSalary);
    public void investInBusiness(String name);
    public void closeBusiness(String name);
    public void hireForBusiness(String name, Human human);
    public void withdrawFromBusiness(String name);
    public void receivePayment(int cash);
}
