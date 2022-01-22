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
    public CreateTable createTable;
    // have key index? (not primary key index)
    public boolean isKey = false;
    // have Key? if we have key we should use the key to find the data
    public boolean hasKey = false;
    // have column in the key which the type is float
    public boolean[] colIsFloat;
    // have column in the key which the type is double
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
    public StringBuilder insertSB;
    private boolean doubleOutKey = false;

    public TableEntity(String createTableStatement) {
        this.tableDataPath = new LinkedList<>();
        this.columns = new LinkedList<>();
        colIsFloat = new boolean[5];
        colIsDouble = new boolean[5];
        columnIsKey = new boolean[5];
        createTBDefine(createTableStatement);
    }

    public void createTBDefine(String statement) {
        if (statement.contains("KEY") && !statement.contains("PRIMARY")) {
            // It is hard to type the chinese in the ubuntu so only english...
            // JSQLParser has a error in parsing the statement with KEY
            statement = statement.replaceFirst("KEY", "PRIMARY KEY");
            isKey = true;
        }

        try {
            this.createTable = (CreateTable) CCJSqlParserUtil.parse(statement);
            this.name = createTable.getTable().getName();
            int columnCnt = 0;
            for (ColumnDefinition col : this.createTable.getColumnDefinitions()) {
                columns.add(col.getColumnName());
                if (col.getColDataType().getDataType().toLowerCase(Locale.ROOT).equals("float")) {
                    colIsFloat[columnCnt] = true;
                }
                if (col.getColDataType().getDataType().toLowerCase(Locale.ROOT).equals("double")) {
                    colIsDouble[columnCnt] = true;
                }
                columnCnt++;
            }
            this.columnLen = columnCnt;
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
                    insertSB.append("?,");
                    if (name.toLowerCase(Locale.ROOT).contains("updated_at")) {
                        this.updatedatIndex = columnCnt;
                    }
                    columnCnt++;
                }
                insertSB.deleteCharAt(insertSB.length() - 1);
                insertSB.append(")");

                int keyIndextmp = 0;
                for (String s : keyNameList) {
                    int index = 0;
                    for (ColumnDefinition col : this.createTable.getColumnDefinitions()) {
                        if (s.equals(col.getColumnName())) {
                            columnIsKey[index] = true;
                            keyIndex[keyIndextmp] = index;
                            keyType[keyIndextmp++] = col.getColDataType().getDataType();
                        }
                        index++;
                    }
                }
            } else {
                insertSB = new StringBuilder("insert into " + this.name + " values (");
                int cnt = 0;
                for (String name : this.columns) {
                    insertSB.append("?,");
                    cnt++;
                }
                // 把insertSB的最后一个逗号删除掉，然后添加)
                insertSB.deleteCharAt(insertSB.length() - 1);
                insertSB.append(")");
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
