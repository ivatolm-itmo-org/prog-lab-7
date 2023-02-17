package com.ivatolm.app.database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import com.ivatolm.app.humanBeing.HumanBeing;
import com.opencsv.CSVReader;

public class CSVDatabase implements Database {

    CSVDatabase() {
        try {
            FileInputStream fstream = new FileInputStream("123");
            InputStreamReader istream = new InputStreamReader(fstream);

            CSVReader csvReader = new CSVReader(istream);
            String[] nextRecord;
  
            try {
                while ((nextRecord = csvReader.readNext()) != null) {
                    for (String cell : nextRecord) {
                        System.out.print(cell + "\t");
                    }
                    System.out.println();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {

        }
    }

    @Override
    public void write(LinkedList<HumanBeing> data) {
        
    }

    @Override
    public LinkedList<HumanBeing> read() {
        return null;
    }
    
}
