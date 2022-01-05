package test.service;

import com.sin.service.DBConnection;
import com.sin.service.DBManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DBManagerTest {
    //    --data_path /tmp/data --dst_ip 121.41.55.205 --dst_port 3306 --dst_user root --dst_password changesql
    String DstIP = "121.41.55.205";
    String DstPort = "3306";
    String DstUser = "root";
    String DstPassword = "changesql";

    public DBManagerTest() {
    }

    @Test
    public void createDBaTB() throws IOException {
        DBConnection dbconn = new DBConnection(DstIP, DstPort, DstUser, DstPassword);
        DBManager dbManager = new DBManager("tmp/data/");
        try(Connection conn = dbconn.connectDB()) {
            Assertions.assertEquals(1, dbManager.createDB(conn));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
