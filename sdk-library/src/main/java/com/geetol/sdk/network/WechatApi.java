package com.geetol.sdk.network;

import com.geetol.sdk.proguard_data.WechatUser;
import com.google.gson.JsonObject;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * 微信后台接口
 *
 * @author pslilysm
 * @since 1.0.0
 */
public interface WechatApi {

    /**
     * 获取微信用户的AccessToken接口
     *
     * @param url https://api.weixin.qq.com/sns/oauth2/access_token?开头，剩下的字段需自己拼接，可自行查阅微信开发者平台文档
     */
    @GET()
    Observable<JsonObject> getWechatAccessToken(@Url String url);

    /**
     * 获取微信用户信息的接口
     *
     * @param url https://api.weixin.qq.com/sns/userinfo?开头，剩下的字段需自己拼接，可自行查阅微信开发者平台文档
     */
    @GET()
    Observable<WechatUser> getWechatUserInfo(@Url String url);

}
