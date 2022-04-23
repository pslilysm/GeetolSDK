package com.geetol.sdk.constant;

public interface MMKVKeys {

    /**
     * 网络请求用的设备ID
     */
    String DEVICE_ID = "device_id";

    /**
     * 是否调用成功了设备注册接口
     */
    String DEVICE_REGISTERED = "device_registered";

    /**
     * 阿里云配置，用来将音频上传云课堂
     */
    String ALI_OSS_CONFIG = "ali_oss_config";

    /**
     * 用户配置
     */
    String USER_CONFIG = "app_switch_config";

    /**
     * 用户登录数据
     */
    String USER_DATA = "user_data";

    /**
     * 微信登录后保存的OPEN_ID
     */
    String WECHAT_OPEN_ID = "wechat_open_id";

    /**
     * 用户是否同意了用户协议配置
     */
    String USER_AGREED_PROTOCOL = "user_agreed_protocol";

}
