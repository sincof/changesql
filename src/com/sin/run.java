package com.sin;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.sin.service.DBConnection;
import com.sin.service.DBManager;
import com.sin.service.ProgramStatus;
import com.sin.thread.ThreadPoolManager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class run {
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

//    public static final int DEFAULT_THREADS = 4;
//    // 线程池的参数
//    private static final int CORE_POOL_SIZE = 4;
//    private static final int MAX_POOL_SIZE = 8192;
//    private static final int QUEUE_CAPACITY = 8192;
//    private static final long KEEP_ALIVE_TIME = 100 * 60;


    public static void main(String[] args) throws IOException, InterruptedException {

        run run = new run();
        //parse cmd
        JCommander jc = JCommander.newBuilder().addObject(run).build();
        jc.parse(args);

        DBConnection dbconn = new DBConnection(run.DstIP, run.DstPort, run.DstUser, run.DstPassword);
        // 其实后面可以到到多线程在创建数据库，这里创建也行，没差
        DBManager dbManager = new DBManager(run.DataPath); // 获取所有的数据库信息

        int status = ProgramStatus.getProgramStatus();
        switch (status) {
            case -1:
                System.out.println("LOG: First run! (null -> 1)");
                try (Connection conn = dbconn.connectDB()) {
                    dbManager.createDB(conn); // 创建数据表和数据库
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
                ProgramStatus.finishWR();
                System.out.println("LOG: Finish creating table (null -> 1 -> 2)");
                break;
            case 1:
                System.out.println("LOG: Second run! (1 -> 3)");
                try (Connection conn = dbconn.connectDB()) {
                    dbManager.createDB(conn); // 创建数据表和数据库
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
                ProgramStatus.finishWR();
                System.out.println("LOG: Finish creating table (1 -> 3 -> 4)");
                break;
            case 2:
                System.out.println("LOG: Second run! (2 -> 4)");
                ProgramStatus.finishWR();
                System.out.println("LOG: I am status 2 with database & table (2 -> 4)");
                break;
            case 3:
                System.out.println("LOG: Third run!");
                try (Connection conn = dbconn.connectDB()) {
                    dbManager.createDB(conn); // 创建数据表和数据库
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            case 4:
                // insert
                System.out.println("LOG: Start insert data into database");
                ThreadPoolManager threadPoolManager = new ThreadPoolManager(dbManager, dbconn);
                threadPoolManager.runInsertTaskByTableHash();
                break;
        }
    }
    // tdsqlshard-gzh17qjo.sql.tencentcdb.com:135 实例地址
}
