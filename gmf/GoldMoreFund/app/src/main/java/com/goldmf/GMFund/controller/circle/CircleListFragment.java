package com.goldmf.GMFund.controller.circle;

import android.animation.ObjectAnimator;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.controller.SimpleFragment;
import com.goldmf.GMFund.controller.business.ChatController;
import com.goldmf.GMFund.controller.business.CircleController;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.extension.RecyclerViewExtension;
import com.goldmf.GMFund.manager.message.BarMessageManager;
import com.goldmf.GMFund.manager.message.MessageManager;
import com.goldmf.GMFund.manager.message.MessageSession;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.util.IntCounter;
import com.goldmf.GMFund.util.UmengUtil;
import com.orhanobut.logger.Logger;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.subjects.PublishSubject;
import yale.extension.common.shape.RoundCornerShape;
import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_LINK_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_MESSAGE_TYPE_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_SESSION_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_TAB_IDX_INT;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_CircleDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_CircleHintPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.ObjectExtension.apply;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.isScrollToBottom;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_isVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_setBehavior;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.manager.message.MessageSession.Session_Add_Type_Intelligence;
import static com.goldmf.GMFund.manager.message.MessageSession.Session_Add_Type_normal_2;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.getNextPage;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.getPreviousPage;
import static com.goldmf.GMFund.protocol.base.PageArrayHelper.hasMoreData;
import static com.goldmf.GMFund.util.UmengUtil.stat_click_event;
import static java.lang.Math.max;

/**
 * Created by yale on 16/5/10.
 */
public class CircleListFragment extends SimpleFragment {

    private int mMessageType;
    private String mLinkID;
    private String mSessionID;

    private PublishSubject<Integer> mStartFetchMessageListSubject = PublishSubject.create();
    private PublishSubject<Integer> mFinishFetchCircleListSubject = PublishSubject.create();

    private VM mVM = VM.EMPTY;
    private boolean mDataExpired = false;

    public CircleListFragment init(int messageType, String linkID, String sessionID) {
        Bundle arguments = new Bundle();
        arguments.putInt(KEY_MESSAGE_TYPE_INT, messageType);
        arguments.putString(KEY_LINK_ID_STRING, linkID);
        arguments.putString(KEY_SESSION_ID_STRING, sessionID);
        setArguments(arguments);
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMessageType = getArguments().getInt(KEY_MESSAGE_TYPE_INT);
        mLinkID = getArguments().getString(KEY_LINK_ID_STRING);
        mSessionID = getArguments().getString(KEY_SESSION_ID_STRING);
        return inflater.inflate(R.layout.frag_circle_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        stat_click_event(UmengUtil.eEVENTIDTopicListEnter);

        setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
        setupBackButton(this, findToolbar(this));
        changeVisibleSection(TYPE_LOADING);

        setOnSwipeRefreshListener(() -> fetchData(false));
        v_setClick(mReloadSection, v -> fetchData(true));

        Observable<Void> afterDonateObservable = NotificationCenter.afterDonateScoreSubject
                .filter(it -> !getUserVisibleHint())
                .debounce(800, TimeUnit.MILLISECONDS);
        consumeEvent(afterDonateObservable)
                .onNextFinish(ignored -> {
                    if (getView() != null) {
                        mDataExpired = true;
                        if (getUserVisibleHint()) {
                            runOnUIThreadDelayed(() -> fetchData(false), 1000L);
                            mDataExpired = false;
                        }
                    }
                })
                .done();

        consumeEvent(mFinishFetchCircleListSubject)
                .onNextFinish(response -> {
                    if (mVM != null) {
                        resetToolbar(mVM);
                    }
                })
                .done();

        consumeEvent(NotificationCenter.onWriteNewArticleSubject.filter(msgID -> !TextUtils.isEmpty(msgID)))
                .setTag("on_written_new_article")
                .onNextFinish(msgID -> {
                    if (getView() != null) {
                        new GMFDialog.Builder(getActivity())
                                .setMessage("发送成功,是否要分享给好友?")
                                .setPositiveButton("立即分享", (dialog, which) -> {
                                    dialog.dismiss();
                                    UmengUtil.stat_click_event(UmengUtil.eEVENTIDNewTopicShareConfirm);
                                    showActivity(this, an_CircleDetailPage(mMessageType, mLinkID, mSessionID, -1, msgID, true));
                                })
                                .setNegativeButton("取消", (dialog, which) -> {
                                    dialog.dismiss();
                                    UmengUtil.stat_click_event(UmengUtil.eEVENTIDNewTopicShareCancel);
                                })
                                .create().show();
                    }
                })
                .done();

        fetchData(true);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mVM = VM.EMPTY;
        mDataExpired = true;
    }

    private void fetchData(boolean reload) {
        if (reload || mVM == VM.EMPTY) {
            Observable<MResults.MResultsInfo<MessageManager>> observable = ChatController.getMessageManager(mMessageType, mLinkID, mSessionID)
                    .map(manager -> apply(manager, it -> it.isSuccess = it.isSuccess && it.data instanceof BarMessageManager && it.data.getSession() != null));
            consumeEventMRUpdateUI(observable, true)
                    .onNextSuccess(response -> {
                        BarMessageManager manager = (BarMessageManager) response.data;
                        resetContentView(manager);
                    })
                    .done();
        } else {
            ViewPager pager = v_findView(mContentSection, R.id.pager);
            if (pager.getAdapter() == null) {
                setSwipeRefreshing(false);
            } else {
                setSwipeRefreshing(true);
                int currentPageIdx = pager.getCurrentItem();
                mVM.fetchMessageList(this, currentPageIdx);
            }
        }
        mDataExpired = false;
    }

    private void resetContentView(BarMessageManager manager) {
        VM vm = new VM(manager);
        mVM = vm;
        resetToolbar(vm);
        setupTabLayoutAndViewPager(vm);

        MessageSession session = manager.getSession();
        FloatingActionButton addBtn = v_findView(this, R.id.btn_add);
        boolean isShowAddButton = anyMatch(session.addButtonType, Session_Add_Type_normal_2, Session_Add_Type_Intelligence);
        if (isShowAddButton) {
            v_setClick(addBtn, v -> {
                CircleHelper.goToWriteFragment(getActivity(), session);
            });
            v_setBehavior(addBtn, new FloatingActionButton.Behavior() {
                ViewConfiguration mConfiguration = ViewConfiguration.get(getActivity());

                @Override
                public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
                    return target instanceof SwipeRefreshLayout;
                }

                @Override
                public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
                    super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
                    int absDyConsumed = Math.abs(dyConsumed);
                    if (absDyConsumed > mConfiguration.getScaledTouchSlop()) {
                        if (dyConsumed > 0) {
                            ObjectAnimator.ofFloat(child, "translationY", child.getMeasuredHeight() + dp2px(20)).start();
                        } else {
                            ObjectAnimator.ofFloat(child, "translationY", 0F).start();
                        }
                    }
                }
            });
            v_setVisible(addBtn);
        } else {
            v_setGone(addBtn);
        }
    }

    private void resetToolbar(VM vm) {
        MessageSession session = vm.getMessageSession();
        updateTitle(vm.title);

        View notificationBtn = v_findView(this, R.id.btn_notification);
        ImageView notificationImage = v_findView(notificationBtn, R.id.img_notification);
        TextView notificationLabel = v_findView(notificationBtn, R.id.label_notification);
        if (session.number > 0) {
            notificationImage.setImageResource(0);
            notificationImage.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(RED_COLOR, dp2px(10))));
            notificationLabel.setText(String.valueOf(session.number));
        } else {
            notificationImage.setImageResource(R.mipmap.ic_circle_notification);
            notificationImage.setBackgroundColor(0);
            notificationLabel.setText("");
        }
        v_setClick(notificationBtn, v -> {
            mDataExpired = true;
            stat_click_event(UmengUtil.eEVENTIDTopicListHint);
            showActivity(this, an_CircleHintPage(session));
        });
    }

    private void setupTabLayoutAndViewPager(VM vm) {
        TabLayout tabLayout = v_findView(this, R.id.tabLayout);

        ViewPager pager = v_findView(this, R.id.pager);
        if (pager.getAdapter() == null) {
            pager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
                @Override
                public Fragment getItem(int position) {
                    return new CircleListPageFragment().init(position);
                }

                @Override
                public int getCount() {
                    return vm.pageTitles.size();
                }

                @Override
                public CharSequence getPageTitle(int position) {
                    return vm.pageTitles.get(position);
                }
            });
            tabLayout.setupWithViewPager(pager);
            v_setVisibility(tabLayout, vm.pageTitles.size() > 1 ? View.VISIBLE : View.GONE);
        }
    }

    private ViewPager getViewPager() {
        return v_findView(this, R.id.pager);
    }

    private static class VM {
        private static VM EMPTY = new VM(null);

        public String title;
        public List<String> pageTitles;

        private BarMessageManager raw;
        private SparseArray<CommandPageArray<GMFMessage>> pageArrayList = new SparseArray<>();

        public VM(BarMessageManager raw) {
            this.raw = raw;
            MessageSession session = safeGet(() -> raw.getSession(), null);
            title = safeGet(() -> session.title, "");
            pageTitles = Stream.of(safeGet(() -> session.headList, Collections.emptyList())).map(it -> it.title).collect(Collectors.toList());

        }

        public void fetchMessageList(CircleListFragment fragment, int index) {
            safeCall(() -> {

                fragment.setSwipeRefreshing(getMessageListOrNil(index) != null);
                fragment.mStartFetchMessageListSubject.onNext(index);

                fragment.consumeEvent(ChatController.refreshSessionList())
                        .setTag("refresh_session_list")
                        .onNextFinish(ignore -> {
                            if (pageArrayList.get(index) != null) {
                                Observable<MResults.MResultsInfo<CommandPageArray<GMFMessage>>> observable = getPreviousPage(pageArrayList.get(index))
                                        .map(response -> apply(response, it -> it.isSuccess = it.isSuccess && it.data != null));
                                fragment.consumeEventMR(observable)
                                        .setTag("fetchMessageListWithIndex:" + index)
                                        .onNextFinish(response -> {
                                            fragment.setSwipeRefreshing(false);
                                            fragment.mFinishFetchCircleListSubject.onNext(index);
                                        })
                                        .done();
                            } else {
                                Observable<MResults.MResultsInfo<CommandPageArray<GMFMessage>>> observable = CircleController.getCommandPageArray(raw, raw.getSession().headList.get(index))
                                        .map(response -> apply(response, it -> it.isSuccess = it.isSuccess && it.data != null));
                                fragment.consumeEventMR(observable)
                                        .setTag("fetchMessageListWithIndex:" + index)
                                        .onNextSuccess(response -> {
                                            pageArrayList.put(index, response.data);
                                        })
                                        .onNextFinish(response -> {
                                            fragment.setSwipeRefreshing(false);
                                            fragment.mFinishFetchCircleListSubject.onNext(index);
                                        })
                                        .done();
                            }
                        })
                        .done();

            });
        }

        public void fetchMoreMessageList(CircleListFragment fragment, int index) {
            if (pageArrayList.get(index) != null) {
                CommandPageArray<GMFMessage> pageArray = pageArrayList.get(index);
                Observable<MResults.MResultsInfo<CommandPageArray<GMFMessage>>> observable = getNextPage(pageArray)
                        .map(response -> apply(response, it -> it.isSuccess = it.isSuccess && it.data != null));
                fragment.consumeEventMR(observable)
                        .setTag("fetchMoreMessageListWithIndex:" + index)
                        .onNextFinish(response -> {
                            fragment.mFinishFetchCircleListSubject.onNext(index);
                        })
                        .done();
            }
        }

        public boolean isPageArrayHasMoreData(int index) {
            if (pageArrayList.get(index) != null) {
                CommandPageArray<GMFMessage> pageArray = pageArrayList.get(index);
                return hasMoreData(pageArray);
            }
            return false;
        }

        public List<GMFMessage> getMessageListOrNil(int index) {
            if (pageArrayList.get(index) != null) {
                return opt(pageArrayList.get(index).data()).or(Collections.emptyList());
            }
            return null;
        }

        public MessageSession getMessageSession() {
            return safeGet(() -> raw.getSession(), null);
        }

        public BarMessageManager getBarMessageManager() {
            return raw;
        }
    }

    public static CircleListFragment getParentFragment(Fragment fragment) {
        return safeGet(() -> (CircleListFragment) fragment.getParentFragment(), null);
    }

    public static PublishSubject<Integer> getStartFetchMessageListSubject(Fragment fragment) {
        return safeGet(() -> getParentFragment(fragment).mStartFetchMessageListSubject, null);
    }

    public static PublishSubject<Integer> getFinishFetchMessageListSubject(Fragment fragment) {
        return safeGet(() -> getParentFragment(fragment).mFinishFetchCircleListSubject, null);
    }

    public static BarMessageManager getBarMessageManagerSessionFromParent(Fragment fragment) {
        return safeGet(() -> {
            return getParentFragment(fragment).mVM.getBarMessageManager();
        }, null);
    }

    public static List<GMFMessage> getMessageListOrNilFromParent(Fragment fragment, int index) {
        return safeGet(() -> getParentFragment(fragment).mVM.getMessageListOrNil(index), null);
    }

    public static void requestFetchMessageListFromParent(Fragment fragment, int index) {
        safeCall(() -> {
            getParentFragment(fragment).mVM.fetchMessageList(getParentFragment(fragment), index);
        });
    }

    public static void requestFetchMoreMessageListFromParent(Fragment fragment, int index) {
        safeCall(() -> {
            CircleListFragment parentFragment = getParentFragment(fragment);
            parentFragment.mVM.fetchMoreMessageList(parentFragment, index);
        });
    }

    public static boolean getIsPageArrayHasMoreDataFromPage(Fragment fragment, int index) {
        return safeGet(() -> getParentFragment(fragment).mVM.isPageArrayHasMoreData(index), false);
    }

    public static int getSelectedPageIndex(Fragment fragment) {
        return safeGet(() -> getParentFragment(fragment).getViewPager().getCurrentItem(), -1);
    }

    public static class CircleListPageFragment extends SimpleFragment {
        private int mPageIdx;
        private boolean mDataExpired = true;
        private boolean mIsFetchingMore = false;
        private IntCounter counter = CircleHelper.createRewardCounter();

        @Override
        protected boolean isTraceLifeCycle() {
            return false;
        }

        @Override
        protected boolean isDelegateLifeCycleEventToSetUserVisible() {
            return false;
        }

        public CircleListPageFragment init(int pageIdx) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_TAB_IDX_INT, pageIdx);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mPageIdx = max(getArguments().getInt(KEY_TAB_IDX_INT, 0), 0);
            return inflater.inflate(R.layout.frag_circle_list_page, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            changeVisibleSection(TYPE_LOADING);

            opt(getStartFetchMessageListSubject(this))
                    .let(it -> it.filter(index -> index == mPageIdx))
                    .consume(subject -> {
                        consumeEvent(subject)
                                .onNextFinish(index -> {
                                    if (getUserVisibleHint() && getView() != null) {
                                        boolean hasData = v_isVisible(mContentSection);
                                        if (!hasData) {
                                            changeVisibleSection(TYPE_LOADING);
                                        }
                                    }
                                })
                                .done();
                    });
            opt(getFinishFetchMessageListSubject(this))
                    .let(it -> it.filter(index -> index == mPageIdx))
                    .consume(subject -> {
                        consumeEvent(subject)
                                .onNextFinish(index -> {
                                    mIsFetchingMore = false;
                                    mDataExpired = true;
                                    if (getUserVisibleHint() && getView() != null) {
                                        updateContentView();
                                        mDataExpired = false;
                                    }
                                })
                                .done();
                    });

            consumeEvent(Observable.merge(NotificationCenter.onWriteNewArticleSubject, NotificationCenter.onWriteNewCommentSubject, NotificationCenter.onMessageStateChangedSubject))
                    .setTag("on_message_list_changed")
                    .onNextFinish(ignored -> {
                        mDataExpired = true;
                        if (getUserVisibleHint() && getSelectedPageIndex(this) == mPageIdx) {
                            setUserVisibleHint(true);
                        }
                    })
                    .done();

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        boolean mHasCallOnResumed = false;

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mDataExpired = true;
            mIsFetchingMore = false;
            mHasCallOnResumed = false;
            counter.release();
        }

        @Override
        public void onPause() {
            super.onPause();
            if (getUserVisibleHint()) {
                setUserVisibleHint(false);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mHasCallOnResumed) {
                if (getSelectedPageIndex(this) == mPageIdx) {
                    setUserVisibleHint(true);
                }
            } else {
                mHasCallOnResumed = true;
            }
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (mDataExpired) {
                    requestFetchMessageListFromParent(this, mPageIdx);
                    mDataExpired = false;
                    mIsFetchingMore = false;
                } else {
                    boolean hasData = v_isVisible(mContentSection);
                    if (!hasData) {
                        updateContentView();
                    } else {
                        RecyclerView recyclerView = v_findView(this, R.id.recyclerView);
                        opt(recyclerView)
                                .let(it -> it.getAdapter())
                                .consume(it -> it.notifyDataSetChanged());
                    }
                }
            }
        }

        @SuppressWarnings("deprecation")
        private void updateContentView() {
            MessageSession session = getBarMessageManagerSessionFromParent(this).getSession();
            int headID = session.headList.get(mPageIdx).headID;
            List<GMFMessage> list = getMessageListOrNilFromParent(this, mPageIdx);

            if (list != null && !list.isEmpty()) {
                List<CircleHelper.CellVM> items = Stream.of(list)
                        .map(message -> new CircleHelper.CellVM(session, headID, message).optimize())
                        .collect(Collectors.toList());

                RecyclerView recyclerView = v_findView(this, R.id.recyclerView);
                if (recyclerView.getAdapter() != null) {
                    SimpleRecyclerViewAdapter<CircleHelper.CellVM> adapter = RecyclerViewExtension.getSimpleAdapter(recyclerView);
                    adapter.resetItems(items);
                } else {
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                    RecyclerViewExtension.addCellVerticalSpacing(recyclerView, dp2px(10));

                    SimpleRecyclerViewAdapter<CircleHelper.CellVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                            .onCreateItemView(R.layout.cell_circle_list)
                            .onCreateViewHolder(builder -> CircleHelper.createViewHolder(builder, CircleHelper.FLAG_SHOW_USER_TYPE))
                            .onViewHolderCreated((ad, holder) -> {
                                View itemView = holder.itemView;
                                Func0<CircleHelper.CellVM> itemGetter = () -> ad.getItem(holder.getAdapterPosition());
                                FrameLayout rateAnimViewContainer = (FrameLayout) this.mContentSection;
                                Func0<Boolean> ignoreAvatarClickEvent = () -> false;
                                Action0 rewardCallback = () -> {
                                };
                                CircleHelper.afterViewHolderCreated(itemView, itemGetter, counter, rateAnimViewContainer, ignoreAvatarClickEvent, rewardCallback);
                            })
                            .create();
                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            if (!mIsFetchingMore && isScrollToBottom(recyclerView) && getIsPageArrayHasMoreDataFromPage(CircleListPageFragment.this, mPageIdx)) {
                                requestFetchMoreMessageListFromParent(CircleListPageFragment.this, mPageIdx);
                                mIsFetchingMore = true;
                            }
                        }
                    });
                    recyclerView.setAdapter(adapter);
                }

                changeVisibleSection(TYPE_CONTENT);
            } else {
                changeVisibleSection(TYPE_EMPTY);
            }
        }
    }
}
