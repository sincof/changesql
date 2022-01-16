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
                  `id` bigint unsigned NOT NULL,
                  `a` double NOT NULL DEFAULT '0',
                  `b` char(32) NOT NULL DEFAULT '',
                  `updated_at` datetime NOT NULL DEFAULT '2021-12-12 00:00:00',
                  KEY `id` (`id`,`b`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3""";
        TableEntity tableEntity = new TableEntity("a", sql2);
        System.out.println();
    }
}
