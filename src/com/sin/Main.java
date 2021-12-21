package com.sin;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

public class Main {
    @Parameter(names = {"--data_path"}, description = "dir path of source data")
    public String DataPath = "";

    @Parameter(names = {"--dst_ip"}, description = "ip of dst database address")
    public String DstIP = "";

    @Parameter(names = {"--dst_port"}, description = "port of dst database address")
    public Integer DstPort = 0;

    @Parameter(names = {"--dst_user"}, description = "user name of dst database")
    public String DstUser = "";

    @Parameter(names = {"--dst_password"}, description = "password of dst database")
    public String DstPassword = "";

    private static String url = "jdbc:mysql://121.41.55.205:3306/?characterEncoding=utf-8&useSSL=false&serverTimezone=Hongkong&allowPublicKeyRetrieval=true";
    private static String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static String user = "changesql";
    private static String passwd = "changesql";
    static {
        try{
            Class.forName(DRIVER);
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        Main main = new Main();
        //parse cmd
        JCommander jc = JCommander.newBuilder().addObject(main).build();
        jc.parse(args);

        try {
//            Class.forName().newInstance();
            Connection conn = DriverManager.getConnection(url, user, passwd);
            System.out.println(conn.getClientInfo().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // tdsqlshard-gzh17qjo.sql.tencentcdb.com:135 实例地址
}
