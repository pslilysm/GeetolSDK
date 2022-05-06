package com.geetol.sdk;

/**
 * The GeetolSDK
 *
 * @author pslilysm
 * @since 1.0.5
 */
public class GeetolSDK {

    private static GeetolConfig sConfig;

    public static void init(GeetolConfig config) {
        sConfig = config;
    }

    public static GeetolConfig getConfig() {
        if (sConfig == null) {
            throw new IllegalStateException("please check if init is called");
        }
        return sConfig;
    }
}
