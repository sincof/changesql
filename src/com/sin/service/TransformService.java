package com.sin.service;

import com.sin.entity.DatabaseEntity;
import com.sin.entity.TableEntity;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TransformService {
    public static void transformCSV2SQL(DBManager dbManager) throws IOException {
        for (DatabaseEntity database : dbManager.dbList) {
            for (TableEntity table : database.tableEntityMap.values()) {
                Map<String, RowEntity> row = new HashMap<>();
                File tableFile = new File(database.name + table.name);
                if (tableFile.exists()) {
                    System.out.println("Fuck, There are some error in this, " + database.name + table.name + " is created before");
                    return;
                }
                while(!tableFile.createNewFile()){}
                OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tableFile));
                if (table.hasKey) {
                    for (String dataPath : table.tableDataPath) {
                    }
                } else {
                }
            }
        }
    }

    private static class RowEntity {
        Integer index;
        String cmp;
    }

    public static String compressionFloat(String data) {
        String[] strs = data.split("\\.");
        // 数据是整数类型，无法压缩字符长度，让数据库处理吧
        // 数据是浮点类型，可能可以压缩字符长度
        if (strs[0].length() < 8 && strs.length == 2) {
            StringBuilder sb = new StringBuilder(strs[0]);
            sb.append(".");
            int left = 8 - strs[0].length();
            if (left > 0) {
                if (strs[1].length() < left)
                    sb.append(strs[1]);
                else
                    sb.append(strs[1].substring(0, left));
            }
            return sb.toString();
        } else if (strs[0].length() >= 8)
            return strs[0];
        return data;
    }
}