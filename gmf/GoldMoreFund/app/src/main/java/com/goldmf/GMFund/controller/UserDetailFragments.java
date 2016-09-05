package com.goldmf.GMFund.controller;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.controller.business.ChatController;
import com.goldmf.GMFund.controller.business.FundController;
import com.goldmf.GMFund.controller.business.SimulationController;
import com.goldmf.GMFund.controller.business.UserController;
import com.goldmf.GMFund.controller.circle.CircleHelper;
import com.goldmf.GMFund.controller.circle.CircleHelper.CellVM;
import com.goldmf.GMFund.controller.dialog.GMFBottomSheet;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.controller.internal.ChildBinder;
import com.goldmf.GMFund.controller.internal.ChildBinders;
import com.goldmf.GMFund.controller.internal.FundCardViewHelper;
import com.goldmf.GMFund.controller.internal.FundCardViewHelper.FundCardViewModel;
import com.goldmf.GMFund.controller.internal.PickAvatarHelper;
import com.goldmf.GMFund.controller.internal.StockFeedHelper;
import com.goldmf.GMFund.controller.internal.StockFeedHelper.StockFeedVM;
import com.goldmf.GMFund.controller.protocol.VCStateDataProtocol;
import com.goldmf.GMFund.extension.BitmapExtension;
import com.goldmf.GMFund.manager.discover.UserManager;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.manager.message.PersonalFeed;
import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.mine.Trader.TraderPerformance;
import com.goldmf.GMFund.manager.stock.RealUserIncomeChart;
import com.goldmf.GMFund.manager.stock.SimulationUserIncomeChart;
import com.goldmf.GMFund.model.CommonDefine.PlaceHolder;
import com.goldmf.GMFund.model.Feed;
import com.goldmf.GMFund.model.FeedOrder;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.FundBrief.Fund_Status;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.model.GMFMatch;
import com.goldmf.GMFund.model.GMFRankUser;
import com.goldmf.GMFund.model.RichTextUrl;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.model.User.User_Type;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.PageArrayHelper;
import com.goldmf.GMFund.util.GlobalVariableDic;
import com.goldmf.GMFund.util.IntCounter;
import com.goldmf.GMFund.util.PersistentObjectUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.BasicCell;
import com.goldmf.GMFund.widget.UserAvatarView;
import com.yale.ui.mkchart.charts.BarChart;
import com.yale.ui.mkchart.charts.LineChart;
import com.yale.ui.mkchart.components.Legend;
import com.yale.ui.mkchart.components.XAxis;
import com.yale.ui.mkchart.components.YAxis;
import com.yale.ui.mkchart.data.BarData;
import com.yale.ui.mkchart.data.BarDataSet;
import com.yale.ui.mkchart.data.BarEntry;
import com.yale.ui.mkchart.data.Entry;
import com.yale.ui.mkchart.data.LineData;
import com.yale.ui.mkchart.data.LineDataSet;
import com.yale.ui.support.AdvanceSwipeRefreshLayout;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import yale.extension.rx.RXFragment;
import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_USER;
import static com.goldmf.GMFund.controller.FragmentStackActivity.pushFragment;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_AccountProfilePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_LoginPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_MoreFundPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_MyStockAccountDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockTradePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_UserDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BLUE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RISE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLUE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_COPPER_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GOLD_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_IRON_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_SILVER_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getIncomeTextColor;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.IntExtension.inRange;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addCellVerticalSpacing;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addHorizontalSepLine;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.getSimpleAdapter;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.isScrollToBottom;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.isScrollToTop;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setClickEvent;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.StringExtension.anyContain;
import static com.goldmf.GMFund.extension.StringExtension.notEmpty;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.getStatusBarHeight;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_getLayoutParams;
import static com.goldmf.GMFund.extension.ViewExtension.v_preDraw;
import static com.goldmf.GMFund.extension.ViewExtension.v_setBehavior;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageResource;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setTextWithFitBound;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.model.FundBrief.Money_Type.CN;
import static com.goldmf.GMFund.model.FundBrief.Money_Type.HK;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.hasMoreData;
import static com.goldmf.GMFund.util.FormatUtil.formatBigNumber;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatRatio;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;

/**
 * Created by yale on 16/4/14.
 */
public class UserDetailFragments {
    public static class UserDetailFragment extends SimpleFragment {


        private static class VCStateData implements VCStateDataProtocol<UserDetailFragment> {

            File[] pictureFiles;

            public VCStateData() {

            }

            @Override
            public VCStateData init(UserDetailFragment fragment) {
                pictureFiles = fragment.mOutPictureFiles;
                return this;
            }

            @Override
            public void restore(UserDetailFragment fragment) {
                safeCall(() -> fragment.mOutPictureFiles = pictureFiles);
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeSerializable(this.pictureFiles);
            }

            protected VCStateData(Parcel in) {
                this.pictureFiles = (File[]) in.readSerializable();
            }

            public static final Creator<VCStateData> CREATOR = new Creator<VCStateData>() {
                @Override
                public VCStateData createFromParcel(Parcel source) {
                    return new VCStateData(source);
                }

                @Override
                public VCStateData[] newArray(int size) {
                    return new VCStateData[size];
                }
            };
        }


        private User mUser;
        private int mUserID;
        private String mUserName;
        private String mAvatarURL;

        private List<RealUserIncomeChart> mRealPerformance;
        private SimulationUserIncomeChart mSimulationPerformance;
        private User mTraderInfo;
        private CommandPageArray<Feed> mFeedPageArray;
        private PublishSubject<Void> mStartFetchTalentInfoSubject = PublishSubject.create();
        private PublishSubject<MResultsInfo> mFinishFetchTalentInfoSubject = PublishSubject.create();
        private PublishSubject<Void> mStartFetchTraderInfoSubject = PublishSubject.create();
        private PublishSubject<MResultsInfo> mFinishFetchTraderInfoSubject = PublishSubject.create();
        private PublishSubject<Void> mStartFetchRealPerformanceSubject = PublishSubject.create();
        private PublishSubject<MResultsInfo> mFinishFetchRealPerformanceSubject = PublishSubject.create();
        private PublishSubject<Void> mStartFetchSimulationPerformanceSubject = PublishSubject.create();
        private PublishSubject<MResultsInfo> mFinishFetchSimulationPerformanceSubject = PublishSubject.create();
        private PublishSubject<Void> mStartFetchFeedPageArraySubject = PublishSubject.create();
        private PublishSubject<MResultsInfo> mFinishFetchFeedPageArraySubject = PublishSubject.create();
        private boolean mIsFetchingTalentInfo = false;
        private boolean mIsFetchingTraderInfo = false;
        private boolean mIsFetchingRealPerformance = false;
        private boolean mIsFetchingSimulationPerformance = false;
        private boolean mIsFetchingFeedPageArray = false;
        private boolean mHasFetchNecessaryData = false;
        private boolean mDataExpired = true;
        private File[] mOutPictureFiles;
        private User mUserDetailIno;
        private SimulationFailVM mSimulationFailVm;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            User mLoginUser = safeGet(() -> MineManager.getInstance().getmMe(), null);
            int mLoginUserID = safeGet(() -> MineManager.getInstance().getmMe().index, 0);
            String mLoginUserName = safeGet(() -> MineManager.getInstance().getmMe().getName(), "");
            String mLoginAvatarURL = safeGet(() -> MineManager.getInstance().getmMe().getPhotoUrl(), "");

            mUser = safeGet(() -> GlobalVariableDic.shareInstance().pop(KEY_USER), mLoginUser);
            mUserID = safeGet(() -> mUser.index, -1);

            if (mUserID == mLoginUserID) {
                mUserName = mLoginUserName;
                mAvatarURL = mLoginAvatarURL;
            } else {
                mUserName = safeGet(() -> mUser.getName(), "");
                mAvatarURL = safeGet(() -> mUser.getPhotoUrl(), "");
            }
            return inflater.inflate(R.layout.frag_user_detail, container, false);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putSerializable("vc_data", new VCStateData().init(this));
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupBackButton(this, findToolbar(this));
            changeVisibleSection(TYPE_LOADING);

            if (savedInstanceState != null) {
                VCStateData data = (VCStateData) savedInstanceState.getSerializable("vc_data");
                if (data != null) {
                    data.restore(this);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().setStatusBarColor(0);
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                View root = view.findViewById(R.id.root);
                opt(root.getLayoutParams())
                        .cast(ViewGroup.MarginLayoutParams.class)
                        .consume(it -> {
                            it.topMargin = -getStatusBarHeight(this);
                            root.requestLayout();
                        });
            }
            initContentView();
            updateTitle(mUserName);
            v_setClick(mReloadSection, v -> fetchData(true));
            setOnSwipeRefreshListener(() -> fetchData(false));

            consumeEvent(Observable.merge(NotificationCenter.onWriteNewArticleSubject,
                    NotificationCenter.onWriteNewCommentSubject,
                    NotificationCenter.onMessageStateChangedSubject,
                    NotificationCenter.loginSubject,
                    NotificationCenter.logoutSubject))
                    .onNextFinish(response -> {
                        mDataExpired = true;
                        if (getUserVisibleHint() && getView() != null) {
                            setUserVisibleHint(true);
                        }
                    })
                    .done();

            if (MineManager.getInstance().isLoginOK()) {
                consumeEvent(NotificationCenter.userFollowCountChangedSubject)
                        .onNextFinish(ignored -> {
                            if (getView() != null) {
                                UserDetailHeaderVM vm = new UserDetailHeaderVM(mUserDetailIno, null);
                                updateHeaderFollow(vm);
                            }
                        })
                        .done();
            }

            boolean isLoginUser = MineManager.getInstance().isLoginOK() && safeGet(() -> MineManager.getInstance().getmMe().index == mUserID, false);
            if (isLoginUser) {
                consumeEvent(NotificationCenter.userInfoChangedSubject)
                        .onNextFinish(ignored -> {
                            if (getView() != null) {
                                Mine me = MineManager.getInstance().getmMe();
                                if (me != null) {
                                    UserDetailHeaderVM vm = new UserDetailHeaderVM(me, me.getPhotoUrl());
                                    updateHeader(vm);
                                }
                            }
                        })
                        .done();
            }

            fetchData(true);
            mDataExpired = false;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mHasFetchNecessaryData = false;
            mIsFetchingTalentInfo = false;
            mIsFetchingTraderInfo = false;
            mIsFetchingRealPerformance = false;
            mIsFetchingSimulationPerformance = false;
            mDataExpired = true;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (mDataExpired) {
                fetchData(false);
                mDataExpired = false;
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            PickAvatarHelper.handlePickAvatarCallback(this, requestCode, resultCode, data, mOutPictureFiles);
        }

        private void checkIsNeedToShowSetAvatarAlert(String userID) {
            boolean isLoginUser = safeGet(() -> String.valueOf(MineManager.getInstance().getmMe().index).equals(userID), false);
            if (isLoginUser) {
                if (PersistentObjectUtil.readIsNeedToShowSetAvatarAlert()) {
                    UmengUtil.stat_click_event(UmengUtil.eEVENTIDUserHomeSetAvatarAlert);
                    new GMFDialog.Builder(getActivity())
                            .setMessage("给自己换一个性感的头像吧")
                            .setPositiveButton("立即设置", (dialog, which) -> {
                                UmengUtil.stat_click_event(UmengUtil.eEVENTIDUserHomeAlertConfirm);
                                dialog.dismiss();
                                PersistentObjectUtil.writeIsNeedToShowSetAvatarAlert();
                                mOutPictureFiles = new File[1];
                                PickAvatarHelper.showPickPhotoBottomSheet(this, mOutPictureFiles);
                            })
                            .setNegativeButton("取消", (dialog, which) -> {
                                UmengUtil.stat_click_event(UmengUtil.eEVENTIDUserHomeAlertCancel);
                                dialog.dismiss();
                                PersistentObjectUtil.writeIsNeedToShowSetAvatarAlert();
                            })
                            .setCancelable(false)
                            .create()
                            .show();
                }
            }
        }

        private void fetchData(boolean reload) {
            consumeEventMRUpdateUI(SimulationController.freshUserDetail(mUser), reload)
                    .setTag("fresh_user")
                    .onNextFinish(detailResponse -> {
                        if (isSuccess(detailResponse)) {
                            mUserDetailIno = detailResponse.data;
                            if (!mHasFetchNecessaryData) {
                                changeVisibleSection(TYPE_LOADING);
                                consumeEventMRUpdateUI(SimulationController.fetchSimulationPerformance(mUserID), reload)
                                        .setTag("reload_data")
                                        .onNextFinish(simulationResponse -> {
                                            if (isSuccess(simulationResponse) || simulationResponse.errCode == 100015) {
                                                mSimulationPerformance = simulationResponse.data;
                                                UserDetailHeaderVM vm = new UserDetailHeaderVM(safeGet(() -> detailResponse.data, null), mAvatarURL);
                                                updateHeaderFollow(vm);
                                                updateHeader(vm);
                                                updateViewPager(vm);
                                                changeVisibleSection(TYPE_CONTENT);
                                                mHasFetchNecessaryData = true;
                                                checkIsNeedToShowSetAvatarAlert(String.valueOf(mUserID));
                                            } else {
                                                changeVisibleSection(TYPE_LOADING);
                                                tryToFetchTraderInfoInstead(mUserID, traderResponse -> {
                                                    mTraderInfo = traderResponse.data;
                                                    UserDetailHeaderVM vm = new UserDetailHeaderVM(safeGet(() -> detailResponse.data, null), mAvatarURL);
                                                    updateHeaderFollow(vm);
                                                    updateHeader(vm);
                                                    updateViewPager(vm);
                                                    changeVisibleSection(TYPE_CONTENT);
                                                    mHasFetchNecessaryData = true;
                                                    checkIsNeedToShowSetAvatarAlert(String.valueOf(mUserID));
                                                });
                                            }
                                        })
                                        .done();
                            } else {
                                if (!reload) {
                                    setSwipeRefreshing(true);
                                }
                                ViewPager pager = v_findView(mContentSection, R.id.pager);
                                String currentPageTitle = safeGet(() -> pager.getAdapter().getPageTitle(pager.getCurrentItem()).toString(), "");
                                if (anyContain(currentPageTitle, "操盘手", "牛人", "管理组合")) {
                                    fetchTraderInfo();
                                } else if (anyContain(currentPageTitle, "投资业绩")) {
                                    fetchRealPerformance();
                                } else if (anyContain(currentPageTitle, "模拟业绩")) {
                                    fetchSimulationPerformance();
                                } else if (anyContain(currentPageTitle, "动态")) {
                                    fetchFeedPageArray();
                                }
                                checkIsNeedToShowSetAvatarAlert(String.valueOf(mUserID));
                            }

                        } else {
                            changeVisibleSection(TYPE_RELOAD);
                            setReloadSectionTips(getErrorMessage(detailResponse));
                        }
                    })
                    .done();
        }

        private void tryToFetchTraderInfoInstead(int userID, Action1<MResultsInfo<User>> finishCallback) {
            consumeEventMRUpdateUI(FundController.fetchTraderInfo(userID), true)
                    .setTag("fetch_trader_info")
                    .onNextFinish(response -> finishCallback.call(response))
                    .done();
        }

        private void fetchFeedPageArray() {
            if (mIsFetchingFeedPageArray) {
                return;
            }

            mIsFetchingFeedPageArray = true;

            mStartFetchFeedPageArraySubject.onNext(null);
            consumeEventMR(ChatController.fetchUserCirclePageArray(mUserID))
                    .onNextSuccess(response -> {
                        mFeedPageArray = response.data;
                    })
                    .onNextFinish(response -> {
                        mFinishFetchFeedPageArraySubject.onNext(response);
                        mIsFetchingFeedPageArray = false;
                        setSwipeRefreshing(false);
                    })
                    .done();
        }

        private void fetchTalentInfo() {
            if (mIsFetchingTalentInfo)
                return;

            mIsFetchingTalentInfo = true;
            consumeEventMR(FundController.fetchTraderInfo(mUserID))
                    .setTag("fetch_talent_info")
                    .onConsumed(() -> mStartFetchTalentInfoSubject.onNext(null))
                    .onNextSuccess(response -> {
                        mTraderInfo = response.data;
                    })
                    .onNextFinish(response -> {
                        mFinishFetchTalentInfoSubject.onNext(response);
                        mIsFetchingTalentInfo = false;
                        setSwipeRefreshing(false);
                    })
                    .done();
        }

        private void fetchTraderInfo() {
            if (mIsFetchingTraderInfo) {
                return;
            }

            mIsFetchingTraderInfo = true;
            consumeEventMR(FundController.fetchTraderInfo(mUserID))
                    .setTag("fetch_trader_info")
                    .onConsumed(() -> mStartFetchTraderInfoSubject.onNext(null))
                    .onNextSuccess(response -> {
                        mTraderInfo = response.data;
                    })
                    .onNextFinish(response -> {
                        mFinishFetchTraderInfoSubject.onNext(response);
                        mIsFetchingTraderInfo = false;
                        setSwipeRefreshing(false);
                    })
                    .done();
        }

        private void fetchRealPerformance() {
            if (mIsFetchingRealPerformance) {
                return;
            }
            mRealPerformance = null;
            mIsFetchingRealPerformance = true;
            consumeEventMR(UserController.fetchRealPerformance(mUserID))
                    .setTag("fetch_simulation_performance")
                    .onConsumed(() -> mStartFetchRealPerformanceSubject.onNext(null))
                    .onNextSuccess(response -> {
                        mRealPerformance = response.data;
                    })
                    .onNextFinish(response -> {
                        mFinishFetchRealPerformanceSubject.onNext(response);
                        mIsFetchingRealPerformance = false;
                        setSwipeRefreshing(false);
                    })
                    .done();
        }

        private void fetchSimulationPerformance() {
            if (mIsFetchingSimulationPerformance) {
                return;
            }
            mSimulationPerformance = null;
            mIsFetchingSimulationPerformance = true;
            consumeEventMR(SimulationController.fetchSimulationPerformance(mUserID))
                    .setTag("fetch_simulation_performance")
                    .onConsumed(() -> mStartFetchSimulationPerformanceSubject.onNext(null))
                    .onNextSuccess(response -> {
                        mSimulationFailVm = null;
                        mSimulationPerformance = response.data;
                    })
                    .onNextFail(response -> {
                        mSimulationFailVm = new SimulationFailVM(response);
                    })
                    .onNextFinish(response -> {
                        mFinishFetchSimulationPerformanceSubject.onNext(response);
                        mIsFetchingSimulationPerformance = false;
                        setSwipeRefreshing(false);
                    })
                    .done();
        }

        private void initContentView() {
            Stream.of(mLoadingSection, mReloadSection)
                    .forEach(view -> {
                        v_setBehavior(view, new CoordinatorLayout.Behavior<View>() {
                            @Override
                            public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
                                return dependency.getId() == R.id.section_toolbar;
                            }

                            @Override
                            public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
                                ViewGroup.MarginLayoutParams params = v_getLayoutParams(child);
                                params.topMargin = dependency.getBottom();
                                return true;
                            }
                        });
                    });

            View headerBG = v_findView(this, R.id.section_background);
            v_setBehavior(headerBG, new CoordinatorLayout.Behavior<View>() {
                private View mTabLayout = v_findView(getView(), R.id.tabLayout);
                private Rect rect = new Rect();

                @Override
                public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
                    return dependency.getId() == R.id.header;
                }

                @Override
                public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
                    mTabLayout.getGlobalVisibleRect(rect);
                    headerBG.setTranslationY(rect.top - headerBG.getMeasuredHeight());
                    return true;
                }
            });

            View toolbarSection = v_findView(this, R.id.section_toolbar);
            ColorDrawable backgroundDrawable = new ColorDrawable(BLACK_COLOR);
            toolbarSection.setBackgroundDrawable(backgroundDrawable);

            ImageView headerBGImage = v_findView(this, R.id.img_headerBG);
            View headerCover = v_findView(this, R.id.img_header_cover);
            headerCover.setBackgroundColor(0x4A000000);

            v_preDraw(headerBGImage, true, view -> {
                Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_avatar_placeholder);
                headerBGImage.setImageDrawable(new BitmapDrawable(getResources(), defaultBitmap));
                headerBGImage.buildDrawingCache();
                Bitmap bmp = headerBGImage.getDrawingCache();
                BitmapExtension.blur(bmp, headerBGImage, getActivity());
            });
        }

        private void updateHeaderFollow(UserDetailHeaderVM vm) {
            View header = v_findView(this, R.id.header);
            TextView leftLabel = v_findView(header, R.id.label_left);
            TextView rightLabel = v_findView(header, R.id.label_right);
            v_setText(leftLabel, vm.followNum);
            v_setText(rightLabel, vm.fansNum);
        }

        private void performFollow(TextView focusButton, UserDetailHeaderVM vm) {

            if (!MineManager.getInstance().isLoginOK()) {
                showActivity(this, an_LoginPage());

                consumeEvent(NotificationCenter.loginSubject.limit(1))
                        .setTag("action_after_login")
                        .onNextFinish(ignored -> {
                            performFollow(focusButton, vm);
                        })
                        .done();

                consumeEvent(NotificationCenter.cancelLoginSubject.limit(1))
                        .setTag("action_after_cancel_login")
                        .onNextFinish(ignored -> {
                            unsubscribeFromMain("action_after_login");
                        })
                        .done();
            } else {
                if (vm.hasFollow) {
                    GMFBottomSheet sheet = new GMFBottomSheet.Builder(getActivity())
                            .setTitle("确定不再关注" + vm.userName + "?")
                            .addContentItem(new GMFBottomSheet.BottomSheetItem(0, setColor("不再关注", RED_COLOR), 0))
                            .create();
                    sheet.setOnItemClickListener((bottomSheet, item) -> {
                        bottomSheet.dismiss();
                        focusButton.setText("关注");
                        focusButton.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_unattention_white, 0, 0, 0);
                        consumeEventMR(SimulationController.deleteFollowUser(vm.raw))
                                .onNextSuccess(response -> NotificationCenter.userFollowCountChangedSubject.onNext(null))
                                .onNextFail(response -> getErrorMessage(response))
                                .done();
                        vm.hasFollow = !vm.hasFollow;
                    });
                    sheet.show();
                } else {
                    focusButton.setText("已关注");
                    focusButton.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_attention_white, 0, 0, 0);
                    consumeEventMR(SimulationController.addFollowUser(vm.raw))
                            .onNextSuccess(response -> NotificationCenter.userFollowCountChangedSubject.onNext(null))
                            .onNextFail(response -> getErrorMessage(response))
                            .done();
                    vm.hasFollow = !vm.hasFollow;
                }
            }
        }

        private void updateHeader(UserDetailHeaderVM vm) {
            updateTitle(vm.userName);

            TextView focusButton = v_findView(this, R.id.btn_attention);
            focusButton.setText(vm.hasFollow ? "已关注" : "关注");
            focusButton.setCompoundDrawablesWithIntrinsicBounds(vm.hasFollow ? R.mipmap.ic_attention_white : R.mipmap.ic_unattention_white, 0, 0, 0);
            v_setVisibility(focusButton, vm.isLoginUser ? View.GONE : View.VISIBLE);
            v_setVisibility(this, R.id.btn_edit, vm.isLoginUser ? View.VISIBLE : View.GONE);
            v_setClick(this, R.id.btn_edit, v -> showActivity(this, an_AccountProfilePage()));
            v_setClick(focusButton, v -> {
                performFollow(focusButton, vm);
            });

            ImageView headerBGImage = v_findView(this, R.id.img_headerBG);
            View header = v_findView(this, R.id.header);

            Observable<Bitmap> observable = Observable.create(sub -> {
                Bitmap avatarBitmap = null;
                try {
                    byte[] bytes = BitmapExtension.getBytes(new URL(vm.avatarURL).openStream());
                    avatarBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                } catch (IOException e) {
                }
                sub.onNext(avatarBitmap);
                sub.onCompleted();
            });

            consumeEvent(observable.subscribeOn(Schedulers.newThread()))
                    .setTag("fetch_bitmap")
                    .onNextFinish(bitmap -> {
                        if (bitmap == null)
                            return;
                        headerBGImage.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                        headerBGImage.buildDrawingCache();
                        Bitmap bmp = headerBGImage.getDrawingCache();
                        BitmapExtension.blur(bmp, headerBGImage, getActivity());
                        int primaryColor = Palette.from(bmp).generate().getVibrantColor(STATUS_BAR_BLACK);
                        View toolbarSection = v_findView(this, R.id.section_toolbar);
                        ColorDrawable backgroundDrawable = new ColorDrawable(primaryColor);
                        backgroundDrawable.setAlpha(0);
                        toolbarSection.setBackgroundDrawable(backgroundDrawable);
                        v_setBehavior(toolbarSection, new CoordinatorLayout.Behavior<View>() {
                            float alphaFactor = 1.2f;
                            int bannerHeight = 0;
                            int bannerTopMargin = 0;
                            TabLayout tabLayout = v_findView(getView(), R.id.tabLayout);

                            @Override
                            public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
                                boolean isDependOn = dependency.getId() == R.id.header;
                                if (isDependOn) {
                                    bannerTopMargin = ((ViewGroup.MarginLayoutParams) dependency.getLayoutParams()).topMargin;
                                    bannerHeight = dependency.getMeasuredHeight() - tabLayout.getMeasuredHeight();
                                }
                                return isDependOn;
                            }

                            @Override
                            public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
                                int scrolledHeight = -(dependency.getTop() - bannerTopMargin);
                                int alpha = (int) Math.min(((float) scrolledHeight / bannerHeight * 255 * alphaFactor), 255);
                                backgroundDrawable.setAlpha(alpha);
                                return false;
                            }
                        });
                    })
                    .done();

            v_setImageUri(header, R.id.img_avatar, vm.avatarURL);
            v_setClick(header, R.id.img_avatar, v -> {
                boolean isLoginUser = safeGet(() -> String.valueOf(MineManager.getInstance().getmMe().index).equals(vm.userID), false);
                if (isLoginUser) {
                    showActivity(this, an_AccountProfilePage());
                }
            });
            TextView briefLabel = v_findView(header, R.id.label_brief);
            TextView leftLabel = v_findView(header, R.id.label_left);
            TextView rightLabel = v_findView(header, R.id.label_right);
            ImageView avatarBackground = v_findView(header, R.id.img_avatar_bg);
            v_setText(briefLabel, vm.userNum);
            v_setClick(leftLabel, v -> {
                GlobalVariableDic.shareInstance().update(CommonProxyActivity.KEY_USER, vm.raw);
                pushFragment(this, new UserFollowOrFansFragment().init(UserManager.Follow_Type_Follow));
            });
            v_setClick(rightLabel, v -> {
                GlobalVariableDic.shareInstance().update(CommonProxyActivity.KEY_USER, vm.raw);
                pushFragment(this, new UserFollowOrFansFragment().init(UserManager.Follow_Type_Fans));
            });
            if (vm.isTalent) {
                avatarBackground.setImageResource(R.mipmap.ic_avatar_talent_bg);
            } else if (vm.isTrader) {
                avatarBackground.setImageResource(R.mipmap.ic_avatar_trader_bg);
            } else {
                v_setImageResource(avatarBackground, 0);
            }
        }

        @SuppressWarnings("ConstantConditions")
        private void updateViewPager(UserDetailHeaderVM vm) {
            LinkedList<String> tempTitles = new LinkedList<>();

            if (vm.isTrader) {
                tempTitles.add("操盘手");
            } else if (vm.isTalent) {
                tempTitles.add("牛人");
            }
            if (vm.isTrader || vm.isTalent) {
                tempTitles.add("管理组合");
            }
            tempTitles.add("动态");
            if (vm.isLoginUser) {
                tempTitles.add("投资业绩");
            }
            tempTitles.add("模拟业绩");
            UmengUtil.stat_click_event(vm.isLoginUser ? UmengUtil.eEVENTIDHomePageHost : UmengUtil.eEVENTIDHomePageGuest);
            String[] titles = new String[tempTitles.size()];
            tempTitles.toArray(titles);
            ViewPager pager = v_findView(this, R.id.pager);
            if (pager.getAdapter() != null) {
            } else {
                pager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
                    @Override
                    public Fragment getItem(int position) {
                        String title = getPageTitle(position).toString();

                        if (title.contains("牛人")) {
                            return new UserDetailPageTalentFragment();
                        }

                        if (title.contains("操盘手")) {
                            return new UserDetailPageTraderFragment();
                        }

                        if (title.contains("管理组合")) {
                            return new UserDetailPageFundFragment();
                        }

                        if (title.contains("模拟业绩")) {
                            return new UserDetailPageSimulationFragment();
                        }

                        if (title.contains("投资业绩")) {
                            return new UserDetailPageRealFragment();
                        }

                        if (title.contains("动态")) {
                            return new UserDetailPageFeedFragment();
                        }

                        return new UserDetailPageTraderFragment();
                    }

                    @Override
                    public int getCount() {
                        return titles.length;
                    }

                    @Override
                    public CharSequence getPageTitle(int position) {
                        return titles[position];
                    }
                });
                TabLayout tabLayout = v_findView(this, R.id.tabLayout);
                tabLayout.setupWithViewPager(pager);
            }
        }
    }

    private static class UserDetailHeaderVM {
        private User raw;
        private User.UserDetail userMore;
        private String userID;
        private String userName;
        private String avatarURL;
        private boolean hasFollow;
        private boolean isTalent;
        private boolean isTrader;
        private boolean isLoginUser;
        private CharSequence followNum;
        private CharSequence fansNum;
        private CharSequence userNum;

        UserDetailHeaderVM(String userID, String defaultAvatarURL) {
            this((User) null, defaultAvatarURL);
            this.userID = safeGet(() -> userID, "");
        }

        UserDetailHeaderVM(User raw, String defaultAvatarURL) {
            this.raw = raw;
            this.userID = safeGet(() -> String.valueOf(raw.index), "");
            this.userName = safeGet(() -> raw.getName(), "");
            this.hasFollow = safeGet(() -> raw.hasFollow, false);
            this.isTalent = safeGet(() -> anyMatch(raw.type, User_Type.Talent), false);
            this.isTrader = safeGet(() -> anyMatch(raw.type, User_Type.Trader), false);
            this.isLoginUser = safeGet(() -> userID.equals(String.valueOf(MineManager.getInstance().getmMe().index)), false);
            this.avatarURL = safeGet(() -> TextUtils.isEmpty(defaultAvatarURL) ? raw.getPhotoUrl() : defaultAvatarURL, "");
            this.userNum = String.format("小金号：%s", notEmpty(userID) ? userID : PlaceHolder.NULL_VALUE);
            this.userMore = safeGet(() -> raw.more, null);
            if (userMore != null) {
                this.followNum = concatNoBreak("关注 ", setColor("" + safeGet(() -> userMore.followNum, 0), TEXT_WHITE_COLOR));
                this.fansNum = concatNoBreak("粉丝 ", setColor("" + safeGet(() -> userMore.fansNum, 0), TEXT_WHITE_COLOR));
            } else {
                this.followNum = concatNoBreak("关注 ", setColor("--", TEXT_WHITE_COLOR));
                this.fansNum = concatNoBreak("粉丝 ", setColor("--", TEXT_WHITE_COLOR));
            }
        }
    }

    private static class SimulationFailVM {

        private int mErrCode;
        private String mErrorMsg;

        SimulationFailVM(MResultsInfo info) {
            mErrCode = info.errCode;
            mErrorMsg = info.msg;
        }
    }

    private static UserDetailFragment getParentUserDetailFragment(Fragment fragment) {
        return safeGet(() -> ((UserDetailFragment) fragment.getParentFragment()), null);
    }

    @Nullable
    private static List<RealUserIncomeChart> getRealPerformanceFromParent(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment(fragment).mRealPerformance, null);
    }

    @Nullable
    private static SimulationUserIncomeChart getSimulationPerformanceFromParent(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment(fragment).mSimulationPerformance, null);
    }

    private static SimulationFailVM getSimulationPerformanceFailVmFromParent(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment((fragment)).mSimulationFailVm, null);
    }

    @Nullable
    private static String getUserIDFromParent(Fragment fragment) {
        return safeGet(() -> String.valueOf(getParentUserDetailFragment(fragment).mUserID), null);
    }

    @Nullable
    private static User getUserInfoFromParent(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment(fragment).mTraderInfo, null);
    }

    private static CommandPageArray<Feed> getFeedPageArrayFromParent(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment(fragment).mFeedPageArray, null);
    }

    @Nullable
    private static PublishSubject<Void> getStartFetchTalentInfoSubject(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment(fragment).mStartFetchTalentInfoSubject, null);
    }

    @Nullable
    private static PublishSubject<MResultsInfo> getFinishFetchTalentInfoSubject(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment(fragment).mFinishFetchTalentInfoSubject, null);
    }

    @Nullable
    private static PublishSubject<Void> getStartFetchTraderInfoSubject(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment(fragment).mStartFetchTraderInfoSubject, null);
    }

    @Nullable
    private static PublishSubject<MResultsInfo> getFinishFetchTraderInfoSubject(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment(fragment).mFinishFetchTraderInfoSubject, null);
    }

    @Nullable
    private static PublishSubject<Void> getStartFetchSimulationPerformanceSubjectFromParent(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment(fragment).mStartFetchSimulationPerformanceSubject, null);
    }

    @Nullable
    private static PublishSubject<MResultsInfo> getFinishFetchSimulationPerformanceSubjectFromParent(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment(fragment).mFinishFetchSimulationPerformanceSubject, null);
    }

    @Nullable
    private static PublishSubject<Void> getStartFetchRealPerformanceSubjectFromParent(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment(fragment).mStartFetchRealPerformanceSubject, null);
    }

    @Nullable
    private static PublishSubject<MResultsInfo> getFinishFetchRealPerformanceSubjectFromParent(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment(fragment).mFinishFetchRealPerformanceSubject, null);
    }


    @Nullable
    private static PublishSubject<Void> getStartFetchFeedPageArrayFromParent(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment(fragment).mStartFetchFeedPageArraySubject, null);
    }

    @Nullable
    private static PublishSubject<MResultsInfo> getFinishFetchFeedPageArrayFromParent(Fragment fragment) {
        return safeGet(() -> getParentUserDetailFragment(fragment).mFinishFetchFeedPageArraySubject, null);
    }


    private static void requestFetchTalentInfoFromParent(Fragment fragment) {
        safeCall(() -> getParentUserDetailFragment(fragment).fetchTalentInfo());
    }

    private static void requestFetchTraderInfoFromParent(Fragment fragment) {
        safeCall(() -> getParentUserDetailFragment(fragment).fetchTraderInfo());
    }

    private static void requestFetchRealPerformanceFromParent(Fragment fragment) {
        safeCall(() -> getParentUserDetailFragment(fragment).fetchRealPerformance());
    }

    private static void requestFetchSimulationPerformanceFromParent(Fragment fragment) {
        safeCall(() -> getParentUserDetailFragment(fragment).fetchSimulationPerformance());
    }

    private static void requestFetchFeedPageArrayFromParent(Fragment fragment) {
        safeCall(() -> getParentUserDetailFragment(fragment).fetchFeedPageArray());
    }


    public static class UserDetailPageTalentFragment extends SimpleFragment {

        private boolean mHasData = false;

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
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_user_detail_page_talent, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            v_setClick(mReloadSection, v -> {
                changeVisibleSection(TYPE_LOADING);
                requestFetchTalentInfoFromParent(this);
            });

            opt(getStartFetchTalentInfoSubject(this))
                    .consume(subject -> {
                        consumeEvent(subject)
                                .setTag("on_start_fetch")
                                .onNextFinish(ignored -> {
                                    if (!mHasData) {
                                        changeVisibleSection(TYPE_LOADING);
                                    }
                                })
                                .done();
                    });
            opt(getFinishFetchTalentInfoSubject(this))
                    .consume(subject -> {
                        consumeEventMR(subject)
                                .onNextSuccess(response -> {
                                    resetData();
                                    mHasData = true;
                                })
                                .onNextFail(response -> {
                                    setReloadSectionTips(getErrorMessage(response));
                                    changeVisibleSection(TYPE_RELOAD);
                                })
                                .done();
                    });

            changeVisibleSection(TYPE_LOADING);

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (opt(getUserInfoFromParent(this)).isAbsent()) {
                    changeVisibleSection(TYPE_LOADING);
                    requestFetchTalentInfoFromParent(this);
                    mHasData = false;
                } else {
                    resetData();
                    mHasData = true;
                }
            }
        }

        private void resetData() {

            User userInfo = getUserInfoFromParent(this);
            if (userInfo == null) {
                changeVisibleSection(TYPE_EMPTY);
            } else {
                TalentPageVm vm = new TalentPageVm(userInfo);
                View introductionCard = v_findView(mContentSection, R.id.card_introduction);
                v_setText(introductionCard, R.id.label_left, vm.accumulateInvestCount);
                v_setText(introductionCard, R.id.label_medium, vm.accumulateHoldAmount);
                v_setText(introductionCard, R.id.label_right, vm.averageIncomeRatio);
                v_setText(introductionCard, R.id.label_brief1, vm.talentIntroduce);
                v_setText(introductionCard, R.id.label_brief2, vm.tradeConcept);
                changeVisibleSection(TYPE_CONTENT);
            }
        }
    }

    public static class TalentPageVm {
        public CharSequence accumulateInvestCount;
        public CharSequence accumulateHoldAmount;
        public CharSequence averageIncomeRatio;
        public CharSequence talentIntroduce;
        public CharSequence tradeConcept;

        public TalentPageVm(User raw) {
            this.accumulateInvestCount = generateTitleAndSubtitle(() -> setColor(String.valueOf(raw.trader.investedNum), BLUE_COLOR), () -> "累计投资人次");
            this.accumulateHoldAmount = generateTitleAndSubtitle(() -> setColor(formatBigNumber(raw.trader.managerAmount, false, 0, 2), BLUE_COLOR), () -> "累计操盘金额");
            this.averageIncomeRatio = generateTitleAndSubtitle(() -> setColor(formatRatio(raw.trader.historicalReturn, false, 2), getIncomeTextColor(raw.trader.historicalReturn)), () -> "平均收益率");
            this.talentIntroduce = safeGet(() -> raw.trader.more.introduction, "");
            this.tradeConcept = safeGet(() -> raw.trader.more.managementConcept, "");
        }

        private static CharSequence generateTitleAndSubtitle(Func0<CharSequence> titleGetter, Func0<CharSequence> subtitleGetter) {
            return safeGet(() -> concat(setFontSize(titleGetter.call(), sp2px(16)), setFontSize(subtitleGetter.call(), sp2px(12))))
                    .def(concat(setFontSize(PlaceHolder.NULL_VALUE, sp2px(16)), setFontSize(PlaceHolder.NULL_VALUE, sp2px(12)))).get();
        }
    }

    public static class UserDetailPageTraderFragment extends SimpleFragment {

        private boolean mHasData = false;

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
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            UmengUtil.stat_click_event(UmengUtil.eEVENTIDHomePageTrader);
            return inflater.inflate(R.layout.frag_user_detail_page_trader, container, false);
        }

        @SuppressWarnings("CodeBlock2Expr")
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            v_setClick(mReloadSection, v -> {
                changeVisibleSection(TYPE_LOADING);
                requestFetchTraderInfoFromParent(this);
            });

            opt(getStartFetchTraderInfoSubject(this))
                    .consume(subject -> {
                        consumeEvent(subject)
                                .setTag("on_start_fetch")
                                .onNextFinish(ignored -> {
                                    if (!mHasData) {
                                        changeVisibleSection(TYPE_LOADING);
                                    }
                                })
                                .done();
                    });
            opt(getFinishFetchTraderInfoSubject(this))
                    .consume(subject -> {
                        consumeEventMR(subject)
                                .setTag("on_finish_fetch")
                                .onNextSuccess(response -> {
                                    resetData();
                                    mHasData = true;
                                })
                                .onNextFail(response -> {
                                    setReloadSectionTips(getErrorMessage(response));
                                    changeVisibleSection(TYPE_RELOAD);
                                })
                                .done();
                    });


            changeVisibleSection(TYPE_LOADING);
            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mHasData = false;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (opt(getUserInfoFromParent(this)).isAbsent()) {
                    changeVisibleSection(TYPE_LOADING);
                    requestFetchTraderInfoFromParent(this);
                    mHasData = false;
                } else {
                    resetData();
                    mHasData = true;
                }
            }
        }

        private void resetData() {
            User userInfo = getUserInfoFromParent(this);
            if (userInfo == null) {
                changeVisibleSection(TYPE_EMPTY);
            } else {
                TraderPageVM vm = new TraderPageVM(userInfo);
                View introductionCard = v_findView(mContentSection, R.id.card_introduction);
                v_setText(introductionCard, R.id.label_left, vm.accumulateInvestCount);
                v_setText(introductionCard, R.id.label_medium, vm.accumulateHoldAmount);
                v_setText(introductionCard, R.id.label_right, vm.averageIncomeRatio);
                v_setText(introductionCard, R.id.label_brief, vm.traderBrief);

                View historyIncomeCard = v_findView(mContentSection, R.id.card_history_income);
                opt(vm.historyIncomeChartData)
                        .consume(it -> {
                            BarChart chart = v_findView(historyIncomeCard, R.id.chart_history_income);

                            chart.setTouchEnabled(false);
                            chart.setDrawGridBackground(false);
                            chart.getLegend().setEnabled(false);
                            chart.setDescription("");
                            chart.setDrawBorders(false);
                            XAxis xAxis = chart.getXAxis();
                            xAxis.setDrawGridLines(false);
                            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                            xAxis.setTextSize(10);
                            xAxis.setTextColor(0xFF999999);
                            xAxis.setDrawAxisLine(false);
                            chart.getAxis(YAxis.AxisDependency.RIGHT).setEnabled(false);
                            YAxis yAxis = chart.getAxis(YAxis.AxisDependency.LEFT);
                            yAxis.setDrawAxisLine(false);
                            yAxis.setValueFormatter(value -> formatRatio((double) value, false, 0));
                            yAxis.setTextColor(0xFFD0D0D0);
                            yAxis.setTextSize(10);

                            boolean needToAnimate = chart.getData() == null;
                            chart.setData(it);
                            if (needToAnimate) {
                                v_preDraw(chart, true, v -> chart.animateY(1000));
                            }
                        });
                v_setVisibility(historyIncomeCard, vm.historyIncomeChartData == null ? View.GONE : View.VISIBLE);


                Context ctx = getActivity();
                Resources res = ctx.getResources();
                GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(ctx.getResources())
                        .setRoundingParams(new RoundingParams().setCornersRadius(dp2px(4)))
                        .setPlaceholderImage(res.getDrawable(R.mipmap.ic_fund_cover_placeholder), ScalingUtils.ScaleType.CENTER_CROP)
                        .build();
                LinearLayout listView = v_findView(mContentSection, R.id.listView);
                v_preDraw(listView, true, view -> {
                    float defaultRatio = 2.5f;
                    Stream.of(vm.moreLinkList)
                            .forEach(it -> {
                                listView.removeAllViewsInLayout();
                                CardView cardView = new CardView(ctx);
                                cardView.setRadius(dp2px(4));
                                SimpleDraweeView draweeView = new SimpleDraweeView(ctx, hierarchy);
                                draweeView.setController(Fresco.newDraweeControllerBuilder()
                                        .setUri(it.image)
                                        .setControllerListener(new BaseControllerListener<ImageInfo>() {
                                            @Override
                                            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                                                super.onFinalImageSet(id, imageInfo, animatable);
                                                opt(imageInfo).consume(it -> {
                                                    float ratio = (float) it.getWidth() / it.getHeight();
                                                    CardView.LayoutParams params = v_getLayoutParams(draweeView);
                                                    params.height = (int) ((float) params.width / ratio);
                                                    draweeView.requestLayout();
                                                });
                                            }
                                        })
                                        .build());

                                int width = listView.getMeasuredWidth();
                                int height = (int) ((float) width / defaultRatio);
                                cardView.addView(draweeView, new CardView.LayoutParams(width, height));

                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
                                params.topMargin = dp2px(16);
                                listView.addView(cardView, params);

                                v_setClick(cardView, v -> CMDParser.parse(it.url).call(ctx));
                            });
                    listView.addView(new Space(ctx), new LinearLayout.LayoutParams(1, dp2px(16)));
                });

                changeVisibleSection(TYPE_CONTENT);
            }
        }
    }

    private static class TraderPageVM {
        public CharSequence accumulateInvestCount;
        public CharSequence accumulateHoldAmount;
        public CharSequence averageIncomeRatio;
        public CharSequence traderBrief;
        @Nullable
        public BarData historyIncomeChartData;
        public List<RichTextUrl> moreLinkList;

        public TraderPageVM(User raw) {
            this.accumulateInvestCount = generateTitleAndSubtitle(() -> setColor(String.valueOf(raw.trader.investedNum), BLUE_COLOR), () -> "累计投资人次");
            this.accumulateHoldAmount = generateTitleAndSubtitle(() -> setColor(formatBigNumber(raw.trader.managerAmount, false, 0, 2), BLUE_COLOR), () -> "累计操盘金额");
            this.averageIncomeRatio = generateTitleAndSubtitle(() -> setColor(formatRatio(raw.trader.historicalReturn, false, 2), getIncomeTextColor(raw.trader.historicalReturn)), () -> "平均收益率");
            this.traderBrief = safeGet(() -> raw.trader.more.introduction, "");
            this.historyIncomeChartData = generateHistoryIncomeChartData(raw);
            this.moreLinkList = safeGet(() -> raw.trader.more.introductionUrls, Collections.<RichTextUrl>emptyList());
        }

        private static CharSequence generateTitleAndSubtitle(Func0<CharSequence> titleGetter, Func0<CharSequence> subtitleGetter) {
            return safeGet(() -> concat(setFontSize(titleGetter.call(), sp2px(16)), setFontSize(subtitleGetter.call(), sp2px(12))))
                    .def(concat(setFontSize(PlaceHolder.NULL_VALUE, sp2px(16)), setFontSize(PlaceHolder.NULL_VALUE, sp2px(12)))).get();
        }

        private BarData generateHistoryIncomeChartData(User raw) {
            return safeGet(() -> {
                TraderPerformance performance = raw.trader.more.performance;
                List<String> xValues = performance.timeInfo;

                List<Double> rawValues = performance.performanceInfo;
                int valueCount = rawValues.size();
                List<BarEntry> yValues = new ArrayList<>(valueCount);
                for (int i = 0; i < valueCount; i++) {
                    yValues.add(new BarEntry(rawValues.get(i).floatValue(), i));
                }
                BarDataSet dataSet = new BarDataSet(yValues, "");
                dataSet.setColor(0xFFEB4A35);
                dataSet.setBarSpacePercent(40);
                dataSet.setValueTextSize(12);
                dataSet.setValueTextColor(0xFFEB4A35);
                BarData barData = new BarData(xValues, dataSet);
                barData.setValueFormatter(value -> formatRatio((double) value, false, 0));
                barData.setHighlightEnabled(false);
                return barData;
            }, null);
        }
    }

    public static class UserDetailPageFundFragment extends SimpleFragment {

        private boolean mHasData = false;

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
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_user_detail_page_fund, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            changeVisibleSection(TYPE_LOADING);

            v_setClick(mReloadSection, v -> {
                changeVisibleSection(TYPE_LOADING);
                requestFetchTraderInfoFromParent(this);
            });

            opt(getStartFetchTraderInfoSubject(this))
                    .consume(subject -> {
                        consumeEvent(subject)
                                .setTag("on_start_fetch")
                                .onNextFinish(response -> {
                                    if (!mHasData) {
                                        changeVisibleSection(TYPE_LOADING);
                                    }
                                })
                                .done();
                    });
            opt(getFinishFetchTraderInfoSubject(this))
                    .consume(subject -> {
                        consumeEventMR(subject)
                                .setTag("on_finish_fetch")
                                .onNextSuccess(response -> {
                                    resetData();
                                    mHasData = true;
                                })
                                .onNextFail(response -> {
                                    setReloadSectionTips(getErrorMessage(response));
                                    changeVisibleSection(TYPE_RELOAD);
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
        public void onDestroyView() {
            super.onDestroyView();
            mHasData = false;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser) {
                User userInfo = getUserInfoFromParent(this);
                if (userInfo == null) {
                    changeVisibleSection(TYPE_LOADING);
                    requestFetchTraderInfoFromParent(this);
                    mHasData = false;
                } else {
                    resetData();
                    mHasData = true;
                }
            }
        }

        private void resetData() {
            Context ctx = getActivity();
            User userInfo = getUserInfoFromParent(this);
            FundPageVM vm = new FundPageVM(userInfo);
            if (vm.managedFunds.isEmpty()) {
                setEmptySectionTips("无管理组合", "");
                changeVisibleSection(TYPE_EMPTY);
            } else {
                {
                    View capitalFundSection = v_findView(mContentSection, R.id.section_capital_fund);
                    if (!vm.capitalFunds.isEmpty()) {
                        LinearLayout listView = v_findView(capitalFundSection, R.id.listView);
                        FundCardViewHelper.resetFundCardListViewWithViewModel(listView, vm.capitalFunds, 0);
                        consumeEvent(FundCardViewHelper.createObservableUpdateListTimer(new WeakReference<RXFragment>(this), listView, 0))
                                .setTag("update_list_timer")
                                .done();
                    } else {
                        unsubscribeFromMain("update_list_timer");
                    }
                    v_setVisibility(capitalFundSection, vm.capitalFunds.isEmpty() ? View.GONE : View.VISIBLE);
                }
                {
                    View lockedFundSection = v_findView(mContentSection, R.id.section_locked_fund);
                    if (!vm.lockedFunds.isEmpty()) {
                        LinearLayout listView = v_findView(lockedFundSection, R.id.listView);
                        FundCardViewHelper.resetFundCardListViewWithViewModel(listView, vm.lockedFunds, 0);
                    }
                    v_setVisibility(lockedFundSection, vm.lockedFunds.isEmpty() ? View.GONE : View.VISIBLE);
                }

                BasicCell allFundCell = v_findView(mContentSection, R.id.cell_all_fund);
                if (!vm.managedFunds.isEmpty()) {
                    v_setText(allFundCell.getTitleLabel(), String.format(Locale.getDefault(), "全部组合(%d)", vm.managedFunds.size()));
                    v_setClick(allFundCell, v -> showActivity(this, an_MoreFundPage(vm.managedFunds)));
                }
                v_setVisibility(allFundCell, vm.managedFunds.isEmpty() ? View.GONE : View.VISIBLE);

                changeVisibleSection(TYPE_CONTENT);
            }
        }
    }

    private static class FundPageVM {
        List<FundCardViewModel> capitalFunds;
        List<FundCardViewModel> lockedFunds;
        List<FundBrief> managedFunds;

        public FundPageVM(User raw) {
            this.capitalFunds = safeGet(
                    () -> Stream.of(raw.trader.more.holdFunds)
                            .filter(it -> anyMatch(it.status, Fund_Status.Booking, Fund_Status.Capital))
                            .map(it -> FundCardViewHelper.createViewModel(it))
                            .collect(Collectors.toList()),
                    Collections.<FundCardViewModel>emptyList());
            this.lockedFunds = safeGet(
                    () -> Stream.of(raw.trader.more.holdFunds)
                            .filter(it -> anyMatch(it.status, Fund_Status.LockIn))
                            .map(it -> FundCardViewHelper.createViewModel(it))
                            .collect(Collectors.<FundCardViewModel>toList()),
                    Collections.<FundCardViewModel>emptyList());
            this.managedFunds = safeGet(() -> raw.trader.more.holdFunds, Collections.<FundBrief>emptyList());
        }
    }

    private static void setupLineChart(LineChart chart, LineData lineData) {
        if (chart != null && lineData != null) {
            chart.setTouchEnabled(false);
            chart.setDrawGridBackground(false);
            chart.setDrawBorders(false);
            chart.setDescription("");
            chart.getXAxis().setLabelsToSkip(Math.max(lineData.getXValCount() - 2, 0));
            Legend legend = chart.getLegend();
            legend.setTextColor(TEXT_GREY_COLOR);
            legend.setTextSize(10);

            XAxis xAxis = chart.getXAxis();
            xAxis.setDrawGridLines(false);
            xAxis.setDrawAxisLine(false);
            xAxis.setTextSize(10);
            xAxis.setAvoidFirstLastClipping(true);
            xAxis.setTextColor(TEXT_GREY_COLOR);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            chart.getAxis(YAxis.AxisDependency.LEFT).setEnabled(false);

            YAxis yAxis = chart.getAxis(YAxis.AxisDependency.RIGHT);
            yAxis.setStartAtZero(false);
            yAxis.setDrawGridLines(true);
            yAxis.setDrawAxisLine(false);
            yAxis.setGridLineWidth(dp2px(0.5f));
            yAxis.setGridColor(0xFFEEEEEE);
            yAxis.setValueFormatter(value -> formatRatio((double) value, false, 0, 2));
            yAxis.setTextSize(10);
            yAxis.setTextColor(TEXT_GREY_COLOR);

            boolean isAnimate = chart.getData() == null;
            chart.setData(lineData);
            if (isAnimate) {
                v_preDraw(chart, true, v -> chart.animateX(1000));
            }
        }
    }

    public static class UserDetailPageRealFragment extends SimpleFragment {

        private boolean mHasData = false;

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
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_user_detail_page_real, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            changeVisibleSection(TYPE_LOADING);
            v_setClick(mReloadSection, v -> {
                changeVisibleSection(TYPE_LOADING);
                requestFetchRealPerformanceFromParent(this);
            });

            opt(getStartFetchRealPerformanceSubjectFromParent(this))
                    .consume(subject -> {
                        consumeEvent(subject)
                                .setTag("on_start_fetch_performance")
                                .onNextFinish(ignored -> {
                                    if (!mHasData) {
                                        changeVisibleSection(TYPE_LOADING);
                                    }
                                })
                                .done();
                    });

            opt(getFinishFetchRealPerformanceSubjectFromParent(this))
                    .consume(subject -> {
                        consumeEvent(subject)
                                .setTag("on_finish_fetch_performance")
                                .onNextFinish(response -> {
                                    if (isSuccess(response)) {
                                        resetData();
                                        mHasData = true;
                                    } else {
                                        setReloadSectionTips(getErrorMessage(response));
                                        changeVisibleSection(TYPE_RELOAD);
                                    }
                                })
                                .done();
                    });

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mHasData = false;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (getRealPerformanceFromParent(this) == null) {
                    changeVisibleSection(TYPE_LOADING);
                    requestFetchRealPerformanceFromParent(this);
                    mHasData = false;
                } else {
                    resetData();
                    mHasData = true;
                }
            }
        }

        private void resetData() {
            RealPageVM vm = new RealPageVM(getRealPerformanceFromParent(this));

            {
                int cardID = R.id.card_performance_cn;
                int moneyType = CN;
                boolean hasPerformance = vm.hasCNPerformance;
                CharSequence accumulateIncome = vm.cnAccumulateIncome;
                CharSequence accumulateIncomeRatio = vm.cnAccumulateIncomeRatio;
                CharSequence investedFundCount = vm.cnInvestedFundCount;
                LineData lineData = vm.cnLineData;
                View card = v_findView(mContentSection, cardID);
                if (hasPerformance) {
                    v_setText(card, R.id.label_accumulate_income, accumulateIncome);
                    v_setText(card, R.id.label_accumulate_income_ratio, accumulateIncomeRatio);
                    v_setText(card, R.id.label_invested_fund_count, investedFundCount);


                    LineChart lineChart = v_findView(card, R.id.chart_performance);
                    if (lineData != null) {
                        setupLineChart(lineChart, lineData);
                    }
                    v_setVisibility(lineChart, lineData != null ? View.VISIBLE : View.GONE);
                    v_setClick(card, R.id.btn_check, v -> showActivity(this, an_MyStockAccountDetailPage(moneyType)));
                }
                v_setVisibility(card, hasPerformance ? View.VISIBLE : View.GONE);
            }
            {
                int cardID = R.id.card_performance_hk;
                int moneyType = HK;
                boolean hasPerformance = vm.hasHKPerformance;
                CharSequence accumulateIncome = vm.hkAccumulateIncome;
                CharSequence accumulateIncomeRatio = vm.hkAccumulateIncomeRatio;
                CharSequence investedFundCount = vm.hkInvestedFundCount;
                LineData lineData = vm.hkLineData;
                View card = v_findView(mContentSection, cardID);
                if (hasPerformance) {
                    v_setText(card, R.id.label_accumulate_income, accumulateIncome);
                    v_setText(card, R.id.label_accumulate_income_ratio, accumulateIncomeRatio);
                    v_setText(card, R.id.label_invested_fund_count, investedFundCount);


                    LineChart lineChart = v_findView(card, R.id.chart_performance);
                    if (lineData != null) {
                        setupLineChart(lineChart, lineData);
                    }
                    v_setVisibility(lineChart, lineData != null ? View.VISIBLE : View.GONE);
                    v_setClick(card, R.id.btn_check, v -> showActivity(this, an_MyStockAccountDetailPage(moneyType)));
                }
                v_setVisibility(card, hasPerformance ? View.VISIBLE : View.GONE);
            }
            {
                TextView view = v_findView(mContentSection, R.id.card_not_invest_cn_brief);
                view.setMovementMethod(new LinkMovementMethod());
                v_setText(mContentSection,
                        R.id.card_not_invest_cn_brief,
                        concatNoBreak(safeGet(() -> FortuneManager.getInstance().cnAccount.investMoney, 0D) == 0 ? "尚未开始投资," : "",
                                setColor(setClickEvent("查看沪深账户", v -> showActivity(getActivity(), an_MyStockAccountDetailPage(Money_Type.CN))), TEXT_BLUE_COLOR)));
            }
            {
                TextView view = v_findView(mContentSection, R.id.card_not_invest_hk_brief);
                view.setMovementMethod(new LinkMovementMethod());
                v_setText(mContentSection,
                        R.id.card_not_invest_hk_brief,
                        concatNoBreak(safeGet(() -> FortuneManager.getInstance().hkAccount.investMoney, 0D) == 0 ? "尚未开始投资," : "",
                                setColor(setClickEvent("查看港股账户", v -> showActivity(getActivity(), an_MyStockAccountDetailPage(Money_Type.HK))), TEXT_BLUE_COLOR)));
            }


            v_setVisibility(mContentSection, R.id.card_not_invest_cn, vm.hasCNPerformance ? View.GONE : View.VISIBLE);
            v_setVisibility(mContentSection, R.id.card_not_invest_hk, vm.hasHKPerformance ? View.GONE : View.VISIBLE);

            changeVisibleSection(TYPE_CONTENT);
        }
    }

    private static class RealPageVM {
        boolean hasCNPerformance;
        boolean hasHKPerformance;

        CharSequence cnAccumulateIncomeRatio;
        CharSequence cnAccumulateIncome;
        CharSequence cnInvestedFundCount;
        CharSequence hkAccumulateIncomeRatio;
        CharSequence hkAccumulateIncome;
        CharSequence hkInvestedFundCount;

        @Nullable
        LineData cnLineData;
        @Nullable
        LineData hkLineData;

        @SuppressWarnings("ConstantConditions")
        public RealPageVM(List<RealUserIncomeChart> raw) {
            RealUserIncomeChart cnPerformance = safeGet(() -> Stream.of(raw).filter(it -> it.money == Money_Type.CN).findFirst().orElse(null), null);
            RealUserIncomeChart hkPerformance = safeGet(() -> Stream.of(raw).filter(it -> it.money == Money_Type.HK).findFirst().orElse(null), null);

            this.hasCNPerformance = cnPerformance != null;
            this.hasHKPerformance = hkPerformance != null;

            this.cnAccumulateIncomeRatio = safeGet(() -> setColor(formatRatio(cnPerformance.totalIncomeRatio, false, 2), getIncomeTextColor(cnPerformance.totalIncomeRatio)), setColor(PlaceHolder.NULL_VALUE, RISE_COLOR));
            this.cnAccumulateIncome = safeGet(() -> setColor(formatMoney(cnPerformance.totalIncome, false, 2), getIncomeTextColor(cnPerformance.totalIncome)), setColor(PlaceHolder.NULL_VALUE, RISE_COLOR));
            this.cnInvestedFundCount = safeGet(() -> setColor(String.valueOf(cnPerformance.investFundCount), TEXT_BLUE_COLOR), setColor(PlaceHolder.NULL_VALUE, TEXT_BLUE_COLOR));
            this.hkAccumulateIncomeRatio = safeGet(() -> setColor(formatRatio(hkPerformance.totalIncomeRatio, false, 2), getIncomeTextColor(hkPerformance.totalIncomeRatio)), setColor(PlaceHolder.NULL_VALUE, RISE_COLOR));
            this.hkAccumulateIncome = safeGet(() -> setColor(formatMoney(hkPerformance.totalIncome, false, 2), getIncomeTextColor(hkPerformance.totalIncome)), setColor(PlaceHolder.NULL_VALUE, RISE_COLOR));
            this.hkInvestedFundCount = safeGet(() -> setColor(String.valueOf(hkPerformance.investFundCount), TEXT_BLUE_COLOR), setColor(PlaceHolder.NULL_VALUE, TEXT_BLUE_COLOR));

            this.cnLineData = generateLineData(cnPerformance);
            this.hkLineData = generateLineData(hkPerformance);
        }

        private LineData generateLineData(RealUserIncomeChart raw) {
            return safeGet(() -> {
                List<String> xValues = raw.timeData;
                LineData lineData = new LineData(xValues);

                {
                    List<Double> userData = raw.valueData;
                    ArrayList<Entry> entries = new ArrayList<>(userData.size());
                    int index = 0;
                    for (Double value : userData) {
                        entries.add(new Entry(value.floatValue(), index++));
                    }
                    LineDataSet userLineDataSet = new LineDataSet(entries, raw.valueName);
                    userLineDataSet.setColor(RED_COLOR);
                    userLineDataSet.setDrawCubic(true);
                    userLineDataSet.setDrawCircles(false);
                    userLineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
                    userLineDataSet.setLineWidth(dp2px(0.8f));
                    lineData.addDataSet(userLineDataSet);
                }

                {
                    List<Double> contrastData = raw.contrastData;
                    ArrayList<Entry> entries = new ArrayList<>(contrastData.size());
                    int index = 0;
                    for (Double value : contrastData) {
                        entries.add(new Entry(value.floatValue(), index++));
                    }
                    LineDataSet contrastLineDataSet = new LineDataSet(entries, raw.contrastName);
                    contrastLineDataSet.setColor(GREY_COLOR);
                    contrastLineDataSet.setDrawCubic(true);
                    contrastLineDataSet.setDrawCircles(false);
                    contrastLineDataSet.setLineWidth(dp2px(0.8f));
                    contrastLineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
                    lineData.addDataSet(contrastLineDataSet);
                }

                lineData.setDrawValues(false);
                return lineData;
            }, null);
        }

    }

    public static class UserDetailPageSimulationFragment extends SimpleFragment {
        private boolean mHasData = false;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_user_detail_page_simulation, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            changeVisibleSection(TYPE_LOADING);

            v_setClick(mReloadSection, v -> {
                changeVisibleSection(TYPE_LOADING);
                requestFetchSimulationPerformanceFromParent(this);
            });

            opt(getStartFetchSimulationPerformanceSubjectFromParent(this))
                    .consume(subject -> {
                        consumeEvent(subject)
                                .setTag("on_start_fetch_performance")
                                .onNextFinish(ignored -> {
                                    if (!mHasData) {
                                        changeVisibleSection(TYPE_LOADING);
                                    }
                                })
                                .done();
                    });

            opt(getFinishFetchSimulationPerformanceSubjectFromParent(this))
                    .consume(subject -> {
                        consumeEvent(subject)
                                .setTag("on_finish_fetch_performance")
                                .onNextFinish(response -> {
                                    if (isSuccess(response)) {
                                        resetData();
                                        mHasData = true;
                                    } else {
                                        SimulationFailVM failVm = getSimulationPerformanceFailVmFromParent(this);
                                        if (failVm != null && failVm.mErrCode == 100015) {
                                            setReloadSectionTips(failVm.mErrorMsg);
                                        } else {
                                            setReloadSectionTips(getErrorMessage(response));
                                        }
                                        changeVisibleSection(TYPE_RELOAD);

                                    }
                                })
                                .done();
                    });

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mHasData = false;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (getSimulationPerformanceFromParent(this) == null) {
                    SimulationFailVM failVm = getSimulationPerformanceFailVmFromParent(this);
                    if (failVm != null && failVm.mErrCode == 100015) {
                        changeVisibleSection(TYPE_RELOAD);
                        setReloadSectionTips(failVm.mErrorMsg);
                    } else {
                        changeVisibleSection(TYPE_LOADING);
                        requestFetchSimulationPerformanceFromParent(this);
                        mHasData = false;
                    }
                } else {
                    resetData();
                    mHasData = true;
                }
            }
        }

        public void resetData() {

            SimulationPageVM vm = new SimulationPageVM(getSimulationPerformanceFromParent(this));
            List<SimulationUserIncomeChart.BestMatch> matchList = vm.bestMatchList;
            View rankCard = v_findView(mContentSection, R.id.card_rank);
            LinearLayout historyRankContainer = v_findView(rankCard, R.id.section_rank_history);
            historyRankContainer.removeAllViews();
            v_setVisibility(rankCard, R.id.label_no_rank, matchList.isEmpty() ? View.VISIBLE : View.GONE);
            v_setVisibility(historyRankContainer, matchList.isEmpty() ? View.GONE : View.VISIBLE);
            Stream.of(matchList).map(SimulationRankVM::new).map(it -> createCell(getActivity(), historyRankContainer, it)).forEach(cell -> historyRankContainer.addView(cell));

            TextView rankLabel = v_findView(rankCard, R.id.label_rank);
            if (vm.isInRank) {
                int textColor = TEXT_IRON_COLOR;
                int backgroundResource = R.mipmap.bg_rank_iron;
                int textSize = sp2px(12);
                if (inRange(vm.rankPosition, 1, 9)) {
                    textColor = TEXT_GOLD_COLOR;
                    backgroundResource = R.mipmap.bg_rank_gold;
                    textSize = sp2px(20);
                } else if (inRange(vm.rankPosition, 10, 99)) {
                    textColor = TEXT_SILVER_COLOR;
                    backgroundResource = R.mipmap.bg_rank_silver;
                    textSize = sp2px(18);
                } else if (inRange(vm.rankPosition, 100, 999)) {
                    textColor = TEXT_COPPER_COLOR;
                    backgroundResource = R.mipmap.bg_rank_copper;
                    textSize = sp2px(16);
                }

                rankLabel.setText(setFontSize(String.valueOf(vm.rankPosition), textSize));
                rankLabel.setTextColor(textColor);
                rankLabel.setBackgroundResource(backgroundResource);
            } else {
                rankLabel.setText(setFontSize("未上榜", sp2px(14)));
                rankLabel.setTextColor(TEXT_IRON_COLOR);
                rankLabel.setBackgroundResource(R.mipmap.bg_rank_iron);
            }

            View performanceCard = v_findView(mContentSection, R.id.card_performance);
            v_setText(performanceCard, R.id.label_winning, vm.historyWinning);
            v_setText(performanceCard, R.id.label_total_income, vm.totalIncome);
            v_setText(performanceCard, R.id.label_trade_count, vm.tradeCount);

            LineChart chart = v_findView(performanceCard, R.id.chart_performance);
            if (vm.lineData != null) {
                setupLineChart(chart, vm.lineData);
            }
            v_setVisibility(chart, vm.lineData != null ? View.VISIBLE : View.GONE);
            v_setClick(performanceCard, R.id.btn_check, v -> {
                int userID = Integer.valueOf(opt(getUserIDFromParent(this)).or("-1"));
                if (userID != -1) {
                    showActivity(this, an_StockTradePage(userID, null, 0));
                    UmengUtil.stat_click_event(UmengUtil.eEVENTIDHomePageTraderMore);
                }
            });
            v_setVisibility(performanceCard, R.id.section_has_chart, vm.lineData != null ? View.VISIBLE : View.GONE);
            v_setVisibility(performanceCard, R.id.section_no_chart, vm.lineData != null ? View.GONE : View.VISIBLE);
            v_setVisibility(mContentSection, R.id.label_hint, vm.isInRank ? View.VISIBLE : View.GONE);

            changeVisibleSection(TYPE_CONTENT);
        }

        private View createCell(Context ctx, ViewGroup parent, SimulationRankVM vm) {
            View cell = LayoutInflater.from(ctx).inflate(R.layout.cell_history_rank_user, parent, false);
            TextView rankLabel = v_findView(cell, R.id.label_rank);
            rankLabel.setText(setFontSize(String.valueOf(vm.matchRank), sp2px(28)));
            rankLabel.setTextColor(TEXT_GOLD_COLOR);
            rankLabel.setBackgroundResource(R.mipmap.bg_rank_gold);
            v_setText(cell, R.id.label_match_income_ratio, vm.incomeRatio);
            v_setText(cell, R.id.label_match_time, vm.matchTime);
            v_setText(cell, R.id.label_match_name, vm.matchName);
            return cell;
        }
    }

    private static class SimulationPageVM {
        public boolean isInRank;
        public int rankPosition;
        public CharSequence historyWinning;
        public CharSequence totalIncome;
        public CharSequence tradeCount;
        @Nullable
        public LineData lineData;
        public List<SimulationUserIncomeChart.BestMatch> bestMatchList;

        public SimulationPageVM(SimulationUserIncomeChart raw) {
            this.isInRank = safeGet(() -> raw.rankPosition >= 1, false);
            this.rankPosition = safeGet(() -> raw.rankPosition, 0);
            this.historyWinning = safeGet(() -> formatRatio(raw.winRatio, false, 2), PlaceHolder.NULL_VALUE);
            this.totalIncome = safeGet(() -> setColor(formatRatio(raw.account.totalIncomeRatio, false, 2), getIncomeTextColor(raw.account.totalIncomeRatio)), setColor(PlaceHolder.NULL_VALUE, RISE_COLOR));
            this.tradeCount = setColor(safeGet(() -> String.valueOf(raw.tradeCount), PlaceHolder.NULL_VALUE), TEXT_BLUE_COLOR);
            this.lineData = generateLineData(raw);
            this.bestMatchList = safeGet(() -> raw.bestMatchList, Collections.<SimulationUserIncomeChart.BestMatch>emptyList());
        }

        private LineData generateLineData(SimulationUserIncomeChart raw) {
            return safeGet(() -> {
                List<String> xValues = raw.timeData;
                LineData lineData = new LineData(xValues);

                {
                    List<Double> userData = raw.valueData;
                    ArrayList<Entry> entries = new ArrayList<>(userData.size());
                    int index = 0;
                    for (Double value : userData) {
                        entries.add(new Entry(value.floatValue(), index++));
                    }
                    LineDataSet userLineDataSet = new LineDataSet(entries, raw.valueName);
                    userLineDataSet.setColor(RED_COLOR);
                    userLineDataSet.setDrawCubic(true);
                    userLineDataSet.setDrawCircles(false);
                    userLineDataSet.setLineWidth(dp2px(0.8f));
                    userLineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
                    lineData.addDataSet(userLineDataSet);
                }

                {
                    List<Double> contrastData = raw.contrastData;
                    ArrayList<Entry> entries = new ArrayList<>(contrastData.size());
                    int index = 0;
                    for (Double value : contrastData) {
                        entries.add(new Entry(value.floatValue(), index++));
                    }
                    LineDataSet contrastLineDataSet = new LineDataSet(entries, raw.contrastName);
                    contrastLineDataSet.setColor(GREY_COLOR);
                    contrastLineDataSet.setDrawCubic(true);
                    contrastLineDataSet.setDrawCircles(false);
                    contrastLineDataSet.setLineWidth(dp2px(0.8f));
                    contrastLineDataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
                    lineData.addDataSet(contrastLineDataSet);
                }

                lineData.setDrawValues(false);
                return lineData;
            }, null);
        }

    }

    public static class SimulationRankVM {
        public CharSequence matchRank;
        public CharSequence incomeRatio;
        public CharSequence matchTime;
        public CharSequence matchName;

        public SimulationRankVM(SimulationUserIncomeChart.BestMatch match) {
            GMFRankUser result = safeGet(() -> match.result, null);
            GMFMatch matchInfo = safeGet(() -> match.matchInfo, null);
            GMFRankUser.UserPoint point = result == null ? null : safeGet(() -> result.point, null);
            this.matchRank = "" + safeGet(() -> match.result.position, 0);
            this.incomeRatio = concatNoBreak("收益率：", setColor(safeGet(() -> formatRatio(point == null ? null : point.value, false, 2)).def(PlaceHolder.NULL_VALUE).get(), getIncomeTextColor(point == null ? null : point.value, RISE_COLOR)));
            this.matchTime = concatNoBreak("参赛日期：", safeGet(() -> formatSecond(match.matchInfo.startTime, "yyyy.MM.dd")).def(PlaceHolder.NULL_VALUE).get()
                    , "-", safeGet(() -> formatSecond(matchInfo == null ? null : matchInfo.stopTime, "MM.dd")).def(PlaceHolder.NULL_VALUE).get());
            this.matchName = concatNoBreak("比赛名称：", safeGet(() -> matchInfo == null ? null : matchInfo.title).def(PlaceHolder.NULL_VALUE).get(), " ", safeGet(() -> matchInfo == null ? null : matchInfo.count, "--") + "人");
        }
    }

    public static class UserDetailPageFeedFragment extends SimpleFragment {
        private boolean mIsFetchingMore = false;

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
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_user_detail_page_feed, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            changeVisibleSection(TYPE_LOADING);

            v_setClick(mReloadSection, v -> {
                setUserVisibleHint(true);
            });

            opt(getStartFetchFeedPageArrayFromParent(this))
                    .consume(subject -> {
                        consumeEvent(subject)
                                .onNextFinish(nil -> {
                                    CommandPageArray<Feed> pageArray = getFeedPageArrayFromParent(this);
                                    if (pageArray == null) {
                                        changeVisibleSection(TYPE_LOADING);
                                    }
                                })
                                .done();
                    });

            opt(getFinishFetchFeedPageArrayFromParent(this))
                    .consume(subject -> {
                        consumeEventMR(subject)
                                .onNextSuccess(response -> {
                                    if (getUserVisibleHint()) {
                                        setUserVisibleHint(true);
                                    }
                                })
                                .onNextFail(response -> {
                                    changeVisibleSection(TYPE_RELOAD);
                                    setReloadSectionTips(getErrorMessage(response));
                                })
                                .done();
                    });

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mIsFetchingMore = false;
            safeCall(() -> {
                RecyclerView recyclerView = v_findView(this, R.id.recyclerView);
                if (recyclerView != null) {
                    FeedListAdapter adapter = (FeedListAdapter) recyclerView.getAdapter();
                    if (adapter != null)
                        adapter.release();
                }
            });
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                CommandPageArray<Feed> pageArray = getFeedPageArrayFromParent(this);
                if (pageArray == null) {
                    changeVisibleSection(TYPE_LOADING);
                    requestFetchFeedPageArrayFromParent(this);
                } else {
                    resetRecyclerView(pageArray);
                }
            }
        }

        private void resetRecyclerView(CommandPageArray<Feed> pageArray) {
            RecyclerView recyclerView = v_findView(this, R.id.recyclerView);
            List<CellVMUnion> items = Stream.of(pageArray.data())
                    .filter(it -> it instanceof PersonalFeed || it instanceof FeedOrder)
                    .map(it -> new CellVMUnion(it))
                    .collect(Collectors.toList());
            if (recyclerView.getAdapter() != null) {
                FeedListAdapter adapter = (FeedListAdapter) recyclerView.getAdapter();
                adapter.resetItems(items);
            } else {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                addCellVerticalSpacing(recyclerView, dp2px(10));

                FrameLayout topLayer = v_findView(this, R.id.embed_root);
                FeedListAdapter adapter = new FeedListAdapter(items, () -> topLayer);
                recyclerView.setAdapter(adapter);
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (!mIsFetchingMore && hasMoreData(pageArray) && isScrollToBottom(recyclerView)) {
                            mIsFetchingMore = true;
                            consumeEventMR(PageArrayHelper.getNextPage(pageArray))
                                    .onNextSuccess(response -> resetRecyclerView(pageArray))
                                    .onNextFinish(response -> mIsFetchingMore = false)
                                    .done();
                        }
                    }
                });
            }
            if (items.isEmpty()) {
                changeVisibleSection(TYPE_EMPTY);
            } else {
                changeVisibleSection(TYPE_CONTENT);
            }
        }
    }

    private static final int FEED_LIST_VIEW_TYPE_CIRCLE = 0;
    private static final int FEED_LIST_VIEW_TYPE_STOCK = 1;

    private static class FeedListAdapter extends RecyclerView.Adapter<FeedListViewHolder> {
        private List<CellVMUnion> mItems;
        private Func0<FrameLayout> mTopLayerGetter;
        private IntCounter counter = CircleHelper.createRewardCounter();

        public FeedListAdapter(List<CellVMUnion> items, Func0<FrameLayout> topLayerGetter) {
            mItems = new LinkedList<>(items);
            mTopLayerGetter = topLayerGetter;
        }

        public void release() {
            counter.release();
        }

        @Override
        public FeedListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            int layoutID = anyMatch(viewType, FEED_LIST_VIEW_TYPE_CIRCLE) ? R.layout.cell_circle_list : R.layout.cell_stock_feed;
            View itemView = inflater.inflate(layoutID, parent, false);
            FeedListViewHolder holder = new FeedListViewHolder(itemView, viewType);
            holder.onViewHolderCreated(this, viewType);
            return holder;
        }

        @Override
        public void onBindViewHolder(FeedListViewHolder holder, int position) {
            CellVMUnion item = mItems.get(position);
            holder.configureView(item, position);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).circleVM != null ? FEED_LIST_VIEW_TYPE_CIRCLE : FEED_LIST_VIEW_TYPE_STOCK;
        }

        public CellVMUnion getItem(int position) {
            return mItems.get(position);
        }

        public void resetItems(List<CellVMUnion> items) {
            mItems = new LinkedList<>(items);
            notifyDataSetChanged();
        }
    }

    private static class FeedListViewHolder extends RecyclerView.ViewHolder {
        private ChildBinder childBinder;

        public FeedListViewHolder(View itemView, int viewType) {
            super(itemView);
            childBinder = ChildBinders.createWithView(itemView);
            if (viewType == FEED_LIST_VIEW_TYPE_CIRCLE) {
                CircleHelper.bindChildViews(childBinder);
            } else if (viewType == FEED_LIST_VIEW_TYPE_STOCK) {
                StockFeedHelper.bindChildViews(childBinder);
            }
        }

        public void onViewHolderCreated(FeedListAdapter adapter, int viewType) {
            if (viewType == FEED_LIST_VIEW_TYPE_CIRCLE) {
                Func0<CellVM> itemGetter = () -> adapter.getItem(getAdapterPosition()).circleVM;
                Func0<Boolean> ignoreAvatarClickEvent = () -> true;
                Action0 rewardCallback = () -> {
                };
                CircleHelper.afterViewHolderCreated(itemView, itemGetter, adapter.counter, adapter.mTopLayerGetter.call(), ignoreAvatarClickEvent, rewardCallback);
            } else if (viewType == FEED_LIST_VIEW_TYPE_STOCK) {
                CircleHelper.ItemStore<StockFeedVM> store = pos -> adapter.getItem(pos).stockVM;
                StockFeedHelper.afterViewHolderCreated(store, this);
            }
        }

        public void configureView(CellVMUnion vm, int pos) {
            if (vm.circleVM != null) {
                Func0<Boolean> isContentNotChange = () -> getAdapterPosition() == pos;
                CircleHelper.configureView(childBinder, isContentNotChange, vm.circleVM, CircleHelper.FLAG_SHOW_BAR_INFO);
            } else if (vm.stockVM != null) {
                StockFeedHelper.configureView(childBinder, vm.stockVM, 0);
            }
        }
    }

    private static class CellVMUnion {
        private CellVM circleVM;
        private StockFeedVM stockVM;

        public CellVMUnion(Feed feed) {
            if (feed instanceof PersonalFeed) {
                PersonalFeed personalFeed = PersonalFeed.class.cast(feed);
                this.circleVM = new CellVM(personalFeed.sesion, personalFeed.message).optimize();
            } else if (feed instanceof FeedOrder) {
                this.stockVM = new StockFeedVM(FeedOrder.class.cast(feed));
            }
        }
    }

    public static class UserFollowOrFansFragment extends SimpleFragment {

        private RecyclerView mRecyclerView;
        private boolean mDataIsExpired = true;
        private User mUser;
        private int mFollowType;
        private boolean mIsLoadingMore = false;
        private CommandPageArray<User> mUserArray;

        public UserFollowOrFansFragment init(int type) {
            Bundle arguments = new Bundle();
            arguments.putInt("follow_type", type);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mUser = GlobalVariableDic.shareInstance().pop(CommonProxyActivity.KEY_USER);
            mFollowType = getArguments().getInt("follow_type");
            return inflater.inflate(R.layout.frag_user_attention_and_fans, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));
            changeVisibleSection(TYPE_LOADING);
            updateTitle("");

            mRecyclerView = v_findView(this, R.id.recyclerView);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            addHorizontalSepLine(mRecyclerView, new Rect(dp2px(50), 0, 0, 0));
            setOnSwipeRefreshListener(() -> resetData(false));
            AdvanceSwipeRefreshLayout.class
                    .cast(mRefreshLayout)
                    .setOnPreInterceptTouchEventDelegate(ev -> !isScrollToTop(mRecyclerView));

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mDataIsExpired = true;
            mIsLoadingMore = false;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (mDataIsExpired) {
                    resetData(false);
                    mDataIsExpired = false;
                }
            }
        }

        private void resetData(boolean reload) {
            consumeEventMRUpdateUI(SimulationController.freshFollowUser(mUser, mFollowType), reload)
                    .setTag("fetch_follow")
                    .onNextFinish(response -> {
                        if (isSuccess(response)) {
                            CommandPageArray<User> data = response.data;
                            resetRecyclerView(data);
                        } else {
                            changeVisibleSection(TYPE_RELOAD);
                            setReloadSectionTips(getErrorMessage(response));
                        }
                    })
                    .done();
        }

        private void resetRecyclerView(CommandPageArray<User> userArray) {
            List<UserFollowOrFansVM> items = Stream.of(opt(userArray).let(it -> it.data()).or(Collections.emptyList())).map(UserFollowOrFansVM::new).collect(Collectors.toList());
            updateTitle(mUser.getName() + "的" + ((mFollowType == UserManager.Follow_Type_Follow) ? "关注" : "粉丝") + "(" + items.size() + ")");
            if (mRecyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<UserFollowOrFansVM> adapter = getSimpleAdapter(mRecyclerView);
                adapter.resetItems(items);
            } else {

                SimpleRecyclerViewAdapter<UserFollowOrFansVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                        .onCreateItemView(R.layout.cell_attention_or_fans)
                        .onCreateViewHolder(builder -> {
                            builder.bindChildWithTag("avatarImage", R.id.img_avatar)
                                    .bindChildWithTag("userNameLabel", R.id.label_user_name)
                                    .bindChildWithTag("attentionLabel", R.id.label_attention)
                                    .configureView((item, pos) -> {
                                        UserAvatarView avatarImage = builder.getChildWithTag("avatarImage");
                                        avatarImage.updateView(item.imageUrl, item.userType);
                                        v_setTextWithFitBound(builder.getChildWithTag("userNameLabel"), item.userName);
                                        TextView attentionLabel = builder.getChildWithTag("attentionLabel");
                                        int loginUserId = safeGet(() -> MineManager.getInstance().getmMe().index, 0);
                                        if (item.userId == loginUserId) {
                                            v_setGone(attentionLabel);
                                        } else {
                                            attentionLabel.setText(item.hasFollow ? "已关注" : "关注");
                                            attentionLabel.setCompoundDrawablesWithIntrinsicBounds(item.hasFollow ? R.mipmap.ic_attention_black : R.mipmap.ic_unattention_black, 0, 0, 0);
                                            attentionLabel.setBackgroundResource(item.hasFollow ? R.drawable.shape_round_stroke_button_grey_pressed : R.drawable.shape_round_stroke_button_grey_normal);
                                            v_setVisible(attentionLabel);
                                        }
                                    });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            TextView attentionLabel = v_findView(holder.itemView, R.id.label_attention);
                            v_setClick(attentionLabel, v -> {
                                if (!MineManager.getInstance().isLoginOK()) {
                                    showActivity(this, an_LoginPage());
                                    return;
                                }
                                UserFollowOrFansVM vm = ad.getItem(holder.getAdapterPosition());
                                if (vm.hasFollow) {
                                    GMFBottomSheet sheet = new GMFBottomSheet.Builder(getActivity())
                                            .setTitle("确定不再关注" + vm.userName + "?")
                                            .addContentItem(new GMFBottomSheet.BottomSheetItem(0, setColor("不再关注", RED_COLOR), 0))
                                            .create();
                                    sheet.setOnItemClickListener((bottomSheet, item) -> {
                                        bottomSheet.dismiss();
                                        attentionLabel.setText("关注");
                                        attentionLabel.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_unattention_black, 0, 0, 0);
                                        attentionLabel.setBackgroundResource(R.drawable.shape_round_stroke_button_grey_normal);
                                        consumeEventMR(SimulationController.deleteFollowUser(vm.raw))
                                                .onNextSuccess(response -> NotificationCenter.userFollowCountChangedSubject.onNext(null))
                                                .onNextFail(response -> getErrorMessage(response))
                                                .done();
                                        vm.hasFollow = !vm.hasFollow;
                                    });
                                    sheet.show();
                                } else {
                                    attentionLabel.setText("已关注");
                                    attentionLabel.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_attention_black, 0, 0, 0);
                                    attentionLabel.setBackgroundResource(R.drawable.shape_round_stroke_button_grey_pressed);
                                    consumeEventMR(SimulationController.addFollowUser(vm.raw))
                                            .onNextSuccess(response -> NotificationCenter.userFollowCountChangedSubject.onNext(null))
                                            .onNextFail(response -> getErrorMessage(response))
                                            .done();
                                    vm.hasFollow = !vm.hasFollow;
                                }
                            });

                            v_setClick(holder.itemView, v -> {
                                UserFollowOrFansVM vm = ad.getItem(holder.getAdapterPosition());
                                showActivity(this, an_UserDetailPage(vm.raw));
                            });
                        })
                        .create();
                adapter.addFooter(adapter.createFooterView(getActivity(), R.layout.footer_loading_more));
                adapter.addFooter(adapter.createFooterView(getContext(), R.layout.footer_no_more_data));
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (isScrollToBottom(recyclerView) && !mIsLoadingMore && hasMoreData(userArray)) {
                            mIsLoadingMore = true;
                            consumeEventMR(PageArrayHelper.getNextPage(userArray))
                                    .onNextSuccess(response -> resetRecyclerView(userArray))
                                    .onNextFinish(response -> mIsLoadingMore = false)
                                    .done();
                        }
                    }
                });
                mRecyclerView.setAdapter(adapter);
            }

            SimpleRecyclerViewAdapter adapter = (SimpleRecyclerViewAdapter) mRecyclerView.getAdapter();
            View loadingMoreFooter = adapter.getFooter(0);
            View noMoreDataFooter = adapter.getFooter(1);
            loadingMoreFooter.setVisibility(hasMoreData(userArray) ? View.VISIBLE : View.GONE);
            noMoreDataFooter.setVisibility(hasMoreData(userArray) ? View.GONE : View.VISIBLE);

            if (items.isEmpty()) {
                changeVisibleSection(TYPE_EMPTY);
            } else {
                changeVisibleSection(TYPE_CONTENT);
            }
        }
    }

    public static class UserFollowOrFansVM {

        private User raw;
        private int userId;
        private int userType;
        private String imageUrl;
        private CharSequence userName;
        private boolean hasFollow;

        public UserFollowOrFansVM(User user) {
            this.raw = user;
            this.userId = safeGet(() -> user.index, 0);
            this.userType = safeGet(() -> user.type, User_Type.Custom);
            this.imageUrl = safeGet(() -> user.getPhotoUrl(), "");
            this.userName = safeGet(() -> user.getName(), "");
            this.hasFollow = safeGet(() -> user.hasFollow, false);
        }
    }

}
