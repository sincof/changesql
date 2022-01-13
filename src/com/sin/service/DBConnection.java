package com.sin.service;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static String url;
    private static String user;
    private static String passwd;
    private HikariDataSource ds;


    static {
        try{
            String DRIVER = "com.mysql.cj.jdbc.Driver";
            Class.forName(DRIVER);
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public DBConnection(String ip, String port, String user, String passwd){
        DBConnection.url = "jdbc:mysql://" + ip + ":" + port + "/?characterEncoding=utf-8&useSSL=false&serverTimezone=Hongkong&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true";
        DBConnection.user = user;
        DBConnection.passwd = passwd;
        ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(user);
        ds.setPassword(passwd);
        ds.setConnectionTimeout(6000*1000);
    }

    public Connection connectDB() throws SQLException {
        Connection conn = this.ds.getConnection();
        return conn;
    }
}
