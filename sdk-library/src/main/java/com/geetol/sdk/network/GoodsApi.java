package com.geetol.sdk.network;

import com.geetol.sdk.proguard_data.AlipayOrder;
import com.geetol.sdk.proguard_data.WechatOrder;

import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * 会员商品相关接口
 *
 * @author pslilysm
 * @since 1.0.0
 */
public interface GoodsApi {

    @FormUrlEncoded
    @POST("app/order.one")
    Observable<AlipayOrder> newAlipayOrder(@FieldMap Map<String, String> fieldMap);

    @FormUrlEncoded
    @POST("app/order.one")
    Observable<WechatOrder> newWechatOrder(@FieldMap Map<String, String> fieldMap);

}
