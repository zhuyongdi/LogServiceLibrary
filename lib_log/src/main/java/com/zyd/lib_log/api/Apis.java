package com.zyd.lib_log.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zyd.lib_log.MessageListener;
import com.zyd.lib_mine.utils.HttpURLConnectionUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Apis {

    /**
     * 上传日志
     * @param logUrl 日志url
     * @param callback 回调
     */
    public static void addLog(final String logUrl, final MessageListener<BaseResponse<Boolean>> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://43.139.71.125:8879/custom/log/addLog";
                SaveLogReq req = new SaveLogReq();
                req.setLogUrl(logUrl);
                req.setProjectName(AppUtils.getAppName());
                req.setDeviceId(DeviceUtils.getUniqueDeviceId());
                String json = new Gson().toJson(req);
                HttpURLConnectionUtil.sendPost(HttpURLConnectionUtil.SendType.JSON, url, json, null, new HttpURLConnectionUtil.Callback() {
                    @Override
                    public void onSuccess(String result) {
                        if (result == null) {
                            if (callback != null) {
                                callback.onNewMessage(BaseResponse.createFailBean("result is null"));
                            }
                            return;
                        }
                        BaseResponse<Boolean> response = new Gson().fromJson(result, new TypeToken<BaseResponse<Boolean>>() {
                        }.getType());
                        if (response == null) {
                            if (callback != null) {
                                callback.onNewMessage(BaseResponse.createFailBean("transfer gson fail"));
                            }
                            return;
                        }
                        if (response.getCode() != 200) {
                            if (callback != null) {
                                callback.onNewMessage(BaseResponse.createFailBean("not success,code=" + response.getCode() + ",msg=" + response.getMsg()));
                            }
                            return;
                        }
                        if (callback != null) {
                            callback.onNewMessage(BaseResponse.createSuccessBean(response.getData()));
                        }
                    }

                    @Override
                    public void onFail(String msg) {
                        if (callback != null) {
                            callback.onNewMessage(BaseResponse.createFailBean(msg));
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 上传当前的日志文件名和文件大小
     * @param fileList 文件集合
     * @param callback 回调
     */
    public static void uploadFileInfo(@NonNull final List<FileBean> fileList, final MessageListener<BaseResponse<Boolean>> callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://43.139.71.125:8879/custom/log/addLogFileInfo";
                AddLogFileInfoReq req = new AddLogFileInfoReq();
                req.setDeviceId(DeviceUtils.getUniqueDeviceId());
                req.setLogFileInfo(new Gson().toJson(fileList));
                String json = new Gson().toJson(req);
                HttpURLConnectionUtil.sendPost(HttpURLConnectionUtil.SendType.JSON, url, json, null, new HttpURLConnectionUtil.Callback() {
                    @Override
                    public void onSuccess(String result) {
                        if (result == null) {
                            if (callback != null) {
                                callback.onNewMessage(BaseResponse.createFailBean("result is null"));
                            }
                            return;
                        }
                        BaseResponse<Boolean> response = new Gson().fromJson(result, new TypeToken<BaseResponse<Boolean>>() {
                        }.getType());
                        if (response == null) {
                            if (callback != null) {
                                callback.onNewMessage(BaseResponse.createFailBean("transfer gson fail"));
                            }
                            return;
                        }
                        if (response.getCode() != 200) {
                            if (callback != null) {
                                callback.onNewMessage(BaseResponse.createFailBean("not success,code=" + response.getCode() + ",msg=" + response.getMsg()));
                            }
                            return;
                        }
                        if (callback != null) {
                            callback.onNewMessage(BaseResponse.createSuccessBean(response.getData()));
                        }
                    }

                    @Override
                    public void onFail(String msg) {
                        if (callback != null) {
                            callback.onNewMessage(BaseResponse.createFailBean(msg));
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 是否需要立即上传日志
     */
    public static void isNeedUploadLogImmediately(final MessageListener<BaseResponse<Boolean>> callback) {
        new Thread(() -> {
            String url = "http://43.139.71.125:8879/custom/log/isUploadNow";
            Map<String, String> params = new HashMap<>();
            params.put("deviceId", DeviceUtils.getUniqueDeviceId());
            HttpURLConnectionUtil.sendGet(url, params, new HttpURLConnectionUtil.Callback() {
                @Override
                public void onSuccess(String result) {
                    Log.i("zzzzzzzzzz", "result=" + result);
                    if (result == null) {
                        if (callback != null) {
                            callback.onNewMessage(BaseResponse.createFailBean("result is null"));
                        }
                        return;
                    }
                    BaseResponse<Boolean> response = new Gson().fromJson(result, new TypeToken<BaseResponse<Boolean>>() {
                    }.getType());
                    if (response == null) {
                        if (callback != null) {
                            callback.onNewMessage(BaseResponse.createFailBean("transfer gson fail"));
                        }
                        return;
                    }
                    if (response.getCode() != 200) {
                        if (callback != null) {
                            callback.onNewMessage(BaseResponse.createFailBean("not success,code=" + response.getCode() + ",msg=" + response.getMsg()));
                        }
                        return;
                    }
                    if (callback != null) {
                        callback.onNewMessage(BaseResponse.createSuccessBean(response.getData()));
                    }
                }

                @Override
                public void onFail(String msg) {
                    Log.i("zzzzzzzzzz", "onFail,msg=" + msg);
                    if (callback != null) {
                        callback.onNewMessage(BaseResponse.createFailBean(msg));
                    }
                }
            });
        }).start();
    }


}
