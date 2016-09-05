
package com.goldmf.GMFund.controller;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment;
import com.goldmf.GMFund.extension.ViewExtension;
import com.goldmf.GMFund.extension.ViewGroupExtension;
import com.goldmf.GMFund.model.LineData.KLineData;
import com.goldmf.GMFund.model.LineData.TLineData;
import com.goldmf.GMFund.model.Stock;
import com.goldmf.GMFund.model.StockData.StockKline;
import com.goldmf.GMFund.model.StockData.StockTline;
import com.goldmf.GMFund.protocol.base.PageArrayHelper;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.ChartContainerOnTouchListener;
import com.goldmf.GMFund.widget.ChartController;
import com.goldmf.GMFund.widget.ChartController.ChartInfo;
import com.goldmf.GMFund.widget.ChartController.EnvDataPager;
import com.goldmf.GMFund.widget.DayKLineDetailChartRender;
import com.goldmf.GMFund.widget.DrawFetchMoreView;
import com.goldmf.GMFund.widget.DrawLineView;
import com.goldmf.GMFund.widget.FiveDayDetailChartRender;
import com.goldmf.GMFund.widget.FiveOrderCell;
import com.goldmf.GMFund.widget.KLineChartBaseRender;
import com.goldmf.GMFund.widget.MonthKLineDetailChartRender;
import com.goldmf.GMFund.widget.TimesDetailChartRender;
import com.goldmf.GMFund.widget.WeekKLineDetailChartRender;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.yale.infinitychartview.lib.ChartViewContainer;
import io.yale.infinitychartview.lib.DataSourceDelegate;
import io.yale.infinitychartview.lib.InfinityChartView;
import io.yale.infinitychartview.lib.layout.EndToStartLayoutDelegate;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Action3;
import rx.functions.Func0;
import rx.subjects.PublishSubject;
import yale.extension.common.Optional;
import yale.extension.common.shape.RoundCornerShape;

import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_STOCK_ID_STRING;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_CHART_BIG;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_FIVE_DAY_TLINE;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_KLINE;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_KLINE_DAY;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_KLINE_MONTH;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_KLINE_WEEK;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_TIMES_TLINE;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_TLINE_DAY;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_TLINE_FIVE_DAY;
import static com.goldmf.GMFund.controller.StockChartFragments.StockChartDetailFragment.TYPE_CHANGE_AUTHORITY;
import static com.goldmf.GMFund.controller.StockChartFragments.StockChartDetailFragment.sRefreshChartValueSubject;
import static com.goldmf.GMFund.controller.business.StockController.fetchStockDetailInfo;
import static com.goldmf.GMFund.controller.business.StockController.fetchStockKLineData;
import static com.goldmf.GMFund.controller.business.StockController.fetchStockTLineData;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_BLUE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_PURPLE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_YELLOW_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getFiveOrderPriceColor;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.zipToList;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.model.LineData.GMFStockAnswer_Authority_Backward;
import static com.goldmf.GMFund.model.LineData.GMFStockAnswer_Authority_Forward;
import static com.goldmf.GMFund.model.LineData.GMFStockAnswer_Authority_No;
import static com.goldmf.GMFund.model.LineData.KLineData.Spec_Type_BOLL;
import static com.goldmf.GMFund.model.LineData.KLineData.Spec_Type_KDJ;
import static com.goldmf.GMFund.model.LineData.KLineData.Spec_Type_MACD;
import static com.goldmf.GMFund.model.LineData.KLineData.Spec_Type_NONE;
import static com.goldmf.GMFund.model.LineData.KLineData.Spec_Type_RSI;
import static com.goldmf.GMFund.model.LineData.TLineData.Tline_Type_5Day;
import static com.goldmf.GMFund.model.LineData.TLineData.Tline_Type_Day;
import static com.goldmf.GMFund.model.StockInfo.StockBrief.STOCK_CLASS_TYPE_GOVERNMENT_LOAN;
import static com.goldmf.GMFund.model.StockInfo.StockBrief.STOCK_CLASS_TYPE_SPEC;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.hasMoreData;
import static com.goldmf.GMFund.util.FormatUtil.computeBiggerNumberUnit;
import static com.goldmf.GMFund.util.FormatUtil.formatBigNumber;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatRatio;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;

/**
 * Created by Evan on 16/2/20 上午10:26.
 */
public class StockChartFragments {
    private StockChartFragments() {
    }

    public static class StockTypeShare {
        public int mAuthorityType;
        public int mSpecType;
        public int mTabPosition;

        public StockTypeShare(int authorityType, int specType, int tabPosition) {
            mAuthorityType = authorityType;
            mSpecType = specType;
            mTabPosition = tabPosition;
        }
    }

    public static class StockChartDetailFragment extends SimpleFragment {
        public static PublishSubject<Void> sRefreshChartValueSubject = PublishSubject.create();
        public static PublishSubject<StockTypeShare> sChartTypeChanged = PublishSubject.create();
        public static final int TYPE_CHANGE_NONE = 100;
        public static final int TYPE_CHANGE_AUTHORITY = 101;
        public static final int TYPE_CHANGE_SPEC = 102;

        private String mStockId;
        private int mChartType;
        private int mAuthorityType;
        private int mSpecType;
        private TabLayout mTabLayout;
        private View mTypeSection;
        private String[] titles = {"分时", "五日", "日K", "周K", "月K"};
        private Optional<SimpleFragment> mCurrentFragment = Optional.empty();

        public int currentAuthorityType = GMFStockAnswer_Authority_No;
        public int currentSpecType = Spec_Type_NONE;
        public int currentSelectedTab = 0;
        private boolean isFirstEnter = true;

        private interface CreateFragmentFunc extends Func0<Fragment> {
        }

        Class[] sClazzList = {
                TimesDetailChartFragment.class,
                FiveDayDetailChartFragment.class,
                DayKLineDetailChartFragment.class,
                WeekKLineDetailChartFragment.class,
                MonthKLineDetailChartFragment.class
        };

        CreateFragmentFunc[] sActionList = {
                TimesDetailChartFragment::new,
                FiveDayDetailChartFragment::new,
                DayKLineDetailChartFragment::new,
                WeekKLineDetailChartFragment::new,
                MonthKLineDetailChartFragment::new
        };

        public StockChartDetailFragment init(String stockId, int chartType, int authorityType, int specType) {
            Bundle arguments = new Bundle();
            arguments.putString(KEY_STOCK_ID_STRING, stockId);
            arguments.putInt("chart_type", chartType);
            arguments.putInt("authority_type", authorityType);
            arguments.putInt("spec_type", specType);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mStockId = getArguments().getString(KEY_STOCK_ID_STRING);
            mChartType = getArguments().getInt("chart_type");
            mAuthorityType = getArguments().getInt("authority_type");
            mSpecType = getArguments().getInt("spec_type");
            return inflater.inflate(R.layout.frag_stock_detail, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            mTabLayout = v_findView(mContentSection, R.id.tabLayout);
            mTabLayout.removeAllTabs();
            for (int i = 0; i < titles.length; i++) {
                mTabLayout.addTab(mTabLayout.newTab().setText(titles[i]));
            }
            mTypeSection = v_findView(mContentSection, R.id.section_type);

            v_setClick(mReloadSection, v -> {
                v_setGone(v);
                v_setGone(mContentSection);
                v_setVisible(mLoadingSection);
                fetchData(false);
            });
            fetchData(true);
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser) {
                Observable<MResultsInfo<Stock>> observable = fetchStockDetailInfo(false, mStockId)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .delaySubscription(10, TimeUnit.SECONDS)
                        .repeat();
                consumeEventMR(observable)
                        .setTag("reset_refresh_stock_detail")
                        .addToVisible()
                        .onNextSuccess(response -> {
                            StockDetailInfo vm = new StockDetailInfo(response.data);
                            updateContentSection(vm);
                            changeVisibleSection(TYPE_CONTENT);
                        })
                        .onNextFail(response -> {
                            setReloadSectionTips(getErrorMessage(response));
                            changeVisibleSection(TYPE_RELOAD);
                        })
                        .done();
            }
        }

        private void fetchData(boolean reload) {
            consumeEventMRUpdateUI(fetchStockDetailInfo(reload, mStockId), reload)
                    .setTag("refresh_stock_detail")
                    .onNextSuccess(response -> {
                        StockDetailInfo vm = new StockDetailInfo(response.data);
                        updateContentSection(vm);
                    })
                    .done();
        }

        private void updateContentSection(StockDetailInfo data) {
            setupHeader(data);
            setupContent(data);
        }

        private void setupHeader(StockDetailInfo data) {
            View headerSection = v_findView(mContentSection, R.id.section_header);
            v_setClick(headerSection, R.id.btn_cancel, () -> {
                sChartTypeChanged.onNext(new StockTypeShare(currentAuthorityType, currentSpecType, currentSelectedTab));
                Pair.create(currentAuthorityType, currentSpecType);
                getActivity().finish();
            });

            View headerContainer = v_findView(headerSection, R.id.header_container);
            View normalHeader = v_findView(headerContainer, R.id.header_normal);

            v_setText(normalHeader, R.id.label_stock_name_and_code, data.stockTitle);
            v_setText(normalHeader, R.id.label_last_price, data.currentPrice);
            v_setText(normalHeader, R.id.label_turnover, data.turnover);
            v_setText(normalHeader, R.id.label_change_ratio, data.changeRatio);
            v_setText(normalHeader, R.id.label_date, data.date);
        }

        private void setupContent(StockDetailInfo data) {

            int selectedPos;
            if (isFirstEnter) {
                int authorityIndex = 0;
                if (mAuthorityType == GMFStockAnswer_Authority_No) {
                    authorityIndex = 0;
                } else if (mAuthorityType == GMFStockAnswer_Authority_Forward) {
                    authorityIndex = 1;
                } else if (mAuthorityType == GMFStockAnswer_Authority_Backward) {
                    authorityIndex = 2;
                }
                int specIndex = 0;
                if (mSpecType == Spec_Type_NONE) {
                    specIndex = 0;
                } else if (mSpecType == Spec_Type_MACD) {
                    specIndex = 1;
                } else if (mSpecType == Spec_Type_KDJ) {
                    specIndex = 2;
                } else if (mSpecType == Spec_Type_RSI) {
                    specIndex = 3;
                } else if (mSpecType == Spec_Type_BOLL) {
                    specIndex = 4;
                }
                onAuthorityTabSelected(mTypeSection, authorityIndex);
                onSpecTabSelected(mTypeSection, specIndex);
                currentAuthorityType = mAuthorityType;
                currentSpecType = mSpecType;

                if (mChartType == TYPE_TLINE_DAY) {
                    v_setGone(mTypeSection);
                    selectedPos = 0;
                } else if (mChartType == TYPE_TLINE_FIVE_DAY) {
                    v_setGone(mTypeSection);
                    selectedPos = 1;
                } else if (mChartType == TYPE_KLINE_DAY) {
                    v_setVisible(mTypeSection);
                    selectedPos = 2;
                } else if (mChartType == TYPE_KLINE_WEEK) {
                    v_setVisible(mTypeSection);
                    selectedPos = 3;
                } else if (mChartType == TYPE_KLINE_MONTH) {
                    v_setVisible(mTypeSection);
                    selectedPos = 4;
                } else {
                    selectedPos = 0;
                }
                mTabLayout.getTabAt(selectedPos).select();
                currentSelectedTab = selectedPos;

                isFirstEnter = false;
            } else {
                selectedPos = mTabLayout.getSelectedTabPosition();
            }
            replaceCurrentFragment(selectedPos, sClazzList[selectedPos], sActionList[selectedPos], data.source, currentAuthorityType, currentSpecType, false, TYPE_CHANGE_NONE);

            mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(Tab tab) {
                    int position = tab.getPosition();
                    if (position == 0) {
                        v_setGone(mTypeSection);
                    } else if (position == 1) {
                        v_setGone(mTypeSection);
                    } else if (position == 2) {
                        v_setVisible(mTypeSection);
                    } else if (position == 3) {
                        v_setVisible(mTypeSection);
                    } else if (position == 4) {
                        v_setVisible(mTypeSection);
                    }
                    replaceCurrentFragment(position, sClazzList[position], sActionList[position], data.source, currentAuthorityType, currentSpecType, true, TYPE_CHANGE_NONE);
                    currentSelectedTab = position;
                }

                @Override
                public void onTabUnselected(Tab tab) {

                }

                @Override
                public void onTabReselected(Tab tab) {

                }
            });
            ViewGroupExtension.v_forEach(mTypeSection, R.id.container_authority_type, (pos, child) -> child.setOnClickListener(v -> onAuthorityTabClick(data, mTypeSection, pos)));
            ViewGroupExtension.v_forEach(mTypeSection, R.id.container_spec_type, (pos, child) -> child.setOnClickListener(v -> onSpecTabClick(data, mTypeSection, pos)));
        }

        private void onAuthorityTabClick(StockDetailInfo data, View typeSection, int index) {
            onAuthorityTabSelected(typeSection, index);
            ViewGroupExtension.v_forEach(typeSection, R.id.container_spec_type, (pos, child) -> {
                if (child.isSelected()) {

                    if (index == 0) {
                        currentAuthorityType = GMFStockAnswer_Authority_No;
                    } else if (index == 1) {
                        currentAuthorityType = GMFStockAnswer_Authority_Forward;
                    } else if (index == 2) {
                        currentAuthorityType = GMFStockAnswer_Authority_Backward;
                    }

                    if (pos == 0) {
                        currentSpecType = Spec_Type_NONE;
                    } else if (pos == 1) {
                        currentSpecType = Spec_Type_MACD;
                    } else if (pos == 2) {
                        currentSpecType = Spec_Type_KDJ;
                    } else if (pos == 3) {
                        currentSpecType = Spec_Type_RSI;
                    } else if (pos == 4) {
                        currentSpecType = Spec_Type_BOLL;
                    }

                    int position = mTabLayout.getSelectedTabPosition();
                    replaceCurrentFragment(position, sClazzList[position], sActionList[position], data.source, currentAuthorityType, currentSpecType, true, TYPE_CHANGE_AUTHORITY);
                }
            });
        }

        private void onSpecTabClick(StockDetailInfo data, View typeSection, int index) {
            onSpecTabSelected(typeSection, index);
            ViewGroupExtension.v_forEach(typeSection, R.id.container_authority_type, (pos, child) -> {
                if (child.isSelected()) {

                    if (pos == 0) {
                        currentAuthorityType = GMFStockAnswer_Authority_No;
                    } else if (pos == 1) {
                        currentAuthorityType = GMFStockAnswer_Authority_Forward;
                    } else if (pos == 2) {
                        currentAuthorityType = GMFStockAnswer_Authority_Backward;
                    }

                    if (index == 0) {
                        currentSpecType = Spec_Type_NONE;
                    } else if (index == 1) {
                        currentSpecType = Spec_Type_MACD;
                    } else if (index == 2) {
                        currentSpecType = Spec_Type_KDJ;
                    } else if (index == 3) {
                        currentSpecType = Spec_Type_RSI;
                    } else if (index == 4) {
                        currentSpecType = Spec_Type_BOLL;
                    }

                    int position = mTabLayout.getSelectedTabPosition();
                    replaceCurrentFragment(position, sClazzList[position], sActionList[position], data.source, currentAuthorityType, currentSpecType, true, TYPE_CHANGE_SPEC);
                }
            });
        }

        private void onAuthorityTabSelected(View typeSection, int index) {
            ViewGroupExtension.v_forEach(typeSection, R.id.container_authority_type, (pos, child) -> {
                if (index == pos && !child.isSelected()) {
                    child.setSelected(true);
                    ((TextView) child).setTextColor(TEXT_BLACK_COLOR);
                } else if (index != pos) {
                    if (child.isSelected())
                        child.setSelected(false);
                    ((TextView) child).setTextColor(TEXT_GREY_COLOR);
                }
            });
        }

        private void onSpecTabSelected(View typeSection, int index) {
            ViewGroupExtension.v_forEach(typeSection, R.id.container_spec_type, (pos, child) -> {
                if (index == pos && !child.isSelected()) {
                    child.setSelected(true);
                    ((TextView) child).setTextColor(TEXT_BLACK_COLOR);
                } else if (index != pos) {
                    if (child.isSelected())
                        child.setSelected(false);
                    ((TextView) child).setTextColor(TEXT_GREY_COLOR);
                }
            });
        }

        private void replaceCurrentFragment(int position, Class clazz, CreateFragmentFunc func, Stock stock, int authorityType, int specType, boolean reload, int chageType) {
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            SimpleFragment cacheFragment = (SimpleFragment) fm.findFragmentByTag(clazz.getSimpleName());
            SimpleFragment newFragment = cacheFragment == null ? (SimpleFragment) func.call() : null;

            if (cacheFragment != null) {
                if (position == 0) {
                    TimesDetailChartFragment fragment = ((TimesDetailChartFragment) cacheFragment);
                    fragment.fetchData(reload);
                    transaction.show(fragment);
                    fragment.setUserVisibleHint(true);
                } else if (position == 1) {
                    FiveDayDetailChartFragment fragment = ((FiveDayDetailChartFragment) cacheFragment);
                    fragment.fetchData(reload);
                    transaction.show(fragment);
                    fragment.setUserVisibleHint(true);
                } else if (position == 2) {
                    DayKLineDetailChartFragment fragment = ((DayKLineDetailChartFragment) cacheFragment);
                    fragment.fetchData(reload, authorityType, specType, chageType);
                    transaction.show(fragment);
                    fragment.setUserVisibleHint(true);
                } else if (position == 3) {
                    WeekKLineDetailChartFragment fragment = ((WeekKLineDetailChartFragment) cacheFragment);
                    fragment.fetchData(reload, authorityType, specType, chageType);
                    transaction.show(fragment);
                    fragment.setUserVisibleHint(true);
                } else if (position == 4) {
                    MonthKLineDetailChartFragment fragment = ((MonthKLineDetailChartFragment) cacheFragment);
                    fragment.fetchData(reload, authorityType, specType, chageType);
                    transaction.show(fragment);
                    fragment.setUserVisibleHint(true);
                }
            } else {
                if (position == 0) {
                    TimesDetailChartFragment fragment = ((TimesDetailChartFragment) newFragment).init(stock);
                    transaction.add(R.id.container_fragment, fragment, clazz.getSimpleName());
                } else if (position == 1) {
                    FiveDayDetailChartFragment fragment = ((FiveDayDetailChartFragment) newFragment).init(stock);
                    transaction.add(R.id.container_fragment, fragment, clazz.getSimpleName());
                } else if (position == 2) {
                    DayKLineDetailChartFragment fragment = ((DayKLineDetailChartFragment) newFragment).init(stock, authorityType, specType, chageType);
                    transaction.add(R.id.container_fragment, fragment, clazz.getSimpleName());
                } else if (position == 3) {
                    WeekKLineDetailChartFragment fragment = ((WeekKLineDetailChartFragment) newFragment).init(stock, authorityType, specType, chageType);
                    transaction.add(R.id.container_fragment, fragment, clazz.getSimpleName());
                } else if (position == 4) {
                    MonthKLineDetailChartFragment fragment = ((MonthKLineDetailChartFragment) newFragment).init(stock, authorityType, specType, chageType);
                    transaction.add(R.id.container_fragment, fragment, clazz.getSimpleName());
                }
            }

            if (mCurrentFragment.isPresent() && (mCurrentFragment.get() != cacheFragment)) {
                transaction.hide(mCurrentFragment.get());
                mCurrentFragment.get().setUserVisibleHint(false);
            }
            if ((mCurrentFragment.isAbsent() || mCurrentFragment.get() != cacheFragment)) {
                transaction.commit();
            }
            mCurrentFragment = Optional.of(cacheFragment != null ? cacheFragment : newFragment);
        }
    }

    public static class StockDetailInfo {

        public Stock source;
        public Boolean isSuspension;
        public CharSequence stockId;
        public CharSequence stockName;
        public CharSequence stockCode;
        public CharSequence stockTitle;
        public CharSequence turnover;
        public CharSequence date;
        public CharSequence currentPrice;
        public CharSequence changeRatio;

        public StockDetailInfo(Stock stock) {
            source = stock;
            if (stock != null) {
                stockId = stock.index;
                isSuspension = stock.suspension;
                stockName = stock.name;
                stockCode = stock.code;
                stockTitle = setFontSize(setColor(stock.name + " | " + stock.code, TEXT_BLACK_COLOR), sp2px(16));
                currentPrice = setFontSize(setColor(formatMoney(stock.last, false, 2), getFiveOrderPriceColor(stock.last, stock.prevClose)), sp2px(16));
                turnover = setFontSize(setColor(formatBigNumber(stock.change, true, 2), getFiveOrderPriceColor(stock.last, stock.prevClose)), sp2px(16));
                changeRatio = setFontSize(setColor(formatRatio(stock.changeRatio, true, 2), getFiveOrderPriceColor(stock.last, stock.prevClose)), sp2px(16));
                date = setFontSize(setColor(formatSecond(stock.lastFreshTime, "yyyy-MM-dd HH:mm"), TEXT_GREY_COLOR), sp2px(12));

            } else {
                stockId = null;
                isSuspension = false;
                stockName = "--";
                stockCode = "--";
                stockTitle = setFontSize(setColor(stockName + " " + stockCode, WHITE_COLOR), sp2px(14));
                currentPrice = setFontSize("--", sp2px(52));
                turnover = setFontSize("--", sp2px(16));
                changeRatio = setFontSize("--", sp2px(16));
                date = concatNoBreak("--", " ", "--");
            }
        }
    }

    public static class TimesDetailChartFragment extends SimpleFragment {

        private Stock mStock;
        private EnvDataPager mEnvDataPager;
        private ChartViewContainer<ChartInfo> mChartContainer;
        private TimesDetailChartRender mRender;
        private FrameLayout mChartSection;
        private DrawLineView mLineView;
        private ChartContainerOnTouchListener mListener;

        public TimesDetailChartFragment init(Stock stock) {
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
            return inflater.inflate(R.layout.frag_times_detail_chart, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            initView();
            v_setClick(mReloadSection, v -> {
                v_setGone(v);
                v_setGone(mEmptySection);
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
            if (mStock.stockClassType == STOCK_CLASS_TYPE_GOVERNMENT_LOAN || mStock.stockClassType == STOCK_CLASS_TYPE_SPEC) {
                consumeEventMRUpdateUI(fetchStockTLineData(mStock, GMFStockAnswer_Authority_No, Tline_Type_Day), reload)
                        .onNextSuccess(response -> {
                            updateContentSection(response.data);
                            v_setGone(mContentSection, R.id.section_five_order);
                        })
                        .done();
            } else {
                Observable<List<MResultsInfo>> observable = zipToList(fetchStockDetailInfo(reload, mStock.index),
                        fetchStockTLineData(mStock, GMFStockAnswer_Authority_No, Tline_Type_Day));
                consumeEventMRListUpdateUI(observable, reload)
                        .onNextSuccess(responses -> {
                            MResultsInfo<Stock> stockCallback = responses.get(0);
                            MResultsInfo<StockTline> tLineCallback = responses.get(1);
                            updateFiveOrderSection(stockCallback.data);
                            updateContentSection(tLineCallback.data);
                        })
                        .done();
            }
        }

        private void updateFiveOrderSection(Stock stock) {
            View parent = v_findView(this, R.id.section_five_order);

            {
                Iterator<? extends FiveOrderCell> cells = Stream.of(R.id.cell_bid1, R.id.cell_bid2, R.id.cell_bid3, R.id.cell_bid4, R.id.cell_bid5).map(it -> (FiveOrderCell) v_findView(parent, it)).getIterator();
                Iterator<? extends Stock.Quotation> quotations = Stream.of(stock.bids).getIterator();

                while (cells.hasNext() && quotations.hasNext()) {
                    FiveOrderCell cell = cells.next();
                    Stock.Quotation quotation = quotations.next();
                    cell.setPrice(quotation.price, stock.prevClose);
                    cell.setCount((long) quotation.amount);
                }
            }

            {
                Iterator<? extends FiveOrderCell> cells = Stream.of(R.id.cell_ask1, R.id.cell_ask2, R.id.cell_ask3, R.id.cell_ask4, R.id.cell_ask5).map(it -> (FiveOrderCell) v_findView(parent, it)).getIterator();
                Iterator<? extends Stock.Quotation> quotations = Stream.of(stock.asks).getIterator();

                while (cells.hasNext() && quotations.hasNext()) {
                    FiveOrderCell cell = cells.next();
                    Stock.Quotation quotation = quotations.next();
                    cell.setPrice(quotation.price, stock.prevClose);
                    cell.setCount((long) quotation.amount);
                }
            }
        }

        private void updateContentSection(StockTline data) {
            List<ChartInfo> chartList = formatChartData(data.data);

            ChartViewContainer.DEBUG = true;
            setupLightPoint(chartList);
            if (mEnvDataPager == null) {
                mEnvDataPager = new EnvDataPager();
            }
            mEnvDataPager.setChartData(chartList, StockDetailFragment.TYPE_TIMES_TLINE, Spec_Type_NONE, false);
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
                mRender = new TimesDetailChartRender();
            }
            mRender.setEnvData(mEnvDataPager);
            mChartContainer.setRenderDelegate(mRender);
            mChartContainer.notifyDataSetChanged();

            View headerContainer = v_findView(getActivity(), R.id.header_container);
            View normalHeader = v_findView(headerContainer, R.id.header_normal);
            View timesLineHeader = v_findView(headerContainer, R.id.header_times_long_click);

            Action0 itemClickHandler = () -> {
            };
            Action3<Float, ChartInfo, List<ChartInfo>> itemLongClickHandler = (x, info, visibleData) -> {
                mLineView.drawLine(x, info, visibleData, TYPE_TIMES_TLINE, TYPE_CHART_BIG);
                v_setVisible(mLineView);
                updateLongPressSection(timesLineHeader, info);
                v_setVisible(timesLineHeader);
                v_setGone(normalHeader);
            };
            Action0 itemLongUpHandler = () -> {
                v_setGone(mLineView);
                v_setGone(timesLineHeader);
                v_setVisible(normalHeader);
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

        private void updateLongPressSection(View timesLineHeader, ChartInfo info) {
            ChartLongPressInfo pressInfo = new ChartLongPressInfo(info);

            v_setText(timesLineHeader, R.id.label_last_price, pressInfo.lastPrice);
            v_setText(timesLineHeader, R.id.label_change_ratio, pressInfo.timesChangeRatio);
            v_setText(timesLineHeader, R.id.label_volume, pressInfo.timesVolume);
            v_setText(timesLineHeader, R.id.label_average_price, pressInfo.averagePrice);
            v_setText(timesLineHeader, R.id.label_date, pressInfo.timesDate);
        }

        private void setupTextSection() {
            View leftSection = v_findView(mContentSection, R.id.section_chart_left);
            View rightSection = v_findView(mContentSection, R.id.section_chart_right);
            v_setText(leftSection, R.id.label_left_text1, formatBigNumber(mRender.maxPrice, false, 2, 2, false));
            v_setText(leftSection, R.id.label_left_text2, formatBigNumber(mRender.preClose, false, 2, 2, false));
            v_setText(leftSection, R.id.label_left_text3, formatBigNumber(mRender.minPrice, false, 2, 2, false));
            v_setText(leftSection, R.id.label_left_text4, formatBigNumber(mRender.maxVolume, false, 2, 2, false));
            v_setText(leftSection, R.id.label_left_text5, "万");

            v_setText(rightSection, R.id.label_right_text1, formatRatio((mRender.maxPrice - mRender.preClose) / mRender.preClose, false, 2));
            v_setText(rightSection, R.id.label_right_text2, formatRatio((mRender.preClose - mRender.preClose) / mRender.preClose, false, 2));
            v_setText(rightSection, R.id.label_right_text3, formatRatio((mRender.minPrice - mRender.preClose) / mRender.preClose, false, 2));
            v_setGone(rightSection, R.id.label_right_text4);
            v_setGone(rightSection, R.id.label_right_text5);
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
                float upperHeight = (height - dp2px(24)) / 4 * 3;

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
                        maxTimesLine = preClose+mTimesHalf;
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
    }

    public static class FiveDayDetailChartFragment extends SimpleFragment {

        private Stock mStock;
        private EnvDataPager mEnvDataPager;
        private ChartViewContainer<ChartInfo> mChartContainer;
        private FiveDayDetailChartRender mRender;
        private DrawLineView mLineView;
        private ChartContainerOnTouchListener mListener;

        public FiveDayDetailChartFragment init(Stock stock) {
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
            return inflater.inflate(R.layout.frag_five_day_detail_chart, container, false);
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
            mChartContainer = v_findView(mContentSection, R.id.chart_container);
            mLineView = v_findView(mContentSection, R.id.line_view);
        }

        private void fetchData(boolean reload) {
            Observable<MResultsInfo<StockTline>> observable = fetchStockTLineData(mStock, GMFStockAnswer_Authority_No, Tline_Type_5Day)
                    .map(it -> {
                        if (it.data == null) {
                            it.isSuccess = false;
                        }
                        return it;
                    });
            consumeEventMRUpdateUI(observable, reload)
                    .onNextSuccess(response -> {
                        updateChartSection(response.data);
                    })
                    .done();
        }

        private void updateChartSection(StockTline data) {
            List<ChartInfo> chartList = formatChartData(data.data);

            ChartViewContainer.DEBUG = true;
            if (mEnvDataPager == null) {
                mEnvDataPager = new EnvDataPager();
            }
            mEnvDataPager.setChartData(chartList, StockDetailFragment.TYPE_TIMES_TLINE, Spec_Type_NONE, false);
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
                mRender = new FiveDayDetailChartRender();
            }
            mRender.setEnvData(mEnvDataPager);
            mChartContainer.setRenderDelegate(mRender);
            mChartContainer.notifyDataSetChanged();

            View headerContainer = v_findView(getActivity(), R.id.header_container);
            View normalHeader = v_findView(headerContainer, R.id.header_normal);
            View timesLineHeader = v_findView(headerContainer, R.id.header_times_long_click);

            Action0 itemClickHandler = () -> {
            };
            Action3<Float, ChartInfo, List<ChartInfo>> itemLongClickHandler = (x, info, visibleData) -> {
                mLineView.drawLine(x, info, visibleData, TYPE_FIVE_DAY_TLINE, TYPE_CHART_BIG);
                v_setVisible(mLineView);
                updateLongPressSection(timesLineHeader, info);
                v_setVisible(timesLineHeader);
                v_setGone(normalHeader);
            };
            Action0 itemLongUpHandler = () -> {
                v_setGone(mLineView);
                v_setGone(timesLineHeader);
                v_setVisible(normalHeader);
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

        private void updateLongPressSection(View timesLineHeader, ChartInfo info) {
            ChartLongPressInfo pressInfo = new ChartLongPressInfo(info);

            v_setText(timesLineHeader, R.id.label_last_price, pressInfo.lastPrice);
            v_setText(timesLineHeader, R.id.label_change_ratio, pressInfo.timesChangeRatio);
            v_setText(timesLineHeader, R.id.label_volume, pressInfo.timesVolume);
            v_setText(timesLineHeader, R.id.label_average_price, pressInfo.averagePrice);
            v_setText(timesLineHeader, R.id.label_date, pressInfo.timesDate);
        }

        private void setupTextSection() {
            View leftSection = v_findView(mContentSection, R.id.section_chart_left);
            View rightSection = v_findView(mContentSection, R.id.section_chart_right);
            v_setText(leftSection, R.id.label_left_text1, formatBigNumber(mRender.maxValue, false, 2, 2, false));
            v_setText(leftSection, R.id.label_left_text2, formatBigNumber(mRender.preClose, false, 2, 2, false));
            v_setText(leftSection, R.id.label_left_text3, formatBigNumber(mRender.minValue, false, 2, 2, false));
            v_setText(leftSection, R.id.label_left_text4, formatBigNumber(mRender.maxVolume, false, 2, 2, false));
            v_setText(leftSection, R.id.label_left_text5, "万");

            v_setText(rightSection, R.id.label_right_text1, formatRatio((mRender.maxValue - mRender.preClose) / mRender.preClose, false, 2));
            v_setText(rightSection, R.id.label_right_text2, formatRatio((mRender.preClose - mRender.preClose) / mRender.preClose, false, 2));
            v_setText(rightSection, R.id.label_right_text3, formatRatio((mRender.minValue - mRender.preClose) / mRender.preClose, false, 2));
            v_setGone(rightSection, R.id.label_right_text4);
            v_setGone(rightSection, R.id.label_right_text5);
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
    }

    public static class TimesChartInfo {

        public double last;
        public double average;
        public double prevClose;
        public double volume;
        public double turnover;
        public long time;
        public boolean isDrawLine;

        public TimesChartInfo(TLineData data, boolean isShow) {
            last = data.last;
            average = data.avg;
            prevClose = data.prevClose;
            volume = data.volume;
            turnover = data.turnover;
            time = data.traderTime;
            isDrawLine = isShow;
        }
    }

    public static class DayKLineDetailChartFragment extends SimpleFragment {

        private Stock mStock;
        private int mAuthorityType;
        private int mSpecType;
        private int mChangeType;
        private StockKline mPageArray;
        private EnvDataPager mEnvDataPager;
        private InfinityChartView<ChartInfo> mChartView;
        private ChartViewContainer<ChartInfo> mChartContainer;
        private DayKLineDetailChartRender mRender;
        private View mChartSection;
        private DrawLineView mLineView;
        private boolean mIsLoadingMore = false;
        private ChartContainerOnTouchListener mListener;
        private DrawFetchMoreView mFetchMoreView;

        public DayKLineDetailChartFragment init(Stock stock, int authorityType, int specType, int changeType) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(Stock.class.getName(), stock);
            arguments.putInt("authority_type", authorityType);
            arguments.putInt("spec_type", specType);
            arguments.putInt("change_type", changeType);
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
            mChangeType = getArguments().getInt("change_type");
            return inflater.inflate(R.layout.frag_kline_detail_chart, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            initView();
            v_setClick(mReloadSection, v -> {
                v_setGone(v);
                v_setGone(mContentSection);
                v_setVisible(mLoadingSection);
                fetchData(false, mAuthorityType, mSpecType, mChangeType);
            });
            fetchData(true, mAuthorityType, mSpecType, mChangeType);
        }

        private void initView() {
            mChartSection = v_findView(mContentSection, R.id.section_chart);
            mChartView = v_findView(mChartSection, R.id.chart_view);
            mLineView = v_findView(mChartSection, R.id.line_view);
            mChartContainer = v_findView(mChartView, R.id.chart_container);
            mFetchMoreView = v_findView(mChartView, R.id.chart_left_loading_view);
            mChartContainer.setLayoutDelegate(new EndToStartLayoutDelegate<ChartController.ChartInfo>(getActivity()));
        }

        private void fetchData(boolean reload, int authorityType, int specType, int changeType) {

            boolean showLoading = reload;
            if (reload && mPageArray != null && mPageArray.authority == authorityType) {
                showLoading = !mPageArray.specDataValid(specType);
            }

            consumeEventMRUpdateUI(fetchStockKLineData(showLoading, mStock, authorityType, KLineData.Kline_Type_Day, specType), showLoading)
                    .setTag("day_kline")
                    .onNextSuccess(response -> {
                        mPageArray = response.data;
                        updateContentSection(response.data, specType, changeType);
                    })
                    .done();
        }

        private void updateContentSection(StockKline data, int specType, int changeType) {
            List<ChartInfo> chartList = formatChartData(data.data());

            ChartViewContainer.DEBUG = true;
            if (mEnvDataPager == null) {
                mEnvDataPager = new EnvDataPager();
            }
            mEnvDataPager.setChartData(chartList, StockDetailFragment.TYPE_KLINE, specType, false);
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
                mRender = new DayKLineDetailChartRender();
            }
            mRender.setEnvData(mEnvDataPager);
            mChartContainer.setRenderDelegate(mRender);
            if (changeType == TYPE_CHANGE_AUTHORITY) {
                Rect visibleRect = mChartContainer.scrollRange();
                mChartContainer.scrollTo(visibleRect.right, visibleRect.top);
            }

            mChartContainer.notifyDataSetChanged();
            mChartView.setOnRefreshListener(direction -> fetchMoreData(direction));

            View headerContainer = v_findView(getActivity(), R.id.header_container);
            View normalHeader = v_findView(headerContainer, R.id.header_normal);
            View dayHeader = v_findView(headerContainer, R.id.header_kline_long_click);
            View detailLabelSection = v_findView(mContentSection, R.id.section_label_detail_normal);
            View pressDetailLabelSection = v_findView(mContentSection, R.id.section_label_detail_press);

            Action0 itemClickHandler = () -> {
            };
            Action3<Float, ChartInfo, List<ChartInfo>> itemLongClickHandler = (x, info, visibleData) -> {
                mLineView.drawLine(x, info, visibleData, TYPE_KLINE, TYPE_CHART_BIG);
                updateLongPressSection(dayHeader, pressDetailLabelSection, x, info);
                v_setVisible(dayHeader);
                v_setGone(normalHeader);
                v_setVisible(pressDetailLabelSection);
                v_setGone(detailLabelSection);
                v_setVisible(mLineView);
            };
            Action0 itemLongUpHandler = () -> {
                v_setGone(mLineView);
                v_setGone(dayHeader);
                v_setVisible(normalHeader);
                v_setGone(pressDetailLabelSection);
                v_setVisible(detailLabelSection);
            };
            if (mListener == null) {
                mListener = new ChartContainerOnTouchListener(mChartContainer, itemClickHandler, itemLongClickHandler, itemLongUpHandler, false, TYPE_KLINE);
            }
            mChartContainer.setOnTouchListener(mListener);

            KLineChartRateInfo info = new KLineChartRateInfo();
            consumeEvent(sRefreshChartValueSubject)
                    .setTag("refresh_value")
                    .onNextFinish(fresh -> {
                        info.setData(mRender);
                        refreshTextSection(detailLabelSection, info);
                    })
                    .done();
        }

        private void updateLongPressSection(View dayHeader, View pressDetailLabelSection, float x, ChartInfo info) {
            ChartLongPressInfo pressInfo = new ChartLongPressInfo(info);

            v_setText(dayHeader, R.id.label_open_price, pressInfo.openPrice);
            v_setText(dayHeader, R.id.label_max_price, pressInfo.maxPrice);
            v_setText(dayHeader, R.id.label_min_price, pressInfo.minPrice);
            v_setText(dayHeader, R.id.label_close_price, pressInfo.closePrice);
            v_setText(dayHeader, R.id.label_change_ratio, pressInfo.klineChangeRatio);
            v_setText(dayHeader, R.id.label_date, pressInfo.klineDate);

            View bottomLabel = v_findView(pressDetailLabelSection, R.id.label_bottom_chart);
            FrameLayout.LayoutParams detailParams = (FrameLayout.LayoutParams) pressDetailLabelSection.getLayoutParams();
            FrameLayout.LayoutParams bottomParams = (FrameLayout.LayoutParams) bottomLabel.getLayoutParams();
            if (x <= mChartContainer.getMeasuredWidth() / 2) {
                detailParams.gravity = Gravity.RIGHT;
                bottomParams.gravity = Gravity.RIGHT;
            } else {
                detailParams.gravity = Gravity.LEFT;
                bottomParams.gravity = Gravity.LEFT;
            }
            pressDetailLabelSection.setLayoutParams(detailParams);
            bottomLabel.setLayoutParams(bottomParams);

            v_setText(pressDetailLabelSection, R.id.label_upper_chart, pressInfo.maField);
            if (mRender.mEnvDataPager.mSpecType == Spec_Type_NONE) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.volumeField);
            } else if (mRender.mEnvDataPager.mSpecType == Spec_Type_MACD) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.macdField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_KDJ) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.kdjField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_RSI) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.rsiField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_BOLL) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.bollField);
            }
        }

        private void refreshTextSection(View detailLabelSection, KLineChartRateInfo info) {

            View leftTopSection = v_findView(mContentSection, R.id.section_left_top);
            View leftBottomSection = v_findView(mContentSection, R.id.section_left_bottom);
            View specSection = v_findView(leftBottomSection, R.id.section_spec);
            View macdSection = v_findView(leftBottomSection, R.id.section_macd);
            View kdjSection = v_findView(leftBottomSection, R.id.section_kdj);
            View[] specSections = {specSection, macdSection, kdjSection};
            Action1<View> showBottomFunc = arg -> {
                for (View view : specSections) {
                    view.setVisibility(arg == view ? View.VISIBLE : View.GONE);
                }
            };

            v_setText(detailLabelSection, R.id.label_upper_chart, info.maTitle);

            v_setText(leftTopSection, R.id.label_max_price, info.maxValue);
            v_setText(leftTopSection, R.id.label_mid_price, info.midValue);
            v_setText(leftTopSection, R.id.label_min_price, info.minValue);
            if (info.specType == Spec_Type_NONE) {
                showBottomFunc.call(specSection);
                v_setText(specSection, R.id.label_top, info.maxVolume);
                v_setText(specSection, R.id.label_bottom, info.volumeUnit);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.maxVolume + "" + info.volumeUnit);
            } else if (info.specType == Spec_Type_MACD) {
                showBottomFunc.call(macdSection);
                v_setText(macdSection, R.id.label_top, info.maxMACD);
                v_setText(macdSection, R.id.label_mid, info.macdZero);
                v_setText(macdSection, R.id.label_bottom, info.minMACD);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.macdNormalTilte);
            } else if (info.specType == KLineData.Spec_Type_KDJ) {
                showBottomFunc.call(kdjSection);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.kdjNormalTilte);
            } else if (info.specType == KLineData.Spec_Type_RSI) {
                showBottomFunc.call(specSection);
                v_setText(specSection, R.id.label_top, info.maxRSI);
                v_setText(specSection, R.id.label_bottom, info.minRSI);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.rsiNormalTilte);
            } else if (info.specType == KLineData.Spec_Type_BOLL) {
                showBottomFunc.call(specSection);
                v_setText(specSection, R.id.label_top, info.maxBoll);
                v_setText(specSection, R.id.label_bottom, info.minBoll);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.bollNormalTilte);
            }
        }

        private void fetchMoreData(int direction) {
            if (mIsLoadingMore)
                return;
            if (!hasMoreData(mPageArray)) {
                mChartView.post(() -> {
                    mFetchMoreView.setText("无更多数据");
                    mChartView.setRefreshing(false, direction);
                    mIsLoadingMore = false;
                });
                return;
            }
            mFetchMoreView.setText("加载中...");
            mIsLoadingMore = true;
            PageArrayHelper.getPreviousPage(mPageArray)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        List<ChartInfo> chartInfo = formatChartData(result.data.data());
                        mEnvDataPager.mChartData.clear();
                        mEnvDataPager.mChartData.addAll(chartInfo);
                        mChartContainer.notifyDataSetChanged();
                        mChartView.setRefreshing(false, direction);
                        mIsLoadingMore = false;
                    });
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
    }

    public static class WeekKLineDetailChartFragment extends SimpleFragment {
        private StockKline mPageArray;
        private Stock mStock;
        private EnvDataPager mEnvDataPager;
        private InfinityChartView<ChartController.ChartInfo> mChartView;
        private ChartViewContainer<ChartController.ChartInfo> mChartContainer;
        private DrawFetchMoreView mFetchMoreView;
        private int mAuthorityType;
        private int mSpecType;
        private int mChangeType;
        private WeekKLineDetailChartRender mRender;
        private View mChartSection;
        private DrawLineView mLineView;
        private boolean mIsLoadingMore = false;
        private ChartContainerOnTouchListener mListener;

        public WeekKLineDetailChartFragment init(Stock stock, int authorityType, int specType, int changeType) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(Stock.class.getName(), stock);
            arguments.putInt("authority_type", authorityType);
            arguments.putInt("spec_type", specType);
            arguments.putInt("change_type", changeType);
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
            mChangeType = getArguments().getInt("change_type");
            return inflater.inflate(R.layout.frag_kline_detail_chart, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            initView();
            v_setClick(mReloadSection, v -> {
                v_setGone(v);
                v_setGone(mContentSection);
                v_setVisible(mLoadingSection);
                fetchData(false, mAuthorityType, mSpecType, mChangeType);
            });
            fetchData(true, mAuthorityType, mSpecType, mChangeType);
        }

        private void initView() {
            mChartSection = v_findView(mContentSection, R.id.section_chart);
            mChartView = v_findView(mChartSection, R.id.chart_view);
            mLineView = v_findView(mChartSection, R.id.line_view);
            mChartContainer = v_findView(mChartView, R.id.chart_container);
            mFetchMoreView = v_findView(mChartView, R.id.chart_left_loading_view);
            mFetchMoreView.setText("加载中...");
            mChartContainer.setLayoutDelegate(new EndToStartLayoutDelegate<ChartController.ChartInfo>(getActivity()));
        }

        private void fetchData(boolean reload, int authorityType, int specType, int changeType) {

            boolean showLoading = reload;
            if (reload && mPageArray != null && mPageArray.authority == authorityType) {
                showLoading = !mPageArray.specDataValid(specType);
            }

            if (showLoading) {
                v_setGone(mContentSection);
                v_setGone(mReloadSection);
                v_setGone(mContentSection);
                v_setVisible(mLoadingSection);
            }

            consumeEventMRUpdateUI(fetchStockKLineData(showLoading, mStock, authorityType, KLineData.Kline_Type_Week, specType), showLoading)
                    .setTag("week_kline")
                    .onNextSuccess(response -> {
                        mPageArray = response.data;
                        updateContentSection(response.data, specType, changeType);
                    })
                    .done();
        }

        private void updateContentSection(StockKline data, int specType, int changeType) {
            List<ChartInfo> chartList = formatChartData(data.data());

            ChartViewContainer.DEBUG = true;
            if (mEnvDataPager == null) {
                mEnvDataPager = new EnvDataPager();
            }
            mEnvDataPager.setChartData(chartList, StockDetailFragment.TYPE_KLINE, specType, false);
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
                mRender = new WeekKLineDetailChartRender();
            }
            mRender.setEnvData(mEnvDataPager);
            mChartContainer.setRenderDelegate(mRender);
            if (changeType == TYPE_CHANGE_AUTHORITY) {
                Rect visibleRect = mChartContainer.scrollRange();
                mChartContainer.scrollTo(visibleRect.right, visibleRect.top);
            }
            mChartContainer.notifyDataSetChanged();
            mChartView.setOnRefreshListener(direction -> fetchMoreData(direction));

            View headerContainer = v_findView(getActivity(), R.id.header_container);
            View normalHeader = v_findView(headerContainer, R.id.header_normal);
            View dayHeader = v_findView(headerContainer, R.id.header_kline_long_click);
            View detailLabelSection = v_findView(mContentSection, R.id.section_label_detail_normal);
            View pressDetailLabelSection = v_findView(mContentSection, R.id.section_label_detail_press);

            Action0 itemClickHandler = () -> {
            };
            Action3<Float, ChartInfo, List<ChartInfo>> itemLongClickHandler = (x, info, visibleData) -> {
                mLineView.drawLine(x, info, visibleData, TYPE_KLINE, TYPE_CHART_BIG);
                updateLongPressSection(dayHeader, pressDetailLabelSection, x, info);
                v_setVisible(dayHeader);
                v_setGone(normalHeader);
                v_setVisible(pressDetailLabelSection);
                v_setGone(detailLabelSection);
                v_setVisible(mLineView);
            };
            Action0 itemLongUpHandler = () -> {
                v_setGone(mLineView);
                v_setGone(dayHeader);
                v_setVisible(normalHeader);
                v_setGone(pressDetailLabelSection);
                v_setVisible(detailLabelSection);
            };
            if (mListener == null) {
                mListener = new ChartContainerOnTouchListener(mChartContainer, itemClickHandler, itemLongClickHandler, itemLongUpHandler, false, TYPE_KLINE);
            }
            mChartContainer.setOnTouchListener(mListener);

            KLineChartRateInfo info = new KLineChartRateInfo();
            consumeEvent(sRefreshChartValueSubject)
                    .setTag("refresh_value")
                    .onNextFinish(fresh -> {
                        info.setData(mRender);
                        refreshTextSection(detailLabelSection, info);
                    })
                    .done();
        }

        private void updateLongPressSection(View dayHeader, View pressDetailLabelSection, float x, ChartInfo info) {
            ChartLongPressInfo pressInfo = new ChartLongPressInfo(info);

            v_setText(dayHeader, R.id.label_open_price, pressInfo.openPrice);
            v_setText(dayHeader, R.id.label_max_price, pressInfo.maxPrice);
            v_setText(dayHeader, R.id.label_min_price, pressInfo.minPrice);
            v_setText(dayHeader, R.id.label_close_price, pressInfo.closePrice);
            v_setText(dayHeader, R.id.label_change_ratio, pressInfo.klineChangeRatio);
            v_setText(dayHeader, R.id.label_date, pressInfo.klineDate);

            View bottomLabel = v_findView(pressDetailLabelSection, R.id.label_bottom_chart);
            FrameLayout.LayoutParams detailParams = (FrameLayout.LayoutParams) pressDetailLabelSection.getLayoutParams();
            FrameLayout.LayoutParams bottomParams = (FrameLayout.LayoutParams) bottomLabel.getLayoutParams();
            if (x <= mChartContainer.getMeasuredWidth() / 2) {
                detailParams.gravity = Gravity.RIGHT;
                bottomParams.gravity = Gravity.RIGHT;
            } else {
                detailParams.gravity = Gravity.LEFT;
                bottomParams.gravity = Gravity.LEFT;
            }
            pressDetailLabelSection.setLayoutParams(detailParams);
            bottomLabel.setLayoutParams(bottomParams);

            v_setText(pressDetailLabelSection, R.id.label_upper_chart, pressInfo.maField);
            if (mRender.mEnvDataPager.mSpecType == Spec_Type_NONE) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.volumeField);
            } else if (mRender.mEnvDataPager.mSpecType == Spec_Type_MACD) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.macdField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_KDJ) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.kdjField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_RSI) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.rsiField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_BOLL) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.bollField);
            }
        }


        private void refreshTextSection(View detailLabelSection, KLineChartRateInfo info) {

            View leftTopSection = v_findView(mContentSection, R.id.section_left_top);
            View leftBottomSection = v_findView(mContentSection, R.id.section_left_bottom);
            View specSection = v_findView(leftBottomSection, R.id.section_spec);
            View macdSection = v_findView(leftBottomSection, R.id.section_macd);
            View kdjSection = v_findView(leftBottomSection, R.id.section_kdj);
            View[] specSections = {specSection, macdSection, kdjSection};
            Action1<View> showBottomFunc = arg -> {
                for (View view : specSections) {
                    view.setVisibility(arg == view ? View.VISIBLE : View.GONE);
                }
            };

            v_setText(detailLabelSection, R.id.label_upper_chart, info.maTitle);

            v_setText(leftTopSection, R.id.label_max_price, info.maxValue);
            v_setText(leftTopSection, R.id.label_mid_price, info.midValue);
            v_setText(leftTopSection, R.id.label_min_price, info.minValue);
            if (info.specType == Spec_Type_NONE) {
                showBottomFunc.call(specSection);
                v_setText(specSection, R.id.label_top, info.maxVolume);
                v_setText(specSection, R.id.label_bottom, info.volumeUnit);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.maxVolume + "" + info.volumeUnit);
            } else if (info.specType == Spec_Type_MACD) {
                showBottomFunc.call(macdSection);
                v_setText(macdSection, R.id.label_top, info.maxMACD);
                v_setText(macdSection, R.id.label_mid, info.macdZero);
                v_setText(macdSection, R.id.label_bottom, info.minMACD);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.macdNormalTilte);
            } else if (info.specType == KLineData.Spec_Type_KDJ) {
                showBottomFunc.call(kdjSection);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.kdjNormalTilte);
            } else if (info.specType == KLineData.Spec_Type_RSI) {
                showBottomFunc.call(specSection);
                v_setText(specSection, R.id.label_top, info.maxRSI);
                v_setText(specSection, R.id.label_bottom, info.minRSI);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.rsiNormalTilte);
            } else if (info.specType == KLineData.Spec_Type_BOLL) {
                showBottomFunc.call(specSection);
                v_setText(specSection, R.id.label_top, info.maxBoll);
                v_setText(specSection, R.id.label_bottom, info.minBoll);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.bollNormalTilte);
            }
        }

        private void fetchMoreData(int direction) {
            if (mIsLoadingMore)
                return;
            if (!hasMoreData(mPageArray)) {
                mChartView.post(() -> {
                    mFetchMoreView.setText("无更多数据");
                    mChartView.setRefreshing(false, direction);
                    mIsLoadingMore = false;
                });
                return;
            }
            mFetchMoreView.setText("加载中...");
            mIsLoadingMore = true;
            PageArrayHelper.getPreviousPage(mPageArray)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        List<ChartInfo> chartInfo = formatChartData(result.data.data());
                        mEnvDataPager.mChartData.clear();
                        mEnvDataPager.mChartData.addAll(chartInfo);
                        mChartContainer.notifyDataSetChanged();
                        mChartView.setRefreshing(false, direction);
                        mIsLoadingMore = false;
                    });
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
    }

    public static class MonthKLineDetailChartFragment extends SimpleFragment {
        private StockKline mPageArray;
        private Stock mStock;
        private EnvDataPager mEnvDataPager;
        private InfinityChartView<ChartInfo> mChartView;
        private ChartViewContainer<ChartController.ChartInfo> mChartContainer;
        private int mAuthorityType;
        private int mSpecType;
        private int mChangeType;
        private MonthKLineDetailChartRender mRender;
        private View mChartSection;
        private DrawLineView mLineView;
        private boolean mIsLoadingMore = false;
        private ChartContainerOnTouchListener mListener;
        private DrawFetchMoreView mFetchMoreView;

        public MonthKLineDetailChartFragment init(Stock stock, int authorityType, int specType, int changeType) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(Stock.class.getName(), stock);
            arguments.putInt("authority_type", authorityType);
            arguments.putInt("spec_type", specType);
            arguments.putInt("change_type", changeType);
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
            mChangeType = getArguments().getInt("spec_type");
            return inflater.inflate(R.layout.frag_kline_detail_chart, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            initView();
            v_setClick(mReloadSection, v -> {
                v_setGone(v);
                v_setGone(mContentSection);
                v_setVisible(mLoadingSection);
                fetchData(false, mAuthorityType, mSpecType, mChangeType);
            });
            fetchData(true, mAuthorityType, mSpecType, mChangeType);
        }

        private void initView() {
            mChartSection = v_findView(mContentSection, R.id.section_chart);
            mChartView = v_findView(mChartSection, R.id.chart_view);
            mLineView = v_findView(mChartSection, R.id.line_view);
            mChartContainer = v_findView(mChartView, R.id.chart_container);
            mFetchMoreView = v_findView(mChartView, R.id.chart_left_loading_view);
            mFetchMoreView.setText("加载中...");
            mChartContainer.setLayoutDelegate(new EndToStartLayoutDelegate<ChartController.ChartInfo>(getActivity()));
        }

        private void fetchData(boolean reload, int authorityType, int specType, int changeType) {
            boolean showLoading = reload;
            if (reload && mPageArray != null && mPageArray.authority == authorityType) {
                showLoading = !mPageArray.specDataValid(specType);
            }

            if (showLoading) {
                v_setGone(mContentSection);
                v_setGone(mReloadSection);
                v_setVisible(mLoadingSection);
            }

            consumeEventMRUpdateUI(fetchStockKLineData(showLoading, mStock, authorityType, KLineData.Kline_Type_Month, specType), showLoading)
                    .setTag("month_kline")
                    .onNextSuccess(response -> {
                        mPageArray = response.data;
                        updateContentSection(response.data, specType, changeType);
                    })
                    .done();
        }

        private void updateContentSection(StockKline data, int specType, int changeType) {
            List<ChartInfo> chartList = formatChartData(data.data());

            ChartViewContainer.DEBUG = true;
            if (mEnvDataPager == null) {
                mEnvDataPager = new EnvDataPager();
            }
            mEnvDataPager.setChartData(chartList, StockDetailFragment.TYPE_KLINE, specType, false);
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
                mRender = new MonthKLineDetailChartRender();
            }
            mRender.setEnvData(mEnvDataPager);
            mChartContainer.setRenderDelegate(mRender);
            mChartContainer.notifyDataSetChanged();
            if (changeType == TYPE_CHANGE_AUTHORITY) {
                Rect visibleRect = mChartContainer.scrollRange();
                mChartContainer.scrollTo(visibleRect.right, visibleRect.top);
            }
            mChartView.setOnRefreshListener(direction -> fetchMoreData(direction));

            View headerContainer = v_findView(getActivity(), R.id.header_container);
            View normalHeader = v_findView(headerContainer, R.id.header_normal);
            View dayHeader = v_findView(headerContainer, R.id.header_kline_long_click);
            View detailLabelSection = v_findView(mContentSection, R.id.section_label_detail_normal);
            View pressDetailLabelSection = v_findView(mContentSection, R.id.section_label_detail_press);

            Action0 itemClickHandler = () -> {
            };
            Action3<Float, ChartInfo, List<ChartInfo>> itemLongClickHandler = (x, info, visibleData) -> {
                mLineView.drawLine(x, info, visibleData, TYPE_KLINE, TYPE_CHART_BIG);
                updateLongPressSection(dayHeader, pressDetailLabelSection, x, info);
                v_setVisible(dayHeader);
                v_setGone(normalHeader);
                v_setVisible(pressDetailLabelSection);
                v_setGone(detailLabelSection);
                v_setVisible(mLineView);
            };
            Action0 itemLongUpHandler = () -> {
                v_setGone(mLineView);
                v_setGone(dayHeader);
                v_setVisible(normalHeader);
                v_setGone(pressDetailLabelSection);
                v_setVisible(detailLabelSection);
            };
            if (mListener == null) {
                mListener = new ChartContainerOnTouchListener(mChartContainer, itemClickHandler, itemLongClickHandler, itemLongUpHandler, false, TYPE_KLINE);
            }
            mChartContainer.setOnTouchListener(mListener);

            KLineChartRateInfo info = new KLineChartRateInfo();
            consumeEvent(sRefreshChartValueSubject)
                    .setTag("refresh_value")
                    .onNextFinish(fresh -> {
                        info.setData(mRender);
                        refreshTextSection(detailLabelSection, info);
                    })
                    .done();
        }

        private void updateLongPressSection(View dayHeader, View pressDetailLabelSection, float x, ChartInfo info) {
            ChartLongPressInfo pressInfo = new ChartLongPressInfo(info);

            v_setText(dayHeader, R.id.label_open_price, pressInfo.openPrice);
            v_setText(dayHeader, R.id.label_max_price, pressInfo.maxPrice);
            v_setText(dayHeader, R.id.label_min_price, pressInfo.minPrice);
            v_setText(dayHeader, R.id.label_close_price, pressInfo.closePrice);
            v_setText(dayHeader, R.id.label_change_ratio, pressInfo.klineChangeRatio);
            v_setText(dayHeader, R.id.label_date, pressInfo.klineDate);

            View bottomLabel = v_findView(pressDetailLabelSection, R.id.label_bottom_chart);
            FrameLayout.LayoutParams detailParams = (FrameLayout.LayoutParams) pressDetailLabelSection.getLayoutParams();
            FrameLayout.LayoutParams bottomParams = (FrameLayout.LayoutParams) bottomLabel.getLayoutParams();
            if (x <= mChartContainer.getMeasuredWidth() / 2) {
                detailParams.gravity = Gravity.RIGHT;
                bottomParams.gravity = Gravity.RIGHT;
            } else {
                detailParams.gravity = Gravity.LEFT;
                bottomParams.gravity = Gravity.LEFT;
            }
            pressDetailLabelSection.setLayoutParams(detailParams);
            bottomLabel.setLayoutParams(bottomParams);

            v_setText(pressDetailLabelSection, R.id.label_upper_chart, pressInfo.maField);
            if (mRender.mEnvDataPager.mSpecType == Spec_Type_NONE) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.volumeField);
            } else if (mRender.mEnvDataPager.mSpecType == Spec_Type_MACD) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.macdField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_KDJ) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.kdjField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_RSI) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.rsiField);
            } else if (mRender.mEnvDataPager.mSpecType == KLineData.Spec_Type_BOLL) {
                v_setText(pressDetailLabelSection, R.id.label_bottom_chart, pressInfo.bollField);
            }
        }


        private void refreshTextSection(View detailLabelSection, KLineChartRateInfo info) {

            View leftTopSection = v_findView(mContentSection, R.id.section_left_top);
            View leftBottomSection = v_findView(mContentSection, R.id.section_left_bottom);
            View specSection = v_findView(leftBottomSection, R.id.section_spec);
            View macdSection = v_findView(leftBottomSection, R.id.section_macd);
            View kdjSection = v_findView(leftBottomSection, R.id.section_kdj);
            View[] specSections = {specSection, macdSection, kdjSection};
            Action1<View> showBottomFunc = arg -> {
                for (View view : specSections) {
                    view.setVisibility(arg == view ? View.VISIBLE : View.GONE);
                }
            };

            v_setText(detailLabelSection, R.id.label_upper_chart, info.maTitle);

            v_setText(leftTopSection, R.id.label_max_price, info.maxValue);
            v_setText(leftTopSection, R.id.label_mid_price, info.midValue);
            v_setText(leftTopSection, R.id.label_min_price, info.minValue);
            if (info.specType == Spec_Type_NONE) {
                showBottomFunc.call(specSection);
                v_setText(specSection, R.id.label_top, info.maxVolume);
                v_setText(specSection, R.id.label_bottom, info.volumeUnit);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.maxVolume + "" + info.volumeUnit);
            } else if (info.specType == Spec_Type_MACD) {
                showBottomFunc.call(macdSection);
                v_setText(macdSection, R.id.label_top, info.maxMACD);
                v_setText(macdSection, R.id.label_mid, info.macdZero);
                v_setText(macdSection, R.id.label_bottom, info.minMACD);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.macdNormalTilte);
            } else if (info.specType == KLineData.Spec_Type_KDJ) {
                showBottomFunc.call(kdjSection);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.kdjNormalTilte);
            } else if (info.specType == KLineData.Spec_Type_RSI) {
                showBottomFunc.call(specSection);
                v_setText(specSection, R.id.label_top, info.maxRSI);
                v_setText(specSection, R.id.label_bottom, info.minRSI);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.rsiNormalTilte);
            } else if (info.specType == KLineData.Spec_Type_BOLL) {
                showBottomFunc.call(specSection);
                v_setText(specSection, R.id.label_top, info.maxBoll);
                v_setText(specSection, R.id.label_bottom, info.minBoll);
                v_setText(detailLabelSection, R.id.label_bottom_chart, info.bollNormalTilte);
            }
        }

        private void fetchMoreData(int direction) {
            if (mIsLoadingMore)
                return;
            if (!hasMoreData(mPageArray)) {
                mChartView.post(() -> {
                    mFetchMoreView.setText("无更多数据");
                    mChartView.setRefreshing(false, direction);
                    mIsLoadingMore = false;
                });
                return;
            }
            mFetchMoreView.setText("加载中...");
            mIsLoadingMore = true;
            PageArrayHelper.getPreviousPage(mPageArray)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        List<ChartInfo> chartInfo = formatChartData(result.data.data());
                        mEnvDataPager.mChartData.clear();
                        mEnvDataPager.mChartData.addAll(chartInfo);
                        mChartContainer.notifyDataSetChanged();
                        mChartView.setRefreshing(false, direction);
                        mIsLoadingMore = false;
                    });
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
    }

    public static class KLineChartRateInfo {

        public CharSequence maxValue;
        public CharSequence midValue;
        public CharSequence minValue;
        public CharSequence maxVolume;
        public CharSequence volumeUnit;
        public CharSequence maTitle;
        public CharSequence maxMACD;
        public CharSequence macdZero;
        public CharSequence minMACD;
        public CharSequence maxRSI;
        public CharSequence minRSI;
        public CharSequence maxBoll;
        public CharSequence minBoll;
        public CharSequence macdNormalTilte;
        public CharSequence kdjNormalTilte;
        public CharSequence rsiNormalTilte;
        public CharSequence bollNormalTilte;
        public int specType;
        public int chartType;

        public void setData(KLineChartBaseRender data) {

            specType = data.mEnvDataPager.mSpecType;
            chartType = data.mEnvDataPager.mChartType;
            maxValue = formatBigNumber(data.mEnvDataPager.mMaxValue, false, 2, 2, false);
            midValue = formatBigNumber((data.mEnvDataPager.mMaxValue + data.mEnvDataPager.mMinValue) / 2, false, 2, 2, false);
            minValue = formatBigNumber(data.mEnvDataPager.mMinValue, false, 2, 2, false);
            maxVolume = formatBigNumber(data.mEnvDataPager.mMaxVolume, false, 2, 2, false);
            volumeUnit = computeBiggerNumberUnit(data.mEnvDataPager.mMaxVolume);
            maxMACD = formatBigNumber(data.mEnvDataPager.mMaxMACD, false, 2, 2, false);
            macdZero = "0.0";
            minMACD = formatBigNumber(data.mEnvDataPager.mMinMACD, true, 2, 2, false);
            maxRSI = formatBigNumber(data.mEnvDataPager.mMaxRSI, false, 2, 2, false);
            minRSI = formatBigNumber(data.mEnvDataPager.mMinRSI, false, 2, 2, false);
            maxBoll = formatBigNumber(data.mEnvDataPager.mMaxBoll, false, 2, 2, false);
            minBoll = formatBigNumber(data.mEnvDataPager.mMinBoll, false, 2, 2, false);
            if (data.mVisibleData != null && !safeGet(() -> data.mVisibleData.isEmpty(), true)) {
                maTitle = concatNoBreak(
                        setColor("MA5: " + formatBigNumber(data.mVisibleData.get(0).ma5, false, 2, 2, false), LINE_BLUE_COLOR),
                        setColor(" MA10: " + formatBigNumber(data.mVisibleData.get(0).ma10, false, 2, 2, false), LINE_YELLOW_COLOR),
                        setColor(" MA20: " + formatBigNumber(data.mVisibleData.get(0).ma20, false, 2, 2, false), LINE_PURPLE_COLOR));
            } else {
                maTitle = concatNoBreak(
                        setColor("MA5: ", LINE_BLUE_COLOR),
                        setColor(" MA10: ", LINE_YELLOW_COLOR),
                        setColor(" MA20: ", LINE_PURPLE_COLOR));
            }

            macdNormalTilte = "MACD(12,26,9)";
            kdjNormalTilte = "KDJ(9,3,3)";
            rsiNormalTilte = "RSI(6,12,24)";
            bollNormalTilte = "BOLL(20,2)";
        }
    }

    public static class ChartLongPressInfo {

        public CharSequence smallTimesLastPrice;
        public CharSequence smallKLineChangeRatio;

        public CharSequence lastPrice;
        public CharSequence timesChangeRatio;
        public CharSequence timesVolume;
        public CharSequence averagePrice;
        public CharSequence timesDate;

        public CharSequence openPrice;
        public CharSequence maxPrice;
        public CharSequence minPrice;
        public CharSequence closePrice;
        public CharSequence klineChangeRatio;
        public CharSequence klineDate;

        public CharSequence maField;
        public CharSequence macdField;
        public CharSequence volumeField;
        public CharSequence kdjField;
        public CharSequence rsiField;
        public CharSequence bollField;


        public ChartLongPressInfo(ChartInfo info) {

            if (info != null) {
                smallTimesLastPrice = concatNoBreak(
                        setFontSize(setColor(formatMoney(info.last, false, 2), getFiveOrderPriceColor(info.last, info.preClose)), sp2px(48)));

                smallKLineChangeRatio = concatNoBreak(
                        setFontSize(setColor(formatRatio((info.close - info.preClose) / info.preClose, true, 2), getFiveOrderPriceColor(info.close, info.preClose)), sp2px(48)));

                lastPrice = concatNoBreak(
                        setFontSize(setColor("价格 ", TEXT_GREY_COLOR), sp2px(14)), setFontSize(setColor(formatMoney(info.last, false, 2), getFiveOrderPriceColor(info.last, info.preClose)), sp2px(14)));
                timesChangeRatio = concatNoBreak(
                        setFontSize(setColor("涨幅 ", TEXT_GREY_COLOR), sp2px(14)), setFontSize(setColor(formatRatio((info.last - info.preClose) / info.preClose, true, 2), getFiveOrderPriceColor(info.last, info.preClose)), sp2px(14)));
                timesVolume = concatNoBreak(
                        setFontSize(setColor("成交 ", TEXT_GREY_COLOR), sp2px(14)), setFontSize(setColor(formatBigNumber(info.volume, 1, 2), TEXT_BLACK_COLOR), sp2px(14)));
                averagePrice = concatNoBreak(
                        setFontSize(setColor("均价 ", TEXT_GREY_COLOR), sp2px(14)), setFontSize(setColor(formatMoney(info.average, false, 2), getFiveOrderPriceColor(info.average, info.preClose)), sp2px(14)));
                timesDate = setFontSize(setColor(formatSecond(info.time, "MM-dd HH:mm"), TEXT_GREY_COLOR), sp2px(12));

                openPrice = concatNoBreak(
                        setFontSize(setColor("开盘 ", TEXT_GREY_COLOR), sp2px(14)), setFontSize(setColor(formatMoney(info.open, false, 2), getFiveOrderPriceColor(info.open, info.preClose)), sp2px(14)));
                maxPrice = concatNoBreak(
                        setFontSize(setColor("最高 ", TEXT_GREY_COLOR), sp2px(14)), setFontSize(setColor(formatMoney(info.max, false, 2), getFiveOrderPriceColor(info.max, info.preClose)), sp2px(14)));
                minPrice = concatNoBreak(
                        setFontSize(setColor("最低 ", TEXT_GREY_COLOR), sp2px(14)), setFontSize(setColor(formatMoney(info.min, false, 2), getFiveOrderPriceColor(info.min, info.preClose)), sp2px(14)));
                closePrice = concatNoBreak(
                        setFontSize(setColor("收盘 ", TEXT_GREY_COLOR), sp2px(14)), setFontSize(setColor(formatMoney(info.close, false, 2), getFiveOrderPriceColor(info.close, info.preClose)), sp2px(14)));
                klineChangeRatio = concatNoBreak(
                        setFontSize(setColor("涨幅 ", TEXT_GREY_COLOR), sp2px(14)), setFontSize(setColor(formatRatio((info.close - info.preClose) / info.preClose, true, 2), getFiveOrderPriceColor(info.close, info.preClose)), sp2px(14)));
                klineDate = setFontSize(setColor(formatSecond(info.time, "yyyy-MM-dd"), TEXT_GREY_COLOR), sp2px(12));

                maField = concatNoBreak(
                        setColor("MA5: " + formatBigNumber(info.ma5, false, 2, 2, false), LINE_BLUE_COLOR),
                        setColor(" MA10: " + formatBigNumber(info.ma10, false, 2, 2, false), LINE_YELLOW_COLOR),
                        setColor(" MA20: " + formatBigNumber(info.ma20, false, 2, 2, false), LINE_PURPLE_COLOR));
                volumeField = concatNoBreak(
                        setColor(formatBigNumber(info.volume, false, 2, 2, false) + computeBiggerNumberUnit(info.volume), TEXT_GREY_COLOR));
                macdField = concatNoBreak(
                        setColor("DIFF: " + formatBigNumber(info.diff, false, 3, 3, false), LINE_YELLOW_COLOR),
                        setColor(" DEA: " + formatBigNumber(info.dea, false, 3, 3, false), LINE_BLUE_COLOR),
                        setColor(" MACD: " + formatBigNumber(info.macd, false, 3, 3, false), LINE_PURPLE_COLOR));
                kdjField = concatNoBreak(
                        setColor("K: " + formatBigNumber(info.kValue, false, 3, 3, false), LINE_BLUE_COLOR),
                        setColor(" D: " + formatBigNumber(info.dValue, false, 3, 3, false), LINE_PURPLE_COLOR),
                        setColor(" J: " + formatBigNumber(info.jValue, false, 3, 3, false), LINE_YELLOW_COLOR));
                rsiField = concatNoBreak(
                        setColor("RSI6: " + formatBigNumber(info.rsi6, false, 3, 3, false), LINE_BLUE_COLOR),
                        setColor(" RSI12: " + formatBigNumber(info.rsi12, false, 3, 3, false), LINE_PURPLE_COLOR),
                        setColor(" RSI24: " + formatBigNumber(info.rsi24, false, 3, 3, false), LINE_YELLOW_COLOR));

                bollField = concatNoBreak(
                        setColor("UPPER: " + formatBigNumber(info.upper, false, 3, 3, false), LINE_YELLOW_COLOR),
                        setColor(" MID: " + formatBigNumber(info.mid, false, 3, 3, false), LINE_BLUE_COLOR),
                        setColor(" LOWER: " + formatBigNumber(info.lower, false, 3, 3, false), LINE_YELLOW_COLOR));
            }
        }
    }

}