package com.geetol.sdk;

/**
 * SDK初始化类
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class GeetolSDKConfig {

    public static String LOG_TAG = "DEBUG_GeetolSDK";
    public static String BASE_URL;
    public static String APP_ID;
    public static String APP_KEY;
    public static String APP_NAME;

    /**
     * 设置SDK打印日志的TAG，Release包不会输出日志
     *
     * @param logTag
     */
    public static void setLogTag(String logTag) {
        LOG_TAG = logTag;
    }

    /**
     * 初始化SDK配置
     * 在Application初始化时调用
     *
     * @param url      后台请求地址
     * @param app_id   应用的app_id，用来请求后台接口
     * @param app_key  应用的app_key，用来请求后台接口
     * @param app_name 应用名
     */
    public static void init(String url, String app_id, String app_key, String app_name) {
        BASE_URL = url;
        APP_ID = app_id;
        APP_KEY = app_key;
        APP_NAME = app_name;
    }

}
