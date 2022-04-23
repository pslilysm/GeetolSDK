package com.geetol.sdk.network;

import com.geetol.sdk.proguard_data.AppUpdateBean;
import com.geetol.sdk.proguard_data.CommonResult;
import com.geetol.sdk.proguard_data.UserConfig;

import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * 应用基础接口
 *
 * @author pslilysm
 * @since 1.0.0
 */
public interface AppApi {

    /**
     * 注册设备接口
     */
    @FormUrlEncoded
    @POST("app/reg")
    Observable<CommonResult<Void>> registerDevice(@FieldMap Map<String, String> fieldMap);

    /**
     * 获取阿里云配置接口
     */
    @FormUrlEncoded
    @POST("app/get_ali_oss")
    Observable<CommonResult<String>> getAliOss(@FieldMap Map<String, String> fieldMap);

    /**
     * 获取用户基本配置接口
     */
    @FormUrlEncoded
    @POST("app/update")
    Observable<UserConfig> getUserConfig(@FieldMap Map<String, String> fieldMap);

    /**
     * 检测应用更新接口
     *
     * @param fieldMap 传入一个空内容的Map
     */
    @FormUrlEncoded
    @POST("app/getnew")
    Observable<AppUpdateBean> checkUpdate(@FieldMap Map<String, String> fieldMap);

}
