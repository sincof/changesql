package com.sin.thread;

import com.sin.entity.DatabaseEntity;
import com.sin.entity.TableEntity;
import com.sin.service.DBConnection;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.Index;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

public class InsertByDatabase implements Callable<Boolean> {
    private final DatabaseEntity databaseEntity;
    private final DBConnection dbConnection;

    public InsertByDatabase(DatabaseEntity databaseEntity) {
        this.databaseEntity = databaseEntity;
        this.dbConnection = null;
    }

    public InsertByDatabase(DatabaseEntity databaseEntity, DBConnection dbConnection) {
        this.databaseEntity = databaseEntity;
        this.dbConnection = dbConnection;
    }

    // 1.   读取文件中的数据，当行数达到1000行的时候
    //      直接发送1000次查询给数据库
    //      硬等数据库放回1000次查询结果
    //      根据放回的结果发送更新以及插入块
    @Override
    public Boolean call() {
        try{
            // 在该线程里面需要使用的唯一一个连接
            assert dbConnection != null;
            Connection connection = dbConnection.connectDB();
            // 该表存在主键 或者 唯一索引
            boolean primaryorUniqueKey = false;

            for (TableEntity curTable : databaseEntity.tableEntityMap.values()) {
                String tableName = curTable.name;
                // 判断是否有唯一索引或者主键
                List<Index> list = curTable.createTable.getIndexes();
                // 获取主键的列的名字，主键可能包含多个列
                List<String> primaryKey = new LinkedList<>();
                // 用于主键判断的条件
                StringBuilder primaryStat = new StringBuilder();
                for(Index index : list){
                    if("primary key".equals(index.getType().toLowerCase(Locale.ROOT))){
                        primaryorUniqueKey = true;
                        primaryKey = index.getColumnsNames();
                        for(String s : primaryKey){
                            if(primaryStat.length() == 0)
                                primaryStat = new StringBuilder(s + "=?");
                            primaryStat.append(" and ").append(s).append("=?");
                        }
                        break;
                    }else if("unique key".equals(index.getType().toLowerCase(Locale.ROOT))){
                        primaryorUniqueKey = true;
                        break;
                    }
                }
                if(primaryorUniqueKey){
                    // 在tableEntity中，createTable只在第一次的时候更新过，后面的更新都记录在Map里面
                    // UPDATE table_name
                    // SET column1=value1,column2=value2,...
                    // WHERE some_column=some_value;
                    StringBuilder updateSB = new StringBuilder("update " + tableName + " set ");
                    // INSERT INTO table_name
                    // VALUES (value1,value2,value3,...);
                    StringBuilder insertSB = new StringBuilder("insert into " + tableName + " values (");
                    for(String name : curTable.columns){
                        updateSB.append(name).append("=?,");
                        insertSB.append("?,");
                    }
                    // 把updateSB的最后一个逗号删除掉，然后添加where 语句
                    updateSB.deleteCharAt(updateSB.length() - 1);
                    updateSB.append(" where ").append(primaryStat).append(";");
                    // 把insertSB的最后一个逗号删除掉，然后添加) 语句
                    insertSB.deleteCharAt(insertSB.length() - 1);
                    insertSB.append(");");

                    PreparedStatement selectStatement = connection.prepareStatement("select updated_at from " + tableName + " where " + primaryStat + ";");
                    PreparedStatement updateStatement = connection.prepareStatement(updateSB.toString());
                    PreparedStatement insertStatement = connection.prepareStatement(insertSB.toString());

                    // 对所有插入的语句进行一个个计数
                    int cnt = 0;
                    List<String[]> dataList = new ArrayList<>(1002);
                    for (String dataPath : curTable.tableDataPath) {
                        BufferedReader br = new BufferedReader(new FileReader(dataPath));
                        String line = br.readLine();
                        String[] data;
                        while (line != null) {
                            data = line.split(",");
                            dataList.add(data);
                            for(int i = 0; i < data.length; i++){
                                selectStatement.setString(i, data[i]);
                            }
                            line = br.readLine();
                            cnt++;
                            if(cnt == 1000){
                                selectStatement.executeBatch();
                            }
                        }
                    }
                    if(cnt != 0){
                    }
                }else{
                    // 如果不存在唯一索引或者主键
                }

            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
