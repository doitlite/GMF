package com.goldmf.GMFund.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.OverScroller;
import android.widget.ScrollView;

import rx.functions.Action0;
import rx.functions.Action2;

/**
 * Created by Evan on 16/4/29 下午4:21.
 */
public class ObservableScrollView extends ScrollView {
    private Action2<Integer, Integer> mOnScrollChangedListener = null;
    private Action0 mOnScrollStopListener = null;
    private OverScroller mScroller;

    public ObservableScrollView(Context context) {
        this(context, null);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //        init(context);
    }

    //    private void init(Context context) {
    //        mScroller = new OverScroller(context);
    //        ReflectionUtils.setField(this, "android.widget.ScrollView", "mScroller", mScroller);
    //    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedListener != null)
            mOnScrollChangedListener.call(l, t);
    }

    public void setOnScrollChangedListener(Action2<Integer, Integer> listener) {
        mOnScrollChangedListener = listener;
    }

    //    public void setOnScrollStopListener(Action0 listener) {
    //        mOnScrollStopListener = listener;
    //    }
    //
    //    public void checkScrollStop() {
    //        boolean mIsBeingDragged = (Boolean) ReflectionUtils.getField(this, "android.widget.ScrollView", "mIsBeingDragged");
    //        if (mScroller.isFinished() && !mIsBeingDragged) {
    //            if (mOnScrollStopListener != null)
    //                mOnScrollStopListener.call();
    //        }
    //    }
    //
    //    @Override
    //    public void computeScroll() {
    //        super.computeScroll();
    //        checkScrollStop();
    //    }
}
