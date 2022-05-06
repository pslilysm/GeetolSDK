package com.geetol.sdk;

import android.text.TextUtils;

import java.util.concurrent.ScheduledExecutorService;

import pers.cxd.corelibrary.util.ExecutorsHolder;

/**
 * GeetolSDK配置类
 *
 * @author pslilysm
 * @since 1.0.5
 */
public class GeetolConfig {

    /**
     * 后台请求地址
     */
    private final String mBaseUrl;
    /**
     * 应用的app_id，用来请求后台接口
     */
    private final String mAppId;
    /**
     * 应用的app_key，用来请求后台接口
     */
    private final String mAppKey;
    /**
     * 应用名
     */
    private final String mAppName;
    /**
     * 日志TAG
     */
    private final String mLogTag;
    /**
     * 全局IO线程池
     */
    private final ScheduledExecutorService mSdkGlobalIOExecutor;
    /**
     * 全局计算线程池
     */
    private final ScheduledExecutorService mSdkGlobalComputerExecutor;
    /**
     * 腾讯Bugly key
     */
    private final String mBuglyKey;
    /**
     * 友盟key
     */
    private final String mUmengKey;
    /**
     * DEBUG模式
     */
    private final boolean mDebug;

    private GeetolConfig(String mBaseUrl,
                        String mAppId,
                        String mAppKey,
                        String mAppName,
                        String mLogTag,
                        ScheduledExecutorService mSdkGlobalIOExecutor,
                        ScheduledExecutorService mSdkGlobalComputerExecutor,
                        String mBuglyKey,
                        String mUmengKey,
                        boolean mDebug) {
        if (TextUtils.isEmpty(mBaseUrl)
                || TextUtils.isEmpty(mAppId)
                || TextUtils.isEmpty(mAppKey)) {
            throw new IllegalArgumentException("please check the base config are initialized");
        }
        this.mBaseUrl = mBaseUrl;
        this.mAppId = mAppId;
        this.mAppKey = mAppKey;
        this.mAppName = mAppName;
        this.mLogTag = mLogTag;
        this.mSdkGlobalIOExecutor = mSdkGlobalIOExecutor;
        this.mSdkGlobalComputerExecutor = mSdkGlobalComputerExecutor;
        this.mBuglyKey = mBuglyKey;
        this.mUmengKey = mUmengKey;
        this.mDebug = mDebug;
    }

    public String getBaseUrl() {
        return mBaseUrl;
    }

    public String getAppId() {
        return mAppId;
    }

    public String getAppKey() {
        return mAppKey;
    }

    public String getAppName() {
        return mAppName;
    }

    public String getLogTag() {
        return mLogTag;
    }

    public ScheduledExecutorService getSdkGlobalIOExecutor() {
        return mSdkGlobalIOExecutor;
    }

    public ScheduledExecutorService getSdkGlobalComputerExecutor() {
        return mSdkGlobalComputerExecutor;
    }

    public String getBuglyKey() {
        return mBuglyKey;
    }

    public String getUmengKey() {
        return mUmengKey;
    }

    public boolean debug() {
        return mDebug;
    }

    public static class Builder {

        private String mBaseUrl;
        private String mAppId;
        private String mAppKey;
        private String mAppName;
        private String mLogTag = "DEBUG_GeetolSDK";
        private ScheduledExecutorService mSdkGlobalIOExecutor = ExecutorsHolder.io();
        private ScheduledExecutorService mSdkGlobalComputerExecutor = ExecutorsHolder.compute();
        private String mBuglyKey;
        private String mUmengKey;
        private boolean mDebug;

        public Builder baseUrl(String baseUrl) {
            this.mBaseUrl = baseUrl;
            return this;
        }

        public Builder appId(String appId) {
            this.mAppId = appId;
            return this;
        }

        public Builder appKey(String appKey) {
            this.mAppKey = appKey;
            return this;
        }

        public Builder appName(String appName) {
            this.mAppName = appName;
            return this;
        }

        public Builder logTag(String logTag) {
            this.mLogTag = logTag;
            return this;
        }

        public Builder ioExecutor(ScheduledExecutorService ioExecutor) {
            this.mSdkGlobalIOExecutor = ioExecutor;
            return this;
        }

        public Builder computeExecutor(ScheduledExecutorService computeExecutor) {
            this.mSdkGlobalComputerExecutor = computeExecutor;
            return this;
        }

        public Builder buglyKey(String buglyKey) {
            this.mBuglyKey = buglyKey;
            return this;
        }

        public Builder umengKey(String umengKey) {
            this.mUmengKey = umengKey;
            return this;
        }

        public Builder debug(boolean debug) {
            this.mDebug = debug;
            return this;
        }

        public GeetolConfig build() {
            return new GeetolConfig(mBaseUrl, mAppId, mAppKey, mAppName, mLogTag, mSdkGlobalIOExecutor, mSdkGlobalComputerExecutor, mBuglyKey, mUmengKey, mDebug);
        }
    }

}
