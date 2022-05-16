package com.geetol.sdk.base;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.geetol.sdk.GeetolApplication;
import com.geetol.sdk.constant.EventCodes;
import com.geetol.sdk.manager.AppConfigManager;
import com.geetol.sdk.network.GTRetrofitClient;
import com.geetol.sdk.network.GoodsApi;
import com.geetol.sdk.proguard_data.AlipayOrder;
import com.geetol.sdk.proguard_data.UserConfig;
import com.geetol.sdk.proguard_data.WechatOrder;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import pers.cxd.corelibrary.EventHandler;
import pers.cxd.corelibrary.base.BaseAct;
import pers.cxd.corelibrary.util.ExceptionUtil;
import pers.cxd.corelibrary.util.ExecutorsHolder;
import pers.cxd.corelibrary.util.ThreadUtil;
import pers.cxd.corelibrary.util.ToastUtil;
import pers.cxd.corelibrary.util.reflection.ReflectionUtil;
import pers.cxd.rxlibrary.BaseHttpObserverImpl;
import pers.cxd.rxlibrary.RxUtil;

/**
 * 一个基本的会员页，封装好了业务逻辑
 *
 * @author pslilysm
 * @since 1.0.6
 */
public abstract class BaseVipActivity extends BaseAct implements IAppView, EventHandler.EventCallback {

    protected final Handler mH = new Handler(Looper.getMainLooper());
    protected final CompositeDisposable mSub = new CompositeDisposable();
    /**
     * 微信支付方式
     */
    protected final int WECHAT = 1;
    /**
     * 支付宝支付方式
     */
    protected final int ALIPAY = 2;
    /**
     * 当前的支付方式，WECHAT or ALIPAY，默认支付宝
     */
    protected int mPayWay = ALIPAY;

    @Override
    public void setUp(@Nullable Bundle savedInstanceState) {
        EventHandler.getDefault().registerEvent(EventCodes.WECHAT_PAY_SUCCESS, this);
        EventHandler.getDefault().registerEvent(EventCodes.WECHAT_PAY_FAILURE, this);
        UserConfig userConfig = AppConfigManager.getInstance().getUserConfig();
        List<UserConfig.Good> goods = Collections.emptyList();
        if (userConfig != null) {
            goods = new ArrayList<>(userConfig.getGds());
            Collections.sort(goods);
        }
        vipSetup(savedInstanceState, goods);
    }

    /**
     * 会员页UI初始化
     *
     * @param savedInstanceState 销毁时保存好的数据
     */
    protected abstract void vipSetup(@Nullable Bundle savedInstanceState, List<UserConfig.Good> goods);

    /**
     * 初始化用户UI，包括头像，名字，会员状态等
     */
    protected abstract void initUserView();

    /**
     * @return 是否一定要登录后才能充值的开关代码位
     */
    protected abstract String getMustLoginBeforePaySwtCode();

    /**
     * 跳转到登录页
     */
    protected abstract void startToLoginActivity();

    /**
     * 请求微信支付
     *
     * @param gid 商品ID
     */
    protected void payWithWechat(int gid) {
        if (!AppConfigManager.getInstance().isLogin()
                && AppConfigManager.getInstance().isSwtOpen(getMustLoginBeforePaySwtCode())) {
            ToastUtil.showShort("请先登录");
            startToLoginActivity();
            return;
        }
        showLoadingView("创建订单中...");
        Map<String, String> param = new HashMap<>();
        param.put("type", "1");
        param.put("pid", String.valueOf(gid));
        param.put("amount", "0");
        param.put("pway", String.valueOf(mPayWay));
        RxUtil.execute(GTRetrofitClient.getInstance().getApi(GoodsApi.class).newWechatOrder(param),
                new BaseHttpObserverImpl<WechatOrder>(mSub) {
                    @Override
                    public void onNext(@io.reactivex.rxjava3.annotations.NonNull WechatOrder wechatOrder) {
                        if (!isDestroyed()) {
                            if (wechatOrder.isIssucc()) {
                                IWXAPI api = GeetolApplication.getWechatApi();
                                api.registerApp(wechatOrder.getAppid());
                                PayReq req = new PayReq();//PayReq就是订单信息对象//给req对象赋值
                                req.appId = wechatOrder.getAppid();//APPID
                                req.partnerId = wechatOrder.getPartnerId();//商户号
                                req.prepayId = wechatOrder.getPrepayid();  //预订单id
                                req.nonceStr = wechatOrder.getNonce_str();//随机数
                                req.timeStamp = wechatOrder.getTimestramp();//时间戳
                                req.packageValue = wechatOrder.getPackage_str();//固定值Sign=WXPay
                                req.sign = wechatOrder.getSign();//签名
                                api.sendReq(req);//将订单信息对象发送给微信服务器，即发送支付请求
                            } else {
                                hideLoadingView();
                                ToastUtil.showShort(wechatOrder.getMsg());
                            }
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        super.onError(e);
                        hideLoadingView();
                        ToastUtil.showShort("创建订单失败，请稍后再试");
                    }
                }, RxUtil.Transformers.IOToMain());
    }

    /**
     * 请求支付宝支付
     *
     * @param gid 商品ID
     */
    protected void payWithAlipay(int gid) {
        if (!AppConfigManager.getInstance().isLogin()
                && AppConfigManager.getInstance().isSwtOpen(getMustLoginBeforePaySwtCode())) {
            ToastUtil.showShort("请先登录");
            startToLoginActivity();
            return;
        }
        showLoadingView("创建订单中...");
        Map<String, String> param = new HashMap<>();
        param.put("type", "1");
        param.put("pid", String.valueOf(gid));
        param.put("amount", "0");
        param.put("pway", String.valueOf(mPayWay));
        RxUtil.execute(GTRetrofitClient.getInstance().getApi(GoodsApi.class).newAlipayOrder(param),
                new BaseHttpObserverImpl<AlipayOrder>(mSub) {
                    @Override
                    public void onNext(@io.reactivex.rxjava3.annotations.NonNull AlipayOrder alipayOrder) {
                        if (!isDestroyed()) {
                            if (alipayOrder.isIssucc()) {
                                Object alipay;
                                Map<String, String> map;
                                try {
                                    alipay = ReflectionUtil.newInstance(getClassLoader().loadClass("com.alipay.sdk.app.PayTask"),
                                            Activity.class, BaseVipActivity.this);
                                    map = ReflectionUtil.invokeMethod(alipay, "payV2",
                                            String.class, boolean.class,
                                            alipayOrder.getPackage_str(), true);
                                } catch (ReflectiveOperationException e) {
                                    throw ExceptionUtil.rethrow(e);
                                }
                                String resultStatus = map.get("resultStatus");
                                if (resultStatus != null) {
                                    if (!TextUtils.equals(resultStatus, "9000")) {
                                        mH.post(BaseVipActivity.this::hideLoadingView);
                                    }
                                    switch (resultStatus) {
                                        case "9000":
                                            updateUserConfigAfterPaid(0);
                                            break;
                                        case "8000":
                                        case "6004":
                                            ToastUtil.showShort("正在处理中");
                                            break;
                                        case "4000":
                                            ToastUtil.showShort("订单支付失败");
                                            break;
                                        case "5000":
                                            ToastUtil.showShort("重复请求");
                                            break;
                                        case "6001":
                                            ToastUtil.showShort("已取消支付");
                                            break;
                                        case "6002":
                                            ToastUtil.showShort("网络连接出错");
                                            break;
                                        default:
                                            ToastUtil.showShort("支付失败");
                                            break;
                                    }
                                }
                            } else {
                                mH.post(BaseVipActivity.this::hideLoadingView);
                                ToastUtil.showShort(alipayOrder.getMsg());
                            }
                        }
                    }


                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        super.onError(e);
                        if (!isDestroyed()) {
                            mH.post(BaseVipActivity.this::hideLoadingView);
                            ToastUtil.showShort("创建订单失败，请稍后再试");
                        }
                    }
                }, RxUtil.Transformers.IO());
    }

    @Override
    public void handleEvent(@NonNull Message msg) {
        if (msg.what == EventCodes.WECHAT_PAY_SUCCESS) {
            ExecutorsHolder.io().execute(() -> updateUserConfigAfterPaid(0));
        } else if (msg.what == EventCodes.WECHAT_PAY_FAILURE) {
            hideLoadingView();
        }
    }

    /**
     * 支付成功后调用去同步后台数据
     *
     * @param retryTimes 重试次数，大于三次不再重试，第一次调用请传0
     */
    private void updateUserConfigAfterPaid(final int retryTimes) {
        ThreadUtil.throwIfMainThread();
        if (retryTimes > 3) {
            mH.post(this::hideLoadingView);
            ToastUtil.showShort("同步支付结果失败，如果已支付，请重启APP或联系客服");
            return;
        }
        mH.post(() -> showLoadingView("同步支付结果中..."));
        AppConfigManager.getInstance().refreshUserConfigBlocked();
        if (!isDestroyed()) {
            if (!AppConfigManager.getInstance().isVip()) {
                // 还不是VIP，可能服务端数据没更新，等待一秒更新一下用户配置
                ExecutorsHolder.io().schedule(() ->
                        updateUserConfigAfterPaid(retryTimes + 1), 1, TimeUnit.SECONDS);
            } else {
                mH.post(this::hideLoadingView);
                EventHandler.getDefault().sendEventOpted(Message.obtain(null, EventCodes.USER_CHARGED_VIP));
                setResult(RESULT_OK);
                mH.post(this::initUserView);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSub.clear();
        EventHandler.getDefault().unregisterAllEvent(this);
        mH.removeCallbacksAndMessages(null);
    }
}
