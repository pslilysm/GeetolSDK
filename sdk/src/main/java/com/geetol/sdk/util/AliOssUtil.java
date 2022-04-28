package com.geetol.sdk.util;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.geetol.sdk.GeetolSDKConfig;
import com.geetol.sdk.proguard_data.AliOssConfig;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import pers.cxd.corelibrary.AppHolder;
import pers.cxd.corelibrary.util.DataStructureUtil;

/**
 * 阿里云工具类
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class AliOssUtil {

    private static volatile AliOssConfig sConfig;
    private static volatile OSS sOSSClient;

    public static void initOSSClient(AliOssConfig config) {
        if (config == null) {
            return;
        }
        sConfig = config;
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // connction time out default 15s
        conf.setSocketTimeout(15 * 1000); // socket timeout，default 15s
        conf.setMaxConcurrentRequest(5); // synchronous request number，default 5
        conf.setMaxErrorRetry(2); // retry，default 2
//        OSSLog.enableLog(); //write local log file ,path is SDCard_path\OSSLog\logs.csv
        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(
                config.getAccessKeyId(),
                config.getAccessKeySecret());
        sOSSClient = new OSSClient(AppHolder.get(), config.getEndpoint(), credentialProvider, conf);
    }

    /**
     * 异步上传文件至阿里云
     *
     * @param uploadFile 需要上传的文件
     * @param callback   回调
     * @param ioExecutor 异步操作的线程池，null则使用SDK的IO线程池
     */
    public static void uploadFileAsync(File uploadFile, Callback callback, @Nullable ExecutorService ioExecutor) {
        if (sOSSClient != null) {
            if (ioExecutor == null) {
                ioExecutor = GeetolSDKConfig.SDK_GLOBAL_IO_EXECUTOR;
            }
            ioExecutor.execute(() -> {
                byte[] data;
                try {
                    data = FileUtils.readFileToByteArray(uploadFile);
                } catch (IOException e) {
                    callback.onFailure();
                    return;
                }
                // 文件名为文件内容的MD5
                String aliOssName = new String(Hex.encodeHex(DigestUtils.md5(data)));
                final WeakReference<Callback> wrCallback = new WeakReference<>(callback);
                sOSSClient.asyncPutObject(new PutObjectRequest(sConfig.getBucketName(), aliOssName, data), new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                    @Override
                    public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                        Callback callback1 = wrCallback.get();
                        if (callback1 != null) {
                            callback1.onSuccess("http://" + sConfig.getBucketName() + "." + sConfig.getEndpoint() + "/" + aliOssName);
                        }
                    }

                    @Override
                    public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                        Callback callback1 = wrCallback.get();
                        if (callback1 != null) {
                            callback1.onFailure();
                        }
                    }
                });
            });
        } else {
            callback.onFailure();
        }
    }

    /**
     * 多线程同时异步上传多个文件至阿里云
     *
     * @param uploadFiles   要上传的文件
     * @param batchCallback 回调
     * @param ioExecutor    异步操作的线程池，null则使用SDK的IO线程池
     */
    public static void uploadBatchFileAsync(List<File> uploadFiles, BatchCallback batchCallback, @Nullable ExecutorService ioExecutor) {
        if (DataStructureUtil.isEmpty(uploadFiles)) {
            throw new IllegalArgumentException("uploadFiles not have any elements to upload");
        }
        if (sOSSClient != null) {
            if (ioExecutor == null) {
                ioExecutor = GeetolSDKConfig.SDK_GLOBAL_IO_EXECUTOR;
            }
            final WeakReference<BatchCallback> wrBatchCallback = new WeakReference<>(batchCallback);
            ioExecutor.execute(() -> {
                final CopyOnWriteArrayList<String> urls = new CopyOnWriteArrayList<>();
                final CountDownLatch countDownLatch = new CountDownLatch(uploadFiles.size());
                final Thread curThread = Thread.currentThread();
                uploadFiles.forEach(uploadFile -> {
                    byte[] data;
                    try {
                        data = FileUtils.readFileToByteArray(uploadFile);
                    } catch (IOException e) {
                        curThread.interrupt();
                        return;
                    }
                    // 文件名为文件内容的MD5
                    String aliOssName = new String(Hex.encodeHex(DigestUtils.md5(data)));
                    sOSSClient.asyncPutObject(new PutObjectRequest(sConfig.getBucketName(), aliOssName, data), new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                        @Override
                        public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                            urls.add("http://" + sConfig.getBucketName() + "." + sConfig.getEndpoint() + "/" + aliOssName);
                            countDownLatch.countDown();
                        }

                        @Override
                        public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                            curThread.interrupt();
                        }
                    });
                });
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    BatchCallback batchCallback1 = wrBatchCallback.get();
                    if (batchCallback1 != null) {
                        batchCallback1.onFailure();
                    }
                    return;
                }
                BatchCallback batchCallback1 = wrBatchCallback.get();
                if (batchCallback1 != null) {
                    batchCallback1.onSuccess(urls);
                }
            });
        } else {
            batchCallback.onFailure();
        }
    }

    /**
     * 异步上传Uri至阿里云
     *
     * @param ctx        用来获取Uri的内容
     * @param uploadUri  需要上传的Uri
     * @param callback   回调
     * @param ioExecutor 异步操作的线程池，null则使用SDK的IO线程池
     */
    public static void uploadUriAsync(Context ctx, Uri uploadUri, Callback callback, @Nullable ExecutorService ioExecutor) {
        if (sOSSClient != null) {
            if (ioExecutor == null) {
                ioExecutor = GeetolSDKConfig.SDK_GLOBAL_IO_EXECUTOR;
            }
            final WeakReference<Callback> wrCallback = new WeakReference<>(callback);
            ioExecutor.execute(() -> {
                byte[] data;
                try (InputStream is = ctx.getContentResolver().openInputStream(uploadUri)) {
                    data = new byte[is.available()];
                    IOUtils.read(is, data);
                } catch (IOException ex) {
                    callback.onFailure();
                    return;
                }
                String aliOssName = new String(Hex.encodeHex(DigestUtils.md5(data)));
                sOSSClient.asyncPutObject(new PutObjectRequest(sConfig.getBucketName(), aliOssName, data), new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                    @Override
                    public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                        Callback callback1 = wrCallback.get();
                        if (callback1 != null) {
                            callback1.onSuccess("http://" + sConfig.getBucketName() + "." + sConfig.getEndpoint() + "/" + aliOssName);
                        }
                    }

                    @Override
                    public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                        Callback callback1 = wrCallback.get();
                        if (callback1 != null) {
                            callback1.onFailure();
                        }
                    }
                });
            });
        } else {
            callback.onFailure();
        }
    }

    /**
     * 多线程同时异步上传多个Uri至阿里云
     *
     * @param ctx           用来获取Uri的内容
     * @param uploadUris    需要上传的Uris
     * @param batchCallback 回调
     * @param ioExecutor    异步操作的线程池，null则使用SDK的IO线程池
     */
    public static void uploadBatchUriAsync(Context ctx, List<Uri> uploadUris, BatchCallback batchCallback, @Nullable ExecutorService ioExecutor) {
        if (DataStructureUtil.isEmpty(uploadUris)) {
            throw new IllegalArgumentException("uploadUris not have any elements to upload");
        }
        if (sOSSClient != null) {
            if (ioExecutor == null) {
                ioExecutor = GeetolSDKConfig.SDK_GLOBAL_IO_EXECUTOR;
            }
            final WeakReference<BatchCallback> wrBatchCallback = new WeakReference<>(batchCallback);
            ioExecutor.execute(() -> {
                final CopyOnWriteArrayList<String> urls = new CopyOnWriteArrayList<>();
                final Thread curThread = Thread.currentThread();
                final CountDownLatch countDownLatch = new CountDownLatch(uploadUris.size());
                uploadUris.forEach(uploadUri -> {
                    byte[] data;
                    try (InputStream is = ctx.getContentResolver().openInputStream(uploadUri)) {
                        data = new byte[is.available()];
                        IOUtils.read(is, data);
                    } catch (IOException ex) {
                        curThread.interrupt();
                        return;
                    }
                    String aliOssName = new String(Hex.encodeHex(DigestUtils.md5(data)));
                    sOSSClient.asyncPutObject(new PutObjectRequest(sConfig.getBucketName(), aliOssName, data), new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                        @Override
                        public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                            urls.add("http://" + sConfig.getBucketName() + "." + sConfig.getEndpoint() + "/" + aliOssName);
                            countDownLatch.countDown();
                        }

                        @Override
                        public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                            curThread.interrupt();
                        }
                    });
                });
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    BatchCallback batchCallback1 = wrBatchCallback.get();
                    if (batchCallback1 != null) {
                        batchCallback1.onFailure();
                    }
                    return;
                }
                BatchCallback batchCallback1 = wrBatchCallback.get();
                if (batchCallback1 != null) {
                    batchCallback1.onSuccess(urls);
                }
            });
        } else {
            batchCallback.onFailure();
        }
    }

    public interface Callback {

        /**
         * 上传成功的回调
         *
         * @param aliOssUrl 上传后的链接
         */
        default void onSuccess(String aliOssUrl) {
        }

        /**
         * 上传失败后的回调
         */
        default void onFailure() {
        }
    }

    public interface BatchCallback {

        /**
         * 上传成功的回调
         *
         * @param aliOssUrls 上传完成后的链接集合
         */
        default void onSuccess(CopyOnWriteArrayList<String> aliOssUrls) {
        }

        /**
         * 上传失败后的回调
         */
        default void onFailure() {
        }
    }

}
