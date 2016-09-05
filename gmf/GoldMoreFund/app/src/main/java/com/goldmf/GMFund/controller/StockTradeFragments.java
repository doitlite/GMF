package com.goldmf.GMFund.controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.controller.business.FundController;
import com.goldmf.GMFund.controller.business.StockController;
import com.goldmf.GMFund.controller.business.StockController.TradeExtraFundReal;
import com.goldmf.GMFund.controller.business.StockController.TradeUserPosition;
import com.goldmf.GMFund.controller.dialog.StockOrderDialog;
import com.goldmf.GMFund.controller.internal.ChildBinder;
import com.goldmf.GMFund.controller.internal.ChildBinders;
import com.goldmf.GMFund.extension.IntExtension;
import com.goldmf.GMFund.extension.UIControllerExtension;
import com.goldmf.GMFund.extension.ViewExtension;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.stock.SearchHistoryStockStore;
import com.goldmf.GMFund.manager.stock.SimulationAccountMore;
import com.goldmf.GMFund.model.CommonDefine.PlaceHolder;
import com.goldmf.GMFund.model.Fund;
import com.goldmf.GMFund.model.Order;
import com.goldmf.GMFund.model.Stock;
import com.goldmf.GMFund.model.StockInfo.StockBrief;
import com.goldmf.GMFund.model.StockInfo.StockSimple;
import com.goldmf.GMFund.model.StockPosition;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.PageArrayHelper;
import com.goldmf.GMFund.util.NumberUtil;
import com.goldmf.GMFund.util.PersistentObjectUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.AdvanceNestedScrollView;
import com.goldmf.GMFund.widget.BuySellStockIndicator;
import com.goldmf.GMFund.widget.FiveOrderCell;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.goldmf.GMFund.widget.GMFWebview;
import com.goldmf.GMFund.widget.InputAccessoryView;
import com.goldmf.GMFund.widget.PriceControlView;
import com.goldmf.GMFund.widget.ProgressButton;
import com.goldmf.GMFund.widget.SearchView;
import com.goldmf.GMFund.widget.ToggleCell;
import com.yale.ui.support.AdvanceSwipeRefreshLayout;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.subjects.PublishSubject;
import yale.extension.common.Optional;
import yale.extension.common.RangeD;
import yale.extension.common.shape.RoundCornerShape;
import yale.extension.system.SimpleListViewAdapter;
import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_FUND_NAME_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_ORDER_DATA_SOURCE_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_FUND_ID_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_KEYWORD_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_RANGE_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_STOCK_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_TAB_IDX_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_USER_ID_INT;
import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.FragmentStackActivity.pushFragment;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_QuotationDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_SelectStockPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockAnalysePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockHomePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockOrderListRealPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockTradePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BLUE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.ORANGE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.YELLOW_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getIncomeTextColor;
import static com.goldmf.GMFund.extension.AccessoryViewExtension.isKeyboardVisible;
import static com.goldmf.GMFund.extension.AccessoryViewExtension.registKeyboardChangedListener;
import static com.goldmf.GMFund.extension.AccessoryViewExtension.unregistKeyboardChangedListener;
import static com.goldmf.GMFund.extension.IntExtension.containOption;
import static com.goldmf.GMFund.extension.IntExtension.inRange;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.MResultExtension.zipToList;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addHorizontalSepLine;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.getSimpleAdapter;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.isScrollToBottom;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.StringExtension.anyContain;
import static com.goldmf.GMFund.extension.StringExtension.anyMatch;
import static com.goldmf.GMFund.extension.StringExtension.notEmpty;
import static com.goldmf.GMFund.extension.UIControllerExtension.createAlertDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.hideKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.performGoBack;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_addTextChangedListener;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_isVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_setBackgroundColor;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_unsignedNumberFormatter;
import static com.goldmf.GMFund.extension.ViewExtension.v_updateLayoutParams;
import static com.goldmf.GMFund.extension.ViewGroupExtension.v_forEach;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.hasMoreData;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatRatio;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;
import static com.goldmf.GMFund.util.FormatUtil.formatStockCode;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by yale on 16/2/22.
 */
public class StockTradeFragments {

    private static class TradeContext {
        public static TradeContext NULL = new TradeContext();

        static {
            NULL.userID = -1;
            NULL.stockID = "";
            NULL.fundID = -1;
        }

        public int userID;
        public String stockID;
        public int fundID;

        public boolean isFundStockTrade() {
            return fundID != -1;
        }

        public boolean isMe() {
            return MineManager.getInstance().isLoginOK() && userID == MineManager.getInstance().getmMe().index;
        }
    }

    private static PublishSubject<String> sSelectStockBuySubject = PublishSubject.create();
    private static PublishSubject<String> sSelectStockSellSubject = PublishSubject.create();

    protected static final int INCOME_TYPE_TOTAL = 0;     //总盈亏
    protected static final int INCOME_TYPE_TODAY = 1;     //今日盈亏

    public static class BaseStockTradeFragment extends SimpleFragment {

        protected TradeContext mTradeContext = new TradeContext();
        protected TradeUserPosition mTradeUserPosition = TradeUserPosition.NULL;
        protected String mStockBuyID;
        protected String mStockSellID;
        protected Stock mStockBuy;
        protected Stock mStockSell;
        protected CommandPageArray<Order> mSuccessOrderPageArray;
        protected CommandPageArray<Order> mCancelableOrderPageArray;
        protected Fund mFund;

        protected PublishSubject<Void> mStockBuyChangedSubject = PublishSubject.create();
        protected PublishSubject<Void> mStockSellChangedSubject = PublishSubject.create();
        protected PublishSubject<Void> mUserPositionChangedSubject = PublishSubject.create();
        protected PublishSubject<Void> mSuccessOrderChangedSubject = PublishSubject.create();
        protected PublishSubject<Void> mCancelableOrderChangedSubject = PublishSubject.create();

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            int loginUserID = safeGet(() -> MineManager.getInstance().getmMe().index, -1);
            mTradeContext.userID = safeGet(() -> getArguments().getInt(KEY_USER_ID_INT, -1), loginUserID);
            mTradeContext.stockID = safeGet(() -> getArguments().getString(KEY_STOCK_ID_STRING), "");
            mTradeContext.fundID = getArguments().getInt(KEY_FUND_ID_INT, -1);
            if (mTradeContext.userID == -1) {
                mTradeContext.userID = loginUserID;
            }

            mStockBuyID = mTradeContext.stockID;
            mStockSellID = mStockBuyID;

            if (safeGet(() -> mTradeContext.userID == MineManager.getInstance().getmMe().index, true)) {
                UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockPostionHose);
            } else {
                UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockPostionGuest);
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mTradeUserPosition = null;
            mStockBuy = null;
            mSuccessOrderPageArray = null;
            mCancelableOrderPageArray = null;
        }

        public TradeContext getTradeContext() {
            return mTradeContext;
        }

        public PublishSubject<Void> getStockBuyChangedSubject() {
            return mStockBuyChangedSubject;
        }

        public PublishSubject<Void> getStockSellChangedSubject() {
            return mStockSellChangedSubject;
        }

        public PublishSubject<Void> getAccountChangedSubject() {
            return mUserPositionChangedSubject;
        }

        public PublishSubject<Void> getSuccessOrderChangedSubject() {
            return mSuccessOrderChangedSubject;
        }

        public PublishSubject<Void> getCancelableOrderChangedSubject() {
            return mCancelableOrderChangedSubject;
        }

        public TradeUserPosition getTradeUserPosition() {
            return mTradeUserPosition;
        }

        public Optional<Stock> getStockBuy() {
            return opt(mStockBuy);
        }

        public Optional<Stock> getStockSell() {
            return opt(mStockSell);
        }

        public Optional<CommandPageArray<Order>> getSuccessOrderPageArray() {
            return opt(mSuccessOrderPageArray);
        }

        public Optional<CommandPageArray<Order>> getCancelableOrderPageArray() {
            return opt(mCancelableOrderPageArray);
        }

        public void requestRefetchUserPosition() {
            Observable<MResultsInfo<TradeUserPosition>> observable;
            if (getTradeContext().isFundStockTrade()) {
                observable = StockController.fetchUserPositionReal(getTradeContext().fundID);
            } else {
                observable = StockController.fetchUserPositionSimulation(getTradeContext().userID);
            }
            observable.subscribeOn(AndroidSchedulers.mainThread())
                    .delaySubscription(2, TimeUnit.SECONDS);
            consumeEventMR(observable)
                    .setTag("refresh_user_position")
                    .onNextSuccess(response -> {
                        mTradeUserPosition = response.data;
                        mTradeUserPosition.extraFundReal = TradeExtraFundReal.create(mFund);
                        mUserPositionChangedSubject.onNext(null);
                    })
                    .done();
        }

        public void requestRefetchSuccessOrder() {
            Observable<MResultsInfo<CommandPageArray<Order>>> observable = StockController.fetchStockOrderListSimulation(getTradeContext().userID)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .delaySubscription(2, TimeUnit.SECONDS);
            consumeEventMR(observable)
                    .setTag("refresh_success_order")
                    .onNextSuccess(response -> {
                        mSuccessOrderPageArray = response.data;
                        mSuccessOrderChangedSubject.onNext(null);
                    })
                    .done();
        }

        @SuppressWarnings("unchecked")
        public void requestRefetchCancelableOrder() {
            Observable<MResultsInfo<CommandPageArray<Order>>> observable;
            if (getTradeContext().isFundStockTrade()) {
                observable = (Observable) StockController.fetchPendingOrderListReal(getTradeContext().fundID);
            } else {
                observable = StockController.fetchPendingOrderListSimulation();
            }
            consumeEventMR(observable)
                    .setTag("refresh_cancelable_order")
                    .onNextSuccess(response -> {
                        mCancelableOrderPageArray = response.data;
                        mCancelableOrderChangedSubject.onNext(null);
                    })
                    .done();
        }
    }

    public static PublishSubject<Pair<String, String>> sSelectTradeTabSubject = PublishSubject.create();

    public static class StockTradeFragment extends BaseStockTradeFragment {

        public static final PublishSubject<Void> sEnterStockTradeFragmentSubject = PublishSubject.create();

        private static final int FETCH_STOCK_TYPE_BUY = 1;
        private static final int FETCH_STOCK_TYPE_SELL = 1 << 1;
        private int mInitTabIndex = 0;
        private boolean mHasFetchNecessaryData = false;
        private WebViewFragments.WebViewFragmentDelegate mDelegate;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_stock_trade, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            sEnterStockTradeFragmentSubject.onNext(null);

            mInitTabIndex = getArguments().getInt(KEY_TAB_IDX_INT);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));
            if (getTradeContext().isFundStockTrade()) {
                String title = getArguments().getString(KEY_FUND_NAME_STRING, "实盘");
                updateTitle(title);
            } else {
                updateTitle("模拟盘");
            }

            v_setClick(mReloadSection, v -> fetchData(true));
            setOnSwipeRefreshListener(v -> fetchData(false));
            Observable<String> stockBuySubject = sSelectStockBuySubject
                    .filter(it -> !TextUtils.isEmpty(it))
                    .debounce(200, TimeUnit.MILLISECONDS);
            consumeEvent(stockBuySubject)
                    .setTag("on_stock_buy_changed")
                    .onNextFinish(stockID -> {
                        consumeEventMR(StockController.fetchStockDetailInfo(false, stockID))
                                .setTag("fetch_selected_buy_stock_detail")
                                .onNextSuccess(response -> {
                                    mStockBuy = response.data;
                                    mStockBuyID = response.data.index;
                                    mStockBuyChangedSubject.onNext(null);
                                    ViewPager pager = v_findView(this, R.id.pager);
                                    if (pager.getAdapter() != null) {
                                        int selectedIdx = pager.getCurrentItem();
                                        String title = pager.getAdapter().getPageTitle(selectedIdx).toString();
                                        if (title.equals("买入")) {
                                            restartFetchStockDetailTimer(mStockBuyID, FETCH_STOCK_TYPE_BUY);
                                        }
                                    }
                                })
                                .onNextFinish(response -> {
                                    changeVisibleSection(TYPE_CONTENT);
                                })
                                .done();
                    })
                    .done();

            Observable<String> stockSellSubject = sSelectStockSellSubject
                    .filter(it -> !TextUtils.isEmpty(it))
                    .debounce(200, TimeUnit.MILLISECONDS);
            consumeEvent(stockSellSubject)
                    .setTag("on_stock_sell_changed")
                    .onNextFinish(stockID -> {
                        consumeEventMR(StockController.fetchStockDetailInfo(false, stockID))
                                .setTag("fetch_selected_sell_stock_detail")
                                .onNextSuccess(response -> {
                                    mStockSell = response.data;
                                    mStockSellID = response.data.index;
                                    mStockSellChangedSubject.onNext(null);
                                    ViewPager pager = v_findView(this, R.id.pager);
                                    if (pager.getAdapter() != null) {
                                        int selectedIdx = pager.getCurrentItem();
                                        String title = pager.getAdapter().getPageTitle(selectedIdx).toString();
                                        if (title.equals("卖出")) {
                                            restartFetchStockDetailTimer(mStockSellID, FETCH_STOCK_TYPE_SELL);
                                        }
                                    }
                                })
                                .onNextFinish(response -> {
                                    changeVisibleSection(TYPE_CONTENT);
                                })
                                .done();
                    })
                    .done();

            AdvanceSwipeRefreshLayout.class.cast(mRefreshLayout)
                    .setOnPreInterceptTouchEventDelegate(new AdvanceSwipeRefreshLayout.OnPreInterceptTouchEventDelegate() {
                        ViewPager pager = v_findView(mContentSection, R.id.pager);
                        Rect outRect = new Rect();

                        @Override
                        public boolean shouldDisallowInterceptTouchEvent(MotionEvent ev) {
                            String pageTitle = safeGet(() -> pager.getAdapter().getPageTitle(pager.getCurrentItem()).toString(), "");
                            if (anyMatch(pageTitle, "持仓")) {
                                int childCount = pager.getChildCount();
                                for (int i = 0; i < childCount; i++) {
                                    View child = pager.getChildAt(i);
                                    child.getGlobalVisibleRect(outRect);
                                    pager.getChildVisibleRect(child, outRect, null);
                                    if (outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                                        View appBarLayout = v_findView(child, R.id.appBarLayout);
                                        if (appBarLayout != null) {
                                            return appBarLayout.getTop() < 0;
                                        }
                                    }
                                }
                            }
                            return false;
                        }
                    });

            consumeEvent(sSelectTradeTabSubject)
                    .setTag("select_tab")
                    .onNextFinish(pair -> {
                        String title = pair.second;
                        if (anyContain(title, "持仓")) {
                            mInitTabIndex = 0;
                        } else if (anyContain(title, "买入")) {
                            mInitTabIndex = 1;
                            mStockBuyID = pair.first;
                        } else if (anyContain(title, "卖出")) {
                            mInitTabIndex = 2;
                            mStockSellID = pair.first;
                        } else if (anyContain(title, "撤单")) {
                            mInitTabIndex = 3;
                        } else if (anyContain(title, "记录")) {
                            mInitTabIndex = 4;
                        }
                        ViewPager pager = v_findView(mContentSection, R.id.pager);
                        pager.setCurrentItem(mInitTabIndex);
                        fetchData(false);
                        if (anyContain(title, "买入")) {
                            sSelectStockBuySubject.onNext(mStockBuyID);
                        } else if (anyContain(title, "卖出")) {
                            sSelectStockSellSubject.onNext(mStockSellID);
                        }
                    })
                    .done();

            fetchData(true);

            GMFWebview webview = v_findView(this, R.id.webView_invisible);
            mDelegate = new WebViewFragments.WebViewFragmentDelegate(this, webview, false);
            mDelegate.onViewCreated("");
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser) {
                Observable<Void> observable = NotificationCenter.currentTradingPageOrderStatusChange
                        .debounce(800, TimeUnit.MILLISECONDS);
                consumeEvent(observable)
                        .setTag("ObserveOrderStatus")
                        .addToVisible()
                        .onNextFinish(response -> {
                            if (getView() != null) {
                                requestRefetchUserPosition();
                            }
                        })
                        .done();
            }
        }

        @Override
        protected boolean onInterceptGoBack() {
            return super.onInterceptGoBack();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mHasFetchNecessaryData = false;
            mDelegate.onDestroyView();
        }

        private void fetchData(boolean reload) {
            int userID = getTradeContext().userID;
            if (!mHasFetchNecessaryData) {
                String stockBuyID = mStockBuyID;
                String stockSellID = mStockSellID;
                if (!notEmpty(stockBuyID, stockSellID)) {
                    Observable<List<MResultsInfo>> observable;
                    if (getTradeContext().isFundStockTrade()) {
                        observable = zipToList(fetchFundDetail(), fetchUserPosition(), fetchSuccessOrderList(userID), fetchCancelOrderList());
                    } else {
                        observable = zipToList(fetchUserPosition(), fetchSuccessOrderList(userID), fetchCancelOrderList());
                    }
                    consumeEventMRListUpdateUI(observable, reload)
                            .setTag("reload_data")
                            .onNextSuccess(responses -> {
                                if (getTradeContext().isFundStockTrade()) {
                                    Fund fund = safeGet(() -> ((MResultsInfo<Fund>) responses.get(0)).data, null);
                                    if (fund != null) {
                                        GMFWebview webview = v_findView(this, R.id.webView_invisible);
                                        webview.loadUrl(fund.fundStatusChangeURL());
                                    }
                                }
                                setupViewPagerAndTabLayout();
                            })
                            .done();

                } else {
                    Observable<List<MResultsInfo>> observable;
                    if (stockBuyID.equals(stockSellID)) {
                        observable = zipToList(fetchStockDetail(false, stockBuyID, FETCH_STOCK_TYPE_BUY | FETCH_STOCK_TYPE_SELL),
                                fetchUserPosition(),
                                fetchSuccessOrderList(userID),
                                fetchCancelOrderList());
                    } else {
                        observable = zipToList(fetchStockDetail(false, stockBuyID, FETCH_STOCK_TYPE_BUY),
                                fetchStockDetail(false, stockSellID, FETCH_STOCK_TYPE_SELL),
                                fetchUserPosition(),
                                fetchSuccessOrderList(userID),
                                fetchCancelOrderList());
                    }

                    consumeEventMRListUpdateUI(observable, reload)
                            .setTag("reload_data")
                            .onNextSuccess(responses -> {
                                setupViewPagerAndTabLayout();
                            })
                            .done();
                }
                mHasFetchNecessaryData = true;
            } else {
                ViewPager pager = v_findView(mContentSection, R.id.pager);
                String title = safeGet(() -> pager.getAdapter().getPageTitle(pager.getCurrentItem()).toString(), "");
                if (anyContain(title, "持仓")) {
                    consumeEventMRUpdateUI(fetchUserPosition(), reload)
                            .setTag("fetch_user_position")
                            .onNextSuccess(response -> {
                                mTradeUserPosition = response.data;
                                mTradeUserPosition.extraFundReal = TradeExtraFundReal.create(mFund);
                                mUserPositionChangedSubject.onNext(null);
                            })
                            .done();
                } else if (anyContain(title, "买入")) {
                    requestRefetchUserPosition();

                    if (TextUtils.isEmpty(mStockBuyID)) {
                        setSwipeRefreshing(false);
                        changeVisibleSection(TYPE_CONTENT);
                        return;
                    }
                    consumeEventMRUpdateUI(fetchStockDetail(false, opt(mStockBuyID).or(""), FETCH_STOCK_TYPE_BUY), reload)
                            .setTag("fetch_stock_buy_detail")
                            .onNextSuccess(response -> {
                                mStockBuyID = response.data.index;
                                mStockBuy = response.data;
                                mStockBuyChangedSubject.onNext(null);
                            })
                            .done();
                } else if (anyContain(title, "卖出")) {
                    requestRefetchUserPosition();

                    if (TextUtils.isEmpty(mStockSellID)) {
                        setSwipeRefreshing(false);
                        changeVisibleSection(TYPE_CONTENT);
                        return;
                    }

                    consumeEventMRUpdateUI(fetchStockDetail(false, opt(mStockSellID).or(""), FETCH_STOCK_TYPE_SELL), reload)
                            .setTag("fetch_stock_sell_detail")
                            .onNextSuccess(response -> {
                                mStockSellID = response.data.index;
                                mStockSell = response.data;
                                mStockSellChangedSubject.onNext(null);
                            })
                            .done();
                } else if (anyContain(title, "撤单")) {
                    consumeEventMRUpdateUI(fetchCancelOrderList(), reload)
                            .setTag("fetch_cancelable_order")
                            .onNextSuccess(response -> {
                                mCancelableOrderPageArray = response.data;
                                mCancelableOrderChangedSubject.onNext(null);
                            })
                            .done();
                } else if (anyContain(title, "记录")) {
                    consumeEventMRUpdateUI(fetchSuccessOrderList(userID), reload)
                            .setTag("fetch_success_order")
                            .onNextSuccess(response -> {
                                mSuccessOrderPageArray = response.data;
                                mSuccessOrderChangedSubject.onNext(null);
                            })
                            .done();
                } else {
                    setSwipeRefreshing(false);
                }
            }
        }


        private Observable<MResultsInfo<Stock>> fetchStockDetail(boolean local, String stockID, int fetchType) {
            return StockController.fetchStockDetailInfo(local, stockID)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(response -> {
                        if (isSuccess(response)) {
                            if (containOption(fetchType, FETCH_STOCK_TYPE_BUY)) {
                                mStockBuy = response.data;
                                mStockBuyID = response.data.index;
                            }
                            if (containOption(fetchType, FETCH_STOCK_TYPE_SELL)) {
                                mStockSell = response.data;
                                mStockSellID = response.data.index;
                            }
                        }
                    });
        }

        private Observable<MResultsInfo<Fund>> fetchFundDetail() {
            int fundID = getTradeContext().fundID;
            return FundController.fetchFundInfo(fundID, true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(response -> {
                        if (isSuccess(response)) {
                            mFund = response.data;
                        }
                    });
        }

        private Observable<MResultsInfo<TradeUserPosition>> fetchUserPosition() {
            boolean isFundTrade = mTradeContext.isFundStockTrade();
            return (isFundTrade ? StockController.fetchUserPositionReal(mTradeContext.fundID) : StockController.fetchUserPositionSimulation(mTradeContext.userID))
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(response -> {
                        if (isSuccess(response)) {
                            mTradeUserPosition = response.data;
                            mTradeUserPosition.extraFundReal = TradeExtraFundReal.create(mFund);
                        }
                    });
        }

        private Observable<MResultsInfo<CommandPageArray<Order>>> fetchSuccessOrderList(int userID) {
            return StockController.fetchStockOrderListSimulation(userID)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(response -> {
                        if (isSuccess(response)) {
                            mSuccessOrderPageArray = response.data;
                        }
                    });
        }

        @SuppressWarnings("unchecked")
        private Observable<MResultsInfo<CommandPageArray<Order>>> fetchCancelOrderList() {
            Observable<MResultsInfo<CommandPageArray<Order>>> observable;
            if (getTradeContext().isFundStockTrade()) {
                observable = (Observable) StockController.fetchPendingOrderListReal(getTradeContext().fundID);
            } else {
                observable = StockController.fetchPendingOrderListSimulation();
            }

            return observable
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(response -> {
                        if (isSuccess(response)) {
                            mCancelableOrderPageArray = response.data;
                        }
                    });
        }

        private void setupViewPagerAndTabLayout() {
            ViewPager pager = v_findView(this, R.id.pager);
            String[] titles;
            CreateFragmentFunc[] actionList;
            TradeContext tradeContext = getTradeContext();
            if (tradeContext.isMe()) {
                if (tradeContext.isFundStockTrade()) {
                    titles = new String[]{"持仓", "买入", "卖出", "撤单", "其它"};
                    actionList = new CreateFragmentFunc[]{
                            () -> new StockTradePageHoldStockFragment(),
                            () -> new StockTradePageTradeFragment().init(true),
                            () -> new StockTradePageTradeFragment().init(false),
                            () -> new StockTradePagePendingOrderFragment(),
                            () -> new StockTradePageOtherFragment().init(new Bundle())};
                } else {
                    titles = new String[]{"持仓", "买入", "卖出", "撤单", "记录"};
                    actionList = new CreateFragmentFunc[]{
                            () -> new StockTradePageHoldStockFragment(),
                            () -> new StockTradePageTradeFragment().init(true),
                            () -> new StockTradePageTradeFragment().init(false),
                            () -> new StockTradePagePendingOrderFragment(),
                            () -> new StockTradePageHistoryOrderFragment()};
                }
            } else {
                titles = new String[]{"持仓", "记录"};
                actionList = new CreateFragmentFunc[]{
                        () -> new StockTradePageHoldStockFragment(),
                        () -> new StockTradePageHistoryOrderFragment()};
            }
            pager.setAdapter(new PagerAdapter(getChildFragmentManager(), titles, actionList));
            InputAccessoryView accessoryView = v_findView(this, R.id.accessoryView);
            ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);

                    String title = pager.getAdapter().getPageTitle(position).toString();
                    if (anyMatch(title, "买入", "卖出")) {
                        if (anyMatch(title, "买入")) {
                            restartFetchStockDetailTimer(mStockBuyID, FETCH_STOCK_TYPE_BUY);
                        } else if (anyMatch(title, "卖出")) {
                            restartFetchStockDetailTimer(mStockSellID, FETCH_STOCK_TYPE_SELL);
                        }
                    } else {
                        stopFetchStockDetailTimer();
                    }
                    if (anyMatch(title, "持仓")) {
                        restartFetchAccountDetailTimer();
                    } else {
                        stopFetchAccountDetailTimer();
                    }
                    accessoryView.resetChildren(null);
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                    hideKeyboardFromWindow(pager);
                }
            };
            pager.addOnPageChangeListener(onPageChangeListener);

            TabLayout tabLayout = v_findView(this, R.id.tabLayout);
            tabLayout.setupWithViewPager(v_findView(this, R.id.pager));
            int tabIndex = min(max(mInitTabIndex, 0), pager.getAdapter().getCount());
            pager.setCurrentItem(tabIndex, true);
            onPageChangeListener.onPageSelected(tabIndex);
        }

        private void stopFetchAccountDetailTimer() {
            unsubscribeFromVisible("account_timer");
        }

        private void restartFetchAccountDetailTimer() {
            Observable<MResultsInfo<TradeUserPosition>> observable;
            if (getTradeContext().isFundStockTrade()) {
                observable = StockController.fetchUserPositionReal(getTradeContext().fundID);
            } else {
                observable = StockController.fetchUserPositionSimulation(getTradeContext().userID);
            }
            observable.subscribeOn(AndroidSchedulers.mainThread())
                    .delaySubscription(5, TimeUnit.SECONDS)
                    .repeat();
            consumeEventMR(observable)
                    .addToVisible()
                    .setTag("account_timer")
                    .onNextSuccess(response -> {
                        mTradeUserPosition = response.data;
                        mTradeUserPosition.extraFundReal = TradeExtraFundReal.create(mFund);
                        mUserPositionChangedSubject.onNext(null);
                    })
                    .done();
        }

        private void stopFetchStockDetailTimer() {
            unsubscribeFromVisible("stock_timer");
        }

        private void restartFetchStockDetailTimer(String stockID, int type) {
            if (TextUtils.isEmpty(stockID)) {
                stopFetchStockDetailTimer();
                return;
            }

            Observable<MResultsInfo<Stock>> observable = StockController.fetchStockDetailInfo(false, stockID)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .delaySubscription(5, TimeUnit.SECONDS)
                    .repeat();
            consumeEventMR(observable)
                    .setTag("stock_timer")
                    .onNextSuccess(response -> {
                        if (containOption(type, FETCH_STOCK_TYPE_BUY)) {
                            mStockBuy = response.data;
                            mStockBuyChangedSubject.onNext(null);
                        }
                        if (containOption(type, FETCH_STOCK_TYPE_SELL)) {
                            mStockSell = response.data;
                            mStockSellChangedSubject.onNext(null);
                        }
                    })
                    .done();
        }
    }

    private static Optional<StockTradeFragment> getParentFragment(Fragment fragment) {
        if (fragment != null && fragment.getParentFragment() != null && fragment.getParentFragment() instanceof StockTradeFragment) {
            return Optional.of((StockTradeFragment) fragment.getParentFragment());
        }

        return Optional.empty();
    }

    private static TradeContext getTradeContext(Fragment fragment) {
        return getParentFragment(fragment).let(it -> it.getTradeContext()).or(TradeContext.NULL);
    }

    private static Optional<PublishSubject<Void>> getStockChangedSubjectFromParent(Fragment fragment, boolean isBuy) {
        return getParentFragment(fragment).let(it -> isBuy ? it.getStockBuyChangedSubject() : it.getStockSellChangedSubject());
    }

    private static Optional<PublishSubject<Void>> getAccountChangedSubjectFromParent(Fragment fragment) {
        return getParentFragment(fragment).let(it -> it.getAccountChangedSubject());
    }

    private static Optional<PublishSubject<Void>> getSuccessOrderChangedSubjectFromParent(Fragment fragment) {
        return getParentFragment(fragment).let(it -> it.getSuccessOrderChangedSubject());
    }

    private static Optional<PublishSubject<Void>> getCancelableOrderChangedSubjectFromParent(Fragment fragment) {
        return getParentFragment(fragment).let(it -> it.getCancelableOrderChangedSubject());
    }

    private static TradeUserPosition getUserPositionFromParent(Fragment fragment) {
        return getParentFragment(fragment).let(it -> it.getTradeUserPosition()).or(TradeUserPosition.NULL);
    }

    private static Optional<Stock> getStockFromParent(Fragment fragment, boolean isBuy) {
        return getParentFragment(fragment).let(it -> isBuy ? it.getStockBuy() : it.getStockSell()).orNull();
    }

    private static Optional<CommandPageArray<Order>> getSuccessOrderPageArrayFromParent(Fragment fragment) {
        return getParentFragment(fragment).let(it -> it.getSuccessOrderPageArray().orNull());
    }

    private static Optional<CommandPageArray<Order>> getCancelableOrderPageArrayFromParent(Fragment fragment) {
        return getParentFragment(fragment).let(it -> it.getCancelableOrderPageArray().orNull());
    }

    private static void requestRefetchUserPositionOfParent(Fragment fragment) {
        getParentFragment(fragment).apply(it -> it.requestRefetchUserPosition());
    }

    private static void requestRefetchSuccessOrderOfParent(Fragment fragment) {
        getParentFragment(fragment).apply(it -> it.requestRefetchSuccessOrder());
    }

    private static void requestRefetchCancelableOrderOfParent(Fragment fragment) {
        getParentFragment(fragment).apply(it -> it.requestRefetchCancelableOrder());
    }

    private interface CreateFragmentFunc extends Func0<Fragment> {
    }

    private static class PagerAdapter extends FragmentPagerAdapter {
        private String[] mTitles;
        private CreateFragmentFunc[] mActionList;

        public PagerAdapter(FragmentManager fm, String[] titles, CreateFragmentFunc[] actionList) {
            super(fm);
            mTitles = titles;
            mActionList = actionList;
        }

        @Override
        public Fragment getItem(int position) {
            return mActionList[position].call();
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }

    public static class StockTradePageHoldStockFragment extends SimpleFragment {


        private boolean mDataIsExpired = true;
        private int mCurrentOpenedGroupPosition = -1;
        private List<HoldStockCellVM> mMineHoldStocks;

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
            return inflater.inflate(R.layout.frag_stock_trade_page_hold_stock, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            changeVisibleSection(TYPE_LOADING);

            getAccountChangedSubjectFromParent(this)
                    .consume(subject -> {
                        consumeEvent(subject)
                                .setTag("on_account_change")
                                .onNextFinish(nil -> {
                                    mDataIsExpired = true;
                                    if (getUserVisibleHint() && getView() != null) {
                                        resetData(false);
                                    }
                                })
                                .done();
                    });

            AdvanceNestedScrollView.class.cast(mContentSection)
                    .setOnPreInterceptTouchEventDelegate(new AdvanceNestedScrollView.OnPreInterceptTouchEventDelegate() {
                        AppBarLayout appBarLayout = v_findView(view, R.id.appBarLayout);
                        View child = v_findView(mContentSection, R.id.listView);
                        PointF mDownPoint = new PointF();
                        PointF mLastPoint = new PointF();

                        @Override
                        public boolean shouldDisallowInterceptTouchEvent(MotionEvent ev) {
                            switch (ev.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    mDownPoint.set(ev.getRawX(), ev.getRawY());
                                    break;
                                case MotionEvent.ACTION_MOVE: {
                                    if (intentToScrollTolUp(ev) && child.canScrollVertically(-1)) {
                                        return true;
                                    }
                                    if (appBarLayout.getBottom() <= 0 && intentToScrollToDown(ev) && child.canScrollVertically(1)) {
                                        return true;
                                    }
                                }
                                break;
                            }

                            mLastPoint.set(ev.getRawX(), ev.getRawY());
                            return false;
                        }

                        private boolean intentToScrollTolUp(MotionEvent ev) {
                            return ev.getRawY() > mLastPoint.y;
                        }

                        private boolean intentToScrollToDown(MotionEvent ev) {
                            return ev.getRawY() < mLastPoint.y;
                        }
                    });

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
                if (mDataIsExpired) {
                    boolean reload = !v_isVisible(mContentSection);
                    resetData(reload);
                    mDataIsExpired = false;
                }
            }
        }

        private void resetData(boolean reload) {
            if (reload) {
                changeVisibleSection(TYPE_LOADING);
            }
            TradeContext tradeContext = getTradeContext(this);
            int userID = tradeContext.userID;
            int loginUserID = safeGet(() -> MineManager.getInstance().getmMe().index, -1);
            boolean isOwn = userID == loginUserID;

            if (MineManager.getInstance().isLoginOK() && !isOwn) {
                Observable<List<MResultsInfo>> observable;
                if (tradeContext.isFundStockTrade()) {
                    observable = zipToList(StockController.fetchUserPositionSimulation(loginUserID), StockController.fetchUserPositionReal(tradeContext.fundID));
                } else {
                    observable = zipToList(StockController.fetchUserPositionSimulation(loginUserID), StockController.fetchUserPositionSimulation(userID));
                }
                consumeEventMRListUpdateUI(observable, false)
                        .setTag("refresh_mine_position")
                        .onNextSuccess(responses -> {
                            TradeUserPosition mineAccount = (TradeUserPosition) responses.get(0).data;
                            TradeUserPosition userPosition = (TradeUserPosition) responses.get(1).data;
                            if (mineAccount != null) {
                                mMineHoldStocks = Stream.of(mineAccount.holdStocks).map(it -> new HoldStockCellVM(it)).collect(Collectors.toList());
                            }
                            HoldStockHeaderVM headerVM = new HoldStockHeaderVM(userPosition);
                            if (userPosition.isNotNull()) {
                                List<HoldStockCellVM> holdStocks = Stream.of(userPosition.holdStocks).map(it -> new HoldStockCellVM(it)).collect(Collectors.toList());
                                List<HoldStockCellVM> historyStocks = Stream.of(userPosition.historyStocks).map(it -> new HoldStockCellVM(it)).collect(Collectors.toList());
                                if (mineAccount != null) {
                                    for (HoldStockCellVM hold : holdStocks) {
                                        for (HoldStockCellVM mineHold : mMineHoldStocks) {
                                            if (hold.stockId.equals(mineHold.stockId) && mineHold.isCanSell) {
                                                hold.isCanSell = true;
                                                break;
                                            } else {
                                                hold.isCanSell = false;
                                            }
                                        }
                                    }
                                }
                                updateHeaderSection(headerVM);
                                updateHoldStockListSection(holdStocks, historyStocks);
                                changeVisibleSection(TYPE_CONTENT);
                            }
                        })
                        .done();

            } else {
                TradeUserPosition userPosition = getUserPositionFromParent(this);
                if (!userPosition.isNull()) {
                    HoldStockHeaderVM headerVM = new HoldStockHeaderVM(userPosition);
                    List<HoldStockCellVM> holdStocks = Stream.of(userPosition.holdStocks).map(it -> new HoldStockCellVM(it)).collect(Collectors.toList());
                    List<HoldStockCellVM> historyStocks = Stream.of(userPosition.historyStocks).map(it -> new HoldStockCellVM(it)).collect(Collectors.toList());
                    updateHeaderSection(headerVM);
                    updateHoldStockListSection(holdStocks, historyStocks);
                    changeVisibleSection(TYPE_CONTENT);
                }
            }
        }

        private void updateHeaderSection(HoldStockHeaderVM info) {
            View headerView = v_findView(this, R.id.header_hold_stock);

            TextView todayIncomeTitleLabel = v_findView(headerView, R.id.label_today_income_title);
            v_setText(todayIncomeTitleLabel, getTradeContext(this).isFundStockTrade() ? "当日盈亏" : "模拟盘当日盈亏");

            v_setText(headerView, R.id.label_income_and_income_ratio, info.dayIncomeAndRatio);

            CharSequence[][] textsArray;
            if (getTradeContext(this).isFundStockTrade()) {
                textsArray = new CharSequence[][]{
                        {info.totalCapital,
                                info.marketValue,
                                info.validCapital,
                                info.totalIncome,
                                info.totalIncomeRatio,
                                info.currentPosition
                        },
                        {
                                info.raisedCapital,
                                info.traderCapital,
                                info.tradeDuration,
                                info.incomeSharePercent,
                                info.warningLine,
                                info.clearLine
                        },
                        {
                                info.startTime,
                                info.stopTime}};
            } else {
                textsArray = new CharSequence[][]{
                        {info.totalCapital,
                                info.marketValue,
                                info.validCapital,
                                info.totalIncome,
                                info.totalIncomeRatio,
                                info.currentPosition
                        }};

            }

            ViewPager panelPager = v_findView(this, R.id.pager_panel);
            ViewGroup indicatorGroup = v_findView(this, R.id.group_indicator);
            int panelCount = textsArray.length;
            int lastItemIdx = panelPager.getAdapter() != null ? panelPager.getCurrentItem() : -1;
            if (panelPager.getAdapter() == null) {
                panelPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    }

                    @Override
                    public void onPageSelected(int position) {
                        v_forEach(indicatorGroup, (idx, child) -> child.setSelected(idx == (position % panelCount)));
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
            }
            panelPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
                @Override
                public Fragment getItem(int position) {
                    CharSequence[] texts = textsArray[position % textsArray.length];
                    StockTradePagePanelFragment ret = new StockTradePagePanelFragment();
                    ret.texts = texts;
                    return ret;
                }

                @Override
                public int getCount() {
                    return panelCount == 1 ? panelCount : panelCount * 1000;
                }
            });
            if (panelCount > 1) {
                if (lastItemIdx != -1) {
                    panelPager.setCurrentItem(lastItemIdx, false);
                } else {
                    panelPager.setCurrentItem(panelPager.getAdapter().getCount() / 2, false);
                }
            }

            indicatorGroup.removeAllViews();
            if (textsArray.length > 1) {
                Stream.range(0, textsArray.length)
                        .forEach(idx -> {
                            View bubble = new View(getActivity());
                            StateListDrawable drawable = new StateListDrawable();
                            drawable.addState(new int[]{android.R.attr.state_selected}, new ShapeDrawable(new RoundCornerShape(0xFFBEBEBE, dp2px(2))));
                            drawable.addState(new int[0], new ShapeDrawable(new RoundCornerShape(0x66BEBEBE, dp2px(2))));
                            bubble.setBackgroundDrawable(drawable);
                            indicatorGroup.addView(bubble, dp2px(4), dp2px(4));
                            v_updateLayoutParams(bubble, ViewGroup.MarginLayoutParams.class, params -> params.setMargins(dp2px(2), 0, dp2px(2), 0));
                        });
                v_forEach(indicatorGroup, (idx, child) -> child.setSelected(idx == (panelPager.getCurrentItem() % panelCount)));
            }
        }

        @SuppressWarnings("unchecked")
        private void updateHoldStockListSection(List<HoldStockCellVM> holdStocks, List<HoldStockCellVM> historyStocks) {
            ExpandableListView mListView = v_findView(this, R.id.listView);
            View holdStockCell = v_findView(this, R.id.cell_hold_stock_header);
            View historyHoldStockLabel = v_findView(this, R.id.label_history_hold_stock);

            TradeContext tradeContext = getTradeContext(this);
            boolean isOwn = tradeContext.isMe();

            v_setClick(historyHoldStockLabel, v -> {
                String userID = String.valueOf(tradeContext.userID);
                pushFragment(this, new HistoryHoldStockListFragment().init(userID));
            });

            if (holdStocks.isEmpty() && historyStocks.isEmpty()) {
                v_setVisible(mEmptySection);
                v_setClick(mEmptySection, R.id.btn_add, v -> {
                    showActivity(this, an_StockHomePage(0));
                    MainFragments.sStockSelectTabSubject.onNext(0);
                });
                v_setGone(holdStockCell);
                v_setGone(historyHoldStockLabel);
                v_setGone(mListView);
                return;
            } else if (historyStocks.isEmpty()) {
                v_setGone(historyHoldStockLabel);
            } else if (holdStocks.isEmpty()) {
                v_setGone(holdStockCell);
                v_setGone(mListView);
                return;
            }

            if (isOwn) {
                v_setText(holdStockCell, R.id.text1, "股票/代码");
                v_setText(holdStockCell, R.id.text2, "持有/可卖");
            } else {
                v_setText(holdStockCell, R.id.text1, "股票/代码");
                v_setText(holdStockCell, R.id.text2, "仓位");
            }
            v_setText(holdStockCell, R.id.text3, "现价/成本");
            v_setText(holdStockCell, R.id.text4, "浮动盈亏");

            if (mListView.getAdapter() != null) {
                HoldStockListAdapter adapter = (HoldStockListAdapter) mListView.getExpandableListAdapter();
                adapter.mTradeContext = getTradeContext(this);
                adapter.mGroupList = holdStocks;
                adapter.notifyDataSetChanged();
            } else {
                HoldStockListAdapter adapter = new HoldStockListAdapter(getTradeContext(this), holdStocks);
                mListView.setAdapter(adapter);
                mListView.setOnGroupClickListener((parent, view, groupPos, id) -> true);
            }

            mListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                    if (mCurrentOpenedGroupPosition == -1) {
                        mListView.expandGroup(groupPosition);
                        mCurrentOpenedGroupPosition = groupPosition;
                    } else {
                        if (mCurrentOpenedGroupPosition == groupPosition) {
                            mListView.collapseGroup(groupPosition);
                            mCurrentOpenedGroupPosition = -1;
                        } else {
                            mListView.collapseGroup(mCurrentOpenedGroupPosition);
                            mListView.expandGroup(groupPosition);
                            mCurrentOpenedGroupPosition = groupPosition;
                        }
                    }
                    return true;
                }
            });
        }
    }

    public static class StockTradePagePanelFragment extends Fragment {
        private CharSequence[] texts = new CharSequence[0];

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.panel_stock_trade_page_hold_stock, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            CharSequence text0 = texts.length > 0 ? texts[0] : "";
            CharSequence text1 = texts.length > 1 ? texts[1] : "";
            CharSequence text2 = texts.length > 2 ? texts[2] : "";
            CharSequence text3 = texts.length > 3 ? texts[3] : "";
            CharSequence text4 = texts.length > 4 ? texts[4] : "";
            CharSequence text5 = texts.length > 5 ? texts[5] : "";
            v_setText(view, R.id.label_top_left, text0);
            v_setText(view, R.id.label_top_medium, text1);
            v_setText(view, R.id.label_top_right, text2);
            v_setText(view, R.id.label_bottom_left, text3);
            v_setText(view, R.id.label_bottom_medium, text4);
            v_setText(view, R.id.label_bottom_right, text5);
        }
    }


    private static class HoldStockListAdapter extends BaseExpandableListAdapter {
        private TradeContext mTradeContext;
        private List<HoldStockCellVM> mGroupList;

        public HoldStockListAdapter(TradeContext tradeContext, List<HoldStockCellVM> groupList) {
            mTradeContext = tradeContext;
            mGroupList = groupList;
        }

        @Override
        public int getGroupCount() {
            return mGroupList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mGroupList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mGroupList.get(groupPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_hold_stock, parent, false);
            }
            HoldStockCellVM item = (HoldStockCellVM) getGroup(groupPosition);
            if (mTradeContext.isMe()) {
                v_setText(convertView, R.id.text2, item.holdCountAndSellableCount);
            } else {
                v_setText(convertView, R.id.text2, item.position);
            }

            v_setText(convertView, R.id.text1, item.stockNameAndCode);
            v_setText(convertView, R.id.text3, item.currentPriceAndCost);
            v_setText(convertView, R.id.text4, item.currentTotalIncomeRatioAndIncome);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_hold_stock_brief, parent, false);
            }
            HoldStockCellVM item = (HoldStockCellVM) getGroup(groupPosition);

            v_setClick(convertView, R.id.text1, v -> showActivity(parent.getContext(), an_QuotationDetailPage(item.stockId)));
            if (mTradeContext.isMe()) {
                v_setClick(convertView, R.id.text2, v -> sSelectTradeTabSubject.onNext(Pair.create(item.stockId, "买入")));
            } else {
                v_setClick(convertView, R.id.text2, v -> showActivity(parent.getContext(), an_StockTradePage(-1, item.stockId, 1)));
            }

            TextView saleButton = v_findView(convertView, R.id.text3);

            if (item.isCanSell && MineManager.getInstance().isLoginOK()) {
                if (mTradeContext.isMe()) {
                    v_setClick(saleButton, v -> sSelectTradeTabSubject.onNext(Pair.create(item.stockId, "卖出")));
                } else {
                    v_setClick(saleButton, v -> showActivity(parent.getContext(), an_StockTradePage(-1, item.stockId, 2)));
                }
                saleButton.setTextColor(ORANGE_COLOR);
                saleButton.setEnabled(true);
            } else {
                saleButton.setTextColor(TEXT_GREY_COLOR);
                saleButton.setEnabled(false);
            }

            TextView analyseBtn = v_findView(convertView, R.id.text4);
            if (mTradeContext.isFundStockTrade()) {
                analyseBtn.setTextColor(TEXT_GREY_COLOR);
                analyseBtn.setEnabled(false);
            } else {
                analyseBtn.setTextColor(TEXT_BLACK_COLOR);
                analyseBtn.setEnabled(true);
                v_setClick(analyseBtn, v -> showActivity(parent.getContext(), an_StockAnalysePage(mTradeContext.userID, item.stockId, item.range)));
            }
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    public static class HistoryHoldStockListFragment extends SimpleFragment {

        private int mCurrentOpenedGroupPosition = -1;
        private boolean mDataIsExpired = true;

        private String mUserID;
        private ExpandableListView mListView;
        private List<HoldStockCellVM> mMineHoldStocks;

        protected HistoryHoldStockListFragment init(String userID) {
            Bundle arguments = new Bundle();
            arguments.putString(KEY_USER_ID_INT, opt(userID).or(""));
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            String defaultUserID = safeGet(() -> String.valueOf(MineManager.getInstance().getmMe().index), "");
            mUserID = safeGet(() -> getArguments().getString(KEY_USER_ID_INT), defaultUserID);
            return inflater.inflate(R.layout.frag_history_stock_list, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            v_setClick(mReloadSection, v -> fetchData(true));
            setOnSwipeRefreshListener(() -> fetchData(false));
            mListView = v_findView(this, R.id.listView);
            AdvanceSwipeRefreshLayout.class
                    .cast(mRefreshLayout)
                    .setOnPreInterceptTouchEventDelegate(ev -> mListView.getChildCount() > 0 && mListView.getChildAt(0).getTop() < 0);

            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                }
            });
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
                if (mDataIsExpired) {
                    fetchData(true);
                    mDataIsExpired = false;
                }
            }
        }

        private void fetchData(boolean isReload) {
            if (isReload) {
                changeVisibleSection(TYPE_LOADING);
            } else {
                setSwipeRefreshing(true);
            }

            TradeContext tradeContext = getTradeContext(this);
            int loginUserID = safeGet(() -> MineManager.getInstance().getmMe().index, -1);
            int userID = tradeContext.userID;
            int fundID = tradeContext.fundID;

            if (MineManager.getInstance().isLoginOK()) {
                Observable<List<MResultsInfo>> observable;
                if (tradeContext.isFundStockTrade()) {
                    observable = zipToList(StockController.fetchUserPositionSimulation(loginUserID), StockController.fetchUserPositionReal(fundID));
                } else {
                    observable = zipToList(StockController.fetchUserPositionSimulation(loginUserID), StockController.fetchUserPositionSimulation(userID));
                }
                consumeEventMRListUpdateUI(observable, false)
                        .setTag("refresh_mine_position")
                        .onNextSuccess(responses -> {
                            SimulationAccountMore mineAccount = (SimulationAccountMore) responses.get(0).data;
                            SimulationAccountMore userAccount = (SimulationAccountMore) responses.get(1).data;
                            if (mineAccount != null) {
                                mMineHoldStocks = Stream.of(mineAccount.holdStocks).map(it -> new HoldStockCellVM(it)).collect(Collectors.toList());
                            }
                            if (userAccount != null) {
                                List<HoldStockCellVM> historyStocks = Stream.of(userAccount.historyStocks).map(it -> new HoldStockCellVM(it)).collect(Collectors.toList());
                                if (mineAccount != null) {
                                    for (HoldStockCellVM hold : historyStocks) {
                                        for (HoldStockCellVM mineHold : mMineHoldStocks) {
                                            if (hold.stockId.equals(mineHold.stockId) && mineHold.isCanSell) {
                                                hold.isCanSell = true;
                                                break;
                                            } else {
                                                hold.isCanSell = false;
                                            }
                                        }
                                    }
                                }
                                updateHoldStockListSection(historyStocks);
                            }
                        })
                        .done();
            } else {
                Observable<MResultsInfo<TradeUserPosition>> observable;
                if (tradeContext.isFundStockTrade()) {
                    observable = StockController.fetchUserPositionReal(fundID);
                } else {
                    observable = StockController.fetchUserPositionSimulation(userID);
                }

                consumeEventMR(observable)
                        .setTag("refresh_user_position")
                        .onNextSuccess(response -> {
                            TradeUserPosition userPosition = response.data;
                            if (userPosition.isNotNull()) {
                                List<HoldStockCellVM> historyStocks = Stream.of(userPosition.historyStocks).map(it -> new HoldStockCellVM(it)).collect(Collectors.toList());
                                updateHoldStockListSection(historyStocks);
                                v_setVisible(mContentSection);
                            } else {
                                v_setVisible(mReloadSection);
                            }
                            v_setGone(mLoadingSection);
                            setSwipeRefreshing(false);
                        })
                        .done();
            }
        }


        private void updateHoldStockListSection(List<HoldStockCellVM> historyStocks) {

            if (historyStocks == null || historyStocks.isEmpty()) {
                v_setVisible(mEmptySection);
                return;
            }


            if (mListView.getAdapter() != null) {
                HistoryHoldStockListAdapter adapter = (HistoryHoldStockListAdapter) mListView.getExpandableListAdapter();
                adapter.mUserID = mUserID;
                adapter.mGroupList = historyStocks;
                adapter.notifyDataSetChanged();
            } else {
                HistoryHoldStockListAdapter adapter = new HistoryHoldStockListAdapter(mUserID, historyStocks);
                mListView.setAdapter(adapter);
                mListView.setOnGroupClickListener((parent, view, groupPos, id) -> true);
            }

            mListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                    if (mCurrentOpenedGroupPosition == -1) {
                        mListView.expandGroup(groupPosition);
                        mCurrentOpenedGroupPosition = groupPosition;
                    } else {
                        if (mCurrentOpenedGroupPosition == groupPosition) {
                            mListView.collapseGroup(groupPosition);
                            mCurrentOpenedGroupPosition = -1;
                        } else {
                            mListView.collapseGroup(mCurrentOpenedGroupPosition);
                            mListView.expandGroup(groupPosition);
                            mCurrentOpenedGroupPosition = groupPosition;
                        }
                    }
                    return true;
                }
            });
        }
    }

    private static class HistoryHoldStockListAdapter extends BaseExpandableListAdapter {

        private String mUserID;
        private List<HoldStockCellVM> mGroupList;
        private boolean isOwned;

        public HistoryHoldStockListAdapter(String userID, List<HoldStockCellVM> groupList) {
            mUserID = userID;
            mGroupList = groupList;
            isOwned = safeGet(() -> mUserID.equals(String.valueOf(MineManager.getInstance().getmMe().index)), true);
        }

        @Override
        public int getGroupCount() {
            return mGroupList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mGroupList.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mGroupList.get(groupPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_hold_stock, parent, false);
            }
            HoldStockCellVM item = (HoldStockCellVM) getGroup(groupPosition);
            v_setText(convertView, R.id.text1, item.stockNameAndCode);
            v_setText(convertView, R.id.text2, item.historyHoldCountAndDay);
            v_setText(convertView, R.id.text3, item.sellPriceAndCost);
            v_setText(convertView, R.id.text4, item.finalTotalIncomeRatioAndIncome);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_hold_stock_brief, parent, false);
            }
            HoldStockCellVM item = (HoldStockCellVM) getGroup(groupPosition);

            v_setClick(convertView, R.id.text1, v -> showActivity(parent.getContext(), an_QuotationDetailPage(item.stockId)));
            v_setClick(convertView, R.id.text2, v -> showActivity(parent.getContext(), an_StockTradePage(-1, item.stockId, 1)));
            TextView saleButton = v_findView(convertView, R.id.text3);
            saleButton.setTextColor(TEXT_GREY_COLOR);
            saleButton.setEnabled(false);

            if (item.isCanSell && MineManager.getInstance().isLoginOK()) {
                v_setClick(saleButton, v -> showActivity(parent.getContext(), an_StockTradePage(-1, item.stockId, 2)));
                saleButton.setTextColor(ORANGE_COLOR);
                saleButton.setEnabled(true);
            } else {
                saleButton.setTextColor(TEXT_GREY_COLOR);
                saleButton.setEnabled(false);
            }

            v_setClick(convertView, R.id.text4, v -> showActivity(parent.getContext(), an_StockAnalysePage(mUserID, item.stockId, item.range)));
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }

    private static class HoldStockHeaderVM {
        public CharSequence dayIncomeAndRatio;

        public CharSequence totalCapital;       //总资产
        public CharSequence marketValue;        //股票市值
        public CharSequence validCapital;       //可用资金

        public CharSequence totalIncome;        //总收益
        public CharSequence totalIncomeRatio;   //总收益率
        public CharSequence currentPosition;    //当前仓位

        public CharSequence raisedCapital;      //初始募集资金
        public CharSequence traderCapital;      //操盘人出资
        public CharSequence tradeDuration;      //交易周期

        public CharSequence incomeSharePercent; //收益分成比例
        public CharSequence warningLine;        //预警线
        public CharSequence clearLine;          //清仓线

        public CharSequence startTime;          //开始运行日期
        public CharSequence stopTime;           //结束运行日期


        public HoldStockHeaderVM(TradeUserPosition raw) {
            this.dayIncomeAndRatio = setColor(concatNoBreak(setFontSize(concatNoBreak(formatMoney(raw.dayIncome, true, 2), " "),
                    sp2px(30)), setFontSize(formatRatio(raw.dayIncomeRatio, true, 2), sp2px(14))), getIncomeTextColor(raw.dayIncome));
            this.totalCapital = concat("总资产", formatToReadableMoney(raw.totalCapital));
            this.marketValue = concat("股票市值", formatToReadableMoney(raw.stockCapital));
            this.validCapital = concat("可用资金", formatToReadableMoney(raw.cashCapital));

            this.currentPosition = concat("当前仓位", formatToReadableRatio(raw.currentPosition));
            this.totalIncome = concat("总盈亏", formatToIncome(raw.totalIncome));
            this.totalIncomeRatio = concat("总收益", formatToIncomeRatio(raw.totalIncomeRatio));

            TradeExtraFundReal extra = raw.extraFundReal;
            this.raisedCapital = concat("初始投资金额", formatToReadableMoney(extra.raisedCapital));
            this.traderCapital = concat("操盘手出资", formatToReadableMoney(extra.traderCapital));
            this.tradeDuration = concat("交易周期", formatSecondText(String.format(Locale.getDefault(), "T+%d(剩余%d日)", extra.runningDay, extra.remainingDay)));

            this.incomeSharePercent = concat("收益分成比例", formatSecondText(formatRatio(extra.incomeSharePercent, false, 0, 2)));
            this.warningLine = concat("预警线", formatSecondText(formatRatio(extra.warningLine, false, 0, 2)));
            this.clearLine = concat("清仓线", formatSecondText(formatRatio(extra.clearLing, false, 0, 2)));

            this.startTime = concat("开始运行时间", formatSecondText(formatSecond(extra.startTime, "yyyy/MM/dd")));
            this.stopTime = concat("结束运行时间", formatSecondText(formatSecond(extra.stopTime, "yyyy/MM/dd")));
        }

        private CharSequence formatToReadableMoney(Double bigNumber) {
            return formatSecondText(formatMoney(bigNumber, false, 2));
        }

        private CharSequence formatToReadableRatio(Double ratio) {
            return formatSecondText(formatRatio(ratio, false, 2));
        }

        private CharSequence formatToIncome(Double income) {
            return formatSecondText(formatMoney(income, false, 2), getIncomeTextColor(income));
        }

        private CharSequence formatToIncomeRatio(Double incomeRatio) {
            return formatSecondText(formatRatio(incomeRatio, false, 2), getIncomeTextColor(incomeRatio));
        }

        private CharSequence formatSecondText(CharSequence text) {
            return formatSecondText(text, TEXT_WHITE_COLOR);
        }

        private CharSequence formatSecondText(CharSequence text, int color) {
            return setFontSize(setColor(text, color), sp2px(14));
        }
    }

    private static class HoldStockCellVM {
        public StockPosition raw;
        public String stockId;
        public String range;

        public CharSequence stockNameAndValue;
        public CharSequence holdCountAndSellableCount;
        public CharSequence position;
        public CharSequence currentPriceAndCost;
        public boolean isCanSell = false;
        public CharSequence sellPriceAndCost;
        public CharSequence currentTotalIncomeRatioAndIncome;
        //        public CharSequence currentTodayIncomeRatioAndIncome;

        public CharSequence stockNameAndCode;
        public CharSequence historyHoldCountAndDay;
        public CharSequence finalTotalIncomeRatioAndIncome;
        private StockSimple mStock;

        public HoldStockCellVM(StockPosition raw) {
            mStock = raw.stock;

            this.raw = raw;
            this.stockId = raw.stock.index;
            this.range = raw.range;

            this.stockNameAndValue = generateTitleAndSubtitle(() -> mStock.name, () -> formatMoney(raw.holdCapital, false, 2), TEXT_GREY_COLOR);
            this.holdCountAndSellableCount = generateTitleAndSubtitle(() -> formatMoney(raw.holdAmount, false, 0), () -> formatMoney(raw.validAmount, false, 0), TEXT_GREY_COLOR);

            this.position = generateTitleAndSubtitle(() -> formatRatio(raw.curPosition, false, 2), () -> " ", TEXT_GREY_COLOR);
            this.currentPriceAndCost = generateTitleAndSubtitle(() -> formatMoney(raw.currentPrice, false, 2), () -> formatMoney(raw.holdPrice, false, 2), TEXT_GREY_COLOR);
            this.isCanSell = safeGet(() -> raw.validAmount, 0d) > 0;
            this.sellPriceAndCost = generateTitleAndSubtitle(() -> formatMoney(raw.sellPrice, false, 2), () -> formatMoney(raw.holdPrice, false, 2), TEXT_GREY_COLOR);
            this.currentTotalIncomeRatioAndIncome = generateTitleAndSubtitle(() -> setColor(formatMoney(raw.income, false, 2), getIncomeTextColor(raw.incomeRatio, TEXT_GREY_COLOR)),
                    () -> formatRatio(raw.incomeRatio, false, 2), getIncomeTextColor(raw.income, TEXT_GREY_COLOR));
            //            this.currentTodayIncomeRatioAndIncome = generateTitleAndSubtitle(() -> setColor(formatRatio(raw.todayIncomeRatio, true, 2), getIncomeTextColor(raw.todayIncomeRatio, TEXT_GREY_COLOR)),
            //                    () -> formatMoney(raw.todayIncome, true, 2), getIncomeTextColor(raw.todayIncome, TEXT_GREY_COLOR));

            this.stockNameAndCode = generateTitleAndSubtitle(() -> mStock.name, () -> mStock.code, TEXT_GREY_COLOR);
            this.historyHoldCountAndDay = generateTitleAndSubtitle(() -> formatMoney(raw.totalSellAmount, false, 0), () -> raw.holdTimeStr, TEXT_GREY_COLOR);
            this.finalTotalIncomeRatioAndIncome = this.currentTotalIncomeRatioAndIncome;
        }

        private CharSequence generateTitleAndSubtitle(Func0<CharSequence> titleGetter, Func0<CharSequence> subTitle, int subTitleColor) {
            return safeGet(() -> (CharSequence) concat(titleGetter.call(), setColor(setFontSize(subTitle.call(), sp2px(10)), subTitleColor)))
                    .def(generateDefaultTitleAndSubtitle()).get();
        }

        private CharSequence generateDefaultTitleAndSubtitle() {
            return concat(PlaceHolder.NULL_VALUE, setColor(setFontSize(PlaceHolder.NULL_VALUE, sp2px(10)), TEXT_GREY_COLOR));
        }
    }

    public static class StockTradePageHistoryOrderFragment extends SimpleFragment {

        private boolean mDataIsExpired = true;
        private boolean mIsLoadingMore = false;
        private Optional<CommandPageArray<Order>> mCachePageArray = Optional.empty();

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
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup
                container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_stock_trade_page_hitsory_order, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            changeVisibleSection(TYPE_LOADING);

            RecyclerView recyclerView = v_findView(this, R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            {
                Paint paint = new Paint();
                paint.setColor(0x0D000000);
                paint.setStrokeWidth(dp2px(1));
                recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
                        super.onDrawOver(c, parent, state);
                        v_forEach(parent, (pos, child) -> c.drawLine(child.getLeft(), child.getBottom(), child.getRight(), child.getBottom(), paint));
                    }
                });
            }

            getSuccessOrderChangedSubjectFromParent(this)
                    .apply(subject -> {
                        consumeEvent(subject)
                                .setTag("on_success_order_changed")
                                .onNextFinish(ignored -> {
                                    if (getUserVisibleHint() && getView() != null) {
                                        resetData(getSuccessOrderPageArrayFromParent(this));
                                    } else {
                                        mDataIsExpired = true;
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
            mDataIsExpired = true;
            mIsLoadingMore = false;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (mDataIsExpired) {
                    resetData(getSuccessOrderPageArrayFromParent(this));
                    mDataIsExpired = false;
                }
            }
        }

        public void resetData(Optional<CommandPageArray<Order>> pageArray) {
            mCachePageArray = pageArray;
            mIsLoadingMore = false;

            if (pageArray.let(it -> it.data()).let(it -> it.isEmpty()).or(Boolean.TRUE)) {
                changeVisibleSection(TYPE_EMPTY);
            } else {
                List<HistoryOrderVM> items = Stream.of(pageArray.get().data()).map(it -> new HistoryOrderVM(it)).collect(Collectors.toList());
                updateContentSection(items);
                changeVisibleSection(TYPE_CONTENT);
            }
        }

        public void fetchMoreData() {
            if (mIsLoadingMore || !hasMoreData(mCachePageArray)) {
                return;
            }

            mIsLoadingMore = true;
            CommandPageArray<Order> pageArray = mCachePageArray.get();
            consumeEventMR(PageArrayHelper.getNextPage(pageArray))
                    .setTag("fetch_more")
                    .onNextSuccess(response -> {
                        List<HistoryOrderVM> items = Stream.of(pageArray.data()).map(it -> new HistoryOrderVM(it)).collect(Collectors.toList());
                        updateContentSection(items);
                    })
                    .onNextFinish(response -> {
                        mIsLoadingMore = false;
                    })
                    .done();
        }

        @SuppressWarnings("unchecked")
        private void updateContentSection(List<HistoryOrderVM> items) {
            RecyclerView recyclerView = v_findView(this, R.id.recyclerView);
            if (recyclerView.getLayoutManager() == null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            }

            SimpleRecyclerViewAdapter<HistoryOrderVM> adapter = getSimpleAdapter(recyclerView);
            if (adapter != null) {
                adapter.resetItems(items);
            } else {
                adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                        .onCreateItemView((parent, viewType) -> LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_stock_order_history, parent, false))
                        .onCreateViewHolder(builder -> builder
                                .bindChildWithTag("indicator", R.id.indicator)
                                .bindChildWithTag("stockNameAndCode", R.id.text1)
                                .bindChildWithTag("countAndBuyPrice", R.id.text2)
                                .bindChildWithTag("dateAndTime", R.id.text3)
                                .configureView((item, pos) -> {
                                    builder.<BuySellStockIndicator>getChildWithTag("indicator").setState(item.actionType);
                                    v_setText(builder.getChildWithTag("stockNameAndCode"), item.stockNameAndCode);
                                    v_setText(builder.getChildWithTag("countAndBuyPrice"), item.countAndBuyPrice);
                                    v_setText(builder.getChildWithTag("dateAndTime"), item.dateAndTime);
                                })
                                .create())
                        .onViewHolderCreated((ad, holder) -> {
                            v_setClick(holder.itemView, v -> {
                                HistoryOrderVM item = ad.getItem(holder.getAdapterPosition());
                                if (item != null) {
                                    showActivity(this, an_QuotationDetailPage(item.stockID));
                                }
                            });
                        })
                        .create();
                adapter.addFooter(adapter.createFooterView(getActivity(), R.layout.footer_loading_more));
                recyclerView.clearOnScrollListeners();
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (!mIsLoadingMore && hasMoreData(mCachePageArray)) {
                            if (isScrollToBottom(recyclerView)) {
                                fetchMoreData();
                            }
                        }
                    }

                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            if (!mIsLoadingMore && hasMoreData(mCachePageArray)) {
                                fetchMoreData();
                            }
                        }
                    }
                });
                recyclerView.setAdapter(adapter);
            }

            View loadingMoreFooter = adapter.getFooter(0);
            loadingMoreFooter.setVisibility(hasMoreData(mCachePageArray) ? View.VISIBLE : View.GONE);
        }
    }

    private static class HistoryOrderVM {
        public String stockID;
        public int actionType;
        public CharSequence stockNameAndCode;
        public CharSequence countAndBuyPrice;
        public CharSequence dateAndTime;

        public HistoryOrderVM(Order raw) {
            StockSimple stock = raw.stock;

            this.stockID = safeGet(() -> stock.index, "");
            this.actionType = raw.buy ? BuySellStockIndicator.STATE_BUY : BuySellStockIndicator.STATE_SELL;
            this.stockNameAndCode = concat(stock.name, setFontSize(setColor(stock.code, TEXT_GREY_COLOR), sp2px(10)));
            this.countAndBuyPrice = concat(formatMoney(raw.transactionAmount, false, 0), setFontSize(setColor(formatMoney(raw.transactionPrice, false, 2), TEXT_GREY_COLOR), sp2px(10)));
            this.dateAndTime = concat(formatSecond(raw.transactionTime, "MM/dd"), setFontSize(setColor(formatSecond(raw.transactionTime, "HH:mm"), TEXT_GREY_COLOR), sp2px(10)));
        }
    }

    public static class StockTradePagePendingOrderFragment extends SimpleFragment {
        private boolean mDataIsExpired = true;
        private boolean mIsLoadingMore = false;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_stock_trade_page_pending_order, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            changeVisibleSection(TYPE_LOADING);

            RecyclerView recyclerView = v_findView(this, R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            addHorizontalSepLine(recyclerView);

            PublishSubject<Void> subject = getCancelableOrderChangedSubjectFromParent(this).orNull();
            if (subject != null) {
                consumeEvent(subject)
                        .setTag("on_cancelable_order_change")
                        .onNextFinish(ignored -> {
                            mDataIsExpired = true;
                            if (getUserVisibleHint() && getView() != null) {
                                resetData(getCancelableOrderPageArrayFromParent(this));
                            }
                        })
                        .done();
            }

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
                    resetData(getCancelableOrderPageArrayFromParent(this));
                    mDataIsExpired = false;
                }
            }
        }

        public void resetData(Optional<CommandPageArray<Order>> pageArray) {
            if (pageArray.let(it -> it.data()).let(it -> it.isEmpty()).or(Boolean.TRUE)) {
                changeVisibleSection(TYPE_EMPTY);
            } else {
                updateContentSection(pageArray.get());
                changeVisibleSection(TYPE_CONTENT);
            }
        }

        private void updateContentSection(CommandPageArray<Order> pageArray) {
            consumeEvent(NotificationCenter.freshStockInfoSubject)
                    .setTag("refresh_stock_info")
                    .onNextFinish(ignored -> {
                        updateContentSection(pageArray);
                    })
                    .done();

            List<PendingOrderVM> items = Stream.of(pageArray.data())
                    .map(it -> new PendingOrderVM(it))
                    .collect(Collectors.toList());
            RecyclerView recyclerView = v_findView(this, R.id.recyclerView);

            if (recyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<PendingOrderVM> adapter = getSimpleAdapter(recyclerView);
                adapter.resetItems(items);
            } else {
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (!mIsLoadingMore && PageArrayHelper.hasMoreData(pageArray) && isScrollToBottom(recyclerView)) {
                            mIsLoadingMore = true;
                            consumeEventMR(PageArrayHelper.getNextPage(pageArray))
                                    .setTag("get_next")
                                    .onNextSuccess(response -> {
                                        updateContentSection(pageArray);
                                    })
                                    .onNextFinish(ignored -> {
                                        mIsLoadingMore = false;
                                    })
                                    .done();
                        }
                    }
                });

                SimpleRecyclerViewAdapter<PendingOrderVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                        .onCreateItemView(R.layout.cell_stock_order_pending)
                        .onCreateViewHolder(builder -> {
                            ChildBinder binder = ChildBinders.createWithBuilder(builder);
                            bindPendingOrderCell(binder);
                            builder.configureView((item, pos) -> {
                                Func0<PendingOrderVM> itemGetter = () -> item;
                                configurePendingOrderCell(binder, itemGetter);
                            });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            v_setClick(holder.itemView, v -> {
                                Order order = ad.getItem(holder.getAdapterPosition()).raw;
                                if (order.canCancel()) {
                                    StockOrderDialog.Item dItem = new StockOrderDialog.Item();
                                    dItem.stockName = order.stock.name;
                                    dItem.stockCode = order.stock.code;
                                    dItem.count = order.orderAmount;
                                    if (order.orderModel == Order.Model_Market) {
                                        dItem.way = "市价委托";
                                        dItem.price = null;
                                    } else if (order.orderModel == Order.Model_Limit) {
                                        dItem.way = "限价委托";
                                        dItem.price = order.orderPrice;
                                    }
                                    StockOrderDialog dialog = new StockOrderDialog(getActivity(), StockOrderDialog.STYLE_CANCEL, dItem);
                                    dialog.setDelegate((d, btn) -> {
                                        dialog.setCancelable(false);
                                        btn.setText("正在撤单", ProgressButton.Mode.Loading);
                                        btn.setMode(ProgressButton.Mode.Loading);
                                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockCancelClick);
                                        Observable<MResultsInfo<Void>> observable;
                                        if (getTradeContext(this).isFundStockTrade()) {
                                            observable = StockController.cancelRealOrderWithFundID(getTradeContext(this).fundID, order);
                                        } else {
                                            observable = StockController.cancelSimulationOrder(order);
                                        }
                                        consumeEventMR(observable.delay(1, TimeUnit.SECONDS))
                                                .setTag("cancel")
                                                .onNextStart(response -> {
                                                    dialog.dismiss();
                                                })
                                                .onNextSuccess(response -> {
                                                    requestRefetchCancelableOrderOfParent(this);
                                                    showToast(this, "已提单");
                                                    UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockCancelSuc);
                                                })
                                                .onNextFail(response -> {
                                                    showToast(this, getErrorMessage(response));
                                                })
                                                .done();
                                    });
                                    dialog.show();
                                } else {
                                    createAlertDialog(this, "该委托正在撤单中").show();
                                }
                            });
                        })
                        .create();
                recyclerView.setAdapter(adapter);
            }
        }
    }

    private static void bindPendingOrderCell(ChildBinder binder) {
        binder.bindChildWithTag("indicator", R.id.indicator)
                .bindChildWithTag("nameAndCode", R.id.text2)
                .bindChildWithTag("expectCountAndPrice", R.id.text3)
                .bindChildWithTag("actualCountAndPrice", R.id.text4)
                .bindChildWithTag("status", R.id.text5);
    }

    private static void configurePendingOrderCell(ChildBinder binder, Func0<PendingOrderVM> itemGetter) {
        PendingOrderVM item = itemGetter.call();
        binder.<BuySellStockIndicator>getChildWithTag("indicator").setState(item.actionType);
        v_setText(binder.getChildWithTag("nameAndCode"), item.stockNameAndCode);
        v_setText(binder.getChildWithTag("expectCountAndPrice"), item.expectCountAndPrice);
        v_setText(binder.getChildWithTag("actualCountAndPrice"), item.actualCountAndPrice);
        v_setText(binder.getChildWithTag("status"), item.status);
    }


    private static class PendingOrderVM {
        public Order raw;

        public int actionType;
        public CharSequence stockNameAndCode;
        public CharSequence expectCountAndPrice;
        public CharSequence actualCountAndPrice;
        public CharSequence status;

        public PendingOrderVM(Order raw) {
            StockSimple stock = raw.stock;

            this.raw = raw;
            this.actionType = safeGet(() -> raw.buy, false) ? BuySellStockIndicator.STATE_BUY : BuySellStockIndicator.STATE_SELL;
            this.stockNameAndCode = generateTitleAndSubtitle(() -> stock.name, () -> stock.code);
            this.expectCountAndPrice = generateTitleAndSubtitle(() -> formatMoney(raw.orderAmount, false, 0), () -> raw.orderModel == Order.Model_Market ? "市价" : formatMoney(raw.orderPrice, false, 2));
            this.actualCountAndPrice = generateTitleAndSubtitle(() -> formatMoney(raw.transactionAmount, false, 0), () -> raw.transactionPrice == 0 ? "--" : formatMoney(raw.transactionPrice, false, 2));
            this.status = safeGet(() -> raw.getStatusText(), PlaceHolder.NULL_VALUE);
        }

        private static CharSequence generateTitleAndSubtitle(Func0<CharSequence> titleGetter, Func0<CharSequence> subTitleGetter) {
            return safeGet(() -> concat(titleGetter.call(), setFontSize(setColor(subTitleGetter.call(), TEXT_GREY_COLOR), sp2px(10))))
                    .def(concat(PlaceHolder.NULL_VALUE, setFontSize(setColor(PlaceHolder.NULL_VALUE, TEXT_GREY_COLOR), sp2px(10)))).get();
        }
    }

    public static class StockTradePageTradeFragment extends SimpleFragment {

        private boolean mIsBuyStock = true;
        private boolean mNeedToUpdateCurrentPrice = true;
        private boolean mNeedToUpdateStockCount = true;
        private boolean mNeedToUpdateTradeButton = true;
        private OnGlobalLayoutListener mOnLayoutListener = null;

        public StockTradePageTradeFragment init(boolean isBuy) {
            Bundle arguments = new Bundle();
            arguments.putBoolean("is_buy", isBuy);
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
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mIsBuyStock = getArguments().getBoolean("is_buy");
            return inflater.inflate(R.layout.frag_stock_trade_page_trade, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);


            //  init recyclerView
            {
                RecyclerView recyclerView = v_findView(view, R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.VERTICAL, false));
                addHorizontalSepLine(recyclerView);
            }

            //  init section_operation
            {
                View parent = v_findView(mContentSection, R.id.section_operate);
                parent.post(() -> {
                    v_updateLayoutParams((View) parent.getParent(), params -> {
                        if (parent.getMeasuredHeight() > 0) {
                            params.height = Math.max(parent.getMeasuredHeight(), params.height);
                        }

                    });
                });

                PriceControlView priceControlView = v_findView(parent, R.id.priceControl);
                priceControlView.updateTheme(mIsBuyStock ? PriceControlView.THEME_BLUE : PriceControlView.THEME_ORANGE);
                EditText stockCountField = v_findView(parent, R.id.field_count);
                stockCountField.setHint(mIsBuyStock ? "买入量" : "卖出量");

                v_addTextChangedListener(stockCountField, v_unsignedNumberFormatter());

                v_setText(parent, R.id.btn_buy_or_sell, mIsBuyStock ? "买入" : "卖出");
                v_setBackgroundColor(parent, R.id.btn_buy_or_sell, mIsBuyStock ? BLUE_COLOR : ORANGE_COLOR);

                v_setText(parent, R.id.btn_buy_or_sell_market, mIsBuyStock ? "市价买入" : "市价卖出");
                v_setBackgroundColor(parent, R.id.btn_buy_or_sell_market, mIsBuyStock ? BLUE_COLOR : ORANGE_COLOR);

                View stockCountSection = v_findView(this, R.id.section_stock_count);
                stockCountSection.setBackgroundDrawable(new ShapeDrawable(
                        new RoundCornerShape(Color.TRANSPARENT, dp2px(2)).border(mIsBuyStock ? BLUE_COLOR : ORANGE_COLOR, dp2px(1))));
            }

            List<HoldStockCellVM> holdStockCellVMs = Stream.of(getUserPositionFromParent(this).holdStocks)
                    .map(it -> new HoldStockCellVM(it))
                    .collect(Collectors.toList());
            updateHoldStockSection(holdStockCellVMs);

            Optional<Stock> stock = getStockFromParent(this, mIsBuyStock);
            onStockInfoChanged(stock.orNull());

            consumeEvent(mIsBuyStock ? sSelectStockBuySubject : sSelectStockSellSubject)
                    .setTag("stock_changed")
                    .onNextFinish(ignored -> {
                        mNeedToUpdateCurrentPrice = true;
                        mNeedToUpdateTradeButton = true;
                        mNeedToUpdateStockCount = true;
                    })
                    .done();

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                Optional<PublishSubject<Void>> stockChangedSubject = (getStockChangedSubjectFromParent(this, mIsBuyStock));
                if (stockChangedSubject.isPresent()) {
                    consumeEvent(stockChangedSubject.get())
                            .setTag("on_stock_change")
                            .addToVisible()
                            .onNextFinish(nil -> {
                                Optional<Stock> stock = getStockFromParent(this, mIsBuyStock);
                                onStockInfoChanged(stock.orNull());
                            })
                            .done();
                }

                Optional<PublishSubject<Void>> accountChangedSubject = getAccountChangedSubjectFromParent(this);
                if (accountChangedSubject.isPresent()) {
                    consumeEvent(accountChangedSubject.get())
                            .addToVisible()
                            .onNextFinish(nil -> {
                                List<HoldStockCellVM> holdStockCellVMs = Stream.of(getUserPositionFromParent(this).holdStocks)
                                        .map(it -> new HoldStockCellVM(it))
                                        .collect(Collectors.toList());
                                updateHoldStockSection(holdStockCellVMs);
                            })
                            .done();
                }
            }

            if (!isVisibleToUser || getView() == null) {
                unregistKeyboardChangedListener(mOnLayoutListener, getParentFragment());
                mOnLayoutListener = null;
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mNeedToUpdateCurrentPrice = true;
            mNeedToUpdateTradeButton = true;
            mNeedToUpdateStockCount = true;
            unregistKeyboardChangedListener(mOnLayoutListener, getParentFragment());
            mOnLayoutListener = null;
        }

        private Func0<Double> maxTradeCountGetter = () -> 0D;

        private void onMaxTradeCountChanged(double maxBuyCount, double maxSellCount) {
            Double maxTradeCount = mIsBuyStock ? maxBuyCount : maxSellCount;
            maxTradeCountGetter = () -> maxTradeCount;
            updateInputAccessoryView();

            {
                View parent = v_findView(mContentSection, R.id.section_operate);
                PriceControlView priceControlView = v_findView(parent, R.id.priceControl);
                EditText stockCountField = v_findView(parent, R.id.field_count);

                v_setClick(parent, R.id.btn_buy_or_sell, v -> {
                    Double price = priceControlView.getCurrentPrice();
                    Long count = NumberUtil.toLong(stockCountField.getText().toString(), 0L);
                    performBuyOrSellAction(price, count, maxTradeCount);
                    if (mIsBuyStock) {
                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockBuyPriceLimit);
                    } else {
                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockSellPriceLimit);
                    }
                });

                v_setClick(parent, R.id.btn_buy_or_sell_market, v -> {
                    Long count = NumberUtil.toLong(stockCountField.getText().toString(), 0L);
                    performBuyOrSellAction(null, count, maxTradeCount);
                    if (mIsBuyStock) {
                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockBuyPriceMarket);
                    } else {
                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockSellPriceMarket);
                    }
                });

                View stockCountSection = v_findView(this, R.id.section_stock_count);
                v_setClick(stockCountSection, R.id.label_hint, v -> {
                    int count = maxTradeCount.intValue();
                    v_setText(stockCountSection, R.id.field_count, count + "");
                });
            }
        }

        private void updateInputAccessoryView() {
            InputAccessoryView accessoryView = v_findView(getParentFragment(), R.id.accessoryView);
            View parent = v_findView(mContentSection, R.id.section_stock_count);
            EditText stockCountField = v_findView(parent, R.id.field_count);
            PriceControlView priceControlView = v_findView(mContentSection, R.id.priceControl);
            priceControlView.setPriceLabelOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && isKeyboardVisible(this)) {
                    Optional<Stock> stock = getStockFromParent(this, mIsBuyStock);
                    if (stock.isPresent()) {
                        Double lowPrice = stock.let(it -> it.declineLimit).or(null);
                        Double highPrice = stock.let(it -> it.surgedLimit).or(null);
                        resetAccessoryViewWhenStockPriceFieldGainFocus(accessoryView, priceControlView, lowPrice, highPrice);
                    } else {
                        accessoryView.resetChildren(null);
                    }
                }
            });
            stockCountField.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && isKeyboardVisible(this)) {
                    Optional<Stock> stock = getStockFromParent(this, mIsBuyStock);
                    if (stock.isPresent()) {

                        int countPerHand = stock.get().numberPerHand;
                        resetAccessoryViewWhenStockCountFieldGainFocus(accessoryView, stockCountField, countPerHand);
                    } else {
                        accessoryView.resetChildren(null);
                    }
                }
            });

            if (mOnLayoutListener == null) {
                if (getView() != null && getUserVisibleHint()) {
                    mOnLayoutListener = registKeyboardChangedListener(mOnLayoutListener, getParentFragment(), isKeyboardVisible -> {
                        boolean isConsumed = false;

                        if (stockCountField.isFocused()) {
                            Optional<Stock> stock = getStockFromParent(this, mIsBuyStock);
                            if (stock.isPresent()) {
                                int countPerHand = stock.get().numberPerHand;
                                resetAccessoryViewWhenStockCountFieldGainFocus(accessoryView, stockCountField, countPerHand);
                                isConsumed = true;
                            }
                        }

                        if (priceControlView.isPriceLabelGainFocus()) {
                            Optional<Stock> stock = getStockFromParent(this, mIsBuyStock);
                            if (stock.isPresent()) {
                                Double lowPrice = stock.let(it -> it.declineLimit).or(null);
                                Double highPrice = stock.let(it -> it.surgedLimit).or(null);
                                resetAccessoryViewWhenStockPriceFieldGainFocus(accessoryView, priceControlView, lowPrice, highPrice);
                            }
                            isConsumed = true;
                        }

                        if (!isConsumed) {
                            accessoryView.resetChildren(null);
                        }
                        v_setVisibility(accessoryView, isKeyboardVisible ? View.VISIBLE : View.GONE);

                        priceControlView.refreshCurrentPrice();
                    });
                } else {
                    unregistKeyboardChangedListener(mOnLayoutListener, getParentFragment());
                    mOnLayoutListener = null;
                }
            }
        }

        private void resetAccessoryViewWhenStockCountFieldGainFocus(InputAccessoryView accessoryView, EditText countField, int countPerHand) {
            final String TAG = "count_accessory_view";
            Context context = accessoryView.getContext();
            if (accessoryView.getChildCount() > 0 && accessoryView.getChildAt(0).getTag() != null && TAG.equals(accessoryView.getChildAt(0).getTag().toString())) {
                ViewGroup parent = (ViewGroup) accessoryView.getChildAt(0);
                v_forEach(parent, (pos, child) -> {
                    String text = opt(child).cast(TextView.class).let(it -> it.getText().toString()).or("");
                    v_setClick(child, v -> {
                        safeCall(() -> {
                            int maxCount = maxTradeCountGetter.call().intValue();
                            Integer newCount = null;
                            if (anyMatch(text, "1/4")) {
                                newCount = maxCount / 4;
                            } else if (anyMatch(text, "1/3")) {
                                newCount = maxCount / 3;
                            } else if (anyContain(text, "1/2")) {
                                newCount = maxCount / 2;
                            } else if (anyContain(text, "全部")) {
                                newCount = maxCount;
                            }
                            if (newCount != null) {
                                newCount = max(newCount - (newCount % countPerHand), 0);
                                countField.setText("");
                                countField.append(newCount.toString());
                            }
                        });
                    });
                });
            } else {
                List<View> children = Stream.of(new String[]{"1/4", "1/3", "1/2", "全部"})
                        .map(text -> {
                            TextView textView = new TextView(context);
                            textView.setText(text);
                            textView.setTextSize(18f);
                            textView.setTextColor(0xFFFFFFFF);
                            textView.setGravity(Gravity.CENTER);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1);
                            params.weight = 1;
                            textView.setLayoutParams(params);
                            textView.setOnClickListener(v -> {
                                safeCall(() -> {
                                    int maxCount = maxTradeCountGetter.call().intValue();
                                    Integer newCount = null;
                                    if (anyMatch(text, "1/4")) {
                                        newCount = maxCount / 4;
                                    } else if (anyMatch(text, "1/3")) {
                                        newCount = maxCount / 3;
                                    } else if (anyContain(text, "1/2")) {
                                        newCount = maxCount / 2;
                                    } else if (anyContain(text, "全部")) {
                                        newCount = maxCount;
                                    }
                                    if (newCount != null) {
                                        newCount = max(newCount - (newCount % countPerHand), 0);
                                        countField.setText("");
                                        countField.append(newCount.toString());
                                    }
                                });
                            });
                            return textView;
                        })
                        .collect(Collectors.toList());

                LinearLayout contentView = new LinearLayout(context);
                contentView.setTag(TAG);
                contentView.setOrientation(LinearLayout.HORIZONTAL);
                contentView.setBackgroundColor(0xFF7B7B7B);
                InputAccessoryView.LayoutParams params = new InputAccessoryView.LayoutParams(-1, dp2px(36));
                contentView.setLayoutParams(params);
                for (View child : children) {
                    contentView.addView(child);
                }

                accessoryView.resetChildren(contentView);
            }
        }

        private static void resetAccessoryViewWhenStockPriceFieldGainFocus(InputAccessoryView accessoryView, PriceControlView priceControlView, Double lowPrice, Double highPrice) {
            final String TAG = "price_accessory_view";
            Context context = accessoryView.getContext();
            String lowPriceText = "跌停 " + formatMoney(lowPrice, false, 2);
            String highPriceText = "涨停 " + formatMoney(highPrice, false, 2);
            if (accessoryView.getChildCount() > 0 && accessoryView.getChildAt(0).getTag() != null && TAG.equals(accessoryView.getChildAt(0).getTag().toString())) {
                ViewGroup parent = (ViewGroup) accessoryView.getChildAt(0);
                v_forEach(parent, (pos, child) -> {
                    TextView textView = (TextView) child;
                    String text = textView.getText().toString();
                    if (anyContain(text, "跌")) {
                        textView.setText(lowPriceText);
                    } else if (anyContain(text, "跌")) {
                        textView.setText(highPriceText);
                    }
                    v_setClick(child, v -> {
                        if (text.contains("跌")) {
                            priceControlView.setCurrentPrice(lowPrice);
                        } else if (text.contains("涨")) {
                            priceControlView.setCurrentPrice(highPrice);
                        }
                    });
                });
            } else {
                List<View> children = Stream.of(new String[]{lowPriceText, highPriceText})
                        .map(text -> {
                            TextView textView = new TextView(context);
                            textView.setText(text);
                            textView.setTextSize(18f);
                            textView.setTextColor(0xFFFFFFFF);
                            textView.setGravity(Gravity.CENTER);
                            textView.setPadding(dp2px(16), 0, dp2px(16), 0);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -1);
                            textView.setLayoutParams(params);
                            textView.setOnClickListener(v -> {
                                if (text.contains("跌")) {
                                    priceControlView.setCurrentPrice(lowPrice);
                                } else if (text.contains("涨")) {
                                    priceControlView.setCurrentPrice(highPrice);
                                }
                            });
                            return textView;
                        })
                        .collect(Collectors.toList());

                LinearLayout contentView = new LinearLayout(context);
                contentView.setOrientation(LinearLayout.HORIZONTAL);
                contentView.setBackgroundColor(0xFF7B7B7B);
                contentView.setTag(TAG);
                InputAccessoryView.LayoutParams params = new InputAccessoryView.LayoutParams(-1, dp2px(36));
                contentView.setLayoutParams(params);
                for (View child : children) {
                    contentView.addView(child);
                }
                accessoryView.resetChildren(contentView);
            }
        }


        /**
         * 执行下单的操作
         *
         * @param price 价格为空时代表按市价买入
         * @param count 购买数量
         */
        private void performBuyOrSellAction(@Nullable Double price, Long count, Double maxTradeCount) {
            Optional<Stock> cacheStock = getStockFromParent(this, mIsBuyStock);
            if (cacheStock.isAbsent())
                return;

            Stock stock = cacheStock.get();
            if (stock == null) {
                showToast(this, "请先选择股票或者点击搜索");
                return;
            }


            if (count == null || count <= 0) {
                showToast(this, "请输入委托数量");
                return;
            }

            if (price != null) {
                if (price > stock.surgedLimit) {
                    showToast(this, String.format("%s不可大于涨停价格", mIsBuyStock ? "买入" : "卖出"));
                    return;
                }
                if (price < stock.declineLimit) {
                    showToast(this, String.format("%s不可小于跌停价格", mIsBuyStock ? "买入" : "卖出"));
                    return;
                }
            }

            if (mIsBuyStock) {
                if (count % stock.numberPerHand != 0) {
                    showToast(this, String.format(Locale.getDefault(), "委托数量需要为%d的整数倍", stock.numberPerHand));
                    return;
                }
            } else {
                if (count % stock.numberPerHand != 0 && count != stock.stockPosition.validAmount) {
                    showToast(this, "委托数量必须为100的整数倍或全部卖出。");
                    return;
                }
            }

            if (count > maxTradeCount) {
                if (mIsBuyStock) {
                    showToast(this, "委托数量不得大于最大可买数量");
                } else {
                    showToast(this, "委托数量不得大于最大可卖数量");
                }

                return;
            }

            StockOrderDialog.Item item = new StockOrderDialog.Item();
            item.stockName = stock.name;
            item.stockCode = stock.code;
            item.way = price == null ? "市价委托" : "限价委托";
            item.count = count;
            item.price = price;
            StockOrderDialog dialog = new StockOrderDialog(getActivity(), mIsBuyStock ? StockOrderDialog.STYLE_BUY : StockOrderDialog.STYLE_SELL, item);
            dialog.setDelegate((d, btn) -> {
                Order order = new Order();
                order.stock = stock;
                order.buy = mIsBuyStock;
                order.orderPrice = Optional.of(price).or(stock.last);
                order.orderAmount = count;
                order.orderModel = price == null ? Order.Model_Market : Order.Model_Limit;

                TradeContext tradeContext = getTradeContext(this);
                if (tradeContext.isFundStockTrade()) {
                    d.setCancelable(false);
                    btn.setText("正在风控检测", ProgressButton.Mode.Loading);
                    btn.setMode(ProgressButton.Mode.Loading);
                    consumeEventMR(StockController.checkRealOrderWithFundID(tradeContext.fundID, order))
                            .onNextSuccess(ignored -> {
                                btn.setText("提交中", ProgressButton.Mode.Loading);
                                btn.setMode(ProgressButton.Mode.Loading);
                                consumeEventMR(StockController.addRealOrderWithFundID(tradeContext.fundID, order))
                                        .onNextStart(nil -> d.dismiss())
                                        .onNextSuccess(response -> {
                                            showToast(this, "委托已提交");
                                            if (!PersistentObjectUtil.readHasBuyOrSellStockBefore()) {
                                                PersistentObjectUtil.writeHasBuyOrSellStockBefore(true);

                                                UIControllerExtension.createAlertDialog(this, "未成交及部分成交的委托单可在【撤单】中查看，已成交的委托单可在【记录】中查看。").show();
                                            }

                                            requestRefetchUserPositionOfParent(this);
                                            requestRefetchSuccessOrderOfParent(this);
                                            requestRefetchCancelableOrderOfParent(this);

                                            PriceControlView controlView = v_findView(this, R.id.priceControl);
                                            onPriceChangedSubject.onNext(controlView.getCurrentPrice());
                                        })
                                        .onNextFail(response -> {
                                            showToast(this, getErrorMessage(response));
                                        })
                                        .done();
                            })
                            .onNextFail(response -> {
                                dialog.setCancelable(true);
                                dialog.setErrorText(getErrorMessage(response));
                                btn.setText("风险监测未通过", ProgressButton.Mode.FAILURE);
                                btn.setMode(ProgressButton.Mode.FAILURE);
                            })
                            .done();
                } else {
                    d.dismiss();
                    GMFProgressDialog progressDialog = new GMFProgressDialog(getActivity(), "提交中...");
                    progressDialog.show();
                    consumeEventMR(StockController.addSimulationOrder(order).delay(1, TimeUnit.SECONDS))
                            .setTag("add_order")
                            .onNextStart(response -> {
                                progressDialog.dismiss();
                            })
                            .onNextSuccess(response -> {
                                showToast(this, "委托已提交");

                                if (!PersistentObjectUtil.readHasBuyOrSellStockBefore()) {
                                    PersistentObjectUtil.writeHasBuyOrSellStockBefore(true);

                                    UIControllerExtension.createAlertDialog(this, "未成交及部分成交的委托单可在【撤单】中查看，已成交的委托单可在【记录】中查看。").show();
                                }

                                requestRefetchUserPositionOfParent(this);
                                requestRefetchSuccessOrderOfParent(this);
                                requestRefetchCancelableOrderOfParent(this);
                                PriceControlView controlView = v_findView(this, R.id.priceControl);
                                onPriceChangedSubject.onNext(controlView.getCurrentPrice());
                            })
                            .onNextFail(response -> {
                                showToast(this, getErrorMessage(response));
                            })
                            .done();
                }
            });
            dialog.show();
        }

        private void onStockInfoChanged(Stock stock) {
            updateStockNameSection(stock);
            updateStockPriceSection(stock);
            updateStockCountSection();
            updateStockTraderButtonSection(stock);
            updateFiveOrderSection(stock);
        }

        private void updateStockCountSection() {
            if (mNeedToUpdateStockCount) {
                v_setText(mContentSection, R.id.field_count, "");
                mNeedToUpdateStockCount = false;
            }
        }

        private void updateStockNameSection(StockSimple stock) {
            String stockName = opt(stock).let(it -> it.name).or("");
            String stockCode = opt(stock).let(it -> it.code).or("");

            View stockNameSection = v_findView(this, R.id.section_stock_name);

            stockNameSection.setBackgroundDrawable(new ShapeDrawable(
                    new RoundCornerShape(Color.TRANSPARENT, dp2px(2)).border(mIsBuyStock ? BLUE_COLOR : ORANGE_COLOR, dp2px(1))));
            v_setText(stockNameSection, R.id.label_stock_name, stockName);
            v_setText(stockNameSection, R.id.label_stock_code, stockCode);

            consumeEvent(SelectStockFragment.SELECT_STOCK_SUBJECT.limit(1))
                    .setTag("select_stock")
                    .onNextFinish(it -> {
                        if (mIsBuyStock) {
                            sSelectStockBuySubject.onNext(it.index);
                        } else {
                            sSelectStockSellSubject.onNext(it.index);
                        }
                    })
                    .done();
            v_setClick(stockNameSection, v -> showActivity(this, an_SelectStockPage(stockCode)));
        }

        private void updateStockPrice(double price) {
            PriceControlView controlView = v_findView(this, R.id.priceControl);
            controlView.setCurrentPrice(price);
        }

        private void updateStockTraderButtonSection(Stock stock) {
            View parent = v_findView(mContentSection, R.id.section_operate);
            Button leftButton = v_findView(parent, R.id.btn_buy_or_sell);
            Button rightButton = v_findView(parent, R.id.btn_buy_or_sell_market);
            if (stock == null)
                return;
            if (stock.suspension) {
                if (mNeedToUpdateTradeButton) {
                    showToast(this, "股票停牌，暂不可交易");
                    mNeedToUpdateTradeButton = false;
                }
                leftButton.setTextColor(TEXT_GREY_COLOR);
                rightButton.setTextColor(TEXT_GREY_COLOR);
                v_setBackgroundColor(leftButton, getResources().getColor(R.color.gmf_button_disable));
                v_setBackgroundColor(rightButton, getResources().getColor(R.color.gmf_button_disable));
                leftButton.setEnabled(false);
                rightButton.setEnabled(false);

            } else {
                leftButton.setTextColor(TEXT_WHITE_COLOR);
                rightButton.setTextColor(TEXT_WHITE_COLOR);
                v_setBackgroundColor(leftButton, mIsBuyStock ? BLUE_COLOR : ORANGE_COLOR);
                v_setBackgroundColor(rightButton, mIsBuyStock ? BLUE_COLOR : ORANGE_COLOR);
                leftButton.setEnabled(true);
                rightButton.setEnabled(true);
            }
        }

        public PublishSubject<Double> onPriceChangedSubject = PublishSubject.create();

        private void updateStockPriceSection(Stock stock) {
            PriceControlView controlView = v_findView(this, R.id.priceControl);
            if (stock != null) {
                consumeEvent(onPriceChangedSubject)
                        .setTag("on_price_changed")
                        .onNextFinish(currentPrice -> {
                            TradeContext tradeContext = getTradeContext(this);
                            Observable<MResultsInfo<Pair<Double, Double>>> observable;
                            if (tradeContext.isFundStockTrade()) {
                                observable = StockController.queryTradeCountReal(tradeContext.fundID, stock, currentPrice);
                            } else {
                                observable = StockController.queryTradeCountSimualtion(stock, currentPrice);
                            }
                            consumeEventMR(observable)
                                    .setTag("query_price")
                                    .onNextSuccess(response -> {
                                        onMaxTradeCountChanged(response.data.first, response.data.second);
                                        Double maxTradeCount = safeGet(() -> mIsBuyStock ? response.data.first : response.data.second, 0D);
                                        if (mIsBuyStock) {
                                            updateStockBuyCountHint(maxTradeCount.intValue());
                                        } else {
                                            updateStockSellCountHint(maxTradeCount.intValue());
                                        }
                                    })
                                    .done();
                        })
                        .done();
                controlView.setOnPriceChangedListener(currentPrice -> {
                    onPriceChangedSubject.onNext(currentPrice);
                });
                controlView.setPriceRange(new RangeD(opt(stock.declineLimit).or(0D), opt(stock.surgedLimit).or(0D)));
                if (mNeedToUpdateCurrentPrice) {
                    if (mIsBuyStock && stock.asks.size() > 0) {
                        //买股票用卖1的价格.
                        updateStockPrice(stock.asks.get(0).price);
                    } else if (!mIsBuyStock && stock.bids.size() > 0) {
                        //卖股票用买1的价格.
                        updateStockPrice(stock.bids.get(0).price);
                    }
                    mNeedToUpdateCurrentPrice = false;
                }
            } else {
                unsubscribeFromMain("on_price_changed");
                clearStockCountHint();
                controlView.setOnPriceChangedListener(null);
                controlView.setPriceRange(null);
                controlView.setCurrentPrice(null);
            }
        }

        private void updateStockBuyCountHint(Integer maxBuyCount) {
            View stockCountSection = v_findView(this, R.id.section_stock_count);
            v_setText(stockCountSection, R.id.label_hint, concatNoBreak("可买", setColor(maxBuyCount + "", BLUE_COLOR), "股"));
        }

        private void updateStockSellCountHint(int maxSellCount) {
            View stockCountSection = v_findView(this, R.id.section_stock_count);

            v_setText(stockCountSection, R.id.label_hint, concatNoBreak("可卖", setColor(maxSellCount + "", ORANGE_COLOR), "股"));
        }

        private void clearStockCountHint() {
            View stockCountSection = v_findView(this, R.id.section_stock_count);
            v_setText(stockCountSection, R.id.label_hint, "");
        }

        private void updateFiveOrderSection(Stock stock) {

            View parent = v_findView(this, R.id.section_five_order);
            if (stock == null) {
                Stream.of(
                        R.id.cell_bid1, R.id.cell_bid2, R.id.cell_bid3, R.id.cell_bid4, R.id.cell_bid5,
                        R.id.cell_ask1, R.id.cell_ask2, R.id.cell_ask3, R.id.cell_ask4, R.id.cell_ask5)
                        .map(it -> (FiveOrderCell) v_findView(parent, it))
                        .forEach(cell -> {
                            cell.setPrice(null, null);
                            cell.setCount(null);
                        });

            } else {
                {
                    Stream<FiveOrderCell> cellStream = Stream.of(R.id.cell_bid1, R.id.cell_bid2, R.id.cell_bid3, R.id.cell_bid4, R.id.cell_bid5).map(it -> (FiveOrderCell) v_findView(parent, it));
                    Stream<Stock.Quotation> quotationStream = Stream.of(stock.bids);

                    Stream.zip(cellStream, quotationStream, (cell, quotation) -> Pair.create(cell, quotation))
                            .forEach(pair -> {
                                FiveOrderCell cell = pair.first;
                                Stock.Quotation quotation = pair.second;
                                cell.setPrice(quotation.price, stock.prevClose);
                                cell.setCount((long) quotation.amount);
                                v_setClick(cell, v -> {
                                    updateStockPrice(quotation.price);
                                    UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockBuyFivePrice);
                                });
                            });
                }

                {
                    Stream<FiveOrderCell> cellStream = Stream.of(R.id.cell_ask1, R.id.cell_ask2, R.id.cell_ask3, R.id.cell_ask4, R.id.cell_ask5).map(it -> (FiveOrderCell) v_findView(parent, it));
                    Stream<Stock.Quotation> quotations = Stream.of(stock.asks);

                    Stream.zip(cellStream, quotations, (cell, quotation) -> Pair.create(cell, quotation))
                            .forEach(pair -> {
                                FiveOrderCell cell = pair.first;
                                Stock.Quotation quotation = pair.second;
                                cell.setPrice(quotation.price, stock.prevClose);
                                cell.setCount((long) quotation.amount);
                                v_setClick(cell, v -> {
                                    updateStockPrice(quotation.price);
                                    UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockBuyFivePrice);
                                });
                            });
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void updateHoldStockSection(List<HoldStockCellVM> items) {

            View holdStockSection = v_findView(mContentSection, R.id.section_hold_stock);
            if (items.isEmpty()) {
                v_setGone(holdStockSection);
            } else {
                v_setVisible(holdStockSection);
                RecyclerView recyclerView = v_findView(holdStockSection, R.id.recyclerView);
                if (recyclerView.getAdapter() != null) {
                    SimpleRecyclerViewAdapter<HoldStockCellVM> adapter = (SimpleRecyclerViewAdapter<HoldStockCellVM>) recyclerView.getAdapter();
                    adapter.resetItems(items);
                } else {
                    SimpleRecyclerViewAdapter<HoldStockCellVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                            .onCreateItemView((parent, viewType) -> LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_hold_stock, parent, false))
                            .onCreateViewHolder(builder -> {
                                builder.bindChildWithTag("nameAndValue", R.id.text1)
                                        .bindChildWithTag("holdCountAndSellableCount", R.id.text2)
                                        .bindChildWithTag("priceAndCost", R.id.text3)
                                        .bindChildWithTag("incomeRatioAndIncome", R.id.text4)
                                        .configureView((item, pos) -> {
                                            v_setText(builder.getChildWithTag("nameAndValue"), item.stockNameAndCode);
                                            v_setText(builder.getChildWithTag("holdCountAndSellableCount"), item.holdCountAndSellableCount);
                                            v_setText(builder.getChildWithTag("priceAndCost"), item.currentPriceAndCost);
                                            v_setText(builder.getChildWithTag("incomeRatioAndIncome"), item.currentTotalIncomeRatioAndIncome);
                                        });
                                return builder.create();
                            })
                            .onViewHolderCreated((ad, holder) -> {
                                v_setClick(holder.itemView, v -> {
                                    HoldStockCellVM item = ad.getItem(holder.getAdapterPosition());
                                    safeCall(() -> {
                                        if (item.raw.stock != null) {
                                            updateStockNameSection(item.raw.stock);
                                            updateStockPriceSection(null);
                                            clearStockCountHint();
                                            updateFiveOrderSection(null);
                                        }
                                        (mIsBuyStock ? sSelectStockBuySubject : sSelectStockSellSubject).onNext(item.stockId);
                                    });
                                });
                            })
                            .create();
                    recyclerView.setAdapter(adapter);
                }
            }
        }
    }

    public static class SelectStockFragment extends SimpleFragment {
        public static PublishSubject<StockBrief> SELECT_STOCK_SUBJECT = PublishSubject.create();
        private PublishSubject<String> mSearchSubject = PublishSubject.create();
        private OnGlobalLayoutListener onLayoutListener;

        public SelectStockFragment init(Optional<String> keyword) {
            Bundle arguments = new Bundle();
            keyword.consume(it -> arguments.putString(KEY_KEYWORD_STRING, it));
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_select_stock, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, YELLOW_COLOR);
            setupBackButton(this, ViewExtension.<SearchView>v_findView(view, R.id.searchView).getCancelButton());

            resetAccessoryView(getActivity(), v_findView(view, R.id.accessoryView), ViewExtension.<SearchView>v_findView(view, R.id.searchView).getEditText());

            {
                RecyclerView recyclerView = v_findView(view, R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                addHorizontalSepLine(recyclerView);
            }

            consumeEvent(mSearchSubject.debounce(300, TimeUnit.MILLISECONDS))
                    .onNextFinish(it -> searchByKeyword(it))
                    .done();

            {
                SearchView searchView = v_findView(this, R.id.searchView);
                EditText editText = searchView.getEditText();
                ImageView cleanButton = searchView.getCleanButton();
                v_setClick(cleanButton, () -> editText.setText(null));
                v_addTextChangedListener(editText, editable -> mSearchSubject.onNext(editable.toString()));
                searchByKeyword("");
            }

            {

                View accessoryView = v_findView(view, R.id.accessoryView);
                onLayoutListener = registKeyboardChangedListener(onLayoutListener, this, isVisible -> {
                    v_setVisibility(accessoryView, isVisible ? View.VISIBLE : View.GONE);
                });
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            unregistKeyboardChangedListener(onLayoutListener, this);
            onLayoutListener = null;

            SearchHistoryStockStore.getInstance().archiveNow();
        }

        @SuppressWarnings({"Convert2streamapi", "CodeBlock2Expr"})
        private static void resetAccessoryView(Context context, InputAccessoryView accessoryView, EditText editText) {
            List<View> children = Stream.of(new String[]{"60", "00", "30", "600", "000", "002"})
                    .map(text -> {
                        TextView textView = new TextView(context);
                        textView.setText(text);
                        textView.setTextSize(18f);
                        textView.setTextColor(0xFFFFFFFF);
                        textView.setGravity(Gravity.CENTER);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1);
                        params.weight = 1;
                        textView.setLayoutParams(params);
                        textView.setOnClickListener(v -> {
                            editText.append(textView.getText().toString());
                        });
                        return textView;
                    })
                    .collect(Collectors.toList());

            LinearLayout contentView = new LinearLayout(context);
            contentView.setOrientation(LinearLayout.HORIZONTAL);
            contentView.setBackgroundColor(0xFF7B7B7B);
            InputAccessoryView.LayoutParams params = new InputAccessoryView.LayoutParams(-1, dp2px(36));
            contentView.setLayoutParams(params);
            for (View child : children) {
                contentView.addView(child);
            }

            accessoryView.resetChildren(contentView);
        }

        private void searchByKeyword(String keyword) {
            TextView cleanAllLabel = v_findView(this, R.id.label_clean_history);
            SearchView searchView = v_findView(this, R.id.searchView);
            ImageView cleanButton = searchView.getCleanButton();
            if (TextUtils.isEmpty(keyword)) {
                List<StockBrief> historyResult = SearchHistoryStockStore.getInstance().getAllItems();
                List<SearchResultItem> items = Stream.of(historyResult)
                        .filter(it -> it.stockClassType != StockBrief.STOCK_CLASS_TYPE_GOVERNMENT_LOAN && it.stockClassType != StockBrief.STOCK_CLASS_TYPE_SPEC)
                        .map(it -> new SearchResultItem(it, keyword)).collect(Collectors.toList());
                updateResultList(items);
                v_setGone(cleanButton);
                v_setVisible(cleanAllLabel);
                if (items.isEmpty()) {
                    v_setText(cleanAllLabel, "您还没有搜索历史");
                } else {
                    v_setText(cleanAllLabel, "清空搜索历史");

                    v_setClick(cleanAllLabel, () -> {
                        Stream.of(historyResult)
                                .forEach(item -> SearchHistoryStockStore.getInstance().deleteItem(item));
                        searchByKeyword(keyword);
                    });
                }
                return;
            }
            v_setVisible(cleanButton);
            v_setGone(cleanAllLabel);
            v_setVisible(mLoadingSection);

            consumeEventMR(StockController.searchStockByKeyword(keyword))
                    .setTag("search_by_keywrod")
                    .onNextStart(response -> {
                        if (isSuccess(response)) {
                            List<SearchResultItem> items = Stream.of(response.data)
                                    .filter(it -> it.stockClassType != StockBrief.STOCK_CLASS_TYPE_GOVERNMENT_LOAN && it.stockClassType != StockBrief.STOCK_CLASS_TYPE_SPEC)
                                    .map(it -> new SearchResultItem(it, keyword)).collect(Collectors.toList());
                            updateResultList(items);
                        } else {
                            updateResultList(Collections.emptyList());
                        }
                        changeVisibleSection(TYPE_CONTENT);
                    })
                    .done();

        }

        private void updateResultList(List<SearchResultItem> items) {
            RecyclerView recyclerView = v_findView(this, R.id.recyclerView);
            if (recyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<SearchResultItem> adapter = getSimpleAdapter(recyclerView);
                adapter.resetItems(items);
            } else {
                SimpleRecyclerViewAdapter<SearchResultItem> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                        .onCreateItemView((parent, viewType) -> LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_select_stock, parent, false))
                        .onCreateViewHolder(builder -> {
                            builder.bindChildWithTag("nameAndCode", R.id.text1)
                                    .configureView((item, pos) -> {
                                        v_setText(builder.getChildWithTag("nameAndCode"), item.nameAndCode);
                                    });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            v_setClick(holder.itemView, v -> {
                                SearchResultItem item = ad.getItem(holder.getAdapterPosition());
                                SearchHistoryStockStore.getInstance().appendItem(item.raw);
                                SELECT_STOCK_SUBJECT.onNext(item.raw);
                                hideKeyboardFromWindow(this);
                                goBack(this);
                            });
                        })
                        .create();
                recyclerView.setAdapter(adapter);
            }
        }
    }

    private static class SearchResultItem {
        public StockBrief raw;
        public String stockId;
        public CharSequence nameAndCode;


        public SearchResultItem(StockBrief raw, String keyword) {
            this.raw = raw;
            this.stockId = safeGet(() -> raw.index, "");
            this.nameAndCode = concat(safeGet(() -> raw.name, ""), setFontSize(formatStockCode(safeGet(() -> raw.code, ""), keyword), sp2px(10)));
        }
    }

    public static class StockTradePageOtherFragment extends SimpleFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_stock_trade_page_other, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            v_setClick(this, R.id.cell_today_deal_order, v -> {
                int dataSource = StockOrderListFragment.DATA_SOURCE_TODAY_DEAL;
                int fundID = getTradeContext(this).fundID;
                showActivity(this, an_StockOrderListRealPage(dataSource, fundID));
            });
            v_setClick(this, R.id.cell_today_order, v -> {
                int dataSource = StockOrderListFragment.DATA_SOURCE_TODAY;
                int fundID = getTradeContext(this).fundID;
                showActivity(this, an_StockOrderListRealPage(dataSource, fundID));
            });
            v_setClick(this, R.id.cell_history_deal_order, v -> {
                int dataSource = StockOrderListFragment.DATA_SOURCE_HISTORY_DEAL;
                int fundID = getTradeContext(this).fundID;
                showActivity(this, an_StockOrderListRealPage(dataSource, fundID));
            });
        }
    }

    public static class StockAnalyseFragment extends SimpleFragment {
        private View mHeaderSection;
        private int mUserID;
        private String mStockId;
        private String mRange;

        public StockAnalyseFragment init(int userID, String stockId, String range) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_USER_ID_INT, userID);
            arguments.putString(KEY_STOCK_ID_STRING, stockId);
            arguments.putString(KEY_RANGE_STRING, range);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mUserID = Optional.of(getArguments().getInt(KEY_USER_ID_INT)).or(0);
            mStockId = Optional.of(getArguments().getString(KEY_STOCK_ID_STRING)).or("");
            mRange = Optional.of(getArguments().getString(KEY_RANGE_STRING)).or("");
            return inflater.inflate(R.layout.frag_stock_analyse, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            mRefreshLayout.setOnRefreshListener(() -> fetchData(false));

            ListView listView = v_findView(mContentSection, R.id.listView);
            mHeaderSection = LayoutInflater.from(listView.getContext()).inflate(R.layout.header_stock_analyse, listView, false);
            listView.addHeaderView(mHeaderSection);

            fetchData(true);
        }

        private void fetchData(boolean reload) {
            int userID = mUserID;
            String stockID = mStockId;
            String range = mRange;
            consumeEventMRUpdateUI(StockController.fetchUserPositionDetailSimulation(userID, stockID, range), reload)
                    .onNextSuccess(response -> {
                        StockPosition raw = response.data;
                        StockAnalyseHeaderVM info = new StockAnalyseHeaderVM(raw);
                        updateTitle(info.isCurrentHold ? "盈亏分析 (当前持仓)" : "盈亏分析 (历史持仓)");
                        updateHeaderView(info);
                        List<HistoryOrderVM> items = Stream.of(raw.orderList).map(it -> new HistoryOrderVM(it)).collect(Collectors.toList());
                        updateStockRecordSection(items);
                        v_setVisible(mContentSection);
                    })
                    .done();
        }

        private static int mCurrentIncomeType = 0;

        private void updateHeaderView(StockAnalyseHeaderVM info) {
            View parent = mHeaderSection;
            v_setText(parent, R.id.label_stock_name_and_code, info.nameAndCode);
            v_setText(parent, R.id.label_stock_price, info.price);
            v_setClick(mHeaderSection, R.id.section_current_stock, v -> showActivity(this, an_QuotationDetailPage(info.stockID)));

            if (info.isCurrentHold) {
                v_setVisible(parent, R.id.section_float);
                v_setGone(parent, R.id.section_final);
            } else {
                v_setGone(parent, R.id.section_float);
                v_setVisible(parent, R.id.section_final);
            }

            if (info.isCurrentHold) {
                parent = v_findView(parent, R.id.section_float);
                v_setText(parent, R.id.label_stock_income, mCurrentIncomeType == INCOME_TYPE_TOTAL ? info.currentTotalIncomeAndIncomeRatio : info.currentTodayIncomeAndIncomeRatio);
                v_setText(parent, R.id.label_cost, info.cost);
                v_setText(parent, R.id.label_hold_count, info.holdCount);
                v_setText(parent, R.id.label_value, info.value);
                v_setText(parent, R.id.label_position, info.position);
                v_setText(parent, R.id.label_sellable_count, info.sellableCount);
                v_setText(parent, R.id.label_sellCount, info.totalSellCount);
                v_setText(parent, R.id.label_income_type, mCurrentIncomeType == INCOME_TYPE_TODAY ? "今日盈亏" : "浮动盈亏");
                v_setClick(parent, v -> {
                    int incomeType = mCurrentIncomeType;
                    if (incomeType == INCOME_TYPE_TOTAL)
                        mCurrentIncomeType = INCOME_TYPE_TODAY;
                    else if (incomeType == INCOME_TYPE_TODAY)
                        mCurrentIncomeType = INCOME_TYPE_TOTAL;
                    updateHeaderView(info);
                });
            } else {
                parent = v_findView(parent, R.id.section_final);
                v_setText(parent, R.id.label_stock_income, info.currentTotalIncomeAndIncomeRatio);
                v_setText(parent, R.id.label_avg_buy_price, info.currentTotalIncomeAndIncomeRatio);
                v_setText(parent, R.id.label_avg_buy_price, info.avgBuyPrice);
                v_setText(parent, R.id.label_avg_sell_price, info.avgSellPrice);
                v_setText(parent, R.id.label_accumulate_buy_count, info.accumulateBuyCount);
            }

            v_setText(mHeaderSection, R.id.trade_time, info.holdTimeStr);
        }

        private void updateStockRecordSection(List<HistoryOrderVM> items) {
            v_findView(mHeaderSection, R.id.header_list).setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);

            ListView listView = v_findView(mContentSection, R.id.listView);
            if (items.isEmpty()) {
                listView.setAdapter(null);
            } else {
                SimpleListViewAdapter<HistoryOrderVM> adapter = new SimpleListViewAdapter.Builder<>(items)
                        .onCreateItemView(R.layout.cell_stock_order_history)
                        .onCreateViewHolder(builder -> {
                            ChildBinder binder = ChildBinders.createWithBuilder(builder);
                            bindHistoryOrderCell(binder);
                            builder.configureView((item, pos) -> {
                                Func0<HistoryOrderVM> itemGetter = () -> item;
                                configureHistoryOrderCell(binder, itemGetter);
                            });
                            return builder.create();
                        })
                        .create();
                listView.setAdapter(adapter);
                int headerCount = 1;
                int startOffset = -headerCount;
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    int revisePosition = position + startOffset;
                    if (inRange(revisePosition, 0, items.size() - 1)) {
                        HistoryOrderVM item = items.get(revisePosition);
                        showActivity(StockAnalyseFragment.this, an_QuotationDetailPage(item.stockID));
                    }
                });
            }
        }
    }

    private static void bindHistoryOrderCell(ChildBinder binder) {
        binder.bindChildWithTag("indicator", R.id.indicator)
                .bindChildWithTag("stockNameAndCode", R.id.text1)
                .bindChildWithTag("countAndBuyPrice", R.id.text2)
                .bindChildWithTag("dateAndTime", R.id.text3);
    }

    private static void configureHistoryOrderCell(ChildBinder binder, Func0<HistoryOrderVM> itemGetter) {
        HistoryOrderVM item = itemGetter.call();
        binder.<BuySellStockIndicator>getChildWithTag("indicator").setState(item.actionType);
        v_setText(binder.getChildWithTag("stockNameAndCode"), item.stockNameAndCode);
        v_setText(binder.getChildWithTag("countAndBuyPrice"), item.countAndBuyPrice);
        v_setText(binder.getChildWithTag("dateAndTime"), item.dateAndTime);
    }

    private static class StockAnalyseHeaderVM {
        public String stockID;
        public boolean isCurrentHold;

        public CharSequence nameAndCode;
        public CharSequence price;

        public CharSequence currentTodayIncomeAndIncomeRatio;
        public CharSequence currentTotalIncomeAndIncomeRatio;
        public CharSequence cost;
        public CharSequence holdCount;
        public CharSequence value;
        public CharSequence position;
        public CharSequence sellableCount;
        public CharSequence totalSellCount;

        public CharSequence finalIncomeAndIncomeRatio;
        public CharSequence avgBuyPrice;
        public CharSequence avgSellPrice;
        public CharSequence accumulateBuyCount;

        public CharSequence holdTimeStr;

        public StockAnalyseHeaderVM(StockPosition raw) {
            StockSimple stock = raw.stock;

            this.stockID = safeGet(() -> raw.stock.index, "");
            this.isCurrentHold = safeGet(() -> raw.holdAmount > 0, false);
            this.nameAndCode = concat(stock.name, setFontSize(setColor(stock.code, TEXT_GREY_COLOR), sp2px(10)));
            this.price = formatMoney(raw.currentPrice, false, 2);
            this.currentTodayIncomeAndIncomeRatio = setColor(concatNoBreak(formatMoney(raw.todayIncome, true, 2), "   ", setFontSize(formatRatio(raw.todayIncomeRatio, true, 2), sp2px(14))), getIncomeTextColor(raw.todayIncome));
            this.currentTotalIncomeAndIncomeRatio = setColor(concatNoBreak(formatMoney(raw.income, true, 2), "   ", setFontSize(formatRatio(raw.incomeRatio, true, 2), sp2px(14))), getIncomeTextColor(raw.income));
            this.cost = generateNumberTextOfTable("成本", raw.holdPrice, 2);
            this.holdCount = generateNumberTextOfTable("持有", raw.holdAmount, 0);
            this.value = generateNumberTextOfTable("市值", raw.holdCapital, 2);
            this.position = generateRatioTextOfTable("仓位", raw.curPosition, 2);
            this.sellableCount = generateNumberTextOfTable("可卖", raw.validAmount, 0);
            this.totalSellCount = generateNumberTextOfTable("累计卖出数量", raw.totalSellAmount, 0);

            this.finalIncomeAndIncomeRatio = currentTodayIncomeAndIncomeRatio;
            this.avgBuyPrice = generateNumberTextOfTable("买入成本", raw.holdPrice, 2);
            this.avgSellPrice = generateNumberTextOfTable("卖出成本", raw.sellPrice, 2);
            this.accumulateBuyCount = generateNumberTextOfTable("累计买入数量", raw.totalBuyAmount, 0);

            this.holdTimeStr = "交易记录 (" + raw.holdTimeStr + ")";
        }

        private CharSequence generateNumberTextOfTable(String title, Double value, int scale) {
            return concat(title, setFontSize(setColor(formatMoney(value, false, scale), TEXT_BLACK_COLOR), sp2px(14)));
        }

        private CharSequence generateRatioTextOfTable(String title, Double value, int scale) {
            return concat(title, setFontSize(setColor(formatRatio(value, false, scale), TEXT_BLACK_COLOR), sp2px(14)));
        }
    }

    public static class StockSettingFragment extends SimpleFragment {
        private int mUserID;
        private String mStockID;
        private String mRange;
        private TextWatcher mStopEarnTextWatcher = null;
        private TextWatcher mStopLossTextWatcher = null;

        public StockSettingFragment init(int userID, String stockID, String range) {
            Bundle arguments = new Bundle();
            arguments.putInt(CommonProxyActivity.KEY_USER_ID_INT, userID);
            arguments.putString(CommonProxyActivity.KEY_STOCK_ID_STRING, stockID);
            arguments.putString(CommonProxyActivity.KEY_RANGE_STRING, range);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mUserID = getArguments().getInt(CommonProxyActivity.KEY_USER_ID_INT);
            mStockID = getArguments().getString(CommonProxyActivity.KEY_STOCK_ID_STRING);
            mRange = getArguments().getString(CommonProxyActivity.KEY_RANGE_STRING);
            return inflater.inflate(R.layout.frag_stock_setting, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupBackButton(this, findToolbar(this));
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);

            v_setClick(findToolbar(this), R.id.btn_done, v -> {
                performGoBack(this);
            });
            v_setClick(mReloadSection, v -> {
                fetchData();
            });

            fetchData();
        }

        private void fetchData() {
            v_setGone(findToolbar(this), R.id.btn_done);
            consumeEventMRUpdateUI(StockController.fetchUserPositionDetailSimulation(mUserID, mStockID, mRange), true)
                    .onNextSuccess(response -> {
                        StockSettingVM vm = new StockSettingVM(response.data);
                        resetContentSection(vm);
                        v_setVisible(findToolbar(this), R.id.btn_done);
                    })
                    .done();
        }

        private void resetContentSection(StockSettingVM vm) {
            View stockBriefGroup = v_findView(mContentSection, R.id.group_stock_brief);
            opt(stockBriefGroup)
                    .consume(parent -> {
                        v_setText(parent, R.id.label_stock_name_and_code, vm.stockNameAndCode);
                        v_setText(parent, R.id.label_stock_price, vm.stockPrice);
                        v_setText(parent, R.id.label_stock_income, vm.stockIncome);
                    });

            ToggleCell stopEarnCell = v_findView(mContentSection, R.id.cell_stop_earn);
            stopEarnCell.setListener(isOn -> {
                hideKeyboardFromWindow(this);
                v_setVisibility(mContentSection, R.id.group_stop_earn, isOn ? View.VISIBLE : View.GONE);
            });

            ToggleCell stopLossCell = v_findView(mContentSection, R.id.cell_stop_loss);
            stopLossCell.setListener(isOn -> {
                hideKeyboardFromWindow(this);
                v_setVisibility(mContentSection, R.id.group_stop_loss, isOn ? View.VISIBLE : View.GONE);
            });

            EditText stopEarnField = v_findView(mContentSection, R.id.field_earn_ratio);
            View stopEarnTag = v_findView(mContentSection, R.id.tag_earn_ratio);
            stopEarnField.removeTextChangedListener(mStopEarnTextWatcher);
            mStopEarnTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    v_setVisibility(stopEarnTag, s.length() > 0 ? View.VISIBLE : View.GONE);
                }
            };
            stopEarnField.addTextChangedListener(mStopEarnTextWatcher);

            EditText stopLossField = v_findView(mContentSection, R.id.field_loss_ratio);
            View stopLossTag = v_findView(mContentSection, R.id.tag_loss_ratio);
            stopLossField.removeTextChangedListener(mStopLossTextWatcher);
            mStopLossTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    v_setVisibility(stopLossTag, s.length() > 0 ? View.VISIBLE : View.GONE);
                }
            };
            stopEarnField.addTextChangedListener(mStopLossTextWatcher);
        }
    }

    private static class StockSettingVM {
        CharSequence stockNameAndCode;
        CharSequence stockPrice;
        CharSequence stockIncome;

        public StockSettingVM(StockPosition raw) {
            this.stockNameAndCode = safeGet(() -> {
                CharSequence name = raw.stock.name;
                CharSequence code = setFontSize(setColor(raw.stock.code, TEXT_GREY_COLOR), sp2px(10));
                return concat(name, code);
            }, PlaceHolder.NULL_VALUE);
            this.stockPrice = safeGet(() -> {
                CharSequence currentPrice = formatMoney(raw.currentPrice, false, 0, 2);
                CharSequence buyPrice = setFontSize(setColor(formatMoney(raw.holdPrice, false, 0, 2), TEXT_GREY_COLOR), sp2px(10));
                return concat(currentPrice, buyPrice);
            }, PlaceHolder.NULL_VALUE);
            this.stockIncome = safeGet(() -> {
                CharSequence incomeRatio = formatRatio(raw.incomeRatio, true, 0, 2);
                CharSequence income = setFontSize(formatMoney(raw.income, true, 0, 2), sp2px(10));
                return setColor(concat(incomeRatio, income), getIncomeTextColor(raw.income));
            }, PlaceHolder.NULL_VALUE);
        }
    }

    public static class StockOrderListFragment extends SimpleFragment {
        public static final int DATA_SOURCE_TODAY = 1;
        public static final int DATA_SOURCE_TODAY_DEAL = 2;
        public static final int DATA_SOURCE_HISTORY_DEAL = 3;

        private int mDataSource;
        private int mFundID;

        private boolean misLoadingMore = false;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mDataSource = getArguments().getInt(KEY_ORDER_DATA_SOURCE_INT);
            mFundID = getArguments().getInt(KEY_FUND_ID_INT);
            return inflater.inflate(R.layout.frag_stock_order_list, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupBackButton(this, findToolbar(this));
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            v_setClick(mReloadSection, v -> fetchData(true, false));

            if (mDataSource == DATA_SOURCE_TODAY) {
                updateTitle("当日委托");
            } else if (mDataSource == DATA_SOURCE_TODAY_DEAL) {
                updateTitle("当日成交");
            } else if (mDataSource == DATA_SOURCE_HISTORY_DEAL) {
                updateTitle("历史成交");
            }

            fetchData(true, true);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            misLoadingMore = false;
        }

        @SuppressWarnings("unchecked")
        private void fetchData(boolean reload, boolean allowCache) {
            Observable<MResultsInfo<CommandPageArray<Order>>> observable = null;
            if (mDataSource == DATA_SOURCE_TODAY) {
                observable = (Observable) StockController.fetchTodayOrderListReal(mFundID);
            } else if (mDataSource == DATA_SOURCE_TODAY_DEAL) {
                observable = (Observable) StockController.fetchTodayDealOrderListReal(mFundID);
            } else if (mDataSource == DATA_SOURCE_HISTORY_DEAL) {
                observable = (Observable) StockController.fetchHistoryOrderListReal(mFundID);
            }
            if (observable != null) {
                consumeEventMRUpdateUI(observable, reload)
                        .setTag("fetch_data")
                        .onNextSuccess(response -> {
                            resetContentSection(response.data);
                        })
                        .done();
            }
        }

        private void resetContentSection(CommandPageArray<Order> pageArray) {

            consumeEvent(NotificationCenter.freshStockInfoSubject)
                    .setTag("refresh_stock_info")
                    .onNextFinish(ignored -> {
                        resetContentSection(pageArray);
                    })
                    .done();


            boolean isPendingOrder = IntExtension.anyMatch(mDataSource, DATA_SOURCE_TODAY_DEAL);
            v_setVisibility(mContentSection, R.id.header_history_order, isPendingOrder ? View.GONE : View.VISIBLE);
            v_setVisibility(mContentSection, R.id.header_pending_order, isPendingOrder ? View.VISIBLE : View.GONE);

            if (pageArray.data().isEmpty()) {
                setOnSwipeRefreshListener(() -> {
                    setSwipeRefreshing(false);
                });
                changeVisibleSection(TYPE_EMPTY);
                return;
            }

            setOnSwipeRefreshListener(() -> {
                consumeEventMR(PageArrayHelper.getPreviousPage(pageArray))
                        .setTag("get_previous")
                        .onNextSuccess(response -> {
                            resetContentSection(pageArray);
                        })
                        .onNextFinish(ignored -> {
                            setSwipeRefreshing(false);
                        })
                        .done();
            });

            RecyclerView recyclerView = v_findView(mContentSection, R.id.recyclerView);

            if (recyclerView.getAdapter() == null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (!misLoadingMore &&
                                PageArrayHelper.hasMoreData(pageArray) &&
                                isScrollToBottom(recyclerView)) {
                            misLoadingMore = true;
                            consumeEventMR(PageArrayHelper.getNextPage(pageArray))
                                    .setTag("get_next")
                                    .onNextSuccess(response -> {
                                        resetContentSection(pageArray);
                                    })
                                    .onNextFinish(ignored -> {
                                        misLoadingMore = false;
                                    })
                                    .done();
                        }
                    }
                });
            }

            if (isPendingOrder) {
                List<PendingOrderVM> items = Stream.of(opt(pageArray.data()).or(Collections.emptyList()))
                        .map(it -> new PendingOrderVM(it))
                        .collect(Collectors.toList());
                if (recyclerView.getAdapter() != null) {
                    SimpleRecyclerViewAdapter<PendingOrderVM> adapter = getSimpleAdapter(recyclerView);
                    adapter.resetItems(items);
                } else {
                    SimpleRecyclerViewAdapter<PendingOrderVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                            .onCreateItemView(R.layout.cell_stock_order_pending)
                            .onCreateViewHolder(builder -> {
                                ChildBinder binder = ChildBinders.createWithBuilder(builder);
                                bindPendingOrderCell(binder);
                                builder.configureView((item, pos) -> {
                                    Func0<PendingOrderVM> itemGetter = () -> item;
                                    configurePendingOrderCell(binder, itemGetter);
                                });
                                return builder.create();
                            })
                            .create();
                    recyclerView.setAdapter(adapter);
                }

            } else {
                List<HistoryOrderVM> items = Stream.of(opt(pageArray.data()).or(Collections.emptyList()))
                        .map(it -> new HistoryOrderVM(it))
                        .collect(Collectors.toList());
                if (recyclerView.getAdapter() != null) {
                    SimpleRecyclerViewAdapter<HistoryOrderVM> adapter = getSimpleAdapter(recyclerView);
                    adapter.resetItems(items);
                } else {
                    SimpleRecyclerViewAdapter<HistoryOrderVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                            .onCreateItemView(R.layout.cell_stock_order_history)
                            .onCreateViewHolder(builder -> {
                                ChildBinder binder = ChildBinders.createWithBuilder(builder);
                                bindHistoryOrderCell(binder);
                                builder.configureView((item, pos) -> {
                                    Func0<HistoryOrderVM> itemGetter = () -> item;
                                    configureHistoryOrderCell(binder, itemGetter);
                                });
                                return builder.create();
                            })
                            .onViewHolderCreated((ad, holder) -> {
                                v_setClick(holder.itemView, v -> {
                                    HistoryOrderVM item = ad.getItem(holder.getAdapterPosition());
                                    showActivity(getActivity(), an_QuotationDetailPage(item.stockID));
                                });
                            })
                            .create();
                    recyclerView.setAdapter(adapter);
                }
            }
        }
    }
}
