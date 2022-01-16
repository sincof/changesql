package com.sin.entity;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TableEntity {
    public List<String> tableDataPath;

    // 表格属性
    public List<String> columns;
    public Map<String, ColumnDefinition> columnDefinitionMap;
    public CreateTable createTable;

    public TableEntity(String name, String createTableStatement) {
        this.tableDataPath = new LinkedList<>();
        this.columns = new LinkedList<String>();
        this.columnDefinitionMap = new HashMap<String, ColumnDefinition>();
        createTBDefine(createTableStatement);
    }

    public void createTBDefine(String statement) {
        System.out.println(statement);
        statement = statement.replaceAll("\\n", "");
//        statement = statement.replaceAll("`","");
        try {
            this.createTable = (CreateTable) CCJSqlParserUtil.parse(statement);
            for (ColumnDefinition col : this.createTable.getColumnDefinitions()) {
                columns.add(col.getColumnName());
                columnDefinitionMap.put(col.getColumnName(), col);
            }
        } catch (JSQLParserException e) {
            System.out.println(statement);
            e.printStackTrace();
        }
    }

    public void addTBDefine(String createStatement) {
        System.out.println(createStatement);
        try {
            CreateTable otherCreateTable = (CreateTable) CCJSqlParserUtil.parse(createStatement);
            for (ColumnDefinition col : otherCreateTable.getColumnDefinitions()) {
                String colName = col.getColumnName();
                if (columnDefinitionMap.containsKey(colName) && compareColumnType(columnDefinitionMap.get(colName), col)) {
                    columnDefinitionMap.put(colName, col);
                }
            }
        } catch (JSQLParserException e) {
            System.out.println(createStatement);
            e.printStackTrace();
        }
    }

    public boolean compareColumnType(ColumnDefinition orig, ColumnDefinition newO) {
        // getDataType 是直接获得Data的Type没有后缀，既是没有后面的关于字符类似char长度的字段， 比较的时候需要注意
        // 获取类型的前缀
        String origS = orig.getColDataType().getDataType().toUpperCase(), newOS = newO.getColDataType().getDataType().toUpperCase();
        if ("INTEGER".equals(origS))
            origS = "INT";
        if ("INTEGER".equals(newOS))
            newOS = "INT";
        // 个人愚见，Date类型的数据差距太大，压根就不是精度问题了，如果采用了不同的Date类型，这两个类型的数据一定是无法成功合并的
        // 肯定不存在相同表下面的列数据类型不同的情况，那样肯定是错的
        if (SQLNumberType.contains(origS) && SQLNumberType.contains(newOS)) {
            SQLNumberType origT = SQLNumberType.valueOf(origS);
            SQLNumberType newOT = SQLNumberType.valueOf(newOS);
            if (origT.ordinal() > newOT.ordinal()) {
                return false;
            } else if (origT.ordinal() < newOT.ordinal()) {
                return true;
            } else if (origT == SQLNumberType.DECIMAL) {
                // 比较他们的参数，优先选择参数值大的 DECIMAL 含有两个参数 M N， 选择M大的, DECIMAL一定会有参数么？不一定，还得判断下
                // 是给定参数的取值范围大，还是未给定的参数范围大？
                // 两者中都取最大的
                List<String> origPar = orig.getColDataType().getArgumentsStringList();
                List<String> newOPar = newO.getColDataType().getArgumentsStringList();
                if (origPar == null && newOPar != null)
                    return true;
                else if (origPar != null && newOPar == null)
                    return false;
                else if (origPar == null)
                    return false;
                int[] o = new int[2], n = new int[2];
                int cnt = 0;
                for (String s : origPar) {
                    o[cnt++] = Integer.parseInt(s);
                }
                cnt = 0;
                for (String s : newOPar) {
                    n[cnt++] = Integer.parseInt(s);
                }
                o[0] = Math.max(o[0], n[0]);
                o[1] = Math.max(o[1], n[1]);
                List<String> newPar = new LinkedList<>();
                newPar.add(String.valueOf(o[0]));
                newPar.add(String.valueOf(o[1]));
                orig.getColDataType().setArgumentsStringList(newPar);
            }
            return false;
        } else if (SQLStringType.contains(origS) && SQLStringType.contains(newOS)) {
            // 如果存在参数列表就用参数列表中值大的，如果没有参数列表，就按表中的顺序来， String类型的参数要么只有一个，要么没有
            List<String> origPar = orig.getColDataType().getArgumentsStringList();
            List<String> newOPar = newO.getColDataType().getArgumentsStringList();
            SQLStringType origT = SQLStringType.valueOf(origS);
            SQLStringType newOT = SQLStringType.valueOf(newOS);
            if (origPar != null && newOPar != null) {
                // 如果都有长度要求，直接设置长度为最长得到那一个
                int o = Integer.parseInt(origPar.get(0)), n = Integer.parseInt(newOPar.get(0));
                return o < n;
            } else if (origPar != null) {
                // 如果只有original存在长度要求
                int o = Integer.parseInt(origPar.get(0));
                return true;
            } else if (newOPar != null) {
                // 如果只有新来的由长度要求
                return false;
            } else {
                // 如果没有下标要求，直接比较他们在index的下标位置
                return origT.ordinal() < newOT.ordinal();
            }
        }
        return false;
    }
}
