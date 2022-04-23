package com.geetol.sdk.proguard_data;

/**
 * 支付宝订单
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class AlipayOrder {
    private boolean issucc;
    private String msg;
    private String code;
    private String appid;
    private float amount;
    private String package_str;

    public boolean isIssucc() {
        return issucc;
    }

    public String getMsg() {
        return msg;
    }

    public String getCode() {
        return code;
    }

    public String getAppid() {
        return appid;
    }

    public float getAmount() {
        return amount;
    }

    public String getPackage_str() {
        return package_str;
    }
}
