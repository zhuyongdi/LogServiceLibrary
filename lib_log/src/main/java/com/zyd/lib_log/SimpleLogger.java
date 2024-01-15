package com.zyd.lib_log;

import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.blankj.utilcode.util.DeviceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SimpleLogger implements ILogger {

    private static final List<String> LOG_LIST = new Vector<>();
    private LogFilter mLogFilter;

    @Override
    public ILogger setLogFilter(LogFilter filter) {
        mLogFilter = filter;
        return this;
    }

    @Override
    public void log(String tag, String msg, Object... format) {
        if (!accept(tag, msg)) {
            return;
        }
        logInterval(tag, msg, format);
    }

    private void logInterval(String tag, String msg, Object... format) {
        if (!accept(tag, msg)) {
            return;
        }
        if (Configuration.LOG_TO_LOG_SERVICE) {
            String tagString = obtainLogString(tag, msg, format);
            boolean isSend = false;
            if (Configuration.LOG_SERVICE_MESSENGER != null) {
                try {
                    Message message = Message.obtain(null, LogService.HANDLER_OF_WHAT_NOTIFY_LOG_UPDATE);
                    Bundle bundle = new Bundle();
                    ArrayList<String> arrayList = new ArrayList<>();
                    if (!LOG_LIST.isEmpty()) {
                        arrayList.addAll(LOG_LIST);
                    }
                    arrayList.add(tagString);
                    bundle.putStringArrayList("log", arrayList);
                    message.setData(bundle);
                    Configuration.LOG_SERVICE_MESSENGER.send(message);
                    isSend = true;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            //如果没有发送成功，那么就添加到这里的集合中
            if (!isSend) {
                LOG_LIST.add(tagString);
            }
            //如果发送成功，清空集合
            else {
                LOG_LIST.clear();
            }
        }
        if (Configuration.LOG_ANDROID) {
            e(tag, msg, format);
        }
    }

    @Override
    public void i(String tag, String msg, Object... formats) {
        if (!accept(tag, msg)) {
            return;
        }
        Log.i(tag, ((formats == null || formats.length == 0) ? msg : String.format(msg, formats)));
    }

    @Override
    public void d(String tag, String msg, Object... formats) {
        if (!accept(tag, msg)) {
            return;
        }
        Log.d(tag, ((formats == null || formats.length == 0) ? msg : String.format(msg, formats)));
    }

    @Override
    public void e(String tag, String msg, Object... formats) {
        if (!accept(tag, msg)) {
            return;
        }
        Log.e(tag, ((formats == null || formats.length == 0) ? msg : String.format(msg, formats)));
    }

    @Override
    public void v(String tag, String msg, Object... formats) {
        if (!accept(tag, msg)) {
            return;
        }
        Log.v(tag, ((formats == null || formats.length == 0) ? msg : String.format(msg, formats)));
    }

    @Override
    public void saveToFile(String tag, String msg, String fileName, boolean isAppend) {
        this.e(tag, msg);
    }

    protected String obtainLogString(String tag, String msg, Object... formats) {
        return DateUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss.SSS") + " " + DeviceUtils.getManufacturer() + " Android" + Build.VERSION.SDK_INT + " " + tag + "：" + ((formats == null || formats.length == 0) ? msg : String.format(msg, formats)) + "\n";
    }

    protected boolean accept(String tag, String msg) {
        return mLogFilter == null || mLogFilter.accept(tag, msg);
    }
}
