package com.yale.ui.scrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import rx.functions.Action2;


/**
 * Created by yale on 15/9/1.
 */
public class ObservableScrollView extends ScrollView {
    private Action2<Integer, Integer> mOnScrollChangedListener = null;

    public ObservableScrollView(Context context) {
        this(context, null);
    }

    public ObservableScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ObservableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedListener != null)
            mOnScrollChangedListener.call(l, t);
    }

    public void setOnScrollChangedListener(Action2<Integer, Integer> listener) {
        mOnScrollChangedListener = listener;
    }
}
