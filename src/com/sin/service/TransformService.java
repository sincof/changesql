package com.sin.service;

import com.mysql.cj.MysqlType;
import com.sin.entity.DatabaseEntity;
import com.sin.entity.TableEntity;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class TransformService {
    public static void transformCSV2SQL(DBManager dbManager) throws IOException, ParseException {
        for (DatabaseEntity database : dbManager.dbList) {
            for (TableEntity table : database.tableEntityMap.values()) {
                int writeCnt = 0;
                Map<String, RowEntity> rowMap = new HashMap<>();
                PriorityQueue<Integer> duplicateRow = new PriorityQueue<>();
                // for every table in the database, create a new file for it
                File tableFile = new File(database.name + table.name);
                if (tableFile.exists()) {
                    System.out.println("Fuck, There are some error in this, " + database.name + table.name + " is created before");
                    return;
                }
                if (!tableFile.createNewFile()) {
                    System.out.println("Fuck, creating new file fails!");
                    return;
                }
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tableFile));
                for (String dataPath : table.tableDataPath) {
                    File dataFile = new File(dataPath);
                    BufferedReader br = new BufferedReader(new FileReader(dataFile));
                    String line = br.readLine().trim();
                    while (line != null) {
                        String[] data = line.trim().split(",");
                        if (data.length != table.columnLen && line.charAt(line.length() - 1) != ',') {
                            System.out.println("ERROR number of columns is not right" + database.name + table.name + ": " + line);
                            continue;
                        }
//                        for (int i = 0; i < table.columnLen; i++) {
//                            if (table.colIsFloat[i])
//                                data[i] = compressionFloat(data[i]);
//                        }
                        String hash = table.columnToHash(data);
                        if (rowMap.containsKey(hash)) {
                            String[] tmpStrs = rowMap.get(hash).data.split(",");
                            if (tmpStrs.length > table.updatedatIndex) {
                                String time = tmpStrs[table.updatedatIndex];
                                if (compareTime(time, data[table.updatedatIndex]) < 0) {
                                    RowEntity row = rowMap.get(hash);
                                    if (!row.flag) {
                                        duplicateRow.add(row.index);
                                        row.flag = true;
                                    }
                                    row.data = line;
                                }
                            }
                        } else {
                            StringBuilder sb = new StringBuilder(data[0]);
                            for (int i = 1; i < data.length; i++)
                                sb.append(",").append(data[i]);
                            RowEntity newRow = new RowEntity(writeCnt++, sb.toString());
                            rowMap.put(hash, newRow);
                            sb.append("\n");
                            writer.write(sb.toString());
                            if (data[1].contains(":")) {
                                System.out.println("Error data contain illegal character! " + data[1]);
                                return;
                            }
                        }
                        line = br.readLine();
                    }
                    br.close();
//                    dataFile.delete();
                }
                writer.close();

                File updateFile = new File(database.name + table.name + String.valueOf(2));
                if (updateFile.exists()) {
                    System.out.println("Who create the fucking file!" + database.name + table.name + String.valueOf(2));
                    return;
                }
                if (!updateFile.createNewFile()) {
                    System.out.println("Who create the fucking file!" + database.name + table.name + String.valueOf(2));
                    return;
                }
                int readCnt = 0;
                BufferedReader br = new BufferedReader(new FileReader(tableFile));
                writer = new OutputStreamWriter(new FileOutputStream(updateFile));
                String line = br.readLine();
                while (line != null) {
                    if (!duplicateRow.isEmpty() && readCnt == duplicateRow.peek()) {
                        duplicateRow.poll();
                        String hash = table.columnToHash(line.split(","));
                        writer.write(rowMap.get(hash).data + "\n");
                    } else
                        writer.write(line + "\n");
                    readCnt++;
                    line = br.readLine();
                }
                tableFile.delete();
                br.close();
                writer.close();
            }
        }
    }

    private static class RowEntity {
        int index;
        boolean flag;
        String data;

        public RowEntity(int index, String data) {
            this.index = index;
            this.data = data;
            flag = false;
        }
    }

    // a < b: return true
    // a > b: return false
    public static int compareTime(String oriS, String newS) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date oriD = df.parse(oriS), newD = df.parse(newS);
        return oriD.compareTo(newD);
    }

    // 为什么要要对float进行转化呢？
    // 1. 减少位数
    // 2. 举例子：主键是id bigint a float，(1, 123456001), (1, 123456002) 如果按照之前的的来说的话，这两个是不同主键，
    //    但是插入到MYSQL数据库就会出现duplicate的错误，由于在数据库里面 float精度只有6位，也就是说 实际上存储着(1, 123456000)
    // 还可能要对double进行转化成符合mysql 数据库的数据，看能不能跑过数据把！
    public static String compressionFloat(String data) {
        String[] strs = data.split("\\.");
        int len0 = strs[0].length();
        // 数据是整数类型，无法压缩字符长度，让数据库处理吧
        // 数据是浮点类型，可能可以压缩字符长度
        if (len0 < 6 && strs.length == 2) {
            StringBuilder sb = new StringBuilder(strs[0]);
            sb.append(".");
            int left = 6 - len0;
            if (strs[1].length() <= left)
                sb.append(strs[1]);
            else {
                if (strs[1].charAt(left) > '4' || (strs[1].length() > (left + 3) && strs[1].startsWith("999", left))) {
                    // have no idea about this! -> strs[1].charAt(left - 1) + 1
                    char[] ch = strs[1].substring(0, left).toCharArray();
                    boolean flag = true;
                    for (int i = ch.length - 1; i >= 0; i--) {
                        if (ch[i] == '9') {
                            ch[i] = '0';
                        } else {
                            ch[i] += 1;
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        ch = strs[0].toCharArray();
                        for (int i = ch.length - 1; i >= 0; i--) {
                            if (ch[i] == '9') {
                                ch[i] = '0';
                            } else {
                                ch[i] += 1;
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            ch = new char[len0];
                            return 1 + String.valueOf(ch).replace('\0', '0');
                        }
                        return String.valueOf(ch);
                    }
                    return strs[0] + "." + String.valueOf(ch);
                } else
                    sb.append(strs[1].substring(0, left));
            }
            return sb.toString();
        } else if (len0 == 6) {
            if ((strs[1].length() >= 1 && strs[1].charAt(0) > '4') || (strs[1].length() > 3 && strs[1].startsWith("999", 1))) {
                char[] ch = strs[0].toCharArray();
                boolean flag = true;
                for (int i = ch.length - 1; i >= 0; i--) {
                    if (ch[i] == '9') {
                        ch[i] = '0';
                    } else {
                        ch[i] += 1;
                        flag = false;
                        break;
                    }
                }
                if (flag)
                    return "1000000";
                return String.valueOf(ch);
            }
            return strs[0];
        } else if (len0 > 6) {
            StringBuilder sb;
            if (strs[0].charAt(6) > '5' || (strs[0].length() > 9 && strs[0].substring(5, 8).equals("999"))) {
                char[] ch = strs[0].substring(0, 6).toCharArray();
                boolean flag = true;
                for (int i = ch.length - 1; i >= 0; i--) {
                    if (ch[i] == '9') {
                        ch[i] = '0';
                    } else {
                        ch[i] += 1;
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    ch = new char[len0 - 6];
                    return "1000000" + String.valueOf(ch).replace('\0', '0');
                }
                char[] kong = new char[len0 - 6];
                return String.valueOf(ch) + String.valueOf(kong).replace('\0', '0');
            } else
                sb = new StringBuilder(strs[0].substring(0, 5)).append(strs[0].charAt(5));
            char[] ch = new char[len0 - 6];
            sb.append(String.valueOf(ch).replace('\0', '0'));
            return sb.toString();
        }
        return data;
    }

    public void convert() {
        MysqlType.FLOAT.getPrecision();
    }
}