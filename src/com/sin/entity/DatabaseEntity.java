package com.sin.entity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DatabaseEntity {
    public String name;
//    public List<String> availDatabasePath;
    public List<TableEntity> tableEntities;
    public Map<String, TableEntity> tableEntityMap;

    public DatabaseEntity(String name) {
        this.name = name;
//        availDatabasePath = new LinkedList<>();
        tableEntities = new LinkedList<>();
        tableEntityMap = new HashMap<>();
    }
}
