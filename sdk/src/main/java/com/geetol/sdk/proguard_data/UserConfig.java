package com.geetol.sdk.proguard_data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户基本配置
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class UserConfig {

    private Vip vip;
    private List<Object> ads;
    private List<Swt> swt;
    private List<Good> gds;
    private Contract contract;
    private String hpurl;
    private String context;
    private Config config;
    private String share_url;
    private ArrayList<Object> region;
    private String ip;
    private ArrayList<Object> nads;
    private int total;
    private boolean issucc;
    private String msg;
    private String code;

    public Vip getVip() {
        return vip;
    }

    public List<Object> getAds() {
        return ads;
    }

    public List<Swt> getSwt() {
        return swt;
    }

    public List<Good> getGds() {
        return gds;
    }

    public Contract getContract() {
        return contract;
    }

    public String getHpurl() {
        return hpurl;
    }

    public String getContext() {
        return context;
    }

    public Config getConfig() {
        return config;
    }

    public String getShare_url() {
        return share_url;
    }

    public ArrayList<Object> getRegion() {
        return region;
    }

    public String getIp() {
        return ip;
    }

    public ArrayList<Object> getNads() {
        return nads;
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

    public static class Vip {
        private String viplevel;
        private int count;
        private String time;
        private boolean isout;
        private String viptag;
        private String ctime;

        public String getViplevel() {
            return viplevel;
        }

        public int getCount() {
            return count;
        }

        public String getTime() {
            return time;
        }

        public boolean isIsout() {
            return isout;
        }

        public String getViptag() {
            return viptag;
        }

        public String getCtime() {
            return ctime;
        }
    }

    public static class Swt {
        private String name;
        private String code;
        private int val1;
        private String val2;

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }

        public int getVal1() {
            return val1;
        }

        public String getVal2() {
            return val2;
        }
    }

    public static class Good implements Comparable<Good> {
        private String value;
        private String name;
        private double price;
        private int gid;
        private String remark;
        private double original;
        private String bg1;
        private String bg2;
        private double xwprice;
        private String payway;
        private String code;

        private boolean isHot;

        private boolean isSelected;

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public int getGid() {
            return gid;
        }

        public String getRemark() {
            return remark;
        }

        public double getOriginal() {
            return original;
        }

        public String getBg1() {
            return bg1;
        }

        public String getBg2() {
            return bg2;
        }

        public double getXwprice() {
            return xwprice;
        }

        public String getPayway() {
            return payway;
        }

        public String getCode() {
            return code;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public boolean isHot() {
            return isHot;
        }

        public void setHot(boolean hot) {
            isHot = hot;
        }

        @Override
        public int compareTo(Good o) {
            return Double.compare(o.price, this.price);
        }
    }

    public static class Contract {
        private String txt;
        // 客服QQ
        private String num;

        public String getTxt() {
            return txt;
        }

        public String getNum() {
            return num;
        }
    }

    public static class Config {
        private String wxid;
        private String wxsecret;

        public String getWxid() {
            return wxid;
        }

        public String getWxsecret() {
            return wxsecret;
        }
    }

}
