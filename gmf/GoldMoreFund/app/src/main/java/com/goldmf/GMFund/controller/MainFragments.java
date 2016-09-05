package com.goldmf.GMFund.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.Shape;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
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
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.goldmf.GMFund.BuildConfig;
import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.MyConfig;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.controller.FocusImagePagerAdapter.FocusImageCellVM;
import com.goldmf.GMFund.controller.FundFragments.AllFundFragment;
import com.goldmf.GMFund.controller.business.CashController;
import com.goldmf.GMFund.controller.business.ChatController;
import com.goldmf.GMFund.controller.business.CommonController;
import com.goldmf.GMFund.controller.business.FundController;
import com.goldmf.GMFund.controller.business.ScoreController;
import com.goldmf.GMFund.controller.business.StockController;
import com.goldmf.GMFund.controller.business.UserController;
import com.goldmf.GMFund.controller.chat.ChatFragments;
import com.goldmf.GMFund.controller.chat.ChatViewModels.ConversationListCellVM;
import com.goldmf.GMFund.controller.circle.CircleHelper;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.controller.dialog.SigningDialog;
import com.goldmf.GMFund.controller.internal.ChildBinder;
import com.goldmf.GMFund.controller.internal.ChildBinders;
import com.goldmf.GMFund.controller.internal.FundCardViewHelper;
import com.goldmf.GMFund.controller.internal.FundCardViewHelper.FundCardViewModel;
import com.goldmf.GMFund.controller.internal.StockFeedHelper;
import com.goldmf.GMFund.controller.internal.StockMatchHomeCardHelper;
import com.goldmf.GMFund.controller.internal.StockMatchHomeCardHelper.StockMatchHomeVM;
import com.goldmf.GMFund.controller.internal.TraderCardViewHelper;
import com.goldmf.GMFund.controller.internal.TraderCardViewHelper.TraderCardVM;
import com.goldmf.GMFund.extension.ListExtension;
import com.goldmf.GMFund.extension.MResultExtension;
import com.goldmf.GMFund.extension.RecyclerViewExtension;
import com.goldmf.GMFund.extension.SimulationAccountExtension;
import com.goldmf.GMFund.extension.ViewExtension;
import com.goldmf.GMFund.manager.BaseManager;
import com.goldmf.GMFund.manager.common.RedPoint;
import com.goldmf.GMFund.manager.discover.DiscoverManager;
import com.goldmf.GMFund.manager.discover.FocusInfo;
import com.goldmf.GMFund.manager.discover.TotalInfo;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.manager.fortune.FundFamily;
import com.goldmf.GMFund.manager.message.MessageSession;
import com.goldmf.GMFund.manager.message.SessionGroup;
import com.goldmf.GMFund.manager.message.SessionManager;
import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.score.DayActionManager;
import com.goldmf.GMFund.manager.score.ScoreAccount.ScoreAction;
import com.goldmf.GMFund.manager.score.ScoreManager;
import com.goldmf.GMFund.manager.stock.GMFLiveItem;
import com.goldmf.GMFund.manager.stock.SimulationStockManager;
import com.goldmf.GMFund.manager.stock.TopTrader;
import com.goldmf.GMFund.manager.trader.TraderManager;
import com.goldmf.GMFund.model.CommonDefine;
import com.goldmf.GMFund.model.CommonDefine.PlaceHolder;
import com.goldmf.GMFund.model.FeedOrder;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.FundBrief.Fund_Status;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.model.GMFMatch;
import com.goldmf.GMFund.model.SimulationAccount;
import com.goldmf.GMFund.model.StockInfo.StockBrief;
import com.goldmf.GMFund.model.StockInfo.StockSimple;
import com.goldmf.GMFund.model.StockPosition;
import com.goldmf.GMFund.model.TarLinkButton.TarLinkText;
import com.goldmf.GMFund.model.User.User_Type;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.PageArrayHelper;
import com.goldmf.GMFund.util.FrescoHelper;
import com.goldmf.GMFund.util.IntCounter;
import com.goldmf.GMFund.util.PersistentObjectUtil;
import com.goldmf.GMFund.util.SecondUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.AdvanceNestedScrollView;
import com.goldmf.GMFund.widget.BasicCell;
import com.goldmf.GMFund.widget.FoldableGallery;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.goldmf.GMFund.widget.LoopPlayTextsView;
import com.goldmf.GMFund.widget.QACell;
import com.goldmf.GMFund.widget.SectionHeader;
import com.goldmf.GMFund.widget.StockAccountProfileView;
import com.goldmf.GMFund.widget.StockIndexBriefView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.yale.ui.support.AdvanceSwipeRefreshLayout;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action3;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;
import yale.extension.common.Optional;
import yale.extension.common.animation.ViewAnimationProxy;
import yale.extension.common.shape.RoundCornerShape;
import yale.extension.rx.ConsumeEventChain;
import yale.extension.system.SimpleRecyclerViewAdapter;
import yale.extension.system.SimpleViewHolder;

import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.business.MatchController.refreshStockMatch;
import static com.goldmf.GMFund.controller.business.MatchController.refreshStockMatchWithMine;
import static com.goldmf.GMFund.controller.business.MatchController.signupMatch;
import static com.goldmf.GMFund.controller.business.ScoreController.gainTodayScore;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_AboutPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_AllStockCompetePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_AllTalentPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_AllTraderPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_AwardHomePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_CashJournalPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_CouponListPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_DevModePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_FeedbackPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_FocusStockEditPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_LoginPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ManagedGroupsPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_MineStockCompetePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_MoreFundPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_MyStockAccountDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_OpenSimulationAccountPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_QuotationDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_RechargePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ScoreHomePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockMarketLivePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockMatchDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockSearchPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockTradePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_UserDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WebViewPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.GREEN_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LIGHT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_SEP_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_LIGHT_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.YELLOW_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getFiveOrderPriceColor;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getIncomeTextColor;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.IntExtension.notMatch;
import static com.goldmf.GMFund.extension.ListExtension.getFromList;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.MResultExtension.zipToList;
import static com.goldmf.GMFund.extension.ObjectExtension.apply;
import static com.goldmf.GMFund.extension.ObjectExtension.notNull;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addCellVerticalSpacing;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addHorizontalSepLine;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.getSimpleAdapter;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.isScrollToBottom;
import static com.goldmf.GMFund.extension.SimulationAccountExtension.isNeedToCheckSimulationAccountState;
import static com.goldmf.GMFund.extension.SimulationAccountExtension.isOpenedSimulationAccount;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setStyle;
import static com.goldmf.GMFund.extension.UIControllerExtension.createAlertDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.getScreenSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_getGlobalHitRect;
import static com.goldmf.GMFund.extension.ViewExtension.v_getSizePreDraw;
import static com.goldmf.GMFund.extension.ViewExtension.v_isVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_preDraw;
import static com.goldmf.GMFund.extension.ViewExtension.v_removeFromParent;
import static com.goldmf.GMFund.extension.ViewExtension.v_reviseTouchArea;
import static com.goldmf.GMFund.extension.ViewExtension.v_setBehavior;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setEnabled;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageResource;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setInvisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_setLongClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setOnRefreshing;
import static com.goldmf.GMFund.extension.ViewExtension.v_setSelected;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setTextWithFitBound;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_updateLayoutParams;
import static com.goldmf.GMFund.extension.ViewGroupExtension.v_forEach;
import static com.goldmf.GMFund.manager.score.DayActionManager.DayInfo;
import static com.goldmf.GMFund.model.GMFMatch.STATE_ING;
import static com.goldmf.GMFund.model.GMFMatch.STATE_OVER;
import static com.goldmf.GMFund.model.GMFMatch.STATE_SIGNUP;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.getData;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.getPreviousPage;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.hasMoreData;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatRatio;
import static com.goldmf.GMFund.util.UmengUtil.eEvENTIDInvestExchangeCoupons;
import static java.util.Collections.emptyList;

/**
 * Created by yale on 16/2/19.
 */
public class MainFragments {
    public static PublishSubject<Boolean> FortuneRedPointTabSubject = PublishSubject.create();
    public static PublishSubject<Boolean> ScoreRedPointTabSubject = PublishSubject.create();

    public static abstract class BaseMainFragment extends SimpleFragment {
        @Override
        protected boolean isTraceLifeCycle() {
            return false;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if (!isParentMainActivity()) {
                setupBackButton(this, findToolbar(this), R.drawable.ic_arrow_left_dark);
                int color = ((ColorDrawable) findToolbar(this).getBackground()).getColor();
                safeCall(() -> setStatusBarBackgroundColor(this, color));
            }
        }

        @Override
        public final void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (getView() == null)
                return;

            if (!isParentMainActivity()) {
                onUserVisibleHintChanged(isVisibleToUser);
                return;
            }

            if (isVisibleToUser) {
                getCurrentSelectedTabIdx()
                        .reserveIf(it -> it == getCorrespondTabIndex())
                        .apply(it -> {
                            onUserVisibleHintChanged(true);
                        });
            } else {
                onUserVisibleHintChanged(false);
            }
        }

        public void onUserVisibleHintChanged(boolean isVisibleToUser) {
        }

        protected abstract int getCorrespondTabIndex();

        protected Optional<Integer> getCurrentSelectedTabIdx() {
            if (isParentMainActivity()) {
                return ((MainActivityV2) getActivity()).getSelectedTabIndex();
            }

            return Optional.empty();
        }

        protected void updateRegistGuide(boolean play) {
            if (isParentMainActivity()) {
                ((MainActivityV2) getActivity()).updateGuideImage(play);
            }
        }

        protected boolean isParentMainActivity() {
            return getActivity() != null && getActivity() instanceof MainActivityV2;
        }
    }

    public static final class ADHomeFragment extends BaseMainFragment {
        private boolean mHasData = false;
        private boolean mDataExpiredRemote = false;
        private boolean mDataExpiredLocal = false;
        private IntCounter mIntCounter = CircleHelper.createRewardCounter();

        @Override
        protected int getCorrespondTabIndex() {
            return 0;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_ad_home, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setOnSwipeRefreshListener(() -> performFetchRemoteData(false));
            v_setClick(mRefreshLayout, () -> performFetchRemoteData(false));
            v_setClick(mReloadSection, v -> performFetchRemoteData(true));

            addDaySIgnCellToViewTree();

            consumeEvent(Observable.merge(NotificationCenter.loginSubject, NotificationCenter.logoutSubject))
                    .setTag("data_expired_remote")
                    .onNextFinish(nil -> {
                        mDataExpiredRemote = true;
                        if (getView() != null && getUserVisibleHint()) {
                            performFetchRemoteData(!mHasData);
                            mDataExpiredRemote = false;
                        }
                    })
                    .done();
            consumeEvent(NotificationCenter.afterDonateScoreSubject)
                    .setTag("data_expired_local")
                    .onNextFinish(nil -> {
                        mDataExpiredLocal = true;
                        if (getView() != null && getUserVisibleHint()) {
                            ADVMCollection collection = new ADVMCollection(DiscoverManager.getInstance());
                            setupContentView(collection);
                            mDataExpiredLocal = false;
                        }
                    })
                    .done();

            consumeEvent(NotificationCenter.daySignStatusChangeSubject)
                    .setTag("day_sign_status_change")
                    .onNextFinish(response -> {
                        checkDaySign();
                    })
                    .done();

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        @Override
        public void onUserVisibleHintChanged(boolean isVisibleToUser) {
            super.onUserVisibleHintChanged(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (!mHasData || mDataExpiredRemote) {
                    performFetchRemoteData(!mHasData);
                }
                if (mHasData && mDataExpiredLocal) {
                    ADVMCollection collection = new ADVMCollection(DiscoverManager.getInstance());
                    setupContentView(collection);
                    mDataExpiredLocal = false;
                }
                updateDaySignCell();
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mIntCounter.release();
            mHasData = false;
            mDataExpiredRemote = true;
            mDataExpiredLocal = false;
        }

        private void addDaySIgnCellToViewTree() {
            ViewGroup cellParent = (ViewGroup) v_findView(mContentSection, R.id.gridView).getParent();

            View daySignCell = LayoutInflater.from(cellParent.getContext()).inflate(R.layout.cell_day_sign, cellParent, false);
            v_updateLayoutParams(daySignCell, LinearLayout.LayoutParams.class, params -> {
                params.setMargins(0, dp2px(10), 0, 0);
            });
            daySignCell.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(WHITE_COLOR, dp2px(4)).border(LINE_SEP_COLOR, dp2px(0.5f))));
            v_setGone(daySignCell);
            cellParent.addView(daySignCell, 2);

            v_setClick(daySignCell, R.id.btn_sign, v -> {
                showDaySignDialogWithAnimation(daySignCell);
            });
        }

        private void showDaySignDialogWithAnimation(View daySignCell) {
            Rect childRect = new Rect();
            v_getGlobalHitRect(daySignCell, childRect);

            FrameLayout dimLayer = new FrameLayout(getActivity());
            FrameLayout fakeDialog = new FrameLayout(getActivity());
            fakeDialog.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(WHITE_COLOR, dp2px(4)).border(LINE_SEP_COLOR, dp2px(0.5f))));

            dimLayer.addView(fakeDialog, apply(new FrameLayout.LayoutParams(-2, -2), params -> {
                params.width = childRect.width();
                params.height = childRect.height();
                params.leftMargin = childRect.left;
                params.topMargin = childRect.top;
            }));

            Button completeButton = new Button(getActivity());
            completeButton.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(RED_COLOR, dp2px(4))));

            int startButtonWidth = dp2px(80);
            int endButtonWidth = dp2px(295);
            int startButtonHeight = dp2px(32);
            int endButtonHeight = dp2px(40);
            int startLeftMargin = childRect.width() - startButtonWidth - dp2px(16);
            int startTopMargin = (childRect.height() - dp2px(32)) >> 1;
            int endLeftMargin = dp2px(20);
            int endTopMargin = dp2px(306);
            fakeDialog.addView(completeButton, apply(new FrameLayout.LayoutParams(-2, -2), params -> {
                params.width = startButtonWidth;
                params.height = startButtonHeight;
                params.leftMargin = startLeftMargin;
                params.topMargin = startTopMargin;
            }));


            SigningDialog dialog = new SigningDialog(getActivity());
            dialog.mContentWrapper.setVisibility(View.INVISIBLE);

            Rect screenSize = getScreenSize(this);

            ViewGroup rootView = (FrameLayout) dialog.mContentWrapper.getParent();
            rootView.addView(dimLayer, screenSize.width(), screenSize.height());
            v_updateLayoutParams(rootView, params -> {
                params.width = screenSize.width();
                params.height = screenSize.height();
            });
            dialog.show();

            ViewAnimationProxy cellAnimationProxy = new ViewAnimationProxy(fakeDialog);
            PropertyValuesHolder widthHolder = PropertyValuesHolder.ofInt("width", childRect.width(), dp2px(335));
            PropertyValuesHolder heightHolder = PropertyValuesHolder.ofInt("height", childRect.height(), dp2px(366));
            float destTranslationX = (screenSize.width() - dp2px(335)) / 2 - childRect.left;
            PropertyValuesHolder translationXHolder = PropertyValuesHolder.ofFloat("translationX", 0, destTranslationX);
            float destTranslationY = (screenSize.height() - dp2px(366)) / 2 - childRect.top;
            PropertyValuesHolder translationYHolder = PropertyValuesHolder.ofFloat("translationY", 0, destTranslationY);
            ObjectAnimator cellAnimator = ObjectAnimator.ofPropertyValuesHolder(cellAnimationProxy, widthHolder, heightHolder, translationXHolder, translationYHolder);

            ViewAnimationProxy buttonProxy = new ViewAnimationProxy(completeButton);
            PropertyValuesHolder buttonLeftMarginHolder = PropertyValuesHolder.ofInt("leftMargin", startLeftMargin, endLeftMargin);
            PropertyValuesHolder buttonTopMarginHolder = PropertyValuesHolder.ofInt("topMargin", startTopMargin, endTopMargin);
            PropertyValuesHolder buttonWidthHolder = PropertyValuesHolder.ofInt("width", startButtonWidth, endButtonWidth);
            PropertyValuesHolder buttonHeightHolder = PropertyValuesHolder.ofInt("height", startButtonHeight, endButtonHeight);
            ObjectAnimator buttonAnimator = ObjectAnimator.ofPropertyValuesHolder(buttonProxy,
                    buttonLeftMarginHolder, buttonTopMarginHolder, buttonWidthHolder, buttonHeightHolder);


            AnimatorSet set = new AnimatorSet();
            set.setDuration(400L);
            set.setInterpolator(new AccelerateDecelerateInterpolator());
            set.play(cellAnimator).with(buttonAnimator);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    dialog.mContentWrapper.setVisibility(View.VISIBLE);
                    v_removeFromParent(dimLayer);
                    performDaySign();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dialog.mContentWrapper.setVisibility(View.VISIBLE);
                    v_removeFromParent(dimLayer);
                    performDaySign();
                }
            });
            set.start();
        }

        @SuppressWarnings("unchecked")
        private void performFetchRemoteData(boolean reload) {
            if (reload) {
                changeVisibleSection(TYPE_LOADING);
            } else {
                mRefreshLayout.setRefreshing(true);
            }

            consumeEventMRUpdateUI(CommonController.fetchPromotion(!reload), reload)
                    .setTag("remote_data")
                    .onNextSuccess(responses -> {
                        DiscoverManager dm = DiscoverManager.getInstance();
                        ADVMCollection collection = new ADVMCollection(dm);
                        setupContentView(collection);
                        checkDaySign();
                        mHasData = true;
                        mDataExpiredRemote = false;
                    })
                    .done();
        }

        private void setupContentView(ADVMCollection collection) {
            setupBanner(collection);
            setupGridView(collection);
            setupFundSection(collection);
            setupTraderSection(collection);
            setupTalentSection(collection);
            setupMatchSection(collection);
            setupTopicSection(collection);
        }

        private void setupBanner(ADVMCollection collection) {
            List<FocusInfo> items = collection.bannerVMs;
            Context ctx = getActivity();
            Rect screenSize = getScreenSize(this);
            ViewPager pager = v_findView(mContentSection, R.id.pager);
            LinearLayout indicatorGroup = v_findView(mContentSection, R.id.group_indicator);

            float ratio = 375F / 150F;
            v_updateLayoutParams((View) pager.getParent(), params -> {
                params.width = -1;
                params.height = (int) (screenSize.width() / ratio);
            });

            indicatorGroup.removeAllViews();
            Stream.of(items)
                    .forEach(it -> {
                        View indicator = new View(ctx);
                        StateListDrawable background = new StateListDrawable();
                        background.addState(new int[]{android.R.attr.state_selected}, new ShapeDrawable(new RoundCornerShape(WHITE_COLOR, dp2px(2))));
                        background.addState(new int[]{}, new ShapeDrawable(new RoundCornerShape(0x66FFFFFF, dp2px(2))));
                        indicator.setBackgroundDrawable(background);
                        indicatorGroup.addView(indicator, dp2px(4), dp2px(4));
                        v_updateLayoutParams(indicator, ViewGroup.MarginLayoutParams.class, params -> {
                            params.leftMargin = dp2px(2.5f);
                            params.rightMargin = dp2px(2.5f);
                        });
                    });

            final int[] eventIDArray = {
                    UmengUtil.eEvENTIDHomeClickBanner1,
                    UmengUtil.eEvENTIDHomeClickBanner2,
                    UmengUtil.eEvENTIDHomeClickBanner3,
                    UmengUtil.eEvENTIDHomeClickBanner4,
                    UmengUtil.eEvENTIDHomeClickBanner5,
                    UmengUtil.eEvENTIDHomeClickBanner6,
                    UmengUtil.eEvENTIDHomeClickBanner7,
                    UmengUtil.eEvENTIDHomeClickBanner8,
                    UmengUtil.eEvENTIDHomeClickBanner9,
                    UmengUtil.eEvENTIDHomeClickBanner10
            };
            pager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
                @Override
                public Fragment getItem(int position) {
                    return newFocusBannerFragment(position, items, eventIDArray);
                }

                @Override
                public int getCount() {
                    return items.size() * 1000;
                }
            });
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    v_forEach(indicatorGroup, (pos, child) -> child.setSelected(pos == (position % items.size())));
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            if (indicatorGroup.getChildCount() > 0) {
                if (pager.getCurrentItem() == 0) {
                    int currentIdx = items.size() * 500;
                    pager.setCurrentItem(currentIdx);
                }
                indicatorGroup.getChildAt(pager.getCurrentItem() % items.size());
            }
            v_setVisibility((View) pager.getParent(), items.isEmpty() ? View.GONE : View.VISIBLE);
        }

        @NonNull
        private static Fragment newFocusBannerFragment(final int position, List<FocusInfo> items, int[] eventIDArray) {
            FocusBannerFragment ret = new FocusBannerFragment();
            ret.position = position;
            ret.items = items;
            ret.eventIDArray = eventIDArray;
            return ret;
        }

        public static class FocusBannerFragment extends Fragment {
            private int position;
            private List<FocusInfo> items = Collections.emptyList();
            private int[] eventIDArray = new int[0];

            @Nullable
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                Context ctx = inflater.getContext();
                GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(ctx.getResources())
                        .setPlaceholderImage(R.mipmap.ic_focus_placeholder)
                        .build();
                return new SimpleDraweeView(ctx, hierarchy);
            }

            @Override
            public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);
                FocusInfo item = ListExtension.getFromList(items, position % items.size());
                if (item != null) {
                    SimpleDraweeView draweeView = (SimpleDraweeView) view;
                    v_setImageUri(draweeView, item.imageUrl);
                    v_setClick(draweeView, v -> {
                        CMDParser.parse(item.tarLink).call(getActivity());
                        UmengUtil.stat_enter_focus_image_page(getActivity(), Optional.of(this));
                        if (position >= 0 && position < eventIDArray.length)
                            UmengUtil.stat_click_event(eventIDArray[position]);
                    });
                }
            }
        }


        private void setupGridView(ADVMCollection collection) {
            List<ADHomeGridCellVM> items = collection.gridVMs;

            final int COLUMN_COUNT = Math.min(4, items.size());
            ViewGroup gridView = v_findView(this, R.id.gridView);
            gridView.setPadding(0, dp2px(15), 0, dp2px(15));

            gridView.removeAllViewsInLayout();
            ViewGroup currentRow = null;
            int index = 0;
            int currentColumnIdx = 0;
            for (ADHomeGridCellVM item : items) {
                if (currentRow == null || currentColumnIdx == 0) {
                    currentRow = newRow();
                    gridView.addView(currentRow);
                }
                View column = newColumn(currentRow, item, COLUMN_COUNT);
                currentRow.addView(column);
                int gridItemIndex = index;
                v_setClick(column, v -> {
                    CMDParser.parse(item.link).call(getActivity());
                    switch (gridItemIndex) {
                        case 1:
                            UmengUtil.stat_enter_home_grid1_page(getActivity(), Optional.of(this));
                            break;
                        case 2:
                            UmengUtil.stat_enter_home_grid2_page(getActivity(), Optional.of(this));
                            break;
                        case 3:
                            UmengUtil.stat_enter_home_grid3_page(getActivity(), Optional.of(this));
                            break;
                        case 4:
                            UmengUtil.stat_enter_home_grid4_page(getActivity(), Optional.of(this));
                            break;
                        case 5:
                            UmengUtil.stat_enter_home_grid5_page(getActivity(), Optional.of(this));
                            break;
                        case 6:
                            UmengUtil.stat_enter_home_grid6_page(getActivity(), Optional.of(this));
                            break;
                    }
                });
                index++;
                currentColumnIdx++;
                if (currentColumnIdx >= COLUMN_COUNT) {
                    currentColumnIdx = 0;
                }
            }
            if (currentColumnIdx > 0 && currentColumnIdx < COLUMN_COUNT) {
                while (currentColumnIdx < COLUMN_COUNT) {
                    currentRow.addView(newEmptyColumn());
                    currentColumnIdx++;
                }
            }
        }

        private ViewGroup newRow() {
            Context ctx = getActivity();
            LinearLayout row = new LinearLayout(ctx);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            return row;
        }

        private View newColumn(ViewGroup parent, ADHomeGridCellVM item, int columnCount) {
            Context ctx = getActivity();
            View column = LayoutInflater.from(ctx).inflate(R.layout.cell_ad_home_grid, parent, false);
            v_setImageUri(column, R.id.img_icon, item.iconImageUrl);
            v_setTextWithFitBound(column, R.id.label_title_and_content, item.title);

            v_updateLayoutParams(column, params -> {
                params.width = getScreenSize(getActivity()).width() / columnCount;
            });

            return column;
        }

        private View newEmptyColumn() {
            View view = new View(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 1);
            params.weight = 1;
            view.setLayoutParams(params);
            return view;
        }

        private void setupFundSection(ADVMCollection collection) {
            List<FundBrief> items = collection.fundVMs;
            ViewGroup fundSection = v_findView(mContentSection, R.id.section_fund);
            SectionHeader header = SectionHeader.class.cast(fundSection.getChildAt(0));
            header.setTitle(collection.fundSectionTitle);
            header.setExtraTitle(collection.fundSectionMore.content);
            v_setClick(header, v -> CMDParser.parse(collection.fundSectionMore.tarLink).call(getActivity()));
            LinearLayout listView = v_findView(fundSection, R.id.list_fund);
            int flags = FundCardViewHelper.FLAG_NO_MARGIN |
                    FundCardViewHelper.FLAG_BACKGROUND_PURE_WHITE |
                    FundCardViewHelper.FLAG_SHOW_SEP_LINE;
            FundCardViewHelper.resetFundCardListViewWithFundBrief(listView, items, flags);
            v_setVisibility(fundSection, items.isEmpty() ? View.GONE : View.VISIBLE);
        }

        private void setupTraderSection(ADVMCollection collection) {
            Context ctx = getActivity();
            List<TraderCardVM> items = collection.traderVMs;
            ViewGroup traderSection = v_findView(mContentSection, R.id.section_trader);
            SectionHeader header = SectionHeader.class.cast(traderSection.getChildAt(0));
            header.setTitle(collection.traderSectionTitle);
            header.setExtraTitle(collection.traderSectionMore.content);
            v_setClick(header, v -> CMDParser.parse(collection.traderSectionMore.tarLink).call(getActivity()));
            LinearLayout listView = v_findView(traderSection, R.id.list_trader);
            listView.removeAllViewsInLayout();
            int flags = TraderCardViewHelper.FLAG_BACKGROUND_PURE_WHITE;
            Stream.of(items)
                    .forEach(it -> {
                        View cell = TraderCardViewHelper.createCell(listView);
                        Func0<TraderCardVM> itemGetter = () -> it;
                        ChildBinder binder = ChildBinders.createWithView(cell);
                        TraderCardViewHelper.afterCellCreated(cell, itemGetter, flags);
                        TraderCardViewHelper.bindChildren(binder);
                        TraderCardViewHelper.configureCell(binder, itemGetter);
                        listView.addView(cell);
                        View sepLine = new View(ctx);
                        sepLine.setBackgroundColor(LINE_SEP_COLOR);
                        listView.addView(sepLine, -1, dp2px(0.5f));
                        v_updateLayoutParams(sepLine, ViewGroup.MarginLayoutParams.class, params -> params.setMargins(dp2px(10), 0, 0, 0));
                    });
            if (listView.getChildCount() > 0) {
                listView.removeViewAt(listView.getChildCount() - 1);
            }
            v_setVisibility(traderSection, items.isEmpty() ? View.GONE : View.VISIBLE);
        }

        private void setupTalentSection(ADVMCollection collection) {
            Context ctx = getActivity();
            List<TraderCardVM> items = collection.talentVMs;
            ViewGroup traderSection = v_findView(mContentSection, R.id.section_talent);
            SectionHeader header = SectionHeader.class.cast(traderSection.getChildAt(0));
            header.setTitle(collection.talentSectionTitle);
            header.setExtraTitle(collection.talentSectionMore.content);
            v_setClick(header, v -> CMDParser.parse(collection.talentSectionMore.tarLink).call(getActivity()));
            LinearLayout listView = v_findView(traderSection, R.id.list_talent);
            listView.removeAllViewsInLayout();
            int flags = TraderCardViewHelper.FLAG_BACKGROUND_PURE_WHITE;
            Stream.of(items)
                    .forEach(it -> {
                        View cell = TraderCardViewHelper.createCell(listView);
                        Func0<TraderCardVM> itemGetter = () -> it;
                        ChildBinder binder = ChildBinders.createWithView(cell);
                        TraderCardViewHelper.afterCellCreated(cell, itemGetter, flags);
                        TraderCardViewHelper.bindChildren(binder);
                        TraderCardViewHelper.configureCell(binder, itemGetter);
                        listView.addView(cell);

                        View sepLine = new View(ctx);
                        sepLine.setBackgroundColor(LINE_SEP_COLOR);
                        listView.addView(sepLine, -1, dp2px(0.5f));
                        v_updateLayoutParams(sepLine, ViewGroup.MarginLayoutParams.class, params -> params.setMargins(dp2px(10), 0, 0, 0));
                    });
            if (listView.getChildCount() > 0) {
                listView.removeViewAt(listView.getChildCount() - 1);
            }
            v_setVisibility(traderSection, items.isEmpty() ? View.GONE : View.VISIBLE);
        }

        private void setupMatchSection(ADVMCollection collection) {
            List<StockMatchHomeVM> items = collection.matchVMs;
            ViewGroup matchSection = v_findView(mContentSection, R.id.section_match);
            SectionHeader header = SectionHeader.class.cast(matchSection.getChildAt(0));
            header.setTitle(collection.matchSectionTitle);
            header.setExtraTitle(collection.matchSectionMore.content);
            v_setClick(header, v -> CMDParser.parse(collection.matchSectionMore.tarLink).call(getActivity()));
            LinearLayout listView = v_findView(matchSection, R.id.list_match);
            listView.removeAllViews();
            Stream.of(items)
                    .forEach(it -> {
                        Func0<StockMatchHomeVM> itemGetter = () -> it;
                        int flags = StockMatchHomeCardHelper.FLAG_USE_HOME_STYLE;
                        View cell = StockMatchHomeCardHelper.createCell(listView);
                        ChildBinder binder = ChildBinders.createWithView(cell);
                        StockMatchHomeCardHelper.afterCellCreated(cell, itemGetter, flags);
                        StockMatchHomeCardHelper.bindChilds(binder);
                        StockMatchHomeCardHelper.configureView(binder, itemGetter, flags);
                        listView.addView(cell);
                    });
            v_setVisibility(matchSection, items.isEmpty() ? View.GONE : View.VISIBLE);
        }

        private void setupTopicSection(ADVMCollection collection) {
            Context ctx = getActivity();
            List<CircleHelper.CellVM> items = collection.topicVMs;
            ViewGroup topicSection = v_findView(mContentSection, R.id.section_topic);
            SectionHeader header = SectionHeader.class.cast(topicSection.getChildAt(0));
            header.setTitle(collection.topicMoreTitle);
            header.setExtraTitle(collection.topicSectionMore.content);
            v_setClick(header, v -> CMDParser.parse(collection.topicSectionMore.tarLink).call(getActivity()));
            LinearLayout listView = v_findView(topicSection, R.id.list_topic);
            listView.removeAllViews();
            Stream.of(items)
                    .forEach(it -> {
                        Func0<CircleHelper.CellVM> itemGetter = () -> it;
                        View cell = LayoutInflater.from(ctx).inflate(R.layout.cell_circle_list, listView, false);
                        ChildBinder binder = ChildBinders.createWithView(cell);
                        Func0<Boolean> ignoreAvatarClickEvent = () -> false;
                        Action0 rewardCallback = () -> {
                        };
                        FrameLayout topLayer = FrameLayout.class.cast(mContentSection.getParent());
                        CircleHelper.afterViewHolderCreated(cell, itemGetter, mIntCounter, topLayer, ignoreAvatarClickEvent, rewardCallback);
                        CircleHelper.bindChildViews(binder);
                        final int flags = CircleHelper.FLAG_SHOW_USER_TYPE |
                                CircleHelper.FLAG_SHOW_BAR_INFO |
                                CircleHelper.FLAG_HIDE_BAR_SEP_LINE |
                                CircleHelper.FLAG_HIDE_COMMENTS |
                                CircleHelper.FLAG_HIDE_RATE_BTN;
                        CircleHelper.configureView(binder, () -> true, itemGetter.call(), flags);
                        listView.addView(cell);

                        View space = new View(ctx);
                        space.setBackgroundColor(WHITE_COLOR);
                        listView.addView(space, -1, dp2px(15));

                        View sepLine = new View(ctx);
                        sepLine.setBackgroundColor(LINE_SEP_COLOR);
                        listView.addView(sepLine, -1, dp2px(0.5f));
                        v_updateLayoutParams(sepLine, ViewGroup.MarginLayoutParams.class, params -> params.setMargins(dp2px(16), 0, dp2px(16), 0));

                        View space2 = new View(ctx);
                        space2.setBackgroundColor(WHITE_COLOR);
                        listView.addView(space2, -1, dp2px(10));
                    });
            if (listView.getChildCount() > 1) {
                Stream.range(0, 2)
                        .forEach(it -> {
                            listView.removeViewAt(listView.getChildCount() - 1);
                        });
            }
            v_setVisibility(topicSection, items.isEmpty() ? View.GONE : View.VISIBLE);
        }


        private View newListCell(ViewGroup parent, FocusImageCellVM item) {
            Context ctx = getActivity();
            View cell = LayoutInflater.from(ctx).inflate(R.layout.cell_ad_home_list, parent, false);
            v_setImageUri(cell, R.id.img_cover, item.imageURL);
            return cell;
        }

        private void checkDaySign() {
            DayActionManager manager = ScoreManager.getInstance().checkin;
            boolean noCache = manager == null || manager.dayActionInfoList == null || manager.dayActionInfoList.isEmpty();
            boolean isLogin = MineManager.getInstance().isLoginOK();
            if (noCache) {
                if (isLogin) {
                    consumeEventMRList(MResultExtension.zipToList(ScoreController.refreshDayAction(), ScoreController.refreshScoreActions()))
                            .setPolicy(ConsumeEventChain.POLICY.IGNORED)
                            .setTag("refresh_day_action")
                            .onNextSuccess(response -> {
                                List<ScoreAction> actions = (List<ScoreAction>) response.get(1).data;
                                ScoreAction daySignAction = Stream.of(actions)
                                        .filter(it -> it.actionType == ScoreAction.ScoreAction_type_SignIn)
                                        .findFirst().orElse(null);
                                updateDaySignCell(daySignAction);
                            })
                            .done();
                }
            } else {
                updateDaySignCell();
            }
        }

        private void updateDaySignCell() {
            updateDaySignCell(null);
        }

        private void updateDaySignCell(ScoreAction action) {
            View cell = v_findView(this, R.id.cell_day_sign);
            if (cell != null) {
                DayActionManager manager = ScoreManager.getInstance().checkin;
                DayInfo dayInfo = safeGet(() -> getFromList(manager.dayActionInfoList, manager.todayPos), null);
                boolean notSign = dayInfo != null && safeGet(() -> !manager.todayGained, false);
                if (notSign) {
                    CharSequence score = safeGet(() -> String.valueOf((int) dayInfo.amount), "???");
                    v_setText(cell, R.id.label_score, score);
                    String status = String.format(Locale.getDefault(), "已签到%d天", manager.todayPos);
                    v_setText(cell, R.id.label_status, status);
                }
                int oldVisibility = cell.getVisibility();
                int newVisibility = notSign ? View.VISIBLE : View.GONE;
                String iconURL = safeGet(() -> action.imgUrl, "");
                if (!TextUtils.isEmpty(iconURL)) {
                    v_setImageUri(cell, R.id.img_icon, iconURL);
                }
                if (notMatch(oldVisibility, newVisibility)) {
                    v_setVisibility(cell, newVisibility);
                    if (newVisibility == View.VISIBLE) {
                        ViewAnimationProxy proxy = new ViewAnimationProxy(cell);

                        v_setGone(cell, R.id.wrapper_content);
                        ObjectAnimator animator = ObjectAnimator.ofInt(proxy, "height", 0, dp2px(80));
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                v_updateLayoutParams(cell, params -> {
                                    params.height = dp2px(80);
                                });
                                v_setVisible(cell, R.id.wrapper_content);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                super.onAnimationCancel(animation);
                                v_updateLayoutParams(cell, params -> {
                                    params.height = dp2px(80);
                                });
                                v_setVisible(cell, R.id.wrapper_content);
                            }
                        });
                        animator.start();
                    }
                }
            }
        }

        private static void performDaySign() {
            safeCall(() -> {
                ScoreManager.getInstance().checkin.todayGained = true;
                NotificationCenter.daySignStatusChangeSubject.onNext(null);
            });
            gainTodayScore()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        if (isSuccess(response)) {
                        } else {
                            showToast(MyApplication.SHARE_INSTANCE, getErrorMessage(response));
                            safeCall(() -> {
                                boolean alreadySign = response.errCode == 5022641;
                                if (!alreadySign) {
                                    ScoreManager.getInstance().checkin.todayGained = false;
                                    NotificationCenter.daySignStatusChangeSubject.onNext(null);
                                }
                            });
                        }
                    });

        }
    }

    private static class ADVMCollection {
        private List<FocusInfo> bannerVMs;
        private List<ADHomeGridCellVM> gridVMs;
        private CharSequence fundSectionTitle;
        private TarLinkText fundSectionMore;
        private List<FundBrief> fundVMs;
        private CharSequence traderSectionTitle;
        private TarLinkText traderSectionMore;
        private List<TraderCardVM> traderVMs;
        private CharSequence talentSectionTitle;
        private TarLinkText talentSectionMore;
        private List<TraderCardVM> talentVMs;
        private CharSequence matchSectionTitle;
        private TarLinkText matchSectionMore;
        private List<StockMatchHomeVM> matchVMs;
        private CharSequence topicMoreTitle;
        private TarLinkText topicSectionMore;
        private List<CircleHelper.CellVM> topicVMs;

        public ADVMCollection(DiscoverManager dm) {
            this.bannerVMs = dm.promotionFocusList.list;
            this.gridVMs = Stream.of(dm.promotionList.list)
                    .map(it -> new ADHomeGridCellVM(it))
                    .collect(Collectors.toList());
            this.fundSectionTitle = dm.promotionFundList.title;
            this.fundSectionMore = dm.promotionFundList.more;
            this.fundVMs = Stream.of(dm.promotionFundList.list)
                    .collect(Collectors.toList());
            this.traderSectionTitle = dm.promotionTraderList.title;
            this.traderSectionMore = dm.promotionTraderList.more;
            this.traderVMs = Stream.of(dm.promotionTraderList.list)
                    .map(it -> new TraderCardViewHelper.TraderCardVM(it))
                    .collect(Collectors.toList());
            this.talentSectionTitle = dm.promotionTalentList.title;
            this.talentSectionMore = dm.promotionTalentList.more;
            this.talentVMs = Stream.of(dm.promotionTalentList.list)
                    .map(it -> new TraderCardViewHelper.TraderCardVM(it))
                    .collect(Collectors.toList());
            this.matchSectionTitle = dm.promotionMatchList.title;
            this.matchSectionMore = dm.promotionMatchList.more;
            this.matchVMs = Stream.of(dm.promotionMatchList.list)
                    .map(it -> new StockMatchHomeVM(it))
                    .collect(Collectors.toList());
            this.topicMoreTitle = dm.promotionMessageList.title;
            this.topicSectionMore = dm.promotionMessageList.more;
            this.topicVMs = Stream.of(dm.promotionMessageList.list)
                    .map(it -> new CircleHelper.CellVM(it.sesion, it.message))
                    .collect(Collectors.toList());
        }
    }

    private static class ADHomeGridCellVM {
        private String link;
        private String iconImageUrl;
        private String categoryImageUrl;
        private CharSequence title;

        public static ADHomeGridCellVM create(FocusInfo raw) {
            if (raw != null && !raw.bShowTotal) {
                return new ADHomeGridCellVM(raw);
            }
            return null;
        }

        public ADHomeGridCellVM(FocusInfo raw) {
            this.link = raw.tarLink;
            this.iconImageUrl = raw.iconUrl;
            this.categoryImageUrl = raw.tipsIconUrl;
            this.title = raw.info1;
        }

        private int colorToInt(String color) {
            if (!TextUtils.isEmpty(color)) {
                if (color.equalsIgnoreCase("red")) {
                    return TEXT_RED_COLOR;
                }
            }
            return TEXT_GREY_COLOR;
        }
    }

    public static PublishSubject<Integer> sFocusSelectTabSubject = PublishSubject.create();
    public static PublishSubject<Integer> sStockSelectTabSubject = PublishSubject.create();
    public static PublishSubject<Void> sStockHomeStopTimerSubject = PublishSubject.create();

    public static final class StockHomeFragment extends BaseMainFragment {

        private ViewPager mPager;
        private int selectTabIndex = 1;

        public StockHomeFragment init(int tabIndex) {
            Bundle arguments = new Bundle();
            arguments.putInt(MainActivityV2.KEY_STOCK_TAB_INDEX, tabIndex);
            setArguments(arguments);
            return this;
        }

        @Override
        protected int getCorrespondTabIndex() {
            return 1;
        }

        @Override
        public void onUserVisibleHintChanged(boolean isVisibleToUser) {
            super.onUserVisibleHintChanged(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (mPager != null) {
                    sFocusSelectTabSubject.onNext(mPager.getCurrentItem());
                }
            } else {
                sStockHomeStopTimerSubject.onNext(null);
            }
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            if (getArguments() != null) {
                selectTabIndex = getArguments().getInt(MainActivityV2.KEY_STOCK_TAB_INDEX, 1);
            }
            return inflater.inflate(R.layout.frag_stock_home, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            //init view pager
            mPager = v_findView(view, R.id.pager);
            mPager.setOffscreenPageLimit(2);
            mPager.setAdapter(new StockHomePagerAdapter(getChildFragmentManager()));
            mPager.setCurrentItem(selectTabIndex);

            //init tabLayout
            TabLayout tabLayout = v_findView(view, R.id.tabLayout);
            tabLayout.setupWithViewPager(v_findView(view, R.id.pager));

            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    int position = tab.getPosition();
                    mPager.setCurrentItem(position);
                    sStockSelectTabSubject.onNext(position);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            v_setClick(this, R.id.btn_search, v -> {
                UmengUtil.stat_click_event(UmengUtil.eEVENTIDClickSearch);
                showActivity(this, an_StockSearchPage());
            });

            consumeEvent(sStockSelectTabSubject)
                    .setTag("select_tab")
                    .onNextFinish(tabIndex -> {
                        selectTabIndex = tabIndex;
                        mPager.setCurrentItem(tabIndex);
                    })
                    .done();
        }
    }

    private static class StockHomePagerAdapter extends FragmentPagerAdapter {
        private static String[] titles = {"动态", "自选", "大赛"};

        public StockHomePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new StockFeedFragment();
            } else if (position == 1) {
                return new FocusStockHomeFragment();
            } else {
                return new StockMatchHomeFragment();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    /**
     * 动态
     */
    @SuppressWarnings("unchecked")
    public static class StockFeedFragment extends SimpleFragment {

        private AdvanceSwipeRefreshLayout mRefreshLayout;

        private TopTrader mAllTraderStore;
        private CommandPageArray<FeedOrder> mBuyTraderStore;
        private CommandPageArray<FeedOrder> mSellTraderStore;
        private boolean mIsLoadingMore = false;
        private boolean mDataIsExpired = true;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_stock_feed, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            changeVisibleSection(TYPE_LOADING);
            mRefreshLayout = (AdvanceSwipeRefreshLayout) mContentSection;
            v_setClick(mReloadSection, v -> reloadData(true));

            mRefreshLayout.setOnRefreshListener(() -> reloadData(false));
            View appBarLayout = v_findView(mContentSection, R.id.appBarLayout);
            mRefreshLayout.setOnPreInterceptTouchEventDelegate(ev -> appBarLayout.getTop() < 0);

            RecyclerView recyclerView = v_findView(this, R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            addHorizontalSepLine(recyclerView);
            addCellVerticalSpacing(recyclerView, dp2px(10), 1, 0);

            consumeEvent(sStockSelectTabSubject)
                    .setTag("select_action")
                    .onNextFinish(tabIndex -> {
                        if (tabIndex == 0) {
                            mDataIsExpired = true;
                            if (getUserVisibleHint() && getView() != null) {
                                reloadData(true);
                            }
                            mDataIsExpired = false;
                        }
                    })
                    .done();

            consumeEvent(sFocusSelectTabSubject)
                    .setTag("select_focus_tab")
                    .onNextFinish(tabIndex -> {
                        if (tabIndex == 0) {
                            mDataIsExpired = true;
                            setUserVisibleHint(true);
                        }
                    })
                    .done();

            consumeEvent(sStockHomeStopTimerSubject)
                    .setTag("stop_timer")
                    .onNextFinish(nil -> {
                        unsubscribeFromVisible("feed_list_timer");
                    })
                    .done();

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mDataIsExpired = true;
            mAllTraderStore = null;
            mBuyTraderStore = null;
            mSellTraderStore = null;
            mIsLoadingMore = false;
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mDataIsExpired = true;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);

            if (isVisibleToUser && getView() != null) {
                if (mDataIsExpired) {
                    boolean reload = !v_isVisible(mContentSection);
                    reloadData(reload);
                }
                mDataIsExpired = false;
            }

            if (isVisibleToUser && getView() != null) {

                Observable<MResultsInfo<Void>> observable = StockController.fetchFollowStock(false)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .delaySubscription(5, TimeUnit.SECONDS)
                        .repeat();
                consumeEvent(observable)
                        .setTag("feed_list_timer")
                        .addToVisible()
                        .onNextFinish(nil -> {
                            List<StockIndexItem> indexItems = Stream.of(SimulationStockManager.getInstance().focusCompareIndexs)
                                    .limit(3)
                                    .map(it -> new StockIndexItem(it))
                                    .collect(Collectors.toList());
                            updateIndexSection(indexItems);
                        })
                        .done();
            }
        }

        @SuppressWarnings("unchecked")
        private void reloadData(boolean reload) {
            unsubscribeFromMain("fetch_more");

            if (notNull(mAllTraderStore, mBuyTraderStore, mSellTraderStore)) {

                Observable<List<MResultsInfo>> observable = zipToList(StockController.fetchFollowStock(reload), StockController.fetchLiveHeadLineList(reload), getPreviousPage(getCurrentStore()));

                consumeEventMRList(observable)
                        .setTag("reload")
                        .onNextFinish(responses -> {
                            setSwipeRefreshing(false);

                            List<StockIndexItem> indexItems = Stream.of(SimulationStockManager.getInstance().focusCompareIndexs)
                                    .limit(3)
                                    .map(it -> new StockIndexItem(it))
                                    .collect(Collectors.toList());
                            MResultsInfo<List<GMFLiveItem>> liveHeadLineResponse = responses.get(1);
                            List<GMFLiveItem> liveHeadLines = liveHeadLineResponse.data;

                            updateIndexSection(indexItems);
                            updateLiveSection(liveHeadLines);
                            updateStockFeedSection();

                            changeVisibleSection(TYPE_CONTENT);
                        })
                        .done();

            } else {

                Observable<List<MResultsInfo>> observable = zipToList(
                        StockController.fetchFollowStock(reload),
                        StockController.fetchLiveHeadLineList(reload),
                        StockController.fetchTopTraderAll(),
                        StockController.fetchTopTraderOther(StockController.FEED_TYPE_BUY),
                        StockController.fetchTopTraderOther(StockController.FEED_TYPE_SELL));

                consumeEventMRListUpdateUI(observable, reload)
                        .setTag("reload")
                        .onNextSuccess(responses -> {

                            List<StockIndexItem> indexItems = Stream.of(SimulationStockManager.getInstance().focusCompareIndexs)
                                    .limit(3)
                                    .map(it -> new StockIndexItem(it))
                                    .collect(Collectors.toList());
                            MResultsInfo<List<GMFLiveItem>> liveHeadLineResponse = responses.get(1);
                            List<GMFLiveItem> liveHeadLines = liveHeadLineResponse.data;

                            mAllTraderStore = (TopTrader) responses.get(2).data;
                            mBuyTraderStore = (CommandPageArray<FeedOrder>) responses.get(3).data;
                            mSellTraderStore = (CommandPageArray<FeedOrder>) responses.get(4).data;
                            List<StockRankItem> rankItems = Stream.of(opt(mAllTraderStore.focusList).or(emptyList())).map(it -> new StockRankItem(it)).collect(Collectors.toList());

                            updateRankTable(rankItems);
                            updateIndexSection(indexItems);
                            updateLiveSection(liveHeadLines);
                            updateStockFeedSection();
                        })
                        .done();
            }
        }

        private void fetchMoreData() {
            if (mIsLoadingMore || !hasMoreData(getCurrentStore()))
                return;

            mIsLoadingMore = true;
            consumeEventMR(PageArrayHelper.getNextPage(getCurrentStore()))
                    .setTag("fetch_more")
                    .onNextSuccess(response -> {
                        updateStockFeedSection();
                    })
                    .onNextFinish(response -> {
                        mIsLoadingMore = false;
                    })
                    .done();
        }

        private void updateRankTable(List<StockRankItem> rankItems) {
            List<List<StockRankItem>> chunkList = ListExtension.splitFromList(rankItems, 3);
            Stream<List<StockRankItem>> chunkStream = Stream.of(chunkList)
                    .map(it -> {
                        while (it.size() < 3) {
                            it.add(null);
                        }
                        return it;
                    });

            Func1<StockRankItem, View> toColumn = item -> {
                FrameLayout decorView = new FrameLayout(getActivity());
                {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2);
                    params.weight = 1.0f;
                    decorView.setLayoutParams(params);
                }

                if (item != null) {
                    LayoutInflater inflater = LayoutInflater.from(getActivity());
                    View child = inflater.inflate(R.layout.cell_stock_rank, decorView, false);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) child.getLayoutParams();
                    params.gravity = Gravity.CENTER;
                    params.topMargin = dp2px(16);
                    params.bottomMargin = dp2px(16);
                    v_setImageUri(child, R.id.img_icon, item.iconURL);
                    v_setText(child, R.id.label_title, item.title);
                    v_setClick(child, (v -> {

                        if (item.tarLink.contains("stockGame")) {
                            UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockGame);
                        } else {
                            UmengUtil.stat_click_event(UmengUtil.eEVENTIDRankingList);
                        }

                        CMDParser.parse(item.tarLink).call(getActivity());
                    }));
                    decorView.addView(child);
                }

                return decorView;
            };

            Stream<LinearLayout> rows = chunkStream.map(items -> {
                LinearLayout row = new LinearLayout(getActivity());
                row.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
                row.setLayoutParams(params);

                Stream.of(items).map(it -> toColumn.call(it)).forEach(it -> row.addView(it));
                return row;
            });

            ViewGroup rankTable = v_findView(mContentSection, R.id.table_rank);
            rankTable.removeAllViewsInLayout();
            rows.forEach(it -> rankTable.addView(it));
        }

        private void updateIndexSection(List<StockIndexItem> stockIndexItems) {
            LinearLayout parent = v_findView(mContentSection, R.id.section_index);
            Rect screenSize = getScreenSize(this);
            int childMargin = dp2px(10);
            int childWidth = (screenSize.width() - childMargin * 4) / 3;
            int childHeight = -2;
            parent.removeAllViewsInLayout();
            Stream.of(stockIndexItems)
                    .forEach(it -> {
                        StockIndexBriefView child = new StockIndexBriefView(parent.getContext());

                        child.setCurrentPrice(it.currentPrice)
                                .setChangePrice(it.changePrice)
                                .setChangePriceRatio(it.changePriceRatio)
                                .setIndexName(it.name)
                                .setTextColor(it.color)
                                .updateView();

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(childWidth, childHeight);
                        params.leftMargin = childMargin;
                        params.topMargin = dp2px(6);
                        params.bottomMargin = dp2px(6);
                        parent.addView(child, params);

                        v_setClick(child, v -> {
                            UmengUtil.stat_click_event(UmengUtil.eEVENTIDSelectHZSpec);
                            showActivity(this, an_QuotationDetailPage(it.raw.index));
                        });
                    });
        }

        private void updateLiveSection(List<GMFLiveItem> liveHeadLines) {
            List<String> texts = Stream.of(liveHeadLines).map(it -> it.title).collect(Collectors.toList());

            LoopPlayTextsView loopPlayTextsView = v_findView(mContentSection, R.id.loopView);
            loopPlayTextsView.resetChildrenWithTexts(texts);
            loopPlayTextsView.setOnChildClickListener((child, pos) -> {

                UmengUtil.stat_click_event(UmengUtil.eEVENTIDSelectStockLive);
                showActivity(this, an_StockMarketLivePage(pos));
            });
            v_setVisibility(mContentSection, R.id.section_live, texts.isEmpty() ? View.GONE : View.VISIBLE);
        }

        private static final int RANGE_ALL = StockController.FEED_TYPE_BEGIN;
        private static final int RANGE_BUY = StockController.FEED_TYPE_BUY;
        private static final int RANGE_SELL = StockController.FEED_TYPE_SELL;

        private int mLastRange = RANGE_ALL;

        private void updateStockFeedSection() {
            View header = v_findView(mContentSection, R.id.header_stock_feed);
            v_setClick(header, R.id.area_range, v -> {
                int range = mLastRange;

                if (range == RANGE_ALL) {
                    mLastRange = RANGE_BUY;
                } else if (range == RANGE_BUY) {
                    mLastRange = RANGE_SELL;
                } else {
                    mLastRange = RANGE_ALL;
                }
                unsubscribeFromMain("fetch_more");

                updateStockFeedListHeader();
                updateStockFeedListView();
            });


            updateStockFeedListHeader();
            updateStockFeedListView();
        }

        private CommandPageArray<FeedOrder> getCurrentStore() {
            int range = mLastRange;

            if (anyMatch(range, RANGE_BUY))
                return mBuyTraderStore;

            if (anyMatch(range, RANGE_SELL))
                return mSellTraderStore;

            return mAllTraderStore;
        }

        private List<StockFeedHelper.StockFeedVM> createStockFeedList() {
            return Stream.of(getData(getCurrentStore())).map(it -> new StockFeedHelper.StockFeedVM(it)).collect(Collectors.toList());
        }

        private void updateStockFeedListHeader() {
            int range = mLastRange;
            View parent = v_findView(mContentSection, R.id.header_stock_feed);

            if (range == RANGE_ALL) {
                v_setText(parent, R.id.label_range, "全部");
            } else if (range == RANGE_BUY) {
                v_setText(parent, R.id.label_range, "买入");
            } else if (range == RANGE_SELL) {
                v_setText(parent, R.id.label_range, "卖出");
            }
        }

        @SuppressWarnings("unchecked")
        private void updateStockFeedListView() {

            List<StockFeedHelper.StockFeedVM> items = createStockFeedList();

            RecyclerView recyclerView = v_findView(mContentSection, R.id.recyclerView);

            if (items.isEmpty()) {
                v_setVisible(mEmptySection);
                v_setGone(recyclerView);

                SimpleRecyclerViewAdapter<StockFeedHelper.StockFeedVM> adapter = (SimpleRecyclerViewAdapter<StockFeedHelper.StockFeedVM>) recyclerView.getAdapter();
                if (adapter != null) {
                    adapter.resetItems(Collections.EMPTY_LIST);
                }
                return;
            }

            v_setVisible(recyclerView);
            v_setGone(mEmptySection);
            if (recyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<StockFeedHelper.StockFeedVM> adapter = (SimpleRecyclerViewAdapter<StockFeedHelper.StockFeedVM>) recyclerView.getAdapter();
                adapter.resetItems(items);
            } else {
                SimpleRecyclerViewAdapter<StockFeedHelper.StockFeedVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                        .onCreateItemView(R.layout.cell_stock_feed)
                        .onCreateViewHolder(builder -> {
                            int flags = StockFeedHelper.FLAG_SHOW_USER_TYPE_DECORATION | StockFeedHelper.FLAG_AVATAR_CLICKABLE;
                            return StockFeedHelper.createViewHolder(builder, flags);
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            StockFeedHelper.afterViewHolderCreated(ad, holder);
                        })
                        .create();
                v_setBehavior(recyclerView, new AppBarLayout.ScrollingViewBehavior() {
                    @Override
                    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
                        return target instanceof RecyclerView;
                    }

                    @Override
                    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
                        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
                    }

                    @Override
                    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target) {
                        super.onStopNestedScroll(coordinatorLayout, child, target);
                        checkScrollPosition();
                    }

                    private void checkScrollPosition() {
                        if (mIsLoadingMore || !hasMoreData(getCurrentStore())) {
                            return;
                        }

                        if (isScrollToBottom(recyclerView)) {
                            fetchMoreData();
                        }
                    }
                });
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (isScrollToBottom(recyclerView)) {
                            fetchMoreData();
                        }
                    }
                });
                recyclerView.setAdapter(adapter);
            }
        }

    }

    private static class StockRankItem {
        public String iconURL;
        public String title;
        public String tarLink;

        public StockRankItem(FocusInfo raw) {
            this.iconURL = raw.imageUrl;
            this.title = raw.info1;
            this.tarLink = raw.tarLink;
        }
    }

    private static class StockIndexItem {
        public StockBrief raw;
        public CharSequence name;
        public Double currentPrice;
        public Double changePrice;
        public Double changePriceRatio;
        public int color;

        public StockIndexItem(StockBrief raw) {
            this.raw = raw;
            this.name = safeGet(() -> raw.name, "");
            this.currentPrice = safeGet(() -> raw.last, null);
            this.changePrice = safeGet(() -> raw.change, null);
            this.changePriceRatio = safeGet(() -> raw.changeRatio, null);
            this.color = getFiveOrderPriceColor(safeGet(() -> raw.last, null), safeGet(() -> raw.prevClose, null));
        }
    }

    /**
     * 自选页面
     */
    @SuppressWarnings("deprecation")
    public static class FocusStockHomeFragment extends SimpleFragment {
        private AdvanceSwipeRefreshLayout mRefreshLayout;
        private RecyclerView mRecyclerView;
        private boolean mDataIsExpired = true;
        private boolean isCancelLogin = false;

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
            return inflater.inflate(R.layout.frag_focus_stock_home, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            mRefreshLayout = v_findView(view, R.id.refreshLayout);
            mRefreshLayout.setOnRefreshListener(() -> fetchData(false));
            AppBarLayout appBarLayout = v_findView(view, R.id.appBarLayout);
            mRefreshLayout.setOnPreInterceptTouchEventDelegate(ev -> appBarLayout.getTop() < 0);

            v_setClick(mReloadSection, R.id.section_reload, v -> fetchData(true));
            v_setClick(mContentSection, R.id.btn_add, v -> {
                showActivity(this, an_StockSearchPage());
            });

            mRecyclerView = v_findView(view, R.id.recyclerView);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            addHorizontalSepLine(mRecyclerView);

            Observable<Void> observable = Observable.merge(NotificationCenter.loginSubject, NotificationCenter.focusStockChangedSubject);
            consumeEvent(observable)
                    .setTag("user_info_changed")
                    .onNextFinish(ignored -> {
                        mDataIsExpired = true;
                        if (getView() != null && getUserVisibleHint()) {
                            fetchData(false);
                            mDataIsExpired = false;
                        }
                    })
                    .done();

            consumeEvent(NotificationCenter.cancelLoginSubject)
                    .setTag("action_cancel_login")
                    .onNextFinish(ignored -> {
                        isCancelLogin = true;
                    })
                    .done();

            consumeEvent(sFocusSelectTabSubject)
                    .setTag("select_focus_tab")
                    .onNextFinish(tabIndex -> {
                        if (tabIndex == 1) {
                            mDataIsExpired = true;
                            setUserVisibleHint(true);
                        }
                    })
                    .done();

            consumeEvent(sStockHomeStopTimerSubject)
                    .setTag("stop_timer")
                    .onNextFinish(nil -> {
                        unsubscribeFromVisible("stock_list_timer");
                    })
                    .done();

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mDataIsExpired = true;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);

            if (isVisibleToUser && getView() != null) {
                boolean reload = !v_isVisible(mContentSection);
                updateData(reload);
            } else {
                isCancelLogin = false;
            }

            if (isVisibleToUser && getView() != null) {
                if (MineManager.getInstance().isLoginOK()) {
                    Observable<MResultsInfo<Void>> observable = StockController.fetchFollowStock(false)
                            .delaySubscription(5, TimeUnit.SECONDS)
                            .repeat();
                    consumeEventMR(observable)
                            .setTag("stock_list_timer")
                            .addToVisible()
                            .onNextSuccess(response -> {
                                List<StockPosition> holdStocks = safeGet(() -> SimulationStockManager.getInstance().getAccountMore().holdStocks, Collections.<StockPosition>emptyList());
                                List<FocusStockCellVM> stockItems = Stream.of(SimulationStockManager.getInstance().followStockList)
                                        .map(it -> {
                                            boolean isHold = Stream.of(holdStocks).filter(holdStock -> it.index.equals(holdStock.stock.index)).limit(1).count() > 0;
                                            return new FocusStockCellVM(it, isHold);
                                        })
                                        .collect(Collectors.toList());
                                updateFocusStockSection(stockItems);
                            })
                            .done();
                }
            }
        }

        private void updateData(boolean reload) {
            if (!MineManager.getInstance().isLoginOK()) {
                fetchLoginData();
            } else {
                if (mDataIsExpired) {
                    fetchData(reload);
                    mDataIsExpired = false;
                }
                isCancelLogin = false;
            }
        }

        private void fetchLoginData() {
            v_setVisible(mReloadSection);
            v_setGone(mContentSection);
            v_setText(mReloadSection, R.id.label_title, "您还没登录");
            if (!isCancelLogin) {
                showActivity(this, an_LoginPage());
            }
        }

        @SuppressWarnings("unchecked")
        private void fetchData(boolean reload) {
            changeVisibleSection(TYPE_LOADING);

            Observable<List<MResultsInfo>> observable = zipToList(StockController.fetchFollowStock(reload),
                    StockController.fetchSimulationAccount(reload));
            int flags = FLAG_NO_ERROR_TOAST;
            consumeEventMRListUpdateUI(observable, reload, flags)
                    .setTag("reload_data")
                    .onNextSuccess(responses -> {
                        List<StockPosition> holdStocks = safeGet(() -> SimulationStockManager.getInstance().getAccountMore().holdStocks, Collections.<StockPosition>emptyList());
                        List<FocusStockCellVM> stockItems = Stream.of(SimulationStockManager.getInstance().followStockList)
                                .map(it -> {
                                    boolean isHold = Stream.of(holdStocks).filter(holdStock -> it.index.equals(holdStock.stock.index)).limit(1).count() > 0;
                                    return new FocusStockCellVM(it, isHold);
                                })
                                .collect(Collectors.toList());
                        SimulationCapitalCellVM vm = new SimulationCapitalCellVM(SimulationStockManager.getInstance().getAccount());
                        updateSimulationCapitalSection(vm);
                        updateFocusStockSection(stockItems);
                        changeVisibleSection(TYPE_CONTENT);
                    })
                    .onNextFail(responses -> {
                        View capitalHeader = v_findView(mContentSection, R.id.header_capital);
                        v_setText(capitalHeader, R.id.label_total_capital, "尚未开通模拟账户");
                        v_setText(capitalHeader, R.id.label_income_today, "去开通");
                        v_setClick(capitalHeader, v -> {
                            showActivity(this, an_StockTradePage(-1, "", 0));
                        });
                    })
                    .done();
            mDataIsExpired = false;
        }

        private void updateSimulationCapitalSection(SimulationCapitalCellVM vm) {
            View capitalHeader = v_findView(mContentSection, R.id.header_capital);
            v_setText(capitalHeader, R.id.label_total_capital, vm.totalCapital);
            v_setText(capitalHeader, R.id.label_income_today, vm.todayIncome);
            v_setClick(capitalHeader, v -> {
                showActivity(this, an_StockTradePage(-1, "", 0));
            });
        }

        private int mLastRange = RANGE_ALL;
        private int mLastSort = SORT_BY_DEFAULT;
        private int mLastFlag = FLAG_SHOW_CHANGE_RATIO;

        private void updateFocusStockSection(List<FocusStockCellVM> items) {

            consumeEvent(NotificationCenter.followStockSortChangeSubject)
                    .setTag("follow_stock_sort_changed")
                    .onNextFinish(nil -> {
                        mLastRange = RANGE_ALL;
                        mLastSort = SORT_BY_DEFAULT;
                        updateFocusStockHeader();
                        resortFocusStockList(items);
                    })
                    .done();

            View header = v_findView(mContentSection, R.id.header_focus_stock);
            v_setClick(header, R.id.area1, v -> {

                UmengUtil.stat_click_event(UmengUtil.eEVENTIDChangeAllHolds);

                int range = mLastRange;
                if (range == RANGE_ALL) {
                    range = RANGE_HOLD;
                } else if (range == RANGE_HOLD) {
                    range = RANGE_ALL;
                }
                mLastRange = range;
                updateFocusStockHeader();
                resortFocusStockList(items);
            });
            v_setClick(header, R.id.area2, v -> {
                int sort = mLastSort;
                if (sort == SORT_BY_PRICE_ASCENT) {
                    sort = SORT_BY_PRICE_DESCENT;
                } else if (sort == SORT_BY_PRICE_DESCENT) {
                    sort = SORT_BY_DEFAULT;
                } else {
                    sort = SORT_BY_PRICE_ASCENT;
                }
                mLastSort = sort;
                updateFocusStockHeader();
                resortFocusStockList(items);
            });
            v_setClick(header, R.id.area3, v -> {
                int flag = mLastFlag;
                int sort = mLastSort;
                if (flag == FLAG_SHOW_CHANGE) {
                    if (sort == SORT_BY_CHANGE_DESCENT) {
                        sort = SORT_BY_CHANGE_ASCENT;
                    } else if (sort == SORT_BY_CHANGE_ASCENT) {
                        sort = SORT_BY_DEFAULT;
                    } else {
                        sort = SORT_BY_CHANGE_DESCENT;
                    }
                } else if (flag == FLAG_SHOW_CHANGE_RATIO) {
                    if (sort == SORT_BY_CHANGE_RATIO_DESCENT) {
                        sort = SORT_BY_CHANGE_RATIO_ASCENT;
                    } else if (sort == SORT_BY_CHANGE_RATIO_ASCENT) {
                        sort = SORT_BY_DEFAULT;
                    } else {
                        sort = SORT_BY_CHANGE_RATIO_DESCENT;
                    }
                }
                mLastSort = sort;
                updateFocusStockHeader();
                resortFocusStockList(items);
            });

            if (items.isEmpty()) {
                v_setGone(header);
                v_setGone(mContentSection, R.id.recyclerView);
                v_setVisible(mContentSection, R.id.section_add_stock);
            } else {
                v_setVisible(header);
                v_setVisible(mContentSection, R.id.recyclerView);
                v_setGone(mContentSection, R.id.section_add_stock);
            }

            updateFocusStockHeader();
            resortFocusStockList(items);
        }

        private void updateFocusStockHeader() {
            int range = mLastRange;
            int sort = mLastSort;
            int flag = mLastFlag;

            View header = v_findView(mContentSection, R.id.header_focus_stock);
            TextView rangeLabel = v_findView(header, R.id.text1);
            ImageView rangeImage = v_findView(header, R.id.icon1);
            TextView priceLabel = v_findView(header, R.id.text2);
            ImageView priceImage = v_findView(header, R.id.icon2);
            TextView changeLabel = v_findView(header, R.id.text3);
            ImageView changeImage = v_findView(header, R.id.icon3);

            final int ICON_TYPE_DEFAULT = 0;
            final int ICON_TYPE_ASCENT = 1;
            final int ICON_TYPE_DESCENT = 2;

            Action3<View, ImageView, Integer> changeIconFunc = (textView, imageView, sortIconType) -> {
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
                params.leftMargin = dp2px(2);
                params.addRule(RelativeLayout.RIGHT_OF, textView.getId());
                if (sortIconType == ICON_TYPE_DEFAULT) {
                    params.bottomMargin = dp2px(2);
                    params.addRule(RelativeLayout.ALIGN_BOTTOM, textView.getId());
                    imageView.setImageResource(R.drawable.ic_menu_drop);
                } else {
                    params.bottomMargin = 0;
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    if (sortIconType == ICON_TYPE_ASCENT) {
                        imageView.setImageResource(R.drawable.ic_sort_ascent);
                    } else if (sortIconType == ICON_TYPE_DESCENT) {
                        imageView.setImageResource(R.drawable.ic_sort_descent);
                    }
                }
                imageView.setLayoutParams(params);
            };

            if (range == RANGE_ALL) {
                v_setText(rangeLabel, "全部");
            } else if (range == RANGE_HOLD) {
                v_setText(rangeLabel, "持有");
            }
            changeIconFunc.call(rangeLabel, rangeImage, ICON_TYPE_DEFAULT);

            if (flag == FLAG_SHOW_CHANGE) {
                v_setText(changeLabel, "涨跌额");
            } else if (flag == FLAG_SHOW_CHANGE_RATIO) {
                v_setText(changeLabel, "涨跌幅");
            }

            if (sort == SORT_BY_DEFAULT) {
                changeIconFunc.call(priceLabel, priceImage, ICON_TYPE_DEFAULT);
                changeIconFunc.call(changeLabel, changeImage, ICON_TYPE_DEFAULT);
            } else if (sort == SORT_BY_PRICE_ASCENT) {
                changeIconFunc.call(priceLabel, priceImage, ICON_TYPE_ASCENT);
                changeIconFunc.call(changeLabel, changeImage, ICON_TYPE_DEFAULT);
            } else if (sort == SORT_BY_PRICE_DESCENT) {
                changeIconFunc.call(priceLabel, priceImage, ICON_TYPE_DESCENT);
                changeIconFunc.call(changeLabel, changeImage, ICON_TYPE_DEFAULT);
            } else if (sort == SORT_BY_CHANGE_ASCENT || sort == SORT_BY_CHANGE_RATIO_ASCENT) {
                changeIconFunc.call(priceLabel, priceImage, ICON_TYPE_DEFAULT);
                changeIconFunc.call(changeLabel, changeImage, ICON_TYPE_ASCENT);
            } else if (sort == SORT_BY_CHANGE_DESCENT || sort == SORT_BY_CHANGE_RATIO_DESCENT) {
                changeIconFunc.call(priceLabel, priceImage, ICON_TYPE_DEFAULT);
                changeIconFunc.call(changeLabel, changeImage, ICON_TYPE_DESCENT);
            }
        }

        private void resortFocusStockList(List<FocusStockCellVM> items) {
            Func1<FocusStockCellVM, Boolean> filterFunc = filterFuncMap.get(mLastRange);
            Func2<FocusStockCellVM, FocusStockCellVM, Integer> sortFunc = sortFuncMap.get(mLastSort);
            if (filterFunc == null || sortFunc == null) {
                return;
            }

            List<FocusStockCellVM> sortedItems = Stream.of(items)
                    .filter(it -> filterFunc.call(it))
                    .sorted((lhs, rhs) -> sortFunc.call(lhs, rhs))
                    .collect(Collectors.toList());

            RecyclerView recyclerView = v_findView(mContentSection, R.id.recyclerView);
            if (recyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<FocusStockCellVM> adapter = getSimpleAdapter(recyclerView);
                adapter.resetItems(sortedItems);
            } else {
                SimpleRecyclerViewAdapter<FocusStockCellVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(sortedItems)
                        .onCreateItemView(R.layout.cell_focus_stock)
                        .onCreateViewHolder(builder -> {
                            builder
                                    .bindChildWithTag("nameAndCode", R.id.label_stock_name_and_code)
                                    .bindChildWithTag("price", R.id.label_price)
                                    .bindChildWithTag("float", R.id.label_float)
                                    .bindChildWithTag("indicator", R.id.indicator)
                                    .configureView((item, pos) -> {
                                        v_setText(builder.getChildWithTag("nameAndCode"), item.stockNameAndCode);
                                        v_setText(builder.getChildWithTag("price"), item.price);
                                        TextView ratioLabel = builder.getChildWithTag("float");
                                        if (mLastFlag == FLAG_SHOW_CHANGE) {
                                            ratioLabel.setText(item.change);
                                        } else if (mLastFlag == FLAG_SHOW_CHANGE_RATIO) {
                                            ratioLabel.setText(item.changeRatio);
                                        }
                                        ratioLabel.setBackgroundDrawable(new ShapeDrawable(item.priceRatioViewBG));
                                        builder.getChildWithTag("indicator").setVisibility(item.isHold ? View.VISIBLE : View.INVISIBLE);
                                    });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            View itemView = holder.itemView;
                            v_setLongClick(itemView, v -> {
                                showActivity(this, an_FocusStockEditPage());
                            });
                            v_setClick(itemView, v -> {
                                FocusStockCellVM item = ad.getItem(holder.getAdapterPosition());
                                showActivity(this, an_QuotationDetailPage(item.raw.index));
                            });
                            v_setLongClick(itemView, R.id.label_float, v -> {
                                showActivity(this, an_FocusStockEditPage());
                            });
                            v_setClick(itemView, R.id.label_float, v -> {
                                UmengUtil.stat_click_event(UmengUtil.eEVENTIDChangeStockChange);

                                if (mLastFlag == FLAG_SHOW_CHANGE) {
                                    mLastFlag = FLAG_SHOW_CHANGE_RATIO;
                                } else if (mLastFlag == FLAG_SHOW_CHANGE_RATIO) {
                                    mLastFlag = FLAG_SHOW_CHANGE;
                                }
                                if (mLastSort == SORT_BY_CHANGE_ASCENT) {
                                    mLastSort = SORT_BY_CHANGE_RATIO_ASCENT;
                                } else if (mLastSort == SORT_BY_CHANGE_DESCENT) {
                                    mLastSort = SORT_BY_CHANGE_RATIO_DESCENT;
                                } else if (mLastSort == SORT_BY_CHANGE_RATIO_ASCENT) {
                                    mLastSort = SORT_BY_CHANGE_ASCENT;
                                } else if (mLastSort == SORT_BY_CHANGE_RATIO_DESCENT) {
                                    mLastSort = SORT_BY_CHANGE_DESCENT;
                                }
                                updateFocusStockHeader();
                                resortFocusStockList(ad.getItems());
                            });
                        })
                        .create();
                recyclerView.setAdapter(adapter);
            }
        }

        private static int FLAG_SHOW_CHANGE = 0;
        private static int FLAG_SHOW_CHANGE_RATIO = 1;

        private static int RANGE_ALL = 0;
        private static int RANGE_HOLD = 1;
        private static SparseArray<Func1<FocusStockCellVM, Boolean>> filterFuncMap = new SparseArray<>();

        static {
            filterFuncMap.append(RANGE_ALL, it -> true);
            filterFuncMap.append(RANGE_HOLD, it -> it.isHold);
        }

        private static int SORT_BY_DEFAULT = 0;

        private static int SORT_BY_PRICE_ASCENT = 1;
        private static int SORT_BY_PRICE_DESCENT = 2;

        private static int SORT_BY_CHANGE_ASCENT = 3;
        private static int SORT_BY_CHANGE_DESCENT = 4;

        private static int SORT_BY_CHANGE_RATIO_ASCENT = 5;
        private static int SORT_BY_CHANGE_RATIO_DESCENT = 6;
        private static SparseArray<Func2<FocusStockCellVM, FocusStockCellVM, Integer>> sortFuncMap = new SparseArray<>();
        private static Func1<Func2<FocusStockCellVM, FocusStockCellVM, Integer>, Func2<FocusStockCellVM, FocusStockCellVM, Integer>> reverse = it -> (left, right) -> it.call(right, left);

        static {
            sortFuncMap.append(SORT_BY_DEFAULT, (left, right) -> {
                return 0;
            });

            sortFuncMap.append(SORT_BY_PRICE_ASCENT, (left, right) -> {
                if (left.rawPrice > right.rawPrice) {
                    return 1;
                } else if (left.rawPrice < right.rawPrice) {
                    return -1;
                } else {
                    return 0;
                }
            });
            sortFuncMap.append(SORT_BY_PRICE_DESCENT, reverse.call(sortFuncMap.get(SORT_BY_PRICE_ASCENT)));

            sortFuncMap.append(SORT_BY_CHANGE_ASCENT, (left, right) -> {
                if (left.rawChange > right.rawChange) {
                    return 1;
                } else if (left.rawChange < right.rawChange) {
                    return -1;
                } else {
                    return 0;
                }
            });
            sortFuncMap.append(SORT_BY_CHANGE_DESCENT, reverse.call(sortFuncMap.get(SORT_BY_CHANGE_ASCENT)));

            sortFuncMap.append(SORT_BY_CHANGE_RATIO_ASCENT, (left, right) -> {
                if (left.rawChangeRatio > right.rawChangeRatio) {
                    return 1;
                } else if (left.rawChangeRatio < right.rawChangeRatio) {
                    return -1;
                } else {
                    return 0;
                }
            });
            sortFuncMap.append(SORT_BY_CHANGE_RATIO_DESCENT, reverse.call(sortFuncMap.get(SORT_BY_CHANGE_RATIO_ASCENT)));
        }
    }

    private static class SimulationCapitalCellVM {

        private boolean isRunning;
        private CharSequence totalCapital;
        private CharSequence todayIncome;

        public SimulationCapitalCellVM(SimulationAccount account) {
            isRunning = SimulationAccountExtension.isOpenedSimulationAccount();
            if (isRunning) {
                this.totalCapital = concat("模拟账户总资产", setFontSize(setColor(formatMoney(account.fortuneTurnover, false, 2), TEXT_BLACK_COLOR), sp2px(16)));
                this.todayIncome = concat("今日盈亏", setFontSize(setColor(formatMoney(account.todayIncome, true, 2), getIncomeTextColor(account.todayIncome)), sp2px(16)));
            } else {
                this.totalCapital = "尚未开通模拟炒股";
                this.todayIncome = "去开通";
            }
        }
    }

    private static class FocusStockCellVM {

        public StockSimple raw;
        public String stockId;
        public CharSequence stockNameAndCode;
        public CharSequence price;
        public CharSequence change;
        public CharSequence changeRatio;
        public Shape priceRatioViewBG = new RoundCornerShape(LIGHT_GREY_COLOR, dp2px(3));
        public boolean isHold;

        public long rawAddTime;
        public double rawPrice;
        public double rawChange;
        public double rawChangeRatio;

        public FocusStockCellVM(StockBrief raw, boolean isHold) {
            this.raw = raw;
            this.stockId = safeGet(() -> raw.index, "");
            this.stockNameAndCode = safeGet(() -> concat(raw.name, setColor(setFontSize(raw.code, sp2px(10)), TEXT_GREY_COLOR)))
                    .def(concat(PlaceHolder.NULL_VALUE, setColor(setFontSize(PlaceHolder.NULL_VALUE, sp2px(10)), TEXT_GREY_COLOR))).get();
            this.price = safeGet(() -> formatMoney(raw.last, false, 2), PlaceHolder.NULL_VALUE);
            this.change = safeGet(() -> formatMoney(raw.change, true, 2), PlaceHolder.NULL_VALUE);
            this.changeRatio = safeGet(() -> formatRatio(raw.changeRatio, true, 2), PlaceHolder.NULL_VALUE);
            this.isHold = isHold;

            this.rawAddTime = 0;
            this.rawPrice = safeGet(() -> raw.last, 0.0);
            this.rawChange = safeGet(() -> raw.change, 0.0);
            this.rawChangeRatio = safeGet(() -> raw.changeRatio, 0.0);
            this.priceRatioViewBG = new RoundCornerShape(raw.change > 0 ? RED_COLOR : (raw.change == 0 ? LIGHT_GREY_COLOR : GREEN_COLOR), dp2px(3));
        }
    }

    public static class FocusStockEditFragment extends SimpleFragment {
        private PublishSubject<Void> itemCheckChangedSubject = PublishSubject.create();
        private boolean mSortChanged = false;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_focus_stock_edit, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            v_setClick(mReloadSection, v -> fetchData());
            fetchData();
        }

        @Override
        protected boolean onInterceptGoBack() {
            if (mSortChanged) {
                RecyclerView recyclerView = v_findView(mContentSection, R.id.recyclerView);
                if (recyclerView.getAdapter() != null) {
                    GMFProgressDialog dialog = new GMFProgressDialog(getActivity(), "操作中，请稍候...");
                    dialog.show();

                    NotificationCenter.followStockSortChangeSubject.onNext(null);
                    SimpleRecyclerViewAdapter<FocusStockEditVM> adapter = getSimpleAdapter(recyclerView);
                    List<StockBrief> items = Stream.of(adapter.getItems())
                            .map(it -> it.raw)
                            .collect(Collectors.toList());
                    consumeEventMR(StockController.resortFollowStock(items))
                            .onNextStart(response -> {
                                dialog.dismiss();
                            })
                            .onNextFail(response -> {
                                showToast(this, getErrorMessage(response));
                            })
                            .onNextFinish(response -> {
                                mSortChanged = false;
                                goBack(this);
                            })
                            .done();
                    return true;
                }
            }

            NotificationCenter.focusStockChangedSubject.onNext(null);
            return super.onInterceptGoBack();
        }

        private void fetchData() {
            consumeEventMRUpdateUI(StockController.fetchFollowStock(true), true)
                    .onNextSuccess(response -> {
                        List<StockBrief> focusStocks = SimulationStockManager.getInstance().followStockList;
                        List<StockSimple> holdStocks = Stream.of(SimulationStockManager.getInstance().getAccountMore().holdStocks)
                                .map(it -> it.stock)
                                .collect(Collectors.toList());
                        resetRecyclerView(focusStocks, holdStocks);
                        setupBottomSection();
                    })
                    .done();
        }

        private void resetRecyclerView(List<StockBrief> focusStocks, List<StockSimple> holdStocks) {
            List<FocusStockEditVM> items = Stream.of(focusStocks)
                    .map(it -> new FocusStockEditVM(it, holdStocks))
                    .collect(Collectors.toList());

            Context ctx = getActivity();
            RecyclerView recyclerView = v_findView(mContentSection, R.id.recyclerView);
            if (recyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<FocusStockEditVM> adapter = getSimpleAdapter(recyclerView);
                adapter.resetItems(items);
            } else {
                recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
                RecyclerViewExtension.addHorizontalSepLine(recyclerView);

                SimpleRecyclerViewAdapter<FocusStockEditVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                        .onCreateItemView(R.layout.cell_focus_stock_edit)
                        .onCreateViewHolder(builder -> {
                            builder.bindChildWithTag("stockNameAndPriceLabel", R.id.label_stock_name_and_code)
                                    .bindChildWithTag("priceLabel", R.id.label_stock_price)
                                    .bindChildWithTag("indicatorImage", R.id.img_indicator)
                                    .bindChildWithTag("deleteCheckBox", R.id.cb_delete)
                                    .configureView((holder, item, pos) -> {
                                        v_setText(builder.getChildWithTag("stockNameAndPriceLabel"), item.stockNameAndCode);
                                        v_setText(builder.getChildWithTag("priceLabel"), item.price);
                                        v_setVisibility(builder.getChildWithTag("indicatorImage"), item.isHold ? View.VISIBLE : View.GONE);
                                        v_setSelected(builder.getChildWithTag("deleteCheckBox"), item.isChecked);
                                        v_setImageResource(builder.getChildWithTag("deleteCheckBox"), item.isChecked ? R.mipmap.bg_checkbox_checked : R.mipmap.bg_checkbox_unchecked);
                                    });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            Func0<FocusStockEditVM> itemGetter = () -> ad.getItem(holder.getAdapterPosition());
                            View itemView = holder.itemView;
                            v_setClick(itemView, R.id.cb_delete, v -> {
                                FocusStockEditVM item = itemGetter.call();
                                item.isChecked = !item.isChecked;
                                ad.notifyItemChanged(holder.getAdapterPosition());
                                itemCheckChangedSubject.onNext(null);
                            });
                        })
                        .create();
                recyclerView.setAdapter(adapter);
                recyclerView.setItemAnimator(null);
                ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                    @Override
                    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                        int swipeFlags = 0;
                        return makeMovementFlags(dragFlags, swipeFlags);
                    }

                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return true;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    }

                    @Override
                    public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
                        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
                        Collections.swap(adapter.getItems(), fromPos, toPos);
                        adapter.notifyItemMoved(fromPos, toPos);
                        mSortChanged = true;
                    }
                });
                helper.attachToRecyclerView(recyclerView);
            }
            recyclerView.post(() -> itemCheckChangedSubject.onNext(null));
        }

        private void setupBottomSection() {
            RecyclerView recyclerView = v_findView(mContentSection, R.id.recyclerView);
            View bottomSection = v_findView(mContentSection, R.id.section_bottom);
            Func0<List<FocusStockEditVM>> itemsGetter = () -> {
                SimpleRecyclerViewAdapter<FocusStockEditVM> adapter = getSimpleAdapter(recyclerView);
                return adapter.getItems();
            };

            Button allBtn = v_findView(bottomSection, R.id.btn_all);
            v_setClick(allBtn, v -> {
                boolean isAlreadyAllChecked = Stream.of(itemsGetter.call()).filter(it -> !it.isChecked).limit(1).count() == 0;
                SimpleRecyclerViewAdapter<FocusStockEditVM> adapter = getSimpleAdapter(v_findView(mContentSection, R.id.recyclerView));
                Stream.of(adapter.getItems())
                        .forEach(it -> {
                            it.isChecked = isAlreadyAllChecked ? false : true;
                        });
                adapter.notifyDataSetChanged();
                itemCheckChangedSubject.onNext(null);
            });

            v_setClick(bottomSection, R.id.btn_delete, v -> {
                List<FocusStockEditVM> items = itemsGetter.call();
                List<StockBrief> needToDeleteItems = Stream.of(items)
                        .filter(it -> it.isChecked)
                        .map(it -> it.raw)
                        .collect(Collectors.toList());
                if (!needToDeleteItems.isEmpty()) {
                    GMFProgressDialog dialog = new GMFProgressDialog(getActivity(), "操作中，请稍候...");
                    dialog.show();
                    consumeEventMR(StockController.deleteFollowStock(needToDeleteItems))
                            .onNextStart(response -> {
                                dialog.dismiss();
                            })
                            .onNextSuccess(response -> {
                                SimpleRecyclerViewAdapter<FocusStockEditVM> adapter = getSimpleAdapter(recyclerView);
                                int count = items.size();
                                for (int i = count - 1; i >= 0; i--) {
                                    if (items.get(i).isChecked) {
                                        items.remove(i);
                                    }
                                }
                                adapter.resetItems(items);
                                itemCheckChangedSubject.onNext(null);
                            })
                            .onNextFail(response -> {
                                showToast(this, getErrorMessage(response));
                            })
                            .done();
                }
            });

            Button deleteBtn = v_findView(bottomSection, R.id.btn_delete);
            deleteBtn.setTextColor(new ColorStateList(new int[][]{{android.R.attr.state_enabled}, {}}, new int[]{TEXT_RED_COLOR, TEXT_GREY_COLOR}));

            consumeEvent(itemCheckChangedSubject)
                    .setTag("onItemCheckChanged")
                    .onNextFinish(ignored -> {
                        long checkedItemCount = Stream.of(itemsGetter.call())
                                .filter(it -> it.isChecked)
                                .count();
                        boolean isAlreadyAllChecked = checkedItemCount == itemsGetter.call().size();
                        allBtn.setText(isAlreadyAllChecked ? "全不选" : "全选");
                        v_setText(bottomSection, R.id.btn_delete, checkedItemCount > 0 ? String.format(Locale.getDefault(), "删除（%d）", checkedItemCount) : "删除");
                        v_setEnabled(bottomSection, R.id.btn_delete, checkedItemCount > 0);
                    })
                    .done();
        }
    }

    private static class FocusStockEditVM {
        private StockBrief raw;
        public CharSequence stockNameAndCode;
        public CharSequence price;
        public boolean isHold;
        public boolean isChecked;

        public FocusStockEditVM(StockBrief raw, List<StockSimple> holdStocks) {
            this.raw = raw;
            this.stockNameAndCode = safeGet(() -> {
                CharSequence name = setStyle(raw.name, Typeface.BOLD);
                CharSequence code = setColor(setFontSize(raw.code, sp2px(10)), TEXT_GREY_LIGHT_COLOR);
                return concat(name, code);
            }, "");

            this.price = safeGet(() -> formatMoney(raw.last, false, 0, 2), PlaceHolder.NULL_VALUE);
            this.isHold = safeGet(() -> Stream.of(holdStocks).filter(it -> it.index.equals(raw.index)).limit(1).count() > 0, false);
        }
    }

    /**
     * 股票大赛
     */
    public static class StockMatchHomeFragment extends SimpleFragment {

        private boolean mDataExpired = true;
        private StockMatchHomeImageAdapter mAdapter;
        private ViewPager mPager;
        private Subscription mAutoPlayFocusImageSubscription;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_stock_match2, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            changeVisibleSection(TYPE_LOADING);
            setOnSwipeRefreshListener(() -> fetchData(false));
            v_setClick(mReloadSection, v -> fetchData(true));

            Observable<Void> observable = Observable.merge(NotificationCenter.loginSubject, NotificationCenter.logoutSubject);
            consumeEvent(observable)
                    .setTag("user_info_changed")
                    .onNextFinish(ignored -> {
                        mDataExpired = true;
                        if (getView() != null && getUserVisibleHint()) {
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

            consumeEvent(sFocusSelectTabSubject)
                    .setTag("select_match")
                    .onNextFinish(tabIndex -> {
                        if (tabIndex == 2) {
                            mDataExpired = true;
                            setUserVisibleHint(true);
                        }
                    })
                    .done();

            consumeEvent(sStockSelectTabSubject)
                    .setTag("select_action")
                    .onNextFinish(tabIndex -> {
                        if (tabIndex == 2) {
                            mDataExpired = true;
                            setUserVisibleHint(true);
                        }
                    })
                    .done();

            consumeEvent(sStockHomeStopTimerSubject)
                    .setTag("stop_timer")
                    .onNextFinish(nil -> {
                        stopAutoPlayFocusImage();
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
            stopAutoPlayFocusImage();
        }


        @Override
        protected boolean isTraceLifeCycle() {
            return false;
        }

        @Override
        protected boolean isDelegateLifeCycleEventToSetUserVisible() {
            return false;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (mDataExpired) {
                    fetchData(false);
                    mDataExpired = false;
                }
            } else {
                stopAutoPlayFocusImage();
            }
        }

        private void fetchData(boolean reload) {
            if (reload) {
                changeVisibleSection(TYPE_LOADING);
            }

            if (MineManager.getInstance().isLoginOK()) {

                Observable<List<MResultsInfo>> observable = zipToList(refreshStockMatch(), refreshStockMatchWithMine());
                consumeEventMRListUpdateUI(observable, reload)
                        .setTag("reload_data")
                        .onNextSuccess(responses -> {
                            MResultsInfo<CommandPageArray<GMFMatch>> allMatchCallback = responses.get(0);
                            MResultsInfo<CommandPageArray<GMFMatch>> mineMatchCallback = responses.get(1);

                            updateContent(allMatchCallback.data, mineMatchCallback.data);
                            changeVisibleSection(TYPE_CONTENT);
                        })
                        .done();
            } else {
                consumeEventMRUpdateUI(refreshStockMatch(), reload)
                        .setTag("reload_data")
                        .onNextSuccess(responses -> {
                            CommandPageArray<GMFMatch> allMatchCallback = responses.data;
                            updateContent(allMatchCallback, null);
                            changeVisibleSection(TYPE_CONTENT);
                        })
                        .done();
            }


        }

        private void updateContent(CommandPageArray<GMFMatch> allMatchArray, CommandPageArray<GMFMatch> mineMatchArray) {
            List<StockMatchHomeVM> focusImageData = Stream.of(opt(allMatchArray).let(it -> it.data()).or(Collections.emptyList()))
                    .filter(it -> it.state != STATE_OVER)
                    .map(it -> new StockMatchHomeVM(it))
                    .collect(Collectors.toList());

            List<StockMatchHomeVM> allMatchData = null;
            if (allMatchArray != null) {
                allMatchData = Stream.of(opt(allMatchArray).let(it -> it.data()).or(Collections.emptyList()))
                        .filter(it -> !it.bSignUp && it.state != STATE_OVER)
                        .map(it -> new StockMatchHomeVM(it))
                        .collect(Collectors.toList());

                List<StockMatchHomeVM> overAllMatchData = Stream.of(opt(allMatchArray).let(it -> it.data()).or(Collections.emptyList()))
                        .filter(it -> !it.bSignUp && it.state == STATE_OVER)
                        .map(it -> new StockMatchHomeVM(it))
                        .collect(Collectors.toList());

                if (overAllMatchData != null && !overAllMatchData.isEmpty()) {
                    allMatchData.add(overAllMatchData.get(0));
                }
            }

            List<StockMatchHomeVM> mineMatchData = null;
            if (mineMatchArray != null) {
                mineMatchData = Stream.of(opt(mineMatchArray).let(it -> it.data()).or(Collections.emptyList()))
                        .filter(it -> it.state != STATE_OVER)
                        .map(it -> new StockMatchHomeVM(it))
                        .collect(Collectors.toList());

                List<StockMatchHomeVM> overMineMatchData = Stream.of(opt(mineMatchArray).let(it -> it.data()).or(Collections.emptyList()))
                        .filter(it -> it.state == STATE_OVER)
                        .map(it -> new StockMatchHomeVM(it))
                        .collect(Collectors.toList());

                if (overMineMatchData != null && !overMineMatchData.isEmpty()) {
                    mineMatchData.add(overMineMatchData.get(0));
                }
            }

            updateFocusImageSection(focusImageData);
            updateMineMatchSection(mineMatchData);
            updateAllMatchSection(allMatchData);
            updateSetMatchSection();
        }

        private void updateFocusImageSection(List<StockMatchHomeVM> focusImageData) {
            View imageSection = v_findView(mContentSection, R.id.img_focus_container);
            mPager = v_findView(imageSection, R.id.pager);
            LinearLayout pointContainer = v_findView(imageSection, R.id.point_container);
            if (focusImageData.isEmpty()) {
                v_setGone(imageSection);
                return;
            }

            v_preDraw(mPager, true, view -> {
                ViewGroup.LayoutParams parentParams = mPager.getLayoutParams();
                parentParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                parentParams.height = (int) (mPager.getMeasuredWidth() * 0.4);
                mPager.setLayoutParams(parentParams);
            });

            if (mPager.getAdapter() == null) {
                mAdapter = new StockMatchHomeImageAdapter(getChildFragmentManager(), focusImageData);
                mPager.setAdapter(mAdapter);
                int currentIndex = mAdapter.getCount() / 2 - mAdapter.getCount() / 2 % focusImageData.size();
                mPager.setCurrentItem(currentIndex);
            } else {
                mAdapter.notifyDataSetChanged();
                mPager.setCurrentItem(mPager.getCurrentItem());
            }

            if (focusImageData.size() == 1) {
                v_setGone(pointContainer);
            } else {
                pointContainer.removeAllViews();
                for (int i = 0; i < focusImageData.size(); i++) {
                    View pointView = new View(getActivity());
                    pointView.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0x33000000, dp2px(4))));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp2px(8), dp2px(8));
                    if (i != 0) {
                        params.leftMargin = dp2px(8);
                    } else {
                        pointView.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(BLACK_COLOR, dp2px(4))));
                    }
                    pointContainer.addView(pointView, params);
                }
            }

            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {

                    position = position % focusImageData.size();
                    for (int i = 0; i < pointContainer.getChildCount(); i++) {
                        View child = pointContainer.getChildAt(i);
                        child.setBackgroundDrawable(i == position ? new ShapeDrawable(new RoundCornerShape(BLACK_COLOR, dp2px(4))) : new ShapeDrawable(new RoundCornerShape(0x33000000, dp2px(4))));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            mPager.setOnTouchListener((v, event) -> {
                int action = event.getAction() & event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                    mRefreshLayout.setEnabled(false);
                    stopAutoPlayFocusImage();
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_OUTSIDE || action == MotionEvent.ACTION_CANCEL) {
                    mRefreshLayout.setEnabled(true);
                    startAutoPlayFocusImage();
                }
                return false;
            });

            startAutoPlayFocusImage();

        }

        private void startAutoPlayFocusImage() {
            stopAutoPlayFocusImage();
            mAutoPlayFocusImageSubscription = Observable.interval(6, 6, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .filter(nil -> mPager.getAdapter() != null)
                    .subscribe(nil -> {
                        int childCount = mPager.getAdapter().getCount();
                        int nextIndex = mPager.getCurrentItem() + 1;
                        if (nextIndex < childCount) {
                            mPager.setCurrentItem(nextIndex);
                        }
                    });
        }

        private void stopAutoPlayFocusImage() {
            if (mAutoPlayFocusImageSubscription != null) {
                mAutoPlayFocusImageSubscription.unsubscribe();
                mAutoPlayFocusImageSubscription = null;
            }
        }

        private void updateMineMatchSection(List<StockMatchHomeVM> mineMatchData) {
            View mineMatchSection = v_findView(mContentSection, R.id.section_mine_match);
            View mineMatchCell = v_findView(mineMatchSection, R.id.cell_mine_match);
            v_setClick(mineMatchCell, v -> showActivity(this, an_MineStockCompetePage()));
            LinearLayout mineMatchContainer = v_findView(mineMatchSection, R.id.list_mine_match);
            mineMatchContainer.removeAllViews();
            if (mineMatchData == null) {
                v_setGone(mineMatchSection);
                return;
            }

            if (mineMatchData.isEmpty()) {
                v_setGone(mineMatchSection);
            } else {
                Stream.of(mineMatchData)
                        .map(data -> createStockMatchCell(mineMatchContainer, data))
                        .filter(view -> view != null)
                        .forEach(mineMatchContainer::addView);
                v_setVisible(mineMatchSection);
            }
        }

        private void updateAllMatchSection(List<StockMatchHomeVM> allMatchData) {
            View allMatchSection = v_findView(mContentSection, R.id.section_all_match);
            View allMatchCell = v_findView(allMatchSection, R.id.cell_all_match);
            v_setClick(allMatchCell, v -> showActivity(this, an_AllStockCompetePage()));
            LinearLayout allMatchContainer = v_findView(allMatchSection, R.id.list_all_match);
            allMatchContainer.removeAllViews();
            if (allMatchData.isEmpty()) {
                View cell = LayoutInflater.from(getActivity()).inflate(R.layout.cell_stock_no_match_list, allMatchContainer, false);
                allMatchContainer.addView(cell);
            } else {
                Stream.of(allMatchData)
                        .map(data -> createStockMatchCell(allMatchContainer, data))
                        .filter(view -> view != null)
                        .forEach(allMatchContainer::addView);
            }
        }

        private View createStockMatchCell(ViewGroup parent, StockMatchHomeVM vm) {
            Context ctx = parent.getContext();
            View cell = LayoutInflater.from(ctx).inflate(R.layout.cell_stock_match_list_page, parent, false);
            v_setClick(cell, v -> showActivity(this, an_StockMatchDetailPage(vm.matchId)));
            v_setText(cell, R.id.label_match_name, vm.matchTitle);
            v_setImageResource(cell, R.id.img_match_state, vm.resId);
            View signingButton = v_findView(cell, R.id.btn_signing);
            if (vm.hasSignUp) {
                v_setText(cell, R.id.label_match_rank, vm.rankPosition);
                v_setVisible(cell, R.id.label_match_rank);
                v_setGone(signingButton);
            } else {
                if (vm.matchState == STATE_SIGNUP || vm.matchState == STATE_ING) {
                    v_setGone(cell, R.id.label_match_rank);
                    v_setVisible(signingButton);
                    v_setClick(signingButton, v -> showActivity(this, an_StockMatchDetailPage(vm.matchId)));
                } else {
                    v_setGone(cell, R.id.label_match_rank);
                    v_setGone(signingButton);
                }
            }
            return cell;
        }

        private void updateSetMatchSection() {
            View setMatchCell = v_findView(mContentSection, R.id.cell_set_match);
            v_setClick(setMatchCell, v -> showActivity(this, an_WebViewPage(CommonDefine.H5URL_Create_Game())));
        }

        private void performSignUp(View view, StockMatchHomeVM vm) {
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
                            NotificationCenter.signUpStockMatchSuccessSubject.onNext(vm.matchId);
                        });
                        builder.create().show();
                    })
                    .onNextFail(response -> {
                        createAlertDialog(this, getErrorMessage(response)).show();
                    })
                    .done();
        }
    }

    public static class StockMatchHomeImageAdapter extends FragmentPagerAdapter {

        private List<StockMatchHomeVM> mData;

        public StockMatchHomeImageAdapter(FragmentManager fm, List<StockMatchHomeVM> data) {
            super(fm);
            mData = data;
        }

        @Override
        public Fragment getItem(int position) {
            if (mData != null) {
                position = position % mData.size();
                return new StockMatchHomeImageFragment().init(mData.get(position).coverURL, mData.get(position).matchId);
            }
            return null;
        }

        @Override
        public int getCount() {
            if (mData != null) {
                if (mData.size() > 1)
                    return mData.size() * 1000;
                return mData.size();
            }
            return 0;
        }
    }

    public static class StockMatchHomeImageFragment extends BaseFragment {

        //        private StockMatchHomeVM mVM;
        private String coverUrl;
        private String matchId;

        public StockMatchHomeImageFragment init(String coverUrl, String matchId) {
            Bundle arguments = new Bundle();
            arguments.putString("cover_url", coverUrl);
            arguments.putString("match_id", matchId);
            setArguments(arguments);
            return this;
        }

        @Override
        protected boolean isTraceLifeCycle() {
            return false;
        }

        @Override
        protected boolean logLifeCycleEvent() {
            return false;
        }

        @Override
        protected boolean isDelegateLifeCycleEventToSetUserVisible() {
            return false;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            //            mVM = (StockMatchHomeVM) getArguments().getSerializable(StockMatchHomeVM.class.getSimpleName());
            coverUrl = getArguments().getString("cover_url", "");
            matchId = getArguments().getString("match_id", "");

            SimpleDraweeView view = new SimpleDraweeView(inflater.getContext());
            GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
                    .setPlaceholderImage(getResources().getDrawable(R.mipmap.ic_focus_placeholder))
                    .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                    .setRoundingParams(new RoundingParams().setCornersRadius(dp2px(3)))
                    .build();
            view.setHierarchy(hierarchy);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            SimpleDraweeView imageView = (SimpleDraweeView) view;
            imageView.setImageURI(Uri.parse(coverUrl));
            //            v_setClick(view, v -> CMDParser.parse(mVM.matchId).call(getActivity()));
            v_setClick(view, v -> showActivity(this, an_StockMatchDetailPage(matchId)));
        }
    }

    /**
     * Created by yale on 15/7/20.
     * 投资首页的Fragment
     */
    public static final class InvestHomeFragment extends BaseMainFragment {

        private PublishSubject<Void> onExpandFoldableGallerySubject = PublishSubject.create();
        private Handler mHandler = new Handler();
        private List<FocusInfo> mCacheFocusList;
        private boolean mDataExpired = true;


        @Override
        protected int getCorrespondTabIndex() {
            return 2;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_invest_home, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            changeVisibleSection(TYPE_LOADING);
            v_setClick(mReloadSection, v -> fetchData(true));
            setOnSwipeRefreshListener(() -> fetchData(false));

            beginUpdateFundDetailTimer();

            FoldableGallery gallery = v_findView(mContentSection, R.id.gallery);
            v_setClick(mContentSection, R.id.layer_top, v -> expandGallery());
            v_setClick(mContentSection, R.id.btn_collapse, v -> {
                gallery.collapse(true);
                mHandler.postDelayed(() -> {
                    AdvanceNestedScrollView scrollView = v_findView(mContentSection, R.id.scrollView);
                    Scroller scroller = scrollView.getOffsetTopAndBottomScroller();
                    scroller.forceFinished(true);
                    scroller.startScroll(0, (int) scrollView.getTranslationY(), 0, -(int) scrollView.getTranslationY());
                    scrollView.invalidate();
                    if (isParentMainActivity()) {
                        View rootView = getActivity().findViewById(R.id.container_fragment);
                        ValueAnimator animator = ValueAnimator.ofInt(0, dp2px(56));
                        animator.addUpdateListener(animation -> {
                            int value = (int) animation.getAnimatedValue();
                            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rootView.getLayoutParams();
                            params.bottomMargin = value;
                            rootView.setLayoutParams(params);
                        });
                        animator.start();
                        updateRegistGuide(true);
                    }
                }, 400L);
            });

            AdvanceNestedScrollView scrollView = v_findView(mContentSection, R.id.scrollView);
            AdvanceSwipeRefreshLayout.class.cast(mRefreshLayout)
                    .setOnPreInterceptTouchEventDelegate(ev -> {
                        if (gallery.isExpand() && gallery.getChildCount() > 0) {
                            return true;
                        }

                        return scrollView.getScrollY() > 0;

                    });

            //            v_setBehavior(scrollView, new CoordinatorLayout.Behavior<AdvanceNestedScrollView>() {
            //                View topLayer = v_findView(mContentSection, R.id.layer_top);
            //
            //                @Override
            //                public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, AdvanceNestedScrollView child, View directTargetChild, View target, int nestedScrollAxes) {
            //                    return topLayer.getVisibility() == View.VISIBLE && target == child;
            //                }
            //
            //                @Override
            //                public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AdvanceNestedScrollView child, View target, int dx, int dy, int[] consumed) {
            //                    super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
            //                    if (dy < 0) {
            //                        if (target.getScrollY() == 0) {
            //                            child.setTranslationY(child.getTranslationY() - dy);
            //                            child.invalidate();
            //                            consumed[1] = dy;
            //                        }
            //                    } else if (dy > 0 && target.getTranslationY() > 0) {
            //                        int offset = Math.max(-dy, (int) -target.getTranslationY());
            //                        child.setTranslationY(child.getTranslationY() + offset);
            //                        child.invalidate();
            //                        consumed[1] = -offset;
            //                    }
            //                }
            //
            //                @Override
            //                public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AdvanceNestedScrollView child, View target) {
            //                    super.onStopNestedScroll(coordinatorLayout, child, target);
            //                    if (child.getTranslationY() > child.getMeasuredHeight() / 3) {
            //                        expandGallery();
            //                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDTouziIntroPic);
            //                    } else {
            //                        Scroller scroller = child.getOffsetTopAndBottomScroller();
            //                        scroller.forceFinished(true);
            //                        scroller.startScroll(0, (int) child.getTranslationY(), 0, -(int) child.getTranslationY());
            //                        child.invalidate();
            //                    }
            //                }
            //            });

            consumeEvent(Observable.merge(NotificationCenter.loginSubject, NotificationCenter.logoutSubject).debounce(300, TimeUnit.MILLISECONDS))
                    .onNextFinish(ignored -> {
                        mDataExpired = true;
                        if (getUserVisibleHint() && getView() != null) {
                            fetchData(false);
                        }
                    })
                    .done();

            consumeEvent(NotificationCenter.investedFundSubject)
                    .onNextFinish(ignored -> {
                        mDataExpired = true;
                        if (getView() != null) {
                            fetchData(false);
                            mDataExpired = false;
                        }
                    })
                    .done();

            consumeEvent(onExpandFoldableGallerySubject)
                    .setTag("expanded_gallery")
                    .onNextFinish(ignored -> {
                        if (getUserVisibleHint() && getView() != null) {
                            fetchData(false);
                        }
                    })
                    .done();

            fetchData(true);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mDataExpired = true;
        }

        @Override
        public void onUserVisibleHintChanged(boolean isVisibleToUser) {
            super.onUserVisibleHintChanged(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (mCacheFocusList != null) {
                    updateFoldableGallery(mCacheFocusList);
                }

                if (mDataExpired) {
                    boolean reload = mContentSection.getVisibility() != View.VISIBLE;
                    fetchData(reload);
                    mDataExpired = false;
                }
            }

            if (!isVisibleToUser) {
                mDataExpired = true;
            }
        }

        private void expandGallery() {
            AdvanceNestedScrollView scrollView = v_findView(mContentSection, R.id.scrollView);
            FoldableGallery gallery = v_findView(mContentSection, R.id.gallery);

            int extraY = dp2px(56);
            Scroller scroller = scrollView.getOffsetTopAndBottomScroller();
            scroller.forceFinished(true);
            scroller.startScroll(0, (int) scrollView.getTranslationY(), 0, scrollView.getMeasuredHeight() - (int) scrollView.getTranslationY() + extraY);
            scrollView.invalidate();
            gallery.expand(true);

            if (isParentMainActivity()) {
                View rootView = getActivity().findViewById(R.id.container_fragment);
                ValueAnimator animator = ValueAnimator.ofInt(dp2px(56), 0);
                animator.addUpdateListener(animation -> {
                    int value = (int) animation.getAnimatedValue();
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rootView.getLayoutParams();
                    params.bottomMargin = value;
                    rootView.setLayoutParams(params);
                });
                animator.start();
                updateRegistGuide(false);
            }
            onExpandFoldableGallerySubject.onNext(null);
        }

        @SuppressWarnings("unchecked")
        private void fetchData(boolean reload) {
            Observable<List<MResultsInfo>> observable = zipToList(CommonController.fetchInvestFocusList(reload), FundController.fetchRecommendFundList(reload));
            consumeEventMRListUpdateUI(observable, reload, FLAG_DISABLE_FORCE_SHOW_SWIPE_REFRESH_LAYOUT)
                    .setTag("reload_data")
                    .onNextSuccess(responses -> {
                        MResultsInfo<List<FocusInfo>> focusResponse = responses.get(0);
                        MResultsInfo<List<FundBrief>> fundsResponse = responses.get(1);
                        updateFoldableGallery(opt(focusResponse.data).or(Collections.emptyList()));
                        List<FundBrief> funds = opt(fundsResponse.data).or(Collections.emptyList());
                        updatePlatformSection(new PlatformInfo(DiscoverManager.getInstance().totalInfo));
                        updateFundListSection(funds);
                        updateBottomCells(DiscoverManager.getInstance().totalInfo, funds);
                        mCacheFocusList = opt(focusResponse.data).or(Collections.emptyList());
                    })
                    .done();
            mDataExpired = false;
        }

        private void updateFoldableGallery(List<FocusInfo> focusList) {
            FoldableGallery gallery = ViewExtension.<FoldableGallery>v_findView(mContentSection, R.id.gallery);
            if (gallery.isExpand() && gallery.getChildCount() > 0) {
                return;
            }

            Rect screenSize = getScreenSize(this);
            Stream.of(focusList)
                    .map(it -> it.imageUrl)
                    .filter(it -> !TextUtils.isEmpty(it))
                    .map(it -> ImageRequestBuilder.newBuilderWithSource(Uri.parse(it))
                            .setResizeOptions(new ResizeOptions(screenSize.width(), screenSize.height()))
                            .build())
                    .forEach(it -> Fresco.getImagePipeline().prefetchToDiskCache(it, null));

            List<Uri> downloadedImage = Stream.of(focusList)
                    .map(it -> it.imageUrl)
                    .filter(it -> FrescoHelper.isImageInCache(it))
                    .map(it -> Uri.parse(it))
                    .collect(Collectors.toList());

            if (downloadedImage.size() > 0) {
                FoldableGallery parent = v_findView(mContentSection, R.id.gallery);
                float draweeViewWidthPercent = (float) 325 / 375;
                int[] loadedImageCount = {0};
                Iterator<? extends Integer> indexIterator = Stream.range(0, downloadedImage.size()).getIterator();
                Stream.of(downloadedImage)
                        .forEach(it -> {
                            int index = indexIterator.next();
                            ScrollView scrollView;
                            if (index < parent.getChildCount()) {
                                scrollView = (ScrollView) parent.getChildAt(index);
                                FrameLayout wrapper = (FrameLayout) scrollView.getChildAt(0);
                                SimpleDraweeView draweeView = (SimpleDraweeView) wrapper.getChildAt(0);
                                draweeView.setController(Fresco.newDraweeControllerBuilder()
                                        .setOldController(draweeView.getController())
                                        .setControllerListener(new BaseControllerListener<ImageInfo>() {
                                            @Override
                                            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                                                super.onFinalImageSet(id, imageInfo, animatable);

                                                float ratio = (float) imageInfo.getWidth() / imageInfo.getHeight();
                                                ViewGroup.LayoutParams params = draweeView.getLayoutParams();
                                                params.width = (int) (screenSize.width() * draweeViewWidthPercent);
                                                params.height = (int) ((float) params.width / ratio);
                                                draweeView.setLayoutParams(params);
                                                if (index < 3) {
                                                    loadedImageCount[0]++;
                                                    if (loadedImageCount[0] >= Math.min(downloadedImage.size(), 3)) {
                                                        parent.collapse(false);
                                                        runOnUIThreadDelayed(() -> parent.collapse(false), 32L);
                                                    }
                                                }
                                            }
                                        })
                                        .setImageRequest(ImageRequest.fromUri(it))
                                        .build());
                            } else {
                                scrollView = new ScrollView(parent.getContext());
                                scrollView.setVerticalScrollBarEnabled(false);

                                FrameLayout wrapper = new FrameLayout(parent.getContext());

                                GenericDraweeHierarchy hierarchy = GenericDraweeHierarchyBuilder.newInstance(getResources())
                                        .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                                        .setFadeDuration(0)
                                        .build();

                                SimpleDraweeView draweeView = new SimpleDraweeView(parent.getContext(), hierarchy);
                                draweeView.setController(Fresco.newDraweeControllerBuilder()
                                        .setOldController(draweeView.getController())
                                        .setControllerListener(new BaseControllerListener<ImageInfo>() {
                                            @Override
                                            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                                                super.onFinalImageSet(id, imageInfo, animatable);

                                                float ratio = (float) imageInfo.getWidth() / imageInfo.getHeight();
                                                ViewGroup.LayoutParams params = draweeView.getLayoutParams();
                                                params.width = (int) (screenSize.width() * draweeViewWidthPercent);
                                                params.height = (int) ((float) params.width / ratio);
                                                draweeView.setLayoutParams(params);
                                                if (index < 3) {
                                                    loadedImageCount[0]++;
                                                    if (loadedImageCount[0] >= Math.min(downloadedImage.size(), 3)) {
                                                        parent.collapse(false);
                                                        runOnUIThreadDelayed(() -> parent.collapse(false), 32L);
                                                    }
                                                }
                                            }
                                        })
                                        .setImageRequest(ImageRequest.fromUri(it))
                                        .build());
                                wrapper.addView(draweeView, new FrameLayout.LayoutParams(-2, -2));

                                scrollView.addView(wrapper);
                                parent.addView(scrollView, new FoldableGallery.LayoutParams(-2, -2));
                            }
                            if (index < 3) {
                                v_setVisible(scrollView);
                            } else {
                                v_setInvisible(scrollView);
                            }
                        });
                v_setVisible(mContentSection, R.id.layer_top);
            } else {
                v_setGone(mContentSection, R.id.layer_top);
            }
        }

        private void updatePlatformSection(PlatformInfo platformInfo) {
            v_setText(mContentSection, R.id.label_platform, platformInfo.content);
            if (BuildConfig.DEBUG) {
                v_setClick(v_findView(mContentSection, R.id.label_platform), v -> {
                    FoldableGallery gallery = v_findView(mContentSection, R.id.gallery);
                    gallery.collapse(false);
                });
            }
        }


        @SuppressWarnings("Convert2MethodRef")
        private void updateFundListSection(List<FundBrief> funds) {
            funds = Stream.of(funds)
                    .filter(it -> {
                        return Fund_Status.beforeLockIn(it.status) && Fund_Status.afterReview(it.status) && !Fund_Status.isUnknown(it.status);
                    })
                    .collect(Collectors.toList());


            View noFundCard = v_findView(mContentSection, R.id.card_no_fund);
            noFundCard.setVisibility(funds.isEmpty() ? View.VISIBLE : View.GONE);
            noFundCard.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0xFFE5E5E5, dp2px(4))));

            LinearLayout listContainer = v_findView(mContentSection, R.id.list_fund);
            FundCardViewHelper.resetFundCardListViewWithFundBrief(listContainer, funds, 0);

            //            Context ctx = getActivity();
            //            listContainer.removeAllViewsInLayout();
            //            Stream.of(funds)
            //                    .map(it -> FundCardViewHelper.createViewModel(it))
            //                    .map(it -> {
            //                        View cell = FundCardViewHelper.createCell(ctx, listContainer);
            //                        FundCardViewHelper.afterCreateCell(cell, it);
            //                        FundCardViewHelper.updateCell(cell, it, false);
            //                        return cell;
            //                    })
            //                    .forEach(it -> listContainer.addView(it));
        }

        private static int InvestHome_Fresh_Booking_Time_Base = 500;
        private static int Const_Delay_Time = 10 * 1000;
        private int delayTime = 2 * 1000;

        private void BookingFreshData(FundCardViewModel vm) {

            Observable<MResultsInfo<List<FundBrief>>> observable = FundController.fetchRecommendFundList(false)
                    .delaySubscription(delayTime, TimeUnit.MILLISECONDS)
                    .flatMap(it -> {
                        long countDownTime = vm.raw.beginFundraisingTime + 15 - SecondUtil.currentSecond();
                        long seconds = Math.max(countDownTime, 0L);
                        return Observable.just(it).delay(seconds, TimeUnit.SECONDS);
                    });

            consumeEvent(observable)
                    .setTag("Fresh_List" + vm.raw.index)
                    .setPolicy(ConsumeEventChain.POLICY.IGNORED)
                    .onNextFinish(response -> {
                        delayTime = (delayTime <= Const_Delay_Time) ? Const_Delay_Time : delayTime * 2;
                        if (response.isSuccess) {
                            updateFundListSection(response.data);
                        }
                    })
                    .done();
        }


        private void beginUpdateFundDetailTimer() {

            consumeEvent(Observable.interval(InvestHome_Fresh_Booking_Time_Base, TimeUnit.MILLISECONDS))
                    .setTag("Fresh_Booking_Time")
                    .setPolicy(ConsumeEventChain.POLICY.IGNORED)
                    .onNextFinish(ignored -> {

                        LinearLayout listContainer = v_findView(mContentSection, R.id.list_fund);
                        FundCardViewHelper.updateFundCardListView(listContainer, 0, (cell, vm) -> {
                            boolean isStatusBook = (vm.fundStatus == Fund_Status.Booking);
                            boolean fundHasBegin = vm.raw.beginFundraisingTime <= SecondUtil.currentSecond();
                            boolean beginTimeCorrect = vm.raw.beginFundraisingTime + 20 * 60 * 1000 >= SecondUtil.currentSecond();
                            if (isStatusBook && fundHasBegin && beginTimeCorrect) {
                                BookingFreshData(vm);
                            }
                        });

                        //                        ViewGroupExtension.v_forEach(listContainer, (pos, cell) -> {
                        //                            Object tag = cell.getTag();
                        //                            if (tag != null && tag instanceof FundCardViewHelper.FundCardViewModel) {
                        //                                FundCardViewHelper.FundCardViewModel vm = (FundCardViewHelper.FundCardViewModel) tag;
                        //
                        //                                boolean isStatusBook = (vm.fundStatus == FundBrief.Fund_Status.Booking);
                        //
                        //                                if (isStatusBook) {
                        //                                    vm.update();
                        //                                    FundCardViewHelper.updateCell(cell);
                        //
                        //                                    long temp = SecondUtil.currentSecond();
                        //                                    boolean fundHasBegin = vm.raw.beginFundraisingTime <= SecondUtil.currentSecond();
                        //                                    boolean beginTimeCorrect = vm.raw.beginFundraisingTime + 20 * 60 * 1000 >= SecondUtil.currentSecond();
                        //                                    if (isStatusBook && fundHasBegin && beginTimeCorrect) {
                        //                                        BookingFreshData(vm);
                        //                                    }
                        //                                }
                        //                            }
                        //                        });
                    })
                    .done();
        }

        private void updateBottomCells(TotalInfo totalInfo, List<FundBrief> funds) {

            BasicCell moreFundCell = v_findView(mContentSection, R.id.cell_more_fund);
            String moreFundCellTitleText = safeGet(() -> String.format("全部组合(%d)", funds.size()), "全部组合");
            v_setText(moreFundCell.getTitleLabel(), moreFundCellTitleText);
            int flags = AllFundFragment.FLAG_ALLOW_SWIPE_REFRESH;
            v_setClick(moreFundCell, v -> showActivity(this, an_MoreFundPage(funds, flags)));

            {
                BasicCell moreTraderCell = v_findView(mContentSection, R.id.cell_more_trader);
                String moreTraderCellTitle = safeGet(() -> String.format("全部操盘手(%d)", DiscoverManager.getInstance().totalTrader), "全部操盘手");
                v_setText(moreTraderCell.getTitleLabel(), moreTraderCellTitle);
                v_setClick(moreTraderCell, v -> showActivity(this, an_AllTraderPage()));

                SimpleDraweeView focusTraderView = v_findView(mContentSection, R.id.cell_focus_trader);
                String traderURL = safeGet(() -> DiscoverManager.getInstance().focusTrader.getPhotoUrl(), "");
                v_setImageUri(focusTraderView, traderURL);
                v_setVisibility(focusTraderView, TextUtils.isEmpty(traderURL) ? View.GONE : View.VISIBLE);
            }


            {
                BasicCell moreTalentCell = v_findView(mContentSection, R.id.cell_more_talent);
                String moreTalentCellTitle = safeGet(() -> String.format("全部牛人(%d)", DiscoverManager.getInstance().totalTalent), "全部牛人");
                v_setText(moreTalentCell.getTitleLabel(), moreTalentCellTitle);
                v_setClick(moreTalentCell, v -> showActivity(this, an_AllTalentPage()));

                SimpleDraweeView focusTalenView = v_findView(mContentSection, R.id.cell_focus_talent);
                String talentURL = safeGet(() -> DiscoverManager.getInstance().focusTalent.getPhotoUrl(), "");
                v_setImageUri(focusTalenView, talentURL);
                v_setVisibility(focusTalenView, TextUtils.isEmpty(talentURL) ? View.GONE : View.VISIBLE);
            }
        }
    }

    public static class PlatformInfo implements Serializable {
        public transient final CharSequence content;

        public PlatformInfo(TotalInfo totalInfo) {
            content = safeGet(() -> concat("操盘侠已累计为 " + totalInfo.investCount + " 人次投资管理金额", setFontSize(formatMoney(totalInfo.investMoney, false, 0) + " 元", sp2px(24))))
                    .def(concat("操盘侠已累计为 " + PlaceHolder.NULL_VALUE + " 人次投资管理金额", setFontSize("- 元", sp2px(24))))
                    .get();
        }
    }

    public static class ConversationListFragment extends BaseMainFragment {

        private SwipeRefreshLayout mRefreshControl;
        private RecyclerView mRecyclerView;
        private boolean mDataSetChanged = false;

        @Override
        protected int getCorrespondTabIndex() {
            return 3;
        }

        private BaseManager.OnKeyListener mListener = (key, data) -> {
            if (key.equals(SessionManager.Key_SessionManager_SessionList))
                if (getUserVisibleHint() && getView() != null) {
                    List<MessageSession> snsSessionList = SessionManager.getInstance().getSnsSessionList();
                    List<MessageSession> sessionList = SessionManager.getInstance().getSessionList();
                    handleConversationListChangedEvent(snsSessionList, sessionList);
                } else {
                    mDataSetChanged = true;
                }
        };

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_conversation_list, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // bind child views
            mRefreshControl = (SwipeRefreshLayout) mContentSection;
            mRecyclerView = v_findView(mContentSection, R.id.recyclerView);

            // initialize child views
            v_setClick(mReloadSection, v -> performRefreshSessionList(false));
            mRefreshControl.setOnRefreshListener(() -> performRefreshSessionList(false));
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

            addHorizontalSepLine(mRecyclerView, new Rect(dp2px(60), 0, 0, 0));
            List<MessageSession> snsSessionList = SessionManager.getInstance().getSnsSessionList();
            List<MessageSession> sessionList = SessionManager.getInstance().getSessionList();
            handleConversationListChangedEvent(snsSessionList, sessionList);

            SessionManager.getInstance().addListener(SessionManager.Key_SessionManager_SessionList, mListener);

            performRefreshSessionList(false);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            SessionManager.getInstance().removeListener(mListener);
            mDataSetChanged = true;
        }

        @Override
        public void onUserVisibleHintChanged(boolean isVisibleToUser) {
            super.onUserVisibleHintChanged(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (mDataSetChanged) {
                    mDataSetChanged = false;
                    performRefreshSessionList(false);
                }

                consumeEvent(ChatFragments.sReceivedNewMessageSubject)
                        .addToVisible()
                        .onNextFinish(data -> performRefreshSessionList(false))
                        .done();
            } else {
                mDataSetChanged = true;
            }
        }

        private void performRefreshSessionList(boolean reload) {
            consumeEventMRUpdateUI(ChatController.refreshSessionList(), reload).done();
        }

        private RecyclerView.ItemDecoration mSpacingDecoration;

        private void handleConversationListChangedEvent(List<MessageSession> snsSessionList, List<MessageSession> sessionList) {
            mRefreshControl.setRefreshing(false);
            v_setGone(mLoadingSection);
            v_setGone(mReloadSection);
            v_setVisible(mContentSection);
            List<MessageSession> mergedList = new LinkedList<>();
            if (snsSessionList != null) {
                mergedList.addAll(snsSessionList);
            }
            if (sessionList != null) {
                mergedList.addAll(sessionList);
            }
            if (!mergedList.isEmpty()) {
                List<ConversationListCellVM> items = Stream.of(mergedList).map(ConversationListCellVM::new).collect(Collectors.toList());
                if (mRecyclerView.getAdapter() != null) {
                    SimpleRecyclerViewAdapter<ConversationListCellVM> adapter = getSimpleAdapter(mRecyclerView);
                    adapter.resetItems(items);
                } else {
                    SimpleRecyclerViewAdapter<ConversationListCellVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                            .onCreateItemView(R.layout.cell_chat_conversation)
                            .onCreateViewHolder(builder -> onCreateViewHolder(builder))
                            .onViewHolderCreated((ad, holder) -> {
                                onViewHolderCreated(this, ad, holder);
                            })
                            .create();
                    mRecyclerView.setAdapter(adapter);
                }
                if (mSpacingDecoration != null) {
                    mRecyclerView.removeItemDecoration(mSpacingDecoration);
                }
                int barSessionCount = snsSessionList == null ? 0 : snsSessionList.size();
                mSpacingDecoration = new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        super.getItemOffsets(outRect, view, parent, state);
                        int itemPos = parent.getChildAdapterPosition(view);
                        boolean hasBarSession = barSessionCount > 0;
                        boolean isFirstNormalSession = itemPos == barSessionCount;
                        outRect.top = (hasBarSession && isFirstNormalSession) ? dp2px(16) : 0;
                    }
                };
                mRecyclerView.addItemDecoration(mSpacingDecoration);
            }
        }
    }

    private static SimpleViewHolder<ConversationListCellVM> onCreateViewHolder(SimpleViewHolder.Builder<ConversationListCellVM> builder) {
        builder.bindChildWithTag("iconImage", R.id.img_icon)
                .bindChildWithTag("titleLabel", R.id.label_title)
                .bindChildWithTag("contentLabel", R.id.label_content)
                .bindChildWithTag("dateLabel", R.id.label_date)
                .bindChildWithTag("countLabel", R.id.label_count)
                .configureView((item, pos) -> {
                    configureView(builder, item);
                });
        return builder.create();
    }

    private static void onViewHolderCreated(Fragment fragment, SimpleRecyclerViewAdapter<ConversationListCellVM> ad, SimpleViewHolder<ConversationListCellVM> holder) {
        v_setClick(holder.itemView, v -> {
            ConversationListCellVM item = ad.getItem(holder.getAdapterPosition());
            item.openDetailPage(fragment);
            switch (holder.getAdapterPosition()) {
                case 0:
                    UmengUtil.stat_click_event(UmengUtil.eEvENTIDTopicDetailEnter1);
                    break;
                case 1:
                    UmengUtil.stat_click_event(UmengUtil.eEvENTIDTopicDetailEnter2);
                    break;
                case 2:
                    UmengUtil.stat_click_event(UmengUtil.eEvENTIDTopicDetailEnter3);
                    break;
                case 3:
                    UmengUtil.stat_click_event(UmengUtil.eEvENTIDTopicDetailEnter4);
                    break;
                case 4:
                    UmengUtil.stat_click_event(UmengUtil.eEvENTIDTopicDetailEnter5);
                    break;
                case 5:
                    UmengUtil.stat_click_event(UmengUtil.eEvENTIDTopicDetailEnter6);
                    break;
                case 6:
                    UmengUtil.stat_click_event(UmengUtil.eEvENTIDTopicDetailEnter7);
                    break;
                case 7:
                    UmengUtil.stat_click_event(UmengUtil.eEvENTIDTopicDetailEnter8);
                    break;
                case 8:
                    UmengUtil.stat_click_event(UmengUtil.eEvENTIDTopicDetailEnter9);
                    break;
                case 9:
                    UmengUtil.stat_click_event(UmengUtil.eEvENTIDTopicDetailEnter10);
                    break;
            }

        });
        v_setClick(v_findView(holder.itemView, R.id.label_count), v -> {
            ConversationListCellVM vm = ad.getItem(holder.getAdapterPosition());
            safeCall(() -> {
                SessionManager.getInstance().smartClearRedPoint(vm.raw);
                vm.unreadCount = 0;
            });
            ad.notifyItemChanged(holder.getAdapterPosition());
        });
    }

    private static void configureView(SimpleViewHolder.Builder<ConversationListCellVM> builder, ConversationListCellVM item) {
        v_setImageUri(builder.getChildWithTag("iconImage"), item.iconURL);
        v_setText(builder.getChildWithTag("titleLabel"), item.title);
        v_setText(builder.getChildWithTag("contentLabel"), item.content);
        v_setText(builder.getChildWithTag("dateLabel"), item.isBarSession ? item.todayNewCount : item.time);
        v_setText(builder.getChildWithTag("countLabel"), item.unreadCount > 99 ? "99+" : String.valueOf(item.unreadCount));
        TextView countLabel = builder.getChildWithTag("countLabel");
        boolean hasDot = item.unreadCount > 0 || (item.hintCount > 0 && item.isBarSession);
        if (hasDot) {
            if (item.isGroupSession) {
                v_setText(countLabel, "");
                countLabel.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(RED_COLOR, dp2px(5))));
                countLabel.setMinWidth(dp2px(10));
                countLabel.setMinHeight(dp2px(10));
                v_updateLayoutParams(countLabel, params -> {
                    params.width = dp2px(10);
                    params.height = dp2px(10);
                });
            } else if (item.isBarSession) {
                if (item.unreadCount > 0) {
                    v_setText(countLabel, item.unreadCount > 99 ? "99+" : String.valueOf(item.unreadCount));
                    countLabel.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(RED_COLOR, dp2px(9))));
                    countLabel.setMinWidth(dp2px(18));
                    countLabel.setMinHeight(dp2px(18));
                    v_updateLayoutParams(countLabel, params -> {
                        params.width = -2;
                        params.height = dp2px(18);
                    });
                } else {
                    v_setText(countLabel, "");
                    countLabel.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(RED_COLOR, dp2px(5))));
                    countLabel.setMinWidth(dp2px(10));
                    countLabel.setMinHeight(dp2px(10));
                    v_updateLayoutParams(countLabel, params -> {
                        params.width = dp2px(10);
                        params.height = dp2px(10);
                    });
                }
            } else {
                v_setText(countLabel, item.unreadCount > 99 ? "99+" : String.valueOf(item.unreadCount));
                countLabel.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(RED_COLOR, dp2px(9))));
                countLabel.setMinWidth(dp2px(18));
                countLabel.setMinHeight(dp2px(18));
                v_updateLayoutParams(countLabel, params -> {
                    params.width = -2;
                    params.height = dp2px(18);
                });
            }
        }
        v_setVisibility(countLabel, hasDot ? View.VISIBLE : View.INVISIBLE);
        v_reviseTouchArea(countLabel);
    }

    public static class ConversationGroupFragment extends SimpleFragment {

        private int mMessageType;
        private String mLinkID;
        private String mSessionID;

        private SwipeRefreshLayout mRefreshControl;
        private RecyclerView mRecyclerView;
        private boolean mDataSetChanged = false;

        private BaseManager.OnKeyListener mListener = (key, data) -> {
            if (key.equals(SessionManager.Key_SessionManager_SessionList))
                if (getUserVisibleHint() && getView() != null) {
                    fetchData(false);
                } else {
                    mDataSetChanged = true;
                }
        };

        public ConversationGroupFragment init(int messageType, String linkID, String sessionID) {
            Bundle arguments = new Bundle();
            arguments.putInt(CommonProxyActivity.KEY_MESSAGE_TYPE_INT, messageType);
            arguments.putString(CommonProxyActivity.KEY_LINK_ID_STRING, linkID);
            arguments.putString(CommonProxyActivity.KEY_SESSION_ID_STRING, sessionID);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mMessageType = getArguments().getInt(CommonProxyActivity.KEY_MESSAGE_TYPE_INT);
            mLinkID = getArguments().getString(CommonProxyActivity.KEY_LINK_ID_STRING);
            mSessionID = getArguments().getString(CommonProxyActivity.KEY_SESSION_ID_STRING);
            return inflater.inflate(R.layout.frag_conversation_list, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, YELLOW_COLOR);
            updateTitle("");
            v_updateLayoutParams(v_findView(this, R.id.toolbarTitle), Toolbar.LayoutParams.class, params -> {
                params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            });

            setupBackButton(this, findToolbar(this), R.drawable.ic_arrow_left_dark);

            // bind child views
            mRefreshControl = (SwipeRefreshLayout) mContentSection;
            mRecyclerView = v_findView(mContentSection, R.id.recyclerView);

            // initialize child views
            v_setClick(mReloadSection, v -> fetchData(true));
            mRefreshControl.setOnRefreshListener(() -> fetchData(false));
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

            addHorizontalSepLine(mRecyclerView, new Rect(dp2px(60), 0, 0, 0));

            fetchData(true);

            SessionManager.getInstance().addListener(SessionManager.Key_SessionManager_SessionList, mListener);
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (mDataSetChanged) {
                mDataSetChanged = false;
                fetchData(false);
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            SessionManager.getInstance().removeListener(mListener);
            mDataSetChanged = true;
        }

        private void fetchData(boolean reload) {
            consumeEventMRUpdateUI(ChatController.getMessageManager(mMessageType, mLinkID, mSessionID), reload)
                    .onNextSuccess(response -> {
                        SessionGroup group = (SessionGroup) response.data.getSession();
                        updateTitle(safeGet(() -> group.title, ""));
                        handleConversationListChangedEvent(group.getSessionList());
                    })
                    .done();
        }

        private void handleConversationListChangedEvent(List<MessageSession> sessionList) {
            mRefreshControl.setRefreshing(false);
            v_setGone(mLoadingSection);
            v_setGone(mReloadSection);
            v_setVisible(mContentSection);
            if (!sessionList.isEmpty()) {
                List<ConversationListCellVM> items = Stream.of(sessionList).map(ConversationListCellVM::new).collect(Collectors.toList());
                if (mRecyclerView.getAdapter() != null) {
                    SimpleRecyclerViewAdapter<ConversationListCellVM> adapter = getSimpleAdapter(mRecyclerView);
                    adapter.resetItems(items);
                } else {
                    SimpleRecyclerViewAdapter<ConversationListCellVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                            .onCreateItemView(R.layout.cell_chat_conversation)
                            .onCreateViewHolder(builder -> onCreateViewHolder(builder))
                            .onViewHolderCreated((ad, holder) -> {
                                onViewHolderCreated(this, ad, holder);
                            })
                            .create();
                    mRecyclerView.setAdapter(adapter);
                }
            }
        }
    }

    /**
     * Created by yale on 15/9/15.
     */
    public static class MineFragment extends BaseMainFragment {

        private SwipeRefreshLayout mRefreshControl;
        private View mLoginSection;
        private View mLogoutSection;

        private boolean mHasData = false;
        private boolean mDataExpired = false;

        @Override
        protected int getCorrespondTabIndex() {
            return 4;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_mine, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // bind child views
            mLoginSection = v_findView(this, R.id.section_login);
            mLogoutSection = v_findView(this, R.id.section_logout);
            mRefreshControl = (SwipeRefreshLayout) mLoginSection;
            v_setGone(mLoginSection);
            v_setGone(mLogoutSection);
            changeVisibleSection(TYPE_LOADING);

            v_setClick(this, R.id.section_reload, v -> fetchLoginRequiredData(true));
            v_setClick(this, R.id.menu_more, v -> showActivity(this, an_AboutPage()));
            v_setOnRefreshing(mRefreshControl, () -> fetchLoginRequiredData(false));

            v_setClick(mLoginSection, R.id.section_header, v -> showActivity(this, an_UserDetailPage(null)));
            v_setClick(mLoginSection, R.id.cell_cash_journal, v -> showActivity(this, an_CashJournalPage(Optional.of(this))));
            v_setClick(mLoginSection, R.id.cell_award, v -> {
                safeCall(() -> {
                    RedPoint point = FortuneManager.getInstance().bountyRedPoint;
                    if (point.number > 0) {
                        point.clear();
                        updateRedPoint();
                    }
                });
                showActivity(this, an_AwardHomePage(opt(this)));
                UmengUtil.stat_enter_award_home_from_mine_page(getActivity(), opt(this));
            });
            v_setClick(mLoginSection, R.id.cell_managed_groups, v -> showActivity(this, an_ManagedGroupsPage()));
            v_setClick(mLoginSection, R.id.cell_dev, v -> showActivity(this, an_DevModePage()));
            v_findView(mLoginSection, R.id.cell_dev).setVisibility(MyConfig.isDevModeEnable() ? View.VISIBLE : View.GONE);
            v_setClick(mLoginSection, R.id.cell_score, v -> {
                safeCall(() -> {
                    RedPoint point = ScoreManager.getInstance().scoreRedPoint;
                    if (point.number > 0) {
                        point.clear();
                        updateRedPoint();
                    }
                });
                showActivity(this, an_ScoreHomePage());
            });
            v_setClick(mLoginSection, R.id.cell_coupon, v -> {
                safeCall(() -> {
                    RedPoint point = FortuneManager.getInstance().couponRedPoint;
                    if (point.number > 0) {
                        point.clear();
                        updateRedPoint();
                    }
                });
                showActivity(this, an_CouponListPage());
                UmengUtil.stat_click_event(eEvENTIDInvestExchangeCoupons);
            });
            v_setClick(mLoginSection, R.id.cell_feedback, v -> {
                showActivity(this, an_FeedbackPage(Optional.of(this)));
            });

            v_setClick(mLogoutSection, R.id.section_header, v -> showActivity(this, an_LoginPage()));
            v_setClick(mLogoutSection, R.id.cell_dev, v -> showActivity(this, an_DevModePage()));
            v_findView(mLogoutSection, R.id.cell_dev).setVisibility(MyConfig.isDevModeEnable() ? View.VISIBLE : View.GONE);


            Observable<?> observable = Observable.merge(NotificationCenter.loginSubject,
                    NotificationCenter.userInfoChangedSubject,
                    NotificationCenter.cashChangedSubject,
                    NotificationCenter.simulationAccountChangedSubject,
                    NotificationCenter.scoreChangedSubject)
                    .debounce(100L, TimeUnit.MILLISECONDS);
            consumeEvent(observable)
                    .setTag("user_info_changed")
                    .onNextFinish(data -> {
                        mDataExpired = true;
                        if (getView() != null && getUserVisibleHint()) {
                            updateData();
                        }
                    })
                    .done();

            consumeEvent(NotificationCenter.logoutSubject)
                    .setTag("user_logout")
                    .onNextFinish(data -> {
                        mDataExpired = true;
                        if (getView() != null && getUserVisibleHint()) {
                            updateData();
                        }
                    })
                    .done();

            if (getUserVisibleHint())
                setUserVisibleHint(true);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mHasData = false;
            mDataExpired = true;
        }

        @Override
        public void onUserVisibleHintChanged(boolean isVisibleToUser) {
            super.onUserVisibleHintChanged(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                updateData();
            }

            if (!isVisibleToUser && MineManager.getInstance().isLoginOK()) {
                mDataExpired = true;
            }
        }

        private void updateData() {
            if (!mHasData || mDataExpired) {
                boolean isLogin = MineManager.getInstance().isLoginOK();
                if (isLogin) {
                    boolean isReload = !v_isVisible(mLoginSection);
                    fetchLoginRequiredData(isReload);
                    v_setVisibility(mLoginSection, R.id.cell_dev, MyConfig.isDevModeEnable() ? View.VISIBLE : View.GONE);
                } else {
                    boolean isReload = v_isVisible(mLoginSection);
                    fetchedLogoutRequiredData(isReload);
                }
            }
            updateRedPoint();
            showHighlightRewardCellWithDimBgIfNeeded();
            mDataExpired = false;
        }

        View mTopView;

        private void showHighlightRewardCellWithDimBgIfNeeded() {
            if (mLoginSection == null || !v_isVisible(mLoginSection) || PersistentObjectUtil.readHasShowHomeAwardGuide()) {
                return;
            }

            Activity activity = getActivity();

            PersistentObjectUtil.writeHasShowHomeAwardGuide(true);
            View couponCell = v_findView(this, R.id.cell_coupon);
            v_getSizePreDraw(couponCell, true, size -> {
                int[] location = new int[2];
                couponCell.getLocationInWindow(location);
                Rect cellRect = new Rect(location[0], location[1], location[0] + size.first, location[1] + size.second);

                Action0 removeTopViewFunc = () -> {
                    if (mTopView != null) {
                        ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
                        viewGroup.removeView(mTopView);
                    }
                    mTopView = null;
                };

                removeTopViewFunc.call();

                ViewGroup parent = (ViewGroup) activity.getWindow().getDecorView();
                FrameLayout container = new FrameLayout(activity);
                container.setBackgroundColor(0);
                v_setClick(container, v -> {
                    removeTopViewFunc.call();
                    RedPoint point = FortuneManager.getInstance().couponRedPoint;
                    if (point != null) {
                        point.clear();
                        updateRedPoint();
                    }
                    showActivity(this, an_CouponListPage());
                });

                {
                    View view = new View(activity);
                    view.setBackgroundColor(0x80000000);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, cellRect.top);
                    container.addView(view, params);
                    v_setClick(view, removeTopViewFunc);
                }


                {
                    View view = new View(activity);
                    view.setBackgroundColor(0x80000000);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -1);
                    params.topMargin = cellRect.bottom;
                    container.addView(view, params);
                    v_setClick(view, removeTopViewFunc);
                }

                {
                    ImageView imageView = new ImageView(activity);
                    imageView.setImageResource(R.mipmap.hint_coupon);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, -2);
                    params.bottomMargin = parent.getHeight() - cellRect.top + dp2px(10);
                    params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    container.addView(imageView, params);
                }

                // add container to window
                {
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -1);
                    parent.addView(container, params);
                    mTopView = container;
                }
            });
        }


        @SuppressWarnings("deprecation")
        private void updateRedPoint() {
            {
                RedPoint redPoint = FortuneManager.getInstance().bountyRedPoint;
                String intro = opt(redPoint).let(it -> it.text).or("");
                int count = opt(redPoint).let(it -> it.number).or(0);

                BasicCell awardCell = v_findView(this, R.id.cell_award);
                TextView awardHintLabel = awardCell.getExtraTitleLabel();
                TextView redPointLabel = awardCell.getRedPointLabel();
                awardHintLabel.setText(intro);
                redPointLabel.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0xFFEC1919, dp2px(8))));
                redPointLabel.setText(String.valueOf(count));
                redPointLabel.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                FortuneRedPointTabSubject.onNext(count > 0);
            }

            {
                RedPoint redPoint = ScoreManager.getInstance().scoreRedPoint;
                int count = safeGet(() -> redPoint.number, 0);

                BasicCell scoreCell = v_findView(this, R.id.cell_score);
                TextView awardHintLabel = scoreCell.getExtraTitleLabel();
                TextView redPointLabel = scoreCell.getRedPointLabel();
                awardHintLabel.setText(safeGet(() -> redPoint.text, ""));
                redPointLabel.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0xFFEC1919, dp2px(8))));
                redPointLabel.setText(String.valueOf(count));
                //                redPointLabel.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                redPointLabel.setVisibility(View.GONE);
                ScoreRedPointTabSubject.onNext(count > 0);
            }

            {
                RedPoint redPoint = FortuneManager.getInstance().couponRedPoint;
                int count = safeGet(() -> redPoint.number, 0);
                BasicCell couponCell = v_findView(this, R.id.cell_coupon);
                String text = safeGet(() -> redPoint.text, "");
                TextView extraTitleLabel = couponCell.getExtraTitleLabel();
                extraTitleLabel.setCompoundDrawablePadding(dp2px(2));
                v_setText(extraTitleLabel, text);
                if (count > 0) {
                    Drawable redPointDrawable = new ShapeDrawable(new RoundCornerShape(RED_COLOR, dp2px(3)));
                    redPointDrawable.setBounds(0, 0, dp2px(6), dp2px(6));
                    extraTitleLabel.setCompoundDrawables(null, null, redPointDrawable, null);
                } else {
                    extraTitleLabel.setCompoundDrawables(null, null, null, null);
                }
            }
        }

        private void fetchLoginRequiredData(boolean reload) {
            if (reload) {
                v_setGone(mLogoutSection);
                v_setGone(mLoginSection);
                changeVisibleSection(TYPE_LOADING);
            } else {
                mRefreshControl.setRefreshing(true);
            }

            Observable<List<MResultsInfo>> observable = zipToList(UserController.refreshUserInfo(reload),
                    CashController.refreshAccount(reload),
                    FundController.fetchMyFundList(reload),
                    StockController.fetchSimulationAccount(reload),
                    CommonController.refreshRedPoint(reload, ScoreManager.getInstance().scoreRedPoint),
                    CommonController.refreshRedPoint(reload, FortuneManager.getInstance().couponRedPoint))
                    .map(it -> it.subList(0, 3));
            consumeEventMRList(observable)
                    .setTag("fetch_info")
                    .onNextFinish(responses -> {
                        if (isSuccess(responses)) {
                            CashController.refreshBankCard(reload, Money_Type.CN);
                            CashController.refreshBankCard(reload, Money_Type.HK);

                            FundFamily cnAccount = FortuneManager.getInstance().cnAccount;
                            FundFamily hkAccount = FortuneManager.getInstance().hkAccount;
                            SimulationAccount simulationAccount = SimulationStockManager.getInstance().getAccount();
                            List<FundBrief> fundList = TraderManager.getInstance().getFundList();

                            updateLoginSection(cnAccount, hkAccount, simulationAccount, fundList);
                            updateRedPoint();
                            v_setVisible(mLoginSection);
                            v_setGone(mLogoutSection);
                            changeVisibleSection(TYPE_CONTENT);
                            showHighlightRewardCellWithDimBgIfNeeded();
                            mHasData = true;
                        } else {
                            setReloadSectionTips(getErrorMessage(responses));
                            changeVisibleSection(TYPE_RELOAD);
                        }
                        mRefreshControl.setRefreshing(false);
                    })
                    .done();
        }

        private void fetchedLogoutRequiredData(boolean reload) {
            if (reload) {
                v_setGone(mLoginSection);
                v_setGone(mLogoutSection);
                changeVisibleSection(TYPE_LOADING);
            } else {
                setSwipeRefreshing(true);
            }

            consumeEventMR(CommonController.fetchQuestionList(reload))
                    .setTag("fetch_info")
                    .onNextFinish(response -> {
                        LinearLayout qaSection = v_findView(mLogoutSection, R.id.section_qa);
                        qaSection.removeAllViewsInLayout();
                        if (isSuccess(response)) {
                            Stream.of(opt(response.data).or(new JsonArray()))
                                    .map(JsonElement::getAsJsonObject)
                                    .map(element -> {
                                        QACell cell = new QACell(getActivity());
                                        cell.setQuestion(element.get("name").getAsString());
                                        cell.setAnswer(element.get("content").getAsString());
                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
                                        params.topMargin = dp2px(16);
                                        cell.setLayoutParams(params);
                                        return cell;
                                    })
                                    .forEach(qaSection::addView);
                            mHasData = true;
                        }
                        v_setGone(mLoginSection);
                        v_setVisible(mLogoutSection);
                        changeVisibleSection(TYPE_CONTENT);
                        setSwipeRefreshing(false);
                    })
                    .done();
        }

        private void updateLoginSection(FundFamily cnAccount, FundFamily hkAccount, SimulationAccount simulationAccount, List<FundBrief> fundList) {
            Mine mine = MineManager.getInstance().getmMe();

            //update headerSection
            {
                View headerSection = v_findView(this, R.id.section_header);
                v_setImageUri(headerSection, R.id.img_avatar, mine.getPhotoUrl());
                TextView nameLabel = v_findView(headerSection, R.id.label_name);
//                v_setImageResource(headerSection, R.id.img_user_type, (mine.type == User_Type.Trader) ? R.mipmap.ic_mark_trader : (mine.type == User_Type.Talent) ? R.mipmap.ic_mark_talent : 0);
                int rightDrawableResIS = (mine.type == User_Type.Trader) ? R.mipmap.ic_mark_trader : (mine.type == User_Type.Talent) ? R.mipmap.ic_mark_talent : 0;
                nameLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, rightDrawableResIS, 0);
                nameLabel.setCompoundDrawablePadding(rightDrawableResIS == 0 ? 0 : dp2px(4));
                v_setText(nameLabel, mine.getName());
                v_setText(headerSection, R.id.label_brief, setColor(TextUtils.isEmpty(Integer.toString(mine.index)) ? "欢迎来到操盘侠" : "我的小金号：" + mine.index, TEXT_GREY_COLOR));
            }

            View accountSection = v_findView(this, R.id.list_fund_account);

            // update cnAccountCell
            {
                FundFamily account = cnAccount;
                boolean isRunning = isStockAccountOpened(account);
                String accountName = "沪深账户";

                StockAccountProfileView cell = v_findView(accountSection, R.id.cell_cn_account);
                cell.setIconResource(R.mipmap.ic_cn_fund);
                cell.setAccountName(accountName);
                double income = safeGet(() -> account.income, 0D);
                if (isRunning) {
                    cell.setIncome(income);
                } else {
                    cell.setIncomeText("尚未投资");
                }

                cell.setHintText(isRunning ? "预计收益" : "雇个牛人为你操盘");
                v_setClick(cell, v -> {
                    showActivity(this, an_MyStockAccountDetailPage(Money_Type.getInstance(account.moneyType)));
                });
            }

            // update hkAccountCell
            {
                FundFamily account = hkAccount;
                boolean isRunning = isStockAccountOpened(account);
                String accountName = "港股账户";

                StockAccountProfileView cell = v_findView(accountSection, R.id.cell_hk_account);
                cell.setIconResource(R.mipmap.ic_hk_fund);
                cell.setAccountName(accountName);
                double income = safeGet(() -> account.income, 0D);
                if (isRunning) {
                    cell.setIncome(income);
                } else {
                    cell.setIncomeText("尚未投资");
                }
                cell.setHintText(isRunning ? "预计收益" : "雇个牛人为你操盘");
                v_setClick(cell, v -> {
                    if (isStockAccountOpened(account)) {
                        showActivity(this, an_MyStockAccountDetailPage(Money_Type.getInstance(account.moneyType)));
                    } else {
                        showActivity(this, an_RechargePage(account.moneyType, 0D));
                    }
                });

                cell.setVisibility(isRunning ? View.VISIBLE : View.GONE);
                v_findView(accountSection, R.id.line2).setVisibility(isRunning ? View.VISIBLE : View.GONE);
            }

            // updateSimulateCell
            {
                SimulationAccount account = simulationAccount;
                boolean isRunning = SimulationAccountExtension.isOpenedSimulationAccount();
                StockAccountProfileView cell = v_findView(accountSection, R.id.cell_simulate_account);
                cell.setIconResource(R.mipmap.ic_simulate_fund);
                cell.setAccountName("模拟炒股");
                if (isRunning) {
                    cell.setIncome(safeGet(() -> account.todayIncome, 0D));
                } else {
                    cell.setIncomeText("尚未开通");
                }
                cell.setHintText(isRunning ? "今日盈亏" : "参加比赛赢大奖");
                v_setClick(cell, v -> {
                    showActivity(this, an_StockTradePage(-1, "", 0));
                });
            }

            // update traderManager
            {
                int userType = safeGet(() -> MineManager.getInstance().getmMe().type, -1);
                boolean hasCreateFundPermission = anyMatch(userType, User_Type.Talent, User_Type.Trader);
                if (hasCreateFundPermission) {
                    v_setVisible(mLoginSection, R.id.cell_managed_groups);
                } else {
                    v_setGone(mLoginSection, R.id.cell_managed_groups);
                }
            }
        }

        private boolean isStockAccountOpened(FundFamily... funds) {
            return safeGet(() -> funds != null && Stream.of(funds).allMatch(it -> it.totalCapital > 0), false);
        }
    }
}
