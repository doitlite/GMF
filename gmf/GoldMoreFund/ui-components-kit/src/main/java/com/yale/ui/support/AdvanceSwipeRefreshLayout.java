package com.yale.ui.support;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.Scroller;

import java.util.logging.Logger;

/**
 * Created by yale on 16/1/12.
 */
public class AdvanceSwipeRefreshLayout extends SwipeRefreshLayout {
    private ViewConfiguration mConfiguration;
    private OnPreInterceptTouchEventDelegate mOnPreInterceptTouchEventDelegate;
    private OnInterceptTouchEventDelegate mOnInterceptTouchEventDelegate;


    public AdvanceSwipeRefreshLayout(Context context) {
        super(context);
        mConfiguration = ViewConfiguration.get(context);
    }

    public AdvanceSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mConfiguration = ViewConfiguration.get(context);
    }

    private float mPrevX;
    private boolean[] shouldIntercept = new boolean[1];

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean disallowIntercept = false;
        if (mOnPreInterceptTouchEventDelegate != null)
            disallowIntercept = mOnPreInterceptTouchEventDelegate.shouldDisallowInterceptTouchEvent(ev);

        if (disallowIntercept) {
            return false;
        }

        if (mOnInterceptTouchEventDelegate != null) {
            boolean isConsumed = mOnInterceptTouchEventDelegate.onInterceptTouchEvent(ev, shouldIntercept);
            if (isConsumed) {
                return shouldIntercept[0];
            }
        }

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = ev.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(ev.getRawX() - mPrevX);
                if (dx >= mConfiguration.getScaledTouchSlop()) {
                    return false;
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    public void setOnPreInterceptTouchEventDelegate(OnPreInterceptTouchEventDelegate listener) {
        mOnPreInterceptTouchEventDelegate = listener;
    }

    public void setOnInterceptTouchEventDelegate(OnInterceptTouchEventDelegate onInterceptTouchEventDelegate) {
        mOnInterceptTouchEventDelegate = onInterceptTouchEventDelegate;
    }

    public interface OnPreInterceptTouchEventDelegate {
        boolean shouldDisallowInterceptTouchEvent(MotionEvent ev);
    }

    public interface OnInterceptTouchEventDelegate {
        boolean onInterceptTouchEvent(MotionEvent event, boolean[] shouldIntercept);
    }
}
