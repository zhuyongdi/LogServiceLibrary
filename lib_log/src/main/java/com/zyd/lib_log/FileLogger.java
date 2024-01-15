package com.zyd.lib_log;

public final class FileLogger extends SimpleLogger {

    @Override
    public void saveToFile(String tag, String msg, String fileName, boolean isAppend) {
        if (!accept(tag, msg)) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtils.createFile(new FileUtils.Callback() {
                    @Override
                    public void onSucceed() {
                        FileUtils.writeString(obtainLogString(tag, msg), fileName, isAppend);
                    }

                    @Override
                    public void onProgress(String msg) {

                    }

                    @Override
                    public void onFailed(String msg) {

                    }
                }, fileName, false);
            }
        }).start();
    }

}
