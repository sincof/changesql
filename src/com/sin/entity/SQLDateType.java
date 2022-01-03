package com.sin.entity;

public enum SQLDateType {
    // 这个应该是用不到了，不同的时间格式都是不一样的，如果相同的列用了不同的数据肯定会报BUG
    YEAR,       //YYYY                  1
    DATE,       // YYYY-MM-DD           3
    TIME,       // HH:MM:SS             3
    DATETIME,   // YYYY-MM-DD HH:MM:SS  8
    TIMESTAMP;  // YYYYMMDD HHMMSS      4

    public static boolean contains(String s) {
        for(SQLDateType dateType : SQLDateType.values()){
            if(dateType.name().equals(s))
                return true;
        }
        return false;
    }
}
