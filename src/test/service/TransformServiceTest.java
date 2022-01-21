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
import java.util.Arrays;

public class TransformServiceTest {
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
    public void compressionTest(){
        String str = "123456589123456789";
        for(int i = str.length(); i> 0; i--){
            StringBuilder sb = new StringBuilder(str.substring(0, i));
            sb.append(".");
            if(i < str.length())
                sb.append(str.substring(i, str.length()));
            System.out.println(sb.toString());
            System.out.println(TransformService.compressionFloat(sb.toString()));
        }
        String[] test = new String[]{"9999999.9", "999999.9", "1234569","123456909", "123456809", "123456509", "555555555"};
        for(int i = test.length - 1; i >= 0; i--){
            System.out.println(test[i]);
            System.out.println(TransformService.compressionFloat(test[i]));
        }
    }
}
