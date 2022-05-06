package com.geetol.sdk.proguard_data;

import java.util.List;

/**
 * 意见反馈详情
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class FeedbackInfo {

    private int id;
    private String addtime;
    private int status;
    private String title;
    private String describe;
    private String type;
    private String endtime;
    private String evalevel;
    private String evadesc;
    private String device_id;
    private String staff;
    private List<ImageBean> img;
    private List<ReplyBean> reply;

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

    public String getEndtime() {
        return endtime;
    }

    public String getEvalevel() {
        return evalevel;
    }

    public String getEvadesc() {
        return evadesc;
    }

    public String getDevice_id() {
        return device_id;
    }

    public String getStaff() {
        return staff;
    }

    public List<ImageBean> getImg() {
        return img;
    }

    public List<ReplyBean> getReply() {
        return reply;
    }

    public static class ReplyBean {
        public static final int RIGHT = 1;
        public static final int LEFT = 2;

        private String addtime;
        private String describe;
        private int type;
        private String staff;
        private List<ImageBean> img;

        public static int getRIGHT() {
            return RIGHT;
        }

        public static int getLEFT() {
            return LEFT;
        }

        public String getAddtime() {
            return addtime;
        }

        public String getDescribe() {
            return describe;
        }

        public int getType() {
            return type;
        }

        public String getStaff() {
            return staff;
        }

        public List<ImageBean> getImg() {
            return img;
        }
    }

    public static class ImageBean {
        private String path;

        public String getPath() {
            return path;
        }
    }

}
