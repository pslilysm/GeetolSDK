package com.geetol.sdk;

import android.text.TextUtils;

import androidx.multidex.MultiDexApplication;

import com.geetol.sdk.constant.MMKVKeys;
import com.geetol.sdk.manager.AppConfigManager;
import com.geetol.sdk.proguard_data.UserConfig;
import com.geetol.sdk.util.ChannelUtil;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.commonsdk.UMConfigure;

import java.util.Objects;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import pers.cxd.corelibrary.AppHolder;
import pers.cxd.corelibrary.Singleton;
import pers.cxd.corelibrary.util.MMKVUtil;

/**
 * GT的基本Application，提供微信的API，并且会在合适的时机初始化Umeng和Bugly
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class GTApplication extends MultiDexApplication {

    private static final Singleton<IWXAPI> sWechatApi = new Singleton<IWXAPI>() {
        @Override
        protected IWXAPI create() {
            UserConfig userConfig = AppConfigManager.getInstance().getUserConfig();
            Objects.requireNonNull(userConfig);
            IWXAPI iwxapi = WXAPIFactory.createWXAPI(AppHolder.get(), userConfig.getConfig().getWxid(), true);
            iwxapi.registerApp(userConfig.getConfig().getWxid());
            return iwxapi;
        }
    };

    public static IWXAPI getWechatApi() {
        return sWechatApi.getInstance();
    }

    private static GTApplication sInstance;

    public static GTApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        RxJavaPlugins.setErrorHandler(throwable -> {
        });
        if (!TextUtils.isEmpty(GTSDKConfig.UMENG_KEY)) {
            UMConfigure.preInit(this, GTSDKConfig.UMENG_KEY, ChannelUtil.getChannelName());
        }
        if (MMKVUtil.decode(MMKVKeys.USER_AGREED_PROTOCOL, false)) {
            init();
        }
    }

    public void init() {
        if (!TextUtils.isEmpty(GTSDKConfig.BUGLY_KEY)) {
            CrashReport.initCrashReport(this, GTSDKConfig.BUGLY_KEY, BuildConfig.DEBUG);
        }
        if (!TextUtils.isEmpty(GTSDKConfig.UMENG_KEY)) {
            UMConfigure.init(this, GTSDKConfig.UMENG_KEY, ChannelUtil.getChannelName(), UMConfigure.DEVICE_TYPE_PHONE, null);
        }
    }

}
