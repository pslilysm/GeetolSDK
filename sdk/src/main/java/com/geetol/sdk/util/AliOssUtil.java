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
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import pers.cxd.corelibrary.AppHolder;

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
     * 上传文件
     *
     * @param uploadFile 需要上传的文件
     * @param callback 回调
     */
    public static void uploadFile(File uploadFile, Callback callback) {
        final WeakReference<Callback> wrCallback = new WeakReference<>(callback);
        if (sOSSClient != null) {
            String aliOssName = new String(Hex.encodeHex(DigestUtils.md5(uploadFile.getName())));
            sOSSClient.asyncPutObject(new PutObjectRequest(sConfig.getBucketName(), aliOssName, uploadFile.getAbsolutePath()), new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
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
        } else {
            callback.onFailure();
        }
    }

    /**
     * 将Uri上传至阿里云
     *
     * @param ctx 用来获取Uri的内容
     * @param uri 需要上传的Uri
     * @param callback 回调
     */
    public static void uploadFile(Context ctx, Uri uri, Callback callback) {
        final WeakReference<Callback> wrCallback = new WeakReference<>(callback);
        if (sOSSClient != null) {
            byte[] data;
            try (InputStream is = ctx.getContentResolver().openInputStream(uri)) {
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
        } else {
            callback.onFailure();
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

}
