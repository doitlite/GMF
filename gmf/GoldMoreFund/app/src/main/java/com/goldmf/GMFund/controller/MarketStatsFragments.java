package com.goldmf.GMFund.controller;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.business.StockController;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.PageArrayHelper;
import com.goldmf.GMFund.util.FormatUtil;
import com.goldmf.GMFund.util.SecondUtil;
import com.goldmf.GMFund.widget.SimpleOnItemTouchListener;
import com.goldmf.GMFund.widget.StaticTableView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action3;
import rx.functions.Func2;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import yale.extension.common.shape.RoundCornerShape;
import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_FIRST_VISIBLE_CHILD_IDX_INT;
import static com.goldmf.GMFund.controller.FragmentStackActivity.pushFragment;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getIncomeTextColor;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addHorizontalSepLine;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.manager.stock.GMFLiveItem.GMFLiveItemMore;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.hasMoreData;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatRatio;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;

/**
 * Created by yale on 16/2/26.
 */
public class MarketStatsFragments {
    public static class MarketStatsFragment extends SimpleFragment {
        private SwipeRefreshLayout mRefreshLayout;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_market_stats, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            mRefreshLayout = (SwipeRefreshLayout) mContentSection;
            mRefreshLayout.setOnRefreshListener(() -> fetchData(false));

            fetchData(true);
        }

        private void fetchData(boolean reload) {
            if (reload) {
                v_setGone(mContentSection);
                v_setGone(mReloadSection);
                v_setVisible(mLoadingSection);
            } else {
                mRefreshLayout.setRefreshing(true);
            }

            StockController.fetchMarketStatsInfo()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(nil -> {

                        List<StockIndexItem> stockIndexItems = Stream.range(0, 4).map(it -> new StockIndexItem()).collect(Collectors.toList());
                        updateStockIndexTable(stockIndexItems);

                        List<IndustryGridItem> industryItems = Stream.range(0, 6).map(it -> new IndustryGridItem()).collect(Collectors.toList());
                        updateIndustryTable(industryItems);

                        List<StockRankItem> riseStockItems = Stream.range(0, 5).map(it -> new StockRankItem()).collect(Collectors.toList());
                        updateRiseStockTable(riseStockItems);

                        List<StockRankItem> fallStockItems = Stream.range(0, 5).map(it -> new StockRankItem()).collect(Collectors.toList());
                        updateFallStockTable(fallStockItems);

                        v_setVisible(mContentSection);
                        v_setGone(mLoadingSection);
                        mRefreshLayout.setRefreshing(false);
                    })
                    .subscribe();
        }

        private void updateStockIndexTable(List<StockIndexItem> items) {
            StaticTableView table = v_findView(this, R.id.table_stock_index);
            table.resetTable(items, 2)
                    .setOnCreateItemView(R.layout.cell_stock_index)
                    .setOnConfigureItemView((itemView, item) -> {
                        v_setText(itemView, R.id.label_name, item.name);
                        v_setText(itemView, R.id.label_value, item.value);
                        v_setText(itemView, R.id.label_change, item.change);
                    })
                    .done();
        }

        private void updateIndustryTable(List<IndustryGridItem> items) {
            v_setClick(mContentSection, R.id.header_industry, v -> pushFragment(this, new IndustryRankListFragment()));

            StaticTableView table = v_findView(this, R.id.table_industry);
            table.resetTable(items, 3)
                    .setOnCreateItemView(R.layout.cell_industry_grid)
                    .setOnConfigureItemView((itemView, item) -> {
                        v_setText(itemView, R.id.label_industry_name, item.industryName);
                        v_setText(itemView, R.id.label_industry_change, item.industryChange);
                        v_setText(itemView, R.id.label_stock_name, item.presentStockName);
                        v_setText(itemView, R.id.label_stock_change, item.presentStockChange);
                    })
                    .setOnClick((itemView, item) -> {
                        pushFragment(this, new StockRankListFragment().init(item.industryId));
                    })
                    .done();
        }

        private void updateRiseStockTable(List<StockRankItem> items) {
            StaticTableView table = v_findView(this, R.id.table_rise_stock);
            table.resetTable(items, 1)
                    .setOnCreateItemView(R.layout.cell_stock_change)
                    .setOnConfigureItemView((itemView, item) -> {
                        v_setText(itemView, R.id.label_stock_name_and_code, item.nameAndCode);
                        v_setText(itemView, R.id.label_stock_price, item.price);
                        v_setText(itemView, R.id.label_stock_change, item.changeRatio);
                    })
                    .done();
        }

        private void updateFallStockTable(List<StockRankItem> items) {
            StaticTableView table = v_findView(this, R.id.table_fall_stock);
            table.resetTable(items, 1)
                    .setOnCreateItemView(R.layout.cell_stock_change)
                    .setOnConfigureItemView((itemView, item) -> {
                        v_setText(itemView, R.id.label_stock_name_and_code, item.nameAndCode);
                        v_setText(itemView, R.id.label_stock_price, item.price);
                        v_setText(itemView, R.id.label_stock_change, item.changeRatio);
                    })
                    .done();
        }
    }

    private static class StockIndexItem {
        public CharSequence name = "上证指数";
        public CharSequence value = "2895.33";
        public CharSequence change = "-188.73 -6.42%";
    }

    private static class IndustryGridItem {
        public String industryId = "001";
        public CharSequence industryName = "航运";
        public CharSequence industryChange = "+6.32%";
        public CharSequence presentStockName = "中海集运";
        public CharSequence presentStockChange = "+708.98 +6.96%";
    }

    private static class StockRankItem {
        public CharSequence nameAndCode = concat("东方海洋", setFontSize(setColor("000735", TEXT_GREY_COLOR), sp2px(10)));
        public CharSequence price = "6.68";
        public CharSequence changeRatio = "+10.09%";
        public long updateTime;
        public double rawPrice;
        public double rawChangeRatio;

        public static StockRankItem createWithSeed(int seed) {
            String[] names = {"东方海洋", "壹桥海森", "大湖股份", "中水渔业", "前海运输"};
            String[] codes = {"000735", "002527", "002100", "000001", "000735"};
            double[] prices = {6.68, 16.09, 7.60, 9.79, 16.24};
            double[] changeRatios = {0.0214, 0.0181, 0.0109, 0.0102, 0.0094, 0.0091};

            int index = seed % names.length;

            StockRankItem item = new StockRankItem();
            item.nameAndCode = concat(names[index], setFontSize(setColor(codes[index], TEXT_GREY_COLOR), sp2px(10)));
            item.price = formatMoney(prices[index], false, 2);
            item.changeRatio = setColor(formatRatio(changeRatios[index], true, 2), getIncomeTextColor(changeRatios[index]));
            item.updateTime = -seed;
            item.rawPrice = prices[index];
            item.rawChangeRatio = changeRatios[index];
            return item;
        }
    }


    public static class IndustryRankListFragment extends SimpleFragment {
        private SwipeRefreshLayout mRefreshLayout;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_industry_rank_list, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            mRefreshLayout = v_findView(mContentSection, R.id.refreshLayout);
            mRefreshLayout.setOnRefreshListener(() -> fetchData(false));

            RecyclerView recyclerView = v_findView(mContentSection, R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.VERTICAL, false));
            addHorizontalSepLine(recyclerView);

            fetchData(true);
        }

        private void fetchData(boolean reload) {
            if (reload) {
                v_setGone(mContentSection);
                v_setGone(mReloadSection);
                v_setVisible(mLoadingSection);
            } else {
                mRefreshLayout.setRefreshing(true);
            }

            Observable.empty()
                    .delay(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() -> {
                        List<IndustryListItem> items = Stream.range(0, 5).map(IndustryListItem::createWithSeed).collect(Collectors.toList());
                        updateIndustrySection(items);
                        v_setVisible(mContentSection);
                        v_setGone(mLoadingSection);
                        mRefreshLayout.setRefreshing(false);
                    })
                    .subscribe();

        }

        private int mLastSort = SORT_BY_TIME_DESCENT;

        private void updateIndustrySection(List<IndustryListItem> items) {
            View header = v_findView(mContentSection, R.id.header_industry);
            v_setClick(header, R.id.area_change, v -> {
                int sort = mLastSort;
                if (sort == SORT_BY_CHANGE_ASCENT) {
                    mLastSort = SORT_BY_CHANGE_DESCENT;
                } else if (sort == SORT_BY_CHANGE_DESCENT) {
                    mLastSort = SORT_BY_TIME_DESCENT;
                } else {
                    mLastSort = SORT_BY_CHANGE_ASCENT;
                }
                updateIndustryHeader();
                updateIndustryList(items);
            });

            updateIndustryHeader();
            updateIndustryList(items);
        }

        private void updateIndustryHeader() {
            int sort = mLastSort;

            final int ICON_TYPE_DEFAULT = 0;
            final int ICON_TYPE_ASCENT = 1;
            final int ICON_TYPE_DESCENT = 2;
            final Action3<TextView, ImageView, Integer> changeImageResFunc = (textView, imageView, iconType) -> {

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
                params.addRule(RelativeLayout.RIGHT_OF, textView.getId());
                params.leftMargin = dp2px(2);
                if (iconType == ICON_TYPE_DEFAULT) {
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                } else {
                    params.addRule(RelativeLayout.ALIGN_BOTTOM, textView.getId());
                    params.bottomMargin = dp2px(2);
                }
                imageView.setLayoutParams(params);

                if (iconType == ICON_TYPE_DEFAULT)
                    imageView.setImageResource(R.drawable.ic_menu_drop);
                else if (iconType == ICON_TYPE_ASCENT)
                    imageView.setImageResource(R.drawable.ic_sort_ascent);
                else if (iconType == ICON_TYPE_DESCENT)
                    imageView.setImageResource(R.drawable.ic_sort_descent);
            };

            View header = v_findView(mContentSection, R.id.header_industry);
            TextView changeLabel = v_findView(header, R.id.label_change);
            ImageView changeImage = v_findView(header, R.id.img_change);

            if (sort == SORT_BY_TIME_DESCENT)
                changeImageResFunc.call(changeLabel, changeImage, ICON_TYPE_DEFAULT);
            else if (sort == SORT_BY_CHANGE_ASCENT)
                changeImageResFunc.call(changeLabel, changeImage, ICON_TYPE_ASCENT);
            else if (sort == SORT_BY_CHANGE_DESCENT)
                changeImageResFunc.call(changeLabel, changeImage, ICON_TYPE_DESCENT);
        }

        private void updateIndustryList(List<IndustryListItem> items) {
            int sort = mLastSort;

            List<IndustryListItem> sortedItems = Observable.from(items).toSortedList(SORT_FUNC_MAP.get(sort)).toBlocking().first();
            RecyclerView recyclerView = v_findView(mContentSection, R.id.recyclerView);
            SimpleRecyclerViewAdapter<IndustryListItem> adapter = new SimpleRecyclerViewAdapter.Builder<>(sortedItems)
                    .onCreateItemView(R.layout.cell_industry_list)
                    .onCreateViewHolder(builder -> {
                        builder.bindChildWithTag("industry_name", R.id.label_industry_name)
                                .bindChildWithTag("industry_change", R.id.label_industry_change)
                                .bindChildWithTag("present_stock", R.id.label_present_stock)
                                .configureView((item, pos) -> {
                                    v_setText(builder.getChildWithTag("industry_name"), item.industryName);
                                    v_setText(builder.getChildWithTag("industry_change"), item.industryChange);
                                    v_setText(builder.getChildWithTag("present_stock"), item.presentStockName);
                                });
                        return builder.create();
                    })
                    .create();
            recyclerView.setAdapter(adapter);
            recyclerView.addOnItemTouchListener(new SimpleOnItemTouchListener(recyclerView, (child, pos) -> {
                IndustryListItem item = sortedItems.get(pos);
                pushFragment(this, new StockRankListFragment().init(item.industryId));
                return true;
            }));
        }

        private static final int SORT_BY_TIME_DESCENT = 0;
        private static final int SORT_BY_CHANGE_ASCENT = 1;
        private static final int SORT_BY_CHANGE_DESCENT = 2;
        private static final SparseArray<Func2<IndustryListItem, IndustryListItem, Integer>> SORT_FUNC_MAP = new SparseArray<>();

        static {
            SORT_FUNC_MAP.append(SORT_BY_TIME_DESCENT, (left, right) -> {
                if (left.updateTime > right.updateTime) {
                    return -1;
                } else if (left.updateTime < right.updateTime) {
                    return 1;
                } else {
                    return 0;
                }
            });
            SORT_FUNC_MAP.append(SORT_BY_CHANGE_ASCENT, (left, right) -> {
                if (left.rawChange > right.rawChange) {
                    return 1;
                } else if (left.rawChange < right.rawChange) {
                    return -1;
                } else {
                    return 0;
                }
            });
            SORT_FUNC_MAP.append(SORT_BY_CHANGE_DESCENT, (left, right) -> {
                if (left.rawChange > right.rawChange) {
                    return -1;
                } else if (left.rawChange < right.rawChange) {
                    return 1;
                } else {
                    return 0;
                }
            });
        }
    }

    private static class IndustryListItem {
        public String industryId;
        public CharSequence industryName;
        public CharSequence industryChange;
        public CharSequence presentStockName;
        public long updateTime;

        public double rawChange;

        public static IndustryListItem createWithSeed(int seed) {
            String[] industryNames = {"航运", "林业", "光学光电子", "酒饮料", "农产品加工"};
            double[] industryChangeRatios = {0.0294, 0.0281, 0.0223, 0.0211, 0.0184};
            String[] presentStockNames = {"罗牛山", "武当山", "视网膜屏", "茅台XO", "南宁糖业"};

            int index = seed % industryNames.length;

            IndustryListItem item = new IndustryListItem();
            item.industryId = String.valueOf(seed);
            item.industryName = industryNames[index];
            item.industryChange = formatRatio(industryChangeRatios[index], true, 2);
            item.presentStockName = presentStockNames[index];
            item.rawChange = industryChangeRatios[index];
            item.updateTime = 0 - seed;
            return item;
        }
    }

    public static class StockRankListFragment extends SimpleFragment {
        private String mIndustryId;

        private SwipeRefreshLayout mRefreshLayout;

        public StockRankListFragment init(String industryId) {
            Bundle arguments = new Bundle();
            arguments.putString("industry_id", industryId);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_stock_rank_list, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            mRefreshLayout = v_findView(mContentSection, R.id.refreshLayout);
            mRefreshLayout.setOnRefreshListener(() -> fetchData(false));
            RecyclerView recyclerView = v_findView(mContentSection, R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.VERTICAL, false));
            addHorizontalSepLine(recyclerView);
            fetchData(true);
        }

        private void fetchData(boolean reload) {
            if (reload) {
                v_setGone(mContentSection);
                v_setGone(mReloadSection);
                v_setVisible(mLoadingSection);
            } else {
                mRefreshLayout.setRefreshing(true);
            }

            Observable.empty()
                    .delay(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnCompleted(() -> {
                        List<StockRankItem> items = Stream.range(0, 5).map(StockRankItem::createWithSeed).collect(Collectors.toList());
                        updateContentSection(items);
                        v_setVisible(mContentSection);
                        v_setGone(mLoadingSection);
                        mRefreshLayout.setRefreshing(false);
                    })
                    .subscribe();

        }

        private int mLastSort = SORT_BY_TIME_DESCENT;

        private void updateContentSection(List<StockRankItem> items) {
            View header = v_findView(mContentSection, R.id.header_stock_rank_list);
            v_setClick(header, R.id.area_price, v -> {
                int sort = mLastSort;

                if (sort == SORT_BY_PRICE_ASCENT)
                    mLastSort = SORT_BY_PRICE_DESCENT;
                else if (sort == SORT_BY_PRICE_DESCENT)
                    mLastSort = SORT_BY_TIME_DESCENT;
                else
                    mLastSort = SORT_BY_PRICE_ASCENT;

                updateStockListHeader();
                updateStockListView(items);
            });
            v_setClick(header, R.id.area_change, v -> {
                int sort = mLastSort;

                if (sort == SORT_BY_CHANGE_RATIO_ASCENT)
                    mLastSort = SORT_BY_CHANGE_RATIO_DESCENT;
                else if (sort == SORT_BY_CHANGE_RATIO_DESCENT)
                    mLastSort = SORT_BY_TIME_DESCENT;
                else
                    mLastSort = SORT_BY_CHANGE_RATIO_ASCENT;

                updateStockListHeader();
                updateStockListView(items);
            });

            updateStockListHeader();
            updateStockListView(items);
        }

        private void updateStockListHeader() {
            int sort = mLastSort;

            final int ICON_TYPE_DEFAULT = 0;
            final int ICON_TYPE_ASCENT = 1;
            final int ICON_TYPE_DESCENT = 2;
            final Action3<TextView, ImageView, Integer> changeImageResFunc = (textView, imageView, iconType) -> {

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
                params.addRule(RelativeLayout.RIGHT_OF, textView.getId());
                params.leftMargin = dp2px(2);
                if (iconType == ICON_TYPE_DEFAULT) {
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                } else {
                    params.addRule(RelativeLayout.ALIGN_BOTTOM, textView.getId());
                    params.bottomMargin = dp2px(2);
                }
                imageView.setLayoutParams(params);

                if (iconType == ICON_TYPE_DEFAULT)
                    imageView.setImageResource(R.drawable.ic_menu_drop);
                else if (iconType == ICON_TYPE_ASCENT)
                    imageView.setImageResource(R.drawable.ic_sort_ascent);
                else if (iconType == ICON_TYPE_DESCENT)
                    imageView.setImageResource(R.drawable.ic_sort_descent);
            };


            View header = v_findView(mContentSection, R.id.header_stock_rank_list);
            TextView priceLabel = v_findView(header, R.id.label_price);
            ImageView priceImage = v_findView(header, R.id.img_price);
            TextView changeLabel = v_findView(header, R.id.label_change);
            ImageView changeImage = v_findView(header, R.id.img_change);

            int priceIconType = ICON_TYPE_DEFAULT;
            int changeIconType = ICON_TYPE_DEFAULT;

            if (sort == SORT_BY_PRICE_ASCENT)
                priceIconType = ICON_TYPE_ASCENT;
            else if (sort == SORT_BY_PRICE_DESCENT)
                priceIconType = ICON_TYPE_DESCENT;
            else if (sort == SORT_BY_CHANGE_RATIO_ASCENT)
                changeIconType = ICON_TYPE_ASCENT;
            else if (sort == SORT_BY_CHANGE_RATIO_DESCENT)
                changeIconType = ICON_TYPE_DESCENT;

            changeImageResFunc.call(priceLabel, priceImage, priceIconType);
            changeImageResFunc.call(changeLabel, changeImage, changeIconType);
        }

        private void updateStockListView(List<StockRankItem> items) {
            int sort = mLastSort;
            List<StockRankItem> sortedItems = Observable.from(items).toSortedList(SORT_FUNC_MAP.get(sort)).toBlocking().first();
            RecyclerView recyclerView = v_findView(mContentSection, R.id.recyclerView);
            SimpleRecyclerViewAdapter<StockRankItem> adapter = new SimpleRecyclerViewAdapter.Builder<>(sortedItems)
                    .onCreateItemView(R.layout.cell_stock_change)
                    .onCreateViewHolder(builder -> {
                        builder.bindChildWithTag("nameAndCode", R.id.label_stock_name_and_code)
                                .bindChildWithTag("price", R.id.label_stock_price)
                                .bindChildWithTag("change", R.id.label_stock_change)
                                .configureView((item, pos) -> {
                                    v_setText(builder.getChildWithTag("nameAndCode"), item.nameAndCode);
                                    v_setText(builder.getChildWithTag("price"), item.price);
                                    v_setText(builder.getChildWithTag("change"), item.changeRatio);
                                });
                        return builder.create();
                    })
                    .create();
            recyclerView.setAdapter(adapter);
        }


        private static final int SORT_BY_TIME_DESCENT = 0;
        private static final int SORT_BY_PRICE_ASCENT = 1;
        private static final int SORT_BY_PRICE_DESCENT = 2;
        private static final int SORT_BY_CHANGE_RATIO_ASCENT = 3;
        private static final int SORT_BY_CHANGE_RATIO_DESCENT = 4;

        private static SparseArray<Func2<StockRankItem, StockRankItem, Integer>> SORT_FUNC_MAP = new SparseArray<>();

        static {
            SORT_FUNC_MAP.append(SORT_BY_TIME_DESCENT, (left, right) -> {
                if (left.updateTime > right.updateTime)
                    return -1;
                else if (left.updateTime < right.updateTime)
                    return 1;
                return 0;
            });
            SORT_FUNC_MAP.append(SORT_BY_PRICE_ASCENT, (left, right) -> {
                if (left.rawPrice > right.rawPrice)
                    return 1;
                else if (left.rawPrice < right.rawPrice)
                    return -1;
                return 0;
            });
            SORT_FUNC_MAP.append(SORT_BY_PRICE_DESCENT, (left, right) -> {
                if (left.rawPrice > right.rawPrice)
                    return -1;
                else if (left.rawPrice < right.rawPrice)
                    return 1;
                return 0;
            });
            SORT_FUNC_MAP.append(SORT_BY_CHANGE_RATIO_ASCENT, (left, right) -> {
                if (left.rawChangeRatio > right.rawChangeRatio)
                    return 1;
                else if (left.rawChangeRatio < right.rawChangeRatio)
                    return -1;
                return 0;
            });
            SORT_FUNC_MAP.append(SORT_BY_CHANGE_RATIO_DESCENT, (left, right) -> {
                if (left.rawChangeRatio > right.rawChangeRatio)
                    return -1;
                else if (left.rawChangeRatio < right.rawChangeRatio)
                    return 1;
                return 0;
            });
        }
    }

    public static class StockMarketLiveFragment extends SimpleFragment {

        private int mFirstVisibleChildIndex = 0;
        private StickyListHeadersListView mListView;
        private StockMarketLiveListAdapter mAdapter;
        private List<LiveContentItem> mChildrenList;
        private View mFooterView;

        public StockMarketLiveFragment init(int firstVisibleChildIndex) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_FIRST_VISIBLE_CHILD_IDX_INT, firstVisibleChildIndex);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mFirstVisibleChildIndex = getArguments().getInt(KEY_FIRST_VISIBLE_CHILD_IDX_INT, mFirstVisibleChildIndex);
            return inflater.inflate(R.layout.frag_stock_market_live, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            mListView = v_findView(this, R.id.listView);
            setOnSwipeRefreshListener(() -> {
                mFirstVisibleChildIndex = 0;
                fetchData(false);
            });

            fetchData(true);
        }

        private boolean mIsLoadingMore = false;
        private CommandPageArray<GMFLiveItemMore> currentStore = null;

        private void fetchData(boolean reload) {
            consumeEventMRUpdateUI(StockController.fetchLiveDataList(), reload)
                    .setTag("fresh_data")
                    .onNextSuccess(response -> {
                        currentStore = response.data;
                        updateLiveLiveSection();
                        v_setVisible(mContentSection);
                    })
                    .done();
        }

        private void fetchMoreData() {
            if (mIsLoadingMore || !hasMoreData(currentStore)) {
                v_setText(mFooterView, R.id.label_loading_more, "没有更多数据");
                return;
            }

            mIsLoadingMore = true;
            consumeEventMR(PageArrayHelper.getNextPage(currentStore))
                    .setTag("fetch_more")
                    .onNextSuccess(response -> {
                        currentStore = response.data;
                        updateLiveLiveSection();
                    })
                    .onNextFinish(response -> {
                        mIsLoadingMore = false;
                    })
                    .done();
        }

        private void updateLiveLiveSection() {
            if (currentStore == null)
                return;

            List<GMFLiveItemMore> data = currentStore.data();
            if (data == null || data.size() == 0) {
                v_setVisible(mEmptySection);
                return;
            }
            String todayDayTime = formatSecond(SecondUtil.currentSecond(), "yyyy-MM-dd");
            mChildrenList = Stream.of(data).map(item -> new LiveContentItem(item, formatSecond(item.createTime, "yyyy-MM-dd").equals(todayDayTime))).collect(Collectors.toList());

            boolean isFirst = (mListView.getAdapter() == null);

            if (mAdapter == null) {
                mAdapter = new StockMarketLiveListAdapter();
                mAdapter.setLiveData(mChildrenList);
                mListView.setAdapter(mAdapter);
            } else {
                mAdapter.setLiveData(mChildrenList);
                mAdapter.notifyDataSetChanged();
            }

            if (isFirst) {
                mFooterView = createFooterView(getActivity(), R.layout.footer_loading_more);
                mListView.addFooterView(mFooterView);
            }

            if (mFirstVisibleChildIndex == 0) {
                mListView.setSelection(mFirstVisibleChildIndex);
            } else {
                mListView.setSelectionFromTop(mFirstVisibleChildIndex, dp2px(60));
            }

            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    int lastVisiblePosition = view.getLastVisiblePosition();
                    if (lastVisiblePosition == mListView.getAdapter().getCount() && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                        fetchMoreData();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                }
            });

        }

        private View createFooterView(Context context, int layoutID) {
            FrameLayout tempParent = new FrameLayout(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            return inflater.inflate(layoutID, tempParent, false);
        }
    }


    private static class StockMarketLiveListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

        private List<LiveContentItem> mChildrenList;

        public void setLiveData(List<LiveContentItem> childrenList) {
            mChildrenList = childrenList;
        }

        @Override
        public int getCount() {
            if (mChildrenList != null) {
                return mChildrenList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mChildrenList != null) {
                return mChildrenList.get(position);
            }
            return null;
        }

        @Override
        public long getHeaderId(int position) {
            LiveContentItem item = (LiveContentItem) getItem(position);
            return formatSecond(item.date, "MM-dd");
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_stock_market_live, parent, false);
            }
            LiveContentItem item = (LiveContentItem) getItem(position);
            TextView dateLabel = v_findView(convertView, R.id.label_date);
            dateLabel.setText(item.date);
            dateLabel.setTextColor(item.isToday ? TEXT_RED_COLOR : TEXT_GREY_COLOR);
            v_findView(convertView, R.id.label_today).setVisibility(item.isToday ? View.VISIBLE : View.INVISIBLE);
            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_stock_market_live, parent, false);
            }
            LiveContentItem item = (LiveContentItem) getItem(position);
            v_setText(convertView, R.id.label_content, item.content);
            TextView timeLabel = v_findView(convertView, R.id.label_time);
            timeLabel.setText(item.time);
            if (item.isToday) {
                timeLabel.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(TEXT_RED_COLOR, dp2px(4))));
            } else {
                timeLabel.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(TEXT_GREY_COLOR, dp2px(4))));
            }

            View line = v_findView(convertView, R.id.label_line);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) line.getLayoutParams();
            if (item.isToday && position == 0) {
                params.topMargin = dp2px(16);
            } else {
                params.topMargin = dp2px(0);
            }
            line.setLayoutParams(params);
            line.setBackgroundColor(item.isToday ? 0x33F64046 : TEXT_GREY_COLOR);

            return convertView;
        }
    }

    private static class LiveContentItem {
        public String date;
        public CharSequence content;
        public String time;
        public boolean isToday;

        public LiveContentItem(GMFLiveItemMore data, boolean isToday) {
            this.date = formatSecond(data.createTime, "MM-dd");
            this.content = data.content;
            this.time = FormatUtil.formatSecond(data.createTime, "HH:mm");
            this.isToday = isToday;
        }
    }
}
