package com.goldmf.GMFund.controller;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Window;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.SubscriptionManager.LIFE_PERIOD;
import com.goldmf.GMFund.controller.protocol.RXViewControllerProtocol;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.dev.AccessLogFormatter;
import com.goldmf.GMFund.util.GlobalVariableDic;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import java.util.LinkedList;
import java.util.List;

import rx.Subscription;
import rx.functions.Action0;
import yale.extension.rx.RXActivity;

import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;

/**
 * Created by yale on 15/7/20.
 */
public class BaseActivity extends RXActivity {
    public static final String KEY_TRANSACTION_DIRECTION = "gmf_transaction_direction";
    private Handler mHandler;
    private Thread mUIThread;
    private boolean mOnForeground = false;
    private List<String> mRealtiveObjectIDList = new LinkedList<>();

    public enum TRANSACTION_DIRECTION {
        DEFAULT,
        VERTICAL,
        NONE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        }
        super.onCreate(savedInstanceState);
        MyApplication.setTopActivity(this);
        if (MyApplication.SHARE_INSTANCE != null && !MyApplication.SHARE_INSTANCE.mHasRequestFreshCommon) {
            MyApplication.SHARE_INSTANCE.mHasRequestFreshCommon = true;
            CommonManager.getInstance().freshCommInfo();
        }
        if (logLifeCycleEvent()) {
            Logger.i(AccessLogFormatter.logForOpenUIController(this, null));
        }
        mHandler = new Handler();
        mUIThread = Thread.currentThread();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOnForeground = true;
        MyApplication.setTopActivity(this);
        if (isTracePageLifeRecycle()) {
//            Logger.e("PageOnStartWithPageName:" + getClass().getSimpleName());
            MobclickAgent.onPageStart(getClass().getSimpleName());
        }
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTracePageLifeRecycle()) {
//            Logger.e("PageOnEndWithPageName:" + getClass().getSimpleName());
            MobclickAgent.onPageEnd(getClass().getSimpleName());
        }
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        mOnForeground = false;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (logLifeCycleEvent()) {
            Logger.i(AccessLogFormatter.logForCloseUIController(this, null));
        }
        for (String objectID : mRealtiveObjectIDList) {
            GlobalVariableDic.shareInstance().remove(objectID);
        }
    }

    @Override
    public void finish() {
        super.finish();
        TRANSACTION_DIRECTION direction = (TRANSACTION_DIRECTION) getIntent().getSerializableExtra(KEY_TRANSACTION_DIRECTION);
        if (direction != null) {
            if (direction == TRANSACTION_DIRECTION.DEFAULT) {
                overridePendingTransition(0, android.support.design.R.anim.abc_shrink_fade_out_from_bottom);
            } else if (direction == TRANSACTION_DIRECTION.VERTICAL) {
                overridePendingTransition(0, android.support.design.R.anim.abc_slide_out_bottom);
            } else if (direction == TRANSACTION_DIRECTION.NONE) {
                overridePendingTransition(0, 0);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            supportFinishAfterTransition();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void addRelativeObjectID(String objectID) {
        if (!TextUtils.isEmpty(objectID)) {
            mRealtiveObjectIDList.add(objectID);
        }
    }

    public boolean onForeground() {
        return mOnForeground;
    }

    protected boolean logLifeCycleEvent() {
        return true;
    }

    public void runOnUIThread(Action0 runnable) {
        if (Thread.currentThread() == mUIThread) {
            runnable.call();
        } else {
            mHandler.post(runnable::call);
        }
    }

    public void runOnUIThreadDelayed(Action0 runnable, long delayTimeInMills) {
        mHandler.postDelayed(runnable::call, delayTimeInMills);
    }

    protected final void updateTitle(CharSequence text) {
        v_setText(findToolbar(this), R.id.toolbarTitle, text);
    }

    protected boolean isTracePageLifeRecycle() {
        return true;
    }
}
