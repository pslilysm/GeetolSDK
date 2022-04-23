package com.geetol.sdk.proguard_data;

/**
 * 后台返回的公共结果模板
 *
 * @param <D> 数据类
 * @author pslilysm
 * @since 1.0.0
 */
public class CommonResult<D> {

    int total;
    boolean issucc;
    String msg;
    String code;
    D data;

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

    public D getData() {
        return data;
    }
}
