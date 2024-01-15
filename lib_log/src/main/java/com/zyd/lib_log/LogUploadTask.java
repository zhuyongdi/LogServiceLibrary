package com.zyd.lib_log;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.FileUtils;
import com.google.gson.Gson;
import com.zyd.lib_log.aliyun_oss.AliyunUploadPresenter;
import com.zyd.lib_log.api.Apis;
import com.zyd.lib_log.api.BaseResponse;
import com.zyd.lib_log.api.FileBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import utils.RandomUtil;

public class LogUploadTask {

    /* 本类的Log的Tag */
    private static final String TAG = "LogUploadTask";
    /* 定时多久上传日志到服务器，10s */
    private static final long PERIOD_OF_UPLOAD_LOG_TO_SERVER = 10 * 1000;
    private final HandlerThread mHandlerThread;
    private final Handler mHandler;
    /* 定时多久上传日志文件信息到服务器，5s */
    private static final long PERIOD_OF_UPLOAD_LOG_FILE_INFO_TO_SERVER = 5 * 1000;

    private static final int FLAG_UPLOAD_LOG_FILE = 1;
    private static final int FLAG_UPLOAD_LOG_FILE_INFO = 2;

    public LogUploadTask() {
        mHandlerThread = new HandlerThread("LogUploadTask");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message originalMsg) {
                Message msg = Message.obtain(originalMsg);
                /* 接收到[立马上传日志文件]的消息 */
                if (msg.what == FLAG_UPLOAD_LOG_FILE) {
                    uploadLogFileToServer();
                }
                /* 接收到[立马上传日志文件信息]的消息 */
                else if (msg.what == FLAG_UPLOAD_LOG_FILE_INFO) {
                    uploadLogFileInfoToServer();
                }
            }
        };
    }

    /**
     * 不需要立即上传日志文件到服务器
     * 当日志文件数量>=2个的时候才会上传最旧的那个
     */
    private void onNoNeedToUploadLogImmediately() {
        uploadFile(LogService.getOldestLogFileWhenFilesNumMoreThanTwo());
    }

    /**
     * 需要立即上传日志文件到服务器
     * 直接上传最旧的那个日志文件
     */
    private void onNeedToUploadLogImmediately() {
        uploadFile(LogService.getOldestLogFileAlways());
    }

    /**
     * 上传日志文件到服务器
     */
    private void uploadLogFileToServer() {
        Log.e(TAG, "aaaaaaaa------------------上传日志文件到服务器开始------------------");
        if (LogService.isWritingLog()) {
            Log.e(TAG, "aaaaaaaa-正在写入中,暂不上传");
            mHandler.sendEmptyMessageDelayed(FLAG_UPLOAD_LOG_FILE, PERIOD_OF_UPLOAD_LOG_TO_SERVER);
            onUploadLogFileToServerFinish();
            return;
        }
        Log.e(TAG, "aaaaaaaa-开始获取是否需要立即上传日志");
        Apis.isNeedUploadLogImmediately(new MessageListener<BaseResponse<Boolean>>() {
            @Override
            public void onNewMessage(BaseResponse<Boolean> data) {
                if (!data.isSuccess()) {
                    onNoNeedToUploadLogImmediately();
                    Log.e(TAG, "aaaaaaaa-获取是否需要立即上传日志失败,msg=" + data.getMsg());
                    return;
                }
                Boolean is = data.getData();
                if (is == null || !is) {
                    Log.e(TAG, "aaaaaaaa-获取是否需要立即上传日志成功,不需要");
                    onNoNeedToUploadLogImmediately();
                } else {
                    Log.e(TAG, "aaaaaaaa-获取是否需要立即上传日志成功,需要");
                    onNeedToUploadLogImmediately();
                }
            }
        });
    }

    private void onUploadLogFileToServerFinish(){
        Log.e(TAG, "aaaaaaaa------------------上传日志文件到服务器结束------------------");
    }

    private void onUploadLogFileInfoToServerFinish(){
        Log.e(TAG, "bbbbbbbb------------------上传日志信息到服务器结束------------------");
    }

    /**
     * 上传日志文件到服务器
     */
    private void uploadFile(@Nullable File file) {
        Log.e(TAG, "aaaaaaaa-要上传的文件,file=" + file);
        if (file == null) {
            mHandler.sendEmptyMessageDelayed(FLAG_UPLOAD_LOG_FILE, PERIOD_OF_UPLOAD_LOG_TO_SERVER);
            onUploadLogFileToServerFinish();
            return;
        }
        Log.e(TAG, "aaaaaaaa-开始上传日志到阿里云服务器");
        AliyunUploadPresenter.getInstance().uploadFile(obtainUrl(), file, new AliyunUploadPresenter.Callback() {
            @Override
            public void onSucceed(String url) {
                Log.e(TAG, "aaaaaaaa-上传日志到阿里云服务器成功,url=" + url + "，开始上传到自己服务器...");
                Apis.addLog(url, new MessageListener<BaseResponse<Boolean>>() {
                    @Override
                    public void onNewMessage(BaseResponse<Boolean> data) {
                        if(!data.isSuccess()){
                            Log.e(TAG, "aaaaaaaa-上传到自己的服务器失败,msg=" + data.getMsg());
                            mHandler.sendEmptyMessageDelayed(FLAG_UPLOAD_LOG_FILE, PERIOD_OF_UPLOAD_LOG_TO_SERVER);
                            onUploadLogFileToServerFinish();
                            return;
                        }
                        Log.e(TAG, "aaaaaaaa-上传到自己的服务器成功");
                        boolean delete = FileUtils.delete(file);
                        if (!delete) {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            FileUtils.delete(file);
                        }
                        mHandler.sendEmptyMessageDelayed(FLAG_UPLOAD_LOG_FILE, PERIOD_OF_UPLOAD_LOG_TO_SERVER);
                        onUploadLogFileToServerFinish();
                    }
                });
            }

            @Override
            public void onLog(String msg) {
//                Log.e(TAG, "上传日志到阿里云服务器--" + msg);
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "aaaaaaaa-上传日志到阿里云服务器--失败：" + msg);
                mHandler.sendEmptyMessageDelayed(FLAG_UPLOAD_LOG_FILE, PERIOD_OF_UPLOAD_LOG_TO_SERVER);
                onUploadLogFileToServerFinish();
            }
        });
    }

    private String obtainUrl() {
        return AppUtils.getAppName() + "/log/" + DeviceUtils.getUniqueDeviceId() + "_" + DateUtil.parseTimestampToTime(System.currentTimeMillis(), null).replaceAll(" ", "-") + "_" + RandomUtil.getRandomNotRepeatArray(100000, 999999, 1)[0] + ".txt";
    }

    /**
     * 上传日志信息到服务器
     */
    private void uploadLogFileInfoToServer() {
        Log.e(TAG, "bbbbbbbb------------------上传日志文件信息到服务器开始------------------");
        File[] allValidFileList = LogService.getAllValidFileList();
        List<FileBean> fileBeanList = new ArrayList<>();
        for (File file : allValidFileList) {
            FileBean fb = new FileBean();
            fb.setPath(file.getAbsolutePath());
            fb.setSize(file.length() + "");
            fileBeanList.add(fb);
        }
        Log.e(TAG, "bbbbbbbb-所有要上传的日志文件信息:" + new Gson().toJson(fileBeanList));
        Apis.uploadFileInfo(fileBeanList, new MessageListener<BaseResponse<Boolean>>() {
            @Override
            public void onNewMessage(BaseResponse<Boolean> data) {
                if (!data.isSuccess()) {
                    Log.e(TAG, "bbbbbbbb-上传日志信息失败,msg=" + data.getMsg());
                    mHandler.sendEmptyMessageDelayed(FLAG_UPLOAD_LOG_FILE_INFO, PERIOD_OF_UPLOAD_LOG_FILE_INFO_TO_SERVER);
                    onUploadLogFileInfoToServerFinish();
                    return;
                }
                Log.e(TAG, "bbbbbbbb-上传日志信息成功");
                mHandler.sendEmptyMessageDelayed(FLAG_UPLOAD_LOG_FILE_INFO, PERIOD_OF_UPLOAD_LOG_FILE_INFO_TO_SERVER);
                onUploadLogFileInfoToServerFinish();
            }
        });
    }

    public void start() {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessage(FLAG_UPLOAD_LOG_FILE);
        mHandler.sendEmptyMessage(FLAG_UPLOAD_LOG_FILE_INFO);
    }

}
