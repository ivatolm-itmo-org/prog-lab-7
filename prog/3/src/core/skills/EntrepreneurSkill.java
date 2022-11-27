package core.skills;

import core.Business;
import core.Person;

interface EntrepreneurSkill {
    Business startBusiness(String name);
    void investInBusiness(String name, int cash);
    void closeBusiness(String name);
    void hireForBusiness(Person person);
    Business[] getBusinesses();
}
