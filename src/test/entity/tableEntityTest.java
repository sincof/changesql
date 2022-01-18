package test.entity;

import com.sin.entity.SQLNumberType;
import com.sin.entity.TableEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class tableEntityTest {
    @Test
    public void enumTest() {
        String[] numStrs = new String[]{"TINYINT", "SMALLINT", "MEDIUMINT", "INT", "BIGINT", "FLOAT", "DOUBLE", "DECIMAL"};
        for (String strs : numStrs) {
            Assertions.assertTrue(SQLNumberType.contains(strs.toLowerCase()));
        }
    }

    @Test
    public void parseTest() {
        String sql1 = """
                CREATE TABLE if not exists `1`  (
                  `id` bigint(20) unsigned NOT NULL,
                  `a` float NOT NULL DEFAULT '0',
                  `b` char(32) NOT NULL DEFAULT '',
                  `updated_at` datetime NOT NULL DEFAULT '2021-12-12 00:00:00',
                  PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8""";
        String sql2 = """
                CREATE TABLE if not exists `1`  (
                  `id` int(10) unsigned NOT NULL,
                  `a` float NOT NULL DEFAULT '0',
                  `b` char(32) NOT NULL DEFAULT '',
                  `updated_at` datetime NOT NULL DEFAULT '2021-12-12 00:00:00',
                  PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8""";
        String sql3 = """
             CREATE TABLE if not exists `4` (
               `id` bigint(20) unsigned NOT NULL,e
               `a` double NOT NULL DEFAULT '0',
               `b` char(32) NOT NULL DEFAULT '',
               `updated_at` datetime NOT NULL DEFAULT '2021-12-12 00:00:00',
                KEY (`id`,`b`)
             ) ENGINE=InnoDB DEFAULT CHARSET=utf8""";
        String sql5 = "";
        if(sql3.contains("KEY") && !sql3.contains("PRIMARY")) {
            sql5 = sql3.replaceFirst("KEY", "PRIMARY KEY");
        }
        String sql4 = " create table test.json_test (c1 int primary key, c2 varchar(20) not null, c3 json default null );";
        TableEntity tableEntity = new TableEntity(sql5);
        System.out.println();
    }
}
