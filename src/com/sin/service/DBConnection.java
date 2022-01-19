package com.sin.service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private HikariDataSource ds;
    private static HikariConfig config = new HikariConfig();


    static {
        try{
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "10000");
            config.addDataSourceProperty("prepStmtCacheSqlLimit","1024");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            String DRIVER = "com.mysql.cj.jdbc.Driver";
            Class.forName(DRIVER);
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public DBConnection(String ip, String port, String user, String passwd){
        config.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/?characterEncoding=utf-8&useSSL=false&serverTimezone=Hongkong&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true");
        config.setUsername(user);
        config.setPassword(passwd);
        ds = new HikariDataSource(config);
//        ds.setConnectionTimeout(6000*1000);
//        ds.setAutoCommit(true);
    }

    public Connection connectDB() throws SQLException {
        Connection conn = this.ds.getConnection();
        return conn;
    }
}
