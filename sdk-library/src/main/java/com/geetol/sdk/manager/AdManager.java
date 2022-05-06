package com.geetol.sdk.manager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdLoadType;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTCustomController;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.geetol.sdk.BuildConfig;
import com.geetol.sdk.GTSDKConfig;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.commonsdk.listener.OnGetOaidListener;

import java.lang.ref.WeakReference;

import pers.cxd.corelibrary.AppHolder;
import pers.cxd.corelibrary.SingletonFactory;
import pers.cxd.corelibrary.util.ScreenUtil;

/**
 * 广告Manager
 * 用于请求开屏页面广告
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class AdManager {

    public static AdManager getInstance() {
        return SingletonFactory.findOrCreate(AdManager.class);
    }

    private volatile boolean initialized;

    private AdManager() {
    }

    /**
     * 请求加载开屏广告
     *
     * @param appId          穿山甲应用ID
     * @param adCodeId       穿山甲广告位ID
     * @param viewWidth      广告View宽度 建议使用{@link ScreenUtil#getRealWidth()}
     * @param viewHeight     广告View高度 建议使用{@link ScreenUtil#getRealHeight()} - {@link ScreenUtil#getNavBarHeight()}
     * @param splashCallback 广告回调
     */
    public void loadSplashAd(String appId, String adCodeId, int viewWidth, int viewHeight, SplashCallback splashCallback) {
        final WeakReference<SplashCallback> wrCallback = new WeakReference<>(splashCallback);
        if (!initialized) {
            UMConfigure.getOaid(AppHolder.get(), new OnGetOaidListener() {
                @Override
                public void onGetOaid(String s) {
                    initAd(appId, s, new TTAdSdk.InitCallback() {
                        @Override
                        public void success() {
                            initialized = true;
                            loadSplashAdInternal(adCodeId, viewWidth, viewHeight, wrCallback);
                        }

                        @Override
                        public void fail(int i, String s) {
                            SplashCallback splashCallback1 = wrCallback.get();
                            if (splashCallback1 != null && splashCallback1.isActive()) {
                                splashCallback1.onFinish();
                            }
                        }
                    });
                }
            });
        } else {
            loadSplashAdInternal(adCodeId, viewWidth, viewHeight, wrCallback);
        }
    }

    private void loadSplashAdInternal(String adCodeId, int viewWidth, int viewHeight, WeakReference<SplashCallback> wrCallback) {
        TTAdNative mTTAdNative = TTAdSdk.getAdManager().createAdNative(AppHolder.get());
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(adCodeId)
                .setImageAcceptedSize(viewWidth, viewHeight)
                .setExpressViewAcceptedSize(viewWidth, viewHeight)
                .setAdLoadType(TTAdLoadType.PRELOAD)
                .build();
        mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
            @Override
            public void onError(int i, String s) {
                SplashCallback splashCallback = wrCallback.get();
                if (splashCallback != null && splashCallback.isActive()) {
                    splashCallback.onFinish();
                }
            }

            @Override
            public void onTimeout() {
                SplashCallback splashCallback = wrCallback.get();
                if (splashCallback != null && splashCallback.isActive()) {
                    splashCallback.onFinish();
                }
            }

            @Override
            public void onSplashAdLoad(TTSplashAd ad) {
                //获取SplashView
                SplashCallback splashCallback = wrCallback.get();
                if (splashCallback != null && splashCallback.isActive()) {
//                    mSplashContainer.removeAllViews();
                    //把SplashView 添加到ViewGroup中,注意开屏广告view：width =屏幕宽；height >=75%屏幕高
                    if (ad != null && ad.getSplashView() != null) {
                        splashCallback.onSplashAdLoaded(ad.getSplashView());
                        ad.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
                            @Override
                            public void onAdClicked(View view, int i) {
                            }

                            @Override
                            public void onAdShow(View view, int i) {
                            }

                            @Override
                            public void onAdSkip() {
                                SplashCallback splashCallback1 = wrCallback.get();
                                if (splashCallback1 != null && splashCallback1.isActive()) {
                                    splashCallback1.onFinish();
                                }
                            }

                            @Override
                            public void onAdTimeOver() {
                                SplashCallback splashCallback1 = wrCallback.get();
                                if (splashCallback1 != null && splashCallback1.isActive()) {
                                    splashCallback1.onFinish();
                                }
                            }
                        });
                    } else {
                        splashCallback.onFinish();
                    }
                }
            }
        }, 4000);
    }

    private void initAd(String appId, String oaId, TTAdSdk.InitCallback initCallback) {
        TTAdConfig adConfig = new TTAdConfig.Builder()
                .appId(appId)
                .useTextureView(false) //默认使用SurfaceView播放视频广告,当有SurfaceView冲突的场景，可以使用TextureView
                .appName(GTSDKConfig.APP_NAME)
                .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK) //落地页主题
                .allowShowNotify(true) //是否允许sdk展示通知栏提示,若设置为false则会导致通知栏不显示下载进度
                .debug(BuildConfig.DEBUG) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI) //允许直接下载的网络状态集合,没有设置的网络下点击下载apk会有二次确认弹窗，弹窗中会披露应用信息
                .supportMultiProcess(false)
                .customController(new TTCustomController() {
                    @Override
                    public boolean isCanUseLocation() {
                        return ContextCompat.checkSelfPermission(AppHolder.get(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && ContextCompat.checkSelfPermission(AppHolder.get(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                    }

                    @Override
                    public boolean isCanUsePhoneState() {
                        return ContextCompat.checkSelfPermission(AppHolder.get(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
                    }

                    @Override
                    public boolean isCanUseWriteExternal() {
                        return ContextCompat.checkSelfPermission(AppHolder.get(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                    }

                    @Override
                    public String getDevOaid() {
                        return oaId;
                    }
                })
                .build();
        TTAdSdk.init(AppHolder.get(), adConfig, initCallback);
    }

    public interface SplashCallback {
        void onSplashAdLoaded(View view);

        void onFinish();

        boolean isFinishing();

        boolean isDestroyed();

        default boolean isActive() {
            return !isFinishing() && !isDestroyed();
        }
    }

}
