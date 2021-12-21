package com.sin.entity;

import java.util.LinkedList;
import java.util.List;

public class TableEntity {
    public String name;
    public String constructSQL;
    // public String tableDefinationPath;
    public List<String> tableDataPath;

    public TableEntity(String name, String constructSQL) {
        this.name = name;
        this.constructSQL = constructSQL;
        // this.tableDefinationPath = tableDefinationPath;
        this.tableDataPath = new LinkedList<>();
    }
}
