package com.goldmf.GMFund.extension;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import rx.functions.Action2;

/**
 * Created by yale on 16/3/22.
 */
public class ViewGroupExtension {
    public static void v_forEach(Activity activity, int viewId, Action2<Integer, View> operation) {
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(viewId);
        v_forEach(viewGroup, operation);
    }

    public static void v_forEach(Fragment fragment, int viewId, Action2<Integer, View> operation) {
        ViewGroup viewGroup = (ViewGroup) fragment.getView().findViewById(viewId);
        v_forEach(viewGroup, operation);
    }

    public static void v_forEach(View view, int viewId, Action2<Integer, View> operation) {
        ViewGroup viewGroup = (ViewGroup) view.findViewById(viewId);
        v_forEach(viewGroup, operation);
    }

    public static void v_forEach(ViewGroup view, Action2<Integer, View> operation) {
        int count = view.getChildCount();
        for (int i = 0; i < count; i++) {
            operation.call(i, view.getChildAt(i));
        }
    }

    public static int v_getChildIndex(View parent, View child) {
        if (parent != null && child != null && parent instanceof ViewGroup) {
            ViewGroup cast = (ViewGroup) parent;
            int childCount = cast.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (cast.getChildAt(i) == child) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static boolean v_hasChild(View parent, @IdRes int viewId) {
        View view = parent.findViewById(viewId);
        if (view != null && view instanceof ViewGroup) {
            return ((ViewGroup) view).getChildCount() > 0;
        }

        return false;
    }
}
