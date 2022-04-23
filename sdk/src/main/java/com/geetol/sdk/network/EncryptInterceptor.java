package com.geetol.sdk.network;

import android.util.Base64;

import com.geetol.sdk.GeetolSDKConfig;
import com.geetol.sdk.manager.AppConfigManager;
import com.geetol.sdk.proguard_data.UserData;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pers.cxd.corelibrary.util.reflection.ReflectionUtil;

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
            throw new RuntimeException();
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RequestBody requestBody = request.body();
        if (requestBody instanceof FormBody) {
//            if (requestBody == null) {
//                requestBody = new FormBody.Builder().build();
//            }
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
        // 获取已经编码的Form表单
        // 这里使用的反射，需要在proguard里面配置
        List<String> encodedNames = ReflectionUtil.getFieldValue(original, "encodedNames");
        List<String> encodedValues = ReflectionUtil.getFieldValue(original, "encodedValues");
        Map<String, String> map = new TreeMap<>(String::compareTo);
        map.put("appid", GeetolSDKConfig.APP_ID);
        map.put("device", AppConfigManager.getInstance().getDeviceID());
        String user_id = "";
        String user_key = "";
        UserData userData = AppConfigManager.getInstance().getUserData();
        if (userData != null) {
            user_id = String.valueOf(userData.getUser_id());
            user_key = userData.getUkey();
        }
        map.put("user_id", user_id);
        map.put("user_key", user_key);
        for (int i = 0; i < encodedNames.size(); i++) {
            // Form表单请求的数据在达到拦截器这里的时候，已经进行了编码
            // 我们需要进行反编码再加密
            // 这里使用的反射，需要在proguard里面配置
            String key = ReflectionUtil.invokeStaticMethod(HttpUrl.class, "percentDecode",
                    String.class, boolean.class, encodedNames.get(i), true);
            String value = ReflectionUtil.invokeStaticMethod(HttpUrl.class, "percentDecode",
                    String.class, boolean.class, encodedValues.get(i), true);
            map.put(key, value);
        }
        StringBuilder sb = new StringBuilder();
        map.forEach((key, value) -> sb.append(key)
                .append("=")
                .append(Base64.encodeToString(value.getBytes(), Base64.NO_WRAP))
                .append("&"));
        sb.append("key=").append(GeetolSDKConfig.APP_KEY);
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
