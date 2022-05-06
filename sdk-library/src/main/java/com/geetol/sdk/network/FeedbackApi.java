package com.geetol.sdk.network;

import com.geetol.sdk.proguard_data.CommonListResult;
import com.geetol.sdk.proguard_data.CommonResult;
import com.geetol.sdk.proguard_data.FeedbackInfo;
import com.geetol.sdk.proguard_data.FeedbackItem;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * 意见反馈相关接口
 *
 * @author pslilysm
 * @since 1.0.0
 */
public interface FeedbackApi {

    /**
     * 新增反馈接口
     *
     * @param title    标题
     * @param describe 内容
     * @param type     类型
     * @param img_url  所有上传阿里云后的图片链接拼接。
     *                 e.g: http://gt-app-default-file.oss-cn-zhangjiakou.aliyuncs.com/10d5f171a3893cbbff2f5a022e18cc36,http://gt-app-default-file.oss-cn-zhangjiakou.aliyuncs.com/f33619864462f41f7374afb256a094b9,
     */
    @FormUrlEncoded
    @POST("app/sup.add_service_oss")
    Observable<CommonResult<Void>> add(@Field("title") String title,
                                       @Field("describe") String describe,
                                       @Field("type") String type,
                                       @Field("img_url") String img_url);

    /**
     * 获取意见反馈列表接口
     *
     * @param page  页码
     * @param limit 每页的数量
     */
    @FormUrlEncoded
    @POST("app/sup.get_service")
    Observable<CommonListResult<FeedbackItem>> list(@Field("page") int page, @Field("limit") int limit);

    /**
     * 获取意见反馈详情接口
     *
     * @param serviceId 服务ID
     */
    @FormUrlEncoded
    @POST("app/sup.get_service_details_oss")
    Observable<CommonResult<FeedbackInfo>> info(@Field("id") int serviceId);

    /**
     * 新增回复接口
     *
     * @param serviceId 服务ID
     * @param content   内容
     * @param imgUrls   图片的所有链接
     */
    @FormUrlEncoded
    @POST("app/sup.add_reply_oss")
    Observable<CommonResult<Void>> reply(@Field("service_id") int serviceId,
                                         @Field("desc") String content,
                                         @Field("img_url") String imgUrls);

    /**
     * 完成意见反馈接口
     *
     * @param serviceId 服务ID
     */
    @FormUrlEncoded
    @POST("app/sup.end_service")
    Observable<CommonResult<Void>> finish(@Field("id") int serviceId);

}
