package com.sin.thread;

import com.mysql.cj.xdevapi.Table;
import com.sin.entity.DatabaseEntity;
import com.sin.entity.TableEntity;
import com.sin.service.DBConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Callable;

public class InsertByDatabase implements Callable<Boolean> {
    private DatabaseEntity databaseEntity;
    private DBConnection dbConnection;

    public InsertByDatabase(DatabaseEntity databaseEntity) {
        this.databaseEntity = databaseEntity;
        this.dbConnection = null;
    }

    public InsertByDatabase(DatabaseEntity databaseEntity, DBConnection dbConnection) {
        this.databaseEntity = databaseEntity;
        this.dbConnection = dbConnection;
    }

    @Override
    public Boolean call() throws IOException, InterruptedException {
        Iterator<TableEntity> it = databaseEntity.tableEntityMap.values().iterator();
        while (it.hasNext()) {
            TableEntity curTable = it.next();
            String tableName = curTable.name;
            for (String dataPath : curTable.tableDataPath) {
                BufferedReader br = new BufferedReader(new FileReader(dataPath));
                String line = br.readLine();
                String[] data;
                while (line != null) {
                    data = line.split(",");
                    line = br.readLine();
                }
            }
        }
//         Thread.sleep(2000);
//         System.out.println("Finish insert tables in Datbase %s".formatted(databaseEntity.name));
        return true;
    }
}
