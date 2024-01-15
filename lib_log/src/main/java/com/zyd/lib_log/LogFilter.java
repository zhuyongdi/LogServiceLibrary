package com.zyd.lib_log;

public interface LogFilter {

    default boolean accept(String tag,String msg) {
        return true;
    }

}
