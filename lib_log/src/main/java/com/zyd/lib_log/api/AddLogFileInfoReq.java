package com.zyd.lib_log.api;

import java.io.Serializable;

public class AddLogFileInfoReq implements Serializable {

    private String deviceId;
    private String logFileInfo;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getLogFileInfo() {
        return logFileInfo;
    }

    public void setLogFileInfo(String logFileInfo) {
        this.logFileInfo = logFileInfo;
    }
}
