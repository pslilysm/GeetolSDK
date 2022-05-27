package com.geetol.sdk.util;

import android.content.Context;
import android.net.Uri;

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
import com.geetol.sdk.proguard_data.AliOssConfig;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import io.reactivex.rxjava3.core.Observable;
import pers.cxd.corelibrary.AppHolder;
import pers.cxd.corelibrary.util.DataStructureUtil;
import pers.cxd.rxlibrary.BaseObserverImpl;
import pers.cxd.rxlibrary.RxUtil;

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
     * @param observer   回调
     */
    public static void uploadFileAsync(File uploadFile, BaseObserverImpl<String> observer) {
        RxUtil.execute(Observable.create(emitter -> {
            if (sOSSClient != null) {
                byte[] data;
                try {
                    data = FileUtils.readFileToByteArray(uploadFile);
                } catch (IOException ex) {
                    emitter.onError(ex);
                    emitter.onComplete();
                    return;
                }
                String aliOssName = new String(Hex.encodeHex(DigestUtils.md5(data)));
                sOSSClient.asyncPutObject(new PutObjectRequest(sConfig.getBucketName(), aliOssName, data), new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                    @Override
                    public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                        emitter.onNext("http://" + sConfig.getBucketName() + "." + sConfig.getEndpoint() + "/" + aliOssName);
                        emitter.onComplete();
                    }

                    @Override
                    public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                        emitter.onError(clientException);
                        emitter.onComplete();
                    }
                });
            } else {
                emitter.onError(new NullPointerException("oss client not initialized"));
                emitter.onComplete();
            }
        }), observer, RxUtil.Transformers.IOToMain());
    }

    /**
     * 多线程同时异步上传多个文件至阿里云
     *
     * @param uploadFiles 要上传的文件
     * @param observer    回调
     */
    public static void uploadBatchFileAsync(List<File> uploadFiles, BaseObserverImpl<CopyOnWriteArrayList<String>> observer) {
        if (DataStructureUtil.isEmpty(uploadFiles)) {
            throw new IllegalArgumentException("uploadFiles not have any elements to upload");
        }
        RxUtil.execute(Observable.create(emitter -> {
            if (sOSSClient != null) {
                final CopyOnWriteArrayList<String> urls = new CopyOnWriteArrayList<>();
                final CountDownLatch countDownLatch = new CountDownLatch(uploadFiles.size());
                final Thread curThread = Thread.currentThread();
                uploadFiles.forEach(uploadFile -> {
                    byte[] data;
                    try {
                        data = FileUtils.readFileToByteArray(uploadFile);
                    } catch (IOException e) {
                        emitter.onError(e);
                        emitter.onComplete();
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
                    emitter.onNext(urls);
                } catch (InterruptedException e) {
                    emitter.onError(e);
                }
            } else {
                emitter.onError(new NullPointerException("oss client not initialized"));
            }
            emitter.onComplete();
        }), observer, RxUtil.Transformers.IOToMain());
    }

    /**
     * 异步上传Uri至阿里云
     *
     * @param ctx       用来获取Uri的内容
     * @param uploadUri 需要上传的Uri
     * @param observer  回调
     */
    public static void uploadUriAsync(Context ctx, Uri uploadUri, BaseObserverImpl<String> observer) {
        RxUtil.execute(Observable.create(emitter -> {
            if (sOSSClient != null) {
                byte[] data;
                try (InputStream is = ctx.getContentResolver().openInputStream(uploadUri)) {
                    data = new byte[is.available()];
                    IOUtils.read(is, data);
                } catch (IOException ex) {
                    emitter.onError(ex);
                    emitter.onComplete();
                    return;
                }
                String aliOssName = new String(Hex.encodeHex(DigestUtils.md5(data)));
                sOSSClient.asyncPutObject(new PutObjectRequest(sConfig.getBucketName(), aliOssName, data), new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                    @Override
                    public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                        emitter.onNext("http://" + sConfig.getBucketName() + "." + sConfig.getEndpoint() + "/" + aliOssName);
                        emitter.onComplete();
                    }

                    @Override
                    public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                        emitter.onError(clientException);
                        emitter.onComplete();
                    }
                });
            } else {
                emitter.onError(new NullPointerException("oss client not initialized"));
                emitter.onComplete();
            }
        }), observer, RxUtil.Transformers.IOToMain());
    }

    /**
     * 多线程同时异步上传多个Uri至阿里云
     *
     * @param ctx        用来获取Uri的内容
     * @param uploadUris 需要上传的Uri集合
     * @param observer   回调
     */
    public static void uploadBatchUriAsync(Context ctx, List<Uri> uploadUris, BaseObserverImpl<CopyOnWriteArrayList<String>> observer) {
        if (DataStructureUtil.isEmpty(uploadUris)) {
            throw new IllegalArgumentException("uploadUris not have any elements to upload");
        }
        RxUtil.execute(Observable.create(emitter -> {
            if (sOSSClient != null) {
                final CopyOnWriteArrayList<String> urls = new CopyOnWriteArrayList<>();
                final Thread curThread = Thread.currentThread();
                final CountDownLatch countDownLatch = new CountDownLatch(uploadUris.size());
                for (Uri uploadUri : uploadUris) {
                    byte[] data;
                    try (InputStream is = ctx.getContentResolver().openInputStream(uploadUri)) {
                        data = new byte[is.available()];
                        IOUtils.read(is, data);
                    } catch (IOException ex) {
                        emitter.onError(ex);
                        emitter.onComplete();
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
                }
                try {
                    countDownLatch.await();
                    emitter.onNext(urls);
                } catch (InterruptedException e) {
                    emitter.onError(e);
                }
            } else {
                emitter.onError(new NullPointerException("oss client not initialized"));
            }
            emitter.onComplete();
        }), observer, RxUtil.Transformers.IOToMain());
    }

}
