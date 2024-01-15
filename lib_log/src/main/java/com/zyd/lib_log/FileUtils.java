package com.zyd.lib_log;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

class FileUtils {

    public static int getFileAvailableByteCounts(String fileName) {
        File file = new File(fileName);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            return fileInputStream.available();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    public static boolean copyFileFromAssets(Context context, final String assetsFilePath, final String destFilePath) {
        boolean res = true;
        try {
            res = writeFileFromInputStream(
                    new File(destFilePath),
                    context.getAssets().open(assetsFilePath), false, null);
        } catch (IOException e) {
            e.printStackTrace();
            res = false;
        }
        return res;
    }

    public static String readFromAssets(Context context, String assetsFileName) {
        AssetManager assets = context.getAssets();
        try {
            InputStream inputStream = assets.open(assetsFileName);
            return readString(inputStream, 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFromAssets2(Context context, String assetsFileName) {
        AssetManager assets = context.getAssets();
        try {
            InputStream inputStream = assets.open(assetsFileName);
            return readString(inputStream, inputStream.available());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean writeString(String text, String fileName, boolean isAppend) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, isAppend)));
            writer.write(text);
            writer.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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

    public static String readString(String fileName) {
        FileInputStream fileInputStream = newFileInputStream(fileName);
        return fileInputStream == null ? null : readString(fileInputStream, 1024);
    }

    public static String readString(String fileName, int eachCount) {
        FileInputStream fileInputStream = newFileInputStream(fileName);
        return fileInputStream == null ? null : readString(fileInputStream, eachCount);
    }

    public static String readString(InputStream inputStream, int eachCount) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            char[] read = new char[eachCount];
            String result = "";
            int tmp;
            while ((tmp = reader.read(read)) != -1) {
                result += new String(read, 0, tmp);
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static FileInputStream newFileInputStream(String fileName) {
        try {
            return new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean writeFileFromInputStream(final File file,
                                                   final InputStream is,
                                                   final boolean append,
                                                   final OnProgressUpdateListener listener) {
        if (is == null || file == null || !file.exists()) {
            return false;
        }
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file, append), 524288);
            if (listener == null) {
                byte[] data = new byte[524288];
                for (int len; (len = is.read(data)) != -1; ) {
                    os.write(data, 0, len);
                }
            } else {
                double totalSize = is.available();
                int curSize = 0;
                listener.onProgressUpdate(0);
                byte[] data = new byte[524288];
                for (int len; (len = is.read(data)) != -1; ) {
                    os.write(data, 0, len);
                    curSize += len;
                    listener.onProgressUpdate(curSize / totalSize);
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param callback                      回调函数
     * @param filePathWithSuffix            文件路径包含文件名后缀
     * @param createNewFileWhenOldFileExist 当旧文件存在的时候是否删除旧文件
     */
    public static void createFile(Callback callback,
                                  String filePathWithSuffix,
                                  boolean createNewFileWhenOldFileExist) {
        if (callback != null) {
            callback.onProgress("开始校验目标文件路径是否合法。。。");
        }
        if (filePathWithSuffix == null || filePathWithSuffix.trim().length() == 0) {
            if (callback != null) {
                callback.onProgress("不合法，路径为空");
            }
            if (callback != null) {
                callback.onFailed("路径为空");
            }
            return;
        }
        File file = new File(filePathWithSuffix);
        if (callback != null) {
            callback.onProgress("合法，开始校验目标文件是否存在。。。");
        }
        //文件存在
        if (file.exists()) {
            if (callback != null) {
                callback.onProgress("目标文件已存在");
            }
            if (callback != null) {
                callback.onProgress("开始校验目标文件是否是一个文件。。。");
            }
            //目标文件是一个文件
            if (file.isFile()) {
                if (callback != null) {
                    callback.onProgress("目标文件是一个文件");
                }
                if (callback != null) {
                    callback.onProgress("开始检查是否需要删除旧文件。。。");
                }
                if (createNewFileWhenOldFileExist) {
                    if (callback != null) {
                        callback.onProgress("需要删除旧文件，开始删除。。。");
                    }
                    boolean delete = file.delete();
                    if (delete) {
                        if (callback != null) {
                            callback.onProgress("删除旧文件成功，开始创建文件。。。");
                        }
                        boolean createNewFileResult = false;
                        try {
                            createNewFileResult = file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (createNewFileResult) {
                            if (callback != null) {
                                callback.onProgress("创建文件成功");
                            }
                            if (callback != null) {
                                callback.onSucceed();
                            }
                        } else {
                            if (callback != null) {
                                callback.onProgress("创建文件失败");
                            }
                            if (callback != null) {
                                callback.onFailed("创建文件失败");
                            }
                        }
                    } else {
                        if (callback != null) {
                            callback.onProgress("删除旧文件失败");
                        }
                        if (callback != null) {
                            callback.onFailed("删除旧文件失败");
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.onProgress("不需要删除旧文件");
                    }
                    if (callback != null) {
                        callback.onSucceed();
                    }
                }
            }
            //目标文件不是一个文件
            else {
                if (callback != null) {
                    callback.onProgress("目标文件是一个文件夹，开始删除。。。");
                }
                deleteFolder(filePathWithSuffix, true, new Callback() {
                    @Override
                    public void onSucceed() {
                        if (callback != null) {
                            callback.onProgress("删除旧文件成功，开始创建文件。。。");
                        }
                        boolean createNewFileResult = false;
                        try {
                            createNewFileResult = file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (createNewFileResult) {
                            if (callback != null) {
                                callback.onProgress("创建文件成功");
                            }
                            if (callback != null) {
                                callback.onSucceed();
                            }
                        } else {
                            if (callback != null) {
                                callback.onProgress("创建文件失败");
                            }
                            if (callback != null) {
                                callback.onFailed("创建文件失败");
                            }
                        }
                    }

                    @Override
                    public void onProgress(String msg) {
                        if (callback != null) {
                            callback.onProgress(msg);
                        }
                    }

                    @Override
                    public void onFailed(String msg) {
                        if (callback != null) {
                            callback.onProgress("删除旧文件夹失败");
                        }
                        if (callback != null) {
                            callback.onFailed(msg);
                        }
                    }
                });
            }
        }
        //文件不存在
        else {
            if (callback != null) {
                callback.onProgress("目标文件不存在，开始检测是否存在父文件夹。。。");
            }
            File parentFile = file.getParentFile();
            //无父文件夹
            if (parentFile == null) {
                if (callback != null) {
                    callback.onProgress("没有父文件夹，开始创建文件。。。");
                }
                boolean createNewFileResult = false;
                try {
                    createNewFileResult = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (createNewFileResult) {
                    if (callback != null) {
                        callback.onProgress("创建文件成功");
                    }
                    if (callback != null) {
                        callback.onSucceed();
                    }
                } else {
                    if (callback != null) {
                        callback.onProgress("创建文件失败");
                    }
                    if (callback != null) {
                        callback.onFailed("创建文件失败");
                    }
                }
            }
            //有父文件夹
            else {
                if (callback != null) {
                    callback.onProgress("有父文件夹，开始检测父文件夹是否存在");
                }
                //父文件夹存在
                if (parentFile.exists()) {
                    if (callback != null) {
                        callback.onProgress("父文件夹存在，开始检测父文件夹是否是一个文件夹。。。");
                    }
                    if (parentFile.isDirectory()) {
                        if (callback != null) {
                            callback.onProgress("父文件夹是一个文件夹，开始创建文件。。。");
                        }
                        boolean createNewFileResult = false;
                        try {
                            createNewFileResult = file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (createNewFileResult) {
                            if (callback != null) {
                                callback.onProgress("创建文件成功");
                            }
                            if (callback != null) {
                                callback.onSucceed();
                            }
                        } else {
                            if (callback != null) {
                                callback.onProgress("创建文件失败");
                            }
                            if (callback != null) {
                                callback.onFailed("创建文件失败");
                            }
                        }
                    } else {
                        if (callback != null) {
                            callback.onProgress("父文件夹是不一个文件夹，开始删除父文件");
                        }
                        boolean delete = parentFile.delete();
                        if (delete) {
                            if (callback != null) {
                                callback.onProgress("删除父文件成功，开始创建文件。。。");
                            }
                            boolean createNewFileResult = false;
                            try {
                                createNewFileResult = file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (createNewFileResult) {
                                if (callback != null) {
                                    callback.onProgress("创建文件成功");
                                }
                                if (callback != null) {
                                    callback.onSucceed();
                                }
                            } else {
                                if (callback != null) {
                                    callback.onProgress("创建文件失败");
                                }
                                if (callback != null) {
                                    callback.onFailed("创建文件失败");
                                }
                            }
                        } else {
                            if (callback != null) {
                                callback.onProgress("删除父文件失败");
                            }
                            if (callback != null) {
                                callback.onFailed("删除父文件失败");
                            }
                        }
                    }
                }
                //父文件夹不存在
                else {
                    if (callback != null) {
                        callback.onProgress("父文件夹不存在，开始创建父文件夹。。。");
                    }
                    boolean mkdirs = parentFile.mkdirs();
                    //创建父文件夹成功
                    if (mkdirs) {
                        if (callback != null) {
                            callback.onProgress("创建父文件夹成功，开始创建文件。。。");
                        }
                        boolean createNewFileResult = false;
                        try {
                            createNewFileResult = file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (createNewFileResult) {
                            if (callback != null) {
                                callback.onProgress("创建文件成功");
                            }
                            if (callback != null) {
                                callback.onSucceed();
                            }
                        } else {
                            if (callback != null) {
                                callback.onProgress("创建文件失败");
                            }
                            if (callback != null) {
                                callback.onFailed("创建文件失败");
                            }
                        }
                    }
                    //创建父文件夹失败
                    else {
                        if (callback != null) {
                            callback.onProgress("创建父文件夹失败");
                        }
                        if (callback != null) {
                            callback.onFailed("创建父文件夹失败");
                        }
                    }
                }
            }
        }
    }

    /**
     * 删除文件夹
     *
     * @param folderPath         文件夹路径
     * @param isDeleteWhenIsFile 是否删除folderPath，当folderPath是一个文件的时候
     * @param callback           回调函数
     */
    public static void deleteFolder(String folderPath, boolean isDeleteWhenIsFile, Callback callback) {
        if (callback != null) {
            callback.onProgress("开始校验文件路径：" + folderPath);
        }
        if (folderPath == null || folderPath.trim().length() == 0) {
            if (callback != null) {
                callback.onProgress("文件路径为空，删除成功");
            }
            if (callback != null) {
                callback.onSucceed();
            }
            return;
        }
        if (callback != null) {
            callback.onProgress("文件路径不为空，开始校验文件是否存在。。。");
        }
        File file = new File(folderPath);
        if (!file.exists()) {
            if (callback != null) {
                callback.onProgress("文件不存在，删除成功");
            }
            if (callback != null) {
                callback.onSucceed();
            }
            return;
        }
        if (callback != null) {
            callback.onProgress("文件存在，开始校验文件夹是否是一个文件夹。。。");
        }
        if (!file.isDirectory()) {
            if (callback != null) {
                callback.onProgress("文件不是一个文件夹，开始校验是否删除文件");
            }
            if (isDeleteWhenIsFile) {
                if (callback != null) {
                    callback.onProgress("需要删除文件，开始删除文件。。。");
                }
                boolean delete = file.delete();
                if (delete) {
                    if (callback != null) {
                        callback.onProgress("删除文件成功");
                    }
                    if (callback != null) {
                        callback.onSucceed();
                    }
                } else {
                    if (callback != null) {
                        callback.onProgress("删除文件失败");
                    }
                    if (callback != null) {
                        callback.onFailed("删除文件失败");
                    }
                }
            } else {
                if (callback != null) {
                    callback.onProgress("不需要删除文件");
                }
                if (callback != null) {
                    callback.onSucceed();
                }
            }
            return;
        }
        if (callback != null) {
            callback.onProgress("文件是一个文件夹，开始检测里面是否有子文件。。。");
        }
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            if (callback != null) {
                callback.onProgress("没有子文件，开始删除本文件夹。。。");
            }
            boolean delete = file.delete();
            if (delete) {
                if (callback != null) {
                    callback.onProgress("删除文件夹成功");
                }
                if (callback != null) {
                    callback.onSucceed();
                }
            } else {
                if (callback != null) {
                    callback.onProgress("删除文件夹失败");
                }
                if (callback != null) {
                    callback.onFailed("删除文件夹失败");
                }
            }
        } else {
            if (callback != null) {
                callback.onProgress("有子文件，开始删除子文件。。。");
            }
            final int[] needDeleteCount = {files.length};
            for (File childFile : files) {
                if (childFile.isFile()) {
                    boolean delete = childFile.delete();
                    if (delete) {
                        needDeleteCount[0]--;
                    }
                } else {
                    deleteFolder(childFile.getPath(), true, new Callback() {
                        @Override
                        public void onSucceed() {
                            needDeleteCount[0]--;
                        }

                        @Override
                        public void onProgress(String msg) {

                        }

                        @Override
                        public void onFailed(String msg) {

                        }
                    });
                }
            }
            if (needDeleteCount[0] == 0) {
                if (callback != null) {
                    callback.onProgress("全部子文件删除成功，开始删除最外层文件夹。。。");
                }
                boolean delete = file.delete();
                if (delete) {
                    if (callback != null) {
                        callback.onProgress("删除成功");
                    }
                    if (callback != null) {
                        callback.onSucceed();
                    }
                } else {
                    if (callback != null) {
                        callback.onProgress("删除失败");
                    }
                    if (callback != null) {
                        callback.onFailed("删除失败");
                    }
                }
            } else {
                if (callback != null) {
                    callback.onProgress("部分子文件夹删除失败");
                }
                if (callback != null) {
                    callback.onFailed("部分子文件夹删除失败");
                }
            }
        }
    }

    public interface Callback {
        void onSucceed();

        void onProgress(String msg);

        void onFailed(String msg);
    }

    public interface OnProgressUpdateListener {
        void onProgressUpdate(double progress);
    }

    public static long getFileSystemAvailableSize(final String anyPathInFs) {
        if (TextUtils.isEmpty(anyPathInFs)) return 0;
        StatFs statFs;
        try {
            statFs = new StatFs(anyPathInFs);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return 0;
        }
        long blockSize;
        long availableSize;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statFs.getBlockSizeLong();
            availableSize = statFs.getAvailableBlocksLong();
        } else {
            blockSize = statFs.getBlockSize();
            availableSize = statFs.getAvailableBlocks();
        }
        return blockSize * availableSize;
    }
}
