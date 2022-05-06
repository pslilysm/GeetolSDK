package com.geetol.sdk.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import pers.cxd.corelibrary.AppHolder;
import pers.cxd.corelibrary.util.ExceptionUtil;


/**
 * 渠道工具类
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class ChannelUtil {

    /**
     * Add {@code manifestPlaceholders = [CHANNEL_NAME_VALUE: "xxxx"]} into your build.gradle's productFlavors
     * <P>Add {@code <meta-data
     *      * android:name="CHANNEL_NAME"
     *      * android:value="${CHANNEL_NAME_VALUE}" />} into your AndroidManifest.xml's Application tag</P>
     *
     * @return the channel name
     */
    public static String getChannelName() {
        Context ctx = AppHolder.get();
        try {
            ApplicationInfo appInfo = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
            return appInfo.metaData.getString("CHANNEL_NAME");
        } catch (PackageManager.NameNotFoundException e) {
            throw ExceptionUtil.rethrow(e);
        }
    }

    public static boolean isVivoChannel() {
        return getChannelName().equals("vivo");
    }

}
