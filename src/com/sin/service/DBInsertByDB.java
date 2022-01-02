package com.sin.service;

import com.sin.entity.DatabaseEntity;

import java.sql.Connection;

public class DBInsertByDB {
    // 专门处理某个库的插入任务
    // 初始化时候参数传入该DB的全部信息
    private DatabaseEntity databaseEntity;
    private Connection conn;

    public DBInsertByDB(Connection conn, DatabaseEntity databaseEntity){
        this.conn = conn;
        this.databaseEntity = databaseEntity;
    }

    public int insertAll(){
        return 0;
    }


}
