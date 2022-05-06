package com.geetol.sdk.proguard_data;

import java.util.List;

/**
 * 后台返回的公共集合结果模板
 *
 * @param <D> 集合里的数据类
 * @author pslilysm
 * @since 1.0.0
 */
public class CommonListResult<D> {

    private int page;
    private int count;
    private List<D> items;
    private boolean issucc;
    private String msg;
    private String code;

    public int getPage() {
        return page;
    }

    public int getCount() {
        return count;
    }

    public List<D> getItems() {
        return items;
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
