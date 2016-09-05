package com.goldmf.GMFund.controller;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.annimon.stream.Stream;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.goldmf.GMFund.DummyActivity;
import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.extension.ViewGroupExtension;
import com.goldmf.GMFund.manager.BaseManager;
import com.goldmf.GMFund.manager.common.RedPoint;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.manager.message.SessionManager;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.widget.MainTabView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func0;
import rx.subjects.PublishSubject;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_LoginPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.YELLOW_COLOR;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.extension.ViewGroupExtension.v_forEach;
import static com.goldmf.GMFund.util.UmengUtil.stat_regist_guide_event;

/**
 * Created by yale on 16/2/18.
 */
public class MainActivityV2 extends BaseActivity {

    public static final String KEY_HOME_TAB_INDEX = "home_tab_index";
    public static final String KEY_STOCK_TAB_INDEX = "stock_tab_index";

    public static PublishSubject<Pair<Integer, Integer>> SELECT_HOME_TAB_SUBJECT = PublishSubject.create();
    private PublishSubject<Void> redDotChangedSubject = PublishSubject.create();
    private ImageView mRegistGuideImage;
    private BaseManager.OnKeyListener mRedPointListener = null;
    private int mSelectedTabIndex = 0;
    private int mStockTabIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSelectedTabIndex = 0;
        savedInstanceState = null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main_v2);
        mStockTabIndex = getIntent().getIntExtra(KEY_STOCK_TAB_INDEX, 1);

        setStatusBarBackgroundColor(this, YELLOW_COLOR);
        mRegistGuideImage = v_findView(this, R.id.img_regist_guide);
        v_setClick(mRegistGuideImage, v -> {
            showActivity(this, an_LoginPage());
            stat_regist_guide_event(this, Optional.of(null));
        });

        v_forEach(this, R.id.group_bottom_button, (pos, child) -> child.setOnClickListener(v -> onTabClick(pos)));
        onTabClick(mSelectedTabIndex);

        consumeEvent(Observable.merge(NotificationCenter.loginSubject, NotificationCenter.logoutSubject))
                .setTag("on_login_state_change")
                .onNextFinish(ignored -> {
                    boolean isPlay = !MineManager.getInstance().isLoginOK();
                    updateGuideImage(isPlay);
                })
                .done();

        consumeEvent(SELECT_HOME_TAB_SUBJECT)
                .setTag("on_select_home_tab")
                .onNextFinish(pair -> {
                    mStockTabIndex = pair.second;
                    onTabClick(pair.first);
                })
                .done();


        NotificationCenter.onEnterMainActivitySubject.onNext(null);

        safeCall(() -> {
            Uri uri = getIntent().getData();
            if (uri != null) {
                CMDParser.parse(uri.getQuery()).call(this);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        consumeEvent(redDotChangedSubject)
                .setTag("refresh_red_dot")
                .addToVisible()
                .onNextFinish(ignored -> refreshRedDot())
                .done();

        Observable<Void> dotChangeObservable = Observable.merge(NotificationCenter.loginSubject,
                NotificationCenter.logoutSubject,
                NotificationCenter.redDotCountChangedSubject)
                .debounce(300, TimeUnit.MILLISECONDS);
        consumeEvent(dotChangeObservable)
                .setTag("push_red_dot_changed_event")
                .addToVisible()
                .onNextFinish(ignored -> redDotChangedSubject.onNext(null))
                .done();
        addRedPointListener((key, object) -> redDotChangedSubject.onNext(null));
        refreshRedDot();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGuideImage(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateGuideImage(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeRedPointListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.SHARE_INSTANCE.mHasLaunchSplash = false;
        mSelectedTabIndex = 0;
        removeRedPointListener();
        Fresco.getImagePipeline().clearMemoryCaches();
        MyApplication.SHARE_INSTANCE.mHasRequestFreshCommon = false;
    }

    @Override
    public void finish() {
        startActivity(new Intent(this, DummyActivity.class));
        super.finish();
    }

    public Optional<Integer> getSelectedTabIndex() {
        return Optional.of(mSelectedTabIndex);
    }

    private void addRedPointListener(BaseManager.OnKeyListener listener) {
        if (mRedPointListener != null) {
            SessionManager.getInstance().removeListener(mRedPointListener);
        }
        if (listener != null) {
            SessionManager.getInstance().addListener(SessionManager.Key_SessionManager_RedPoint, listener);
        }
        mRedPointListener = listener;
    }

    private void removeRedPointListener() {
        if (mRedPointListener != null) {
            SessionManager.getInstance().removeListener(mRedPointListener);
            mRedPointListener = null;
        }
    }

    private void refreshRedDot() {
        RedPoint dotOfMine = FortuneManager.getInstance().bountyRedPoint;

        Optional.of(getTabView(3)).apply(it -> it.setRedDotViewVisibility(SessionManager.getInstance().getRedPoint() > 0 ? View.VISIBLE : View.GONE));
        Optional.of(getTabView(4)).apply(it -> it.setRedDotViewVisibility(dotOfMine.number > 0 ? View.VISIBLE : View.GONE));
    }

    public void updateGuideImage(boolean play) {
        if (!MineManager.getInstance().getmMe().isLoginOk()) {
            Drawable drawable = mRegistGuideImage.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                if (play) {
                    v_setVisible(mRegistGuideImage);
                    ((AnimationDrawable) drawable).start();
                } else {
                    v_setGone(mRegistGuideImage);
                    ((AnimationDrawable) drawable).stop();
                }
            } else {
                v_setGone(mRegistGuideImage);
            }
        } else {
            v_setGone(mRegistGuideImage);
            Drawable drawable = mRegistGuideImage.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                ((AnimationDrawable) drawable).stop();
            }
        }
    }

    private MainTabView getTabView(int index) {
        ViewGroup bottomBar = (ViewGroup) findViewById(R.id.group_bottom_button);
        assert bottomBar != null;
        if (index < 0 || index > bottomBar.getChildCount()) {
            return null;
        } else {
            return (MainTabView) bottomBar.getChildAt(index);
        }
    }

    private interface CreateFragmentFunc extends Func0<Fragment> {
    }

    Integer[] sNeedToCheckLoginStateIdxList = {3};

    Class[] sClazzList = {
            MainFragments.ADHomeFragment.class,
            MainFragments.StockHomeFragment.class,
            MainFragments.InvestHomeFragment.class,
            MainFragments.ConversationListFragment.class,
            MainFragments.MineFragment.class
    };
    CreateFragmentFunc[] sActionList = {
            MainFragments.ADHomeFragment::new,
            MainFragments.StockHomeFragment::new,
            MainFragments.InvestHomeFragment::new,
            MainFragments.ConversationListFragment::new,
            MainFragments.MineFragment::new
    };

    @SuppressWarnings("NumberEquality")
    private void onTabClick(int index) {
        boolean needLoginToAccess = Stream.of(sNeedToCheckLoginStateIdxList).anyMatch(it -> it == index);
        boolean isLogin = MineManager.getInstance().isLoginOK();
        if (!needLoginToAccess || isLogin) {
            mSelectedTabIndex = index;
        }

        v_forEach(this, R.id.group_bottom_button, (pos, child) -> {
            if (index == pos && !child.isSelected()) {
                if (needLoginToAccess && !isLogin) {
                    showActivity(this, an_LoginPage());
                } else {
                    replaceCurrentFragment(index, sClazzList[index], sActionList[index]);
                    child.setSelected(true);
                }
            } else if (index != pos) {
                if (child.isSelected()) {
                    if (!needLoginToAccess || isLogin) {
                        child.setSelected(false);
                    }
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private Optional<Fragment> mCurrentFragment = Optional.empty();

    @SuppressWarnings("Convert2MethodRef")
    private void replaceCurrentFragment(int index, Class clazz, CreateFragmentFunc func) {
        FragmentManager fm = getSupportFragmentManager();

        Fragment cacheFragment = fm.findFragmentByTag(clazz.getSimpleName());
        Fragment newFragment = cacheFragment == null ? func.call() : null;
        FragmentTransaction transaction = fm.beginTransaction();

        if (cacheFragment != null) {
            transaction.show(cacheFragment);
            cacheFragment.setUserVisibleHint(true);
        } else {
            if (index == 1) {
                MainFragments.StockHomeFragment fragment = ((MainFragments.StockHomeFragment) newFragment).init(mStockTabIndex);
                transaction.add(R.id.container_fragment, fragment, clazz.getSimpleName());
            } else {
                transaction.add(R.id.container_fragment, newFragment, clazz.getSimpleName());
            }
        }

        //        if (cacheFragment != null) {
        //            transaction.show(cacheFragment);
        //            cacheFragment.setUserVisibleHint(true);
        //        } else {
        //            transaction.add(R.id.container_fragment, newFragment, clazz.getSimpleName());
        //      }
        //
        if (mCurrentFragment.isPresent()) {
            mCurrentFragment.get().setUserVisibleHint(false);
            transaction.hide(mCurrentFragment.get());
        }

        transaction.commit();

        mCurrentFragment = opt(cacheFragment != null ? cacheFragment : newFragment);
    }
}
