package com.geetol.sdk.proguard_data;

/**
 * 微信订单
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class WechatOrder {

    private String appid;
    private String amount;
    private String timestramp;
    private String nonce_str;
    private String package_str;
    private String sign;
    private String sign_str;
    private String qrcode;
    private String mweburl;
    private String partnerId;
    private String prepayid;
    private boolean issucc;
    private String msg;
    private String code;

    public String getAppid() {
        return appid;
    }

    public String getAmount() {
        return amount;
    }

    public String getTimestramp() {
        return timestramp;
    }

    public String getNonce_str() {
        return nonce_str;
    }

    public String getPackage_str() {
        return package_str;
    }

    public String getSign() {
        return sign;
    }

    public String getSign_str() {
        return sign_str;
    }

    public String getQrcode() {
        return qrcode;
    }

    public String getMweburl() {
        return mweburl;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public String getPrepayid() {
        return prepayid;
    }

    public boolean isIssucc() {
        return issucc;
    }

    public String getMsg() {
        return msg;
    }

    public String getCode() {
        return code;
    }
}
