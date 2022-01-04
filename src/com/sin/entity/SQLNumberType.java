package com.sin.entity;

import java.util.Iterator;
import java.util.Map;

public enum SQLNumberType {
    // MYSQL是不是都支持对于各种类型的数据使用字符串类型插入？
    // 测试了一下， MYSQL 都支持对于各种类型的数据使用字符串类型插入
    // DECIMAL 是M
    TINYINT,    // 1 Bytes,小整数值
    SMALLINT,   // 2 Bytes,大整数值
    MEDIUMINT,  // 3 Bytes,大整数值
    INT,        // 4 Bytes INTEGER, 大整数值 撑死20位
    BIGINT,     // 8 Bytes, 极大整数值
    FLOAT,      // 4 Bytes,单精度 浮点数值
    DOUBLE,     // 8 Bytes,双精度 浮点数值
    DECIMAL;    // 对DECIMAL(M,D) ，如果M>D，为M+2否则为D+2,小数值 第一位代表总长度，第二位代表小数点后的值
                // 再插入的时候，既支持字符串类型插入，也支持数字类型的插入

    public static boolean contains(String str){
        // 为了程序的健壮 以防后面万一脑子抽了瞎几把传东西进来
        if(str == null || str.length() == 0)
            return false;
        str = str.toUpperCase();
        if("INTEGER".equals(str))
            return true;
        for(SQLNumberType sqlNumberType : SQLNumberType.values()){
            if(sqlNumberType.name().equals(str)){
                return true;
            }
        }
        return false;
    }
}
