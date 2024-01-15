package com.zyd.lib_log;

public interface ILogger {
    ILogger setLogFilter(LogFilter filter);
    void log(String tag,String msg,Object... format);
    void i(String tag,String msg,Object... format);
    void d(String tag,String msg,Object... format);
    void e(String tag,String msg,Object... format);
    void v(String tag,String msg,Object... format);
    void saveToFile(String tag, String msg, String fileName, boolean isAppend);
}
