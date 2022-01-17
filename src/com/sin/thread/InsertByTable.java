package com.sin.thread;

import com.sin.entity.TableEntity;
import com.sin.service.DBConnection;
import net.sf.jsqlparser.statement.create.table.Index;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;

public class InsertByTable implements Callable<Boolean> {
    private final TableEntity tableEntity;
    private final String dbName;
    private final DBConnection dbConnection;

    private final int MAX_BATCH_SIZE = 50000;

    public InsertByTable(TableEntity tableEntity, String dbName, DBConnection dbConnection) {
        this.tableEntity = tableEntity;
        this.dbName = dbName;
        this.dbConnection = dbConnection;
        assert this.dbConnection != null;
    }

    // 1.   读取文件中的数据，当行数达到1000行的时候
    //      直接发送1000次查询给数据库
    //      硬等数据库放回1000次查询结果
    //      根据放回的结果发送更新以及插入块
    @Override
    public Boolean call() {
        System.out.println("INSERT TABLE " + tableEntity.name);
        try (Connection connection = dbConnection.connectDB()) {
            // 在该线程里面需要使用的唯一一个连接
            connection.setCatalog(dbName);
            // 该表存在主键 或者 唯一索引
            boolean primaryorUniqueKey = false;

            String tableName = tableEntity.createTable.getTable().getName();
            // String tableName = tableEntity.name;
            // 判断是否有唯一索引或者主键
            List<Index> list = tableEntity.createTable.getIndexes();
            // 获取主键的列的名字，主键可能包含多个列
            List<String> primaryKey = new LinkedList<>();
            // 用于主键判断的条件
            StringBuilder primaryStat = new StringBuilder();
            Map<String, Integer> primaryKeyIndex = new HashMap<>();
            if (list != null) {
                for (Index index : list) {
                    if ("primary key".equals(index.getType().toLowerCase(Locale.ROOT))) {
                        primaryorUniqueKey = true;
                        primaryKey = index.getColumnsNames();
                        for (String s : primaryKey) {
                            if (primaryStat.length() == 0)
                                primaryStat = new StringBuilder(s + "=?");
                            else
                                primaryStat.append(" and ").append(s).append("=?");
                            for (int i = 0; i < tableEntity.columns.size(); i++) {
                                if (tableEntity.columns.get(i).equals(s)) {
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
            }
            if (primaryorUniqueKey) {
                // need to record the updated_at index to use the update statement to update the table
                int updatedatIndex = 0, columnCnt = 0;
                // 在tableEntity中，createTable只在第一次的时候更新过，后面的更新都记录在Map里面
                StringBuilder updateSB = new StringBuilder("update " + tableName + " set ");
                StringBuilder insertSB = new StringBuilder("insert into " + tableName + " values (");
                for (String name : tableEntity.columns) {
                    updateSB.append(name).append("=?,");
                    insertSB.append("?,");
                    if (name.toLowerCase(Locale.ROOT).contains("updated_at")) {
                        updatedatIndex = columnCnt;
                    }
                    columnCnt++;
                }
                // 把updateSB的最后一个逗号删除掉，然后添加where 语句
                updateSB.deleteCharAt(updateSB.length() - 1);
                updateSB.append(" where ").append(primaryStat);
                // updateSB.append(" where ").append(primaryStat).append(";"); // he last ; would like to lead error in the batch execute
                // 把insertSB的最后一个逗号删除掉，然后添加) 语句
                insertSB.deleteCharAt(insertSB.length() - 1);
                insertSB.append(")");

                PreparedStatement selectStatement = connection.prepareStatement("select updated_at from " + tableName + " where " + primaryStat + ";");
                PreparedStatement updateStatement = connection.prepareStatement(updateSB.toString());
                PreparedStatement insertStatement = connection.prepareStatement(insertSB.toString());

                // 对所有插入的语句进行一个个计数
                int updateCnt = 0, insertCnt = 0;
                for (String dataPath : tableEntity.tableDataPath) {
                    BufferedReader br = new BufferedReader(new FileReader(dataPath));
                    String line = br.readLine();
                    String[] data;
                    while (line != null) {
                        if (line.length() != 0) {
                            data = line.split(",");
                            for (int i = 0; i < data.length; i++) {
                                insertStatement.setString(i + 1, data[i]);
                                updateStatement.setString(i + 1, data[i]);
                            }
                            // 设置where的主键等于语句
                            int nowKeyIndex = 1;
                            for (String pk : primaryKey) {
                                updateStatement.setString(data.length + nowKeyIndex, data[primaryKeyIndex.getOrDefault(pk, 0)]);
                                selectStatement.setString(nowKeyIndex, data[primaryKeyIndex.getOrDefault(pk, 0)]);
                                nowKeyIndex++;
                            }
                            ResultSet resultSet = selectStatement.executeQuery();
                            if (resultSet.next()) {
                                // 结果集中存在数据 更新数据
                                String updated_at = resultSet.getString(1);
                                try {
                                    // < 0 is updated_at is before the data[updatedatIndex]
                                    // > 0 is updated_at is after the data[updatedatIndex]
                                    if (compareTime(updated_at, data[updatedatIndex]) < 0) {
                                        updateStatement.addBatch();
                                        updateCnt++;
                                    }
                                } catch (ParseException pe) {
                                    System.out.println("updated_at in DB: " + updated_at + ", updated_at in data: " + data[updatedatIndex]);
                                    pe.printStackTrace();
                                }
                                // 判断是否更新的函数还没写，默认更新
                                // TODO: ERROR Duplicate entry '1784783537-595.527' for key '1.PRIMARY'
                                // 1. 数据库中存在了和插入数据一样的主键，但是没有查询出来
                                // 2. 盲猜就是数据精度出现了问题，虽然没有查询到主键，但是实际上插入的时候却会报这个错误
                                // updateStatement.execute();
                            } else {
                                // 没有结果 就插入数据
                                // insertStatement.execute();
                                insertStatement.addBatch();
                                insertCnt++;
                            }
                            resultSet.close();
                        }
                        line = br.readLine();
                        if (updateCnt >= MAX_BATCH_SIZE) {
                            updateStatement.executeBatch();
                        }
                        if (insertCnt >= MAX_BATCH_SIZE) {
                            insertStatement.executeBatch();
                        }
                    }
                    br.close();
                }
                if (updateCnt != 0) {
                    updateStatement.executeBatch();
                }
                if (insertCnt != 0) {
                    insertStatement.executeBatch();
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
                StringBuilder selectSB = new StringBuilder("select updated_at from " + tableName + " where ");
                StringBuilder updateSB = new StringBuilder("update " + tableName + " set updated_at = ? where ");
                StringBuilder insertSB = new StringBuilder("insert into " + tableName + " values (");
                int updatedatIndex = 0, cnt = 0;
                for (String name : tableEntity.columns) {
                    insertSB.append("?,");
                    if (name.toLowerCase(Locale.ROOT).contains("updated_at")) {
                        updatedatIndex = cnt;
                    } else {
                        selectSB.append(name).append("=? and");
                        updateSB.append(name).append("=? and");
                    }
                    cnt++;
                }
                // 把insertSB的最后一个逗号删除掉，然后添加)
                insertSB.deleteCharAt(insertSB.length() - 1);
                insertSB.append(")");
                // 把selectSB delete final `and`
                // selectSB.deleteCharAt(selectSB.length() - 1);
                selectSB.delete(selectSB.length() - 3, selectSB.length());
                // 把updateSB delete final `and`
                updateSB.delete(updateSB.length() - 3, updateSB.length());
                // updateSB.deleteCharAt(updateSB.length() - 1);

                PreparedStatement selectStatement = connection.prepareStatement(selectSB.toString());
                PreparedStatement updateStatement = connection.prepareStatement(updateSB.toString());
                PreparedStatement insertStatement = connection.prepareStatement(insertSB.toString());

                // 对所有插入的语句进行一个个计数
                int updateCnt = 0, insertCnt = 0;
                for (String dataPath : tableEntity.tableDataPath) {
                    BufferedReader br = new BufferedReader(new FileReader(dataPath));
                    String line = br.readLine();
                    String[] data;
                    while (line != null) {
                        if (line.length() != 0) {
                            data = line.split(",");
                            for (int i = 0; i < data.length; i++) {
                                insertStatement.setString(i + 1, data[i]);
                                if (i < updatedatIndex) {
                                    selectStatement.setString(i + 1, data[i]);
                                    updateStatement.setString(i + 2, data[i]);
                                } else if (i == updatedatIndex)
                                    updateStatement.setString(1, data[i]);
                                else {
                                    selectStatement.setString(i, data[i]);
                                    updateStatement.setString(i + 2, data[i]);
                                }
                            }
                            ResultSet resultSet = selectStatement.executeQuery();
                            if (resultSet.next()) {
                                updateStatement.addBatch();
                                updateCnt++;
                                // updateStatement.execute();
                            } else {
                                insertStatement.addBatch();
                                insertCnt++;
                                //insertStatement.execute();
                            }
                        }
                        line = br.readLine();
                        if (updateCnt >= MAX_BATCH_SIZE) {
                            updateStatement.executeBatch();
                        }
                        if (insertCnt >= MAX_BATCH_SIZE) {
                            insertStatement.executeBatch();
                        }
                    }
                    if (updateCnt != 0) {
                        updateStatement.executeBatch();
                    }
                    if (insertCnt != 0) {
                        insertStatement.executeBatch();
                    }
                    br.close();
                }
                try {
                    insertStatement.close();
                    selectStatement.close();
                    updateStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }catch (SQLException | IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }


    // a < b: return true
    // a > b: return false
    public int compareTime(String oriS, String newS) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date oriD = df.parse(oriS), newD = df.parse(newS);
        return oriD.compareTo(newD);
    }
}
