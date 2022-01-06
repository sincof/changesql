package com.sin.thread;

import com.mysql.cj.x.protobuf.MysqlxCrud;
import com.sin.entity.DatabaseEntity;

import java.util.concurrent.Callable;

public class InsertByDatabase implements Callable<Boolean> {
    private DatabaseEntity databaseEntity;

    public InsertByDatabase(DatabaseEntity databaseEntity){
        this.databaseEntity = databaseEntity;
    }

    @Override
    public Boolean call() throws Exception {
        Thread.sleep(2000);
        System.out.println("Finish insert tables in Datbase %s".formatted(databaseEntity.name));
        return true;
    }
}
