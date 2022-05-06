package com.geetol.sdk.wechatsdk;

import android.os.Message;
import android.text.TextUtils;

import com.geetol.sdk.constant.EventCodes;
import com.geetol.sdk.constant.MMKVKeys;
import com.geetol.sdk.manager.AppConfigManager;
import com.geetol.sdk.network.GTRetrofitClient;
import com.geetol.sdk.network.UserApi;
import com.geetol.sdk.network.WechatApi;
import com.geetol.sdk.proguard_data.CommonResult;
import com.geetol.sdk.proguard_data.UserConfig;
import com.geetol.sdk.proguard_data.UserData;
import com.geetol.sdk.proguard_data.WechatUser;
import com.google.gson.JsonObject;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import io.reactivex.rxjava3.annotations.NonNull;
import pers.cxd.corelibrary.EventHandler;
import pers.cxd.corelibrary.SingletonFactory;
import pers.cxd.corelibrary.util.MMKVUtil;
import pers.cxd.corelibrary.util.ToastUtil;
import pers.cxd.rxlibrary.BaseHttpObserverImpl;
import pers.cxd.rxlibrary.RxUtil;

/**
 * 默认的微信业务处理器
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class DefaultWechatEventHandler {

    /**
     * 登录处理器
     */
    private final IWXAPIEventHandler mLoginHandler = new IWXAPIEventHandler() {
        @Override
        public void onReq(BaseReq baseReq) {

        }

        @Override
        public void onResp(BaseResp baseResp) {
            SendAuth.Resp resp = (SendAuth.Resp) baseResp;
            UserConfig userConfig = AppConfigManager.getInstance().getUserConfig();
            if (baseResp.errCode == BaseResp.ErrCode.ERR_OK && userConfig != null) {
                getAccessToken(resp.code, userConfig);
            } else {
                EventHandler.getDefault().sendMessage(Message.obtain(null, EventCodes.WECHAT_LOGIN_FAILURE, baseResp.errStr));
            }
        }
    };

    /**
     * 支付处理器
     */
    private final IWXAPIEventHandler mPayHandler = new IWXAPIEventHandler() {
        @Override
        public void onReq(BaseReq baseReq) {

        }

        @Override
        public void onResp(BaseResp baseResp) {
            if (baseResp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
                // 支付回调
                int errCord = baseResp.errCode;
                if (errCord == BaseResp.ErrCode.ERR_OK) {
                    EventHandler.getDefault().sendMessage(Message.obtain(null, EventCodes.WECHAT_PAY_SUCCESS));
                } else {
                    EventHandler.getDefault().sendMessage(Message.obtain(null, EventCodes.WECHAT_PAY_FAILURE, baseResp.errStr));
                }
            }
        }
    };

    private DefaultWechatEventHandler() {
    }

    public static DefaultWechatEventHandler getInstance() {
        return SingletonFactory.findOrCreate(DefaultWechatEventHandler.class);
    }

    /**
     * 获取access_token，之后再去请求用户的信息
     *
     * @param code 用户或取access_token的code，仅在ErrCode为0时有效
     */
    private static void getAccessToken(String code, UserConfig userConfig) {
        String loginUrl = "https://api.weixin.qq.com/sns/oauth2/access_token" +
                "?appid=" +
                userConfig.getConfig().getWxid() +
                "&secret=" +
                userConfig.getConfig().getWxsecret() +
                "&code=" +
                code +
                "&grant_type=authorization_code";
        RxUtil.execute(GTRetrofitClient.getInstance().getApi(WechatApi.class).getWechatAccessToken(loginUrl),
                new BaseHttpObserverImpl<JsonObject>(null) {
                    @Override
                    public void onNext(@NonNull JsonObject jsonObject) {
                        String access_token = jsonObject.get("access_token").getAsString();
                        String openid = jsonObject.get("openid").getAsString();
                        if (!TextUtils.isEmpty(access_token) && !TextUtils.isEmpty(openid)) {
                            getUserInfo(access_token, openid);
                        }
                    }
                }, RxUtil.Transformers.IO());
    }

    /**
     * 先调用微信的获取用户信息接口，再调用我们自己后台的登录接口
     *
     * @param access {@link #getAccessToken(String, UserConfig)} 获取到的微信access_token
     * @param openid {@link #getAccessToken(String, UserConfig)} 获取到的微信openid
     */
    private static void getUserInfo(String access, String openid) {
        String getUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access + "&openid=" + openid;
        RxUtil.execute(GTRetrofitClient.getInstance().getApi(WechatApi.class).getWechatUserInfo(getUserInfoUrl),
                new BaseHttpObserverImpl<WechatUser>(null) {
                    @Override
                    public void onNext(@NonNull WechatUser wechatUser) {
                        MMKVUtil.encode(MMKVKeys.WECHAT_OPEN_ID, wechatUser.getOpenid());
                        RxUtil.execute(GTRetrofitClient.getInstance().getApi(UserApi.class).wechatLogin(
                                wechatUser.getOpenid(),
                                wechatUser.getNickname(),
                                String.valueOf(wechatUser.getSex()),
                                wechatUser.getHeadimgurl()),
                                new BaseHttpObserverImpl<CommonResult<UserData>>(null) {
                                    @Override
                                    public void onNext(@NonNull CommonResult<UserData> result) {
                                        if (result.isIssucc()) {
                                            AppConfigManager.getInstance().login(result.getData());
                                            EventHandler.getDefault().sendMessage(Message.obtain(null, EventCodes.WECHAT_LOGIN_SUCCESS));
                                        } else {
                                            ToastUtil.showShort(result.getMsg());
                                        }
                                    }
                                }, RxUtil.Transformers.NON());
                    }
                }, RxUtil.Transformers.NON());
    }

    public IWXAPIEventHandler getLoginHandler() {
        return mLoginHandler;
    }

    public IWXAPIEventHandler getPayHandler() {
        return mPayHandler;
    }
}
