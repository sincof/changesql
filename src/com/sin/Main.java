package com.sin;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.sin.service.DBConnection;
import com.sin.service.DBManager;

import javax.swing.text.html.HTMLEditorKit;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class Main {
    @Parameter(names = {"--data_path"}, description = "dir path of source data")
    public String DataPath = "";

    @Parameter(names = {"--dst_ip"}, description = "ip of dst database address")
    public String DstIP = "";

    @Parameter(names = {"--dst_port"}, description = "port of dst database address")
    public String DstPort = "";

    @Parameter(names = {"--dst_user"}, description = "user name of dst database")
    public String DstUser = "";

    @Parameter(names = {"--dst_password"}, description = "password of dst database")
    public String DstPassword = "";

    public static final int DEFAULT_THREADS  = 4;




    public static void main(String[] args) throws IOException {

        Main main = new Main();
        //parse cmd
        JCommander jc = JCommander.newBuilder().addObject(main).build();
        jc.parse(args);

        DBConnection dbconn = new DBConnection(main.DstIP, main.DstPort, main.DstUser, main.DstPassword);
        Connection conn = dbconn.connectDB();

        DBManager dbManager = new DBManager(main.DataPath);
        dbManager.createDB(conn);



    }
    // tdsqlshard-gzh17qjo.sql.tencentcdb.com:135 实例地址
}
