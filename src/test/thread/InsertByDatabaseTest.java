package test.thread;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

public class InsertByDatabaseTest {

    @Test
    public void insertTest(){}

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
