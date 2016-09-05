package com.goldmf.GMFund.widget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import com.yale.ui.support.AdvanceSwipeRefreshLayout;

/**
 * Created by yale on 16/3/5.
 */
public class AdvanceNestedScrollView extends NestedScrollView {
    private OnPreInterceptTouchEventDelegate mOnPreInterceptTouchEventDelegate;
    private Scroller mOffsetTopAndBottomScroller;

    public AdvanceNestedScrollView(Context context) {
        this(context, null);
    }

    public AdvanceNestedScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdvanceNestedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mOffsetTopAndBottomScroller = new Scroller(context);
        mOffsetTopAndBottomScroller.setFriction(ViewConfiguration.getScrollFriction() * 5);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean disallowIntercept = false;
        if (mOnPreInterceptTouchEventDelegate != null)
            disallowIntercept = mOnPreInterceptTouchEventDelegate.shouldDisallowInterceptTouchEvent(ev);

        if (disallowIntercept) {
            return false;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mOffsetTopAndBottomScroller.computeScrollOffset()) {
//            ViewCompat.offsetTopAndBottom(this, mOffsetTopAndBottomScroller.getCurrY() - getTop());
            setTranslationY(mOffsetTopAndBottomScroller.getCurrY());
            invalidate();
        }
    }

    public Scroller getOffsetTopAndBottomScroller() {
        return mOffsetTopAndBottomScroller;
    }

    public void setOnPreInterceptTouchEventDelegate(OnPreInterceptTouchEventDelegate listener) {
        mOnPreInterceptTouchEventDelegate = listener;
    }

    public interface OnPreInterceptTouchEventDelegate {
        boolean shouldDisallowInterceptTouchEvent(MotionEvent ev);
    }
}
