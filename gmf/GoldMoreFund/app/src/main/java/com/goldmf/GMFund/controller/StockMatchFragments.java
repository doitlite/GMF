package com.goldmf.GMFund.controller;

import android.content.Context;
import android.graphics.Rect;
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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.drawee.view.SimpleDraweeView;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.WebViewFragments.WebViewFragmentDelegate;
import com.goldmf.GMFund.controller.business.StockController;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.controller.dialog.StockMatchExchangeDialog;
import com.goldmf.GMFund.controller.protocol.UMShareHandlerProtocol;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.CommonDefine.PlaceHolder;
import com.goldmf.GMFund.model.GMFMatch;
import com.goldmf.GMFund.model.GMFRankUser;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.PageArrayHelper;
import com.goldmf.GMFund.util.SecondUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.goldmf.GMFund.widget.GMFWebview;
import com.goldmf.GMFund.widget.UserAvatarView;
import com.yale.ui.support.AdvanceSwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import rx.functions.Action1;
import rx.subjects.PublishSubject;
import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_STOCK_COMPETE_ID_STRING;
import static com.goldmf.GMFund.controller.business.MatchController.refreshStockMatch;
import static com.goldmf.GMFund.controller.business.MatchController.refreshStockMatchDetail;
import static com.goldmf.GMFund.controller.business.MatchController.refreshStockMatchWithMine;
import static com.goldmf.GMFund.controller.business.MatchController.signupMatch;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_LoginPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_OpenSimulationAccountPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockMatchDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockTradePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_UserDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WebViewPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BUY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.SELL_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getIncomeTextColor;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addCellVerticalSpacing;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addContentInset;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addHorizontalSepLine;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.getSimpleAdapter;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.isScrollToBottom;
import static com.goldmf.GMFund.extension.SimulationAccountExtension.isNeedToCheckSimulationAccountState;
import static com.goldmf.GMFund.extension.SimulationAccountExtension.isOpenedSimulationAccount;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.createAlertDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_isVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageResource;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setOnRefreshing;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setTextWithFitBound;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.model.GMFMatch.STATE_ING;
import static com.goldmf.GMFund.model.GMFMatch.STATE_OVER;
import static com.goldmf.GMFund.model.GMFMatch.STATE_SIGNUP;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.getNextPage;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.hasData;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.hasMoreData;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.isEmpty;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatRatio;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;
import static com.goldmf.GMFund.util.FormatUtil.formateRemainingDays;

/**
 * Created by Evan on 16/3/8 下午5:37.
 */
public class StockMatchFragments {

    public static class StockMatchListPageAllFragment extends SimpleFragment {

        private RecyclerView mRecyclerView;
        private boolean mDataExpired = true;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_stock_match_list_page_all, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            v_setClick(mReloadSection, v -> fetchData(true));

            changeVisibleSection(TYPE_LOADING);
            mRecyclerView = v_findView(this, R.id.recyclerView);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            int spacing = dp2px(10);
            addContentInset(mRecyclerView, new Rect(spacing, 0, spacing, 0));
            addCellVerticalSpacing(mRecyclerView, spacing, 0, 1);
            setOnSwipeRefreshListener(() -> fetchData(false));

            consumeEvent(NotificationCenter.signUpStockMatchSuccessSubject)
                    .setTag("data_expired")
                    .onNextFinish(response -> {
                        mDataExpired = true;
                        if (getView() != null && getUserVisibleHint()) {
                            fetchData(false);
                            mDataExpired = false;
                        }
                    })
                    .done();


            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
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
                    boolean isReload = !v_isVisible(mContentSection);
                    fetchData(isReload);
                    mDataExpired = false;
                }
            }
        }

        private void fetchData(boolean reload) {
            unsubscribeFromMain("get_next");

            consumeEventMRUpdateUI(refreshStockMatch(), reload)
                    .setTag("reload_data")
                    .onNextSuccess(response -> {
                        CommandPageArray<GMFMatch> pageArray = response.data;
                        if (pageArray.data().isEmpty()) {
                            v_setVisible(mEmptySection);
                        } else {
                            updateSectionContent(pageArray);
                            v_setVisible(mContentSection);
                        }
                    })
                    .done();

        }


        boolean mIsLoadingMore = false;

        private void fetchMoreData(CommandPageArray<GMFMatch> pageArray) {
            if (!mIsLoadingMore && hasMoreData(pageArray)) {
                consumeEventMRUpdateUI(PageArrayHelper.getNextPage(pageArray), false)
                        .setTag("getNext")
                        .onNextSuccess(response -> updateSectionContent(response.data))
                        .onNextFinish(response -> mIsLoadingMore = false)
                        .done();
            }
        }

        @SuppressWarnings("unchecked")
        private void updateSectionContent(CommandPageArray<GMFMatch> pageArray) {
            List<StockMatchInfo> data = Stream.of(opt(pageArray).let(it -> it.data()).or(Collections.emptyList())).map(StockMatchInfo::new).collect(Collectors.toList());

            if (mRecyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<StockMatchInfo> adapter = getSimpleAdapter(mRecyclerView);
                adapter.resetItems(data);
            } else {
                SimpleRecyclerViewAdapter<StockMatchInfo> adapter = new SimpleRecyclerViewAdapter.Builder<>(data)
                        .onCreateItemView(R.layout.cell_stock_match_list_page_all)
                        .onCreateViewHolder(builder -> {
                            builder.bindChildWithTag("coverImage", R.id.img_cover)
                                    .bindChildWithTag("signUpLabel", R.id.label_signUp)
                                    .bindChildWithTag("matchNameLabel", R.id.label_match_name)
                                    .bindChildWithTag("matchAwardLabel", R.id.label_match_award)
                                    .bindChildWithTag("matchStateLabel", R.id.label_match_state)
                                    .bindChildWithTag("matchBriefLabel", R.id.label_match_brief)
                                    .bindChildWithTag("stateImage", R.id.img_match_state)
                                    .configureView((item, pos) -> {
                                        if (item.matchState == STATE_SIGNUP || item.matchState == STATE_ING) {
                                            v_setImageUri(builder.getChildWithTag("coverImage"), item.coverURL);
                                            v_setVisible(builder.getChildWithTag("coverImage"));
                                            builder.getChildWithTag("signUpLabel").setVisibility(item.hasSignUp ? View.VISIBLE : View.GONE);
                                        } else if (item.matchState == STATE_OVER) {
                                            v_setGone(builder.getChildWithTag("coverImage"));
                                            v_setGone(builder.getChildWithTag("signUpLabel"));

                                        }
                                        v_setImageResource(builder.getChildWithTag("stateImage"), item.resId);
                                        v_setText(builder.getChildWithTag("matchNameLabel"), item.matchName);
                                        v_setText(builder.getChildWithTag("matchAwardLabel"), item.matchAward);
                                        v_setText(builder.getChildWithTag("matchStateLabel"), item.matchTime);
                                        v_setText(builder.getChildWithTag("matchBriefLabel"), item.awardDesc);
                                    });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            v_setClick(holder.itemView, v -> {
                                StockMatchInfo item = ad.getItem(holder.getAdapterPosition());
                                showActivity(this, an_StockMatchDetailPage(item.matchID));
                            });
                        })
                        .create();
                adapter.addFooter(adapter.createFooterView(getActivity(), R.layout.footer_loading_more));
                mRecyclerView.setAdapter(adapter);

                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (isScrollToBottom(recyclerView) && !mIsLoadingMore) {
                            fetchMoreData(pageArray);
                            mIsLoadingMore = true;
                        }
                    }
                });
            }

            SimpleRecyclerViewAdapter<StockMatchInfo> adapter = getSimpleAdapter(mRecyclerView);
            View loadingMoreFooter = adapter.getFooter(0);
            loadingMoreFooter.setVisibility(hasMoreData(pageArray) ? View.VISIBLE : View.GONE);
        }
    }

    public static class StockMatchListPageMineFragment extends SimpleFragment {

        private RecyclerView mRecyclerView;
        private boolean mDataExpired = true;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_stock_match_list_page_mine, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            changeVisibleSection(TYPE_LOADING);
            mRecyclerView = v_findView(this, R.id.recyclerView);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            int spacing = dp2px(10);
            addCellVerticalSpacing(mRecyclerView, spacing, 0, 1);
            addContentInset(mRecyclerView, new Rect(spacing, 0, spacing, 0));

            v_setClick(mReloadSection, v -> fetchData(true));
            v_setOnRefreshing(mRefreshLayout, () -> fetchData(false));

            consumeEvent(NotificationCenter.signUpStockMatchSuccessSubject)
                    .setTag("data_expired")
                    .onNextFinish(response -> {
                        mDataExpired = true;
                        if (getUserVisibleHint() && getView() != null) {
                            fetchData(false);
                        }
                    })
                    .done();

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
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
                    boolean isReload = !v_isVisible(mContentSection);
                    fetchData(isReload);
                    mDataExpired = false;
                }
            }
        }

        private void fetchData(boolean reload) {
            unsubscribeFromMain("get_next");
            consumeEventMRUpdateUI(refreshStockMatchWithMine(), reload)
                    .setTag("reload_data")
                    .onNextSuccess(response -> {
                        CommandPageArray<GMFMatch> pageArray = response.data;
                        if (isEmpty(pageArray)) {
                            changeVisibleSection(TYPE_EMPTY);
                        } else {
                            updateSectionContent(pageArray);
                            changeVisibleSection(TYPE_CONTENT);
                        }
                    })
                    .done();

            mDataExpired = false;
        }

        private boolean isLoadingMore = false;

        private void fetchMoreData(CommandPageArray<GMFMatch> pageArray) {
            if (!isLoadingMore && hasMoreData(pageArray)) {
                consumeEventMR(getNextPage(pageArray))
                        .setTag("get_next")
                        .onNextSuccess(response -> {
                            if (hasData(response.data)) {
                                updateSectionContent(response.data);
                            }
                            isLoadingMore = false;
                        })
                        .done();
            }
        }

        private void updateSectionContent(CommandPageArray<GMFMatch> pageArray) {

            List<StockMatchInfo> data = Stream.of(pageArray.data()).map(it -> new StockMatchInfo(it)).collect(Collectors.toList());

            if (mRecyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<StockMatchInfo> adapter = getSimpleAdapter(mRecyclerView);
                adapter.resetItems(data);
            } else {
                SimpleRecyclerViewAdapter<StockMatchInfo> adapter = new SimpleRecyclerViewAdapter.Builder<>(data)
                        .onCreateItemView(R.layout.cell_stock_match_list_page_mine)
                        .onCreateViewHolder(builder -> {
                            builder.bindChildWithTag("nameLabel", R.id.label_match_name)
                                    .bindChildWithTag("stateLabel", R.id.label_match_state)
                                    .bindChildWithTag("rankLabel", R.id.label_match_rank)
                                    .bindChildWithTag("briefLabel", R.id.label_match_brief)
                                    .bindChildWithTag("stateImage", R.id.img_match_state)
                                    .configureView((item, pos) -> {
                                        v_setText(builder.getChildWithTag("nameLabel"), item.matchName);
                                        v_setText(builder.getChildWithTag("stateLabel"), item.matchTime);
                                        v_setImageResource(builder.getChildWithTag("stateImage"), item.resId);
                                        if (anyMatch(item.matchState, STATE_SIGNUP)) {
                                            v_setText(builder.getChildWithTag("rankLabel"), item.matchAward);
                                            v_setText(builder.getChildWithTag("briefLabel"), item.awardDesc);
                                        } else {
                                            v_setText(builder.getChildWithTag("rankLabel"), item.rankPosition);
                                            v_setText(builder.getChildWithTag("briefLabel"), item.rankDescription);
                                        }
                                    });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            v_setClick(holder.itemView, v -> {
                                StockMatchInfo item = ad.getItem(holder.getAdapterPosition());
                                showActivity(this, an_StockMatchDetailPage(item.matchID));
                            });
                        })
                        .create();
                adapter.addFooter(adapter.createFooterView(getActivity(), R.layout.footer_loading_more));
                mRecyclerView.setAdapter(adapter);

                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (isScrollToBottom(recyclerView) && !isLoadingMore) {
                            fetchMoreData(pageArray);
                            isLoadingMore = true;
                        }
                    }
                });
            }

            SimpleRecyclerViewAdapter<StockMatchInfo> adapter = getSimpleAdapter(mRecyclerView);
            View loadingMoreFooter = adapter.getFooter(0);
            loadingMoreFooter.setVisibility(hasMoreData(pageArray) ? View.VISIBLE : View.GONE);
        }
    }

    public static class StockMatchInfo {

        private String tarlink;
        private String pageUrl;
        private String matchID;
        private int matchState;
        public boolean hasSignUp;
        private String coverURL;
        public CharSequence matchName;
        private CharSequence stateDesc;
        public CharSequence matchAward;
        public CharSequence awardDesc;
        private CharSequence rankPosition;
        private CharSequence rankDescription;
        private GMFRankUser rankUser;

        public CharSequence startTime;
        public CharSequence stopTime;
        public CharSequence matchTime;
        public int resId;

        private StockMatchInfo(GMFMatch data) {
            matchID = data.mid;
            matchState = data.state;
            hasSignUp = data.bSignUp;
            coverURL = data.imgUrl;
            matchName = setColor(data.title, data.state != STATE_OVER ? TEXT_BLACK_COLOR : TEXT_GREY_COLOR);
            stateDesc = setColor(data.stateDesc, TEXT_GREY_COLOR);
            matchAward = setColor(data.maxAward, TEXT_RED_COLOR);
            awardDesc = setColor(data.maxAwardDesc, TEXT_GREY_COLOR);
            tarlink = data.tarLink;
            pageUrl = data.pageUrl;
            if (data.result != null) {
                rankPosition = setColor("" + data.result.position, TEXT_RED_COLOR);
                rankUser = data.result;
            }
            rankDescription = setColor("排名", TEXT_GREY_COLOR);

            startTime = safeGet(() -> formatSecond(data.startTime, "MM月dd日"), "--");
            stopTime = safeGet(() -> formatSecond(data.stopTime, "MM月dd日"), "--");
            matchTime = concatNoBreak(setFontSize(setColor(startTime + " ~ " + stopTime, TEXT_GREY_COLOR), sp2px(10)));

            if (data.state == STATE_SIGNUP) {
                resId = R.mipmap.ic_sign_up;
            } else if (data.state == STATE_ING) {
                resId = R.mipmap.ic_sign_in;
            } else {
                resId = R.mipmap.ic_over;
            }
        }
    }


    public static PublishSubject<StockMatchDetailVM> sStockMatchDetailChangeSubject = PublishSubject.create();

    public static class StockMatchDetailFragment2 extends SimpleFragment {

        private String mMatchId;
        private boolean mDataExpired = true;
        private boolean isFirstEnter = true;
        private StockMatchDetailVM mVm;

        public StockMatchDetailFragment2 init(String matchId) {
            Bundle arguments = new Bundle();
            arguments.putString(KEY_STOCK_COMPETE_ID_STRING, matchId);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mMatchId = getArguments().getString(KEY_STOCK_COMPETE_ID_STRING, "");
            return inflater.inflate(R.layout.frag_stock_match_detail2, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            setOnSwipeRefreshListener(() -> fetchData(false));
            v_setClick(mReloadSection, v -> fetchData(true));


            consumeEvent(NotificationCenter.loginSubject)
                    .setTag("user_login")
                    .onNextFinish(ignored -> {
                        mDataExpired = true;
                        if (getUserVisibleHint() && getView() != null) {
                            fetchData(false);
                            mDataExpired = false;
                        }
                    })
                    .done();

            consumeEvent(NotificationCenter.signUpStockMatchSuccessSubject)
                    .setTag("data_expired")
                    .onNextFinish(response -> {
                        mDataExpired = true;
                        if (getView() != null && getUserVisibleHint()) {
                            fetchData(false);
                            mDataExpired = false;
                        }
                    })
                    .done();

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
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
                    fetchData(true);
                    mDataExpired = false;
                }
            }
        }

        private void fetchData(boolean reload) {
            if (reload) {
                changeVisibleSection(TYPE_LOADING);
            } else {
                setSwipeRefreshing(true);
            }

            consumeEventMRUpdateUI(refreshStockMatchDetail(mMatchId), reload)
                    .setTag("reload_data")
                    .onNextSuccess(response -> {
                        if (response.data != null) {
                            mVm = new StockMatchDetailVM(response.data);
                            updateContent(mVm);
                            changeVisibleSection(TYPE_CONTENT);
                        }
                    })
                    .done();
        }

        private void updateContent(StockMatchDetailVM vm) {
            updateTitle(vm.matchName);
            v_setClick(this, R.id.btn_share, v -> showActivity(this, an_WebViewPage(vm.shareURL)));
            if (MineManager.getInstance().isLoginOK() && vm.hasSignUp) {
                v_setVisible(this, R.id.btn_share);
            } else {
                v_setGone(this, R.id.btn_share);
            }


            v_setImageResource(getView(), R.id.img_match_state, vm.resId);

            TabLayout tabLayout = v_findView(this, R.id.tabLayout);
            ViewPager pager = v_findView(this, R.id.pager);
            if (pager.getAdapter() == null) {
                StockMatchDetailAdapter adapter = new StockMatchDetailAdapter(getChildFragmentManager());
                pager.setAdapter(adapter);
                tabLayout.setupWithViewPager(pager);
            } else {
                sStockMatchDetailChangeSubject.onNext(vm);
            }
            if (isFirstEnter) {
                pager.setCurrentItem(vm.matchState == STATE_SIGNUP ? 0 : 1);
                isFirstEnter = false;
            }
        }
    }

    public static class StockMatchDetailAdapter extends FragmentPagerAdapter {

        public StockMatchDetailAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment ret = null;
            if (position == 0)
                ret = new StockMatchRuleFragment();
            else if (position == 1)
                ret = new StockMatchRankFragment();
            return ret;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return "比赛介绍";
            else if (position == 1)
                return "排行榜";
            return null;
        }
    }

    public static class StockMatchRuleFragment extends SimpleFragment {

        private StockMatchDetailVM mVM;
        private boolean mDataExpired = true;
        private WebViewFragmentDelegate mWebViewDelegate;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_stock_match_rule, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mWebViewDelegate = new WebViewFragmentDelegate(this, v_findView(this, R.id.webView), true);

            consumeEvent(sStockMatchDetailChangeSubject)
                    .setTag("reload_data")
                    .onNextFinish(vm -> {
                        updateContent(vm);
                    })
                    .done();

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
            mWebViewDelegate.onViewCreated("");
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mDataExpired = true;
            mWebViewDelegate.onDestroyView();
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (mDataExpired) {
                    fetchData(true);
                    mDataExpired = false;
                }
            }
        }

        private void fetchData(boolean reload) {
            if (reload) {
                changeVisibleSection(TYPE_LOADING);
            }
            mVM = ((StockMatchDetailFragment2) getParentFragment()).mVm;
            updateContent(mVM);
            changeVisibleSection(TYPE_CONTENT);
        }

        private void updateContent(StockMatchDetailVM vm) {
            setupHeaderSection(vm);
            setupContentSection(vm);
            setupBottomSection(vm);
        }


        private void setupHeaderSection(StockMatchDetailVM vm) {

            View headerSection = v_findView(mContentSection, R.id.section_header);
            v_setImageUri(headerSection, R.id.img_cover, vm.coverURL);
            View stateHeader = v_findView(headerSection, R.id.header_match_state);
            v_setText(stateHeader, R.id.label_match_state, vm.matchTime);
            v_setText(stateHeader, R.id.label_signUp_number, vm.signUpCount);
        }

        private void setupContentSection(StockMatchDetailVM vm) {
            GMFWebview webView = v_findView(mContentSection, R.id.webView);
            webView.loadUrl(vm.matchRuleURL);
        }

        private void setupBottomSection(StockMatchDetailVM vm) {
            View bottomBar = v_findView(this, R.id.section_left_bottom);
            TextView bottomLabel = v_findView(bottomBar, R.id.label_bottom_brief);
            if (vm.hasSignUp && vm.matchState != STATE_OVER) {
                v_setText(bottomLabel, vm.stateBrief);
            } else {
                v_setGone(bottomLabel);
            }

            LinearLayout bottomSection = v_findView(bottomBar, R.id.section_bottom_button);
            bottomSection.removeAllViewsInLayout();

            if (anyMatch(vm.matchState, STATE_ING) && vm.hasSignUp) {
                bottomSection.addView(createTradeButton(bottomSection, vm));
            }

            if (anyMatch(vm.matchState, STATE_SIGNUP, STATE_ING) && !vm.hasSignUp) {
                bottomSection.addView(createSignUpButton(bottomSection, vm));
            } else {
                if (MineManager.getInstance().isLoginOK() && vm.hasSignUp)
                    bottomSection.addView(createShareButton(bottomSection, vm));
            }

            bottomSection.setVisibility(bottomSection.getChildCount() > 0 ? View.VISIBLE : View.GONE);
        }

        private View createSignUpButton(ViewGroup container, StockMatchDetailVM vm) {
            container.removeAllViewsInLayout();

            View view = createBottomButton(container.getContext(), button -> {
                button.setText(vm.bottomButtonText);
                button.setTextColor(getResources().getColor(vm.hasSignUp ? R.color.gmf_text_grey : R.color.gmf_text_black));
                button.setBackgroundResource(R.drawable.shape_round_button_yellow_normal);
            });
            v_setClick(view, v -> {
                if (!vm.hasSignUp) {
                    UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockGameSignUp);
                    performSignUp(v, vm);
                } else {
                    showToast(getActivity(), "您已经报名！");
                }
            });
            return view;
        }

        private void performSignUp(View view, StockMatchDetailVM vm) {
            boolean isLogin = MineManager.getInstance().isLoginOK();
            if (!isLogin) {
                showActivity(this, an_LoginPage());

                consumeEvent(NotificationCenter.loginSubject.limit(1))
                        .setTag("action_after_login")
                        .onNextFinish(ignored -> {
                            performSignUp(view, vm);
                        })
                        .done();

                consumeEvent(NotificationCenter.cancelLoginSubject.limit(1))
                        .setTag("action_after_cancel_login")
                        .onNextFinish(ignored -> {
                            unsubscribeFromMain("action_after_login");
                        })
                        .done();
                return;
            }

            if (isNeedToCheckSimulationAccountState()) {
                GMFProgressDialog dialog = new GMFProgressDialog(getActivity());
                dialog.setMessage("正在查询模拟是否开通模拟帐户...");
                dialog.show();

                consumeEventMR(StockController.fetchSimulationAccount(false))
                        .setTag("fetch_simulation_account_state")
                        .onNextFinish(ignored -> {
                            dialog.dismiss();
                            if (!isNeedToCheckSimulationAccountState()) {
                                performSignUp(view, vm);
                            }
                        })
                        .done();
                return;
            }

            if (!isOpenedSimulationAccount()) {
                showActivity(this, an_OpenSimulationAccountPage());
                consumeEvent(NotificationCenter.closeOpenSimulationPageSubject.limit(1))
                        .setTag("action_after_close_open_simulation_page")
                        .onNextFinish(ignored -> {
                            if (isOpenedSimulationAccount()) {
                                performSignUp(view, vm);
                            }
                        })
                        .done();
                return;
            }

            GMFProgressDialog progressDialog = new GMFProgressDialog(getActivity(), "正在报名...");
            progressDialog.show();

            consumeEventMR(signupMatch(vm.matchId))
                    .setTag("perform_sign_up")
                    .onNextStart(response -> {
                        progressDialog.dismiss();
                    })
                    .onNextSuccess(response -> {
                        GMFDialog.Builder builder = new GMFDialog.Builder(view.getContext());
                        builder.setMessage(response.msg);
                        builder.setPositiveButton("确定", (dialog, which) -> {
                            opt(vm.raw).consume(it -> it.editSignUp(true));
                            dialog.dismiss();
                            fetchData(false);
                            NotificationCenter.signUpStockMatchSuccessSubject.onNext(vm.matchId);
                        });
                        builder.create().show();
                    })
                    .onNextFail(response -> {
                        createAlertDialog(this, getErrorMessage(response)).show();
                    })
                    .done();
        }

        private View createTradeButton(ViewGroup container, StockMatchDetailVM vm) {
            View view = createBottomButton(container.getContext(), button -> {
                button.setBackgroundResource(R.drawable.sel_round_button_yellow_bg);
                button.setTextColor(getResources().getColor(R.color.sel_button_text_dark));
                button.setText("立即买卖");
            });
            v_setClick(view, v -> {
                showActivity(this, an_StockTradePage(-1, null, 1));
            });
            return view;
        }

        private View createShareButton(ViewGroup container, StockMatchDetailVM vm) {
            View view = createBottomButton(container.getContext(), button -> {
                button.setBackgroundResource(R.drawable.sel_round_button_red_bg);
                button.setTextColor(getResources().getColor(R.color.sel_button_text_light));
                button.setText(vm.bottomButtonText);
            });
            v_setClick(view, v -> {
                if (getActivity() instanceof UMShareHandlerProtocol) {

                    if (vm.matchState == GMFMatch.STATE_SIGNUP) {
                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockGameSignShare);
                    } else if (vm.matchState == GMFMatch.STATE_ING) {
                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockGameIngShare);
                    } else if (vm.matchState == GMFMatch.STATE_OVER) {
                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockGameEndShare);
                    }

                    //                    ((UMShareHandlerProtocol) getActivity()).onPerformShare(vm.shareInfo, null);
                    //打开h5网页
                    safeCall(() -> showActivity(this, an_WebViewPage(vm.shareURL)));
                }
            });
            return view;
        }

        private View createBottomButton(Context context, Action1<Button> afterInitAction) {
            Button labelView = new Button(context);
            labelView.setTextColor(getResources().getColor(R.color.gmf_text_black));
            labelView.setTextSize(14);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, getResources().getDimensionPixelSize(R.dimen.gmf_button_height_medium));
            params.weight = 1.0f;
            params.leftMargin = params.rightMargin = dp2px(5);
            labelView.setLayoutParams(params);
            afterInitAction.call(labelView);
            return labelView;
        }
    }

    public static class StockMatchRankFragment extends SimpleFragment {

        private RecyclerView mRecyclerView;
        private StockMatchDetailVM mVM;
        private boolean mDataExpired = true;


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_stock_match_rank, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            v_setClick(mReloadSection, v -> fetchData(true));
            mRecyclerView = v_findView(mContentSection, R.id.recyclerView);

            {
                AppBarLayout mAppBarLayout = v_findView(view, R.id.appBarLayout);
                mRefreshLayout = v_findView(getActivity(), R.id.refreshLayout);
                ((AdvanceSwipeRefreshLayout) mRefreshLayout).setOnPreInterceptTouchEventDelegate(ev -> mAppBarLayout.getTop() < 0);
            }

            {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                addHorizontalSepLine(mRecyclerView);
                addContentInset(mRecyclerView, new Rect(0, 0, 0, dp2px(48)));
            }

            consumeEvent(sStockMatchDetailChangeSubject)
                    .setTag("reload_data")
                    .onNextFinish(vm -> {
                        updateContent(vm);
                    })
                    .done();

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
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
                    fetchData(true);
                    mDataExpired = false;
                }
            }
        }

        private void fetchData(boolean reload) {
            if (reload) {
                changeVisibleSection(TYPE_LOADING);
            }
            mVM = safeGet(() -> ((StockMatchDetailFragment2) getParentFragment()).mVm, null);
            if (mVM != null) {
                updateContent(mVM);
            }
        }

        private void updateContent(StockMatchDetailVM vm) {
            if (vm.matchState == STATE_SIGNUP) {
                changeVisibleSection(TYPE_EMPTY);
                v_setText(mEmptySection, R.id.label_title, "比赛还未开始");
                return;
            }

            setupHeaderSection(vm);
            setupContentSection(vm);
            setupBottomSection(vm);
            changeVisibleSection(TYPE_CONTENT);
        }

        private void setupHeaderSection(StockMatchDetailVM vm) {

            View headerSection = v_findView(mContentSection, R.id.section_header);
            LinearLayout myRankHeader = v_findView(headerSection, R.id.header_my_rank);

            boolean hasRankInfo = vm.hasSignUp && vm.myRankInfo != null;
            if (hasRankInfo) {
                RankUserInfo myRankInfo = vm.myRankInfo;
                TextView rankLabel = v_findView(myRankHeader, R.id.label_rank);
                ImageView rankImage = v_findView(myRankHeader, R.id.img_rank);
                if (anyMatch(myRankInfo.position, 1, 2, 3)) {
                    rankLabel.setText("");
                    int backgroundRes = R.mipmap.ic_rank_1;
                    if (anyMatch(myRankInfo.position, 2))
                        backgroundRes = R.mipmap.ic_rank_2;
                    if (anyMatch(myRankInfo.position, 3))
                        backgroundRes = R.mipmap.ic_rank_3;
                    rankImage.setImageResource(backgroundRes);
                } else {
                    rankLabel.setText(setFontSize("" + myRankInfo.position, dp2px(myRankInfo.position >= 100 ? 16 : 20)));
                    rankImage.setImageResource(0);
                }

                UserAvatarView avatarView = v_findView(myRankHeader, R.id.img_avatar);
                avatarView.updateView(myRankInfo.avatarURL, myRankInfo.userType);

//                v_setText(myRankHeader, R.id.label_user_name, myRankInfo.name);
                v_setTextWithFitBound(myRankHeader, R.id.label_user_name, myRankInfo.name);
                v_setText(myRankHeader, R.id.label_user_rank, "第" + myRankInfo.position + "名");
                v_setText(myRankHeader, R.id.label_user_income_ratio, myRankInfo.incomeRatio);
                v_setText(myRankHeader, R.id.label_exchange_brief, myRankInfo.incomeAndExchange);
                v_setClick(myRankHeader, R.id.section_rank, v -> {
                    showActivity(this, an_UserDetailPage(null));
                });
                v_setClick(myRankHeader, R.id.section_exchange, v -> new StockMatchExchangeDialog(getActivity(), vm.myRankInfo.raw).show());
                v_setVisibility(myRankHeader, R.id.section_exchange, myRankInfo.hasIncomeAndExchange ? View.VISIBLE : View.GONE);
            }
            v_setVisibility(myRankHeader, hasRankInfo ? View.VISIBLE : View.GONE);
        }

        @SuppressWarnings("ConstantConditions")
        private void setupContentSection(StockMatchDetailVM vm) {

            if (mRecyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<RankUserInfo> adapter = getSimpleAdapter(mRecyclerView);
                adapter.resetItems(Stream.of(vm.userList).map(it -> new RankUserInfo(it)).collect(Collectors.toList()));
            } else {
                List<RankUserInfo> data = Stream.of(vm.userList).map(it -> new RankUserInfo(it)).collect(Collectors.toList());
                SimpleRecyclerViewAdapter<RankUserInfo> adapter = new SimpleRecyclerViewAdapter.Builder<>(data)
                        .onCreateItemView(R.layout.cell_rank_user)
                        .onCreateViewHolder(builder -> {
                            builder.bindChildWithTag("rankLabel", R.id.label_rank)
                                    .bindChildWithTag("rankImage", R.id.img_rank)
                                    .bindChildWithTag("avatarImage", R.id.img_avatar)
                                    .bindChildWithTag("nameLabel", R.id.label_user_name)
                                    .bindChildWithTag("briefLabel", R.id.label_match_brief)
                                    .bindChildWithTag("incomeRatioLabel", R.id.label_match_income_ratio)
                                    .configureView((item, pos) -> {
                                        TextView rankLabel = builder.getChildWithTag("rankLabel");
                                        ImageView rankImage = builder.getChildWithTag("rankImage");
                                        if (anyMatch(item.position, 1, 2, 3)) {
                                            rankLabel.setText("");
                                            int backgroundRes = R.mipmap.ic_rank_1;
                                            if (anyMatch(item.position, 2))
                                                backgroundRes = R.mipmap.ic_rank_2;
                                            if (anyMatch(item.position, 3))
                                                backgroundRes = R.mipmap.ic_rank_3;
                                            rankImage.setImageResource(backgroundRes);
                                        } else {
                                            rankLabel.setText(setFontSize("" + item.position, dp2px(item.position >= 100 ? 16 : 20)));
                                            rankImage.setImageResource(0);
                                        }

                                        UserAvatarView avatarImage = builder.getChildWithTag("avatarImage");
                                        avatarImage.updateView(item.avatarURL, item.userType);
                                        v_setTextWithFitBound(
                                                builder.getChildWithTag("nameLabel"), item.name);
                                        v_setText(builder.getChildWithTag("briefLabel"), item.lastFeed);
                                        v_setText(builder.getChildWithTag("incomeRatioLabel"), item.incomeRatio);
                                    });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            v_setClick(holder.itemView, v -> {
                                RankUserInfo user = ad.getItem(holder.getAdapterPosition());
                                User currentUser = safeGet(() -> user.raw.getUser(), null);
                                showActivity(this, an_UserDetailPage(currentUser));
                            });
                        })
                        .create();
                mRecyclerView.setAdapter(adapter);

                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                    }
                });

            }
        }

        private void setupBottomSection(StockMatchDetailVM vm) {
            View bottomBar = v_findView(this, R.id.section_left_bottom);
            TextView bottomLabel = v_findView(bottomBar, R.id.label_bottom_brief);
            if (vm.hasSignUp && vm.matchState != STATE_OVER) {
                v_setText(bottomLabel, vm.stateBrief);
            } else {
                v_setGone(bottomLabel);
            }
            LinearLayout bottomSection = v_findView(bottomBar, R.id.section_bottom_button);
            bottomSection.removeAllViewsInLayout();

            if (anyMatch(vm.matchState, STATE_ING) && vm.hasSignUp) {
                bottomSection.addView(createTradeButton(bottomSection, vm));
            }

            if (anyMatch(vm.matchState, STATE_SIGNUP, STATE_ING) && !vm.hasSignUp) {
                bottomSection.addView(createSignUpButton(bottomSection, vm));
            } else {
                if (MineManager.getInstance().isLoginOK() && vm.hasSignUp)
                    bottomSection.addView(createShareButton(bottomSection, vm));
            }

            bottomSection.setVisibility(bottomSection.getChildCount() > 0 ? View.VISIBLE : View.GONE);
        }

        private View createSignUpButton(ViewGroup container, StockMatchDetailVM vm) {
            container.removeAllViewsInLayout();

            View view = createBottomButton(container.getContext(), button -> {
                button.setText(vm.bottomButtonText);
                button.setTextColor(getResources().getColor(vm.hasSignUp ? R.color.gmf_text_grey : R.color.gmf_text_black));
                button.setBackgroundResource(R.drawable.shape_round_button_yellow_normal);
            });
            v_setClick(view, v -> {
                if (!vm.hasSignUp) {
                    UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockGameSignUp);
                    performSignUp(v, vm);
                } else {
                    showToast(getActivity(), "您已经报名！");
                }
            });
            return view;
        }

        private void performSignUp(View view, StockMatchDetailVM vm) {
            boolean isLogin = MineManager.getInstance().isLoginOK();
            if (!isLogin) {
                showActivity(this, an_LoginPage());

                consumeEvent(NotificationCenter.loginSubject.limit(1))
                        .setTag("action_after_login")
                        .onNextFinish(ignored -> {
                            performSignUp(view, vm);
                        })
                        .done();

                consumeEvent(NotificationCenter.cancelLoginSubject.limit(1))
                        .setTag("action_after_cancel_login")
                        .onNextFinish(ignored -> {
                            unsubscribeFromMain("action_after_login");
                        })
                        .done();
                return;
            }

            if (isNeedToCheckSimulationAccountState()) {
                GMFProgressDialog dialog = new GMFProgressDialog(getActivity());
                dialog.setMessage("正在查询模拟是否开通模拟帐户...");
                dialog.show();

                consumeEventMR(StockController.fetchSimulationAccount(false))
                        .setTag("fetch_simulation_account_state")
                        .onNextFinish(ignored -> {
                            dialog.dismiss();
                            if (!isNeedToCheckSimulationAccountState()) {
                                performSignUp(view, vm);
                            }
                        })
                        .done();
                return;
            }

            if (!isOpenedSimulationAccount()) {
                showActivity(this, an_OpenSimulationAccountPage());
                consumeEvent(NotificationCenter.closeOpenSimulationPageSubject.limit(1))
                        .setTag("action_after_close_open_simulation_page")
                        .onNextFinish(ignored -> {
                            if (isOpenedSimulationAccount()) {
                                performSignUp(view, vm);
                            }
                        })
                        .done();
                return;
            }

            GMFProgressDialog progressDialog = new GMFProgressDialog(getActivity(), "正在报名...");
            progressDialog.show();

            consumeEventMR(signupMatch(vm.matchId))
                    .setTag("perform_sign_up")
                    .onNextStart(response -> {
                        progressDialog.dismiss();
                    })
                    .onNextSuccess(response -> {
                        GMFDialog.Builder builder = new GMFDialog.Builder(view.getContext());
                        builder.setMessage(response.msg);
                        builder.setPositiveButton("确定", (dialog, which) -> {
                            opt(vm.raw).consume(it -> it.editSignUp(true));
                            dialog.dismiss();
                            fetchData(false);
                            NotificationCenter.signUpStockMatchSuccessSubject.onNext(vm.matchId);
                        });
                        builder.create().show();
                    })
                    .onNextFail(response -> {
                        createAlertDialog(this, getErrorMessage(response)).show();
                    })
                    .done();
        }

        private View createTradeButton(ViewGroup container, StockMatchDetailVM vm) {
            View view = createBottomButton(container.getContext(), button -> {
                button.setBackgroundResource(R.drawable.sel_round_button_yellow_bg);
                button.setTextColor(getResources().getColor(R.color.sel_button_text_dark));
                button.setText("立即买卖");
            });
            v_setClick(view, v -> {
                showActivity(this, an_StockTradePage(-1, null, 1));
            });
            return view;
        }

        private View createShareButton(ViewGroup container, StockMatchDetailVM vm) {
            View view = createBottomButton(container.getContext(), button -> {
                button.setBackgroundResource(R.drawable.sel_round_button_red_bg);
                button.setTextColor(getResources().getColor(R.color.sel_button_text_light));
                button.setText(vm.bottomButtonText);
            });
            v_setClick(view, v -> {
                if (getActivity() instanceof UMShareHandlerProtocol) {

                    if (vm.matchState == GMFMatch.STATE_SIGNUP) {
                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockGameSignShare);
                    } else if (vm.matchState == GMFMatch.STATE_ING) {
                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockGameIngShare);
                    } else if (vm.matchState == GMFMatch.STATE_OVER) {
                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockGameEndShare);
                    }

                    //                    ((UMShareHandlerProtocol) getActivity()).onPerformShare(vm.shareInfo, null);
                    //打开h5网页
                    String shareURL = vm.shareURL;
                    safeCall(() -> showActivity(this, an_WebViewPage(shareURL)));
                }
            });
            return view;
        }

        private View createBottomButton(Context context, Action1<Button> afterInitAction) {
            Button labelView = new Button(context);
            labelView.setTextColor(getResources().getColor(R.color.gmf_text_black));
            labelView.setTextSize(14);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, getResources().getDimensionPixelSize(R.dimen.gmf_button_height_medium));
            params.weight = 1.0f;
            params.leftMargin = params.rightMargin = dp2px(5);
            labelView.setLayoutParams(params);
            afterInitAction.call(labelView);
            return labelView;
        }

    }

    private static class StockMatchDetailVM {
        public GMFMatch raw;
        public String matchId;
        public int matchState;
        public boolean hasSignUp;
        public String matchRuleURL;
        public String coverURL;
        public CharSequence matchName;
        public CharSequence stateDescription;
        public CharSequence signUpCount;
        public CharSequence stateBrief;

        public CharSequence startTime;
        public CharSequence stopTime;
        public CharSequence matchTime;
        public CharSequence remainOpenTime;

        @Nullable
        public RankUserInfo myRankInfo;
        public List<GMFRankUser> userList;

        public CharSequence bottomButtonText;

        public ShareInfo shareInfo;
        public String shareURL;
        public int resId;

        private StockMatchDetailVM(GMFMatch data) {
            raw = data;
            matchId = safeGet(() -> data.mid, "");
            matchState = safeGet(() -> data.state, STATE_OVER);
            hasSignUp = safeGet(() -> data.bSignUp, Boolean.FALSE);

            startTime = safeGet(() -> formatSecond(data.startTime, "MM月dd日"), "--");
            stopTime = safeGet(() -> formatSecond(data.stopTime, "MM月dd日"), "--");
            matchTime = concatNoBreak("比赛时间：", startTime, " ~ ", stopTime);
            remainOpenTime = concatNoBreak(safeGet(() -> formateRemainingDays((data.startTime - SecondUtil.currentSecond()) * 1f / (24 * 60 * 60)), ""), "后开赛");

            matchRuleURL = safeGet(() -> data.pageUrl, "");
            coverURL = safeGet(() -> data.imgUrl, "");
            matchName = safeGet(() -> data.title, PlaceHolder.NULL_VALUE);
            stateDescription = safeGet(() -> setColor(data.stateDesc, TEXT_GREY_COLOR), setColor(PlaceHolder.NULL_VALUE, TEXT_GREY_COLOR));
            signUpCount = safeGet(() -> setColor(data.count + (data.state == STATE_SIGNUP ? "人已报名" : "人已参赛"), TEXT_GREY_COLOR), setColor(PlaceHolder.NULL_VALUE, TEXT_GREY_COLOR));

            boolean hasRankInfo = safeGet(() -> data.result != null && data.result.position >= 1, false);
            if (hasRankInfo) {
                myRankInfo = new RankUserInfo(data.result);
                safeCall(() -> {
                    Mine mine = MineManager.getInstance().getmMe();
                    myRankInfo.userID = String.valueOf(mine.index);
                    myRankInfo.avatarURL = mine.getPhotoUrl();
                    myRankInfo.name = mine.getName();
                });
            } else {
                myRankInfo = null;
            }

            shareInfo = safeGet(() -> data.shareInfo, null);
            shareURL = safeGet(() -> data.shareURL, "");

            userList = safeGet(() -> data.userList).def(Collections.emptyList()).get();

            if (anyMatch(matchState, STATE_SIGNUP, STATE_ING) && !hasSignUp) {
                bottomButtonText = "我要报名";
            } else {
                bottomButtonText = TextUtils.isEmpty(data.shareButtonName) ? "分享" : data.shareButtonName;
            }

            if (anyMatch(matchState, STATE_SIGNUP) && hasSignUp) {
                stateBrief = concatNoBreak("我已报名 · ", remainOpenTime);
            } else if (anyMatch(matchState, STATE_ING)) {
                if (!hasSignUp) {
                    stateBrief = concatNoBreak("进行中 · ");
                } else {
                    if (hasRankInfo) {
                        {
                            stateBrief = concatNoBreak("进行中 · 第" + data.result.position + "名 · ",
                                    safeGet(() -> {
                                        Double value = Double.valueOf(data.result.point.value);
                                        return setColor(formatRatio(value, false, 0, 2), getIncomeTextColor(value));
                                    }).def(setColor(PlaceHolder.NULL_VALUE, TEXT_GREY_COLOR)).get());
                        }
                    }
                }
            } else if (anyMatch(matchState, STATE_OVER)) {
                if (!hasSignUp) {
                    stateBrief = concatNoBreak("已结束 · ");
                } else {
                    if (hasRankInfo) {
                        {
                            stateBrief = concatNoBreak("已结束 · 第" + data.result.position + "名 · ",
                                    safeGet(() -> {
                                        Double value = Double.valueOf(data.result.point.value);
                                        return setColor(formatRatio(value, false, 0, 2), getIncomeTextColor(value));
                                    }).def(setColor(PlaceHolder.NULL_VALUE, TEXT_GREY_COLOR)).get());
                        }
                    }
                }
            }

            if (data.state == STATE_SIGNUP) {
                resId = R.mipmap.ic_sign_up;
            } else if (data.state == STATE_ING) {
                resId = R.mipmap.ic_sign_in;
            } else {
                resId = R.mipmap.ic_over;
            }
        }
    }

    private static class RankUserInfo {
        private GMFRankUser.UserExchange mExchange;
        @Nullable
        public GMFRankUser raw;
        public String userID;
        public int userType;
        public int position;
        public String avatarURL;
        public CharSequence name;
        public CharSequence lastFeed;
        public CharSequence incomeRatio;
        public boolean hasIncomeAndExchange;
        public CharSequence incomeAndExchange;

        public RankUserInfo(GMFRankUser raw) {
            this.raw = raw;
            this.userID = safeGet(() -> String.valueOf(raw.getUser().index), "");
            this.position = safeGet(() -> raw.position, -1);
            this.avatarURL = safeGet(() -> raw.getUser().getPhotoUrl(), "");
            this.name = safeGet(() -> raw.getUser().getName(), PlaceHolder.NULL_VALUE);
            this.userType = safeGet(() -> raw.getUser().type, 0);

            CharSequence defaultLastFeed = "第" + position + "名";
            this.lastFeed = safeGet(() -> {
                if (TextUtils.isEmpty(raw.lastAction.desc)) {
                    return defaultLastFeed;
                }

                String typeText = anyMatch(raw.lastAction.type, GMFRankUser.UserAction.TYPE_BUY) ? "买入" : "卖出";
                int typeColor = anyMatch(raw.lastAction.type, GMFRankUser.UserAction.TYPE_BUY) ? BUY_COLOR : SELL_COLOR;
                return concatNoBreak(formatSecond(raw.lastAction.time, "yyyy/MM/dd"), " ", setColor(typeText, typeColor), " ", raw.lastAction.desc);
            }).def(defaultLastFeed).get();

            this.incomeRatio = safeGet(() -> {
                Double value = Double.valueOf(raw.point.value);
                return setColor(formatRatio(value, false, 2), getIncomeTextColor(value));
            }).def(setColor(PlaceHolder.NULL_VALUE, TEXT_GREY_COLOR)).get();

            this.hasIncomeAndExchange = safeGet(() -> Double.valueOf(raw.exchange.income) != null, false);
            this.incomeAndExchange = safeGet(() -> (CharSequence) concatNoBreak("共盈利", setColor(formatMoney(raw.exchange.income, false, 0, 2), getIncomeTextColor(raw.exchange.income)), " · 可变现", setColor(formatMoney(raw.exchange.exchange, false, 0, 2), getIncomeTextColor(raw.exchange.exchange)), "元"))
                    .def("共盈利" + PlaceHolder.NULL_VALUE + " · 可变现" + PlaceHolder.NULL_VALUE + "元").get();
        }
    }
}
