package com.yale.ui.framelayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by yale on 16/6/12.
 */
public class InterceptableFrameLayout extends FrameLayout {
    private OnInterceptTouchEventDelegate mDelegate = OnInterceptTouchEventDelegate.NULL;

    public InterceptableFrameLayout(Context context) {
        super(context);
    }

    public InterceptableFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptableFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Boolean shouldIntercept = mDelegate.onInterceptTouchEvent(ev);

        return shouldIntercept != null ? shouldIntercept : super.onInterceptTouchEvent(ev);
    }

    public void setOnInterceptTouchEventDelegate(OnInterceptTouchEventDelegate delegate) {
        this.mDelegate = delegate == null ? OnInterceptTouchEventDelegate.NULL : delegate;
    }

    public interface OnInterceptTouchEventDelegate {
        Boolean onInterceptTouchEvent(MotionEvent ev);

        OnInterceptTouchEventDelegate NULL = new OnInterceptTouchEventDelegate() {
            @Override
            public Boolean onInterceptTouchEvent(MotionEvent ev) {
                return null;
            }
        };
    }
}
