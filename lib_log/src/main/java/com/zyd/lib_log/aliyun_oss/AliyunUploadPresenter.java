package com.zyd.lib_log.aliyun_oss;

import android.content.Context;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.OSSRequest;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.io.File;

import utils.TagUtil;

/**
 * 阿里云上传
 */
public class AliyunUploadPresenter {

    private static final String TAG = TagUtil.obtainTag(AliyunUploadPresenter.class);

    public static AliyunUploadPresenter getInstance() {
        return CH.INST;
    }

    private boolean isUploading;
    private OSS mOss;

    private AliyunUploadPresenter() {
    }

    private static final class CH {
        private static final AliyunUploadPresenter INST = new AliyunUploadPresenter();
    }

    public void init(Context context) {
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        mOss = new OSSClient(context,
                Constants.OSS_ENDPOINT,
                new OSSPlainTextAKSKCredentialProvider(Constants.AK, Constants.SK), conf);
    }

    public boolean isUploading() {
        return isUploading;
    }

    /**
     * 上传文件
     */
    public void uploadFile(String object, File file, Callback callback) {
        isUploading = true;
        if (file == null) {
            isUploading = false;
            if (callback != null) {
                callback.onFailed("无效的文件");
            }
            return;
        }
        if (!file.exists()) {
            isUploading = false;
            if (callback != null) {
                callback.onFailed("文件不存在");
            }
            return;
        }
        if (!file.isFile()) {
            isUploading = false;
            if (callback != null) {
                callback.onFailed("不是一个文件");
            }
            return;
        }
        uploadAFile(object, file.getAbsolutePath(), callback);
    }

    /**
     * 上传一个文件
     */
    private void uploadAFile(
            final String object,
            final String localFile,
            final Callback callback) {
        PutObjectRequest put = new PutObjectRequest(Constants.BUCKET, object, localFile);
        put.setCRC64(OSSRequest.CRC64Config.YES);
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                int progress = (int) (100 * currentSize / totalSize);
                Log.d(TAG, "onProgress,progress=" + progress);
                if (callback != null) {
                    callback.onLog("onProgress,progress=" + progress);
                }
            }
        });
        OSSAsyncTask task = mOss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
            @Override
            public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                Log.d(TAG, "UploadSuccess");
                String url = mOss.presignPublicObjectURL(Constants.BUCKET, object);
                if (callback != null) {
                    callback.onSucceed(url);
                }
            }

            @Override
            public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
                String info = "";
                // 请求异常
                if (clientExcepion != null) {
                    // 本地异常如网络异常等
                    clientExcepion.printStackTrace();
                    info = clientExcepion.toString();
                }
                if (serviceException != null) {
                    // 服务异常
                    info = serviceException.toString();
                }
                Log.i(TAG, "onFailure,info=" + info);
                if (callback != null) {
                    callback.onFailed(info);
                }
            }
        });
    }

    public interface Callback {
        /**
         * 上传文件成功
         */
        void onSucceed(String url);

        /**
         * 上传进度
         */
        void onLog(String msg);

        /**
         * 上传文件失败
         *
         * @param msg 错误信息
         */
        void onFailed(String msg);
    }

}
