package com.geetol.sdk.base;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.geetol.sdk.GTApplication;
import com.geetol.sdk.constant.MMKVKeys;
import com.geetol.sdk.manager.AdManager;
import com.geetol.sdk.manager.AppConfigManager;

import pers.cxd.corelibrary.base.BaseAct;
import pers.cxd.corelibrary.util.ExceptionUtil;
import pers.cxd.corelibrary.util.MMKVUtil;
import pers.cxd.corelibrary.util.ScreenUtil;

/**
 * 一个基本的启动页，封装好了业务逻辑
 *
 * @author pslilysm
 * @since 1.0.4
 */
@SuppressLint("CustomSplashScreen")
public abstract class BaseSplashActivity extends BaseAct implements
        IAppView,
        AppConfigManager.InitCallback,
        AdManager.SplashCallback {

    private View mAdView;

    @Override
    public void setUp(@Nullable Bundle savedInstanceState) {
        if (MMKVUtil.decode(MMKVKeys.USER_AGREED_PROTOCOL, false)) {
            // show ad then goto main activity
            showLoadingView("应用数据加载中...", false);
            AppConfigManager.getInstance().initAsync(this);
        } else {
            // show protocol dialog
            showProtocolDialog();
        }
    }

    /**
     * 显示用户协议弹窗
     */
    public abstract void showProtocolDialog();

    /**
     * @return 广告开关代码位，值1的值=1则为开，其他为关
     */
    public abstract String getAdSwtCode();

    /**
     * @return 广告配置代码位，值1为应用ID，值2为代码位ID
     */
    public abstract String getAdValueSwtCode();

    /**
     * 跳转到主页方法
     */
    protected abstract void gotoMain();

    /**
     * 用户同意了用户协议后调用这个方法
     */
    protected void onUserAgreedProtocol() {
        showLoadingView("应用数据加载中...", false);
        MMKVUtil.encode(MMKVKeys.USER_AGREED_PROTOCOL, true);
        GTApplication.getInstance().init();
        AppConfigManager.getInstance().initAsync(BaseSplashActivity.this);
    }

    @Override
    public void initialized() {
        int adAppId = AppConfigManager.getInstance().getSwtVal1(getAdValueSwtCode(), 0);
        String adCodeId = AppConfigManager.getInstance().getSwtVal2(getAdValueSwtCode(), "");
        if (AppConfigManager.getInstance().isSwtOpen(getAdSwtCode())
                && adAppId != 0
                && !TextUtils.isEmpty(adCodeId)) {
            try {
                AdManager.getInstance().loadSplashAd(String.valueOf(adAppId), adCodeId, ScreenUtil.getRealWidth(),
                        ScreenUtil.getRealHeight() - ScreenUtil.getNavBarHeight(), this);
            } catch (ReflectiveOperationException e) {
                throw ExceptionUtil.rethrow(e);
            }
        } else {
            gotoMain();
        }
    }

    @Override
    public void onSplashAdLoaded(View view) {
        hideLoadingView();
        mAdView = view;
        ((ViewGroup) getWindow().getDecorView()).addView(mAdView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void onFinish() {
        gotoMain();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdView != null) {
            ((ViewGroup) getWindow().getDecorView()).removeView(mAdView);
            mAdView = null;
        }
    }
}
