package com.geetol.sdk.proguard_data;

/**
 * 意见反馈简略内容
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class FeedbackItem {

    private int id;
    private String addtime;
    private int status;
    private String title;
    private String describe;
    private String type;

    public int getId() {
        return id;
    }

    public String getAddtime() {
        return addtime;
    }

    public int getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getDescribe() {
        return describe;
    }

    public String getType() {
        return type;
    }
}
