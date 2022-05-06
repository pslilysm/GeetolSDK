package com.geetol.sdk.network;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.geetol.sdk.GeetolSDK;
import com.geetol.sdk.manager.AppConfigManager;
import com.geetol.sdk.proguard_data.UserData;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pers.cxd.corelibrary.util.ExceptionUtil;

/**
 * 对请求接口的数据进行加密的OkHttp拦截器
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class EncryptInterceptor implements Interceptor {

    private final MessageDigest mDigest;

    public EncryptInterceptor() {
        try {
            mDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RequestBody requestBody = request.body();
        if (requestBody instanceof FormBody) {
            try {
                FormBody formBody = appendCommonForm((FormBody) requestBody);
                request = request.newBuilder()
                        .post(formBody)
                        .build();
            } catch (ReflectiveOperationException ignore) {
            }
        }
        return chain.proceed(request);
    }

    private FormBody appendCommonForm(FormBody original) throws ReflectiveOperationException {
        Map<String, String> map = new TreeMap<>(String::compareTo);
        map.put("appid", GeetolSDK.getConfig().getAppId());
        map.put("device", AppConfigManager.getInstance().getDeviceID());
        UserData userData = AppConfigManager.getInstance().getUserData();
        if (userData != null) {
            map.put("user_id", String.valueOf(userData.getUser_id()));
            map.put("user_key", userData.getUkey());
        }
        int size = original.size();
        for (int i = 0; i < size; i++) {
            map.put(original.name(i), original.value(i));
        }
        StringBuilder sb = new StringBuilder();
        map.forEach((key, value) -> sb.append(key)
                .append("=")
                .append(Base64.encodeToString(value.getBytes(), Base64.NO_WRAP))
                .append("&"));
        sb.append("key=").append(GeetolSDK.getConfig().getAppKey());
        mDigest.update(sb.toString().getBytes());
        FormBody.Builder builder = new FormBody.Builder();
        map.put("sign", byte2hex(mDigest.digest()));
        map.forEach(builder::add);
        return builder.build();
    }

    private String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String temp;
        for (byte value : b) {
            temp = (Integer.toHexString(value & 0XFF));
            if (temp.length() == 1) {
                hs.append("0").append(temp);
            } else {
                hs.append(temp);
            }
        }
        return hs.toString();
    }

}
