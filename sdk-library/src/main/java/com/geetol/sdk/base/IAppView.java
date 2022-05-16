package com.geetol.sdk.base;

import pers.cxd.corelibrary.base.BaseView;
import pers.cxd.corelibrary.base.UiComponent;

/**
 * 基本的应用UI接口，包含展示弹窗，隐藏弹窗，设置弹窗文字等接口
 *
 * @author pslilysm
 * @since 1.0.4
 */
public interface IAppView extends UiComponent, BaseView {

    /**
     * 等同于调用 {@code showLoadingView(s, true)}
     *
     * @param s 等待文案
     */
    default void showLoadingView(CharSequence s) {
        showLoadingView(s, true);
    }

    /**
     * 显示等待UI
     *
     * @param s           等待文案
     * @param cancellable 是否可取消
     */
    void showLoadingView(CharSequence s, boolean cancellable);

    /**
     * 隐藏等待UI
     */
    void hideLoadingView();

    /**
     * 设置等待UI文案
     *
     * @param s 等待文案
     */
    void setLoadingText(CharSequence s);

    /**
     * 销毁当前的UI组件，默认啥也不干
     */
    default void finish() {
    }

}
