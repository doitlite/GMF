package com.goldmf.GMFund.controller;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.base.StringPair;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.controller.WebViewFragments.WebViewFragmentDelegate;
import com.goldmf.GMFund.controller.business.FundController;
import com.goldmf.GMFund.controller.dialog.ShareDialog;
import com.goldmf.GMFund.controller.internal.FundCardViewHelper;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.trader.FundInvestor;
import com.goldmf.GMFund.model.Fund;
import com.goldmf.GMFund.model.Fund.Fund_Button;
import com.goldmf.GMFund.model.Fund.ImageTarLinkInfo;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.FundBrief.Fund_Status;
import com.goldmf.GMFund.model.FundBrief.Fund_Type;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.model.TarLinkButton;
import com.goldmf.GMFund.util.PersistentObjectUtil;
import com.goldmf.GMFund.util.SecondUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.GMFCircleProgressBar;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.goldmf.GMFund.widget.GMFWebview;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.yale.ui.support.AdvanceSwipeRefreshLayout;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import yale.extension.common.Optional;
import yale.extension.common.shape.RoundCornerShape;
import yale.extension.rx.ConsumeEventChain.POLICY;
import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_FUND_ID_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_FUND_LIST;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_VC_TITLE;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_InvestPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_LoginPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BLUE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.PURPLE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.extension.FlagExtension.hasFlag;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.getSimpleAdapter;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.createAlertDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_preDraw;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setOnRefreshing;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.extension.WebViewExtension.syncCookiesImmediately;
import static com.goldmf.GMFund.model.FundBrief.Fund_Status.Booking;
import static com.goldmf.GMFund.model.FundBrief.Fund_Status.Capital;
import static com.goldmf.GMFund.model.FundBrief.Fund_Status.LockIn;
import static com.goldmf.GMFund.model.FundBrief.Fund_Status.Stop;
import static com.goldmf.GMFund.util.FormatUtil.formatBigNumber;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatRatio;
import static com.goldmf.GMFund.util.FormatUtil.formatRemainingTimeOverDay;
import static com.goldmf.GMFund.util.FormatUtil.formatRemainingTimeOverMonth;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;
import static com.goldmf.GMFund.util.UmengUtil.createShareService;
import static com.goldmf.GMFund.util.UmengUtil.stat_add_bonus_event;
import static com.goldmf.GMFund.util.UmengUtil.stat_fund_detail_event;
import static com.goldmf.GMFund.util.UmengUtil.stat_fund_profile_event;
import static com.goldmf.GMFund.util.UmengUtil.stat_fund_trade_event;
import static com.goldmf.GMFund.util.UmengUtil.stat_invested_member_event;
import static java.util.Collections.emptyList;

/**
 * Created by yale on 15/10/17.
 */
public class FundFragments {

    private static PublishSubject<Fund> sFundChangedSubject = PublishSubject.create();

    private FundFragments() {
    }

    public static class FundDetailFragmentV2 extends SimpleFragment {

        private int mFundId;

        private FundDetailInfo mVM;
        private boolean mDataExpired = false;
        private UMShareAPI mShareAPI;

        private static int FundDetail_Fresh_Booking_Time_Base = 500;

        private Fund cacheFund() {
            if (mVM != null)
                return mVM.source;
            else
                return null;
        }

        public FundDetailFragmentV2 init(int fundId) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_FUND_ID_INT, fundId);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mFundId = getArguments().getInt(KEY_FUND_ID_INT);
            return inflater.inflate(R.layout.frag_fund_detail_v2, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));
            findToolbar(this).setBackgroundColor(STATUS_BAR_BLACK);
            v_setClick(mReloadSection, v -> performFetchPageData(false, true));

            // bind child views
            ((AdvanceSwipeRefreshLayout) mRefreshLayout).setOnPreInterceptTouchEventDelegate(new AdvanceSwipeRefreshLayout.OnPreInterceptTouchEventDelegate() {
                AppBarLayout mAppBarLayout = v_findView(view, R.id.appBarLayout);

                @Override
                public boolean shouldDisallowInterceptTouchEvent(MotionEvent ev) {
                    return mAppBarLayout.getTop() < 0;
                }
            });

            // init child views
            v_setOnRefreshing(mRefreshLayout, () -> performFetchPageData(false, false));

            performFetchPageData(true, true);

            consumeEvent(NotificationCenter.investedFundSubject.filter(fundId -> fundId == mFundId))
                    .setTag("investedFund")
                    .onNextFinish(fundId -> {
                        mDataExpired = true;
                        if (getView() != null) {
                            performFetchPageData(false, false);
                            mDataExpired = false;
                        }
                    })
                    .done();
            consumeEvent(Observable.merge(NotificationCenter.loginSubject, NotificationCenter.logoutSubject))
                    .setTag("loginStateChanged")
                    .onNextFinish(nil -> {
                        mDataExpired = true;
                        if (getUserVisibleHint() && getView() != null) {
                            setUserVisibleHint(true);
                        }
                    })
                    .done();
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (getView() != null) {
                if (isVisibleToUser) {
                    if (mDataExpired) {
                        performFetchPageData(false, false);
                    }
                }
            }
        }

        @Override
        protected boolean onInterceptActivityResult(int requestCode, int resultCode, Intent data) {
            if (UmengUtil.handleOnActivityResult(mShareAPI, requestCode, resultCode, data)) {
                mShareAPI = null;
                return true;
            }
            return super.onInterceptActivityResult(requestCode, resultCode, data);
        }

        private void addAddBonusGuide() {
            View bottomBar = v_findView(this, R.id.section_left_bottom);
            View buttonGroup = v_findView(bottomBar, R.id.section_bottom_button);
            View addBonusButton = buttonGroup.findViewWithTag("add_bonus");
            if (addBonusButton != null) {
                Rect bottomBarRect = getViewRectInWindow(bottomBar);
                Rect buttonGroupRect = getViewRectInWindow(buttonGroup);
                Rect buttonRect = getViewRectInWindow(addBonusButton);

                Context ctx = getActivity();
                FrameLayout container = new FrameLayout(ctx);

                {
                    View dimView = new View(ctx);
                    dimView.setBackgroundColor(0xCC000000);

                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, buttonGroupRect.top);
                    container.addView(dimView, params);

                    v_setClick(dimView, v -> resetTopView(null, null));
                }

                {
                    View dimView = new View(ctx);
                    dimView.setBackgroundColor(0xCC000000);

                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bottomBarRect.width() - buttonRect.left, buttonGroupRect.height() + dp2px(12));
                    params.gravity = Gravity.BOTTOM;
                    container.addView(dimView, params);

                    v_setClick(dimView, v -> resetTopView(null, null));
                }

                {
                    LinearLayout guideGroup = new LinearLayout(ctx);
                    guideGroup.setOrientation(LinearLayout.VERTICAL);

                    {
                        ImageView imageView = new ImageView(ctx);
                        imageView.setImageResource(R.mipmap.bg_add_bonus);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp2px(300), dp2px(40));
                        params.gravity = Gravity.CENTER_HORIZONTAL;
                        guideGroup.addView(imageView, params);
                    }
                    {
                        ImageView imageView = new ImageView(ctx);
                        imageView.setImageResource(R.mipmap.ic_arrow_add_bonus);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp2px(32), dp2px(28));
                        params.topMargin = -dp2px(1);

                        int buttonCenterX = buttonRect.left + (buttonRect.width() >> 1);
                        params.leftMargin = buttonCenterX - dp2px(14);

                        guideGroup.addView(imageView, params);
                    }

                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -2);
                    params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
                    params.bottomMargin = bottomBarRect.height();
                    container.addView(guideGroup, params);

                    v_preDraw(guideGroup, true, v -> {
                        ObjectAnimator animator = ObjectAnimator.ofFloat(v, "translationY", 0, -dp2px(15));
                        animator.setDuration(400);
                        animator.setRepeatCount(ObjectAnimator.INFINITE);
                        animator.setRepeatMode(ObjectAnimator.REVERSE);
                        animator.start();
                        mAnimator = animator;
                    });
                }

                resetTopView(container, new FrameLayout.LayoutParams(-1, -1));
            }
        }

        private Rect getViewRectInWindow(View view) {
            int[] location = new int[2];
            view.getLocationInWindow(location);
            return new Rect(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight());
        }

        private View mTopView;
        private Animator mAnimator;

        private void resetTopView(View topView, FrameLayout.LayoutParams params) {
            if (mAnimator != null) {
                mAnimator.cancel();
                mAnimator = null;
            }
            if (getActivity() != null) {
                ViewGroup decorView = (ViewGroup) getActivity().getWindow().getDecorView();
                if (mTopView != null) {
                    decorView.removeView(mTopView);
                }
                if (topView != null) {
                    decorView.addView(topView, params);
                }
                mTopView = topView;
            }
        }

        private static int Const_Delay_Time = 10 * 1000;
        private int delayTime = 2 * 1000;

        @SuppressWarnings("CodeBlock2Expr")
        private void beginUpdateFundDetailTimer() {

            //            unsubscribeFromMain("Fresh_Booking_Time");

            consumeEvent(Observable.interval(FundDetail_Fresh_Booking_Time_Base, TimeUnit.MILLISECONDS)
                    .filter(it -> mVM != null))
                    .setTag("Fresh_Booking_Time")
                    .setPolicy(POLICY.IGNORED)
                    .onNextFinish(ignored -> {
                        mVM.update();
                        setupHeader(mVM);

                        boolean isStatusBook = mVM.fundState == Fund_Status.Booking;
                        boolean fundHasBegin = mVM.source.beginFundraisingTime <= SecondUtil.currentSecond();
                        boolean beginTimeCorrect = mVM.source.beginFundraisingTime + 20 * 60 * 1000 >= SecondUtil.currentSecond();
                        if (isStatusBook && fundHasBegin && beginTimeCorrect) {

                            Observable<MResults.MResultsInfo<Fund>> observable = FundController.fetchFundInfo(mFundId, false)
                                    .delaySubscription(delayTime, TimeUnit.MILLISECONDS)
                                    .flatMap(it -> {
                                        long countDownTime = mVM.source.beginFundraisingTime + 15 - SecondUtil.currentSecond();
                                        long seconds = Math.max(countDownTime, 0L);
                                        return Observable.just(it).delay(seconds, TimeUnit.SECONDS);
                                    });

                            consumeEvent(observable)
                                    .setTag("Fresh_Detail")
                                    .setPolicy(POLICY.IGNORED)
                                    .onNextFinish(response -> {
                                        delayTime = (delayTime <= Const_Delay_Time) ? Const_Delay_Time : delayTime * 2;
                                        if (response.isSuccess) {
                                            mVM = new FundDetailInfo(response.data);
                                            setupSectionContent(mVM, false);
                                        }
                                    })
                                    .done();
                        }
                    })
                    .done();
        }

        private void performFetchPageData(boolean useCache, boolean reload) {
            consumeEventMRUpdateUI(FundController.fetchFundInfo(mFundId, useCache), reload)
                    .setTag("reload_data")
                    .onNextStart(nil -> {
                        v_setGone(mLoadingSection);
                        mRefreshLayout.setRefreshing(false);
                    })
                    .onNextSuccess(response -> {
                        FundDetailInfo vm = new FundDetailInfo(response.data);
                        if (vm.isUnknownType || vm.isUnknownState) {
                            CharSequence title = String.format(Locale.getDefault(), "未知组合%s", vm.isUnknownType ? "类型" : "状态");
                            CharSequence subTitle = "请更新操盘侠APP";
                            setEmptySectionTips(title, subTitle);
                            updateTitle("组合");
                            changeVisibleSection(TYPE_EMPTY);
                        } else {
                            mVM = vm;

                            if (vm.fundState == Booking) {
                                beginUpdateFundDetailTimer();
                            }


                            setupSectionContent(vm, reload);
                            changeVisibleSection(TYPE_CONTENT);
                        }
                    })
                    .done();
        }

        private void setupSectionContent(FundDetailInfo data, boolean reload) {
            setupHeader(data);
            setupViewPager(data, reload);
            setupBottomBar(data);
        }

        @SuppressWarnings("deprecation")
        private void setupHeader(FundDetailInfo vm) {

            int primaryColor = computeHeaderPrimaryColor(vm.source);
            findToolbar(this).setBackgroundColor(primaryColor);
            setStatusBarBackgroundColor(this, primaryColor);
            View toolbarOverlay = findToolbar(this);
            v_setText(toolbarOverlay, R.id.toolbarTitle, vm.source.name);
            TextView fundTypeLabel = v_findView(toolbarOverlay, R.id.label_fund_type);
            boolean showFundTypeLabel = anyMatch(vm.fundInnerType, Fund_Type.Porfolio, Fund_Type.WuYo, Fund_Type.WenJian);
            if (showFundTypeLabel) {
                v_setText(fundTypeLabel, setColor(Fund_Type.toString(vm.fundInnerType), anyMatch(vm.fundPresentType, Fund_Type.Charitable) ? PURPLE_COLOR : BLUE_COLOR));
                fundTypeLabel.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0x66FFFFFF, dp2px(1))));
            }
            fundTypeLabel.setVisibility(showFundTypeLabel ? View.VISIBLE : View.GONE);

            Drawable headerBackground = computeHeaderBackground(vm.source);
            View headerSection = v_findView(mContentSection, R.id.section_header);
            headerSection.setBackgroundDrawable(headerBackground);

            v_setText(headerSection, R.id.label_fund_state, vm.fundStateText);

            int fundInnerType = vm.fundInnerType;
            int fundState = vm.fundState;
            boolean isIncomeVisible = vm.mIncomeVisible;
            boolean isInvested = vm.mHasInvest;

            View reviewHeader = v_findView(headerSection, R.id.header_review);
            View orderHeader = v_findView(headerSection, R.id.header_order);
            View capitalHeader = v_findView(headerSection, R.id.header_capital);
            View lockedOrStopHeader = v_findView(headerSection, R.id.header_locked_or_stop);
            View notInvestedHeader = v_findView(headerSection, R.id.header_not_invest);
            GMFCircleProgressBar mProgressBar = v_findView(headerSection, R.id.progress_raised);
            View[] headers = new View[]{reviewHeader, orderHeader, capitalHeader, lockedOrStopHeader, notInvestedHeader};
            Action1<View> showHeaderFunc = (arg -> {
                for (View header : headers) {
                    header.setVisibility(header == arg ? View.VISIBLE : View.GONE);
                }
            });
            if (fundState == Fund_Status.Review) {
                showHeaderFunc.call(reviewHeader);
            } else if (fundState == Fund_Status.Booking) {
                showHeaderFunc.call(orderHeader);
                v_setText(orderHeader, R.id.label_remaining_time, vm.remainingTime);
            } else if (fundState == Fund_Status.Capital) {
                showHeaderFunc.call(capitalHeader);
                v_setText(capitalHeader, R.id.label_title, Fund_Type.hasInvestLimit(fundInnerType) ? "剩余可投金额" : "组合金额已达");
                v_setText(capitalHeader, R.id.label_raised_money, Fund_Type.hasInvestLimit(fundInnerType) ? vm.remainAmount : vm.raisedAmount);
                v_setText(capitalHeader, R.id.label_raised_money_progress, vm.raisedProgressText);
                mProgressBar.setProgress(vm.raisedProgress);
                mProgressBar.setFillColor(WHITE_COLOR);
                mProgressBar.setBorderColor(Color.parseColor("#1affffff"));
            } else if (fundState == Fund_Status.LockIn) {
                if (isIncomeVisible) {
                    showHeaderFunc.call(lockedOrStopHeader);
                    if (fundInnerType == Fund_Type.Bonus) {
                        v_setText(lockedOrStopHeader, R.id.label_title, "当前年化收益率");
                        v_setText(lockedOrStopHeader, R.id.label_income_ratio, vm.currentIncomeAnnualRatio);
                    } else {
                        v_setText(lockedOrStopHeader, R.id.label_title, "当前收益率");
                        v_setText(lockedOrStopHeader, R.id.label_income_ratio, vm.currentIncomeRatio);
                    }
                    v_setText(lockedOrStopHeader, R.id.label_income, isInvested ? vm.currentIncome : "");
                    v_setVisibility(lockedOrStopHeader, R.id.line_sep, isInvested ? View.VISIBLE : View.INVISIBLE);
                    v_setVisibility(lockedOrStopHeader, R.id.label_income, isInvested ? View.VISIBLE : View.INVISIBLE);
                } else {
                    showHeaderFunc.call(notInvestedHeader);
                    v_setText(notInvestedHeader, R.id.label_title, "当前收益率");
                }
            } else if (fundState == Fund_Status.Stop) {
                if (isIncomeVisible) {
                    showHeaderFunc.call(lockedOrStopHeader);
                    if (fundInnerType == Fund_Type.Bonus) {
                        v_setText(lockedOrStopHeader, R.id.label_title, "最终年化收益率");
                        v_setText(lockedOrStopHeader, R.id.label_income_ratio, vm.finalIncomeAnnualRatio);
                    } else {
                        v_setText(lockedOrStopHeader, R.id.label_title, "最终收益率");
                        v_setText(lockedOrStopHeader, R.id.label_income_ratio, vm.finalIncomeRatio);
                    }
                    v_setText(lockedOrStopHeader, R.id.label_income, isInvested ? vm.finalIncome : "");
                    v_setVisibility(lockedOrStopHeader, R.id.line_sep, isInvested ? View.VISIBLE : View.INVISIBLE);
                    v_setVisibility(lockedOrStopHeader, R.id.label_income, isInvested ? View.VISIBLE : View.INVISIBLE);
                } else {
                    showHeaderFunc.call(notInvestedHeader);
                    v_setText(notInvestedHeader, R.id.label_title, "最终收益率");
                }
            }
        }

        private int computeHeaderPrimaryColor(Fund fund) {
            if (fund != null) {
                switch (fund.type) {
                    case Fund_Type.Porfolio:
                        return 0xFF37A7F8;
                    case Fund_Type.Bonus:
                        return 0xFFFB3F3F;
                    case Fund_Type.Charitable:
                        return 0xFFB820D1;
                }
            }
            return STATUS_BAR_BLACK;
        }

        private Drawable computeHeaderBackground(Fund fund) {
            if (fund != null) {
                switch (fund.type) {
                    case Fund_Type.Porfolio:
                        return getResources().getDrawable(R.mipmap.bg_header_fund_normal);
                    case Fund_Type.Bonus:
                        return getResources().getDrawable(R.mipmap.bg_header_fund_bonus);
                    case Fund_Type.Charitable:
                        return getResources().getDrawable(R.mipmap.bg_header_fund_charitable);
                }
            }

            return new ColorDrawable(STATUS_BAR_BLACK);
        }

        private void setupViewPager(FundDetailInfo data, boolean navigateToProfilePageIfNeeded) {
            TabLayout tabLayout = v_findView(this, R.id.tabLayout);
            ViewPager pager = v_findView(this, R.id.pager);
            pager.setOffscreenPageLimit(3);

            String[] pageTitles;
            if (Fund_Status.afterCapital(data.source.status)) {
                pageTitles = new String[]{"业绩", "概览", "组合信息", "投资成员(" + data.source.investTimes + ")"};
            } else {
                pageTitles = new String[]{"概览", "组合信息", "投资成员(" + data.source.investTimes + ")"};
            }

            if (pager.getAdapter() == null || pager.getAdapter().getCount() != pageTitles.length) {
                pager.setAdapter(new FundDetailPagerAdapter(getChildFragmentManager(), data, pageTitles));
                tabLayout.setupWithViewPager(pager);
            } else {
                FundDetailPagerAdapter adapter = (FundDetailPagerAdapter) pager.getAdapter();
                adapter.mData = data;
                adapter.mPageTitles = pageTitles;
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    tabLayout.getTabAt(i).setText(pageTitles[i]);
                }
                sFundChangedSubject.onNext(data.source);
            }

            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    pager.setCurrentItem(tab.getPosition(), true);
                    String title = (String) opt(tab.getText()).or("");
                    if (title.contains("概览")) {
                        stat_fund_profile_event(getActivity(), opt(FundDetailFragmentV2.this));
                    } else if (title.contains("组合信息")) {
                        stat_fund_detail_event(getActivity(), opt(FundDetailFragmentV2.this));
                    } else if (title.contains("投资成员")) {
                        stat_invested_member_event(getActivity(), opt(FundDetailFragmentV2.this));
                    } else if (title.contains("业绩")) {
                        stat_fund_trade_event(getActivity(), opt(FundDetailFragmentV2.this));
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            if (navigateToProfilePageIfNeeded && MineManager.getInstance().isLoginOK() && !data.mIncomeVisible) {
                if (pageTitles.length == 4) {
                    pager.setCurrentItem(1, true);
                } else if (pageTitles.length == 3) {
                    pager.setCurrentItem(0, true);
                }
            }
        }

        private void setupBottomBar(FundDetailInfo data) {
            View bottomSection = v_findView(mContentSection, R.id.section_left_bottom);
            if (TextUtils.isEmpty(data.bottomBarHint)) {
                v_setGone(bottomSection, R.id.label_hint);
            } else {
                v_setText(bottomSection, R.id.label_hint, data.bottomBarHint);
                v_setVisible(bottomSection, R.id.label_hint);
            }
            LinearLayout buttonSection = v_findView(mContentSection, R.id.section_bottom_button);
            buttonSection.removeAllViewsInLayout();
            if (data.source.buttons.isEmpty()) {
                v_setGone(buttonSection);
            } else {
                v_setVisible(buttonSection);


                Stream.of(data.source.buttons)
                        .map(button -> {
                            switch (Fund_Button.getInstance(button.tag)) {
                                case Invest:
                                    return createInvestButton(buttonSection, data.source);
                                case Invite:
                                    return createInviteButton(buttonSection, button);
                                case Booking:
                                    return createBookButton(buttonSection, data.source);
                                case Preferential:
                                    return createAddBonusButton(buttonSection, data.source, button);
                                case Cancel:
                                case Delete:
                                case ReAdd:
                                default:
                                    return null;
                            }
                        })
                        .filter(button -> button != null)
                        .forEach(cell -> {
                            if (buttonSection.getChildCount() == 0) {
                                buttonSection.addView(cell);
                            } else {
                                LinearLayout.LayoutParams cellParams = (LinearLayout.LayoutParams) cell.getLayoutParams();
                                cellParams.leftMargin = dp2px(16);
                                cell.setLayoutParams(cellParams);
                                buttonSection.addView(cell);
                            }
                        });
                if (data.mHasInvest && !PersistentObjectUtil.readHasShowFundPageBonusGuide(data.source.index)) {
                    PersistentObjectUtil.writeHasShowFundPageBonusGuide(data.source.index, true);
                    View addBonusButton = bottomSection.findViewWithTag("add_bonus");
                    if (addBonusButton != null) {
                        v_preDraw(addBonusButton, true, view -> {
                            addAddBonusGuide();
                        });
                    }
                }
            }
        }

        private View createBookButton(ViewGroup container, Fund fund) {
            View view = createBottomButton(container.getContext(), button -> {
                button.setBackgroundResource(R.drawable.sel_round_button_yellow_bg);
                button.setText("预约");
            });
            v_setClick(view, v -> {
                performBookFund(fund);
                UmengUtil.stat_to_book_event(getActivity(), Optional.of(this));
            });
            return view;
        }

        private void performBookFund(Fund fund) {
            if (MineManager.getInstance().isLoginOK()) {
                GMFProgressDialog progressDialog = new GMFProgressDialog(getActivity());
                progressDialog.setMessage("预约中...");
                progressDialog.show();
                consumeEventMR(FundController.bookFund(fund))
                        .onNextStart(response -> progressDialog.dismiss())
                        .onNextSuccess(response -> createAlertDialog(this, null, "预约提醒分红乐组合'" + fund.name + "'成功，将在组合正式开放投资后第一时间通知你", "知道了").show())
                        .onNextFail(response -> createAlertDialog(this, getErrorMessage(response)).show())
                        .done();
            } else {

                consumeEvent(NotificationCenter.loginSubject)
                        .setTag("login")
                        .onNextFinish(callback -> {
                            unsubscribeFromMain("login");
                            performBookFund(fund);
                        })
                        .done();
                showActivity(this, an_LoginPage());
            }
        }

        private View createAddBonusButton(ViewGroup container, Fund fund, TarLinkButton buttonInfo) {
            Context context = container.getContext();
            View view = createBottomButton(context, button -> {
                button.setBackgroundResource(R.drawable.sel_round_button_red_bg);
                button.setTextColor(getResources().getColor(R.color.sel_button_text_light));
                button.setTextSize(12);
                button.setTag("add_bonus");
                button.setPadding(0, 0, 0, 0);
                if (fund.preferential.maxPreferentialRatio > 0) {
                    button.setText(concat(
                            "我要加息",
                            setFontSize("最高可达" + formatRatio(fund.expectedMinAnnualYield + fund.expectedMaxAnnualYield + fund.preferential.maxPreferentialRatio, false, 0, 2), sp2px(10))));
                } else {
                    button.setText("我要加息");
                }
            });
            v_setClick(view, v -> {
                unsubscribeFromMain("login");
                resetTopView(null, null);
                if (MineManager.getInstance().isLoginOK()) {
                    mDataExpired = true;
                    CMDParser.parse(buttonInfo.tarLink).call(context);
                    stat_add_bonus_event(getActivity(), Optional.of(this));
                } else {
                    showActivity(this, an_LoginPage());
                }
            });
            return view;
        }


        private View createInvestButton(ViewGroup container, Fund fund) {
            View view = createBottomButton(container.getContext(), button -> {
                button.setBackgroundResource(R.drawable.sel_round_button_yellow_bg);
                if (fund.investOrNull != null) {
                    button.setText("追加投资");
                } else {
                    button.setText("立即投资");
                }

                if (fund.innerType == Fund_Type.Bonus && (fund.raisedCapital - fund.targetCapital >= 0)) {
                    button.setEnabled(false);
                } else {
                    button.setEnabled(true);
                }
            });
            v_setClick(view, v -> {
                unsubscribeFromMain("login");
                if (MineManager.getInstance().isLoginOK()) {
                    if (anyMatch(fund.innerType, Fund_Type.Bonus) && fund.raisedCapital - fund.targetCapital >= 0) {
                        createAlertDialog(this, "不可投资", "当前组合已全部投资完", "知道了").show();
                    } else {
                        showActivity(this, an_InvestPage(fund.index));
                    }
                } else {
                    consumeEvent(NotificationCenter.loginSubject)
                            .setTag("login")
                            .onNextFinish(callback -> {
                                unsubscribeFromMain("login");
                                showActivity(this, an_InvestPage(fund.index));
                            })
                            .done();
                    showActivity(this, an_LoginPage());
                }
            });
            UmengUtil.stat_to_invest_event(getActivity(), Optional.of(this));
            return view;
        }

        private View createInviteButton(ViewGroup container, TarLinkButton buttonInfo) {
            View ret = createBottomButton(container.getContext(), button -> {
                button.setBackgroundResource(R.drawable.sel_round_button_red_bg);
                button.setTextColor(getResources().getColor(R.color.sel_button_text_light));
                button.setText("分享");
            });
            v_setClick(ret, v -> {
                if (buttonInfo.shareInfo == null)
                    return;
                ShareDialog shareDialog = new ShareDialog(getActivity(), buttonInfo.shareInfo);
                shareDialog.setShareItemClickEventDelegate((dialog, platform) -> {
                    dialog.dismiss();
                    performInvite(buttonInfo.shareInfo, platform.shareMedia);
                });
                shareDialog.show();
                UmengUtil.stat_enter_share_from_fund_detail_page(getActivity(), Optional.of(this));
                UmengUtil.stat_to_share_event(getActivity(), Optional.of(this));
            });
            return ret;
        }

        private void performInvite(ShareInfo shareInfo, SHARE_MEDIA media) {
            mShareAPI = createShareService(getActivity(), shareInfo.title);
            UmengUtil.performShare(getActivity(), mShareAPI, media, shareInfo, null);
        }

        private View createBottomButton(Context context, Action1<Button> afterInitAction) {
            Button labelView = new Button(context);
            labelView.setTextColor(getResources().getColor(R.color.sel_button_text_dark));
            labelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.gmf_button_text_size_large));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, getResources().getDimensionPixelSize(R.dimen.gmf_button_height_medium));
            params.weight = 1;
            params.gravity = Gravity.CENTER_VERTICAL;
            labelView.setLayoutParams(params);

            afterInitAction.call(labelView);
            return labelView;
        }
    }

    private static class FundDetailPagerAdapter extends FragmentPagerAdapter {
        private FundDetailInfo mData;
        private String[] mPageTitles;

        public FundDetailPagerAdapter(FragmentManager manager, FundDetailInfo data, String[] pageTitles) {
            super(manager);
            mData = data;
            mPageTitles = pageTitles;
        }

        @Override
        public Fragment getItem(int position) {
            String title = getPageTitle(position).toString();
            if (title.contains("业绩"))
                return new FundDetailPageWebFragment().init(mData.source.index, mData.source.fundTradeH5URL());
            else if (title.contains("概览"))
                return new FundDetailPageWebFragment().init(mData.source.index, mData.source.fundInfoH5URL());
            else if (title.contains("组合信息"))
                return new FundDetailPageWebFragment().init(mData.source.index, mData.source.fundDetailH5URL());
            else
                return new FundDetailPageInvestedMemberFragment().init(mData.source);
        }

        @Override
        public int getCount() {
            return mPageTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mPageTitles[position];
        }
    }

    public static class FundDetailPageWebFragment extends SimpleFragment {

        private int mFundId;
        private String mRequestURL;
        private GMFWebview mWebView;
        private WebViewFragmentDelegate mDelegate;

        public FundDetailPageWebFragment init(int fundId, String requestURL) {
            Bundle arguments = new Bundle();
            arguments.putInt("fund_id", fundId);
            arguments.putString("request_url", requestURL);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mFundId = getArguments().getInt("fund_id");
            mRequestURL = getArguments().getString("request_url");
            return inflater.inflate(R.layout.frag_fund_detail_page_web, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // bing child views
            mWebView = v_findView(this, R.id.webView);

            Observable<Fund> observable = sFundChangedSubject
                    .filter(fund -> fund.index == mFundId);
            consumeEvent(observable)
                    .setTag("fundInfoChanged")
                    .onNextFinish(nil -> {
                        v_setVisible(mLoadingSection);
                        mWebView.reload();
                    })
                    .done();

            consumeEvent(Observable.merge(NotificationCenter.loginSubject, NotificationCenter.logoutSubject))
                    .setTag("loginStateChanged")
                    .onNextFinish(nil -> {
                        v_setVisible(mLoadingSection);
                        mWebView.reload();
                    })
                    .done();

            mDelegate = new WebViewFragmentDelegate(this, mWebView, true);
            mDelegate.onViewCreated(mRequestURL);

            runOnUIThreadDelayed(() -> mWebView.loadUrl(mRequestURL), 500L);

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            opt(mDelegate).apply(WebViewFragmentDelegate::onDestroyView);
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            opt(mDelegate).apply(delegate -> delegate.setUserVisibleHint(isVisibleToUser));
        }

        @Override
        protected boolean onInterceptGoBack() {
            return Optional.of(mDelegate).let(WebViewFragmentDelegate::onInterceptGoBack).or(false) || super.onInterceptGoBack();
        }
    }

    private static class FundDetailInfo {
        public Fund source;
        public int fundState;
        public int fundPresentType;
        public int fundInnerType;
        public int moneyType;
        public boolean mIncomeVisible;
        public boolean mHasInvest;
        public boolean isUnknownType;
        public boolean isUnknownState;
        public ShareInfo shareInfo;

        public CharSequence fundStateText;

        public CharSequence raisedAmount;
        public CharSequence remainAmount;
        public CharSequence raisedProgressText;
        public float raisedProgress;

        public CharSequence remainingTime;

        public CharSequence currentIncomeRatio;
        public CharSequence currentIncomeAnnualRatio;
        public CharSequence currentIncome;

        public CharSequence finalIncomeRatio;
        public CharSequence finalIncomeAnnualRatio;
        public CharSequence finalIncome;

        public CharSequence bottomBarHint;

        public void update() {

            this.remainingTime = new SpannableStringBuilder();
            double second = source.beginFundraisingTime - SecondUtil.currentSecond() + 15;//特别处理15s
            StringPair[] pairs = formatRemainingTimeOverDay(second);
            for (StringPair pair : pairs) {
                remainingTime = concatNoBreak(remainingTime, pair.first, setFontSize(pair.second, sp2px(14)), " ");
            }

        }

        public FundDetailInfo(Fund data) {
            this.source = data;

            this.fundState = safeGet(() -> data.status, Fund_Status.NoDefine);
            this.moneyType = safeGet(() -> data.moneyType, Money_Type.CN);
            this.fundPresentType = safeGet(() -> data.type, Fund_Type.NoDefine);
            this.fundInnerType = safeGet(() -> data.innerType, Fund_Type.NoDefine);
            this.mIncomeVisible = safeGet(() -> data.incomeVisible, false);
            this.mHasInvest = safeGet(() -> data.investOrNull.investMoney > 0D, false);
            this.isUnknownType = Fund_Type.isUnknown(fundPresentType) || Fund_Type.isUnknown(fundInnerType);
            this.isUnknownState = Fund_Status.isUnknown(fundState);
            this.shareInfo = safeGet(() -> data.shareInfo, null);

            this.fundStateText = safeGet(() -> data.statusTitle + ", " + data.statusDetail, "");

            this.raisedAmount = concatNoBreak(formatMoney(data.raisedCapital, false, 0), setFontSize(" " + Money_Type.getUnit(moneyType), sp2px(14)));
            if (Fund_Type.hasInvestLimit(data.innerType) && data.targetCapital - data.raisedCapital <= 0) {
                this.remainAmount = "已满额";
            } else {
                this.remainAmount = concatNoBreak(formatMoney(Math.max(data.targetCapital - data.raisedCapital, 0), false, 0), setFontSize(" " + Money_Type.getUnit(moneyType), sp2px(14)));
            }
            this.raisedProgress = (float) (100 * formatRatio(data.raisedCapital / data.targetCapital, 0.5, 0));
            this.raisedProgressText = formatRatio(formatRatio(data.raisedCapital / data.targetCapital, 0.5, 0), false, 0);

            this.currentIncomeRatio = formatRatio(data.currentIncomeRatioOrNull, true, 2);
            if (fundInnerType == Fund_Type.Bonus) {
                if (data.expectedMinAnnualYield > 0) {
                    this.currentIncomeAnnualRatio = formatRatio(data.expectedMinAnnualYield + safeGet(() -> data.floatingAnnualYieldOrNull, 0D) + data.preferential.preferentialRatio, true, 2);
                } else {
                    this.currentIncomeAnnualRatio = formatRatio(null, true, 2);
                }
            } else {
                this.currentIncomeAnnualRatio = formatRatio(data.currentIncomeRatioOrNull, true, 2);
            }
            this.currentIncome = concat(
                    "我的预期盈亏",
                    setFontSize(setColor(formatMoney(safeGet(() -> data.investOrNull.totalIncome, 0D), true, 0, 2) + Money_Type.getUnit(moneyType), TEXT_WHITE_COLOR), sp2px(14)));

            this.finalIncomeRatio = currentIncomeRatio;
            this.finalIncomeAnnualRatio = currentIncomeAnnualRatio;
            this.finalIncome = concat(
                    "我的最终盈亏",
                    setFontSize(setColor(formatMoney(safeGet(() -> data.investOrNull.totalIncome, 0D), true, 0, 2) + Money_Type.getUnit(moneyType), TEXT_WHITE_COLOR), sp2px(14)));


            if (fundState == Fund_Status.Booking) {
                this.bottomBarHint = "已有 " + source.bookingTimes + " 人预约";
            } else if (Fund_Status.afterReview(fundState)) {
                if (data.preferential != null && data.preferential.preferentialRatio > 0) {
                    if (mIncomeVisible) {
                        this.bottomBarHint = concatNoBreak(
                                this.bottomBarHint = "我已投资 " + formatBigNumber(safeGet(() -> source.investOrNull.investMoney, 0D), false, 0, 2) + Money_Type.getUnit(moneyType),
                                setColor("(已加息" + formatRatio(safeGet(() -> data.preferential.preferentialRatio, 0D), false, 0, 2) + ")", TEXT_RED_COLOR)
                        );
                    } else {
                        this.bottomBarHint = concatNoBreak(
                                this.bottomBarHint = "暂未投资",
                                setColor("(已加息" + formatRatio(safeGet(() -> data.preferential.preferentialRatio, 0D), false, 0, 2) + ")", TEXT_RED_COLOR)
                        );
                    }
                } else {
                    if (mIncomeVisible) {
                        this.bottomBarHint = "我已投资 " + formatBigNumber(safeGet(() -> source.investOrNull.investMoney, 0D), false, 0, 2) + Money_Type.getUnit(moneyType);
                    } else {
                        this.bottomBarHint = "";
                    }
                }
            } else {
                this.bottomBarHint = "";
            }

            update();
        }
    }

    public static class FundProtocolFragment extends SimpleFragment {
        private int mFundId;

        public FundProtocolFragment init(int fundId) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_FUND_ID_INT, fundId);
            setArguments(arguments);
            return this;
        }

        @Override
        protected boolean isTraceLifeCycle() {
            return false;
        }

        @Override
        protected boolean isDelegateLifeCycleEventToSetUserVisible() {
            return false;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mFundId = getArguments().getInt(KEY_FUND_ID_INT);
            return inflater.inflate(R.layout.frag_fund_protocol, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);
            v_setClick(mReloadSection, this::performFetchFundInfo);

            performFetchFundInfo();
            UmengUtil.stat_fund_protocol_agree_event(getActivity(), Optional.of(this));
        }

        private void performFetchFundInfo() {
            changeVisibleSection(TYPE_LOADING);
            FundController.fetchFundInfo(mFundId, true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(callback -> {
                        v_setGone(mLoadingSection);
                        if (isSuccess(callback)) {
                            setupWebView(callback.data);
                            changeVisibleSection(TYPE_CONTENT);
                        } else {
                            v_setText(mReloadSection, R.id.label_title, getErrorMessage(callback));
                            changeVisibleSection(TYPE_RELOAD);
                        }
                    });
        }

        private void setupWebView(Fund fund) {
            View view = getView();
            WebView webView = v_findView(view, R.id.webView);
            webView.setWebViewClient(new WebViewClient() {

                @Override
                public void onLoadResource(WebView view, String url) {
                    syncCookiesImmediately(url);
                    super.onLoadResource(view, url);
                }
            });

            syncCookiesImmediately(fund.investAgreementH5URL());
            webView.loadUrl(fund.investAgreementH5URL());
        }
    }

    public static class FundDetailPageProfileFragment extends SimpleFragment {
        private Fund mFund;
        private boolean mDataExpired = true;

        private RecyclerView mRecyclerView;

        @Override
        protected boolean isTraceLifeCycle() {
            return false;
        }

        @Override
        protected boolean isDelegateLifeCycleEventToSetUserVisible() {
            return false;
        }

        public FundDetailPageProfileFragment init(Fund fund) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(Fund.class.getSimpleName(), fund);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mFund = (Fund) getArguments().getSerializable(Fund.class.getSimpleName());
            return inflater.inflate(R.layout.frag_fund_profile, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mRecyclerView = v_findView(this, R.id.recyclerView);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            initRecyclerView();

            consumeEvent(sFundChangedSubject.filter(fund -> fund.index == mFund.index))
                    .setTag("on_fund_change")
                    .onNextFinish(fund -> {
                        mFund = fund;
                        if (getView() != null) {
                            initRecyclerView();
                        } else {
                            mDataExpired = true;
                        }
                    })
                    .done();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mDataExpired = true;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (getView() != null) {
                if (isVisibleToUser && mDataExpired) {
                    initRecyclerView();
                }
            }
        }

        @SuppressWarnings("deprecation")
        private void initRecyclerView() {
            Fund fund = mFund;
            List<ImageTarLinkInfo> items = opt(fund).let(it -> it.imageList).or(emptyList());
            if (mRecyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<ImageTarLinkInfo> adapter = getSimpleAdapter(mRecyclerView);
                adapter.resetItems(items);
            } else {
                SimpleRecyclerViewAdapter<ImageTarLinkInfo> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                        .onCreateItemView(R.layout.cell_fund_profile)
                        .onCreateViewHolder(builder -> {
                            builder.bindChildWithTag("itemView", R.id.itemView)
                                    .bindChildWithTag("coverImage", R.id.img_cover)
                                    .configureView((item, pos) -> {
                                        if (!TextUtils.isEmpty(item.imageUrl)) {
                                            View itemView = builder.getChildWithTag("itemView");
                                            SimpleDraweeView coverImage = builder.getChildWithTag("coverImage");
                                            DraweeController controller = Fresco.newDraweeControllerBuilder()
                                                    .setOldController(coverImage.getController())
                                                    .setUri(item.imageUrl)
                                                    .setControllerListener(new BaseControllerListener<ImageInfo>() {
                                                        @Override
                                                        public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                                                            super.onFinalImageSet(id, imageInfo, animatable);
                                                            if (imageInfo != null && mRecyclerView != null) {
                                                                int parentWidth = mRecyclerView.getMeasuredWidth();
                                                                float ratio = (float) imageInfo.getWidth() / imageInfo.getHeight();
                                                                ViewGroup.LayoutParams params = itemView.getLayoutParams();
                                                                params.height = (int) ((float) parentWidth / ratio);
                                                                itemView.setLayoutParams(params);
                                                            } else {
                                                                ViewGroup.LayoutParams params = itemView.getLayoutParams();
                                                                params.height = dp2px(175);
                                                                itemView.setLayoutParams(params);
                                                            }
                                                        }
                                                    })
                                                    .build();
                                            coverImage.setController(controller);
                                        }
                                    });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            v_setClick(holder.itemView, v -> {
                                ImageTarLinkInfo item = ad.getItem(holder.getAdapterPosition());
                                CMDParser.parse(item.tarlink).call(getActivity());
                            });
                        })
                        .create();

                View headerView = adapter.createHeaderView(this, R.layout.header_fund_profile);
                {
                    SimpleDraweeView coverImage = v_findView(headerView, R.id.img_cover);
                    TextView leftLabel = v_findView(headerView, R.id.label_left);
                    TextView rightLabel = v_findView(headerView, R.id.label_right);
                    View sepLine = v_findView(headerView, R.id.line);
                    TextView dateLabel = v_findView(headerView, R.id.label_date);
                    dateLabel.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0x30FFFFFF, dp2px(4))));

                    switch (fund.type) {
                        case Fund_Type.Porfolio:
                            coverImage.setImageResource(R.mipmap.bg_fund_normal_cover);
                            v_setText(leftLabel, concat("收益分成", setFontSize(formatRatio(1 - fund.sharing.profitSharingRatio, false, 0), sp2px(32))));
                            if (fund.stopLossRatio != 0) {
                                v_setText(rightLabel, concat("本金保障", setFontSize(formatRatio(fund.stopLossRatio, false, 0, 2), sp2px(32))));
                                v_setVisible(sepLine);
                                v_setVisible(rightLabel);
                            } else {
                                v_setGone(sepLine);
                                v_setGone(rightLabel);
                            }
                            break;
                        case Fund_Type.Bonus:
                        default:
                            coverImage.setImageResource(R.mipmap.bg_fund_bonus_cover);

                            String tempTitle = "预期年化收益";
                            double minRatio = 0;
                            double maxRatio = 0;

                            if (fund != null) {
                                minRatio = fund.expectedMinAnnualYield;
                                maxRatio = fund.expectedMinAnnualYield + fund.expectedMaxAnnualYield;

                                if (fund.preferential != null && fund.preferential.preferentialRatio > 0) {
                                    tempTitle = "预期年化收益(已加息)";
                                    minRatio += fund.preferential.preferentialRatio;
                                    maxRatio += fund.preferential.preferentialRatio;
                                }
                            }

                            if (minRatio != maxRatio) {
                                v_setText(leftLabel, concat(tempTitle, setFontSize(formatRatio(minRatio, false, 0, 2).replace("%", "") + "~" + formatRatio(maxRatio, false, 0, 2), sp2px(32))));
                            } else {
                                v_setText(leftLabel, concat(tempTitle, setFontSize(formatRatio(maxRatio, false, 0, 2), sp2px(32))));
                            }

                            v_setGone(sepLine);
                            v_setGone(rightLabel);
                            break;
                    }
                    StringPair[] pairs = formatRemainingTimeOverMonth(fund.stopTime - fund.startTime);
                    StringBuilder builder = new StringBuilder("组合期限 ");
                    for (StringPair pair : pairs)
                        builder.append(pair.first).append(pair.second);
                    v_setText(dateLabel, builder.toString());
                }
                if (anyMatch(fund.type, Fund_Type.Porfolio, Fund_Type.Bonus))
                    adapter.addHeader(headerView);
                mRecyclerView.setAdapter(adapter);
            }
            SimpleRecyclerViewAdapter adapter = getSimpleAdapter(mRecyclerView);
            mEmptySection.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            mDataExpired = false;
        }
    }

    public static class FundDetailPageInvestedMemberFragment extends SimpleFragment {
        private FundBrief mFund;
        private RecyclerView mRecyclerView;
        private boolean mDataExpired = true;

        public FundDetailPageInvestedMemberFragment init(FundBrief fund) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(FundBrief.class.getSimpleName(), fund);
            setArguments(arguments);
            return this;
        }

        @Override
        protected boolean isDelegateLifeCycleEventToSetUserVisible() {
            return false;
        }

        @Override
        protected boolean isTraceLifeCycle() {
            return false;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mFund = (FundBrief) getArguments().getSerializable(FundBrief.class.getSimpleName());
            return inflater.inflate(R.layout.frag_invested_member_list_v2, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mRecyclerView = v_findView(view, R.id.recyclerView);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

            v_setText(this, R.id.text4, "金额(" + Money_Type.getUnit(mFund.moneyType) + ")");

            v_setClick(this, R.id.section_reload, v -> {
                performFetchMemberList(false);
            });

            changeVisibleSection(TYPE_LOADING);

            consumeEvent(sFundChangedSubject.filter(fund -> fund != null && fund.index == mFund.index))
                    .setTag("onFundChange")
                    .onNextFinish(fund -> {
                        mFund = fund;
                        mDataExpired = true;
                        if (getParentFragment().getUserVisibleHint() && getUserVisibleHint() && getView() != null) {
                            performFetchMemberList(true);
                            mDataExpired = false;
                        }
                    })
                    .done();

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mDataExpired = true;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (mDataExpired) {
                    performFetchMemberList(true);
                    mDataExpired = false;
                }
            }
        }

        private void performFetchMemberList(boolean checkLoginState) {
            boolean reload = mLoadingSection.getVisibility() == View.VISIBLE;

            if (checkLoginState && !MineManager.getInstance().isLoginOK()) {
                v_setGone(mEmptySection);
                v_setGone(mContentSection);
                v_setGone(mLoadingSection);
                v_setVisible(mReloadSection);
                v_setText(mReloadSection, R.id.label_title, "您还没登录");
                return;
            }

            consumeEventMRUpdateUI(FundController.fetchInvestorInfoList(mFund), reload)
                    .setTag("fetch_member_list")
                    .onNextSuccess(response -> {
                        List<InvestMemberCellVM> items = InvestMemberCellVM.from(response.data);
                        if (items.isEmpty()) {
                            changeVisibleSection(TYPE_EMPTY);
                        } else {
                            SimpleRecyclerViewAdapter<InvestMemberCellVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                                    .onCreateItemView(R.layout.cell_invested_member)
                                    .onCreateViewHolder(builder -> {
                                        builder.bindChildWithTag("indexLabel", R.id.label_index)
                                                .bindChildWithTag("timeLabel", R.id.label_time)
                                                .bindChildWithTag("nameAndPhoneLabel", R.id.label_name_and_phone)
                                                .bindChildWithTag("amountLabel", R.id.label_amount)
                                                .configureView((item, pos) -> {
                                                    v_setText(builder.getChildWithTag("indexLabel"), item.index);
                                                    v_setText(builder.getChildWithTag("timeLabel"), item.time);
                                                    v_setText(builder.getChildWithTag("nameAndPhoneLabel"), item.nameAndPhone);
                                                    v_setText(builder.getChildWithTag("amountLabel"), item.amount);
                                                });
                                        return builder.create();
                                    })
                                    .create();
                            mRecyclerView.setAdapter(adapter);
                            changeVisibleSection(TYPE_CONTENT);
                        }
                    })
                    .done();
        }
    }

    private static class InvestMemberCellVM {
        public final CharSequence index;
        public final CharSequence time;
        public final CharSequence nameAndPhone;
        public final CharSequence amount;

        public static List<InvestMemberCellVM> from(List<FundInvestor> dataSet) {
            return Stream.of(opt(dataSet).or(emptyList())).map(it -> new InvestMemberCellVM(it)).collect(Collectors.toList());
        }

        public InvestMemberCellVM(FundInvestor source) {
            this.index = "" + source.index;
            this.time = concat(
                    formatSecond(source.investTime, "MM/dd"),
                    setColor(formatSecond(source.investTime, "HH:mm"), TEXT_GREY_COLOR)
            );
            this.nameAndPhone = concat(
                    source.investorName,
                    setColor(source.cellphone, TEXT_GREY_COLOR)
            );
            this.amount = formatMoney(source.investAmount, false, 0);
        }
    }

    public static class FundListFragment extends SimpleFragment {

        private List<FundBrief> mFunds;
        private String mVcTitle;

        public FundListFragment init(List<FundBrief> funds) {
            Bundle arguments = new Bundle();
            if (funds instanceof Serializable) {
                arguments.putSerializable(KEY_FUND_LIST, (Serializable) funds);
            }
            setArguments(arguments);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mFunds = safeGet(() -> (List<FundBrief>) getArguments().getSerializable(KEY_FUND_LIST), null);
            mVcTitle = getArguments().getString(KEY_VC_TITLE, "组合列表");
            return inflater.inflate(R.layout.frag_fund_list, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));
            updateTitle(mVcTitle);

            if (mFunds.isEmpty()) {
                changeVisibleSection(TYPE_EMPTY);
            } else {
                LinearLayout listView = v_findView(view, android.R.id.list);
                FundCardViewHelper.resetFundCardListViewWithFundBrief(listView, mFunds, 0);
                changeVisibleSection(TYPE_CONTENT);
            }

        }
    }


    public static class AllFundFragment extends SimpleFragment implements CachedRecommendFundListGetter {
        public static final int FLAG_ALLOW_SWIPE_REFRESH = 1;

        private List<FundBrief> mPassedData;
        private List<FundBrief> mCachedData;
        private PublishSubject<Void> mSwitchCategorySubject = PublishSubject.create();
        private int mFlags;

        @Override
        public List<FundBrief> getCachedRecommendFundList() {
            return mCachedData;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mPassedData = safeGet(() -> (List<FundBrief>) getArguments().getSerializable(KEY_FUND_LIST), null);
            mCachedData = mPassedData;
            mFlags = safeGet(() -> getArguments().getInt(CommonProxyActivity.KEY_FLAGS_INT), 0);
            return inflater.inflate(R.layout.frag_more_fund, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            setOnSwipeRefreshListener(() -> {
                fetchData(false, false);
            });
            v_setClick(mReloadSection, v -> fetchData(true, false));
            mRefreshLayout.setEnabled(hasFlag(mFlags, FLAG_ALLOW_SWIPE_REFRESH));

            if (mPassedData != null) {
                resetContentSection(mPassedData);
            } else {
                fetchData(true, true);
            }
        }

        private void fetchData(boolean reload, boolean allowCache) {
            consumeEventMRUpdateUI(FundController.fetchRecommendFundList(allowCache), reload)
                    .onNextSuccess(callback -> {
                        mCachedData = callback.data;
                        resetContentSection(mCachedData);
                    })
                    .done();
        }

        private void resetContentSection(List<FundBrief> items) {
            Button classifyButton = v_findView(findToolbar(this), R.id.btn_classify);
            v_setVisible(mContentSection);
            v_setClick(classifyButton, v -> {
                int classify = mLastClassify;
                if (classify == CLASSIFY_BY_CATEGORY) {
                    mLastClassify = CLASSIFY_BY_STATUS;
                } else {
                    mLastClassify = CLASSIFY_BY_CATEGORY;
                }
                v_setText(classifyButton, mLastClassify == CLASSIFY_BY_STATUS ? "按类别" : "按状态");
                updateTabLayoutAndViewPager(mLastClassify, items);
            });
            updateTabLayoutAndViewPager(mLastClassify, items);
            v_setText(classifyButton, mLastClassify == CLASSIFY_BY_STATUS ? "按类别" : "按状态");
            v_setVisible(classifyButton);
        }


        private static final int CLASSIFY_BY_CATEGORY = 0;
        private static final int CLASSIFY_BY_STATUS = 1;
        private int mLastClassify = CLASSIFY_BY_STATUS;

        @SuppressWarnings("ConstantConditions")
        private void updateTabLayoutAndViewPager(int classify, List<FundBrief> funds) {
            ViewPager pager = v_findView(mContentSection, R.id.pager);
            String[] titles = generatePagerTitles(classify, funds);
            int[] classifies = generateClassies(classify);

            TabLayout tabLayout = v_findView(mContentSection, R.id.tabLayout);
            if (pager.getAdapter() != null) {
                AllFundFragmentAdapter adapter = (AllFundFragmentAdapter) pager.getAdapter();
                adapter.titles = titles;
                adapter.classifies = classifies;
                adapter.notifyDataSetChanged();
                mSwitchCategorySubject.onNext(null);
            } else {
                AllFundFragmentAdapter adapter = new AllFundFragmentAdapter(getChildFragmentManager());
                adapter.titles = titles;
                adapter.classifies = classifies;
                pager.setAdapter(adapter);
            }
            tabLayout.setupWithViewPager(pager);
        }

    }


    private static class AllFundFragmentAdapter extends FragmentPagerAdapter {
        private String[] titles;
        private int[] classifies;

        public AllFundFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new AllFundPageListFragment().init(position);
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    private static String[] generatePagerTitles(int classify, List<FundBrief> funds) {
        Observable<FundBrief> observable = Observable.from(funds);

        String[] titles = new String[3];
        if (classify == AllFundFragment.CLASSIFY_BY_CATEGORY) {
            titles[0] = "操盘乐 " + observable.filter(it -> anyMatch(it.type, Fund_Type.Porfolio)).count().toBlocking().first();
            titles[1] = "分红乐  " + observable.filter(it -> anyMatch(it.type, Fund_Type.Bonus)).count().toBlocking().first();
            titles[2] = "公益乐 " + observable.filter(it -> anyMatch(it.type, Fund_Type.Charitable)).count().toBlocking().first();
        } else {
            titles[0] = "投资中 " + observable.filter(it -> anyMatch(it.status, Fund_Status.Capital, Fund_Status.Booking)).count().toBlocking().first();
            titles[1] = "运行中 " + observable.filter(it -> anyMatch(it.status, Fund_Status.LockIn)).count().toBlocking().first();
            titles[2] = "已结算  " + observable.filter(it -> anyMatch(it.status, Fund_Status.Stop)).count().toBlocking().first();
        }
        return titles;
    }

    private static int[] generateClassies(int classify) {
        if (classify == AllFundFragment.CLASSIFY_BY_CATEGORY) {
            return new int[]{AllFundPageListFragment.CLASSIFY_BY_NORMAL_FUND, AllFundPageListFragment.CLASSIFY_BY_BONUS_FUND, AllFundPageListFragment.CLASSIFY_BY_CHARITY_FUND};
        } else {
            return new int[]{AllFundPageListFragment.CLASSIFY_BY_CAPITAL, AllFundPageListFragment.CLASSIFY_BY_LOCKED, AllFundPageListFragment.CLASSIFY_BY_STOP};
        }
    }

    private interface CachedRecommendFundListGetter {
        List<FundBrief> getCachedRecommendFundList();
    }

    public static Optional<PublishSubject<Void>> getSwitchCategorySubjectFromParent(AllFundPageListFragment fragment) {
        return safeGet(() -> {
                    AllFundFragment parent = (AllFundFragment) fragment.getParentFragment();
                    return opt(parent.mSwitchCategorySubject);
                },
                Optional.<PublishSubject<Void>>empty());
    }

    @SuppressWarnings("ConstantConditions")
    public static int getClassifyFromParent(AllFundPageListFragment fragment) {
        return safeGet(() -> {
            AllFundFragment parent = (AllFundFragment) fragment.getParentFragment();
            ViewPager pager = v_findView(parent, R.id.pager);
            AllFundFragmentAdapter adapter = (AllFundFragmentAdapter) pager.getAdapter();
            return adapter.classifies[fragment.mTabIdx];
        }, 0);
    }

    public static class AllFundPageListFragment extends SimpleFragment {

        private static final int CLASSIFY_BY_CAPITAL = 0;   //投资中
        private static final int CLASSIFY_BY_LOCKED = 1;    //运行中
        private static final int CLASSIFY_BY_STOP = 2;    //已结算
        private static final int CLASSIFY_BY_NORMAL_FUND = 3;   //操盘乐
        private static final int CLASSIFY_BY_BONUS_FUND = 4;    //分红乐
        private static final int CLASSIFY_BY_CHARITY_FUND = 5;  //公益乐

        private static SparseArray<Func1<FundBrief, Boolean>> FILTER_FUNC_MAP = new SparseArray<>();

        static {
            FILTER_FUNC_MAP.append(CLASSIFY_BY_CAPITAL, it -> anyMatch(it.status, Capital, Booking));
            FILTER_FUNC_MAP.append(CLASSIFY_BY_LOCKED, it -> anyMatch(it.status, LockIn));
            FILTER_FUNC_MAP.append(CLASSIFY_BY_STOP, it -> anyMatch(it.status, Stop));
            FILTER_FUNC_MAP.append(CLASSIFY_BY_NORMAL_FUND, it -> anyMatch(it.type, Fund_Type.Porfolio));
            FILTER_FUNC_MAP.append(CLASSIFY_BY_BONUS_FUND, it -> anyMatch(it.type, Fund_Type.Bonus));
            FILTER_FUNC_MAP.append(CLASSIFY_BY_CHARITY_FUND, it -> anyMatch(it.type, Fund_Type.Charitable));
        }

        private int mTabIdx;

        public AllFundPageListFragment init(int tabIdx) {
            Bundle arguments = new Bundle();
            arguments.putInt(CommonProxyActivity.KEY_TAB_IDX_INT, tabIdx);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mTabIdx = getArguments().getInt(CommonProxyActivity.KEY_TAB_IDX_INT);
            return inflater.inflate(R.layout.frag_more_fund_page_list, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getSwitchCategorySubjectFromParent(this)
                    .consume(subject -> {
                        consumeEvent(subject)
                                .setTag("on_switch_category")
                                .onNextFinish(ignored -> {
                                    if (getView() != null) {
                                        resetContentSection();
                                    }
                                })
                                .done();
                    });

            runOnUIThreadDelayed(() -> {
                if (getUserVisibleHint()) {
                    setUserVisibleHint(true);
                }
            }, 100L);
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                resetContentSection();
            }
        }

        private void resetContentSection() {
            boolean isFundEmpty = true;
            Fragment parent = getParentFragment();
            if (parent != null && parent instanceof CachedRecommendFundListGetter) {
                CachedRecommendFundListGetter getter = (CachedRecommendFundListGetter) parent;
                int classify = getClassifyFromParent(this);
                List<FundBrief> funds = safeGet(() -> Stream.of(getter.getCachedRecommendFundList()).filter(it -> FILTER_FUNC_MAP.get(classify).call(it)).collect(Collectors.toList()), Collections.<FundBrief>emptyList());
                if (!funds.isEmpty()) {
                    isFundEmpty = false;
                    LinearLayout container = v_findView(mContentSection, R.id.list);
                    FundCardViewHelper.resetFundCardListViewWithFundBrief(container, funds, 0);
                    consumeEvent(FundCardViewHelper.createObservableUpdateListTimer(new WeakReference<>(this), container, 0))
                            .setTag("update_list_timer")
                            .done();
                    changeVisibleSection(TYPE_CONTENT);
                } else {
                    unsubscribeFromMain("update_list_timer");
                }
            }

            if (isFundEmpty) {

                String tempStr = "暂无此类组合";
                switch (getClassifyFromParent(this)) {
                    case CLASSIFY_BY_CAPITAL:
                        tempStr = "暂无投资中组合";
                        break;
                    case CLASSIFY_BY_LOCKED:
                        tempStr = "暂无运行中组合";
                        break;
                    case CLASSIFY_BY_STOP:
                        tempStr = "暂无已结算组合";
                        break;
                    case CLASSIFY_BY_NORMAL_FUND:
                        tempStr = "暂无操盘乐组合";
                        break;
                    case CLASSIFY_BY_BONUS_FUND:
                        tempStr = "暂无分红乐组合";
                        break;
                    case CLASSIFY_BY_CHARITY_FUND:
                        tempStr = "暂无公益乐组合";
                        break;

                }
                setEmptySectionTips(tempStr, "");
                changeVisibleSection(TYPE_EMPTY);
            }
        }
    }

}
