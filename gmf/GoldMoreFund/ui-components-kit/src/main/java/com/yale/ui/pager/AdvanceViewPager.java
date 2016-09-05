package com.yale.ui.pager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by yale on 15/9/17.
 */
public class AdvanceViewPager extends ViewPager {
    private OnPreInterceptTouchEventDelegate mOnPreInterceptTouchEventDelegate;

    public AdvanceViewPager(Context context) {
        super(context);
    }

    public AdvanceViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Boolean disallowIntercept = null;

        if (mOnPreInterceptTouchEventDelegate != null)
            disallowIntercept = mOnPreInterceptTouchEventDelegate.shouldDisallowInterceptTouchEvent(ev);

        if (disallowIntercept != null) {
            return !disallowIntercept;
        }

        try {
            return super.onInterceptTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }

    public void setOnPreInterceptTouchEventDelegate(OnPreInterceptTouchEventDelegate listener) {
        mOnPreInterceptTouchEventDelegate = listener;
    }


    public interface OnPreInterceptTouchEventDelegate {
        Boolean shouldDisallowInterceptTouchEvent(MotionEvent ev);
    }

}
