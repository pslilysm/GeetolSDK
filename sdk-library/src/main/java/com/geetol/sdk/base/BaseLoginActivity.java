package com.geetol.sdk.base;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.geetol.sdk.GeetolApplication;
import com.geetol.sdk.constant.AppConfigs;
import com.geetol.sdk.constant.EventCodes;
import com.geetol.sdk.manager.AppConfigManager;
import com.geetol.sdk.network.GTRetrofitClient;
import com.geetol.sdk.network.UserApi;
import com.geetol.sdk.proguard_data.CommonResult;
import com.geetol.sdk.proguard_data.UserData;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import pers.cxd.corelibrary.EventHandler;
import pers.cxd.corelibrary.base.BaseAct;
import pers.cxd.corelibrary.util.ToastUtil;
import pers.cxd.rxlibrary.BaseHttpObserverImpl;
import pers.cxd.rxlibrary.RxUtil;

/**
 * 一个基本的登录页，封装好了业务逻辑
 *
 * @author pslilysm
 * @since 1.0.4
 */
public abstract class BaseLoginActivity extends BaseAct implements IAppView, Handler.Callback, EventHandler.EventCallback {

    private final IWXAPI mWechatApi = GeetolApplication.getWechatApi();
    private final CompositeDisposable mSub = new CompositeDisposable();
    private final Handler mH = new Handler(Looper.getMainLooper(), this);
    private final int COUNTDOWN_MSG = 100;
    private int mCountdown = 60;
    private String mCode = "";

    @Override
    public void setUp(@Nullable Bundle savedInstanceState) {
        EventHandler.getDefault().registerEvent(EventCodes.WECHAT_LOGIN_SUCCESS, this);
        EventHandler.getDefault().registerEvent(EventCodes.WECHAT_LOGIN_FAILURE, this);
        loginSetup(savedInstanceState);
    }

    /**
     * 登录页UI初始化
     *
     * @param savedInstanceState 销毁时保存好的数据
     */
    protected abstract void loginSetup(@Nullable Bundle savedInstanceState);

    /**
     * 更新验证码倒计时UI
     *
     * @param isCounting 是否正在倒计时
     * @param countdown 还剩多少秒
     */
    protected abstract void updateCountdownUi(boolean isCounting, int countdown);

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        if (mCountdown > 0) {
            updateCountdownUi(true, mCountdown);
            mCountdown--;
            mH.sendMessageDelayed(Message.obtain(null, COUNTDOWN_MSG), 1000);
        } else {
            mCountdown = 60;
            updateCountdownUi(false, mCountdown);
        }
        return true;
    }

    @Override
    public void handleEvent(@NonNull Message msg) {
        hideLoadingView();
        if (msg.what == EventCodes.WECHAT_LOGIN_SUCCESS) {
            ToastUtil.showShort("登录成功");
            EventHandler.getDefault().sendMessage(Message.obtain(null, EventCodes.USER_LOGIN));
            setResult(RESULT_OK);
            finish();
        } else if (msg.what == EventCodes.WECHAT_LOGIN_FAILURE) {
            ToastUtil.showShort("登录失败");
        }
    }

    protected void loginByWechat() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
        if (intent == null) {
            ToastUtil.showShort("请先安装微信");
        } else {
            showLoadingView("登录中...");
            final SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = "login";
            mWechatApi.sendReq(req);
        }
    }

    protected void loginByPhone(String phone, String smsCode) {
        showLoadingView("登录中...");
        RxUtil.execute(GTRetrofitClient.getInstance().getApi(UserApi.class).smsLogin(phone, smsCode, mCode),
                new BaseHttpObserverImpl<CommonResult<UserData>>(mSub) {
                    @Override
                    public void onNext(@io.reactivex.rxjava3.annotations.NonNull CommonResult<UserData> result) {
                        // 更新下应用配置
                        if (!isDestroyed()) {
                            if (result.isIssucc()) {
                                AppConfigManager.getInstance().login(result.getData());
                                EventHandler.getDefault().sendMessage(Message.obtain(null, EventCodes.USER_LOGIN));
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                ToastUtil.showShort(result.getMsg());
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (!isDestroyed()) {
                            hideLoadingView();
                        }
                    }
                }, RxUtil.Transformers.IO());
    }

    protected void sendSmsCode(String phone) {
        showLoadingView("验证码发送中...");
        RxUtil.execute(GTRetrofitClient.getInstance().getApi(UserApi.class).sendSmsCode(phone, AppConfigs.CODE_LOGIN, ""),
                new BaseHttpObserverImpl<CommonResult<Void>>(mSub) {
                    @Override
                    public void onNext(@io.reactivex.rxjava3.annotations.NonNull CommonResult<Void> commonResult) {
                        if (!isDestroyed()) {
                            hideLoadingView();
                            if (commonResult.isIssucc()) {
                                ToastUtil.showShort("发送验证码成功");
                                mCode = commonResult.getCode();
                                mH.sendMessage(Message.obtain(null, COUNTDOWN_MSG));
                            } else {
                                ToastUtil.showShort(commonResult.getMsg());
                            }
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        super.onError(e);
                        if (!isDestroyed()) {
                            hideLoadingView();
                            ToastUtil.showShort("发送验证码失败");
                        }
                    }
                }, RxUtil.Transformers.IOToMain());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSub.clear();
        mH.removeCallbacksAndMessages(null);
        EventHandler.getDefault().unregisterAllEvent(this);
    }
}
