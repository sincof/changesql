package com.sin.util;

public class HashUtil {
    // transform a list of data to the
    public static Long ArrayString(String[] col, int cnt) {
        if (cnt == 0)
            cnt = 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cnt && i < col.length; i++) {
            if(isNumber(col[i])){
            }
        }
        return Long.MAX_VALUE;
    }

    public static boolean isInteger(String col){
        if(col == null || col.length() == 0)
            return false;
        String regex = "[-+]?[0-9]+";
        return col.matches(regex);
    }

    public static boolean isFloat(String col){
        if(col == null || col.length() == 0)
            return false;
        String regex = "[-+]?[0-9]+\\.[0-9]+";
        return col.matches(regex);
    }

    public static boolean isNumber(String col) {
        if (col == null || col.length() == 0)
            return false;
        String regex = "[-+]?[0-9]+\\.?[0-9]*";
        return col.matches(regex);
    }
}