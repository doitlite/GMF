package com.goldmf.GMFund.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.controller.StockChartFragments.ChartLongPressInfo;
import com.goldmf.GMFund.controller.StockChartFragments.KLineChartRateInfo;
import com.goldmf.GMFund.controller.StockChartFragments.StockChartDetailFragment;
import com.goldmf.GMFund.controller.business.StockController;
import com.goldmf.GMFund.controller.dialog.GMFBottomSheet;
import com.goldmf.GMFund.controller.dialog.ShareDialog.SharePlatform;
import com.goldmf.GMFund.controller.internal.StockFeedHelper.StockFeedVM;
import com.goldmf.GMFund.controller.protocol.UMShareHandlerProtocol;
import com.goldmf.GMFund.extension.FileExtension;
import com.goldmf.GMFund.extension.IntExtension;
import com.goldmf.GMFund.extension.UIControllerExtension;
import com.goldmf.GMFund.extension.ViewExtension;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.stock.SearchHistoryStockStore;
import com.goldmf.GMFund.manager.stock.SimulationStockManager;
import com.goldmf.GMFund.manager.trader.TraderManager;
import com.goldmf.GMFund.model.FeedOrder;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.FundBrief.Fund_Status;
import com.goldmf.GMFund.model.LineData;
import com.goldmf.GMFund.model.LineData.KLineData;
import com.goldmf.GMFund.model.LineData.TLineData;
import com.goldmf.GMFund.model.Stock;
import com.goldmf.GMFund.model.Stock.Quotation;
import com.goldmf.GMFund.model.StockData.StockKline;
import com.goldmf.GMFund.model.StockData.StockTline;
import com.goldmf.GMFund.model.StockInfo.StockBrief;
import com.goldmf.GMFund.model.StockPosition;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.PageArrayHelper;
import com.goldmf.GMFund.util.FrescoHelper;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.util.ZxingUtil;
import com.goldmf.GMFund.widget.BuySellStockIndicator;
import com.goldmf.GMFund.widget.ChartContainerOnTouchListener;
import com.goldmf.GMFund.widget.ChartController;
import com.goldmf.GMFund.widget.ChartController.ChartInfo;
import com.goldmf.GMFund.widget.ChartController.EnvDataPager;
import com.goldmf.GMFund.widget.DayKLineChartRender;
import com.goldmf.GMFund.widget.DrawLineView;
import com.goldmf.GMFund.widget.FiveDayChartRender;
import com.goldmf.GMFund.widget.FiveOrderCell;
import com.goldmf.GMFund.widget.InputAccessoryView;
import com.goldmf.GMFund.widget.MonthKLineChartRender;
import com.goldmf.GMFund.widget.ObservableScrollView;
import com.goldmf.GMFund.widget.TimesChartRender;
import com.goldmf.GMFund.widget.UserAvatarView;
import com.goldmf.GMFund.widget.WeekKLineChartRender;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.orhanobut.logger.Logger;
import com.yale.ui.support.AdvanceSwipeRefreshLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.yale.infinitychartview.lib.ChartViewContainer;
import io.yale.infinitychartview.lib.DataSourceDelegate;
import io.yale.infinitychartview.lib.layout.EndToStartLayoutDelegate;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action3;
import rx.functions.Func0;
import rx.subjects.PublishSubject;
import yale.extension.common.Optional;
import yale.extension.common.shape.RoundCornerShape;

import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_STOCK_ID_STRING;
import static com.goldmf.GMFund.controller.FragmentStackActivity.pushFragment;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_CHART_SMALL;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_FIVE_DAY_TLINE;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_KLINE;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_KLINE_DAY;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_KLINE_MONTH;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_KLINE_WEEK;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_TIMES_TLINE;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_TLINE_DAY;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_TLINE_FIVE_DAY;
import static com.goldmf.GMFund.controller.StockChartFragments.StockChartDetailFragment.sRefreshChartValueSubject;
import static com.goldmf.GMFund.controller.business.StockController.addFocusStock;
import static com.goldmf.GMFund.controller.business.StockController.fetchStockDetailInfo;
import static com.goldmf.GMFund.controller.business.StockController.fetchStockKLineData;
import static com.goldmf.GMFund.controller.business.StockController.fetchStockTLineData;
import static com.goldmf.GMFund.controller.business.StockController.searchStockByKeyword;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_FundTradePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_LoginPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_QuotationDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockAnalysePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockChartDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockTradePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_UserDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_SEP_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.YELLOW_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getIncomeBgColor;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.MResultExtension.zipToList;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.getScreenSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_addTextChangedListener;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_isVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.model.LineData.GMFStockAnswer_Authority_No;
import static com.goldmf.GMFund.model.LineData.KLineData.Spec_Type_MACD;
import static com.goldmf.GMFund.model.LineData.KLineData.Spec_Type_NONE;
import static com.goldmf.GMFund.model.LineData.TLineData.Tline_Type_5Day;
import static com.goldmf.GMFund.model.LineData.TLineData.Tline_Type_Day;
import static com.goldmf.GMFund.model.StockInfo.StockBrief.STOCK_CLASS_TYPE_GOVERNMENT_LOAN;
import static com.goldmf.GMFund.model.StockInfo.StockBrief.STOCK_CLASS_TYPE_SPEC;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.getData;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.getPreviousPage;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.hasMoreData;
import static com.goldmf.GMFund.util.FormatUtil.formatBigNumber;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatRatio;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;
import static com.goldmf.GMFund.util.FormatUtil.formatStockCode;

/**
 * Created by Evan on 16/2/20 上午10:26.
 */
public class QuotationFragments {

    private QuotationFragments() {
    }

    public static class StockSearchFragment extends SimpleFragment {

        private PublishSubject<String> mStockSearchSubject = PublishSubject.create();
        private ViewTreeObserver.OnGlobalLayoutListener onLayoutListener;
        private EditText mSearchField;
        private TextView mCleanHistory;
        private ImageView mCleanTextImage;
        private LinearLayout mContainer;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_stock_search, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, YELLOW_COLOR);
            UIControllerExtension.setupBackBlackArrow(this, findToolbar(this));

            mSearchField = v_findView(this, R.id.field_search);
            mCleanTextImage = v_findView(this, R.id.img_clean);
            mContainer = v_findView(this, android.R.id.list);
            mCleanHistory = v_findView(this, R.id.label_clean_history);
            v_setGone(this, R.id.accessoryView);
            resetAccessoryView(getActivity(), v_findView(view, R.id.accessoryView), mSearchField);

            v_setClick(mReloadSection, v -> {
                v_setVisible(v);
                v_setVisible(mLoadingSection);
                performStockSearchList(mSearchField.getText().toString());
            });
            v_setClick(this, R.id.img_clean, v -> {
                if (mSearchField != null) {
                    mSearchField.setText(null);
                    v_setGone(mCleanTextImage);
                }
            });
            searchByKeyword(mSearchField.getText().toString());
            v_addTextChangedListener(mSearchField, editable -> mStockSearchSubject.onNext(editable.toString()));
            consumeEvent(mStockSearchSubject
                    .debounce(300, TimeUnit.MILLISECONDS))
                    .onNextFinish(it -> searchByKeyword(it))
                    .done();

            {
                if (onLayoutListener != null) {
                    mContentSection.getViewTreeObserver().removeGlobalOnLayoutListener(onLayoutListener);
                }
                View accessoryView = v_findView(this, R.id.accessoryView);
                onLayoutListener = () -> {
                    int rootDiff = view.getRootView().getHeight() - view.getHeight();
                    boolean isKeyboardShow = rootDiff > 150;
                    accessoryView.setVisibility(isKeyboardShow ? View.VISIBLE : View.GONE);
                };
                mContentSection.getViewTreeObserver().addOnGlobalLayoutListener(onLayoutListener);
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            if (onLayoutListener != null) {
                mContentSection.getViewTreeObserver().removeGlobalOnLayoutListener(onLayoutListener);
                onLayoutListener = null;
            }

            SearchHistoryStockStore.getInstance().archiveNow();
        }

        @SuppressWarnings({"Convert2streamapi", "CodeBlock2Expr"})
        private static void resetAccessoryView(Context context, InputAccessoryView accessoryView, EditText editText) {
            List<View> childs = Stream.of(new String[]{"60", "00", "30", "600", "000", "002"})
                    .map(text -> {
                        TextView textView = new TextView(context);
                        textView.setText(text);
                        textView.setTextSize(18f);
                        textView.setTextColor(TEXT_WHITE_COLOR);
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
            for (View child : childs) {
                contentView.addView(child);
            }

            accessoryView.removeAllViewsInLayout();
            accessoryView.addView(contentView);
        }

        private void searchByKeyword(String keyword) {
            if (TextUtils.isEmpty(keyword)) {
                performStockSearchHistoryList();
                return;
            }
            performStockSearchList(keyword);
        }

        private void performStockSearchHistoryList() {
            v_setGone(mCleanTextImage);
            mContainer.removeAllViews();
            List<StockBrief> cacheData = SearchHistoryStockStore.getInstance().getAllItems();
            if (cacheData == null || cacheData.isEmpty()) {
                v_setText(mCleanHistory, "您还没有搜索历史");
            } else {
                setupStockSearchHistoryList(cacheData);
                v_setText(mCleanHistory, "清空搜索历史");
                v_setClick(mCleanHistory, () -> {
                    Observable.from(cacheData)
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext(item -> performDeleteSearchStockItem(item))
                            .doOnCompleted(() -> performStockSearchHistoryList())
                            .subscribe();
                });
            }
        }

        private void setupStockSearchHistoryList(List<StockBrief> data) {
            Stream.of(data)
                    .map(item -> {
                        View cell = createStockHistoryCell(mContainer, item);
                        v_setClick(cell, () -> {
                            showActivity(this, an_QuotationDetailPage(item.index));
                            performAddSearchStockItem(item);
                        });
                        v_setClick(cell, R.id.label_delete, () -> {
                            performDeleteSearchStockItem(item);
                            performStockSearchHistoryList();
                        });
                        return cell;
                    })
                    .filter(v -> v != null)
                    .forEach(child -> mContainer.addView(child));
        }

        private View createStockHistoryCell(ViewGroup container, StockBrief item) {
            Context ctx = container.getContext();
            View cell = LayoutInflater.from(ctx).inflate(R.layout.cell_stock_search_history, container, false);
            v_setText(cell, R.id.label_stock_name, item.name);
            v_setText(cell, R.id.label_stock_code, item.code);
            return cell;
        }

        private void performAddSearchStockItem(StockBrief item) {
            SearchHistoryStockStore.getInstance().appendItem(item);
        }

        private void performDeleteSearchStockItem(StockBrief item) {
            SearchHistoryStockStore.getInstance().deleteItem(item);
        }

        private void performStockSearchList(String text) {
            v_setVisible(mCleanTextImage);
            v_setVisible(mLoadingSection);
            mContainer.removeAllViews();
            consumeEventMR(searchStockByKeyword(text))
                    .setTag("keyword")
                    .onNextFinish(callback -> {
                        if (isSuccess(callback)) {
                            List<StockBrief> data = callback.data;

                            if (data == null || data.isEmpty()) {
                                v_setText(mCleanHistory, "没有找到相关股票");
                            } else {
                                setupStockSearchList(data, text);
                            }
                            v_setVisible(mContentSection);
                        } else {
                            v_setVisible(mLoadingSection);
                        }
                        v_setGone(mLoadingSection);
                    })
                    .done();
        }

        private void setupStockSearchList(List<StockBrief> data, String text) {
            v_setGone(mCleanHistory);
            Stream.of(data)
                    .map(item -> {
                        View cell = createStockSearchCell(mContainer, item, text);
                        Button focusButton = v_findView(cell, R.id.btn_stock_focus);
                        List<StockBrief> followList = SimulationStockManager.getInstance().followStockList;
                        if (followList == null || !followList.contains(item)) {
                            v_setText(focusButton, "+ 自选");
                            v_setClick(focusButton, v -> performAddFollowStock(focusButton, item));
                        } else {
                            focusButton.setEnabled(false);
                            v_setText(focusButton, "已加");
                        }
                        v_setClick(cell, v -> {
                            showActivity(this, an_QuotationDetailPage(item.index));
                            SearchHistoryStockStore.getInstance().appendItem(item);
                        });
                        return cell;
                    })
                    .filter(v -> v != null)
                    .forEach(child -> mContainer.addView(child));
        }

        private View createStockSearchCell(ViewGroup container, StockBrief item, String text) {
            Context ctx = container.getContext();
            View cell = LayoutInflater.from(ctx).inflate(R.layout.cell_stock_search, container, false);
            v_setText(cell, R.id.label_stock_name, item.name);
            v_setText(cell, R.id.label_stock_code, formatStockCode(item.code, text));
            return cell;
        }

        private void performAddFollowStock(Button focusButton, StockBrief item) {
            UmengUtil.stat_click_event(UmengUtil.eEVENTIDClickAddOptional);
            consumeEventMR(StockController.addFocusStock(item))
                    .onNextSuccess(response -> {
                        NotificationCenter.focusStockChangedSubject.onNext(null);
                        v_setText(focusButton, "已加");
                        focusButton.setEnabled(false);
                    })
                    .onNextFail(response -> {
                        showToast(this, getErrorMessage(response));
                    })
                    .done();
        }
    }

    /**
     *  
     * 股票詳情頁
     */
    public static class StockDetailFragment extends SimpleFragment {

        public static final int TYPE_TIMES_TLINE = 0;
        public static final int TYPE_FIVE_DAY_TLINE = 1;
        public static final int TYPE_KLINE = 2;

        public static final int TYPE_CHART_SMALL = 0;
        public static final int TYPE_CHART_BIG = 1;

        public static final int TYPE_TLINE_DAY = 0;
        public static final int TYPE_TLINE_FIVE_DAY = 1;
        public static final int TYPE_KLINE_DAY = 2;
        public static final int TYPE_KLINE_WEEK = 3;
        public static final int TYPE_KLINE_MONTH = 4;

        private static final int RANGE_ALL = StockController.FEED_TYPE_BEGIN;
        private static final int RANGE_BUY = StockController.FEED_TYPE_BUY;
        private static final int RANGE_SELL = StockController.FEED_TYPE_SELL;

        private String mStockId;
        private String[] titles = {"分时", "五日", "日K", "周K", "月K"};
        private int authorityType = LineData.GMFStockAnswer_Authority_No;
        private int specType = Spec_Type_NONE;
        private boolean isFirstEnter = true;
        private AdvanceSwipeRefreshLayout mRefreshLayout;
        private ObservableScrollView mScrollView;

        private CommandPageArray<FeedOrder> mAllTraderStore;
        private boolean mIsLoadingMore = false;
        private TabLayout mTabLayout;
        private Stock mStock;

        private interface CreateFragmentFunc extends Func0<Fragment> {
        }

        private Optional<SimpleFragment> mCurrentFragment = Optional.empty();

        Class[] sClazzList = {
                TimesChartFragment.class,
                FiveDayChartFragment.class,
                DayKLineChartFragment.class,
                WeekKLineChartFragment.class,
                MonthKLineFragment.class
        };

        CreateFragmentFunc[] sActionList = {
                TimesChartFragment::new,
                FiveDayChartFragment::new,
                DayKLineChartFragment::new,
                WeekKLineChartFragment::new,
                MonthKLineFragment::new
        };

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mStockId = getArguments().getString(KEY_STOCK_ID_STRING);
            UmengUtil.stat_click_event(UmengUtil.eEVENTIDOpenStockDetail);
            return inflater.inflate(R.layout.frag_quotation_detail, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            findToolbar(this).setBackgroundColor(STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            changeVisibleSection(TYPE_LOADING);
            mRefreshLayout = v_findView(this, R.id.refreshLayout);
            mScrollView = v_findView(this, R.id.scrollView);
            AdvanceSwipeRefreshLayout.class
                    .cast(mRefreshLayout)
                    .setOnPreInterceptTouchEventDelegate(ev -> mScrollView.getScrollY() > 0
                    );
            setOnSwipeRefreshListener(() -> fetchData(false));

            mTabLayout = v_findView(this, R.id.tabLayout);
            mTabLayout.removeAllTabs();
            for (String title : titles) {
                mTabLayout.addTab(mTabLayout.newTab().setText(title));
            }

            View feedHeader = v_findView(mContentSection, R.id.header_stock_feed);
            v_setClick(feedHeader, R.id.area_range, v -> {
                int range = mLastRange;
                if (range == RANGE_ALL) {
                    mLastRange = RANGE_BUY;
                } else if (range == RANGE_BUY) {
                    mLastRange = RANGE_SELL;
                } else {
                    mLastRange = RANGE_ALL;
                }
                consumeEventMR(StockController.fetchTopTraderOther(mStockId, mLastRange))
                        .setTag("reload")
                        .onNextSuccess(responses -> {
                            mAllTraderStore = responses.data;
                            updateStockFeedSection();
                        })
                        .done();
            });

            Toolbar toolbar = findToolbar(this);
            View headerSection = v_findView(this, R.id.section_header);
            mScrollView.setOnScrollChangedListener((l, t) -> {
                if (t + mScrollView.getHeight() >= mScrollView.getChildAt(0).getHeight() && mAllTraderStore != null && !createStockFeedList().isEmpty()) {
                    fetchMoreData();
                }
                if (mStock != null) {
                    if (t < headerSection.getHeight()) {
                        v_setText(toolbar, R.id.label_extra_title, concatNoBreak(mStock.marketInfo, " ", formatSecond(mStock.lastFreshTime, "MM/dd HH:mm:ss")));
                    } else {
                        v_setText(toolbar, R.id.label_extra_title, concatNoBreak(formatMoney(mStock.last, false, 2), " (", formatMoney(mStock.change, true, 2), " ", formatRatio(mStock.changeRatio, true, 2), ")"));
                    }
                }
            });
            fetchData(true);
        }

        private Bitmap generateShareImage(int statusBarColor, int userID, String avatarURL) {
            View[] views = {
                    findToolbar(this),
                    v_findView(mContentSection, R.id.section_header),
                    (View) v_findView(mContentSection, R.id.tabLayout).getParent(),
                    v_findView(mContentSection, R.id.section_chart)
            };

            View stockDetailLabel = views[1].findViewById(R.id.label_user_stock_detail);
            int[] offsetYs = {
                    0,
                    v_isVisible(stockDetailLabel) ? -stockDetailLabel.getMeasuredHeight() : 0,
                    0,
                    0
            };

            Rect screenSize = getScreenSize(this);
            int contentHeight = Stream.of(views)
                    .map(it -> it.getMeasuredHeight())
                    .reduce((sum, current) -> sum + current)
                    .get() + dp2px(108);
            Bitmap bitmap = Bitmap.createBitmap(screenSize.width(), contentHeight, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            canvas.save();
            canvas.drawColor(getResources().getColor(R.color.gmf_act_bg));
            for (int i = 0; i < views.length; i++) {
                View element = views[i];
                int offsetY = offsetYs[i];
                element.draw(canvas);
                canvas.translate(0, element.getMeasuredHeight() + offsetY);
            }

            Paint paint = new Paint();
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(WHITE_COLOR);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(0, 0, canvas.getWidth(), dp2px(108), paint);

            float preStrokeWidth = paint.getStrokeWidth();
            paint.setStrokeWidth(dp2px(0.5f));
            paint.setColor(LINE_SEP_COLOR);
            canvas.drawLine(0, 0, canvas.getWidth(), 0, paint);
            paint.setStrokeWidth(preStrokeWidth);

            paint.setColor(WHITE_COLOR);
            String linkURL = String.format("https://www.caopanman.com/bounty/accept-invite?invite_uid=%d", userID);
            Logger.e("LinkURL:%s", linkURL);
            Bitmap QRCode = ZxingUtil.generateQRCode(linkURL, dp2px(80), dp2px(80));
            if (QRCode != null) {
                canvas.drawBitmap(QRCode, dp2px(20), dp2px(14), paint);
            }

            Bitmap avatar = FrescoHelper.getBitmapFromDisk(avatarURL);
            if (avatar != null) {
                float scale = (float) dp2px(24) / Math.max(avatar.getWidth(), avatar.getHeight());
                Matrix matrix = new Matrix();

                float translationX = dp2px(20) + (dp2px(80) - avatar.getWidth() * scale) / 2;
                float translationY = dp2px(14) + (dp2px(80) - avatar.getHeight() * scale) / 2;
                matrix.setScale(scale, scale);
                matrix.postTranslate(translationX, translationY);
                canvas.drawBitmap(avatar, matrix, paint);
            }

            Bitmap shareLogo = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_stock_share_logo);
            canvas.drawBitmap(shareLogo, canvas.getWidth() - shareLogo.getWidth() - dp2px(16), (dp2px(108) - shareLogo.getHeight()) / 2, paint);


            Rect textBounds = new Rect();
            paint.setColor(TEXT_BLACK_COLOR);
            paint.setFakeBoldText(true);
            paint.setTextSize(sp2px(14));
            String text1 = "长按二维码注册操盘侠";
            String text2 = "跟对牛人炒股票";
            Paint.FontMetrics metrics = paint.getFontMetrics();
            paint.getTextBounds(text1, 0, text1.length(), textBounds);
            canvas.drawText(text1, dp2px(120), dp2px(25) + textBounds.height() + metrics.leading + metrics.descent, paint);
            canvas.drawText(text2, dp2px(120), dp2px(55) + textBounds.height() + metrics.leading + metrics.descent, paint);

            canvas.restore();
            View toolbar = findToolbar(this);
            View titleLabelParent = (View) toolbar.findViewById(R.id.label_title).getParent();
            paint.setColor(statusBarColor);
            canvas.drawRect(0, 0, titleLabelParent.getLeft(), toolbar.getMeasuredHeight(), paint);
            canvas.drawRect(titleLabelParent.getLeft() + titleLabelParent.getMeasuredWidth(), 0, toolbar.getMeasuredWidth(), toolbar.getMeasuredHeight(), paint);


            return bitmap;
//            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
//            ImageView previewImage = new ImageView(getActivity());
//            previewImage.setImageBitmap(bitmap);
//            wm.addView(previewImage, new WindowManager.LayoutParams());
//            v_setClick(previewImage, it -> {
//                wm.removeViewImmediate(previewImage);
//            });
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mAllTraderStore = null;
            mIsLoadingMore = false;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                Observable<MResults.MResultsInfo<Stock>> observable = fetchStockDetailInfo(false, mStockId)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .delaySubscription(10, TimeUnit.SECONDS)
                        .repeat();
                consumeEventMR(observable)
                        .setTag("reset_refresh_stock_detail")
                        .addToVisible()
                        .onNextFinish(response -> {
                            if (isSuccess(response)) {
                                StockDetailHomeInfo vm = new StockDetailHomeInfo(response.data);
                                updateContent(vm);
                            }
                        })
                        .done();
            }
        }

        private void fetchData(boolean reload) {
            unsubscribeFromMain("fetch_more");

            consumeEventMRUpdateUI(fetchStockDetailInfo(reload, mStockId), reload)
                    .setTag("refresh_stock_detail")
                    .onNextStart(response -> {
                        setSwipeRefreshing(false);
                    })
                    .onNextFinish(response -> {
                        if (isSuccess(response)) {
                            mStock = response.data;
                            StockDetailHomeInfo vm = new StockDetailHomeInfo(mStock);
                            updateContent(vm);
                            changeVisibleSection(TYPE_CONTENT);
                        } else {
                            setReloadSectionTips(getErrorMessage(response));
                            changeVisibleSection(TYPE_RELOAD);
                        }
                    })
                    .done();


            if (mAllTraderStore != null) {
                consumeEvent(getPreviousPage(mAllTraderStore))
                        .setTag("reload")
                        .onNextFinish(response -> {
                            updateStockFeedSection();
                        })
                        .done();
            } else {
                consumeEventMR(StockController.fetchTopTraderOther(mStockId, mLastRange))
                        .setTag("reload")
                        .onNextSuccess(responses -> {
                            mAllTraderStore = responses.data;
                            updateStockFeedSection();
                        })
                        .done();
            }
        }

        private void updateContent(StockDetailHomeInfo data) {
            updateHeaderSection(data);
            updateChartSection(data);
            setupBottomBar(data);
        }

        private void updateHeaderSection(StockDetailHomeInfo data) {
            int color = data.color;
            setStatusBarBackgroundColor(this, color);
            Toolbar toolbar = findToolbar(this);
            toolbar.setBackgroundColor(color);

            v_setText(toolbar, R.id.label_title, data.stockTitle);
            v_setText(toolbar, R.id.label_extra_title, data.stockStatus);
            Button focusButton = v_findView(toolbar, R.id.btn_stock_focus);
            v_setText(focusButton, data.stockFocus);
            v_setVisible(focusButton);
            v_setClick(focusButton, v -> {
                if (data.isFocusStock) {
                    UmengUtil.stat_click_event(UmengUtil.eEVENTIDDeleteOptional);
                    performDeleteFollowStock(focusButton, data);
                } else {
                    UmengUtil.stat_click_event(UmengUtil.eEVENTIDAddOptional);
                    performAddFollowStock(focusButton, data);
                }
            });

            View headerSection = v_findView(this, R.id.section_header);
            headerSection.setBackgroundColor(color);
            {
                v_setText(headerSection, R.id.label_last_price, data.currentPrice);
                v_setText(headerSection, R.id.label_change_price, data.changePrice);
                v_setText(headerSection, R.id.label_change_ratio, data.changeRatio);
            }

            {
                View stockDetailCell = v_findView(headerSection, R.id.label_user_stock_detail);
                if (data.stockPosition != null && data.hasStockPosition) {
                    v_setText(stockDetailCell, R.id.label_hold_position, data.holdPosition);
                    v_setText(stockDetailCell, R.id.label_last_income, data.todayIncome);
                    int userId = MineManager.getInstance().getmMe().index;
                    v_setClick(stockDetailCell, v -> showActivity(this, an_StockAnalysePage(String.valueOf(userId), String.valueOf(mStockId), data.range)));
                    v_setVisible(stockDetailCell);
                } else {
                    v_setGone(stockDetailCell);
                }
            }
        }

        private void performAddFollowStock(Button focusButton, StockDetailHomeInfo data) {
            v_setText(focusButton, "- 已加");
            data.isFocusStock = true;
            consumeEventMR(addFocusStock(data.source))
                    .onNextSuccess(response -> NotificationCenter.focusStockChangedSubject.onNext(null))
                    .onNextFail(response -> getErrorMessage(response))
                    .done();
        }

        private void performDeleteFollowStock(Button focusButton, StockDetailHomeInfo data) {
            GMFBottomSheet sheet = new GMFBottomSheet.Builder(getActivity())
                    .setTitle("确定不再关注" + data.stockName + "?")
                    .addContentItem(new GMFBottomSheet.BottomSheetItem(0, setColor("不再关注", RED_COLOR), 0))
                    .create();

            sheet.setOnItemClickListener((bottomSheet, item) -> {
                bottomSheet.dismiss();
                v_setText(focusButton, "+ 自选");
                data.isFocusStock = false;
                consumeEventMR(StockController.deleteFollowStock(data.source))
                        .onNextSuccess(response -> NotificationCenter.focusStockChangedSubject.onNext(null))
                        .onNextFail(response -> getErrorMessage(response))
                        .done();
            });
            sheet.show();
        }

        private void updateChartSection(StockDetailHomeInfo data) {
            int selectedPos;
            if (isFirstEnter) {
                selectedPos = 0;
                mTabLayout.getTabAt(selectedPos).select();
                isFirstEnter = false;
            } else {
                selectedPos = mTabLayout.getSelectedTabPosition();
            }
            replaceCurrentFragment(selectedPos, sClazzList[selectedPos], sActionList[selectedPos], data.source, authorityType, specType);

            mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    int position = tab.getPosition();
                    replaceCurrentFragment(position, sClazzList[position], sActionList[position], data.source, authorityType, specType);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            consumeEvent(StockChartDetailFragment.sChartTypeChanged)
                    .setTag("on_chart_type_change")
                    .onNextFinish(shared -> {
                        authorityType = shared.mAuthorityType;
                        specType = shared.mSpecType;

                        int selectedTab = shared.mTabPosition;
                        mTabLayout.getTabAt(selectedTab).select();
                        replaceCurrentFragment(selectedTab, sClazzList[selectedTab], sActionList[selectedTab], data.source, authorityType, specType);
                    })
                    .done();

            View detailCell = v_findView(mContentSection, R.id.cell_stock_detail);
            v_setText(detailCell, R.id.label_open_price, data.openPrice);
            v_setText(detailCell, R.id.label_pre_close_price, data.preClosePrice);
            v_setText(detailCell, R.id.label_max_high, data.rangeMax);
            v_setText(detailCell, R.id.label_min_low, data.rangMin);
            if (data.stockClassType == StockBrief.STOCK_CLASS_TYPE_NORMAL) {
                v_setText(detailCell, R.id.label_volume, data.volume);
                v_setText(detailCell, R.id.label_market_income_ratio, data.marketIncomeRatio);
                v_setText(detailCell, R.id.label_income_per, data.incomePer);
                v_setText(detailCell, R.id.label_market_capital, data.marketCapital);
            } else if (data.stockClassType == StockBrief.STOCK_CLASS_TYPE_FUND) {
                v_setText(detailCell, R.id.label_volume, data.volume);
                v_setText(detailCell, R.id.label_market_income_ratio, data.fundUnitValue);
                v_setText(detailCell, R.id.label_income_per, data.fundAccuValue);
                v_setText(detailCell, R.id.label_market_capital, data.fundNetDate);
            } else if (data.stockClassType == STOCK_CLASS_TYPE_GOVERNMENT_LOAN) {
                v_setText(detailCell, R.id.label_volume, data.volume);
                v_setText(detailCell, R.id.label_market_income_ratio, data.turnover);
                v_setText(detailCell, R.id.label_income_per, data.heghest52W);
                v_setText(detailCell, R.id.label_market_capital, data.lowest52W);
            } else if (data.stockClassType == STOCK_CLASS_TYPE_SPEC) {
                v_setText(detailCell, R.id.label_volume, data.volume);
                v_setText(detailCell, R.id.label_market_income_ratio, data.turnover);
                v_setText(detailCell, R.id.label_income_per, data.amplitudeValue);
                v_setText(detailCell, R.id.label_market_capital, data.marketCapital);
            }
        }

        private void replaceCurrentFragment(int position, Class clazz, CreateFragmentFunc func, Stock stock, int authorityType, int specType) {
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            SimpleFragment cacheFragment = (SimpleFragment) fm.findFragmentByTag(clazz.getSimpleName());
            SimpleFragment newFragment = cacheFragment == null ? (SimpleFragment) func.call() : null;

            if (cacheFragment != null) {
                if (position == 0) {
                    TimesChartFragment fragment = ((TimesChartFragment) cacheFragment);
                    fragment.fetchData(false);
                    transaction.show(fragment);
                    fragment.setUserVisibleHint(true);
                } else if (position == 1) {
                    FiveDayChartFragment fragment = ((FiveDayChartFragment) cacheFragment);
                    fragment.fetchData(false);
                    transaction.show(fragment);
                    fragment.setUserVisibleHint(true);
                } else if (position == 2) {
                    DayKLineChartFragment fragment = ((DayKLineChartFragment) cacheFragment);
                    fragment.fetchData(false, authorityType, specType);
                    transaction.show(fragment);
                    fragment.setUserVisibleHint(true);
                } else if (position == 3) {
                    WeekKLineChartFragment fragment = ((WeekKLineChartFragment) cacheFragment);
                    fragment.fetchData(false, authorityType, specType);
                    transaction.show(fragment);
                    fragment.setUserVisibleHint(true);
                } else if (position == 4) {
                    MonthKLineFragment fragment = ((MonthKLineFragment) cacheFragment);
                    fragment.fetchData(false, authorityType, specType);
                    transaction.show(fragment);
                    fragment.setUserVisibleHint(true);
                }
            } else {
                if (position == 0) {
                    TimesChartFragment fragment = ((TimesChartFragment) newFragment).init(stock);
                    transaction.add(R.id.container_fragment, fragment, clazz.getSimpleName());
                } else if (position == 1) {
                    FiveDayChartFragment fragment = ((FiveDayChartFragment) newFragment).init(stock);
                    transaction.add(R.id.container_fragment, fragment, clazz.getSimpleName());
                } else if (position == 2) {
                    DayKLineChartFragment fragment = ((DayKLineChartFragment) newFragment).init(stock, authorityType, specType);
                    transaction.add(R.id.container_fragment, fragment, clazz.getSimpleName());
                } else if (position == 3) {
                    WeekKLineChartFragment fragment = ((WeekKLineChartFragment) newFragment).init(stock, authorityType, specType);
                    transaction.add(R.id.container_fragment, fragment, clazz.getSimpleName());
                } else if (position == 4) {
                    MonthKLineFragment fragment = ((MonthKLineFragment) newFragment).init(stock, authorityType, specType);
                    transaction.add(R.id.container_fragment, fragment, clazz.getSimpleName());
                }
            }
            if (mCurrentFragment.isPresent() && (mCurrentFragment.get() != cacheFragment)) {
                transaction.hide(mCurrentFragment.get());
                mCurrentFragment.get().setUserVisibleHint(false);
            }
            if ((mCurrentFragment.isAbsent() || mCurrentFragment.get() != cacheFragment)) {
                transaction.commitAllowingStateLoss();
            }
            mCurrentFragment = Optional.of(cacheFragment != null ? cacheFragment : newFragment);
        }

        private void setupBottomBar(StockDetailHomeInfo data) {
            View bottomSection = v_findView(this, R.id.section_bottom_button);
            if (data.stockClassType == STOCK_CLASS_TYPE_GOVERNMENT_LOAN || data.stockClassType == STOCK_CLASS_TYPE_SPEC) {
                v_setGone(bottomSection);
                return;
            } else {
                v_setVisible(bottomSection);
            }

            TextView briefLabel = v_findView(bottomSection, R.id.label_bottom_brief);
            Button stockBuyButton = v_findView(bottomSection, R.id.btn_stock_buy);
            Button stockSaleButton = v_findView(bottomSection, R.id.btn_stock_sale);
            Button stockShareButton = v_findView(bottomSection, R.id.btn_stock_share);
            stockShareButton.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0, dp2px(dp2px(4))).border(RED_COLOR, dp2px(0.5f))));

            v_setClick(stockShareButton, v -> {
                if (MineManager.getInstance().isLoginOK()) {
                    int color = data.color;
                    int userID = MineManager.getInstance().getmMe().index;
                    String avatarURL = MineManager.getInstance().getmMe().getPhotoUrl();
                    Bitmap bitmap = generateShareImage(color, userID, avatarURL);
                    File localFile = new File(getActivity().getCacheDir().getAbsoluteFile() + File.separator + "tmp.jpg");
                    FileExtension.writeDataToFile(localFile, bitmap, true, Bitmap.CompressFormat.JPEG, 50);
                    bitmap.recycle();
                    if (getActivity() instanceof UMShareHandlerProtocol) {
                        UMShareHandlerProtocol protocol = (UMShareHandlerProtocol) getActivity();
                        ShareInfo shareInfo = new ShareInfo();
                        shareInfo.title = String.format("%s当前实时行情！", data.stockName);
                        shareInfo.msg = "来自操盘侠·顶尖操盘手带你股市翻红";
                        shareInfo.imageUrl = Uri.fromFile(localFile).toString();
                        SharePlatform[] platforms = {SharePlatform.WX, SharePlatform.WX_CIRCLE, SharePlatform.QQ, SharePlatform.SINA};
                        protocol.onPerformShare(shareInfo, platforms);
                    }
                } else {
                    showActivity(this, an_LoginPage());
                }
            });

            if (data.isSuspension) {
                v_setText(briefLabel, "停牌中，暂不可买卖");
                v_setVisible(briefLabel);
                stockBuyButton.setEnabled(false);
                stockSaleButton.setEnabled(false);
            } else {
                v_setGone(briefLabel);
                stockBuyButton.setEnabled(true);
                stockSaleButton.setEnabled(true);
            }
            v_setClick(this, R.id.btn_stock_buy, v -> {
                List<FundBrief> myFunds = Stream.of(TraderManager.getInstance().getFundList())
                        .filter(it -> anyMatch(it.status, Fund_Status.LockIn))
                        .collect(Collectors.toList());
                if (myFunds.isEmpty()) {
                    UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockBidClick);
                    showActivity(this, an_StockTradePage(-1, data.stockId, 1));
                } else {
                    GMFBottomSheet.Builder builder = new GMFBottomSheet.Builder(getActivity())
                            .addContentItem("simulation", "模拟买入");
                    Stream.of(myFunds)
                            .forEach(it -> {
                                builder.addContentItem(it, "组合买入（" + it.name + "）");
                            });
                    GMFBottomSheet bottomSheet = builder.create();
                    bottomSheet.setOnItemClickListener((sheet, item) -> {
                        sheet.dismiss();
                        if (item.tag instanceof FundBrief) {
                            FundBrief fund = (FundBrief) item.tag;
                            showActivity(this, an_FundTradePage(fund.index, fund.name, data.stockId, 1));
                        } else {
                            UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockBidClick);
                            showActivity(this, an_StockTradePage(-1, data.stockId, 1));
                        }
                    });
                    bottomSheet.show();
                }
            });
            v_setClick(this, R.id.btn_stock_sale, v -> {

                List<FundBrief> myFunds = Stream.of(new LinkedList<>(TraderManager.getInstance().getFundList()))
                        .filter(it -> anyMatch(it.status, Fund_Status.LockIn))
                        .collect(Collectors.toList());
                if (myFunds.isEmpty()) {
                    UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockAskClick);
                    showActivity(this, an_StockTradePage(-1, data.stockId, 2));
                } else {
                    GMFBottomSheet.Builder builder = new GMFBottomSheet.Builder(getActivity())
                            .addContentItem("simulation", "模拟卖出");
                    Stream.of(myFunds)
                            .forEach(it -> {
                                builder.addContentItem(it, "组合卖出（" + it.name + "）");
                            });
                    GMFBottomSheet bottomSheet = builder.create();
                    bottomSheet.setOnItemClickListener((sheet, item) -> {
                        sheet.dismiss();
                        if (item.tag instanceof FundBrief) {
                            FundBrief fund = (FundBrief) item.tag;
                            showActivity(this, an_FundTradePage(fund.index, fund.name, data.stockId, 2));
                        } else {
                            UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockAskClick);
                            showActivity(this, an_StockTradePage(-1, data.stockId, 2));
                        }
                    });
                    bottomSheet.show();
                }


            });
        }

        private void fetchMoreData() {
            if (mIsLoadingMore || !hasMoreData(mAllTraderStore))
                return;

            mIsLoadingMore = true;
            consumeEventMR(PageArrayHelper.getNextPage(mAllTraderStore))
                    .setTag("fetch_more")
                    .onNextSuccess(response -> {
                        updateStockFeedSection();
                    })
                    .onNextFinish(response -> {
                        mIsLoadingMore = false;
                    })
                    .done();
        }

        private int mLastRange = RANGE_ALL;

        private void updateStockFeedSection() {
            List<StockFeedVM> items = createStockFeedList();
            updateStockFeedListHeader(items);
            updateStockFeedListView(items);
        }

        private List<StockFeedVM> createStockFeedList() {
            return Stream.of(getData(mAllTraderStore)).map(it -> new StockFeedVM(it)).collect(Collectors.toList());
        }

        private void updateStockFeedListHeader(List<StockFeedVM> items) {
            int range = mLastRange;
            View parent = v_findView(mContentSection, R.id.header_stock_feed);

            if (range == RANGE_ALL) {
                if (items.isEmpty()) {
                    v_setGone(parent);
                } else {
                    v_setVisible(parent);
                }
                v_setText(parent, R.id.label_range, "全部");
            } else if (range == RANGE_BUY) {
                v_setText(parent, R.id.label_range, "买入");
            } else if (range == RANGE_SELL) {
                v_setText(parent, R.id.label_range, "卖出");
            }
        }

        private View createCell(Context ctx, ViewGroup parent, StockFeedVM item) {
            View cell = LayoutInflater.from(ctx).inflate(R.layout.cell_stock_feed, parent, false);
            UserAvatarView avatarView = v_findView(cell, R.id.img_avatar);
            avatarView.updateView(item.avatarURL, item.userType);
            v_setText(cell, R.id.label_user_name_and_income, item.userNameAndIncome);
            v_setText(cell, R.id.label_stock_name_and_code, item.stockNameAndCode);
            v_setText(cell, R.id.label_time, item.time);
            v_setText(cell, R.id.label_position_and_price, item.positionAndPrice);
            v_setText(cell, R.id.label_capital_change, item.capitalChange);
            ((BuySellStockIndicator) v_findView(cell, R.id.indicator)).setState(item.feedType == StockController.FEED_TYPE_BUY ? BuySellStockIndicator.STATE_BUY : BuySellStockIndicator.STATE_SELL);
            v_findView(cell, R.id.section_stock_analyse).setBackgroundColor(item.feedType == StockController.FEED_TYPE_BUY ? Color.parseColor("#EAF1FD") : Color.parseColor("#FCF4E1"));
            return cell;
        }

        @SuppressWarnings("unchecked")
        private void updateStockFeedListView(List<StockFeedVM> items) {

            Context ctx = getActivity();
            LinearLayout container = v_findView(mContentSection, R.id.section_feed_list);
            container.removeAllViews();
            Stream.of(items)
                    .map(item -> {
                        View cell = createCell(ctx, container, item);
                        v_setClick(cell, R.id.section_user, v -> {
                            showActivity(this, an_UserDetailPage(item.user));
                            UmengUtil.stat_click_event(UmengUtil.eEVENTIDTopHomePage);
                        });
                        v_setClick(cell, R.id.section_analyse, v -> {
                            showActivity(this, an_StockAnalysePage(item.userID, item.stockID, item.range));
                            UmengUtil.stat_click_event(UmengUtil.eEVENTIDTopStockProfitLoss);
                        });
                        return cell;
                    })
                    .forEach(cell -> container.addView(cell));
        }
    }

    private static class StockDetailHomeInfo {

        private Stock source;
        private Boolean isSuspension;
        private int color;
        private String stockId;
        private int stockClassType;
        private CharSequence stockName;
        private CharSequence stockCode;
        private CharSequence stockTitle;
        private CharSequence stockStatus;
        private CharSequence stockFocus;
        private boolean isFocusStock;
        private CharSequence currentPrice;
        private CharSequence changePrice;
        private CharSequence changeRatio;
        private CharSequence openPrice;
        private CharSequence preClosePrice;
        private CharSequence rangeMax;
        private CharSequence rangMin;
        private CharSequence volume;
        private CharSequence turnover;
        private CharSequence marketIncomeRatio;
        private CharSequence incomePer;
        private CharSequence marketCapital;

        private StockPosition stockPosition;
        private boolean hasStockPosition = false;
        private String range;

        private List<StockBrief> followStockList;
        private CharSequence holdPosition;
        private CharSequence todayIncome;

        private CharSequence fundUnitValue;
        private CharSequence fundAccuValue;
        private CharSequence fundNetDate;

        private CharSequence heghest52W;
        private CharSequence lowest52W;
        private CharSequence amplitudeValue;

        public StockDetailHomeInfo(Stock stock) {
            source = stock;
            if (stock != null) {
                stockId = stock.index;
                isSuspension = stock.suspension;
                stockClassType = stock.stockClassType;
                if (isSuspension != null) {
                    color = isSuspension || stock.change == 0 ? GREY_COLOR : getIncomeBgColor(stock.last - stock.prevClose);
                } else {
                    color = GREY_COLOR;
                }
                stockName = stock.name;
                stockCode = stock.code;
                stockTitle = setFontSize(setColor(stock.name + " " + stock.code, WHITE_COLOR), sp2px(14));
                stockStatus = concatNoBreak(stock.marketInfo, " ", formatSecond(stock.lastFreshTime, "MM/dd HH:mm:ss"));

                followStockList = SimulationStockManager.getInstance().followStockList;
                isFocusStock = followStockList != null && followStockList.contains(stock);
                stockFocus = isFocusStock ? "- 已加" : "+ 自选";
                currentPrice = setFontSize(setColor(formatMoney(stock.last, false, 2), WHITE_COLOR), sp2px(52));
                changePrice = setFontSize(setColor(formatMoney(stock.change, true, 2), WHITE_COLOR), sp2px(16));
                changeRatio = setFontSize(setColor(formatRatio(stock.changeRatio, true, 2), WHITE_COLOR), sp2px(16));

                openPrice = concat("今开", setFontSize(setColor(formatMoney(stock.open, false, 2), TEXT_BLACK_COLOR), sp2px(10)));
                preClosePrice = concat("昨收", setFontSize(setColor(formatMoney(stock.prevClose, false, 2), TEXT_BLACK_COLOR), sp2px(10)));
                rangeMax = concat("最高", setFontSize(setColor(formatMoney(stock.rangeMax, false, 2), TEXT_BLACK_COLOR), sp2px(10)));
                rangMin = concat("最低", setFontSize(setColor(formatMoney(stock.rangeMin, false, 2), TEXT_BLACK_COLOR), sp2px(10)));
                if (stockClassType == StockBrief.STOCK_CLASS_TYPE_NORMAL) {
                    volume = concat("成交量(股)", setFontSize(setColor(formatBigNumber(stock.volume, false, 2, 2, true), TEXT_BLACK_COLOR), sp2px(10)));
                    marketIncomeRatio = concat("市盈率", setFontSize(setColor(formatBigNumber(stock.PE, false, 2, 2, false), TEXT_BLACK_COLOR), sp2px(10)));
                    incomePer = concat("每股收益", setFontSize(setColor(formatMoney(stock.incomePer, false, 2), TEXT_BLACK_COLOR), sp2px(10)));
                    marketCapital = concat("总市值", setFontSize(setColor(formatBigNumber(stock.mktCap, false, 2, 2, true), TEXT_BLACK_COLOR), sp2px(10)));
                } else if (stockClassType == StockBrief.STOCK_CLASS_TYPE_FUND) {
                    volume = concat("成交量(股)", setFontSize(setColor(formatBigNumber(stock.volume, false, 2, 2, true), TEXT_BLACK_COLOR), sp2px(10)));
                    fundUnitValue = concat("单位净值", setFontSize(setColor(formatMoney(stock.fundUnitValue, false, 2), TEXT_BLACK_COLOR), sp2px(10)));
                    fundAccuValue = concat("累计净值", setFontSize(setColor(formatMoney(stock.fundAccuValue, false, 2), TEXT_BLACK_COLOR), sp2px(10)));
                    fundNetDate = concat("净值日期", setFontSize(setColor((stock.fundNetDate == null || "".equals(stock.fundNetDate)) ? "--" : stock.fundNetDate, TEXT_BLACK_COLOR), sp2px(10)));
                } else if (stockClassType == STOCK_CLASS_TYPE_GOVERNMENT_LOAN) {
                    volume = concat("24h成交量", setFontSize(setColor(formatBigNumber(stock.volume, false, 2, 2, true), TEXT_BLACK_COLOR), sp2px(10)));
                    turnover = concat("24h成交额", setFontSize(setColor(formatBigNumber(stock.turnover, false, 2, 2, true), TEXT_BLACK_COLOR), sp2px(10)));
                    heghest52W = concat("52周最高", setFontSize(setColor(formatMoney(stock.heghest52W, false, 2), TEXT_BLACK_COLOR), sp2px(10)));
                    lowest52W = concat("52周最低", setFontSize(setColor(formatMoney(stock.lowest52W, false, 2), TEXT_BLACK_COLOR), sp2px(10)));
                } else if (stockClassType == STOCK_CLASS_TYPE_SPEC) {
                    volume = concat("成交量(股)", setFontSize(setColor(formatBigNumber(stock.volume, false, 2, 2, true), TEXT_BLACK_COLOR), sp2px(10)));
                    turnover = concat("成交额", setFontSize(setColor(formatBigNumber(stock.turnover, false, 2, 2, true), TEXT_BLACK_COLOR), sp2px(10)));
                    amplitudeValue = concat("振幅", setFontSize(setColor(formatRatio(stock.amplitudeValue, false, 2), TEXT_BLACK_COLOR), sp2px(10)));
                    marketCapital = concat("总市值", setFontSize(setColor(formatBigNumber(stock.mktCap, false, 2, 2, true), TEXT_BLACK_COLOR), sp2px(10)));
                }

                stockPosition = stock.stockPosition;
                List<StockPosition> positionList = SimulationStockManager.getInstance().mineAccountMore.holdStocks;
                if (positionList != null) {
                    for (StockPosition position : positionList) {
                        hasStockPosition = stock.index.equals(position.stock.index);
                    }
                }
                if (stockPosition != null && hasStockPosition) {
                    holdPosition = concatNoBreak("持仓:" + setColor(formatBigNumber(stockPosition.holdAmount, false, 2, 2, false), WHITE_COLOR));
                    todayIncome = concatNoBreak("今日盈亏:" + setColor(formatMoney(stockPosition.todayIncome, true, 2), WHITE_COLOR));
                    range = stockPosition.range;
                } else {
                    holdPosition = concatNoBreak("持仓:" + setColor("--", WHITE_COLOR));
                    todayIncome = concatNoBreak("今日盈亏:" + setColor("--", WHITE_COLOR));
                    range = "";
                }
            } else {
                stockId = null;
                isSuspension = false;
                color = GREY_COLOR;
                stockName = "--";
                stockCode = "--";
                stockTitle = setFontSize(setColor(stockName + " " + stockCode, WHITE_COLOR), sp2px(10));
                stockStatus = concatNoBreak("--", " ", "--");

                followStockList = SimulationStockManager.getInstance().followStockList;
                isFocusStock = false;
                stockFocus = isFocusStock ? "- 已加" : "+ 自选";
                currentPrice = setFontSize("--", sp2px(52));
                changePrice = setFontSize("--", sp2px(16));
                changeRatio = setFontSize("--", sp2px(16));

                openPrice = concat("今开", setFontSize("--", sp2px(10)));
                preClosePrice = concat("昨收", setFontSize("--", sp2px(10)));
                rangeMax = concat("最高", setFontSize("--", sp2px(10)));
                rangMin = concat("最低", setFontSize("--", sp2px(10)));
                volume = concat("成交量(股)", setFontSize("--", sp2px(10)));
                marketIncomeRatio = concat("市盈率", setFontSize("--", sp2px(10)));
                incomePer = concat("每股收益", setFontSize("--", sp2px(10)));
                marketCapital = concat("总市值", setFontSize("--", sp2px(10)));
                volume = concat("成交量(股)", setFontSize("--", sp2px(10)));
                fundUnitValue = concat("单位净值", setFontSize("--", sp2px(10)));
                fundAccuValue = concat("累计净值", setFontSize("--", sp2px(10)));
                fundNetDate = concat("净值日期", setFontSize("--", sp2px(10)));
                heghest52W = concat("52周最高", setFontSize("--", sp2px(10)));
                lowest52W = concat("52周最低", setFontSize("--", sp2px(10)));
                turnover = concat("成交额", setFontSize("--", sp2px(10)));
                amplitudeValue = concat("振幅", setFontSize("--", sp2px(10)));

                openPrice = concat("今开", setFontSize("--", sp2px(10)));
                preClosePrice = concat("昨收", setFontSize("--", sp2px(10)));
                rangeMax = concat("最高", setFontSize("--", sp2px(10)));
                rangMin = concat("最低", setFontSize("--", sp2px(10)));
                volume = concat("成交量(股)", setFontSize("--", sp2px(10)));
                marketIncomeRatio = concat("市盈率", setFontSize("--", sp2px(10)));
                incomePer = concat("每股收益", setFontSize("--", sp2px(10)));
                marketCapital = concat("总市值", setFontSize("--", sp2px(10)));

                holdPosition = concatNoBreak("持仓:" + setColor("--", WHITE_COLOR));
                todayIncome = concatNoBreak("今日盈亏:" + setColor("--", WHITE_COLOR));
            }
        }
    }

    public static class TimesChartFragment extends SimpleFragment {

        private Stock mStock;
        private EnvDataPager mEnvDataPager;
        private ChartViewContainer<ChartController.ChartInfo> mChartContainer;
        private TimesChartRender mRender;
        private FrameLayout mChartSection;
        private DrawLineView mLineView;
        private ChartContainerOnTouchListener mListener;

        public TimesChartFragment init(Stock stock) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(Stock.class.getName(), stock);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockTimeLine);
            mStock = (Stock) getArguments().getSerializable(Stock.class.getName());
            return inflater.inflate(R.layout.frag_times_chart, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            initView();
            v_setClick(mReloadSection, v -> {
                v_setGone(v);
                v_setGone(mContentSection);
                v_setVisible(mLoadingSection);
                fetchData(false);
            });
            fetchData(true);
        }

        private void initView() {
            mChartSection = v_findView(mContentSection, R.id.section_chart);
            mChartContainer = v_findView(mChartSection, R.id.chart_container);
            mLineView = v_findView(mChartSection, R.id.line_view);
        }

        @SuppressWarnings("unchecked")
        private void fetchData(boolean reload) {
            if (reload) {
                v_setGone(mContentSection);
                v_setGone(mReloadSection);
                v_setVisible(mLoadingSection);
            }

            if (mStock.stockClassType == STOCK_CLASS_TYPE_GOVERNMENT_LOAN || mStock.stockClassType == STOCK_CLASS_TYPE_SPEC) {
                consumeEventMRUpdateUI(fetchStockTLineData(mStock, GMFStockAnswer_Authority_No, Tline_Type_Day), reload)
                        .onNextSuccess(response -> {
                            updateContentSection(response.data, GMFStockAnswer_Authority_No, Spec_Type_NONE);
                            v_setGone(mContentSection, R.id.section_five_order);
                        })
                        .done();
            } else {
                Observable<List<MResults.MResultsInfo>> observable = zipToList(fetchStockDetailInfo(reload, mStock.index),
                        fetchStockTLineData(mStock, GMFStockAnswer_Authority_No, Tline_Type_Day));
                consumeEventMRListUpdateUI(observable, reload)
                        .onNextSuccess(responses -> {
                            MResults.MResultsInfo<Stock> stockCallback = responses.get(0);
                            MResults.MResultsInfo<StockTline> tLineCallback = responses.get(1);
                            updateFiveOrderSection(stockCallback.data);
                            updateContentSection(tLineCallback.data, GMFStockAnswer_Authority_No, Spec_Type_NONE);
                        })
                        .done();
            }
        }

        private void updateFiveOrderSection(Stock stock) {
            View parent = v_findView(this, R.id.section_five_order);

            {
                Iterator<? extends FiveOrderCell> cells = Stream.of(R.id.cell_bid1, R.id.cell_bid2, R.id.cell_bid3, R.id.cell_bid4, R.id.cell_bid5).map(it -> (FiveOrderCell) v_findView(parent, it)).getIterator();
                Iterator<? extends Quotation> quotations = Stream.of(stock.bids).getIterator();

                while (cells.hasNext() && quotations.hasNext()) {
                    FiveOrderCell cell = cells.next();
                    Quotation quotation = quotations.next();
                    cell.setPrice(quotation.price, stock.prevClose);
                    cell.setCount((long) quotation.amount);
                }
            }

            {
                Iterator<? extends FiveOrderCell> cells = Stream.of(R.id.cell_ask1, R.id.cell_ask2, R.id.cell_ask3, R.id.cell_ask4, R.id.cell_ask5).map(it -> (FiveOrderCell) v_findView(parent, it)).getIterator();
                Iterator<? extends Quotation> quotations = Stream.of(stock.asks).getIterator();

                while (cells.hasNext() && quotations.hasNext()) {
                    FiveOrderCell cell = cells.next();
                    Quotation quotation = quotations.next();
                    cell.setPrice(quotation.price, stock.prevClose);
                    cell.setCount((long) quotation.amount);
                }
            }
        }

        private void updateContentSection(StockTline data, int authorityType, int specType) {
            List<ChartInfo> chartList = formatChartData(data.data);

            ChartViewContainer.DEBUG = true;
            setupLightPoint(chartList);

            if (mEnvDataPager == null) {
                mEnvDataPager = new EnvDataPager();
            }
            mEnvDataPager.setChartData(chartList, StockDetailFragment.TYPE_TIMES_TLINE, Spec_Type_NONE, true);
            mChartContainer.setDataSourceDelegate(new DataSourceDelegate<ChartInfo>() {
                @Override
                public List<ChartInfo> getAllData() {
                    return mEnvDataPager.mChartData;
                }

                @Override
                public int getDataCountPerPage(List<ChartInfo> list) {
                    return mEnvDataPager.mChartData.size();
                }

            });
            if (mRender == null) {
                mRender = new TimesChartRender();
            }
            mRender.setEnvData(mEnvDataPager);
            mChartContainer.setRenderDelegate(mRender);
            mChartContainer.notifyDataSetChanged();

            View tabSection = v_findView(getActivity(), R.id.section_tab);
            View chartDetailSection = v_findView(getActivity(), R.id.section_chart_detail);
            View timesDetailSection = v_findView(chartDetailSection, R.id.section_times_detail);
            View klineDetailSection = v_findView(chartDetailSection, R.id.section_kline_detail);
            AdvanceSwipeRefreshLayout refreshLayout = v_findView(getActivity(), R.id.refreshLayout);

            Action0 itemClickHandler = () -> {
                showActivity(this, an_StockChartDetailPage(mStock.index, TYPE_TLINE_DAY, authorityType, specType));
            };
            Action3<Float, ChartInfo, List<ChartInfo>> itemLongClickHandler = (x, info, visibleData) -> {
                mLineView.drawLine(x, info, visibleData, TYPE_TIMES_TLINE, TYPE_CHART_SMALL);
                v_setVisible(mLineView);
                refreshLayout.setEnabled(false);
                updateLongPressSection(chartDetailSection, timesDetailSection, info);
                v_setVisible(timesDetailSection);
                v_setGone(klineDetailSection);
                animateToUp(tabSection, chartDetailSection);
            };
            Action0 itemLongUpHandler = () -> {
                refreshLayout.setEnabled(true);
                v_setGone(mLineView);
                animateToDown(tabSection, chartDetailSection);
            };

            if (mListener == null) {
                mListener = new ChartContainerOnTouchListener(mChartContainer, itemClickHandler, itemLongClickHandler, itemLongUpHandler, true, TYPE_TIMES_TLINE);
            }
            mChartContainer.setOnTouchListener(mListener);

            consumeEvent(sRefreshChartValueSubject)
                    .setTag("refresh_value")
                    .onNextFinish(fresh -> {
                        setupTextSection();
                    })
                    .done();
        }

        private void updateLongPressSection(View chartDetailSection, View timesDetailSection, ChartInfo info) {
            ChartLongPressInfo pressInfo = new ChartLongPressInfo(info);

            v_setText(chartDetailSection, R.id.label_title, pressInfo.smallTimesLastPrice);
            v_setText(timesDetailSection, R.id.label_column_1, pressInfo.timesVolume);
            v_setText(timesDetailSection, R.id.label_column_2, pressInfo.timesChangeRatio);
            v_setText(timesDetailSection, R.id.label_column_3, pressInfo.averagePrice);
            v_setVisible(timesDetailSection);
        }

        private void setupTextSection() {

            View rightSection = v_findView(mContentSection, R.id.section_chart_right);
            View leftSection = v_findView(mContentSection, R.id.section_chart_left);
            v_setText(rightSection, R.id.label_right_text1, setFontSize(formatBigNumber(mRender.maxPrice, false, 2, 2, false), sp2px(8)));
            v_setText(rightSection, R.id.label_right_text3, setFontSize(formatBigNumber(mRender.minPrice, false, 2, 2, false), sp2px(8)));
            v_setGone(rightSection, R.id.label_right_text4);
            v_setGone(rightSection, R.id.label_right_text5);
            v_setGone(rightSection, R.id.label_right_text2);

            v_setText(leftSection, R.id.label_left_text1, setFontSize(formatRatio((mRender.maxPrice - mRender.preClose) / mRender.preClose, false, 2), sp2px(8)));
            v_setText(leftSection, R.id.label_left_text3, setFontSize(formatRatio((mRender.minPrice - mRender.preClose) / mRender.preClose, false, 2), sp2px(8)));
            v_setGone(leftSection, R.id.label_left_text2);
            v_setGone(leftSection, R.id.label_left_text4);
            v_setGone(leftSection, R.id.label_left_text5);
        }

        private List<ChartInfo> formatChartData(List<TLineData> data) {
            List<ChartController.ChartInfo> chartList = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                ChartInfo info;
                if (i != 0 && data.get(i).traderTime - data.get(i - 1).traderTime > 3600) {
                    info = new ChartInfo(data.get(i), true);
                } else {
                    info = new ChartInfo(data.get(i), false);
                }
                chartList.add(info);
            }
            return chartList;
        }

        boolean hasPoint = false;

        private void setupLightPoint(List<ChartInfo> data) {
            List<Double> priceData = Stream.of(data).filter(info -> info.last > -1).map(info -> info.last).collect(Collectors.toList());
            if (priceData.isEmpty() || priceData.size() == data.size())
                return;
            ViewExtension.v_globalLayout(mChartContainer, true, v -> {

                float width = mChartContainer.getMeasuredWidth();
                float height = mChartContainer.getMeasuredHeight();
                double preClose = data.get(0).preClose;
                Pair<Double, Double> pair = recomputeRange(priceData);
                double maxValue = pair.first;
                double minValue = pair.second;
                double mTimesHalf;
                double maxTimesLine;
                float mTimesRate;

                float candleWith = (width - 1) / (data.size() - 1);
                float upperHeight = (height - dp2px(14)) / 4 * 3;

                if (Math.abs(maxValue - preClose) > Math.abs(minValue - preClose)) {
                    mTimesHalf = Math.abs(maxValue - preClose);
                    maxTimesLine = maxValue;
                } else if (Math.abs(maxValue - preClose) < Math.abs(minValue - preClose)) {
                    mTimesHalf = Math.abs(minValue - preClose);
                    maxTimesLine = minValue + 2 * mTimesHalf;
                } else {
                    if (maxValue > preClose) {
                        mTimesHalf = Math.abs(maxValue - preClose);
                        maxTimesLine = maxValue;
                    } else if (maxValue == preClose || minValue == preClose) {
                        mTimesHalf = preClose;
                        maxTimesLine = 2 * preClose;
                    } else {
                        mTimesHalf = Math.abs(minValue - preClose);
                        maxTimesLine = preClose + mTimesHalf;
                    }
                }
                mTimesRate = (float) (upperHeight / (2 * mTimesHalf));

                float offSetX = candleWith * (priceData.size() - 1);
                float offSetY = (float) ((maxTimesLine - priceData.get(priceData.size() - 1)) * mTimesRate);

                FrameLayout pointContainer = v_findView(mContentSection, R.id.point_container);
                if (!hasPoint) {
                    View outView = new View(getActivity());
                    outView.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(Color.parseColor("#3498DB"), dp2px(2.5f))));
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp2px(5), dp2px(5));
                    params.gravity = Gravity.CENTER;
                    pointContainer.addView(outView, params);

                    ObjectAnimator animator1 = ObjectAnimator.ofFloat(outView, "scaleX", 1, 2.5f);
                    ObjectAnimator animator2 = ObjectAnimator.ofFloat(outView, "scaleY", 1, 2.5f);
                    ObjectAnimator animator3 = ObjectAnimator.ofFloat(outView, "alpha", 0.8f, 0.3f);
                    animator1.setDuration(2000);
                    animator1.setRepeatCount(ObjectAnimator.INFINITE);
                    animator2.setDuration(2000);
                    animator2.setRepeatCount(ObjectAnimator.INFINITE);
                    animator3.setDuration(2000);
                    animator3.setRepeatCount(ObjectAnimator.INFINITE);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(animator1, animator2, animator3);
                    animatorSet.start();

                    View innerView = new View(getActivity());
                    innerView.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(Color.parseColor("#3498DB"), dp2px(2.5f))));
                    FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(dp2px(5), dp2px(5));
                    params2.gravity = Gravity.CENTER;
                    pointContainer.addView(innerView, params2);

                    hasPoint = true;
                }

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) pointContainer.getLayoutParams();
                params.leftMargin = (int) (offSetX - params.width / 2 + 0.5f);
                params.topMargin = (int) (offSetY - params.height / 2 + 0.5f);
                pointContainer.setLayoutParams(params);
            });
        }

        private Pair<Double, Double> recomputeRange(List<Double> data) {
            ArrayList<Double> subData = new ArrayList<>(data);
            Collections.sort(subData, (lhs, rhs) -> {
                double ret = rhs - lhs;
                if (ret > 0) {
                    return 1;
                } else if (ret < 0) {
                    return -1;
                } else
                    return 0;
            });
            double maxValue = subData.get(0);

            Collections.sort(subData, (lhs, rhs) -> {
                double ret = lhs - rhs;
                if (ret > 0) {
                    return 1;
                } else if (ret < 0) {
                    return -1;
                } else
                    return 0;
            });
            double minValue = subData.get(0);

            return Pair.create(maxValue, minValue);
        }

        private boolean mIsAnimating = false;
        private boolean mIsAnimationToUp = false;

        private void animateToUp(View headerSection, View chartDetailSection) {
            if (mIsAnimating || mIsAnimationToUp)
                return;
            mIsAnimating = true;
            ObjectAnimator animator = ObjectAnimator.ofFloat(headerSection, "translationY", 0, -chartDetailSection.getMeasuredHeight());
            animator.setDuration(200);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsAnimating = false;
                    mIsAnimationToUp = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }

        private void animateToDown(View headerSection, View chartDetailSection) {
            if (mIsAnimating)
                return;
            mIsAnimating = true;
            ObjectAnimator animator = ObjectAnimator.ofFloat(headerSection, "translationY", -chartDetailSection.getMeasuredHeight(), 0);
            animator.setDuration(200);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsAnimating = false;
                    mIsAnimationToUp = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }
    }

    public static class FiveDayChartFragment extends SimpleFragment {

        private Stock mStock;
        private EnvDataPager mEnvDataPager;
        private ChartViewContainer<ChartInfo> mChartContainer;
        private FiveDayChartRender mRender;
        private DrawLineView mLineView;
        private ChartContainerOnTouchListener mListener;

        public FiveDayChartFragment init(Stock stock) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(Stock.class.getName(), stock);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockFiveDay);
            mStock = (Stock) getArguments().getSerializable(Stock.class.getName());
            return inflater.inflate(R.layout.frag_five_day_chart, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            initView();
            v_setClick(mReloadSection, v -> {
                v_setGone(v);
                v_setVisible(mLoadingSection);
                fetchData(false);
            });

            fetchData(true);
        }

        private void initView() {
            mChartContainer = v_findView(mContentSection, R.id.chart_container);
            mLineView = v_findView(mContentSection, R.id.line_view);
            mEnvDataPager = new EnvDataPager();
            mRender = new FiveDayChartRender();

        }

        private void fetchData(boolean reload) {
            if (reload) {
                v_setGone(mContentSection);
                v_setGone(mReloadSection);
                v_setVisible(mLoadingSection);
            }

            consumeEventMRUpdateUI(fetchStockTLineData(mStock, GMFStockAnswer_Authority_No, Tline_Type_5Day), reload)
                    .onNextSuccess(response -> {
                        updateContentSection(response.data, GMFStockAnswer_Authority_No, Spec_Type_NONE);
                    })
                    .done();
        }

        private void updateContentSection(StockTline data, int authorityType, int specType) {
            List<ChartInfo> chartList = formatChartData(data.data);

            ChartViewContainer.DEBUG = true;
            mEnvDataPager.setChartData(chartList, StockDetailFragment.TYPE_TIMES_TLINE, Spec_Type_NONE, true);
            mChartContainer.setDataSourceDelegate(new DataSourceDelegate<ChartInfo>() {
                @Override
                public List<ChartInfo> getAllData() {
                    return mEnvDataPager.mChartData;
                }

                @Override
                public int getDataCountPerPage(List<ChartInfo> list) {
                    return mEnvDataPager.mChartData.size();
                }

            });
            mRender.setEnvData(mEnvDataPager);
            mChartContainer.setRenderDelegate(mRender);
            mChartContainer.notifyDataSetChanged();

            View tabSection = v_findView(getActivity(), R.id.section_tab);
            View chartDetailSection = v_findView(getActivity(), R.id.section_chart_detail);
            View timesDetailSection = v_findView(chartDetailSection, R.id.section_times_detail);
            View klineDetailSection = v_findView(chartDetailSection, R.id.section_kline_detail);
            AdvanceSwipeRefreshLayout refreshLayout = v_findView(getActivity(), R.id.refreshLayout);

            Action0 itemClickHandler = () -> {
                showActivity(this, an_StockChartDetailPage(mStock.index, TYPE_TLINE_FIVE_DAY, authorityType, specType));
            };
            Action3<Float, ChartInfo, List<ChartInfo>> itemLongClickHandler = (x, info, visibleData) -> {
                mLineView.drawLine(x, info, visibleData, TYPE_FIVE_DAY_TLINE, TYPE_CHART_SMALL);
                v_setVisible(mLineView);
                refreshLayout.setEnabled(false);

                updateLongPressSection(chartDetailSection, timesDetailSection, info);
                v_setVisible(timesDetailSection);
                v_setGone(klineDetailSection);
                animateToUp(tabSection, chartDetailSection);
            };
            Action0 itemLongUpHandler = () -> {
                refreshLayout.setEnabled(true);
                v_setGone(mLineView);
                animateToDown(tabSection, chartDetailSection);
            };
            if (mListener == null) {
                mListener = new ChartContainerOnTouchListener(mChartContainer, itemClickHandler, itemLongClickHandler, itemLongUpHandler, true, TYPE_TIMES_TLINE);
            }
            mChartContainer.setOnTouchListener(mListener);

            consumeEvent(sRefreshChartValueSubject)
                    .setTag("refresh_value")
                    .onNextFinish(fresh -> {
                        setupTextSection();
                    })
                    .done();
        }

        private void updateLongPressSection(View chartDetailSection, View timesDetailSection, ChartInfo info) {
            ChartLongPressInfo pressInfo = new ChartLongPressInfo(info);

            v_setText(chartDetailSection, R.id.label_title, pressInfo.smallTimesLastPrice);
            v_setText(timesDetailSection, R.id.label_column_1, pressInfo.timesVolume);
            v_setText(timesDetailSection, R.id.label_column_2, pressInfo.timesChangeRatio);
            v_setText(timesDetailSection, R.id.label_column_3, pressInfo.averagePrice);
            v_setVisible(timesDetailSection);
        }

        private void setupTextSection() {
            View rightSection = v_findView(mContentSection, R.id.section_chart_right);
            View leftSection = v_findView(mContentSection, R.id.section_chart_left);
            v_setText(rightSection, R.id.label_right_text1, setFontSize(formatBigNumber(mRender.maxPrice, false, 2, 2, false), sp2px(8)));
            v_setText(rightSection, R.id.label_right_text3, setFontSize(formatBigNumber(mRender.minPrice, false, 2, 2, false), sp2px(8)));
            v_setGone(rightSection, R.id.label_right_text4);
            v_setGone(rightSection, R.id.label_right_text5);
            v_setGone(rightSection, R.id.label_right_text2);

            v_setText(leftSection, R.id.label_left_text1, setFontSize(formatRatio((mRender.maxPrice - mRender.preClose) / mRender.preClose, false, 2), sp2px(8)));
            v_setText(leftSection, R.id.label_left_text3, setFontSize(formatRatio((mRender.minPrice - mRender.preClose) / mRender.preClose, false, 2), sp2px(8)));
            v_setGone(leftSection, R.id.label_left_text2);
            v_setGone(leftSection, R.id.label_left_text4);
            v_setGone(leftSection, R.id.label_left_text5);
        }

        private List<ChartInfo> formatChartData(List<TLineData> data) {
            List<ChartController.ChartInfo> chartList = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                ChartController.ChartInfo info;
                if (i != data.size() - 1 && !formatSecond(data.get(i).traderTime, "MM-dd").equals(formatSecond(data.get(i + 1).traderTime, "MM-dd"))) {
                    info = new ChartInfo(data.get(i), true);
                } else if (i == data.size() - 1) {
                    info = new ChartController.ChartInfo(data.get(i), true);
                } else {
                    info = new ChartInfo(data.get(i), false);
                }
                chartList.add(info);
            }
            return chartList;
        }

        private boolean mIsAnimating = false;
        private boolean mIsAnimationToUp = false;

        private void animateToUp(View headerSection, View chartDetailSection) {
            if (mIsAnimating || mIsAnimationToUp)
                return;
            mIsAnimating = true;
            ObjectAnimator animator = ObjectAnimator.ofFloat(headerSection, "translationY", 0, -chartDetailSection.getMeasuredHeight());
            animator.setDuration(200);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsAnimating = false;
                    mIsAnimationToUp = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }

        private void animateToDown(View headerSection, View chartDetailSection) {
            if (mIsAnimating)
                return;
            mIsAnimating = true;
            ObjectAnimator animator = ObjectAnimator.ofFloat(headerSection, "translationY", -chartDetailSection.getMeasuredHeight(), 0);
            animator.setDuration(200);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsAnimating = false;
                    mIsAnimationToUp = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }
    }

    public static class DayKLineChartFragment extends SimpleFragment {

        private Stock mStock;
        private int mAuthorityType;
        private int mSpecType;
        private EnvDataPager mEnvDataPager;
        private ChartViewContainer<ChartInfo> mChartContainer;
        private DayKLineChartRender mRender;
        private DrawLineView mLineView;
        private ChartContainerOnTouchListener mListener;

        public DayKLineChartFragment init(Stock stock, int authorityType, int specType) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(Stock.class.getName(), stock);
            arguments.putInt("authority_type", authorityType);
            arguments.putInt("spec_type", specType);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockDay);
            mStock = (Stock) getArguments().getSerializable(Stock.class.getName());
            mAuthorityType = getArguments().getInt("authority_type");
            mSpecType = getArguments().getInt("spec_type");
            return inflater.inflate(R.layout.frag_kline_chart, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            initView();
            v_setClick(mReloadSection, v -> {
                v_setGone(v);
                v_setGone(mContentSection);
                v_setVisible(mLoadingSection);
                fetchData(false, mAuthorityType, mSpecType);
            });
            fetchData(true, mAuthorityType, mSpecType);
        }

        private void initView() {
            mChartContainer = v_findView(mContentSection, R.id.chart_container);
            mChartContainer.setLayoutDelegate(new EndToStartLayoutDelegate<ChartController.ChartInfo>(getActivity()));
            mLineView = v_findView(mContentSection, R.id.line_view);
        }

        private void fetchData(boolean reload, int authorityType, int specType) {
            if (reload) {
                v_setGone(mContentSection);
                v_setGone(mReloadSection);
                v_setVisible(mLoadingSection);
            }

            consumeEventMRUpdateUI(fetchStockKLineData(reload, mStock, authorityType, KLineData.Kline_Type_Day, specType), reload)
                    .setTag("day_kline")
                    .onNextSuccess(response -> {
                        updateContentSection(response.data, authorityType, specType);
                    })
                    .done();
        }

        private void updateContentSection(StockKline data, int authorityType, int specType) {
            List<ChartInfo> chartList = formatChartData(data.data());

            ChartViewContainer.DEBUG = true;
            if (mEnvDataPager == null) {
                mEnvDataPager = new EnvDataPager();
            }
            mEnvDataPager.setChartData(chartList, StockDetailFragment.TYPE_KLINE, specType, true);
            mChartContainer.setDataSourceDelegate(new DataSourceDelegate<ChartInfo>() {
                @Override
                public List<ChartInfo> getAllData() {
                    return mEnvDataPager.mChartData;
                }

                @Override
                public int getDataCountPerPage(List<ChartInfo> list) {
                    return EnvDataPager.DEFAULT_COUNT;
                }

            });
            if (mRender == null) {
                mRender = new DayKLineChartRender();
            }
            mRender.setEnvData(mEnvDataPager);
            mChartContainer.setRenderDelegate(mRender);
            mChartContainer.notifyDataSetChanged();


            View tabSection = v_findView(getActivity(), R.id.section_tab);
            View chartDetailSection = v_findView(getActivity(), R.id.section_chart_detail);
            View timesDetailSection = v_findView(chartDetailSection, R.id.section_times_detail);
            View klineDetailSection = v_findView(chartDetailSection, R.id.section_kline_detail);
            AdvanceSwipeRefreshLayout refreshLayout = v_findView(getActivity(), R.id.refreshLayout);
            View detailLabelSection = v_findView(mContentSection, R.id.section_label_detail);

            Action0 itemClickHandler = () -> {
                showActivity(this, an_StockChartDetailPage(mStock.index, TYPE_KLINE_DAY, authorityType, specType));
            };
            Action3<Float, ChartInfo, List<ChartInfo>> itemLongClickHandler = (x, info, visibleData) -> {
                mLineView.drawLine(x, info, visibleData, TYPE_KLINE, TYPE_CHART_SMALL);
                v_setVisible(mLineView);
                v_setVisible(detailLabelSection);
                v_setGone(timesDetailSection);
                refreshLayout.setEnabled(false);
                v_setVisible(klineDetailSection);

                updateLongPressSection(chartDetailSection, klineDetailSection, x, info);
                animateToUp(tabSection, chartDetailSection);
            };
            Action0 itemLongUpHandler = () -> {
                refreshLayout.setEnabled(true);
                v_setGone(mLineView);
                v_setGone(detailLabelSection);
                animateToDown(tabSection, chartDetailSection);
            };
            if (mListener == null) {
                mListener = new ChartContainerOnTouchListener(mChartContainer, itemClickHandler, itemLongClickHandler, itemLongUpHandler, true, TYPE_KLINE);
            }
            mChartContainer.setOnTouchListener(mListener);

            KLineChartRateInfo info = new KLineChartRateInfo();
            consumeEvent(StockChartDetailFragment.sRefreshChartValueSubject)
                    .setTag("refresh_value")
                    .onNextFinish(fresh -> {
                        info.setData(mRender);
                        setupTextSection(info);
                    })
                    .done();
        }

        private void updateLongPressSection(View chartDetailSection, View klineDetailSection, float x, ChartInfo info) {
            ChartLongPressInfo pressInfo = new ChartLongPressInfo(info);

            v_setText(chartDetailSection, R.id.label_title, pressInfo.smallKLineChangeRatio);
            v_setText(klineDetailSection, R.id.label_column_1, pressInfo.openPrice);
            v_setText(klineDetailSection, R.id.label_column_2, pressInfo.maxPrice);
            v_setText(klineDetailSection, R.id.label_column_3, pressInfo.minPrice);
            v_setText(klineDetailSection, R.id.label_column_4, pressInfo.closePrice);

            View detailLabelSection = v_findView(mContentSection, R.id.section_label_detail);
            View bottomLabel = v_findView(detailLabelSection, R.id.label_bottom_chart);
            FrameLayout.LayoutParams detailParams = (FrameLayout.LayoutParams) detailLabelSection.getLayoutParams();
            FrameLayout.LayoutParams bottomParams = (FrameLayout.LayoutParams) bottomLabel.getLayoutParams();
            if (x <= mChartContainer.getMeasuredWidth() / 2) {
                detailParams.gravity = Gravity.RIGHT;
                bottomParams.gravity = Gravity.RIGHT;
            } else {
                detailParams.gravity = Gravity.LEFT;
                bottomParams.gravity = Gravity.LEFT;
            }
            detailLabelSection.setLayoutParams(detailParams);
            bottomLabel.setLayoutParams(bottomParams);

            v_setText(detailLabelSection, R.id.label_upper_chart, pressInfo.maField);
            if (mRender.mEnvDataPager.mSpecType == Spec_Type_NONE) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.volumeField);
            } else if (mRender.mEnvDataPager.mSpecType == Spec_Type_MACD) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.macdField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_KDJ) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.kdjField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_RSI) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.rsiField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_BOLL) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.bollField);
            }
        }

        private void setupTextSection(KLineChartRateInfo info) {

            View leftTopSection = v_findView(mContentSection, R.id.section_left_top);
            View leftBottomSection = v_findView(mContentSection, R.id.section_left_bottom);

            v_setText(leftTopSection, R.id.label_max_price, info.maxValue);
            v_setText(leftTopSection, R.id.label_min_price, info.minValue);

            if (info.specType == Spec_Type_NONE) {
                v_setGone(leftBottomSection, R.id.label_top);
            } else if (info.specType == KLineData.Spec_Type_MACD) {
                v_setText(leftBottomSection, R.id.label_top, info.macdNormalTilte);
                v_setVisible(leftBottomSection, R.id.label_top);
            } else if (info.specType == KLineData.Spec_Type_KDJ) {
                v_setText(leftBottomSection, R.id.label_top, info.kdjNormalTilte);
                v_setVisible(leftBottomSection, R.id.label_top);
            } else if (info.specType == KLineData.Spec_Type_RSI) {
                v_setText(leftBottomSection, R.id.label_top, info.rsiNormalTilte);
                v_setVisible(leftBottomSection, R.id.label_top);
            } else if (info.specType == KLineData.Spec_Type_BOLL) {
                v_setText(leftBottomSection, R.id.label_top, info.bollNormalTilte);
                v_setVisible(leftBottomSection, R.id.label_top);
            }
        }

        private List<ChartInfo> formatChartData(List<KLineData> data) {
            List<ChartController.ChartInfo> chartList = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                ChartInfo info;
                if (i != 0 && !formatSecond(data.get(i).traderTime, "yyyy-MM").equals(formatSecond(data.get(i - 1).traderTime, "yyyy-MM"))) {
                    info = new ChartInfo(data.get(i), true);
                } else {
                    info = new ChartInfo(data.get(i), false);
                }
                chartList.add(i, info);
            }
            Collections.reverse(chartList);
            return chartList;
        }

        private boolean mIsAnimating = false;
        private boolean mIsAnimationToUp = false;

        private void animateToUp(View headerSection, View chartDetailSection) {
            if (mIsAnimating || mIsAnimationToUp)
                return;
            mIsAnimating = true;
            ObjectAnimator animator = ObjectAnimator.ofFloat(headerSection, "translationY", 0, -chartDetailSection.getMeasuredHeight());
            animator.setDuration(200);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsAnimating = false;
                    mIsAnimationToUp = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }

        private void animateToDown(View headerSection, View chartDetailSection) {
            if (mIsAnimating)
                return;
            mIsAnimating = true;
            ObjectAnimator animator = ObjectAnimator.ofFloat(headerSection, "translationY", -chartDetailSection.getMeasuredHeight(), 0);
            animator.setDuration(200);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsAnimating = false;
                    mIsAnimationToUp = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }
    }

    public static class WeekKLineChartFragment extends SimpleFragment {
        private Stock mStock;
        private int mAuthorityType;
        private int mSpecType;
        private EnvDataPager mEnvDataPager;
        private ChartViewContainer<ChartInfo> mChartContainer;
        private WeekKLineChartRender mRender;
        private DrawLineView mLineView;
        private ChartContainerOnTouchListener mListener;

        public WeekKLineChartFragment init(Stock stock, int authorityType, int specType) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(Stock.class.getName(), stock);
            arguments.putInt("authority_type", authorityType);
            arguments.putInt("spec_type", specType);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockWeek);
            mStock = (Stock) getArguments().getSerializable(Stock.class.getName());
            mAuthorityType = getArguments().getInt("authority_type");
            mSpecType = getArguments().getInt("spec_type");
            return inflater.inflate(R.layout.frag_kline_chart, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            initView();
            v_setClick(mReloadSection, v -> {
                v_setGone(v);
                v_setGone(mContentSection);
                v_setVisible(mLoadingSection);
                fetchData(false, mAuthorityType, mSpecType);
            });
            fetchData(true, mAuthorityType, mSpecType);
        }

        private void initView() {
            mChartContainer = v_findView(mContentSection, R.id.chart_container);
            mChartContainer.setLayoutDelegate(new EndToStartLayoutDelegate<ChartController.ChartInfo>(getActivity()));
            mLineView = v_findView(mContentSection, R.id.line_view);
        }

        private void fetchData(boolean reload, int authorityType, int specType) {
            consumeEventMRUpdateUI(fetchStockKLineData(reload, mStock, authorityType, KLineData.Kline_Type_Week, specType), reload)
                    .setTag("week_kline")
                    .onNextSuccess(response -> {
                        updateContentSection(response.data, authorityType, specType);
                    })
                    .done();
        }

        private void updateContentSection(StockKline data, int authorityType, int specType) {
            List<ChartController.ChartInfo> chartList = formatChartData(data.data());

            ChartViewContainer.DEBUG = true;
            if (mEnvDataPager == null) {
                mEnvDataPager = new EnvDataPager();
            }
            mEnvDataPager.setChartData(chartList, StockDetailFragment.TYPE_KLINE, specType, true);
            mChartContainer.setDataSourceDelegate(new DataSourceDelegate<ChartInfo>() {
                @Override
                public List<ChartInfo> getAllData() {
                    return mEnvDataPager.mChartData;
                }

                @Override
                public int getDataCountPerPage(List<ChartInfo> list) {
                    return EnvDataPager.DEFAULT_COUNT;
                }

            });
            if (mRender == null) {
                mRender = new WeekKLineChartRender();
            }
            mRender.setEnvData(mEnvDataPager);
            mChartContainer.setRenderDelegate(mRender);
            mChartContainer.notifyDataSetChanged();

            View tabSection = v_findView(getActivity(), R.id.section_tab);
            View chartDetailSection = v_findView(getActivity(), R.id.section_chart_detail);
            View timesDetailSection = v_findView(chartDetailSection, R.id.section_times_detail);
            View klineDetailSection = v_findView(chartDetailSection, R.id.section_kline_detail);
            AdvanceSwipeRefreshLayout refreshLayout = v_findView(getActivity(), R.id.refreshLayout);
            View detailLabelSection = v_findView(mContentSection, R.id.section_label_detail);

            Action0 itemClickHandler = () -> {
                showActivity(this, an_StockChartDetailPage(mStock.index, TYPE_KLINE_WEEK, authorityType, specType));
            };
            Action3<Float, ChartInfo, List<ChartInfo>> itemLongClickHandler = (x, info, visibleData) -> {
                mLineView.drawLine(x, info, visibleData, TYPE_KLINE, TYPE_CHART_SMALL);
                v_setVisible(mLineView);
                v_setVisible(detailLabelSection);
                v_setGone(timesDetailSection);
                refreshLayout.setEnabled(false);
                v_setVisible(klineDetailSection);
                updateLongPressSection(chartDetailSection, klineDetailSection, x, info);

                animateToUp(tabSection, chartDetailSection);
            };
            Action0 itemLongUpHandler = () -> {
                refreshLayout.setEnabled(true);
                v_setGone(mLineView);
                v_setGone(detailLabelSection);
                animateToDown(tabSection, chartDetailSection);
            };
            if (mListener == null) {
                mListener = new ChartContainerOnTouchListener(mChartContainer, itemClickHandler, itemLongClickHandler, itemLongUpHandler, true, TYPE_KLINE);
            }
            mChartContainer.setOnTouchListener(mListener);

            KLineChartRateInfo info = new KLineChartRateInfo();
            consumeEvent(StockChartDetailFragment.sRefreshChartValueSubject)
                    .setTag("refresh_value")
                    .onNextFinish(fresh -> {
                        info.setData(mRender);
                        setupTextSection(info);
                    })
                    .done();
        }

        private void updateLongPressSection(View chartDetailSection, View klineDetailSection, float x, ChartInfo info) {
            ChartLongPressInfo pressInfo = new ChartLongPressInfo(info);

            v_setText(chartDetailSection, R.id.label_title, pressInfo.smallKLineChangeRatio);
            v_setText(klineDetailSection, R.id.label_column_1, pressInfo.openPrice);
            v_setText(klineDetailSection, R.id.label_column_2, pressInfo.maxPrice);
            v_setText(klineDetailSection, R.id.label_column_3, pressInfo.minPrice);
            v_setText(klineDetailSection, R.id.label_column_4, pressInfo.closePrice);

            View detailLabelSection = v_findView(mContentSection, R.id.section_label_detail);
            View bottomLabel = v_findView(detailLabelSection, R.id.label_bottom_chart);
            FrameLayout.LayoutParams detailParams = (FrameLayout.LayoutParams) detailLabelSection.getLayoutParams();
            FrameLayout.LayoutParams bottomParams = (FrameLayout.LayoutParams) bottomLabel.getLayoutParams();
            if (x <= mChartContainer.getMeasuredWidth() / 2) {
                detailParams.gravity = Gravity.RIGHT;
                bottomParams.gravity = Gravity.RIGHT;
            } else {
                detailParams.gravity = Gravity.LEFT;
                bottomParams.gravity = Gravity.LEFT;
            }
            detailLabelSection.setLayoutParams(detailParams);
            bottomLabel.setLayoutParams(bottomParams);

            v_setText(detailLabelSection, R.id.label_upper_chart, pressInfo.maField);
            if (mRender.mEnvDataPager.mSpecType == Spec_Type_NONE) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.volumeField);
            } else if (mRender.mEnvDataPager.mSpecType == Spec_Type_MACD) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.macdField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_KDJ) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.kdjField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_RSI) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.rsiField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_BOLL) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.bollField);
            }
        }

        private void setupTextSection(KLineChartRateInfo info) {

            View leftTopSection = v_findView(mContentSection, R.id.section_left_top);
            View leftBottomSection = v_findView(mContentSection, R.id.section_left_bottom);

            v_setText(leftTopSection, R.id.label_max_price, info.maxValue);
            v_setText(leftTopSection, R.id.label_min_price, info.minValue);

            if (info.specType == Spec_Type_NONE) {
                v_setGone(leftBottomSection, R.id.label_top);
            } else if (info.specType == KLineData.Spec_Type_MACD) {
                v_setText(leftBottomSection, R.id.label_top, info.macdNormalTilte);
                v_setVisible(leftBottomSection, R.id.label_top);
            } else if (info.specType == KLineData.Spec_Type_KDJ) {
                v_setText(leftBottomSection, R.id.label_top, info.kdjNormalTilte);
                v_setVisible(leftBottomSection, R.id.label_top);
            } else if (info.specType == KLineData.Spec_Type_RSI) {
                v_setText(leftBottomSection, R.id.label_top, info.rsiNormalTilte);
                v_setVisible(leftBottomSection, R.id.label_top);
            } else if (info.specType == KLineData.Spec_Type_BOLL) {
                v_setText(leftBottomSection, R.id.label_top, info.bollNormalTilte);
                v_setVisible(leftBottomSection, R.id.label_top);
            }
        }

        private List<ChartInfo> formatChartData(List<KLineData> data) {
            List<ChartController.ChartInfo> chartList = new ArrayList<>();
            int preIndex = 0;
            for (int i = 0; i < data.size(); i++) {
                ChartInfo info;
                if (i != 0 && Math.abs(Integer.valueOf(formatSecond(data.get(i).traderTime, "MM")) - Integer.valueOf(formatSecond(data.get(preIndex).traderTime, "MM"))) >= 6) {
                    info = new ChartInfo(data.get(i), true);
                    preIndex = i;
                } else {
                    info = new ChartController.ChartInfo(data.get(i), false);
                }
                chartList.add(i, info);
            }
            Collections.reverse(chartList);
            return chartList;
        }

        private boolean mIsAnimating = false;
        private boolean mIsAnimationToUp = false;

        private void animateToUp(View headerSection, View chartDetailSection) {
            if (mIsAnimating || mIsAnimationToUp)
                return;
            mIsAnimating = true;
            ObjectAnimator animator = ObjectAnimator.ofFloat(headerSection, "translationY", 0, -chartDetailSection.getMeasuredHeight());
            animator.setDuration(200);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsAnimating = false;
                    mIsAnimationToUp = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }

        private void animateToDown(View headerSection, View chartDetailSection) {
            if (mIsAnimating)
                return;
            mIsAnimating = true;
            ObjectAnimator animator = ObjectAnimator.ofFloat(headerSection, "translationY", -chartDetailSection.getMeasuredHeight(), 0);
            animator.setDuration(200);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsAnimating = false;
                    mIsAnimationToUp = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }
    }

    public static class MonthKLineFragment extends SimpleFragment {
        private Stock mStock;
        private int mAuthorityType;
        private int mSpecType;
        private EnvDataPager mEnvDataPager;
        private ChartViewContainer<ChartInfo> mChartContainer;
        private MonthKLineChartRender mRender;
        private DrawLineView mLineView;
        private ChartContainerOnTouchListener mListener;

        public MonthKLineFragment init(Stock stock, int authorityType, int specType) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(Stock.class.getName(), stock);
            arguments.putInt("authority_type", authorityType);
            arguments.putInt("spec_type", specType);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockMonth);
            mStock = (Stock) getArguments().getSerializable(Stock.class.getName());
            mAuthorityType = getArguments().getInt("authority_type");
            mSpecType = getArguments().getInt("spec_type");
            return inflater.inflate(R.layout.frag_kline_chart, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            initView();
            v_setClick(mReloadSection, v -> {
                v_setGone(v);
                v_setGone(mContentSection);
                v_setVisible(mLoadingSection);
                fetchData(false, mAuthorityType, mSpecType);
            });
            fetchData(true, mAuthorityType, mSpecType);
        }

        private void initView() {
            mChartContainer = v_findView(mContentSection, R.id.chart_container);
            mChartContainer.setLayoutDelegate(new EndToStartLayoutDelegate<ChartController.ChartInfo>(getActivity()));
            mLineView = v_findView(mContentSection, R.id.line_view);
        }

        private void fetchData(boolean reload, int authorityType, int specType) {
            if (reload) {
                v_setGone(mContentSection);
                v_setGone(mReloadSection);
                v_setVisible(mLoadingSection);
            }

            consumeEventMRUpdateUI(fetchStockKLineData(reload, mStock, authorityType, KLineData.Kline_Type_Month, specType), reload)
                    .setTag("month_kline")
                    .onNextSuccess(response -> {
                        updateContentSection(response.data, authorityType, specType);
                    })
                    .done();
        }

        private void updateContentSection(StockKline data, int authorityType, int specType) {
            List<ChartInfo> chartList = formatChartData(data.data());

            ChartViewContainer.DEBUG = true;
            if (mEnvDataPager == null) {
                mEnvDataPager = new EnvDataPager();
            }
            mEnvDataPager.setChartData(chartList, StockDetailFragment.TYPE_KLINE, specType, true);
            mChartContainer.setDataSourceDelegate(new DataSourceDelegate<ChartInfo>() {
                @Override
                public List<ChartInfo> getAllData() {
                    return mEnvDataPager.mChartData;
                }

                @Override
                public int getDataCountPerPage(List<ChartInfo> list) {
                    return EnvDataPager.DEFAULT_COUNT;
                }

            });
            if (mRender == null) {
                mRender = new MonthKLineChartRender();
            }
            mRender.setEnvData(mEnvDataPager);
            mChartContainer.setRenderDelegate(mRender);
            mChartContainer.notifyDataSetChanged();

            View tabSection = v_findView(getActivity(), R.id.section_tab);
            View chartDetailSection = v_findView(getActivity(), R.id.section_chart_detail);
            View timesDetailSection = v_findView(chartDetailSection, R.id.section_times_detail);
            View klineDetailSection = v_findView(chartDetailSection, R.id.section_kline_detail);
            AdvanceSwipeRefreshLayout refreshLayout = v_findView(getActivity(), R.id.refreshLayout);
            View detailLabelSection = v_findView(mContentSection, R.id.section_label_detail);

            Action0 itemClickHandler = () -> {
                showActivity(this, an_StockChartDetailPage(mStock.index, TYPE_KLINE_MONTH, authorityType, specType));
            };
            Action3<Float, ChartInfo, List<ChartInfo>> itemLongClickHandler = (x, info, visibleData) -> {
                mLineView.drawLine(x, info, visibleData, TYPE_KLINE, TYPE_CHART_SMALL);
                updateLongPressSection(chartDetailSection, klineDetailSection, x, info);
                v_setVisible(mLineView);
                v_setVisible(detailLabelSection);
                v_setGone(timesDetailSection);
                refreshLayout.setEnabled(false);

                if (info != null) {

                }
                v_setVisible(klineDetailSection);
                animateToUp(tabSection, chartDetailSection);
            };
            Action0 itemLongUpHandler = () -> {
                refreshLayout.setEnabled(true);
                v_setGone(mLineView);
                v_setGone(detailLabelSection);
                animateToDown(tabSection, chartDetailSection);
            };
            if (mListener == null) {
                mListener = new ChartContainerOnTouchListener(mChartContainer, itemClickHandler, itemLongClickHandler, itemLongUpHandler, true, TYPE_KLINE);
            }
            mChartContainer.setOnTouchListener(mListener);

            KLineChartRateInfo info = new KLineChartRateInfo();
            consumeEvent(StockChartDetailFragment.sRefreshChartValueSubject)
                    .setTag("refresh_value")
                    .onNextFinish(fresh -> {
                        info.setData(mRender);
                        setupTextSection(info);
                    })
                    .done();
        }

        private void updateLongPressSection(View chartDetailSection, View klineDetailSection, float x, ChartInfo info) {
            ChartLongPressInfo pressInfo = new ChartLongPressInfo(info);

            v_setText(chartDetailSection, R.id.label_title, pressInfo.smallKLineChangeRatio);
            v_setText(klineDetailSection, R.id.label_column_1, pressInfo.openPrice);
            v_setText(klineDetailSection, R.id.label_column_2, pressInfo.maxPrice);
            v_setText(klineDetailSection, R.id.label_column_3, pressInfo.minPrice);
            v_setText(klineDetailSection, R.id.label_column_4, pressInfo.closePrice);

            View detailLabelSection = v_findView(mContentSection, R.id.section_label_detail);
            View bottomLabel = v_findView(detailLabelSection, R.id.label_bottom_chart);
            FrameLayout.LayoutParams detailParams = (FrameLayout.LayoutParams) detailLabelSection.getLayoutParams();
            FrameLayout.LayoutParams bottomParams = (FrameLayout.LayoutParams) bottomLabel.getLayoutParams();
            if (x <= mChartContainer.getMeasuredWidth() / 2) {
                detailParams.gravity = Gravity.RIGHT;
                bottomParams.gravity = Gravity.RIGHT;
            } else {
                detailParams.gravity = Gravity.LEFT;
                bottomParams.gravity = Gravity.LEFT;
            }
            detailLabelSection.setLayoutParams(detailParams);
            bottomLabel.setLayoutParams(bottomParams);

            v_setText(detailLabelSection, R.id.label_upper_chart, pressInfo.maField);
            if (mRender.mEnvDataPager.mSpecType == Spec_Type_NONE) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.volumeField);
            } else if (mRender.mEnvDataPager.mSpecType == Spec_Type_MACD) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.macdField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_KDJ) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.kdjField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_RSI) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.rsiField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_BOLL) {
                v_setText(detailLabelSection, R.id.label_bottom_chart, pressInfo.bollField);
            }
        }

        private void setupTextSection(KLineChartRateInfo info) {

            View leftTopSection = v_findView(mContentSection, R.id.section_left_top);
            View leftBottomSection = v_findView(mContentSection, R.id.section_left_bottom);

            v_setText(leftTopSection, R.id.label_max_price, info.maxValue);
            v_setText(leftTopSection, R.id.label_min_price, info.minValue);

            if (info.specType == Spec_Type_NONE) {
                v_setGone(leftBottomSection, R.id.label_top);
            } else if (info.specType == KLineData.Spec_Type_MACD) {
                v_setText(leftBottomSection, R.id.label_top, info.macdNormalTilte);
                v_setVisible(leftBottomSection, R.id.label_top);
            } else if (info.specType == KLineData.Spec_Type_KDJ) {
                v_setText(leftBottomSection, R.id.label_top, info.kdjNormalTilte);
                v_setVisible(leftBottomSection, R.id.label_top);
            } else if (info.specType == KLineData.Spec_Type_RSI) {
                v_setText(leftBottomSection, R.id.label_top, info.rsiNormalTilte);
                v_setVisible(leftBottomSection, R.id.label_top);
            } else if (info.specType == KLineData.Spec_Type_BOLL) {
                v_setText(leftBottomSection, R.id.label_top, info.bollNormalTilte);
                v_setVisible(leftBottomSection, R.id.label_top);
            }
        }

        private List<ChartInfo> formatChartData(List<KLineData> data) {
            List<ChartController.ChartInfo> chartList = new ArrayList<>();
            int preIndex = 0;
            for (int i = 0; i < data.size(); i++) {
                ChartInfo info;
                if (i != 0 && Math.abs(Integer.valueOf(formatSecond(data.get(i).traderTime, "yyyy")) - Integer.valueOf(formatSecond(data.get(preIndex).traderTime, "yyyy"))) >= 2) {
                    info = new ChartController.ChartInfo(data.get(i), true);
                    preIndex = i;
                } else {
                    info = new ChartController.ChartInfo(data.get(i), false);
                }
                chartList.add(i, info);
            }
            Collections.reverse(chartList);
            return chartList;
        }

        private boolean mIsAnimating = false;
        private boolean mIsAnimationToUp = false;

        private void animateToUp(View headerSection, View chartDetailSection) {
            if (mIsAnimating || mIsAnimationToUp)
                return;
            mIsAnimating = true;
            ObjectAnimator animator = ObjectAnimator.ofFloat(headerSection, "translationY", 0, -chartDetailSection.getMeasuredHeight());
            animator.setDuration(200);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsAnimating = false;
                    mIsAnimationToUp = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }

        private void animateToDown(View headerSection, View chartDetailSection) {
            if (mIsAnimating)
                return;
            mIsAnimating = true;
            ObjectAnimator animator = ObjectAnimator.ofFloat(headerSection, "translationY", -chartDetailSection.getMeasuredHeight(), 0);
            animator.setDuration(200);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIsAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mIsAnimating = false;
                    mIsAnimationToUp = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        }
    }
}