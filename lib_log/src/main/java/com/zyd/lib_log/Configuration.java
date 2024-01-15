package com.zyd.lib_log;

import android.content.Context;
import android.os.Messenger;

/**
 * a. 在application中调用{@link Logger#addLogger(ILogger)}，参数为{@link FileLogger}
 * b. 在获取读写权限后调用
 *    {@link LogService#start(Context)}即可开启
 */
public class Configuration {

    /* LogService是一个多进程服务，这是用来和本程序进程通讯的Messenger */
    public static Messenger LOG_SERVICE_MESSENGER;

    /* 保存到本地的日志路径 */
    public static String DIR_LOG;

    /* 是否打印日志到LogService */
    public static boolean LOG_TO_LOG_SERVICE = true;

    /* 是否调用android的Log */
    public static boolean LOG_ANDROID = true;

}
