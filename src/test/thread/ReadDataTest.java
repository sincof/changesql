package test.thread;

import com.sin.entity.DatabaseEntity;
import com.sin.entity.TableEntity;
import com.sin.service.DBManager;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Iterator;
import java.util.List;

public class ReadDataTest {
    @Test
    public void rdTest() throws IOException {
        DBManager dbManager = new DBManager("tmp/data"); // 获取所有的数据库信息
//        for(int i = 0; i < 4; i++){
//            for (DatabaseEntity curDB : list) {
//                for (TableEntity curTable : curDB.tableEntityMap.values()) {
//                    for (String path : curTable.tableDataPath) {
//                    }
//                }
//            }
//        }
        // InputStream 字节流
        // 字节流都没有自己完成readLine()的方法，如果我自己写，感觉时间复杂度可能不如bufferedread
        // FileInputStream
//        for(int i = 0; i < 4; i++){
//            Iterator<DatabaseEntity> itDB = list.iterator();
//            while(itDB.hasNext()){
//                DatabaseEntity curDB = itDB.next();
//                Iterator<TableEntity> itTable = curDB.tableEntityMap.values().iterator();
//                while(itTable.hasNext()){
//                    TableEntity curTable = itTable.next();
//                    Iterator<String> itPath = curTable.tableDataPath.listIterator();
//                    while(itPath.hasNext()){
//                        String path = itPath.next();
//                        // FileInputStream 没有readline 不好弄，需要自己去判断结尾 时间复杂度肯定提升
//                        FileInputStream fis = new FileInputStream(new File(path));
////                        String line =
//                    }
//                }
//            }
//        }

        // Reader 字符流
        // BufferedReader
        long startTime, endTime;
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 4; i++) {
            for (DatabaseEntity curDB : dbManager.dbStore.values()) {
                for (TableEntity curTable : curDB.tableEntityMap.values()) {
                    for (String path : curTable.tableDataPath) {
                        BufferedReader br = new BufferedReader(new FileReader(new File(path)));
                        String line = br.readLine();
                        String[] data;
                        while (line != null) {
                            data = line.split(",");
                            line = br.readLine();
                        }
                    }
                }
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println("BufferedReader: " + (endTime - startTime) + "ms");

        // BufferedInputStream 没有readLine方法
//        BufferedInputStream bufferedInputStream =new BufferedInputStream(new FileInputStream(new File("")));

        startTime = System.currentTimeMillis();
        for (int i = 0; i < 4; i++) {
            for (DatabaseEntity curDB : dbManager.dbStore.values()) {
                for (TableEntity curTable : curDB.tableEntityMap.values()) {
                    for (String path : curTable.tableDataPath) {
                        FileReader fileReader = new FileReader(new File(path));
                        LineNumberReader lineNumberReader = new LineNumberReader(fileReader);

                        String line = lineNumberReader.readLine();
                        String[] data;
                        while (line != null) {
                            data = line.split(" ");
                            line = lineNumberReader.readLine();
                        }
                    }
                }
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println("LineNumberReader: " + (endTime - startTime) + "ms");
    }
    // BufferedReader: 19271ms 19346ms 19371ms
    // LineNumberReader: 17000ms 17591ms 18401ms
    //
}
