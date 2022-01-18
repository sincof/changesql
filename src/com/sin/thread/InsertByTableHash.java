package com.sin.thread;

import com.sin.entity.TableEntity;
import com.sin.service.DBConnection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

public class InsertByTableHash implements Callable<Boolean> {
    private final TableEntity tableEntity;
    private final String dbName;
    private final DBConnection dbConnection;

    public InsertByTableHash(TableEntity tableEntity, String dbName, DBConnection dbConnection) {
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
            connection.setCatalog(dbName);

            int MAX_BATCH_SIZE = 50000;
            if (tableEntity.hasKey) {
                PreparedStatement selectStatement = connection.prepareStatement(tableEntity.selectSB.toString());
                PreparedStatement updateStatement = connection.prepareStatement(tableEntity.updateSB.toString());
                PreparedStatement insertStatement = connection.prepareStatement(tableEntity.insertSB.toString());

                // 对所有插入的语句进行一个个计数
                int updateCnt = 0, insertCnt = 0;
                for (String dataPath : tableEntity.tableDataPath) {
                    BufferedReader br = new BufferedReader(new FileReader(dataPath));
                    String line = br.readLine();
                    String[] data;
                    while (line != null) {
                        if (line.length() != 0) {
                            data = line.split(",");

                            int updataStatementIndex = 1;
                            for (int i = 0; i < data.length; i++) {
                                insertStatement.setString(i + 1, data[i]);
                                if (!tableEntity.keySet.contains(i))
                                    updateStatement.setString(updataStatementIndex++, data[i]);
                            }
                            // 设置where的主键等于语句
                            int nowKeyIndex = 1;
                            for (Integer i : tableEntity.keyIndex) {
                                updateStatement.setString(updataStatementIndex++, data[i]);
                                selectStatement.setString(nowKeyIndex, data[i]);
                                nowKeyIndex++;
                            }
                            ResultSet resultSet = selectStatement.executeQuery();
                            if (resultSet.next()) {
                                // 结果集中存在数据 更新数据
                                String updated_at = resultSet.getString(1);
                                try {
                                    // < 0 is updated_at is before the data[updatedatIndex]
                                    // > 0 is updated_at is after the data[updatedatIndex]
                                    if (compareTime(updated_at, data[tableEntity.updatedatIndex]) < 0) {
                                        updateStatement.addBatch();
                                        updateCnt++;
                                    }
                                } catch (ParseException pe) {
                                    System.out.println("updated_at in DB: " + updated_at + ", updated_at in data: " + data[tableEntity.updatedatIndex]);
                                    pe.printStackTrace();
                                }
                            } else {
                                // 没有结果 就插入数据
                                insertStatement.addBatch();
                                insertCnt++;
                            }
                            resultSet.close();
                        }
                        line = br.readLine();
                        if (updateCnt % MAX_BATCH_SIZE == 0) {
                            updateStatement.executeBatch();
                        }
                        if (insertCnt % MAX_BATCH_SIZE == 0) {
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
                PreparedStatement selectStatement = connection.prepareStatement(tableEntity.selectSB.toString());
                PreparedStatement updateStatement = connection.prepareStatement(tableEntity.updateSB.toString());
                PreparedStatement insertStatement = connection.prepareStatement(tableEntity.insertSB.toString());

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
                                if (i < tableEntity.updatedatIndex) {
                                    selectStatement.setString(i + 1, data[i]);
                                    updateStatement.setString(i + 2, data[i]);
                                } else if (i == tableEntity.updatedatIndex)
                                    updateStatement.setString(1, data[i]);
                                else {
                                    selectStatement.setString(i, data[i]);
                                    updateStatement.setString(i + 2, data[i]);
                                }
                            }
                            ResultSet resultSet = selectStatement.executeQuery();
                            if (resultSet.next()) {
                                String updated_at = resultSet.getString(1);
                                try {
                                    // < 0 is updated_at is before the data[updatedatIndex]
                                    // > 0 is updated_at is after the data[updatedatIndex]
                                    if (compareTime(updated_at, data[tableEntity.updatedatIndex]) < 0) {
                                        updateStatement.addBatch();
                                        updateCnt++;
                                    }
                                } catch (ParseException pe) {
                                    System.out.println("updated_at in DB: " + updated_at + ", updated_at in data: " + data[tableEntity.updatedatIndex]);
                                    pe.printStackTrace();
                                }
                            } else {
                                insertStatement.addBatch();
                                insertCnt++;
                                //insertStatement.execute();
                            }
                        }
                        line = br.readLine();
                        if (updateCnt % MAX_BATCH_SIZE == 0) {
                            updateStatement.executeBatch();
                        }
                        if (insertCnt % MAX_BATCH_SIZE == 0) {
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
        } catch (SQLException | IOException ex) {
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
