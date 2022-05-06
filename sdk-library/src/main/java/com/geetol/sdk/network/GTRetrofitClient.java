package com.geetol.sdk.network;

import android.util.Log;

import com.geetol.sdk.GeetolSDK;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.annotations.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import pers.cxd.corelibrary.SingletonFactory;
import pers.cxd.rxlibrary.RetrofitClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * 一个基础的可发送网络请求的RetrofitClient
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class GTRetrofitClient extends RetrofitClient {

    public static GTRetrofitClient getInstance() {
        return SingletonFactory.findOrCreate(GTRetrofitClient.class);
    }

    @Override
    protected String getBaseUrl() {
        return GeetolSDK.getConfig().getBaseUrl();
    }

    @Override
    protected CallAdapter.Factory[] getCallAdapterFactories() {
        return new CallAdapter.Factory[]{RxJava3CallAdapterFactory.createSynchronous()};
    }

    @Override
    protected Converter.Factory[] getConvertFactories() {
        return new Converter.Factory[]{ScalarsConverterFactory.create(), GsonConverterFactory.create()};
    }

    @Override
    protected OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS);
        if (GeetolSDK.getConfig().debug()) {
            builder.addNetworkInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(@NonNull String s) {
                    Log.d(GeetolSDK.getConfig().getLogTag(), s);
                }
            }).setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        builder.addInterceptor(new EncryptInterceptor());
        return builder.build();
    }
}
