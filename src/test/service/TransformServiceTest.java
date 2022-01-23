package test.service;

import com.sin.service.DBConnection;
import com.sin.service.TransformService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class TransformServiceTest {
    // a < b: return true
    // a > b: return false
    public int compareTime(String oriS, String newS) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date oriD = df.parse(oriS), newD = df.parse(newS);
        System.out.println("Original: " + oriD.toString() + " New: " + newD.toString());
        return oriD.compareTo(newD);
    }

    @Test
    public void compareTimeTest() throws ParseException {
        String[] time = new String[]{"2021-03-01 06:10:11", "2021-03-01 06:10:12",
                "2021-04-13 14:33:05", "2021-04-13 14:33:06",
                "2021-04-16 16:22:25", "2021-04-16 16:22:26",
                "2021-06-01 09:33:30 ", "2021-06-01 09:33:31",
                "2021-06-28 07:25:28", "2021-06-28 07:25:30",
                "2021-06-29 07:25:28", "2021-06-29 07:25:30",
                "2021-07-16 11:58:48", "2021-07-16 11:58:49",
                "2021-08-11 12:23:58", "2021-08-11 12:24:59",
                "2021-08-31 12:23:58", "2021-08-31 23:24:59",
                "2021-10-31 23:59:58", "2021-10-31 23:59:59",
                "2021-11-1 0:0:0", "2021-11-1 10:0:0",
                "2021-11-11 20:59:23", "2021-11-11 21:59:24",
                "2021-11-14 20:59:23", "2021-11-14 21:59:24",
                "2021-11-30 23:59:59", "2021-12-1 0:0:0",
                "2021-12-31 23:59:59", "2022-1-1 0:0:0"};
        for (String value : time) System.out.println("370895164,10.720104677469088,fuck," + value);
        for (int i = 0; i < time.length; i++) {
            for (int j = i + 1; j < time.length; j++) {
                Assertions.assertTrue(TransformService.aIsAfterB(time[i], time[j]));
            }
        }
        System.out.println("-------------");
        for (String s : time) Assertions.assertFalse(TransformService.aIsAfterB(s, s));
        System.out.println("-------------");
        for (int i = time.length - 1; i > 0; i--)
            for (int j = i - 1; j >= 0; j--)
                Assertions.assertFalse(TransformService.aIsAfterB(time[i], time[j]));

//        for (int i = 0; i < time.length; i++) {
//            for (int j = i + 1; j < time.length; j++) {
//                Assertions.assertTrue(TransformService.compareTime(time[i], time[j]) < 0);
//            }
//        }
//        System.out.println("-------------");
//        for (String s : time) Assertions.assertEquals(0, TransformService.compareTime(s, s));
//        System.out.println("-------------");
//        for (int i = time.length - 1; i > 0; i--)
//            for (int j = i - 1; j >= 0; j--)
//                Assertions.assertTrue(TransformService.compareTime(time[i], time[j]) > 0);
    }

    @Test
    public void tst() throws IOException, SQLException {
        // precision test
        // --data_path /tmp/data --dst_ip 121.41.55.205 --dst_port 3306 --dst_user root --dst_password changesql
        DBConnection dbConnection = new DBConnection("121.41.55.205", "3306", "root", "changesql");
        Connection conn = dbConnection.connectDB();
        conn.setCatalog("a");
        Statement statement = conn.createStatement();
//        ResultSet resultSet = statement.executeQuery("SELECT a FROM `2`;");
//        statement.execute("DROP table `2`;");
//        statement.execute(
//                ""
//        );
//        int len = resultSet1.getMetaData().getColumnCount();
//        while(resultSet1.next()){
//            System.out.println(resultSet1.getString(1));
//        }
    }

    @Test
    public void compressionTest() {
        String str = "123456589123456789";
        for (int i = str.length(); i > 0; i--) {
            StringBuilder sb = new StringBuilder(str.substring(0, i));
            sb.append(".");
            if (i < str.length())
                sb.append(str.substring(i, str.length()));
            System.out.println(sb.toString());
            System.out.println(TransformService.compressionFloat(sb.toString()));
        }
        String[] test = new String[]{"9999999.9", "999999.9", "1234569", "123456909", "123456809", "123456509", "555555555"};
        for (int i = test.length - 1; i >= 0; i--) {
            System.out.println(test[i]);
            System.out.println(TransformService.compressionFloat(test[i]));
        }
    }
}
