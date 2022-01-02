package com.sin.service;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private static String url = "jdbc:mysql://121.41.55.205:3306/?characterEncoding=utf-8&useSSL=false&serverTimezone=Hongkong&allowPublicKeyRetrieval=true";
    private static String user;
    private static String passwd;
    public static Connection conn;

    static {
        try{
            String DRIVER = "com.mysql.cj.jdbc.Driver";
            Class.forName(DRIVER);
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public DBConnection(String ip, String port, String user, String passwd){
        DBConnection.url = "jdbc:mysql://" + ip + ":" + port + "/?characterEncoding=utf-8&useSSL=false&serverTimezone=Hongkong&allowPublicKeyRetrieval=true";
        DBConnection.user = user;
        DBConnection.passwd = passwd;
    }

    public Connection connectDB(){
        try {
            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(url);
            ds.setUsername(user);
            ds.setPassword(passwd);
            ds.setConnectionTimeout(6000*1000);
            conn = ds.getConnection();
//            conn = DriverManager.getConnection(url, user, passwd);
//            System.out.println(conn.getClientInfo().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
}
