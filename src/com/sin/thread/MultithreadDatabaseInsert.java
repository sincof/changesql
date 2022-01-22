package com.sin.thread;

import com.sin.entity.TableEntity;
import com.sin.service.DBConnection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class MultithreadDatabaseInsert implements Callable<Boolean> {
    private final TableEntity tableEntity;
    private final String dbName;
    private final DBConnection dbConnection;

    public MultithreadDatabaseInsert(TableEntity tableEntity, String dbName, DBConnection dbConnection) {
        this.tableEntity = tableEntity;
        this.dbName = dbName;
        this.dbConnection = dbConnection;
        assert this.dbConnection != null;
    }

    @Override
    public Boolean call() {
        System.out.println("INSERT TABLE " + dbName + " : " + tableEntity.name);
        try (Connection connection = dbConnection.connectDB()) {
            connection.setCatalog(dbName);
            // max batch size
            int MAX_BATCH_SIZE = 40000;
            // 对所有插入的语句进行一个个计数
            int insertCnt = 1;
            PreparedStatement insertStatement = connection.prepareStatement(tableEntity.insertSB.toString());
            BufferedReader br = new BufferedReader(new FileReader(dbName + tableEntity.name + String.valueOf(2)));
            String line = br.readLine();
            String[] data;
            while (line != null) {
                if (line.length() != 0) {
                    data = line.trim().split(",");
                    if (data.length != tableEntity.columnLen && line.charAt(0) == ',') {
                        for (int i = 1; i < data.length; i++)
                            insertStatement.setString(i + 1, data[i]);
                    } else {
                        for (int i = 0; i < data.length; i++)
                            insertStatement.setString(i + 1, data[i]);
                    }
                    insertStatement.addBatch();
                    insertCnt++;
                }
                line = br.readLine();
                if (insertCnt % MAX_BATCH_SIZE == 0) {
                    insertStatement.executeBatch();
                    insertCnt = 1;
                }
            }
            br.close();
            if (insertCnt != 1) {
                insertStatement.executeBatch();
            }
            try {
                insertStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
