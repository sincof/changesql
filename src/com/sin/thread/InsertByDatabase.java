package com.sin.thread;

import com.sin.entity.DatabaseEntity;
import com.sin.entity.TableEntity;
import com.sin.service.DBConnection;
import net.sf.jsqlparser.statement.create.table.Index;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
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
        assert this.dbConnection != null;
    }

    // 1.   读取文件中的数据，当行数达到1000行的时候
    //      直接发送1000次查询给数据库
    //      硬等数据库放回1000次查询结果
    //      根据放回的结果发送更新以及插入块
    @Override
    public Boolean call() {
        try (Connection connection = dbConnection.connectDB()) {
            // 在该线程里面需要使用的唯一一个连接
            connection.setCatalog(databaseEntity.name);
            // 该表存在主键 或者 唯一索引
            boolean primaryorUniqueKey = false;

            for (TableEntity curTable : databaseEntity.tableEntityMap.values()) {
                String tableName = curTable.createTable.getTable().getName();
//                String tableName = curTable.name;
                // 判断是否有唯一索引或者主键
                List<Index> list = curTable.createTable.getIndexes();
                // 获取主键的列的名字，主键可能包含多个列
                List<String> primaryKey = new LinkedList<>();
                // 用于主键判断的条件
                StringBuilder primaryStat = new StringBuilder();
                Map<String, Integer> primaryKeyIndex = new HashMap<>();
                for (Index index : list) {
                    if ("primary key".equals(index.getType().toLowerCase(Locale.ROOT))) {
                        primaryorUniqueKey = true;
                        primaryKey = index.getColumnsNames();
                        for (String s : primaryKey) {
                            if (primaryStat.length() == 0)
                                primaryStat = new StringBuilder(s + "=?");
                            else
                                primaryStat.append(" and ").append(s).append("=?");
                            for (int i = 0; i < curTable.columns.size(); i++) {
                                if (curTable.columns.get(i).equals(s)) {
                                    primaryKeyIndex.put(s, i);
                                }
                            }
                        }
                        break;
                    } else if ("unique key".equals(index.getType().toLowerCase(Locale.ROOT))) {
                        primaryorUniqueKey = true;
                        break;
                    }
                }
                if (primaryorUniqueKey) {
                    // 在tableEntity中，createTable只在第一次的时候更新过，后面的更新都记录在Map里面
                    StringBuilder updateSB = new StringBuilder("update " + tableName + " set ");
                    StringBuilder insertSB = new StringBuilder("insert into " + tableName + " values (");
                    for (String name : curTable.columns) {
                        updateSB.append(name).append("=?,");
                        insertSB.append("?,");
                    }
                    // 把updateSB的最后一个逗号删除掉，然后添加where 语句
                    updateSB.deleteCharAt(updateSB.length() - 1);
                    updateSB.append(" where ").append(primaryStat).append(";");
                    // 把insertSB的最后一个逗号删除掉，然后添加) 语句
                    insertSB.deleteCharAt(insertSB.length() - 1);
                    insertSB.append(")");

                    PreparedStatement selectStatement = connection.prepareStatement("select updated_at from " + tableName + " where " + primaryStat + ";");
                    PreparedStatement updateStatement = connection.prepareStatement(updateSB.toString());
                    PreparedStatement insertStatement = connection.prepareStatement(insertSB.toString());

                    // 对所有插入的语句进行一个个计数
                    int cnt = 0;
                    // 100000的内存可能会用的有点多，待会观察下运行内存被占用了多少
//                    List<String[]> dataList = new ArrayList<>(100000);
                    for (String dataPath : curTable.tableDataPath) {
                        BufferedReader br = new BufferedReader(new FileReader(dataPath));
                        String line = br.readLine();
                        String[] data;
                        while (line != null) {
                            data = line.split(",");
                            for (int i = 0; i < data.length; i++) {
                                insertStatement.setString(i + 1, data[i]);
                                updateStatement.setString(i + 1, data[i]);
                            }
                            // 设置where的主键等于语句
                            int nowKeyIndex = 0;
                            for (String pk : primaryKey) {
                                updateStatement.setString(data.length + nowKeyIndex, data[primaryKeyIndex.getOrDefault(pk, 0)]);
                                selectStatement.setString(nowKeyIndex + 1, data[primaryKeyIndex.getOrDefault(pk, 0)]);
                                nowKeyIndex++;
                            }
                            ResultSet resultSet = selectStatement.executeQuery();
                            if (resultSet.wasNull()) {
                                insertStatement.execute();
                            } else {
                                String updated_at = resultSet.getString(0);

                                updateStatement.execute();
                            }
                            line = br.readLine();
                        }
                        br.close();
                    }
                    try {
                        insertStatement.close();
                        selectStatement.close();
                        updateStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 在tableEntity中，createTable只在第一次的时候更新过，后面的更新都记录在Map里面
                    StringBuilder selectSB = new StringBuilder("select updated_at from " + tableName + "where ");
                    StringBuilder insertSB = new StringBuilder("insert into " + tableName + " values (");
                    int updated_atIndex = 0, cnt = 0;
                    for (String name : curTable.columns) {
                        insertSB.append("?,");
                        if ("updated_at".equals(name)) {
                            updated_atIndex = cnt;
                        } else
                            selectSB.append(name + "=?,");
                        cnt++;
                    }
                    // 把insertSB的最后一个逗号删除掉，然后添加)
                    insertSB.deleteCharAt(insertSB.length() - 1);
                    insertSB.append(")");
                    // 把insertSB的最后一个逗号删除掉，然后添加)
                    selectSB.deleteCharAt(selectSB.length() - 1);
                    selectSB.append(")");

                    PreparedStatement selectStatement = connection.prepareStatement(selectSB.toString());
                    PreparedStatement updateStatement = connection.prepareStatement("update " + tableName + " set updated_at = ?");
                    PreparedStatement insertStatement = connection.prepareStatement(insertSB.toString());

                    // 对所有插入的语句进行一个个计数
//                    int cnt = 0;
                    // 100000的内存可能会用的有点多，待会观察下运行内存被占用了多少
//                    List<String[]> dataList = new ArrayList<>(100000);
                    for (String dataPath : curTable.tableDataPath) {
                        BufferedReader br = new BufferedReader(new FileReader(dataPath));
                        String line = br.readLine();
                        String[] data;
                        while (line != null) {
                            data = line.split(",");
                            for (int i = 0; i < data.length; i++) {
                                insertStatement.setString(i + 1, data[i]);
                                if (i < updated_atIndex)
                                    selectStatement.setString(i + 1, data[i]);
                                else if (i == updated_atIndex)
                                    updateStatement.setString(1, data[i]);
                                else if(i > updated_atIndex)
                                    selectStatement.setString(i, data[i]);
                            }
                            ResultSet resultSet = selectStatement.executeQuery();
                            if (resultSet.wasNull()) {
                                insertStatement.execute();
                            } else {
                                updateStatement.execute();
                            }
                            line = br.readLine();
                        }
                        br.close();
                    }
                    try {
                        insertStatement.close();
                        selectStatement.close();
                        updateStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
