package test.thread;

import com.sin.entity.DatabaseEntity;
import com.sin.entity.TableEntity;
import com.sin.service.DBConnection;
import com.sin.service.DBManager;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class InsertByDatabaseTest {

    //    String DstIP = "tdsqlshard-gzh17qjo.sql.tencentcdb.com";
//    String DstPort = "135";
//    String DstUser = "admin";
//    String DstPassword = "19351sinH_?";
    String DstIP = "121.41.55.205";
    String DstPort = "3306";
    String DstUser = "root";
    String DstPassword = "changesql";

    DBConnection dbConnection = new DBConnection(DstIP, DstPort, DstUser, DstPassword);

    @Test
    public void timeTest() throws ParseException {
        String[] sqltime = new String[]{"2021-03-30 04:30:27","2021-07-18 22:43:04",
                "2021-08-29 18:57:43","2021-09-03 16:57:37","2021-10-18 13:02:10",  "2021-12-11 10:02:42"};
        for(int i = 0; i < sqltime.length - 1; i++){
            Assertions.assertTrue(compareTime(sqltime[i], sqltime[i + 1]) < 0);
        }
    }

    // a < b: return true
    // a > b: return false
    public int compareTime(String oriS, String newS) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date oriD = df.parse(oriS), newD = df.parse(newS);
        return oriD.compareTo(newD);
    }

    // 对于查询测试 使用多个主键的来测试
    @Test
    public void selectTest() throws SQLException, IOException {
        DBManager dbManager = new DBManager("tmp/data"); // 获取所有的数据库信息
        DatabaseEntity databaseEntity = dbManager.dbStore.get("a");
        TableEntity tableEntity = databaseEntity.tableEntityMap.get("1");

        Connection connection = dbConnection.connectDB();
        connection.setCatalog(databaseEntity.name);
        // 该表存在主键 或者 唯一索引
        boolean primaryorUniqueKey = false;
        String tableName = tableEntity.createTable.getTable().getName();

        String s = """
                SELECT *FROM `1`
                    WHERE id
                    in(18777,2877,2989,3157,3652,5015,9346,11912,18368,23828,24934,25091,26629,36657);""";
        PreparedStatement selectStatement = connection.prepareStatement(s);
        ResultSet resultSet = selectStatement.executeQuery();
        ResultSetMetaData data = resultSet.getMetaData();
        int columnCnt = data.getColumnCount();
        int cnt = 0;
        while (resultSet.next()) {
            List<String> list = new ArrayList<>(columnCnt);
            for (int i = 1; i <= columnCnt; i++) {
                list.add(resultSet.getString(i));
            }
            cnt++;
            System.out.println(list);
        }
        System.out.println("total data received: " + cnt);
    }

    // 10w行插入的话，一共50w行左右 接近20s
    // 1w行插入的话，一共50w行做优 差不多39秒
    @Test
    public void insertTest() throws IOException, SQLException {
        DBManager dbManager = new DBManager("tmp/data"); // 获取所有的数据库信息
        DatabaseEntity databaseEntity = dbManager.dbStore.get("a");
        TableEntity tableEntity = databaseEntity.tableEntityMap.get("1");

        Connection connection = dbConnection.connectDB();
        connection.setCatalog(databaseEntity.name);
        // 该表存在主键 或者 唯一索引
        boolean primaryorUniqueKey = false;
        String tableName = tableEntity.createTable.getTable().getName();
        // 判断是否有唯一索引或者主键
        List<Index> list = tableEntity.createTable.getIndexes();
        // 获取主键的列的名字，主键可能包含多个列
        List<String> primaryKey = new LinkedList<>();
        // 用于主键判断的条件
        StringBuilder primaryStat = new StringBuilder();
        for (Index index : list) {
            if ("primary key".equals(index.getType().toLowerCase(Locale.ROOT))) {
                primaryorUniqueKey = true;
                primaryKey = index.getColumnsNames();
                for (String s : primaryKey) {
                    if (primaryStat.length() == 0)
                        primaryStat = new StringBuilder(s + "=?");
                    else
                        primaryStat.append(" and ").append(s).append("=?");
                }
                break;
            } else if ("unique key".equals(index.getType().toLowerCase(Locale.ROOT))) {
                primaryorUniqueKey = true;
                break;
            }
        }

        StringBuilder insertSB = new StringBuilder("insert into " + tableName + " values (");
        for (String name : tableEntity.columns) {
            insertSB.append("?,");
        }
        insertSB.deleteCharAt(insertSB.length() - 1);
        insertSB.append(")");
        PreparedStatement insertStatement = connection.prepareStatement(insertSB.toString());

        // 对所有插入的语句进行一个个计数
        int cnt = 0;
        List<String[]> dataList = new ArrayList<>(1002);
        long startTime = System.currentTimeMillis();
        try {
            for (String dataPath : tableEntity.tableDataPath) {
                BufferedReader br = new BufferedReader(new FileReader(dataPath));
                String line = br.readLine();
                String[] data;
                while (line != null) {
                    data = line.split(",");
                    dataList.add(data);
                    for (int i = 1; i <= data.length; i++)
                        insertStatement.setString(i, data[i - 1]);
                    line = br.readLine();
                    cnt++;
//                        insertStatement.execute();
                    insertStatement.addBatch();
                    if (cnt == 100000) {
                        int[] result = insertStatement.executeBatch();
                        cnt = 0;
                    }
                }
                if (cnt != 0) {
                    insertStatement.executeBatch();
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
            long endTime = System.currentTimeMillis();
        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);
        try {
            insertStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // JSQLPARSER
    // show create table + 表名 可以查看数据库中的DDL，数据库中的DDL格式一定是规整过的
    // 如果在服务器上跑的时候遇到了创建表格错误的问题，可以考虑再写入数据之前先考虑从数据库中获取表的DDL
    @Test
    public void testKey() {
        try {
            for (String sql : sqls) {
                CreateTable createTable = (CreateTable) CCJSqlParserUtil.parse(sql);
                List<Index> list = createTable.getIndexes();
                if (list != null) {
                    for (Index index : list) {
                        List<String> primarkKey = index.getColumnsNames();
                        if ("Primary Key".toLowerCase(Locale.ROOT).equals(index.getType().toLowerCase())) {
                            System.out.println(sql + " has primary key");
                        }
                        if ("Unique key".toLowerCase(Locale.ROOT).equals(index.getType().toLowerCase(Locale.ROOT))) {
                            System.out.println(sql + " has unique key");
                        }
                    }
                }
            }
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }
    // 0: primary key 有一个变量
    // primark key 有两个变量
    // 无primary key 或者 唯一索引
    // primary key 和变量放在一起
    // 有唯一索引和变量放在一起
    // 无唯一索引和变量放在一起

    public String[] sqls = new String[]{
            """
                CREATE TABLE if not exists `1`  (
                  `id` bigint(20) unsigned NOT NULL,
                  `a` float NOT NULL DEFAULT '0',
                  `b` char(32) NOT NULL DEFAULT '',
                  `updated_at` datetime NOT NULL DEFAULT '2021-12-12 00:00:00',
                  PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8""",
            """
                CREATE TABLE if not exists `2` (
                  `id` bigint(20) unsigned NOT NULL,
                  `a` float NOT NULL DEFAULT '0',
                  `b` char(32) NOT NULL DEFAULT '',
                  `updated_at` datetime NOT NULL DEFAULT '2021-12-12 00:00:00',
                  PRIMARY KEY (`id`,`a`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8""",
            """
                CREATE TABLE if not exists `2` (
                  `id` bigint(20) unsigned NOT NULL,
                  `a` float NOT NULL DEFAULT '0',
                  `b` char(32) NOT NULL DEFAULT '',
                  `updated_at` datetime NOT NULL DEFAULT '2021-12-12 00:00:00',
                  PRIMARY KEY (`id`,`a`,`b`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8""",
            """
                CREATE TABLE if not exists `3` (
                  `id` bigint(20) unsigned NOT NULL,
                  `a` double NOT NULL DEFAULT '0',
                  `b` char(32) NOT NULL DEFAULT '',
                  `updated_at` datetime NOT NULL DEFAULT '2021-12-12 00:00:00'
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8""",
            """
                CREATE TABLE if not exists `1`  (
                  `id` bigint(20) unsigned NOT NULL PRIMARY KEY,
                  `a` float NOT NULL DEFAULT '0',
                  `b` char(32) NOT NULL DEFAULT '',
                  `updated_at` datetime NOT NULL DEFAULT '2021-12-12 00:00:00'
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8""",
            """
                CREATE TABLE if not exists `3` (
                  `id` bigint(20) unsigned NOT NULL unique key,
                  `a` double NOT NULL DEFAULT '0',
                  `b` char(32) NOT NULL DEFAULT '',
                  `updated_at` datetime NOT NULL DEFAULT '2021-12-12 00:00:00'
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8""",
            """
                CREATE TABLE IF NOT EXISTS contacts (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    first_name VARCHAR(50),
                    last_name VARCHAR(50) NOT NULL,
                    phone VARCHAR(15) NOT NULL,
                    email VARCHAR(100) NOT NULL,
                    UNIQUE key unique_email (email)
                );"""
    };
}
