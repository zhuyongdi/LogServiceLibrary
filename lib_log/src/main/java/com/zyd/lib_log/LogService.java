package com.zyd.lib_log;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zyd.lib_log.aliyun_oss.AliyunUploadPresenter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 日志服务
 **/
public final class LogService extends Service {

    private final HandlerThread mHandlerThread;
    private final Handler mHandler;
    private final Messenger mMessenger;

    /* Handler的what，写入日志到本地 */
    public static final int HANDLER_OF_WRITE_LOG_TO_LOCAL = 1;
    /* Handler的what，通知刷新日志 */
    public static final int HANDLER_OF_WHAT_NOTIFY_LOG_UPDATE = 2;

    /* 定时多久写入日志到本地并且上传日志到服务器 */
    private static final long PERIOD_OF_WRITE_LOG_TO_LOCAL_AND_UPLOAD_TO_SERVER = 10 * 1000;
    /* 文件系统最小的可用空间 */
    private static final long MIN_FILE_SYSTEM_AVAILABLE_SIZE = 50 * 1024 * 1024;
    /* 单个日志文件大小，500KB*/
    private static final long SINGLE_FILE_SIZE = 500 * 1024;
    /* 日志目录最大大小，200M */
    private static final long MAX_FILE_LENGTH_OF_LOG_FILE = 200 * 1024 * 1024;

    /* 记录日志的ArrayList的最大长度，超过此长度不处理 */
    private static final int MAX_LENGTH_OF_STRING_BUILDER = 10 * 10000;
    /* 本类的Log的Tag */
    private static final String TAG = "LogService";
    /* Log目录 */
    public static String DIR = Configuration.DIR_LOG;
    /* Log文件前缀 */
    private static final String PREFIX = "log_";
    /* Log文件后缀 */
    private static final String SUFFIX = ".txt";
    /* Log文件名长度 */
    private static final int LOG_FILE_NAME_LENGTH = PREFIX.length() + SUFFIX.length() + 13;

    private static volatile boolean mWritingLog;

    /* 记录日志的ArrayList */
    private static final List<String> LOG_LIST = new CopyOnWriteArrayList<>();
    /* 写入日志到本地的回调 */
    private final WriteToLocalCallbackImpl mWriteToLocalCallback = new WriteToLocalCallbackImpl();
    /* 用来给LOG_LIST排序的Comparator */
    private static final Comparator<String> COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return Long.compare(StringUtil.toLong(o1.substring(20, 33)), StringUtil.toLong(o2.substring(20, 33)));
        }
    };

    public static void start(Context context) {
        Intent intent = new Intent(context, LogService.class);
        context.startService(intent);
        context.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e(TAG, "onServiceConnected：LogService服务已建立连接");
                Configuration.LOG_SERVICE_MESSENGER = new Messenger(service);
            }

            /**
             * 内存不足的时候可能会调用这个
             */
            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(TAG, "onServiceDisconnected：LogService服务已失去连接");
                //停止服务
                context.stopService(intent);
                //解绑服务
                context.unbindService(this);
                //重新开启服务
                context.startService(intent);
                //重新绑定服务
                context.bindService(intent, this, 0);
            }
        }, 0);
    }

    public LogService() {
        mHandlerThread = new HandlerThread("LogServiceThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message originalMsg) {
                Message msg = Message.obtain(originalMsg);
                /* 接收到[立马写入日志]的消息 */
                if (msg.what == HANDLER_OF_WRITE_LOG_TO_LOCAL) {
                    writeLogToLocal(mWriteToLocalCallback);
                }
                /* 接收到[通知Log集合更新]的消息 */
                else if (msg.what == HANDLER_OF_WHAT_NOTIFY_LOG_UPDATE) {
                    // 防止ArrayList过长
                    if (LOG_LIST.size() > MAX_LENGTH_OF_STRING_BUILDER) {
                        LOG_LIST.clear();
                    }
                    ArrayList<String> logList = originalMsg.getData().getStringArrayList("log");
                    if (logList != null && !logList.isEmpty()) {
                        LOG_LIST.addAll(logList);
                    }
                }
            }
        };
        mMessenger = new Messenger(mHandler);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        return mMessenger.getBinder();
    }

    /**
     * 获取最新的日志文件
     */
    public static File getLatestLogFile() {
        File folder = new File(DIR);
        File[] files = folder.listFiles(file -> isFileValid(file) && isFileNameValid(file.getName()) && isFileLengthValid(file));
        File result = null;
        if (files != null && files.length > 0) {
            List<File> fileList = Arrays.asList(files);
            fileList.sort((o1, o2) -> -Long.compare(StringUtil.toLong(o1.getName().substring(4, 17)), StringUtil.toLong(o2.getName().substring(4, 17))));
            result = fileList.get(0);
        }
        return result;
    }

    /**
     * 获取最旧的文件（当文件数量超过2个时）
     */
    public static File getOldestLogFileWhenFilesNumMoreThanTwo() {
        File folder = new File(DIR);
        File[] files = folder.listFiles(file -> isFileValid(file) && isFileNameValid(file.getName()) && isFileLengthValid(file));
        File result = null;
        if (files != null && files.length > 1) {
            List<File> fileList = Arrays.asList(files);
            fileList.sort(Comparator.comparingLong(o -> StringUtil.toLong(o.getName().substring(4, 17))));
            result = fileList.get(0);
        }
        return result;
    }

    /**
     * 获取所有的日志文件
     */
    public static File[] getAllValidFileList() {
        File folder = new File(DIR);
        return folder.listFiles(file -> isFileValid(file) && isFileNameValid(file.getName()) && isFileLengthValid(file));
    }

    /**
     * 获取最旧的文件（任何情况）
     */
    public static File getOldestLogFileAlways() {
        File folder = new File(DIR);
        File[] files = folder.listFiles(file -> isFileValid(file) && isFileNameValid(file.getName()) && isFileLengthValid(file));
        File result = null;
        if (files != null && files.length > 1) {
            List<File> fileList = Arrays.asList(files);
            fileList.sort(Comparator.comparingLong(o -> StringUtil.toLong(o.getName().substring(4, 17))));
            result = fileList.get(0);
        }
        return result;
    }

    /**
     * 文件是否合法
     * @param file 文件
     */
    private static boolean isFileValid(File file) {
        return file != null && file.isFile();
    }

    /**
     * 文件名是否合法
     * @param fileName 文件名
     */
    private static boolean isFileNameValid(String fileName) {
        boolean valid1 = fileName.startsWith(PREFIX) && fileName.endsWith(SUFFIX) &&
                fileName.length() <= LOG_FILE_NAME_LENGTH;
        long timeInMillis = StringUtil.toLong(fileName.substring(4, 17));
        return valid1 && timeInMillis > 0 && timeInMillis <= System.currentTimeMillis();
    }

    /**
     * 文件大小是否合法
     * @param file 文件
     */
    private static boolean isFileLengthValid(File file) {
//        return file.length() <= SINGLE_FILE_SIZE + 1024 * 1024;
        return true;
    }

    /**
     * 获取日志文件
     * 如果该日志文件大小没有超过限制，就在这个日志文件追加写入
     * 如果该日志文件大小超过了限制，那就在新的日志文件写入
     */
    private File getLogFile() {
        File latestLogFile = getLatestLogFile();
        File ff = null;
        if (latestLogFile != null) {
            if (com.blankj.utilcode.util.FileUtils.getLength(latestLogFile) < SINGLE_FILE_SIZE) {
                ff = latestLogFile;
            }
        }
        if (ff == null) {
            ff = new File(DIR + PREFIX + System.currentTimeMillis() + SUFFIX);
        }
        return ff;
    }

    /**
     * 删除最旧的文件2个
     */
    private void deleteOldestFile() {
        List<File> fileList = com.blankj.utilcode.util.FileUtils.listFilesInDir(DIR);
        if (!fileList.isEmpty()) {
            // 删除全部的目录
            for (File file : fileList) {
                if (com.blankj.utilcode.util.FileUtils.isDir(file)) {
                    com.blankj.utilcode.util.FileUtils.deleteAllInDir(file);
                    com.blankj.utilcode.util.FileUtils.delete(file);
                    fileList.remove(file);
                }
            }
            if (fileList.isEmpty()) {
                return;
            }
            // 删除全部非法文件命名的文件
            for (File file : fileList) {
                if (com.blankj.utilcode.util.FileUtils.isFile(file)) {
                    if (!isFileNameValid(file.getName())) {
                        com.blankj.utilcode.util.FileUtils.delete(file);
                        fileList.remove(file);
                    }
                }
            }
            if (fileList.isEmpty()) {
                return;
            }
            // 删除全部非法大小的文件
            for (File file : fileList) {
                if (com.blankj.utilcode.util.FileUtils.isFile(file)) {
                    if (file.length() > SINGLE_FILE_SIZE + 5 * 1024 * 1024) {
                        com.blankj.utilcode.util.FileUtils.delete(file);
                        fileList.remove(file);
                    }
                }
            }
            if (fileList.isEmpty()) {
                return;
            }
            // 按照文件名中的时间排序（从旧到新）
            fileList.sort(Comparator.comparingLong(o -> StringUtil.toLong(o.getName().substring(4, 17))));
            // 删除前两个文件
            com.blankj.utilcode.util.FileUtils.delete(fileList.get(0));
            fileList.remove(0);
            if (fileList.isEmpty()) {
                return;
            }
            com.blankj.utilcode.util.FileUtils.delete(fileList.get(0));
            fileList.remove(0);
        }
    }

    /**
     * 写入日志到本地
     * 获取最近的日志文件
     * 如果该日志文件大小没有超过限制，就在这个日志文件追加写入
     * 如果该日志文件大小超过了限制，那就在新的日志文件写入
     */
    private void writeLogToLocal(final WriteToLocalCallback callback) {
        mWritingLog = true;
        Log.e(TAG, "写入日志到本地--开始判断存储空间大小是否够用");
        long fileSystemAvailableSize = FileUtils.getFileSystemAvailableSize(Environment.getExternalStorageDirectory().getAbsolutePath());
        if (fileSystemAvailableSize <= MIN_FILE_SYSTEM_AVAILABLE_SIZE) {
            Log.e(TAG, "写入日志到本地--存储空间不够" + MIN_FILE_SYSTEM_AVAILABLE_SIZE + "，返回");
            if (callback != null) {
                callback.onWriteFailed(null);
            }
            return;
        } else {
            Log.e(TAG, "写入日志到本地--存储空间够用");
        }

        Log.e(TAG, "写入日志到本地--日志目录=" + DIR);

        Log.e(TAG, "写入日志到本地--开始判断日志目录是否是否存在");
        boolean isDirExists = com.blankj.utilcode.util.FileUtils.isFileExists(DIR);
        if (!isDirExists) {
            Log.e(TAG, "写入日志到本地--日志目录不存在，开始创建目录");
            boolean createDirResult = com.blankj.utilcode.util.FileUtils.createOrExistsDir(DIR);
            if (!createDirResult) {
                Log.e(TAG, "写入日志到本地--日志目录创建失败，返回");
                if (callback != null) {
                    callback.onWriteFailed(null);
                }
                return;
            } else {
                Log.e(TAG, "写入日志到本地--日志目录创建成功");
            }
        } else {
            Log.e(TAG, "写入日志到本地--日志目录存在，开始校验目录是否是目录");
            boolean isInstanceOfDir = com.blankj.utilcode.util.FileUtils.isDir(DIR);
            if (!isInstanceOfDir) {
                Log.e(TAG, "写入日志到本地--日志目录存在，不是目录，返回");
                if (callback != null) {
                    callback.onWriteFailed(null);
                }
                return;
            } else {
                Log.e(TAG, "写入日志到本地--日志目录存在，是目录");
            }
        }

        Log.e(TAG, "写入日志到本地--开始校验是否有要写入的日志");

        /* 如果字符串没有东西了，返回失败，因为失败回调里还会继续写入 */
        if (LOG_LIST.isEmpty()) {
            Log.e(TAG, "写入日志到本地--没有日志要写入");
            if (callback != null) {
                callback.onWriteFailed(null);
            }
            return;
        } else {
            Log.e(TAG, "写入日志到本地--有日志要写入,集合大小=" + LOG_LIST.size());
        }

        long limitFileSize = MAX_FILE_LENGTH_OF_LOG_FILE;
        Log.e(TAG, "写入日志到本地--开始校验存储目录空间是否够用1,阈值=" + limitFileSize + "字节," + (limitFileSize / 1024 / 1024) + "MB");
        long length = com.blankj.utilcode.util.FileUtils.getLength(DIR);
        Log.e(TAG, "写入日志到本地--当前目录占用大小=" + length + "字节");
        /* 如果日志文件夹已经超过最大了，先删除部分文件，再操作 */
        if (length > limitFileSize) {
            Log.e(TAG, "写入日志到本地--已超过阈值，删除最旧的2个文件");
            deleteOldestFile();
        }

        Log.e(TAG, "写入日志到本地--开始校验存储目录空间是否够用2,阈值=" + limitFileSize + "字节," + (limitFileSize / 1024 / 1024) + "MB");
        length = com.blankj.utilcode.util.FileUtils.getLength(DIR);
        Log.e(TAG, "写入日志到本地--当前目录占用大小=" + length + "字节");
        /* 删除过后依旧超过最大，说明删除失败了一部分文件，返回失败，因为失败还会继续写入 */
        if (length > limitFileSize) {
            Log.e(TAG, "写入日志到本地--已超过阈值，返回");
            if (callback != null) {
                callback.onWriteFailed(null);
            }
            return;
        } else {
            Log.e(TAG, "写入日志到本地--未超过阈值");
        }

        File file = getLogFile();
        Log.e(TAG, "写入日志到本地--开始创建日志文件，文件路径=" + file.getAbsolutePath());

        FileUtils.createFile(new FileUtils.Callback() {
            @Override
            public void onSucceed() {
                Log.e(TAG, "写入日志到本地--创建日志文件成功，开始写入");
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    List<String> lb = new ArrayList<>(LOG_LIST);
                    //先按照时间排序
                    Collections.sort(lb, COMPARATOR);
                    for (int i = 0, l = lb.size(); i < l; i++) {
                        sb.append(lb.get(i));
                    }
                    writer.write(sb.toString());
                    Log.e(TAG, "写入日志到本地--写入完成");
                    //写入之后清空LogList
                    LOG_LIST.clear();
                    if (callback != null) {
                        callback.onWriteSucceed(file);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "写入日志到本地--写入发生异常：" + e.getMessage());
                    if (callback != null) {
                        callback.onWriteFailed(file);
                    }
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onProgress(String msg) {
                Log.e(TAG, "写入日志到本地--创建日志文件onProgress,msg=" + msg);
            }

            @Override
            public void onFailed(String msg) {
                Log.e(TAG, "写入日志到本地--创建日志文件失败,msg=" + msg);
                if (callback != null) {
                    callback.onWriteFailed(file);
                }
            }
        }, file.getAbsolutePath(), false);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate：服务已创建");

        AliyunUploadPresenter.getInstance().init(this);

        //延时x时间后开始写入
        mHandler.sendEmptyMessageDelayed(HANDLER_OF_WRITE_LOG_TO_LOCAL, PERIOD_OF_WRITE_LOG_TO_LOCAL_AND_UPLOAD_TO_SERVER);

        new LogUploadTask().start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        return Service.START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.e(TAG, "onStart");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "onConfigurationChanged");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(TAG, "onLowMemory");
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.e(TAG, "onRebind");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e(TAG, "onTaskRemoved：程序已被用户从任务列表中移除");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.e(TAG, "onTrimMemory：" + level);
    }

    private interface WriteToLocalCallback {
        /**
         * 写入成功
         * @param file 要写入的文件
         */
        void onWriteSucceed(@NonNull File file);

        /**
         * 写入失败
         * @param file 要写入的文件，可能为空
         */
        void onWriteFailed(@Nullable File file);
    }

    public static boolean isWritingLog() {
        return mWritingLog;
    }

    private final class WriteToLocalCallbackImpl implements WriteToLocalCallback {
        @Override
        public void onWriteSucceed(@NonNull File file) {
            //延时x时间后开始写入
            mHandler.sendEmptyMessageDelayed(HANDLER_OF_WRITE_LOG_TO_LOCAL, PERIOD_OF_WRITE_LOG_TO_LOCAL_AND_UPLOAD_TO_SERVER);
            mWritingLog = false;
        }

        @Override
        public void onWriteFailed(@Nullable File file) {
            if (file != null) {
                //删除文件
                com.blankj.utilcode.util.FileUtils.delete(file);
            }
            //延时x时间后开始写入
            mHandler.sendEmptyMessageDelayed(HANDLER_OF_WRITE_LOG_TO_LOCAL, PERIOD_OF_WRITE_LOG_TO_LOCAL_AND_UPLOAD_TO_SERVER);
            mWritingLog = false;
        }
    }

}
