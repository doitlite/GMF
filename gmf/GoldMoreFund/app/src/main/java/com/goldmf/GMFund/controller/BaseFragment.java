package com.goldmf.GMFund.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.manager.dev.AccessLogFormatter;
import com.goldmf.GMFund.util.UIControllerNameMapper;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import rx.functions.Action0;
import yale.extension.rx.RXFragment;

import static com.goldmf.GMFund.extension.ViewExtension.v_setText;

/**
 * Created by yale on 15/7/20.
 */
public class BaseFragment extends RXFragment {

    protected boolean mForceFinishOnGoBack = false;
    protected boolean mIsOperation = false;
    private Serializable mExtraData;
    private Handler mHandler = new Handler();
    private Thread mUIThread;

    public BaseFragment init(Bundle bundle) {

        if (bundle != null) {
            Bundle arguments = new Bundle(bundle);
            setArguments(arguments);
        } else {
            setArguments(new Bundle());
        }
        return this;
    }


    public void setExtraData(Serializable data) {
        mExtraData = data;
    }

    public Serializable getExtraData() {
        return mExtraData;
    }

    /**
     * -1 for unset, 0 for false, 1 for true
     */
    protected int mIsHostActivityTraceLifeRecycle = -1;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (logLifeCycleEvent()) {
            Logger.i(AccessLogFormatter.logForOpenUIController(getActivity(), this));
        }
        mUIThread = Thread.currentThread();
        mHandler = new Handler();
        if (savedInstanceState != null) {
            mExtraData = savedInstanceState.getSerializable("gmf_extra_data");
        }

        view.setClickable(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("gmf_extra_data", mExtraData);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (logLifeCycleEvent()) {
            Logger.i(AccessLogFormatter.logForCloseUIController(getActivity(), this));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isDelegateLifeCycleEventToSetUserVisible() && getView() != null && needToSetUserVisibleHint()) {
            setUserVisibleHint(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isDelegateLifeCycleEventToSetUserVisible() && getView() != null && needToSetUserVisibleHint()) {
            setUserVisibleHint(false);
        }
    }

    private boolean needToSetUserVisibleHint() {
        Activity activity = getActivity();
        if (activity instanceof FragmentStackActivity) {
            FragmentStackActivity cast = (FragmentStackActivity) activity;
            return !cast.mFragmentStack.isEmpty() && cast.mFragmentStack.peek() == this;
        } else {
            return true;
        }
    }

    private static final int PAGE_STATE_VISIBLE = 1;
    private static final int PAGE_STATE_INVISIBLE = 2;

    private int mPageState = -1;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isTraceLifeCycle()) {
            if (isVisibleToUser && mPageState != PAGE_STATE_VISIBLE) {
                mPageState = PAGE_STATE_VISIBLE;
                MobclickAgent.onPageStart(UIControllerNameMapper.getName(getClass(), "UnknownPage"));
            } else {
                if (mPageState == PAGE_STATE_VISIBLE) {
                    MobclickAgent.onPageEnd(UIControllerNameMapper.getName(getClass(), "UnknownPage"));
                    mPageState = PAGE_STATE_INVISIBLE;
                }
            }
        }
    }

    public Window getWindow() {
        return getActivity() == null ? null : getActivity().getWindow();
    }

    public void setSoftInputMode(int mode) {
        Window window = getWindow();
        if (window != null) {
            window.setSoftInputMode(mode);
        }
    }

    protected boolean hasSharedElement() {
        return false;
    }

    protected boolean logLifeCycleEvent() {
        return true;
    }

    protected boolean isDelegateLifeCycleEventToSetUserVisible() {
        return true;
    }

    protected boolean onInterceptGoBack() {
        if (mIsOperation) {
            showExitDialog(new WeakReference<>(this));
            return true;
        }
        return false;
    }

    private static void showExitDialog(WeakReference<BaseFragment> fragmentRef) {
        GMFDialog.Builder builder = new GMFDialog.Builder(fragmentRef.get().getActivity());
        builder.setTitle("提示");
        builder.setMessage("申请已提交，后台正在处理中，是否关闭此页面？");
        builder.setPositiveButton("确认关闭", (dialog, which) -> {
            dialog.dismiss();
            if (fragmentRef.get() != null && fragmentRef.get().getView() != null) {
                BaseFragment fragment = fragmentRef.get();
                fragment.mIsOperation = false;
                FragmentStackActivity.goBack(fragment);
            }
        });
        builder.setNegativeButton("继续等待");
        builder.create().show();
    }

    protected boolean onInterceptKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    protected boolean onInterceptActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }

    protected void runOnUIThread(Action0 runnable) {
        if (Thread.currentThread() == mUIThread) {
            runnable.call();
        } else {
            mHandler.post(runnable::call);
        }
    }

    protected void runOnUIThreadDelayed(Action0 runnable, long delayTimeInMills) {
        mHandler.postDelayed(runnable::call, delayTimeInMills);
    }

    protected void updateTitle(CharSequence text) {
        v_setText(getView(), R.id.toolbarTitle, text);
    }

    protected boolean isTraceLifeCycle() {
        return !isHostActivityTraceLifeRecycle();
    }

//    @Deprecated
//    protected void addSubscription(Subscription subscription) {
//        addSubscriptionToMain(null, subscription);
//    }

    @SuppressWarnings("PointlessBooleanExpression")
    protected final boolean isHostActivityTraceLifeRecycle() {
        if (mIsHostActivityTraceLifeRecycle == -1) {
            FragmentActivity host = getActivity();
            boolean ret = host != null && host instanceof BaseActivity && ((BaseActivity) host).isTracePageLifeRecycle();
            mIsHostActivityTraceLifeRecycle = (ret == true) ? 1 : 0;
            return ret;
        }
        return mIsHostActivityTraceLifeRecycle == 1;
    }
}
