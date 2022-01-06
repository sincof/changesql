package com.sin.thread;

import com.mysql.cj.x.protobuf.MysqlxCrud;
import com.sin.entity.DatabaseEntity;

public class InsertByDatabase implements Runnable{
    private DatabaseEntity databaseEntity;

    public InsertByDatabase(DatabaseEntity databaseEntity){
        this.databaseEntity = databaseEntity;
    }

    @Override
    public void run() {
        try{
            Thread.sleep(2000);
        }catch (InterruptedException ie){
            ie.printStackTrace();
        }
        System.out.println("Finish insert table in %s".formatted(databaseEntity.name));
    }
}
