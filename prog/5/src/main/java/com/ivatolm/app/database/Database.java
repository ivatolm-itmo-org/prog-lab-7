package com.ivatolm.app.database;

import java.util.LinkedList;

import com.ivatolm.app.humanBeing.HumanBeing;

public interface Database {

    void write(LinkedList<HumanBeing> data);
    LinkedList<HumanBeing> read();

}
