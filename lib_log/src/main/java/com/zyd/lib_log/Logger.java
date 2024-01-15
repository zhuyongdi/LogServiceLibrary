package com.zyd.lib_log;

import android.util.ArraySet;

import java.util.Set;

public class Logger {

    private static final Set<ILogger> LOGGERS = new ArraySet<>();
    private static boolean debug = true;

    public static void setDebug(boolean debug) {
        Logger.debug = debug;
    }

    public static void addLogger(ILogger logger) {
        if (logger != null) {
            LOGGERS.add(logger);
        }
    }

    public static void i(String tag, String msg) {
        if (!debug) {
            return;
        }
        for (ILogger logger : LOGGERS) {
            logger.i(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (!debug) {
            return;
        }
        for (ILogger logger : LOGGERS) {
            logger.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (!debug) {
            return;
        }
        for (ILogger logger : LOGGERS) {
            logger.e(tag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (!debug) {
            return;
        }
        for (ILogger logger : LOGGERS) {
            logger.v(tag, msg);
        }
    }

    public static void log(String tag, String msg, Object... format) {
        if (!debug) {
            return;
        }
        for (ILogger logger : LOGGERS) {
            logger.log(tag, msg, format);
        }
    }

    public static void saveToFile(String tag, String msg, String fileName, boolean isAppend) {
        if (!debug) {
            return;
        }
        for (ILogger logger : LOGGERS) {
            logger.saveToFile(tag, msg, fileName, isAppend);
        }
    }

}
