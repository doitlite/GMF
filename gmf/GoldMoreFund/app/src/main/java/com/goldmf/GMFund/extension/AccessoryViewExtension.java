package com.goldmf.GMFund.extension;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;

import com.goldmf.GMFund.BuildConfig;
import com.orhanobut.logger.Logger;

import rx.functions.Action1;

import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;

/**
 * Created by yale on 16/4/22.
 */
@SuppressWarnings("deprecation")
public class AccessoryViewExtension {
    private AccessoryViewExtension() {
    }

    public static OnGlobalLayoutListener registKeyboardChangedListener(OnGlobalLayoutListener oldListener, Fragment fragment, Action1<Boolean> onKeyboardVisibilityChanged) {
        return registKeyboardChangedListener(oldListener, fragment.getView(), onKeyboardVisibilityChanged);
    }

    public static OnGlobalLayoutListener registKeyboardChangedListener(OnGlobalLayoutListener oldListener, View rootView, Action1<Boolean> onKeyboardVisibilityChangedCallback) {
        if (oldListener != null) {
            rootView.getViewTreeObserver().removeGlobalOnLayoutListener(oldListener);
        }
        OnGlobalLayoutListener onLayoutListener = new OnGlobalLayoutListener() {
            Boolean mPreIsVisible = null;   // -1没显示过; 0为隐藏; 1为可见;

            @Override
            public void onGlobalLayout() {
                View parent = rootView.getRootView();
                int rootDiff = parent.getHeight() - rootView.getHeight();
                boolean isKeyboardShow = rootDiff > dp2px(180);
                if (mPreIsVisible == null || (isKeyboardShow != mPreIsVisible)) {
                    safeCall(() -> onKeyboardVisibilityChangedCallback.call(isKeyboardShow));
                }
                mPreIsVisible = isKeyboardShow;
            }
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(onLayoutListener);


        int rootDiff = rootView.getRootView().getHeight() - rootView.getHeight();
        boolean isKeyboardShow = rootDiff > 150;
        safeCall(() -> onKeyboardVisibilityChangedCallback.call(isKeyboardShow));
        return onLayoutListener;
    }

    public static boolean isKeyboardVisible(Fragment fragment) {
        return isKeyboardVisible(fragment.getView());
    }

    public static boolean isKeyboardVisible(View rootView) {
        int rootDiff = rootView.getRootView().getHeight() - rootView.getHeight();
        return rootDiff > 150;
    }

    public static void unregistKeyboardChangedListener(OnGlobalLayoutListener oldListener, Activity activity) {
        unregistKeyboardChangedListener(oldListener, activity.getWindow());
    }

    public static void unregistKeyboardChangedListener(OnGlobalLayoutListener oldListener, Fragment fragment) {
        unregistKeyboardChangedListener(oldListener, safeGet(() -> fragment.getActivity().getWindow(), null));
    }

    public static void unregistKeyboardChangedListener(OnGlobalLayoutListener oldListener, Window window) {
        if (window == null) {
            return;
        }

        safeCall(() -> {
            View rootView = ViewGroup.class.cast(window.getDecorView()).getChildAt(0);
            rootView.getViewTreeObserver().removeGlobalOnLayoutListener(oldListener);
        });
    }
}
