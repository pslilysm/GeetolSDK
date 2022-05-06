package com.geetol.sdk.network;

import com.geetol.sdk.proguard_data.CommonResult;
import com.geetol.sdk.proguard_data.UserData;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * 用户相关接口
 *
 * @author pslilysm
 * @since 1.0.0
 */
public interface UserApi {

    /**
     * 发送验证码接口
     *
     * @param tel      手机号码
     * @param tpl      验证码用途 登录使用{@link com.geetol.sdk.constant.AppConfigs#CODE_LOGIN}，注销使用{@link com.geetol.sdk.constant.AppConfigs#CODE_REGISTER}
     * @param sms_sign 传空字符串，不可传空对象
     */
    @FormUrlEncoded
    @POST("app/getvarcode")
    Observable<CommonResult<Void>> sendSmsCode(@Field("tel") String tel,
                                               @Field("tpl") String tpl,
                                               @Field("sms_sign") String sms_sign);

    /**
     * 验证码登录接口
     *
     * @param tel      手机号
     * @param sms_code 验证码
     * @param sms_key  发送验证码接口返回的值 {@link CommonResult#getCode()}
     */
    @FormUrlEncoded
    @POST("app/sms.userlogin")
    Observable<CommonResult<UserData>> smsLogin(@Field("tel") String tel,
                                                @Field("smscode") String sms_code,
                                                @Field("smskey") String sms_key);

    /**
     * 微信登录接口
     * 再请求完微信的https://api.weixin.qq.com/sns/userinfo接口后调用
     *
     * @param open_id  微信接口返回的open_id
     * @param nickname 微信接口返回的nickname
     * @param sex      微信接口返回的sex
     * @param headurl  微信接口返回的headurl
     */
    @FormUrlEncoded
    @POST("app/pub_wechat_login")
    Observable<CommonResult<UserData>> wechatLogin(@Field("open_id") String open_id,
                                                   @Field("nickname") String nickname,
                                                   @Field("sex") String sex,
                                                   @Field("headurl") String headurl);

    /**
     * 手机号注销接口
     *
     * @param tel      手机号
     * @param sms_code 验证码
     * @param sms_key  发送验证码接口返回的值 {@link CommonResult#getCode()}
     * @return
     */
    @FormUrlEncoded
    @POST("app/sms.userlogout")
    Observable<CommonResult<Void>> phoneLogoff(@Field("tel") String tel,
                                               @Field("smscode") String sms_code,
                                               @Field("smskey") String sms_key);

    /**
     * 微信用户注销接口
     *
     * @param open_id 微信的open_id
     */
    @FormUrlEncoded
    @POST("app/user_wechat_logout")
    Observable<CommonResult<Void>> wechatLogoff(@Field("open_id") String open_id);

}
