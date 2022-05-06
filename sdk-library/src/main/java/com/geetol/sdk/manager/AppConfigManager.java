package com.geetol.sdk.manager;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.geetol.sdk.constant.MMKVKeys;
import com.geetol.sdk.network.AppApi;
import com.geetol.sdk.network.GTRetrofitClient;
import com.geetol.sdk.proguard_data.AliOssConfig;
import com.geetol.sdk.proguard_data.CommonResult;
import com.geetol.sdk.proguard_data.UserConfig;
import com.geetol.sdk.proguard_data.UserData;
import com.geetol.sdk.util.AliOssUtil;
import com.geetol.sdk.util.secret.BASE64Decoder;
import com.geetol.sdk.util.secret.DesUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.reactivex.rxjava3.annotations.NonNull;
import pers.cxd.corelibrary.SingletonFactory;
import pers.cxd.corelibrary.util.ExecutorsHolder;
import pers.cxd.corelibrary.util.GsonUtil;
import pers.cxd.corelibrary.util.MMKVUtil;
import pers.cxd.corelibrary.util.ScreenUtil;
import pers.cxd.corelibrary.util.ThreadUtil;
import pers.cxd.rxlibrary.BaseHttpObserverImpl;
import pers.cxd.rxlibrary.RxUtil;

/**
 * 应用基本配置Manager
 * 用于管理{@link UserConfig}、{@link UserData}、{@link AliOssConfig}这三个数据
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class AppConfigManager {

    private final Handler mMainH = new Handler(Looper.getMainLooper());
    private volatile UserConfig mUserConfig;
    private volatile UserData mUserData;
    private volatile AliOssConfig mAliOssConfig;

    private AppConfigManager() {
        mUserConfig = GsonUtil.jsonToObject(MMKVUtil.decode(MMKVKeys.USER_CONFIG, String.class), UserConfig.class);
        mUserData = GsonUtil.jsonToObject(MMKVUtil.decode(MMKVKeys.USER_DATA, String.class), UserData.class);
        mAliOssConfig = GsonUtil.jsonToObject(MMKVUtil.decode(MMKVKeys.ALI_OSS_CONFIG, String.class), AliOssConfig.class);
        AliOssUtil.initOSSClient(mAliOssConfig);
    }

    public static AppConfigManager getInstance() {
        return SingletonFactory.findOrCreate(AppConfigManager.class);
    }

    /**
     * 异步初始化，在用户同意了用户协议和隐私政策之后调用
     *
     * @param callback 初始化完成或失败后的回调
     */
    public void initAsync(InitCallback callback) {
        ExecutorsHolder.io().execute(() -> {
            if (!MMKVUtil.decode(MMKVKeys.DEVICE_REGISTERED, false)) {
                registerDeviceBlocked();
            }
            if (mAliOssConfig == null) {
                initAliOssConfigBlocked();
            }
            refreshUserConfigBlocked();
            mMainH.post(() -> {
                if (isAppInitialized()) {
                    callback.initialized();
                } else {
                    callback.initFailed();
                }
            });
        });
    }

    /**
     * @return 当前用户的配置，包含开关、会员、微信AppId等信息
     */
    @Nullable
    public UserConfig getUserConfig() {
        return mUserConfig;
    }

    public void setUserConfig(UserConfig userConfig) {
        this.mUserConfig = userConfig;
        MMKVUtil.encode(MMKVKeys.USER_CONFIG, GsonUtil.objToJson(userConfig));
    }

    /**
     * @return 当前登录之后的用户数据，包含头像、姓名等信息
     */
    @Nullable
    public UserData getUserData() {
        return mUserData;
    }

    public void setUserData(UserData userData) {
        this.mUserData = userData;
        MMKVUtil.encode(MMKVKeys.USER_DATA, GsonUtil.objToJson(userData));
    }

    /**
     * @return 当前应用的阿里云配置
     */
    @Nullable
    public AliOssConfig getAliOssConfig() {
        return mAliOssConfig;
    }

    public void setAliOssConfig(String aliOssConfigEncrypted) {
        String json = decrypt(aliOssConfigEncrypted);
        if (json != null) {
            this.mAliOssConfig = GsonUtil.jsonToObject(json, AliOssConfig.class);
            AliOssUtil.initOSSClient(mAliOssConfig);
            MMKVUtil.encode(MMKVKeys.ALI_OSS_CONFIG, GsonUtil.objToJson(mAliOssConfig));
        }
    }

    /**
     * @return 设备ID，未注册会随机生成一个，供接口请求使用
     */
    public String getDeviceID() {
        String deviceId = MMKVUtil.decode(MMKVKeys.DEVICE_ID, String.class);
        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString().replaceAll("-", "");
            MMKVUtil.encode(MMKVKeys.DEVICE_ID, deviceId);
        }
        return deviceId;
    }

    /**
     * @return true 代表应用基本配置已经初始化完成
     */
    public boolean isAppInitialized() {
        return mUserConfig != null && mAliOssConfig != null;
    }

    /**
     * @return true 表示用户已经登录
     */
    public boolean isLogin() {
        return mUserData != null && mUserConfig != null;
    }

    /**
     * @return true 表示用户是用手机号登录的
     */
    public boolean isLoginByPhone() {
        return TextUtils.isEmpty(MMKVUtil.decode(MMKVKeys.WECHAT_OPEN_ID, String.class));
    }

    /**
     * @return true 表示用户是会员，注意！！未登录也可以是会员
     */
    public boolean isVip() {
        return mUserConfig != null && !mUserConfig.getVip().isIsout();
    }

    /**
     * 登录之后调用该方法，该方法在保存好用户数据后还会同步请求用户配置接口
     *
     * @param userData 用户数据
     */
    public void login(UserData userData) {
        setUserData(userData);
        refreshUserConfigBlocked();
    }

    /**
     * 退出登录，也就是将本地保存的用户数据清空，同时会去更新APP配置。
     */
    public void logout() {
        setUserData(null);
        MMKVUtil.encode(MMKVKeys.WECHAT_OPEN_ID, String.class);
        refreshUserConfigBlocked();
    }

    /**
     * 注销，会BLOCK直到所有接口调用完成
     */
    public void logoff() {
        setUserConfig(null);
        setUserData(null);
        MMKVUtil.encode(MMKVKeys.DEVICE_REGISTERED, false);
        MMKVUtil.encode(MMKVKeys.DEVICE_ID, String.class);
        MMKVUtil.encode(MMKVKeys.WECHAT_OPEN_ID, String.class);
        registerDeviceBlocked();
        refreshUserConfigBlocked();
    }

    /**
     * 注册设备，同时会调用ali配置和app配置接口
     */
    private void registerDeviceBlocked() {
        ThreadUtil.throwIfMainThread();
        Map<String, String> param = new HashMap<>();
        param.put("mac", "");
        param.put("brand", Build.BRAND);
        param.put("model", Build.MODEL);
        param.put("widthpix", String.valueOf(ScreenUtil.getWidth()));
        param.put("heightpix", String.valueOf(ScreenUtil.getHeight()));
        param.put("vercode", Build.VERSION.RELEASE);
        param.put("agent", "geetol");
        RxUtil.execute(GTRetrofitClient.getInstance().getApi(AppApi.class).registerDevice(param),
                new BaseHttpObserverImpl<CommonResult<Void>>(null) {
                    @Override
                    public void onNext(@NonNull CommonResult<Void> voidCommonResult) {
                        if (voidCommonResult.isIssucc()) {
                            MMKVUtil.encode(MMKVKeys.DEVICE_REGISTERED, true);
                        }
                    }
                }, RxUtil.Transformers.NON());
    }

    /**
     * 初始化阿里云OSS配置，该方法会阻塞直至接口调用完成
     *
     * <strong>Note:</strong> 调用接口失败没获取到数据也会返回
     */
    private void initAliOssConfigBlocked() {
        ThreadUtil.throwIfMainThread();
        RxUtil.execute(GTRetrofitClient.getInstance().getApi(AppApi.class).getAliOss(new HashMap<>()),
                new BaseHttpObserverImpl<CommonResult<String>>(null) {
                    @Override
                    public void onNext(@NonNull CommonResult<String> stringCommonResult) {
                        if (stringCommonResult.isIssucc()) {
                            String data = stringCommonResult.getData();
                            setAliOssConfig(data);
                        }
                    }
                }, RxUtil.Transformers.NON());
    }

    /**
     * 更新用户配置，该方法会阻塞直至更新完成
     *
     * <strong>Note:</strong> 调用接口失败没获取到数据也会返回
     */
    public void refreshUserConfigBlocked() {
        ThreadUtil.throwIfMainThread();
        RxUtil.execute(GTRetrofitClient.getInstance().getApi(AppApi.class).getUserConfig(new HashMap<>()),
                new BaseHttpObserverImpl<UserConfig>(null) {
                    @Override
                    public void onNext(@NonNull UserConfig userConfig) {
                        if (userConfig.isIssucc()) {
                            AppConfigManager.getInstance().setUserConfig(userConfig);
                        } else if (TextUtils.equals(userConfig.getCode(), "0x1002")) {
                            logout();
                        }
                    }
                }, RxUtil.Transformers.NON());
    }

    /**
     * 获取开关的val1值
     *
     * @param code       开关代码位
     * @param defaultVal 默认值
     * @return the val1 represent the code, or defaultVal
     */
    public int getSwtVal1(String code, int defaultVal) {
        if (mUserConfig != null) {
            for (UserConfig.Swt swt : mUserConfig.getSwt()) {
                if (TextUtils.equals(code, swt.getCode())) {
                    return swt.getVal1();
                }
            }
        }
        return defaultVal;
    }

    /**
     * 获取开关的val2值
     *
     * @param code       开关代码位
     * @param defaultVal 默认值
     * @return the val2 represent the code, or defaultVal
     */
    public String getSwtVal2(String code, String defaultVal) {
        if (mUserConfig != null) {
            for (UserConfig.Swt swt : mUserConfig.getSwt()) {
                if (TextUtils.equals(code, swt.getCode())) {
                    return swt.getVal2();
                }
            }
        }
        return defaultVal;
    }

    /**
     * 判断开关是否打开
     *
     * @param code 开关代码位
     * @return true 代表开关打开了
     */
    public boolean isSwtOpen(String code) {
        if (mUserConfig != null) {
            for (UserConfig.Swt swt : mUserConfig.getSwt()) {
                if (TextUtils.equals(code, swt.getCode()) && swt.getVal1() == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private String decrypt(String encrypted) {
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] bytes = decoder.decodeBuffer(encrypted);
            byte[] bytes1 = DesUtil.decode(bytes);
            if (bytes1 != null) {
                return new String(bytes1);
            }
        } catch (IOException e) {
        }
        return null;
    }

    public interface InitCallback {

        /**
         * 应用基本数据初始化完成
         */
        void initialized();

        /**
         * 应用基本数据初始化失败，
         * 原因大多数都是网络问题，这时应该提醒用户去重试
         */
        void initFailed();
    }

}
