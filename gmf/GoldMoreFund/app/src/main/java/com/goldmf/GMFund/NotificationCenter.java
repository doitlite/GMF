package com.goldmf.GMFund;

import android.app.Activity;
import android.util.Pair;

import com.goldmf.GMFund.controller.FragmentStackActivity;
import com.goldmf.GMFund.extension.WebViewExtension;
import com.goldmf.GMFund.manager.dev.AccessLogManager;
import com.goldmf.GMFund.manager.fortune.Coupon;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.AppUtil;
import com.goldmf.GMFund.util.JPushUtil;

import java.util.LinkedHashSet;
import java.util.Set;

import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_LoginPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;

/**
 * Created by yale on 15/8/24.
 */
public class NotificationCenter {
    public static PublishSubject<Void> loginSubject = PublishSubject.create();
    public static PublishSubject<Void> logoutSubject = PublishSubject.create();
    public static PublishSubject<Void> needLoginSubject = PublishSubject.create();
    public static PublishSubject<Void> userInfoChangedSubject = PublishSubject.create();
    public static PublishSubject<Void> cancelLoginSubject = PublishSubject.create();
    public static PublishSubject<Void> holdFundListChangedSubject = PublishSubject.create();
    public static PublishSubject<Integer> investedFundSubject = PublishSubject.create();
    public static PublishSubject<Void> cashChangedSubject = PublishSubject.create();
    public static PublishSubject<User> registrationSuccessSubject = PublishSubject.create();
    public static PublishSubject<Void> focusStockChangedSubject = PublishSubject.create();
    public static PublishSubject<Void> redDotCountChangedSubject = PublishSubject.create();
    public static PublishSubject<String> signUpStockMatchSuccessSubject = PublishSubject.create();
    public static PublishSubject<String> scoreChangedSubject = PublishSubject.create();
    public static PublishSubject<Void> openSimulationStockAccountSubject = PublishSubject.create();
    public static PublishSubject<Void> closeOpenSimulationPageSubject = PublishSubject.create();
    public static PublishSubject<Void> closeAuthenticPageSubject = PublishSubject.create();
    public static PublishSubject<Void> closeEditShippingAddressPageSubject = PublishSubject.create();
    public static PublishSubject<Void> simulationAccountChangedSubject = PublishSubject.create();
    public static PublishSubject<Void> onEnterMainActivitySubject = PublishSubject.create();
    public static PublishSubject<String> onWriteNewArticleSubject = PublishSubject.create();
    public static PublishSubject<Void> onWriteNewCommentSubject = PublishSubject.create();
    public static PublishSubject<Void> onMessageStateChangedSubject = PublishSubject.create();
    public static PublishSubject<Void> onCreateNewFundSubject = PublishSubject.create();
    public static PublishSubject<Void> userFollowCountChangedSubject = PublishSubject.create();
    public static PublishSubject<Void> closeBindCNCardPageSubject = PublishSubject.create();
    public static PublishSubject<Void> disallowInterceptGoBackSubject = PublishSubject.create();
    public static PublishSubject<Void> daySignStatusChangeSubject = PublishSubject.create();
    public static PublishSubject<Coupon> selectCouponSubject = PublishSubject.create();
    public static PublishSubject<Void> followStockSortChangeSubject = PublishSubject.create();
    public static PublishSubject<Void> afterDonateScoreSubject = PublishSubject.create();
    public static PublishSubject<Void> currentTradingPageOrderStatusChange = PublishSubject.create();

    // cupide 新增的刷新StockInfo
    public static PublishSubject<Void> freshStockInfoSubject = PublishSubject.create();

    //  风险评测相关
    public static PublishSubject<Pair<Integer, String>> receiveRiskTestResultSubject = PublishSubject.create();
    public static PublishSubject<Activity> riskTestFinishSubject = PublishSubject.create();

    private NotificationCenter() {
    }

    public static void init() {
        needLoginSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(nil -> {
                    if (!MyApplication.SHARE_INSTANCE.mLoginPageShowing) {
                        MyApplication.SHARE_INSTANCE.mLoginPageShowing = true;
                        showActivity(MyApplication.SHARE_INSTANCE, an_LoginPage());
                    }
                });

        cashChangedSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fundId -> FortuneManager.getInstance().freshAccount(null));

        receiveRiskTestResultSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    Mine mine = MineManager.getInstance().getmMe();
                    mine.riskAssessmentGrade = pair.first;
                    mine.riskAssessmentGradeMsg = pair.second;
                });

        loginSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(nil -> {
                    JPushUtil.updateTagsAndAlias(MyApplication.SHARE_INSTANCE);

                    if (MineManager.getInstance().isLoginOK()) {
                        if (MineManager.getInstance().getmMe().uploadLogger) {
                            AccessLogManager.uploadLogSince(3);
                            MineManager.getInstance().getmMe().uploadLogger = false;
                        }
                    }
                });

        cancelLoginSubject
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(ignored -> logoutSubject.onNext(null))
                .subscribe();

        logoutSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(nil -> {
                    WebViewExtension.removeCookiesImmediately();
                    JPushUtil.updateTagsAndAlias(MyApplication.SHARE_INSTANCE);
                });

        riskTestFinishSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(activity -> {
                    if (activity != null) {
                        if (activity instanceof FragmentStackActivity) {
                            ((FragmentStackActivity) activity).goBack();
                        } else {
                            activity.finish();
                        }
                    }
                });
    }

    private static Set<String> generateUserTags() {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(AppUtil.getVersionName(MyApplication.SHARE_INSTANCE).replace(".", "_"));
        Mine mine = MineManager.getInstance().getmMe();
        if (mine == null || !mine.isLoginOk()) {
            set.add("type1");
        } else {
            if (!mine.setAuthenticate) {
                set.add("type2");
            } else if (FortuneManager.getInstance().cnAccount.investMoney < 0) {
                set.add("type3");
            }
        }
        return set;
    }
}
