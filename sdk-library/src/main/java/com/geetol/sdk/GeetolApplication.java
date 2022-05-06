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
public abstract class GeetolApplication extends MultiDexApplication {

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

    private static GeetolApplication sInstance;

    public static GeetolApplication getInstance() {
        return sInstance;
    }

    protected abstract GeetolConfig buildGeetolConfig(GeetolConfig.Builder builder);

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        RxJavaPlugins.setErrorHandler(throwable -> {
        });
        GeetolSDK.init(buildGeetolConfig(new GeetolConfig.Builder()));
        if (!TextUtils.isEmpty(GeetolSDK.getConfig().getUmengKey())) {
            UMConfigure.preInit(this, GeetolSDK.getConfig().getUmengKey(), ChannelUtil.getChannelName());
        }
        if (MMKVUtil.decode(MMKVKeys.USER_AGREED_PROTOCOL, false)) {
            init();
        }
    }

    public void init() {
        if (!TextUtils.isEmpty(GeetolSDK.getConfig().getBuglyKey())) {
            CrashReport.initCrashReport(this, GeetolSDK.getConfig().getBuglyKey(), GeetolSDK.getConfig().debug());
        }
        if (!TextUtils.isEmpty(GeetolSDK.getConfig().getUmengKey())) {
            UMConfigure.init(this, GeetolSDK.getConfig().getUmengKey(), ChannelUtil.getChannelName(), UMConfigure.DEVICE_TYPE_PHONE, null);
        }
    }

}
