package com.goldmf.GMFund.controller;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.goldmf.GMFund.BuildConfig;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.controller.StockTradeFragments.StockTradeFragment;
import com.goldmf.GMFund.controller.business.CommonController;
import com.goldmf.GMFund.controller.business.FundController;
import com.goldmf.GMFund.controller.business.UserController;
import com.goldmf.GMFund.controller.internal.ChildBinder;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.controller.internal.ChildBinders;
import com.goldmf.GMFund.controller.internal.FundCardViewHelper;
import com.goldmf.GMFund.controller.internal.FundCardViewHelper.FundCardViewModel;
import com.goldmf.GMFund.controller.internal.TraderCardViewHelper;
import com.goldmf.GMFund.manager.discover.FocusInfo;
import com.goldmf.GMFund.manager.discover.UserManager;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.CommonDefine.PlaceHolder;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.FundBrief.Fund_Status;
import com.goldmf.GMFund.model.User.User_Type;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import yale.extension.common.shape.RoundCornerShape;
import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ApplyTalentPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ApplyTraderPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_AuthenticPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_CreateFundPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_LoginPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WebViewPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.ObjectExtension.apply;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.getSimpleAdapter;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.isScrollToBottom;
import static com.goldmf.GMFund.extension.UIControllerExtension.createErrorDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_isVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.getNextPage;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.getPreviousPage;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.hasMoreData;

/**
 * Created by yale on 15/10/14.
 */
public class TraderFragments {
    private TraderFragments() {
    }


    public static class MyManagedFundsFragment extends SimpleFragment {

        private boolean mDataExpiredRemote = true;
        private boolean mDataExpiredLocal = false;
        private PublishSubject<Void> mDataSetChangedSubject = PublishSubject.create();
        private Func1<Integer, List<FundBrief>> mPageDataGetter = pos -> Collections.emptyList();

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_my_managed_funds, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            v_setClick(this, R.id.btn_create, () -> {
                mDataExpiredRemote = true;
                showActivity(this, an_CreateFundPage());
            });
            v_setClick(mEmptySection, R.id.btn_create_bottom, () -> {
                mDataExpiredRemote = true;
                showActivity(this, an_CreateFundPage());
            });
            setOnSwipeRefreshListener(() -> performFetchDataIfNeeded(false, false, false));
            v_setClick(mReloadSection, v -> {
                performFetchDataIfNeeded(true, false, false);
            });

            consumeEvent(NotificationCenter.onCreateNewFundSubject)
                    .onNextFinish(response -> {
                        mDataExpiredRemote = true;
                        if (getUserVisibleHint() && getView() != null) {
                            setUserVisibleHint(true);
                        }
                    })
                    .done();

            consumeEvent(StockTradeFragment.sEnterStockTradeFragmentSubject)
                    .onNextFinish(ignored -> {
                        mDataExpiredLocal = true;
                        if (getView() != null && getUserVisibleHint()) {
                            List<FundBrief> allItems = safeGet(() -> FundController.getCachedMyFundList(), Collections.<FundBrief>emptyList());
                            if (!allItems.isEmpty()) {
                                resetContentSection(allItems);
                            }
                            mDataExpiredLocal = false;
                        }
                    })
                    .done();

            changeVisibleSection(TYPE_LOADING);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mDataExpiredRemote = true;
            mDataExpiredLocal = false;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (mDataExpiredRemote) {
                    boolean isContentVisible = v_isVisible(mContentSection);
                    performFetchDataIfNeeded(!isContentVisible, false, false);
                    mDataExpiredRemote = false;
                }
                if (mDataExpiredLocal) {
                    List<FundBrief> allItems = safeGet(() -> FundController.getCachedMyFundList(), Collections.<FundBrief>emptyList());
                    if (!allItems.isEmpty()) {
                        resetContentSection(allItems);
                    }
                    mDataExpiredLocal = false;
                }
            }
        }

        private void performFetchDataIfNeeded(boolean reload, boolean useCache, boolean hasCheck) {
            boolean isTrader = safeGet(() -> MineManager.getInstance().getmMe().type == User_Type.Trader, false);
            boolean isTalent = safeGet(() -> MineManager.getInstance().getmMe().type == User_Type.Talent, false);
            boolean hasCreateFundPermission = isTrader || isTalent;
            if (!hasCreateFundPermission && !hasCheck) {
                consumeEventMRUpdateUI(UserController.refreshUserInfo(false), reload)
                        .onNextSuccess(response -> {
                            performFetchDataIfNeeded(reload, useCache, true);
                        })
                        .done();
            } else if (hasCreateFundPermission) {
                consumeEventMRUpdateUI(FundController.fetchMyFundList(useCache), reload)
                        .setTag("fund_list")
                        .onNextSuccess(response -> {
                            List<FundBrief> allItems = safeGet(() -> response.data, Collections.<FundBrief>emptyList());
                            v_setVisibility(this, R.id.btn_create, allItems.size() > 0 ? View.VISIBLE : View.GONE);
                            if (!allItems.isEmpty()) {
                                resetContentSection(allItems);
                            } else {
//                                unsubscribeFromMain("update_list_timer");
                                resetEmptySection(isTrader);
                            }
                        })
                        .done();

            } else {
                createErrorDialog(this, "非操盘手/牛人不允许创建组合").show();
            }
        }

        private void resetContentSection(List<FundBrief> allItems) {
            List<FundBrief> raisingFunds = Stream.of(allItems)
                    .filter(it -> Fund_Status.beforeLockIn(it.status))
                    .collect(Collectors.toList());
            List<FundBrief> lockedFunds = Stream.of(allItems)
                    .filter(it -> anyMatch(it.status, Fund_Status.LockIn))
                    .collect(Collectors.toList());
            List<FundBrief> stopFunds = Stream.of(allItems)
                    .filter(it -> anyMatch(it.status, Fund_Status.Stop))
                    .collect(Collectors.toList());
            mPageDataGetter = pos -> {
                if (pos == 0) {
                    return raisingFunds;
                } else if (pos == 1) {
                    return lockedFunds;
                } else {
                    return stopFunds;
                }
            };

            TabLayout tabLayout = v_findView(mContentSection, R.id.tabLayout);
            ViewPager pager = v_findView(mContentSection, R.id.pager);
            if (pager.getAdapter() != null) {
                pager.getAdapter().notifyDataSetChanged();
                tabLayout.setupWithViewPager(pager);
                mDataSetChangedSubject.onNext(null);
            } else {
                pager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
                    @Override
                    public Fragment getItem(int position) {
                        MyManagedFundPageFragment ret = new MyManagedFundPageFragment();
                        ret.mPosition = position;
                        return ret;
                    }

                    @Override
                    public int getCount() {
                        return 3;
                    }

                    @Override
                    public CharSequence getPageTitle(int position) {
                        if (position == 0) {
                            return "投资中 " + raisingFunds.size();
                        } else if (position == 1) {
                            return "运行中 " + lockedFunds.size();
                        } else {
                            return "已结算 " + stopFunds.size();
                        }
                    }
                });

                tabLayout.setupWithViewPager(pager);
            }

            changeVisibleSection(TYPE_CONTENT);

        }

        private void resetEmptySection(boolean isTrader) {
            changeVisibleSection(TYPE_EMPTY);

            String userName = safeGet(() -> MineManager.getInstance().getmMe().getName(), PlaceHolder.NULL_VALUE);

            TextView contentLabel = v_findView(mEmptySection, R.id.label_content);
            contentLabel.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(WHITE_COLOR, dp2px(4))));
            if (isTrader) {
                CharSequence text = String.format(Locale.getDefault(), "%s：\n" +
                        "\n" +
                        "首先，经过我们的层层筛选和慎重考虑后，恭喜你成为操盘侠平台的认证操盘手。\n" +
                        "\n" +
                        "操盘侠倡导让专业的人做财富管理的投资理念，致力于服务更多的专业操盘手，依靠专业的投资能力为大众投资者创造更多的投资机会、把控投资风险，提供安全和便捷的投资体验，并带来稳定的收益。\n" +
                        "\n" +
                        "操盘乐是操盘侠推出的由优秀的操盘手领投、投资人跟投的股票合买投资产品，只需简单几步即可创建；同时操盘侠提供高效稳定的股票交易和资产管理软件，让你轻松操盘。\n" +
                        "\n" +
                        "新的旅程，即将开始，点击下方开启第一个操盘之旅。", userName);
                contentLabel.setText(text);
            } else {
                CharSequence text = String.format(Locale.getDefault(), "%s：\n" +
                        "\n" +
                        "首先，感谢你对平台的信任申请成为操盘侠平台的\n" +
                        "股市牛人。\n" +
                        "\n" +
                        "操盘侠是一个公平、透明、靠谱的平台，致力于互联网金融向垂直领域发展，满足用户深层次的金融服务需求；操盘侠需要这样自信的你、怀抱着拥有一颗希望成为真正职业资产管理人的心，勇敢站出来为中国股市里的散户服务，同时成就自己。\n" +
                        "\n" +
                        "操盘乐是操盘侠推出的由合格牛人领投、投资人跟投的股票合买投资产品，只需简单几步即可创建；同时操盘侠提供高效稳定的股票交易和资产管理软件，让你轻松操盘。\n" +
                        "\n" +
                        "新的旅程，即将开始，点击下方开启第一个操盘之旅。", userName);
                contentLabel.setText(text);

            }
        }

    }

    public static class MyManagedFundPageFragment extends SimpleFragment {
        private int mPosition = 0;

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
            return inflater.inflate(R.layout.frag_fund_list, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            resetContentView();

            PublishSubject<Void> observable = safeGet(() -> MyManagedFundsFragment.class.cast(getParentFragment()).mDataSetChangedSubject,
                    null);
            if (observable != null) {
                consumeEvent(observable)
                        .onNextFinish(response -> {
                            if (getView() != null && getUserVisibleHint()) {
                                resetContentView();
                            }
                        })
                        .done();
            }
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (getView() != null && getUserVisibleHint()) {
                resetContentView();
            }
        }

        private void resetContentView() {
            List<FundBrief> relativeItems = safeGet(() -> {
                MyManagedFundsFragment parent = (MyManagedFundsFragment) getParentFragment();
                return parent.mPageDataGetter.call(mPosition);
            }, Collections.<FundBrief>emptyList());
            if (relativeItems.isEmpty()) {
                changeVisibleSection(TYPE_EMPTY);
            } else {
                LinearLayout listView = v_findView(mContentSection, android.R.id.list);
                List<FundCardViewModel> items = Stream.of(relativeItems)
                        .map(it -> new FundCardViewModel(it))
                        .collect(Collectors.toList());
                Func0<List<FundCardViewModel>> itemsGetter = () -> items;
                int flags = FundCardViewHelper.FLAG_SHOW_INCOME_DETAIL;
                FundCardViewHelper.resetFundCardListView(listView, itemsGetter, flags);
                changeVisibleSection(TYPE_CONTENT);
            }
        }
    }


    public static class MoreTraderFragment extends SimpleFragment {

        public static final int SOURCE_TYPE_TRADER = 0;
        public static final int SOURCE_TYPE_TALENT = 1;

        private int mSourceType;
        private boolean mIsFetchingMore = false;
        private boolean mDataExpired = true;

        public MoreTraderFragment init(int sourceType) {
            Bundle arguments = new Bundle();
            arguments.putInt(CommonProxyActivity.KEY_SOURCE_TYPE_INT, sourceType);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mSourceType = getArguments().getInt(CommonProxyActivity.KEY_SOURCE_TYPE_INT, SOURCE_TYPE_TRADER);
            return inflater.inflate(R.layout.frag_more_trader, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            if (anyMatch(mSourceType, SOURCE_TYPE_TRADER, SOURCE_TYPE_TALENT)) {
                updateTitle(mSourceType == SOURCE_TYPE_TRADER ? "操盘手" : "牛人");
                v_setText(this, R.id.btn_apply, mSourceType == SOURCE_TYPE_TRADER ? "申请成为操盘手" : "申请成为牛人");
                v_setClick(this, R.id.btn_apply, v -> {
                    performApply();
                });
            } else {
                goBack(this);
            }

            changeVisibleSection(TYPE_LOADING);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mIsFetchingMore = false;
            mDataExpired = true;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (getUserVisibleHint() && getView() != null) {
                if (mDataExpired) {
                    boolean isContentVisible = v_isVisible(mContentSection);
                    fetchData(!isContentVisible);
                    mDataExpired = false;
                }
            }
        }

        private void performApply() {
            unsubscribeFromMain("perform_apply");

            boolean isLogin = safeGet(() -> MineManager.getInstance().isLoginOK(), false);
            if (!isLogin) {
                showActivity(this, an_LoginPage());
                consumeEvent(Observable.merge(NotificationCenter.loginSubject, NotificationCenter.loginSubject).limit(1))
                        .setTag("perform_apply")
                        .onNextFinish(nil -> {
                            boolean isLoginNow = safeGet(() -> MineManager.getInstance().isLoginOK(), false);
                            if (isLoginNow) {
                                performApply();
                            }
                        })
                        .done();
                return;
            }

            boolean isAuthentic = safeGet(() -> MineManager.getInstance().getmMe().setAuthenticate, false);
            if (!isAuthentic) {
                new GMFDialog.Builder(getActivity())
                        .setMessage(anyMatch(mSourceType, SOURCE_TYPE_TRADER) ? "申请操盘手前需要进行实名认证，是否立即认证？" : "申请牛人前需要进行实名认证，是否立即认证？")
                        .setPositiveButton("立即认证", (dialog, which) -> {
                            dialog.dismiss();
                            showActivity(this, an_AuthenticPage());
                            consumeEvent(NotificationCenter.closeAuthenticPageSubject.limit(1))
                                    .setTag("perform_apply")
                                    .onNextFinish(nil -> {
                                        boolean isAuthenticNow = safeGet(() -> MineManager.getInstance().getmMe().setAuthenticate, false);
                                        if (isAuthenticNow) {
                                            performApply();
                                        }
                                    })
                                    .done();
                        })
                        .setNegativeButton("取消")
                        .create().show();
                return;
            }

            mDataExpired = true;
            if (anyMatch(mSourceType, SOURCE_TYPE_TRADER)) {
                showActivity(this, an_ApplyTraderPage());
            } else if (anyMatch(mSourceType, SOURCE_TYPE_TALENT)) {
                showActivity(this, an_ApplyTalentPage());
            }
        }


        private void fetchData(boolean reload) {
            Observable<MResults.MResultsInfo<UserManager.TraderUserPage>> observable;
            observable = mSourceType == SOURCE_TYPE_TRADER ? CommonController.fetchTraderList() : CommonController.fetchTalentList();
            observable = observable.map(it -> apply(it, response -> response.isSuccess = response.isSuccess && response.data != null));
            consumeEventMRUpdateUI(observable, reload)
                    .onNextSuccess(response -> {
                        if (response.data.data().size() > 0) {
                            resetContentView(response.data);
                            changeVisibleSection(TYPE_CONTENT);
                        } else {
                            changeVisibleSection(TYPE_EMPTY);
                        }
                    })
                    .done();
        }

        private void resetContentView(UserManager.TraderUserPage pageArray) {
            resetRefreshLayout(pageArray);
            resetRecyclerView(pageArray);
        }

        private void resetRefreshLayout(UserManager.TraderUserPage pageArray) {
            setOnSwipeRefreshListener(() -> {
                consumeEventMR(getPreviousPage(pageArray))
                        .onNextSuccess(response -> {
                            setSwipeRefreshing(false);
                            resetContentView(pageArray);
                        })
                        .done();
            });
        }

        private void resetRecyclerView(final UserManager.TraderUserPage pageArray) {
            Context ctx = getActivity();
            Resources res = ctx.getResources();

            List<TraderCardViewHelper.TraderCardVM> items = Stream.of(pageArray.data())
                    .map(it -> new TraderCardViewHelper.TraderCardVM(it))
                    .collect(Collectors.toList());
            RecyclerView recyclerView = v_findView(this, R.id.recyclerView);
            if (recyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<TraderCardViewHelper.TraderCardVM> adapter = getSimpleAdapter(recyclerView);
                adapter.resetItems(items);
            } else {
                recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
                recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                        super.getItemOffsets(outRect, view, parent, state);
                        int adapterPos = parent.getChildAdapterPosition(view);
                        boolean isHeader = adapterPos == 0;
                        boolean isLastCell = adapterPos == parent.getAdapter().getItemCount() - 1;
                        if (!isHeader) {
                            outRect.top = dp2px(8);
                            outRect.left = dp2px(10);
                            outRect.right = dp2px(10);
                        }
                        if (isLastCell) {
                            outRect.bottom = dp2px(36);
                        }
                    }
                });
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        if (!mIsFetchingMore && hasMoreData(pageArray) && isScrollToBottom(recyclerView)) {
                            mIsFetchingMore = true;
                            consumeEventMR(getNextPage(pageArray))
                                    .onNextSuccess(response -> {
                                        resetContentView(pageArray);
                                    })
                                    .onNextFinish(response -> {
                                        mIsFetchingMore = false;
                                    })
                                    .done();
                        }
                    }
                });
                SimpleRecyclerViewAdapter<TraderCardViewHelper.TraderCardVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                        .onCreateItemView((parent, type) -> {
                            return TraderCardViewHelper.createCell(parent);
                        })
                        .onCreateViewHolder(builder -> {
                            ChildBinder binder = ChildBinders.createWithBuilder(builder);
                            TraderCardViewHelper.bindChildren(binder);
                            builder.configureView((item, pos) -> {
                                TraderCardViewHelper.configureCell(binder, () -> item);
                            });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            View itemView = holder.itemView;
                            Func0<TraderCardViewHelper.TraderCardVM> itemGetter = () -> ad.getItem(holder.getAdapterPosition());
                            TraderCardViewHelper.afterCellCreated(itemView, itemGetter, 0);
                        })
                        .create();
                View headerView = adapter.createHeaderView(ctx, (parent, inflater) -> {
                    GenericDraweeHierarchy hierarchy = GenericDraweeHierarchyBuilder.newInstance(res)
                            .setPlaceholderImage(res.getDrawable(R.mipmap.ic_fund_cover_placeholder))
                            .setPlaceholderImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                            .setDesiredAspectRatio(2.5f)
                            .build();
                    SimpleDraweeView coverImage = new SimpleDraweeView(ctx, hierarchy);
                    coverImage.setId(R.id.img_cover);
                    recyclerView.post(() -> {
                        int width = recyclerView.getMeasuredWidth();
                        int height = (int) ((float) width / 2.5f);
                        coverImage.setLayoutParams(new FrameLayout.LayoutParams(width, height));
                    });
                    return coverImage;
                });
                adapter.addHeader(headerView);
                recyclerView.setAdapter(adapter);
            }
            SimpleRecyclerViewAdapter<TraderCardViewHelper.TraderCardVM> adapter = getSimpleAdapter(recyclerView);
            resetHeaderView(adapter, pageArray);
        }

        private void resetHeaderView(SimpleRecyclerViewAdapter<TraderCardViewHelper.TraderCardVM> adapter, UserManager.TraderUserPage pageArray) {
            View headerView = adapter.getHeader(0);
            FocusInfo focusInfo = pageArray.focusInfo;
            if (focusInfo != null) {
                v_setImageUri(headerView, R.id.img_cover, focusInfo.imageUrl);
                v_setClick(headerView, R.id.img_cover, v -> {
                    CMDParser.parse(focusInfo.tarLink).call(getActivity());
                });
            }
        }
    }

    public static class ApplyTraderFragment extends WebViewFragments.WebViewFragment {
        private boolean mForceGoBack = false;

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            consumeEvent(NotificationCenter.disallowInterceptGoBackSubject)
                    .setTag("disallow_intercept_go_back")
                    .onNextFinish(response -> {
                        mForceGoBack = true;
                    })
                    .done();
        }

        @Override
        protected boolean onInterceptGoBack() {
            if (!mForceGoBack) {
                GMFDialog exitDialog = new GMFDialog.Builder(getActivity())
                        .setMessage("是否离开当前操盘手申请流程？")
                        .setPositiveButton("离开", (dialog, which) -> {
                            dialog.dismiss();
                            mForceGoBack = true;
                            goBack(this);
                        })
                        .setNegativeButton("取消")
                        .create();
                exitDialog.show();
            }
            return !mForceGoBack;
        }
    }

    public static class ApplyTalentFragment extends WebViewFragments.WebViewFragment {
        private boolean mForceGoBack = false;

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            consumeEvent(NotificationCenter.disallowInterceptGoBackSubject)
                    .setTag("disallow_intercept_go_back")
                    .onNextFinish(response -> {
                        mForceGoBack = true;
                    })
                    .done();
        }

        @Override
        protected boolean onInterceptGoBack() {
            if (!mForceGoBack) {
                GMFDialog exitDialog = new GMFDialog.Builder(getActivity())
                        .setMessage("是否离开当前牛人申请流程？")
                        .setPositiveButton("离开", (dialog, which) -> {
                            dialog.dismiss();
                            mForceGoBack = true;
                            goBack(this);
                        })
                        .setNegativeButton("取消")
                        .create();
                exitDialog.show();
            }
            return !mForceGoBack;
        }
    }

    public static class CreateFundFragment extends WebViewFragments.WebViewFragment {
        private boolean mForceGoBack = false;

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            if (BuildConfig.DEBUG) {
                mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            }

            consumeEvent(NotificationCenter.disallowInterceptGoBackSubject)
                    .setTag("disallow_intercept_go_back")
                    .onNextFinish(response -> {
                        mForceGoBack = true;
                    })
                    .done();

            consumeEvent(NotificationCenter.cashChangedSubject)
                    .setTag("after_cash_changed")
                    .onNextFinish(response -> {
                        if (getView() != null) {
                            mWebView.reload();
                        }
                    })
                    .done();

            consumeEvent(NotificationCenter.investedFundSubject)
                    .setTag("after_invest_fund")
                    .onNextFinish(response -> {
                        mForceGoBack = true;
                        goBack(this);
                    })
                    .done();
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                mWebView.reload();
            }
        }

        @Override
        protected boolean onInterceptGoBack() {
            if (!mForceGoBack) {
                GMFDialog exitDialog = new GMFDialog.Builder(getActivity())
                        .setMessage("是否离开当前操盘乐组合创建流程?")
                        .setPositiveButton("离开", (dialog, which) -> {
                            dialog.dismiss();
                            mForceGoBack = true;
                            goBack(this);
                        })
                        .setNegativeButton("取消")
                        .create();
                exitDialog.show();
            }
            return !mForceGoBack;
        }
    }
}
