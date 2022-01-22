package com.sin.entity;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.Index;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class TableEntity {
    public List<String> tableDataPath;

    // 表格属性
    public String name;
    public int columnLen;
    public List<String> columns;
    //    public Map<String, ColumnDefinition> columnDefinitionMap;
    public CreateTable createTable;
    // public Set<Integer> keySet;
    // public Set<Integer> floatIndexSet;
    // have key index? (not primary key index)
    public boolean isKey = false;
    // have Key? if we have key we should use the key to find the data
    public boolean hasKey = false;
    // have column in the key which the type is float
    // public int[] floatValueIndex;
    // public int[] floatKeyName;
    public boolean[] colIsFloat;

    public boolean[] colIsDouble;

    // may be key / primary key index
    // which columns in the data is the key
    public int[] keyIndex;
    public boolean[] columnIsKey;
    // get the type of corresponding columns in the keyIndex
    public String[] keyType;
    // index of update in the columns
    public int updatedatIndex = 3;

    // PreparedStatement build string
    // public StringBuilder updateSB, insertSB, selectSB;
    public StringBuilder insertSB;
    private boolean doubleOutKey = false;

    public TableEntity(String createTableStatement) {
        this.tableDataPath = new LinkedList<>();
        this.columns = new LinkedList<>();
//        this.columnDefinitionMap = new HashMap<>();
        // keySet = new HashSet<>();
        // floatIndexSet = new HashSet<>();
        colIsFloat = new boolean[5];
        colIsDouble = new boolean[5];
        columnIsKey = new boolean[5];
        createTBDefine(createTableStatement);
    }

    public void createTBDefine(String statement) {
        // System.out.println(statement);
        // n order to get rid of the row change character
        // statement = statement.replaceAll("\\n", "");
        // statement = statement.replaceAll("`","");
        // because the JSQLParser have problem in parsing the sql statement
        // with the key index and not with the primary key index
        if (statement.contains("KEY") && !statement.contains("PRIMARY")) {
            // It is hard to type the chinese in the ubuntu so only english...
            // JSQLParser has a error in parsing the statement with KEY
            statement = statement.replaceFirst("KEY", "PRIMARY KEY");
            isKey = true;
        }

        // to lower case
        try {
            this.createTable = (CreateTable) CCJSqlParserUtil.parse(statement);
            this.name = createTable.getTable().getName();
//            List<Integer> floatIndex = new LinkedList<>();
            int columnCnt = 0;
            for (ColumnDefinition col : this.createTable.getColumnDefinitions()) {
                columns.add(col.getColumnName());
//                columnDefinitionMap.put(col.getColumnName(), col);

                if (col.getColDataType().getDataType().toLowerCase(Locale.ROOT).equals("float")) {
                    // floatIndexSet.add(columnCnt);
                    colIsFloat[columnCnt] = true;
                }
                if (col.getColDataType().getDataType().toLowerCase(Locale.ROOT).equals("double")) {
                    // floatIndexSet.add(columnCnt);
                    colIsDouble[columnCnt] = true;
                }
                columnCnt++;
            }
            this.columnLen = columnCnt;
//            floatValueIndex = new int[floatIndex.size()];
//            for (int i = 0; i < floatValueIndex.length; i++)
//                floatValueIndex[i] = floatIndex.get(i);

            // get the index of the table
            List<Index> indexList = this.createTable.getIndexes();
            // TODO: test whether the key is added in order
            List<String> keyNameList = new LinkedList<>();
            if (indexList != null) {
                for (Index index : indexList) {
                    String ins = index.getType().toLowerCase(Locale.ROOT);
                    if (ins.contains("key")) {
                        hasKey = true;
                        keyNameList.addAll(index.getColumnsNames());
                    }
                }
            }
            // 有primark key的情况下，除了primary key的所在列全部更新
            // 无primary key的情况下，只更新updated_at参数
            if (hasKey) {
                // initialize the array which store the index data
                keyIndex = new int[keyNameList.size()];
                keyType = new String[keyNameList.size()];

                // construct insert, build, update string for prepared statement
//                updateSB = new StringBuilder("update " + name + " set ");
                insertSB = new StringBuilder("insert into " + name + " values (");
//                selectSB = new StringBuilder("select updated_at from " + name + " where ");
                columnCnt = 0;
                boolean flag;
                for (String col : columns) {
//                    flag = true;
//                    for (String s : keyNameList)
//                        if (s.equals(col)) {
//                            flag = false;
//                            break;
//                        }
//                    if (flag)
//                        updateSB.append(col).append("=?, ");
                    insertSB.append("?,");
                    if (name.toLowerCase(Locale.ROOT).contains("updated_at")) {
                        this.updatedatIndex = columnCnt;
                    }
                    columnCnt++;
                }
                // 把updateSB的最后一个逗号删除掉，然后添加where 语句
//                updateSB.deleteCharAt(updateSB.length() - 1);
//                updateSB.append(" where ");
                // updateSB.append(" where ").append(primaryStat).append(";");
                // the last ; would like to lead error in the batch execute
                // 把insertSB的最后一个逗号删除掉，然后添加) 语句
                insertSB.deleteCharAt(insertSB.length() - 1);
                insertSB.append(")");

                int keyIndextmp = 0;
                for (String s : keyNameList) {
//                    if (keyIndextmp == 0) {
//                        selectSB.append(s).append(" =?");
////                        updateSB.append(s).append(" =?");
//                    } else {
//                        selectSB.append(" and ").append(s).append(" =?");
////                        updateSB.append(" and ").append(s).append(" =?");
//                    }
                    int index = 0;
                    for (ColumnDefinition col : this.createTable.getColumnDefinitions()) {
                        if (s.equals(col.getColumnName())) {
                            // keySet.add(index);
                            columnIsKey[index] = true;
                            keyIndex[keyIndextmp] = index;
                            keyType[keyIndextmp++] = col.getColDataType().getDataType();
                        }
                        index++;
                    }
                }
            } else {
                // do not have the key
//                selectSB = new StringBuilder("select updated_at from " + this.name + " where ");
//                updateSB = new StringBuilder("update " + this.name + " set updated_at = ? where ");
                insertSB = new StringBuilder("insert into " + this.name + " values (");
                int cnt = 0;
                for (String name : this.columns) {
                    insertSB.append("?,");
//                    if (name.toLowerCase(Locale.ROOT).contains("updated_at")) {
//                        this.updatedatIndex = cnt;
//                    } else {
//                        selectSB.append(name).append("=? and");
//                        updateSB.append(name).append("=? and");
//                    }
                    cnt++;
                }
                // 把insertSB的最后一个逗号删除掉，然后添加)
                insertSB.deleteCharAt(insertSB.length() - 1);
                insertSB.append(")");
                // 把selectSB delete final `and`
                // selectSB.deleteCharAt(selectSB.length() - 1);
//                selectSB.delete(selectSB.length() - 3, selectSB.length());
//                // 把updateSB delete final `and`
//                updateSB.delete(updateSB.length() - 3, updateSB.length());
//                // updateSB.deleteCharAt(updateSB.length() - 1);
            }
        } catch (JSQLParserException e) {
            System.out.println(statement);
            e.printStackTrace();
        }

        if(hasKey && statement.contains("double"))
            doubleOutKey = true;
    }

    // 默认认为第四列是updated_at
    public String columnToHash(String[] data) {
        StringBuilder sb = new StringBuilder();
        if (doubleOutKey) {
            for (int i = 0; i < data.length; i++) {
                if (i != updatedatIndex)
                    sb.append(data[i]);
            }
        } else if (hasKey) {
            for (int i = 0; i < data.length; i++) {
                if (i == updatedatIndex || !columnIsKey[i])
                    continue;
                sb.append(data[i]);
            }
        } else {
            for (int i = 0; i < data.length; i++) {
                if (i == updatedatIndex)
                    continue;
                sb.append(data[i]);
            }
        }
        return sb.toString();
    }
}
