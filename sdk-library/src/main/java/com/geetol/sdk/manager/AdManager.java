package com.geetol.sdk.manager;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.geetol.sdk.GeetolSDK;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import pers.cxd.corelibrary.AppHolder;
import pers.cxd.corelibrary.SingletonFactory;
import pers.cxd.corelibrary.util.ScreenUtil;
import pers.cxd.corelibrary.util.reflection.ReflectionUtil;

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

    private static final String TT_AD_PACKAGE = "com.bytedance.sdk.openadsdk";

    private AdManager() {
    }

    /**
     * 请求加载开屏广告
     *
     * @param appId          穿山甲应用ID
     * @param adCodeId       穿山甲广告位ID
     * @param viewWidth      广告View宽度 建议使用{@link ScreenUtil#getRealWidth()}
     * @param viewHeight     广告View高度 建议使用{@link ScreenUtil#getRealHeight()} - {@link ScreenUtil#getNavBarHeight()}
     * @param splashCallback 广告回调，注意这里使用的是WeakReference，所以一定要UI组件去实现回调，不能使用匿名内部类，不然可能会导致没有回调
     * @throws ReflectiveOperationException 未依赖穿山甲广告SDK时抛这个异常，如果依赖了请查看包名是否被混淆还是其他的原因
     */
    public void loadSplashAd(String appId, String adCodeId, int viewWidth, int viewHeight, SplashCallback splashCallback)
            throws ReflectiveOperationException {
        if (splashCallback.getClass().isAnonymousClass()) {
            throw new IllegalArgumentException("An anonymous inner class cannot be used as a splashCallback");
        }
        final WeakReference<SplashCallback> wrCallback = new WeakReference<>(splashCallback);
        if (!initialized) {
            ClassLoader classLoader = AppHolder.get().getClassLoader();
            Class<?> callbackClazz = classLoader.loadClass(TT_AD_PACKAGE + ".TTAdSdk$InitCallback");
            Object initCallback = Proxy.newProxyInstance(classLoader, new Class[]{callbackClazz}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    switch (method.getName()) {
                        case "success":
                            initialized = true;
                            loadSplashAdInternal(adCodeId, viewWidth, viewHeight, wrCallback);
                            break;
                        case "fail":
                            SplashCallback splashCallback1 = wrCallback.get();
                            if (splashCallback1 != null && splashCallback1.isActive()) {
                                splashCallback1.onFinish();
                            }
                            break;
                    }
                    return null;
                }
            });
            initAd(appId, callbackClazz, initCallback);
        } else {
            loadSplashAdInternal(adCodeId, viewWidth, viewHeight, wrCallback);
        }
    }

    private void loadSplashAdInternal(String adCodeId, int viewWidth, int viewHeight, WeakReference<SplashCallback> wrCallback)
            throws ReflectiveOperationException {
        ClassLoader classLoader = AppHolder.get().getClassLoader();
        Object adManager = ReflectionUtil.invokeStaticMethod(TT_AD_PACKAGE + ".TTAdSdk", classLoader, "getAdManager");
        Object mTTAdNative = ReflectionUtil.invokeMethod(adManager, "createAdNative", Context.class, AppHolder.get());
        Object adSlotBuilder = ReflectionUtil.newInstance(TT_AD_PACKAGE + ".AdSlot$Builder", classLoader);
        Class<?> adLoadTypeClazz = classLoader.loadClass(TT_AD_PACKAGE + ".TTAdLoadType");
        Object[] adLoadTypes = adLoadTypeClazz.getEnumConstants();
        Object adLoadType = null;
        assert adLoadTypes != null;
        for (Object loadType : adLoadTypes) {
            if (TextUtils.equals(loadType.toString(), "PRELOAD")) {
                adLoadType = loadType;
                break;
            }
        }
        ReflectionUtil.invokeMethod(adSlotBuilder, "setCodeId", String.class, adCodeId);
        ReflectionUtil.invokeMethod(adSlotBuilder, "setImageAcceptedSize", int.class, int.class, viewWidth, viewHeight);
        ReflectionUtil.invokeMethod(adSlotBuilder, "setExpressViewAcceptedSize", float.class, float.class, viewWidth, viewHeight);
        ReflectionUtil.invokeMethod(adSlotBuilder, "setAdLoadType", adLoadTypeClazz, adLoadType);
        ReflectionUtil.invokeMethod(adSlotBuilder, "setCodeId", String.class, adCodeId);
        Object adSlot = ReflectionUtil.invokeMethod(adSlotBuilder, "build");
        Class<?> splashAdListenerClazz = classLoader.loadClass(TT_AD_PACKAGE + ".TTAdNative$SplashAdListener");
        Object splashAdListener = Proxy.newProxyInstance(classLoader, new Class[]{splashAdListenerClazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                switch (method.getName()) {
                    case "onError":
                    case "onTimeout": {
                        SplashCallback splashCallback = wrCallback.get();
                        if (splashCallback != null && splashCallback.isActive()) {
                            splashCallback.onFinish();
                        }
                    }
                    break;
                    case "onSplashAdLoad":
                        Object ttSplashAd = args[0];
                        SplashCallback splashCallback = wrCallback.get();
                        if (splashCallback != null && splashCallback.isActive()) {
                            if (ttSplashAd != null) {
                                View splashView = ReflectionUtil.invokeMethod(ttSplashAd, "getSplashView");
                                if (splashView != null) {
                                    splashCallback.onSplashAdLoaded(splashView);
                                    Class<?> adInteractionListenerClazz = classLoader.loadClass(TT_AD_PACKAGE + ".TTSplashAd$AdInteractionListener");
                                    Object adInteractionListener = Proxy.newProxyInstance(classLoader, new Class[]{adInteractionListenerClazz}, new InvocationHandler() {
                                        @Override
                                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                            switch (method.getName()) {
                                                case "onAdSkip":
                                                case "onAdTimeOver":
                                                    SplashCallback splashCallback1 = wrCallback.get();
                                                    if (splashCallback1 != null && splashCallback1.isActive()) {
                                                        splashCallback1.onFinish();
                                                    }
                                                    break;
                                            }
                                            return null;
                                        }
                                    });
                                    ReflectionUtil.invokeMethod(ttSplashAd, "setSplashInteractionListener",
                                            adInteractionListenerClazz,
                                            adInteractionListener);
                                }
                            } else {
                                splashCallback.onFinish();
                            }
                        }
                        break;
                }
                return null;
            }
        });
        ReflectionUtil.invokeMethod(mTTAdNative, "loadSplashAd", adSlot.getClass(), splashAdListenerClazz, int.class,
                adSlot, splashAdListener, 4000);
    }

    private void initAd(String appId, Class<?> initCallbackClazz, Object initCallback) throws ReflectiveOperationException {
        ClassLoader classLoader = AppHolder.get().getClassLoader();
        Class<?> builderClazz = classLoader.loadClass(TT_AD_PACKAGE + ".TTAdConfig$Builder");
        Class<?> controllerClazz = classLoader.loadClass(TT_AD_PACKAGE + ".TTCustomController");
        Object builder = ReflectionUtil.newInstance(builderClazz);
        ReflectionUtil.invokeMethod(builder, "appId", String.class, appId);
        ReflectionUtil.invokeMethod(builder, "useTextureView", boolean.class, false);
        ReflectionUtil.invokeMethod(builder, "appName", String.class, GeetolSDK.getConfig().getAppName());
        ReflectionUtil.invokeMethod(builder, "titleBarTheme", int.class, 1);
        ReflectionUtil.invokeMethod(builder, "allowShowNotify", boolean.class, true);
        ReflectionUtil.invokeMethod(builder, "debug", boolean.class, GeetolSDK.getConfig().debug());
        ReflectionUtil.invokeMethod(builder, "directDownloadNetworkType", int[].class, new int[0]);
        ReflectionUtil.invokeMethod(builder, "supportMultiProcess", boolean.class, false);
        Object adConfig = ReflectionUtil.invokeMethod(builder, "build");
        ReflectionUtil.invokeStaticMethod(TT_AD_PACKAGE + ".TTAdSdk", classLoader, "init",
                Context.class,
                adConfig.getClass(),
                initCallbackClazz,
                AppHolder.get(),
                adConfig,
                initCallback);
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
