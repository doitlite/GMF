package com.goldmf.GMFund.controller;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.method.LinkMovementMethod;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.WebViewFragments.WebViewFragment;
import com.goldmf.GMFund.controller.business.FundController;
import com.goldmf.GMFund.controller.business.StockController;
import com.goldmf.GMFund.controller.internal.CashUIController;
import com.goldmf.GMFund.controller.internal.FundCardViewHelper;
import com.goldmf.GMFund.manager.fortune.FortuneInfo;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.stock.SimulationStockManager;
import com.goldmf.GMFund.model.CommonDefine;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.ProgressButton;
import com.yale.ui.support.AdvanceSwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import rx.functions.Func1;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_RechargePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WebViewPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.FundCardViewHelper.*;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getIncomeTextColor;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.IntExtension.notMatch;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.ObjectExtension.*;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setClickEvent;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setOnRefreshing;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.model.FundBrief.*;
import static com.goldmf.GMFund.model.FundBrief.Fund_Status.Stop;
import static com.goldmf.GMFund.model.User.User_Type.isTrader;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;

/**
 * Created by yale on 16/3/24.
 */
public class StockAccountFragments {
    private StockAccountFragments() {
    }

    public static class OpenSimulationAccountFragment extends SimpleFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_open_simulation_account, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);
            v_setClick(mContentSection, R.id.btn_open, v -> performOpenAccount());

            Fragment childFragment = new WebViewFragment().init(CommonDefine.H5URL_SIMU_ACOUNT_OPEN(), false);
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.container_child_fragment, childFragment);
            transaction.commit();
        }

        @Override
        protected boolean onInterceptGoBack() {
            NotificationCenter.closeOpenSimulationPageSubject.onNext(null);
            return super.onInterceptGoBack();
        }

        private void performOpenAccount() {
            ProgressButton openButton = v_findView(mContentSection, R.id.btn_open);
            openButton.setMode(ProgressButton.Mode.Loading);
            consumeEventMR(StockController.createSimulationStockAccount())
                    .setTag("open")
                    .onNextStart(response -> {
                        openButton.setMode(ProgressButton.Mode.Normal);
                    })
                    .onNextSuccess(response -> {
                        showToast(this, "模拟操盘账号开通成功");
                        NotificationCenter.openSimulationStockAccountSubject.onNext(null);
                        NotificationCenter.userInfoChangedSubject.onNext(null);
                        SimulationStockManager.getInstance().accountStatus = SimulationStockManager.ACCOUNT_STATE_NORMAL;
                        goBack(this);
                    })
                    .onNextFail(response -> {
                        showToast(this, getErrorMessage(response));
                    })
                    .done();
        }
    }

    public static class MyStockAccountDetailFragment extends SimpleFragment {

        private int mMoneyType;
        private boolean mHasData = false;
        private StockAccountHeaderVM mFundFamily;
        private boolean mDataExpired = false;

        public MyStockAccountDetailFragment init(int moneyType) {
            Bundle arguments = new Bundle();
            arguments.putInt(CommonProxyActivity.KEY_MONEY_TYPE_INT, moneyType);
            setArguments(arguments);
            return this;
        }

        public List<FundBrief> getInvestFundList() {
            return opt(mFundFamily).let(it -> it.source.investFunds).or(null);
        }

        public List<FundBrief> getManagedFundList() {
            return opt(mFundFamily).let(it -> it.source.sharingFunds).or(null);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mMoneyType = getArguments().getInt(CommonProxyActivity.KEY_MONEY_TYPE_INT);
            return inflater.inflate(R.layout.frag_my_stock_account_detail, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));
            updateTitle();
            v_setGone(this, R.id.section_left_bottom);
            changeVisibleSection(TYPE_LOADING);

            // bind child views
            ((AdvanceSwipeRefreshLayout) mRefreshLayout).setOnPreInterceptTouchEventDelegate(new AdvanceSwipeRefreshLayout.OnPreInterceptTouchEventDelegate() {
                private ViewPager pager = v_findView(mContentSection, R.id.pager);
                Rect rect = new Rect();

                @Override
                public boolean shouldDisallowInterceptTouchEvent(MotionEvent ev) {

                    pager.getGlobalVisibleRect(rect);
                    if (rect.contains((int) ev.getRawX(), (int) ev.getRawY()) && pager.getChildCount() > 0) {
                        View child;
                        for (int i = 0; i < pager.getChildCount(); i++) {
                            child = pager.getChildAt(i);
                            child.getGlobalVisibleRect(rect);
                            if (rect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                                if (child.getId() == R.id.embed_root) {
                                    return ((ViewGroup) child).getChildAt(0).getScrollY() > 0;
                                }
                                break;
                            }
                        }
                    }

                    return false;
                }
            });
            ((AdvanceSwipeRefreshLayout) mRefreshLayout).setOnInterceptTouchEventDelegate(new AdvanceSwipeRefreshLayout.OnInterceptTouchEventDelegate() {
                private AppBarLayout appBarLayout = v_findView(mContentSection, R.id.appBarLayout);

                @Override
                public boolean onInterceptTouchEvent(MotionEvent event, boolean[] shouldIntercept) {
                    if (appBarLayout.getBottom() < appBarLayout.getMeasuredHeight()) {
                        shouldIntercept[0] = false;
                        return true;
                    }

                    return false;
                }
            });
            setOnSwipeRefreshListener(() -> fetchData(false));
            v_setClick(mReloadSection, v -> {
                v_setGone(v);
                fetchData(true);
            });
            v_setClick(view, R.id.btn_withdraw, v -> CashUIController.performWithdraw(getActivity(), Optional.of(this), mMoneyType));
            v_setClick(view, R.id.btn_recharge, v -> {
                showActivity(this, an_RechargePage(mMoneyType, 0D));
            });
            v_setClick(view, R.id.btn_share, v -> {
                showActivity(this, an_WebViewPage(CommonDefine.H5URL_ShowIncome(mMoneyType)));
                UmengUtil.stat_show_income_event(getActivity(), Optional.of(this));
            });

            fetchData(true);

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }

            consumeEvent(NotificationCenter.cashChangedSubject)
                    .setTag("on_cash_changed")
                    .onNextFinish(ignored -> {
                        mDataExpired = true;
                        if (getUserVisibleHint() && getView() != null) {
                            fetchData(false);
                        }
                    })
                    .done();
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (mDataExpired) {
                    fetchData(false);
                    mDataExpired = false;
                }
            }
        }

        private void updateTitle() {
            if (mMoneyType == Money_Type.CN) {
                updateTitle("我的沪深账户");
            } else if (mMoneyType == Money_Type.HK) {
                updateTitle("我的港股账户");
            }
        }

        private void fetchData(boolean reload) {

            consumeEventMRUpdateUI(FundController.fetchMyInvestedFundList(mMoneyType), reload)
                    .setTag("fund_list")
                    .onNextSuccess(response -> {
                        mFundFamily = new StockAccountHeaderVM(response.data);
                        updateHeaderSection(mFundFamily);
                        updateTabLayoutAndViewPager(mFundFamily);

                        v_setVisible(this, R.id.section_content);
                        v_setVisible(this, R.id.section_left_bottom);
                        mHasData = true;
                    })
                    .done();

//            v_setGone(this, R.id.section_reload);
//            addSubscriptionToMain("fund_list", FundController.fetchMyInvestedFundList(mMoneyType)
//                    .map(response -> isSuccess(response) ? map(response, new StockAccountHeaderVM(response.data)) : cast(response, StockAccountHeaderVM.class))
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .doOnCompleted(() -> mRefreshLayout.setRefreshing(false))
//                    .subscribe(data -> {
//                        if (MResultExtension.isSuccess(data)) {
//                            mFundFamily = data.data;
//                            updateHeaderSection(mFundFamily);
//                            updateTabLayoutAndViewPager(mFundFamily);
//
//                            v_setVisible(this, R.id.section_content);
//                            v_setVisible(this, R.id.section_left_bottom);
//                            mHasData = true;
//                        } else {
//                            if (mHasData) {
//                                showToast(this, MResultExtension.getErrorMessage(data));
//                            } else {
//                                v_setVisible(this, R.id.section_reload);
//                                View reloadSection = v_findView(this, R.id.section_reload);
//                                v_setText(reloadSection, R.id.label_title, MResultExtension.getErrorMessage(data));
//                                mRefreshLayout.setEnabled(false);
//                            }
//                        }
//                        v_setGone(this, R.id.section_loading);
//                    }));
            mDataExpired = false;
        }

        private void updateHeaderSection(StockAccountHeaderVM info) {
            View header = v_findView(mContentSection, R.id.section_header);
            v_setText(header, R.id.label_income, info.expectIncome);
            v_setText(header, R.id.label_total_capital, info.totalCapital);
            v_setText(header, R.id.label_invested_amount, info.investedAmount);
            v_setText(header, R.id.label_balance, info.balance);
        }

        private void updateTabLayoutAndViewPager(StockAccountHeaderVM info) {
            TabLayout tabLayout = v_findView(mContentSection, R.id.tabLayout);
            ViewPager pager = v_findView(mContentSection, R.id.pager);
            if (pager.getAdapter() == null) {
                boolean isTrader = safeGet(() -> isTrader(MineManager.getInstance().getmMe().type), false);
                int[] status;
                String[] titles;
                long investedFundCount = Stream.of(getInvestFundList()).filter(it -> notMatch(it.status, Stop)).count();
                long stopFundCount = Stream.of(getInvestFundList()).filter(it -> anyMatch(it.status, Stop)).count();
                long managedFundCount = Stream.of(getManagedFundList()).filter(it -> safeGet(() -> MineManager.getInstance().getmMe().index == it.traderID, false)).count();

                if (isTrader) {
                    status = new int[]{StockAccountDetailPageFragment.PAGE_TYPE_LOCKED, StockAccountDetailPageFragment.PAGE_TYPE_STOP, StockAccountDetailPageFragment.PAGE_TYPE_MANAGED};
                    titles = new String[]{"已投资 " + investedFundCount, "已结算 " + stopFundCount, "管理组合 " + managedFundCount};
                } else {
                    status = new int[]{StockAccountDetailPageFragment.PAGE_TYPE_LOCKED, StockAccountDetailPageFragment.PAGE_TYPE_STOP};
                    titles = new String[]{"已投资 " + investedFundCount, "已结算" + stopFundCount};
                }
                pager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
                    @Override
                    public Fragment getItem(int position) {
                        return new StockAccountDetailPageFragment().init(status[position]);
                    }

                    @Override
                    public int getCount() {
                        return titles.length;
                    }

                    @Override
                    public Object instantiateItem(ViewGroup container, int position) {
                        StockAccountDetailPageFragment fragment = (StockAccountDetailPageFragment) super.instantiateItem(container, position);
                        return fragment;
                    }

                    @Override
                    public CharSequence getPageTitle(int position) {
                        return titles[position];
                    }
                });
                tabLayout.setupWithViewPager(pager);
            }
        }
    }

    private static class StockAccountHeaderVM {
        public FortuneInfo source;
        public CharSequence expectIncome;
        public CharSequence totalCapital;
        public CharSequence investedAmount;
        public CharSequence balance;

        public StockAccountHeaderVM(FortuneInfo raw) {
            int moneyType = safeGet(()->raw.family.moneyType, Money_Type.CN);

            this.source = raw;
            this.expectIncome = concat("预计收益 (" + Money_Type.getUnit(moneyType) + ")",
                    setFontSize(setColor(formatMoney(raw.family.income, true, 2), getIncomeTextColor(raw.family.income)), sp2px(30)));
            this.totalCapital = concat("总资产",
                    setFontSize(setColor(formatMoney(raw.family.totalCapital, false, 2), TEXT_WHITE_COLOR), sp2px(14)));
            this.investedAmount = concat("已投资",
                    setFontSize(setColor(formatMoney(raw.family.investMoney, false, 2), TEXT_WHITE_COLOR), sp2px(14)));
            this.balance = concat("余额",
                    setFontSize(setColor(formatMoney(raw.family.cashBalance, false, 2), TEXT_WHITE_COLOR), sp2px(14)));
        }
    }

    private static MyStockAccountDetailFragment getParentFragment(Fragment fragment) {
        return safeGet(() -> (MyStockAccountDetailFragment) fragment.getParentFragment(), null);
    }

    private static List<FundBrief> getInvestFundListFromParent(Fragment fragment) {
        return safeGet(() -> getParentFragment(fragment).getInvestFundList(), Collections.<FundBrief>emptyList());
    }

    private static List<FundBrief> getManagedFundListFromParent(Fragment fragment) {
        return safeGet(() -> getParentFragment(fragment).getManagedFundList(), Collections.<FundBrief>emptyList());
    }

    private static int getParentMoneyType(Fragment fragment) {
        return safeGet(() -> getParentFragment(fragment).mMoneyType, Money_Type.CN);
    }

    public static class StockAccountDetailPageFragment extends Fragment {
        private static final int PAGE_TYPE_LOCKED = 0;
        private static final int PAGE_TYPE_STOP = 1;
        private static final int PAGE_TYPE_MANAGED = 2;

        private static SparseArray<Func1<FundBrief, Boolean>> FILTER_FUNC_MAP = new SparseArray<>();

        static {
            FILTER_FUNC_MAP.append(PAGE_TYPE_LOCKED, it -> notMatch(it.status, Stop));
            FILTER_FUNC_MAP.append(PAGE_TYPE_STOP, it -> anyMatch(it.status, Stop));
            FILTER_FUNC_MAP.append(PAGE_TYPE_MANAGED, it -> safeGet(() -> it.traderID == MineManager.getInstance().getmMe().index, false));
        }

        private int mPageType;
        private View mContentSection;
        private View mEmptySection;
        private int mMoneyType;

        public StockAccountDetailPageFragment init(int pageType) {
            Bundle arguments = new Bundle();
            arguments.putInt("page_type", pageType);
            setArguments(arguments);
            return this;
        }

        public void updateStatus(int pageType) {
            getArguments().putInt("page_type", pageType);
            mPageType = pageType;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mPageType = getArguments().getInt("page_type");
            mMoneyType = getParentMoneyType(this);
            return inflater.inflate(R.layout.frag_stock_account_detail_page, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mContentSection = v_findView(view, R.id.section_content);
            mEmptySection = v_findView(view, R.id.section_empty);

            {
                CharSequence leftText;
                CharSequence mediumText;
                CharSequence rightText;
                switch (mPageType) {
                    case PAGE_TYPE_STOP: {
                        leftText = "组合/结束日期";
                        mediumText = "已投资(" + Money_Type.getUnit(mMoneyType) + ")";
                        rightText = "最终收益";
                    }
                    break;
                    case PAGE_TYPE_MANAGED: {
                        leftText = "组合/结束日期";
                        mediumText = "管理规模";
                        rightText = "已提/总分成";
                    }
                    break;
                    default: {
                        leftText = "组合/剩余天数";
                        mediumText = "已投资(" + Money_Type.getUnit(mMoneyType) + ")";
                        rightText = "预计收益";
                    }
                    break;
                }

                View header = v_findView(this, R.id.header);
                v_setText(header, R.id.label_left, leftText);
                v_setText(header, R.id.label_medium, mediumText);
                v_setText(header, R.id.label_right, rightText);
            }

            {
                CharSequence text;
                switch (mPageType) {
                    case PAGE_TYPE_STOP: {
                        text = "组合结算后，资金将会自动转入账户余额";
                    }
                    break;
                    case PAGE_TYPE_MANAGED: {
                        text = "操盘手收益分成将按照协议定期转入余额中";
                    }
                    break;
                    default: {
                        if (mMoneyType == Money_Type.CN) {
                            text = concat("沪深余额和未运行资金每天也可获得收益", setColor(setClickEvent("了解余额生息", v -> showActivity(this, an_WebViewPage(CommonDefine.H5URL_Help_Money()))), 0xFF3498DB));
                        } else {
                            text = "投资操盘侠港股组合\n让专业操盘手帮你投资";
                        }
                    }
                    break;
                }
                TextView hintView = v_findView(mContentSection, R.id.label_hint);
                v_setText(hintView, text);
                hintView.setMovementMethod(new LinkMovementMethod());
            }


            boolean isFundListEmpty = true;
            List<FundBrief> funds = anyMatch(mPageType, PAGE_TYPE_MANAGED) ? getManagedFundListFromParent(this) : getInvestFundListFromParent(this);
            if (!funds.isEmpty()) {
                if (Stream.of(funds).filter(it -> FILTER_FUNC_MAP.get(mPageType).call(it)).count() > 0) {
                    LinearLayout listView = v_findView(mContentSection, R.id.list);
                    funds = Stream.of(funds)
                            .filter(it -> FILTER_FUNC_MAP.get(mPageType).call(it))
                            .collect(Collectors.toList());
                    boolean isManageTab = this.mPageType == PAGE_TYPE_MANAGED;
                    int flags = isManageTab ? FLAG_USE_MANAGE_STYLE : FLAG_USE_INVEST_STYLE;
                    FundCardViewHelper.resetFundCardListViewWithFundBrief(listView, funds, flags);
                    v_setVisible(mContentSection);
                    v_setGone(mEmptySection);
                    isFundListEmpty = false;
                }
            }
            if (isFundListEmpty) {
                v_setGone(mContentSection);
                v_setVisible(mEmptySection);
                v_setText(mEmptySection, R.id.label_title, mPageType == PAGE_TYPE_LOCKED ? "暂无已投资组合" : "暂无已结算组合");
                v_setText(mEmptySection, R.id.label_subtitle, "");
            }
        }
    }

}
