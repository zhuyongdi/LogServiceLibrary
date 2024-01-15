package com.zyd.lib_log;

import java.math.BigDecimal;

/**
 * 字符串工具类
 */
class StringUtil {

    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            System.out.println(str.charAt(i));
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isEmpty(String s) {
        return trimHeadTail(s).length() == 0;
    }

    /**
     * 裁剪字符串头部尾部的空格
     * trimHeadTail("  1  ")-->裁剪为"1"
     */
    public static String trimHeadTail(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!" ".equals(String.valueOf(s.charAt(i)))) {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }

    public static BigDecimal toBigDecimal(String s) {
        try {
            return new BigDecimal(s);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return new BigDecimal(0);
    }

    public static double toDouble(String s) {
        double result = 0d;
        try {
            result = Double.parseDouble(s);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return result;
    }

    public static boolean toBoolean(String s) {
        boolean result = false;
        try {
            result = Boolean.parseBoolean(s);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return result;
    }

    public static float toFloat(String s) {
        float result = 0f;
        try {
            result = Float.parseFloat(s);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return result;
    }

    public static int toInt(String s) {
        int result = 0;
        try {
            result = Integer.parseInt(s);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return result;
    }

    public static long toLong(String s) {
        long result = 0L;
        try {
            result = Long.parseLong(s);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return result;
    }

    /**
     * 是否是非法字符
     */
    public static boolean isInvalid(String src) {
        return src != null && src.matches("[`~!@#$^&*()=|{}':;',\\\\[\\\\].<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？]");
    }

    /**
     * 移除字符串末尾的非法字符
     * 示例：
     * 123??? -> 123
     * abc./  -> abc
     */
    public static String trimInvalidCharsAtTails(String src) {
        if (src == null) {
            return null;
        }
        if (src.isEmpty()) {
            return "";
        }
        int invalidIndex = -1;
        for (int i = src.length() - 1; i >= 0; i--) {
            if (isInvalid(src.charAt(i) + "")) {
                invalidIndex = i;
            } else {
                break;
            }
        }
        return invalidIndex == -1 ? src : src.substring(0, invalidIndex);
    }

    /**
     * 是否包含（忽略大小写）
     * @param src 原字符串
     * @param compare 要匹配的字符串
     * 示例
     * (123ABC456,abc) true
     */
    public static boolean containsIgnoreCase(String src, String compare) {
        if (src == null || compare == null) {
            return false;
        }
        int srcL = src.length();
        int cmpL = compare.length();
        if (srcL < cmpL) {
            return false;
        }
        if (src.contains(compare)) {
            return true;
        }
        if (src.equalsIgnoreCase(compare)) {
            return true;
        }
        int start = 0;
        int end = srcL - cmpL;
        for (int i = start; i <= end; i++) {
            String sub = src.substring(i, i + cmpL);
            if (sub.equalsIgnoreCase(compare)) {
                return true;
            }
        }
        return false;
    }

}
