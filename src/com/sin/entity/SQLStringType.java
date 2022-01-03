package com.sin.entity;

import java.util.Map;

public enum SQLStringType {
    CHAR,       // 0-255 bytes, 定长字符串
    VARCHAR,    // 0-65535 bytes 变长字符串
    TINYBLOB   // 0-255 bytes  不超过 255 个字符的二进制字符串
            {
                public static int defaultMaxLength() {
                    return 255;
                }
            },
    TINYTEXT   // 0-255 bytes 短文本字符串
            {
                public static int defaultMaxLength() {
                    return 255;
                }
            },
    BLOB       // 0-65 535 bytes 二进制形式的长文本数据
            {
                public static int defaultMaxLength() {
                    return 65535;
                }
            },
    TEXT       // 0-65 535 bytes 长文本数据
            {
                public static int defaultMaxLength() {
                    return 65535;
                }
            },
    MEDIUMBLOB // 0-16 777 215 bytes 二进制形式的中等长度文本数据
            {
                public static int defaultMaxLength() {
                    return 16777215;
                }
            },
    MEDIUMTEXT // 0-16 777 215 bytes 中等长度文本数据
            {
                public static int defaultMaxLength() {
                    return 16777215;
                }
            },
    LONGBLOB   // 0-4 294 967 295 bytes二进制形式的极大文本数据
            {
                public static int defaultMaxLength() {
                    return Integer.MAX_VALUE;
                }
            },
    LONGTEXT   // 0-4 294 967 295 bytes 极大文本数据
            {
                public static int defaultMaxLength() {
                    return Integer.MAX_VALUE;
                }
            };

    public static boolean contains(String str) {
        for (SQLStringType sqlStringType : SQLStringType.values()) {
            if (sqlStringType.name().equals(str)) {
                return true;
            }
        }
        return false;
    }
}
