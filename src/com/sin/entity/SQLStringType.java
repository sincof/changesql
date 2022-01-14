package com.sin.entity;

import java.util.Map;

public enum SQLStringType {
    CHAR,       // 0-255 bytes, 定长字符串
    VARCHAR,    // 0-65535 bytes 变长字符串
    TINYBLOB,   // 0-255 bytes  不超过 255 个字符的二进制字符串,
    TINYTEXT   // 0-255 bytes 短文本字符串
    ,
    BLOB       // 0-65 535 bytes 二进制形式的长文本数据
    ,
    TEXT       // 0-65 535 bytes 长文本数据
    ,
    MEDIUMBLOB // 0-16 777 215 bytes 二进制形式的中等长度文本数据
    ,
    MEDIUMTEXT // 0-16 777 215 bytes 中等长度文本数据
    ,
    LONGBLOB   // 0-4 294 967 295 bytes二进制形式的极大文本数据
    ,
    LONGTEXT   // 0-4 294 967 295 bytes 极大文本数据
    ;

    public static boolean contains(String str) {
        for (SQLStringType sqlStringType : SQLStringType.values()) {
            if (sqlStringType.name().equals(str)) {
                return true;
            }
        }
        return false;
    }
}
