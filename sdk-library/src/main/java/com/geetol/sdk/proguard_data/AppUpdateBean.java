package com.geetol.sdk.proguard_data;

/**
 * 应用检查更新返回的数据类
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class AppUpdateBean {

    boolean hasnew;
    String downurl;
    String vername;
    int vercode;
    String log;
    int total;
    boolean issucc;
    String msg;
    String code;

    public boolean isHasnew() {
        return hasnew;
    }

    public String getDownurl() {
        return downurl;
    }

    public String getVername() {
        return vername;
    }

    public int getVercode() {
        return vercode;
    }

    public String getLog() {
        return log;
    }

    public int getTotal() {
        return total;
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
