package com.goldmf.GMFund.controller;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.View;

import com.annimon.stream.Stream;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.extension.FlagExtension;

import java.lang.ref.WeakReference;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import yale.extension.common.function.SafeAction0;
import yale.extension.common.function.SafeAction1;
import yale.extension.rx.ConsumeEventChainMR;
import yale.extension.rx.ConsumeEventChainMRList;

import static com.goldmf.GMFund.extension.FlagExtension.hasFlag;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.traceCostTime;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.v_setOnRefreshing;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;

/**
 * Created by yale on 15/10/23.
 */
public class SimpleFragment extends BaseFragment {
    public static final int TYPE_LOADING = 1;
    public static final int TYPE_RELOAD = 1 << 1;
    public static final int TYPE_CONTENT = 1 << 2;
    public static final int TYPE_EMPTY = 1 << 3;


    public static final int FLAG_DISABLE_FORCE_SHOW_SWIPE_REFRESH_LAYOUT = 1;
    public static final int FLAG_NO_ERROR_TOAST = 1 << 1;

    protected View mLoadingSection;
    protected View mReloadSection;
    protected View mContentSection;
    protected View mEmptySection;
    protected SwipeRefreshLayout mRefreshLayout;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefreshLayout = safeFindView(view, SwipeRefreshLayout.class, getRefreshLayoutID(), getContentSectionID());
        mLoadingSection = safeFindView(view, View.class, getLoadingSectionID());
        mReloadSection = safeFindView(view, View.class, getReloadSectionID());
        mContentSection = safeFindView(view, View.class, getContentSectionID());
        mEmptySection = safeFindView(view, View.class, getEmptySectionID());
    }

    public void setEmptySectionTips(CharSequence title, CharSequence subTitle) {
        if (mEmptySection != null) {
            v_setText(mEmptySection, R.id.label_title, title);
            v_setText(mEmptySection, R.id.label_subtitle, subTitle);
        }
    }

    public void setLoadingSectionTips(CharSequence text) {
        if (mLoadingSection != null) {
            v_setText(mLoadingSection, R.id.label_title, text);
        }
    }

    public void setReloadSectionTips(CharSequence text) {
        if (mReloadSection != null) {
            v_setText(mReloadSection, R.id.label_title, text);
        }
    }

    public void setSwipeRefreshable(boolean isRefreshable) {
        if (mRefreshLayout != null) {
            mRefreshLayout.setEnabled(isRefreshable);
            mRefreshLayout.requestDisallowInterceptTouchEvent(!isRefreshable);
        }

    }

    public void setOnSwipeRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        if (listener != null && mRefreshLayout != null) {
            mRefreshLayout.setEnabled(true);
            v_setOnRefreshing(mRefreshLayout, listener);
        }
    }

    public void setOnSwipeRefreshListener(Action1<SwipeRefreshLayout> listener) {
        if (listener != null && mRefreshLayout != null)
            mRefreshLayout.setEnabled(true);
        v_setOnRefreshing(mRefreshLayout, listener);
    }

    public void setSwipeRefreshing(boolean value) {
        if (mRefreshLayout != null)
            mRefreshLayout.setRefreshing(value);
    }

    protected int getRefreshLayoutID() {
        return R.id.refreshLayout;
    }

    protected int getLoadingSectionID() {
        return R.id.section_loading;
    }

    protected int getReloadSectionID() {
        return R.id.section_reload;
    }

    protected int getContentSectionID() {
        return R.id.section_content;
    }

    protected int getEmptySectionID() {
        return R.id.section_empty;
    }

    public void changeVisibleSection(int type) {
        View[] sections = {mLoadingSection, mReloadSection, mContentSection, mEmptySection};
        Integer[] types = {TYPE_LOADING, TYPE_RELOAD, TYPE_CONTENT, TYPE_EMPTY};
        Stream.zip(Stream.of(sections), Stream.of(types), (first, second) -> Pair.create(first, second))
                .filter(it -> it.first != null)
                .forEach(it -> {
                    int relatedType = it.second;

                    boolean isVisible = (relatedType & type) == relatedType;
                    it.first.setVisibility(isVisible ? View.VISIBLE : View.GONE);
                });
    }

    public <T extends MResultsInfo> ConsumeEventChainMR<T> consumeEventMRUpdateUI(Observable<T> observable, boolean isReload) {
        return consumeEventMRUpdateUI(observable, isReload, 0);
    }

    public <T extends MResultsInfo> ConsumeEventChainMR<T> consumeEventMRUpdateUI(Observable<T> observable, boolean isReload, int flags) {
        return consumeEventMRUpdateUI(new WeakReference<>(this), observable, isReload, flags);
    }

    private static <T extends MResultsInfo> ConsumeEventChainMR<T> consumeEventMRUpdateUI(WeakReference<SimpleFragment> vcRef, Observable<T> observable, boolean isReload, int flags) {
        return new ConsumeEventChainMR<T>(vcRef.get()) {

            @Override
            public SafeAction0 getOnConsumed() {
                SafeAction0 operation = super.getOnConsumed();
                return () -> {
                    if (vcRef.get() != null) {
                        SimpleFragment vc = vcRef.get();
                        if (isReload) {
                            vc.changeVisibleSection(TYPE_LOADING);
                        } else {
                            boolean isDisableForceShowSwipeRefreshLayout = (flags & FLAG_DISABLE_FORCE_SHOW_SWIPE_REFRESH_LAYOUT) != 0;
                            if (!isDisableForceShowSwipeRefreshLayout) {
                                vc.setSwipeRefreshing(true);
                            }
                        }
                    }
                    if (operation != null) {
                        safeCall(() -> operation.call());
                    }
                };

            }

            @Override
            public SafeAction1<T> getOnNextSuccess() {
                SafeAction1<T> operation = super.getOnNextSuccess();
                return response -> {
                    if (vcRef.get() != null) {
                        SimpleFragment vc = vcRef.get();
                        vc.changeVisibleSection(TYPE_CONTENT);
                    }
                    if (operation != null) {
                        safeCall(() -> operation.call(response));
                    }
                };
            }

            @Override
            public SafeAction1<T> getOnNextFail() {
                SafeAction1<T> operation = super.getOnNextFail();
                return response -> {
                    if (vcRef.get() != null) {
                        SimpleFragment vc = vcRef.get();
                        if (isReload) {
                            vc.setReloadSectionTips(getErrorMessage(response));
                            vc.changeVisibleSection(TYPE_RELOAD);
                        } else {
                            vc.changeVisibleSection(TYPE_CONTENT);
                            showToast(vc, getErrorMessage(response));
                        }
                    }
                    if (operation != null) {
                        safeCall(() -> operation.call(response));
                    }
                };
            }

            @Override
            public SafeAction1<T> getOnNextFinish() {
                SafeAction1<T> operation = super.getOnNextFinish();
                return response -> {
                    if (vcRef.get() != null) {
                        SimpleFragment vc = vcRef.get();
                        vc.setSwipeRefreshing(false);
                    }
                    if (operation != null) {
                        safeCall(() -> operation.call(response));
                    }
                };
            }
        }.setObservable(observable);
    }

    public <T extends List<MResultsInfo>> ConsumeEventChainMRList<T> consumeEventMRListUpdateUI(Observable<T> observable, boolean isReload) {
        return consumeEventMRListUpdateUI(new WeakReference<>(this), observable, isReload, 0);
    }

    public <T extends List<MResultsInfo>> ConsumeEventChainMRList<T> consumeEventMRListUpdateUI(Observable<T> observable, boolean isReload, int flags) {
        return consumeEventMRListUpdateUI(new WeakReference<>(this), observable, isReload, flags);
    }

    private static <T extends List<MResultsInfo>> ConsumeEventChainMRList<T> consumeEventMRListUpdateUI(WeakReference<SimpleFragment> vcRef, Observable<T> observable, boolean isReload, int flags) {
        return new ConsumeEventChainMRList<T>(vcRef.get()) {

            @Override
            public SafeAction0 getOnConsumed() {
                SafeAction0 operation = super.getOnConsumed();
                return () -> {
                    if (vcRef.get() != null) {
                        SimpleFragment vc = vcRef.get();
                        if (isReload) {
                            vc.changeVisibleSection(TYPE_LOADING);
                        } else {
                            boolean isDisableForceShowSwipeRefreshLayout = (flags & FLAG_DISABLE_FORCE_SHOW_SWIPE_REFRESH_LAYOUT) != 0;
                            if (!isDisableForceShowSwipeRefreshLayout) {
                                vc.setSwipeRefreshing(true);
                            }
                        }
                    }
                    if (operation != null) {
                        safeCall(() -> operation.call());
                    }
                };

            }

            @Override
            public SafeAction1<T> getOnNextSuccess() {
                SafeAction1<T> operation = super.getOnNextSuccess();
                return response -> {
                    if (vcRef.get() != null) {
                        SimpleFragment vc = vcRef.get();
                        vc.changeVisibleSection(TYPE_CONTENT);
                    }
                    if (operation != null) {
                        safeCall(() -> operation.call(response));
                    }
                };
            }

            @Override
            public SafeAction1<T> getOnNextFail() {
                SafeAction1<T> operation = super.getOnNextFail();
                return response -> {
                    if (vcRef.get() != null) {
                        SimpleFragment vc = vcRef.get();
                        if (isReload) {
                            vc.setReloadSectionTips(getErrorMessage(response));
                            vc.changeVisibleSection(TYPE_RELOAD);
                        } else {
                            vc.changeVisibleSection(TYPE_CONTENT);
                            boolean noErrorToast = hasFlag(flags, FLAG_NO_ERROR_TOAST);
                            if (!noErrorToast) {
                                showToast(vc, getErrorMessage(response));
                            }
                        }
                    }
                    if (operation != null) {
                        safeCall(() -> operation.call(response));
                    }
                };
            }

            @Override
            public SafeAction1<T> getOnNextFinish() {
                SafeAction1<T> operation = super.getOnNextFinish();
                return response -> {
                    if (vcRef.get() != null) {
                        SimpleFragment vc = vcRef.get();
                        vc.setSwipeRefreshing(false);
                    }
                    if (operation != null) {
                        safeCall(() -> operation.call(response));
                    }
                };
            }
        }.setObservable(observable);
    }

    @SuppressWarnings("unchecked")
    private static <T extends View> T safeFindView(View view, Class<T> clazz, int... candidates) {
        if (view == null) return null;

        for (int viewID : candidates) {
            View child = view.findViewById(viewID);
            if (child != null && clazz.isInstance(child)) {
                return (T) child;
            }
        }

        return null;
    }
}
