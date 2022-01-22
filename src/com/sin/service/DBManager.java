package com.sin.service;

import com.mysql.cj.xdevapi.Table;
import com.sin.entity.DatabaseEntity;
import com.sin.entity.TableEntity;
import com.zaxxer.hikari.HikariDataSource;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class DBManager {
    public String filePath;
    public ArrayList<DatabaseEntity> dbList; // it seems we do not need list to store and
    // iterate the database list
    // Map is enough to do it
    // Build and do not modify this map object
    // do not care about the concurrent problem.
    public Map<String, DatabaseEntity> dbStore;

    public DBManager(String filePath) throws IOException {
        this.filePath = filePath;
        // dbs = new LinkedList<>();
        dbStore = new HashMap<>();
        findDatabase();
        dbList = new ArrayList<>(dbStore.values());
    }

    public void findDatabase() throws IOException {
//        String filePath = "tmp/data/";
        File dirFile = new File(filePath);
        if (!dirFile.isDirectory())
            return;
        String[] dirs = dirFile.list();
        char[] buf = new char[2048];
        // 遍历每一个source
        for (int i = 0; dirs != null && i < dirs.length; i++) {
            // 每个source对应的路径
            StringBuilder sourcePath = new StringBuilder(filePath);
            if (filePath.charAt(filePath.length() - 1) != '/' || filePath.charAt(filePath.length() - 1) != '\\')
                sourcePath.append("/");
            sourcePath.append(dirs[i]);
            File sourceFile = new File(sourcePath.toString());
            // 每个source下的database目录
            if (!sourceFile.isDirectory())
                continue;
            String[] databaseDir = sourceFile.list();

            // 遍历source下的每一个database，找到每一个database对应的表
            for (int j = 0; databaseDir != null && j < databaseDir.length; j++) {
                String dbPath = sourcePath + "/" + databaseDir[j];
                File dbFile = new File(dbPath);
                if (!dbFile.isDirectory())
                    continue;
                String[] tablelist = dbFile.list();
                // 利用database文件夹的名字获取到对应的对象
                DatabaseEntity dbEntity = dbStore.getOrDefault(databaseDir[j], new DatabaseEntity(databaseDir[j]));

                for (int k = 0; tablelist != null && k < tablelist.length; k++) {
                    // split转义特定含义的字符(., *, $, |等)，例如. 需要使用\\.
                    String[] fname = tablelist[k].split("\\.", 2);
                    // 当文件名异常 或者 不是以sql结尾的，直接跳过
                    if (fname.length != 2 || !"sql".equals(fname[1])) {
                        continue;
                    }
                    // 获取表定义文件的内容
                    File tableDefine = new File(dbPath + "/" + tablelist[k]);
                    // 利用 BufferedReader 去读取表定义文件
                    BufferedReader br = new BufferedReader(new FileReader(tableDefine));
                    Arrays.fill(buf, '\0');
                    int len = br.read(buf, 0, 1024);
                    br.close();
                    // System.out.println(dirs[i] + " : " + databaseDir[j] + " : " + fname[0]);
                    // System.out.println(String.valueOf(buf, 0, len));
                    // BufferedReader dataBR = new BufferedReader(new FileReader(dbPath + "/" + fname[0] + ".csv"));
                    // System.out.println(dataBR.readLine());
                    // System.out.println(dataBR.readLine());
                    // dataBR.close();
                    // 存不存在表名和sql定义表的名字不一样的情况？ should be the same
                    if (!dbEntity.tableEntityMap.containsKey(fname[0])) {
                        // 创建新的表的实体
                        TableEntity table = new TableEntity(String.valueOf(buf, 0, len));
                        table.tableDataPath.add(dbPath + "/" + fname[0] + ".csv");
                        // 塞到map里面去
                        dbEntity.tableEntityMap.put(fname[0], table);
                    } else if (len != -1) {
                        // dbEntity.tableEntityMap.get(fname[0]).addTBDefine(String.valueOf(buf, 0, len));
                        dbEntity.tableEntityMap.get(fname[0]).tableDataPath.add(dbPath + "/" + fname[0] + ".csv");
                    }
                }
                dbStore.put(databaseDir[j], dbEntity);
            }
        }
    }

    // 在程序开始 统一调度 实现插入数据库和表格
    public int createDB(Connection conn) {
//        String cleanDBStatement = "drop database if exists %s";
//        String createDBStatement = "create database if not exists %s;";
        for (String databaseName : dbStore.keySet()) {
            try (Statement statement = conn.createStatement()) {

                statement.execute("create database if not exists " + databaseName);

                // 切换连接的数据库
                conn.setCatalog(databaseName);

                // 如果我在这里运行,必定是需要建立连接的，这样会比较浪费时间
                // 只是建立表格就直接关闭，没有充分利用这个连接
                // 先在这里写吧，后续由于我是统一利用databaseEntity储存的也比较好修改
                DatabaseEntity databaseEntity = dbStore.get(databaseName);
                Iterator<TableEntity> TIt = databaseEntity.tableEntityMap.values().iterator();
                try (Statement createTBStatement = conn.createStatement()) {
                    while (TIt.hasNext()) {
                        TableEntity tableEntity = TIt.next();
                        if (!tableEntity.isKey) {
                            createTBStatement.addBatch(tableEntity.createTable.toString());
                        } else {
                            createTBStatement.addBatch(tableEntity.createTable.toString().replaceFirst("PRIMARY KEY", "KEY"));
                        }
                    }
                    createTBStatement.executeBatch();
                } catch (SQLException e) {

                    e.printStackTrace();
                    return -1;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return 0;
    }

    // 任务是 创建表，创建
    public int createTable(Connection conn) {
        String createTBCommand = "create table %s if not exists";
        return 0;
    }
}