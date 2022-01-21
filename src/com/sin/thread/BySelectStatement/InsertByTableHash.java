//package com.sin.thread.BySelectStatement;
//
//import com.sin.entity.TableEntity;
//import com.sin.service.DBConnection;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.concurrent.Callable;
//
//public class InsertByTableHash implements Callable<Boolean> {
//    private final TableEntity tableEntity;
//    private final String dbName;
//    private final DBConnection dbConnection;
//
//    public InsertByTableHash(TableEntity tableEntity, String dbName, DBConnection dbConnection) {
//        this.tableEntity = tableEntity;
//        this.dbName = dbName;
//        this.dbConnection = dbConnection;
//        assert this.dbConnection != null;
//    }
//
//    // TODO：2. 需要将hashset替换成bloom filter！
//    // TODO：1.如果当前表存在float类型的键的话，需要将data下标位float的那个的有效值降低到6位
//    @Override
//    public Boolean call() {
//        System.out.println("INSERT TABLE " + tableEntity.name);
//        try (Connection connection = dbConnection.connectDB()) {
//            connection.setCatalog(dbName);
//
//            int MAX_BATCH_SIZE = 10000;
//            // 对所有插入的语句进行一个个计数
//            int updateCnt = 1, insertCnt = 1;
//            PreparedStatement selectStatement = connection.prepareStatement(tableEntity.selectSB.toString());
//            PreparedStatement updateStatement = connection.prepareStatement(tableEntity.updateSB.toString());
//            PreparedStatement insertStatement = connection.prepareStatement(tableEntity.insertSB.toString());
//            Set<Long> set = new HashSet<>();
//            Long hashValue;
//            if (tableEntity.hasKey) {
//                for (String dataPath : tableEntity.tableDataPath) {
//                    BufferedReader br = new BufferedReader(new FileReader(dataPath));
//                    String line = br.readLine();
//                    String[] data;
//                    while (line != null) {
//                        if (line.length() != 0) {
//                            data = line.split(",");
//                            hashValue = tableEntity.getHashValue(data);
//                            // 目的是将float类型的数据压缩成8位，保留精度
//                            for (int i = 0; i < data.length; i++) {
//                                if (tableEntity.colIsFloat[i]) {
//                                    data[i] = compressionFloat(data[i]);
////                                    String[] strs = data[i].split("\\.");
////                                    // 数据是整数类型，无法压缩字符长度，让数据库处理吧
////                                    // 数据是浮点类型，可能可以压缩字符长度
////                                    if (strs[0].length() < 8 && strs.length == 2) {
////                                        StringBuilder sb = new StringBuilder(strs[0]);
////                                        sb.append(".");
////                                        int left = 8 - strs[0].length();
////                                        if(left > 0){
////                                            if(strs[1].length() < left)
////                                                sb.append(strs[1]);
////                                            else
////                                                sb.append(strs[1].substring(0, left));
////                                        }
////                                        data[i] = sb.toString();
////                                    }
//                                }
//                            }
//                            if (!set.contains(hashValue)) {
//                                set.add(hashValue);
//                                for (int i = 0; i < data.length; i++)
//                                    insertStatement.setString(i + 1, data[i]);
//                                // 没有结果 就插入数据
//                                insertStatement.addBatch();
//                                insertCnt++;
//                            } else {
//                                int updataStatementIndex = 1;
//                                for (int i = 0; i < data.length; i++) {
//                                    if (!tableEntity.keySet.contains(i))
//                                        updateStatement.setString(updataStatementIndex++, data[i]);
//                                }
//                                // 设置where的主键等于语句
//                                int nowKeyIndex = 1;
//                                for (Integer i : tableEntity.keyIndex) {
//                                    updateStatement.setString(updataStatementIndex++, data[i]);
//                                    selectStatement.setString(nowKeyIndex, data[i]);
//                                    nowKeyIndex++;
//                                }
//                                ResultSet resultSet = selectStatement.executeQuery();
//                                if (resultSet.next()) {
//                                    // 结果集中存在数据 更新数据
//                                    String updated_at = resultSet.getString(1);
//                                    try {
//                                        // < 0 is updated_at is before the data[updatedatIndex]
//                                        // > 0 is updated_at is after the data[updatedatIndex]
//                                        if (compareTime(updated_at, data[tableEntity.updatedatIndex]) < 0) {
//                                            updateStatement.addBatch();
//                                            updateCnt++;
//                                        }
//                                    } catch (ParseException pe) {
//                                        System.out.println("updated_at in DB: " + updated_at + ", updated_at in data: " + data[tableEntity.updatedatIndex]);
//                                        pe.printStackTrace();
//                                    }
//                                }
//                                resultSet.close();
//                            }
//                        }
//                        line = br.readLine();
//                        if (updateCnt % MAX_BATCH_SIZE == 0) {
//                            updateStatement.executeBatch();
//                            updateCnt = 1;
//                        }
//                        if (insertCnt % MAX_BATCH_SIZE == 0) {
//                            insertStatement.executeBatch();
//                            insertCnt = 1;
//                        }
//                    }
//                    br.close();
//                }
//                if (updateCnt != 1) {
//                    updateStatement.executeBatch();
//                }
//                if (insertCnt != 1) {
//                    insertStatement.executeBatch();
//                }
//                try {
//                    insertStatement.close();
//                    selectStatement.close();
//                    updateStatement.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                for (String dataPath : tableEntity.tableDataPath) {
//                    BufferedReader br = new BufferedReader(new FileReader(dataPath));
//                    String line = br.readLine();
//                    String[] data;
//                    while (line != null) {
//                        if (line.length() != 0) {
//                            data = line.split(",");
//                            hashValue = tableEntity.getHashValue(data);
//                            // 目的是将float类型的数据压缩成8位，保留精度
//                            for (int i = 0; i < data.length; i++) {
//                                if (tableEntity.colIsFloat[i]) {
//                                    data[i] = compressionFloat(data[i]);
////                                    String[] strs = data[i].split("\\.");
////                                    // 数据是整数类型，无法压缩字符长度，让数据库处理吧
////                                    // 数据是浮点类型，可能可以压缩字符长度
////                                    if (strs[0].length() < 8 && strs.length == 2) {
////                                        StringBuilder sb = new StringBuilder(strs[0]);
////                                        sb.append(".");
////                                        int left = 8 - strs[0].length();
////                                        if(left > 0){
////                                            if(strs[1].length() < left)
////                                                sb.append(strs[1]);
////                                            else
////                                                sb.append(strs[1].substring(0, left));
////                                        }
////                                        data[i] = sb.toString();
////                                    }
//                                }
//                            }
//                            if (!set.contains(hashValue)) {
//                                set.add(hashValue);
//                                for (int i = 0; i < data.length; i++)
//                                    insertStatement.setString(i + 1, data[i]);
//                                // 没有结果 就插入数据
//                                insertStatement.addBatch();
//                                insertCnt++;
//                            } else {
//                                for (int i = 0; i < data.length; i++) {
//                                    if (i < tableEntity.updatedatIndex) {
//                                        selectStatement.setString(i + 1, data[i]);
//                                        updateStatement.setString(i + 2, data[i]);
//                                    } else if (i == tableEntity.updatedatIndex)
//                                        updateStatement.setString(1, data[i]);
//                                    else {
//                                        selectStatement.setString(i, data[i]);
//                                        updateStatement.setString(i + 2, data[i]);
//                                    }
//                                }
//                                ResultSet resultSet = selectStatement.executeQuery();
//                                if (resultSet.next()) {
//                                    String updated_at = resultSet.getString(1);
//                                    try {
//                                        // < 0 is updated_at is before the data[updatedatIndex]
//                                        // > 0 is updated_at is after the data[updatedatIndex]
//                                        if (compareTime(updated_at, data[tableEntity.updatedatIndex]) < 0) {
//                                            updateStatement.addBatch();
//                                            updateCnt++;
//                                        }
//                                    } catch (ParseException pe) {
//                                        System.out.println("updated_at in DB: " + updated_at + ", updated_at in data: " + data[tableEntity.updatedatIndex]);
//                                        pe.printStackTrace();
//                                    }
//                                }
//                            }
//                        }
//                        line = br.readLine();
//                        if (updateCnt % MAX_BATCH_SIZE == 0) {
//                            updateStatement.executeBatch();
//                            updateCnt = 1;
//                        }
//                        if (insertCnt % MAX_BATCH_SIZE == 0) {
//                            insertStatement.executeBatch();
//                            insertCnt = 1;
//                        }
//                    }
//                    if (updateCnt != 1) {
//                        updateStatement.executeBatch();
//                    }
//                    if (insertCnt != 1) {
//                        insertStatement.executeBatch();
//                    }
//                    br.close();
//                }
//                try {
//                    insertStatement.close();
//                    selectStatement.close();
//                    updateStatement.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                    return false;
//                }
//            }
//        } catch (SQLException | IOException ex) {
//            ex.printStackTrace();
//            return false;
//        }
//        return true;
//    }
//
//
//    // a < b: return true
//    // a > b: return false
//    public int compareTime(String oriS, String newS) throws ParseException {
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
//        Date oriD = df.parse(oriS), newD = df.parse(newS);
//        return oriD.compareTo(newD);
//    }
//
//    public String compressionFloat(String data){
//        String[] strs = data.split("\\.");
//        // 数据是整数类型，无法压缩字符长度，让数据库处理吧
//        // 数据是浮点类型，可能可以压缩字符长度
//        if (strs[0].length() < 8 && strs.length == 2) {
//            StringBuilder sb = new StringBuilder(strs[0]);
//            sb.append(".");
//            int left = 8 - strs[0].length();
//            if(left > 0){
//                if(strs[1].length() < left)
//                    sb.append(strs[1]);
//                else
//                    sb.append(strs[1].substring(0, left));
//            }
//            return sb.toString();
//        }else if(strs[0].length() >= 8)
//            return strs[0];
//        return data;
//    }
//}
