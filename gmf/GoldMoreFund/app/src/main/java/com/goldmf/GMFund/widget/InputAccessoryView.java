package com.goldmf.GMFund.widget;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by yale on 16/2/25.
 */
public class InputAccessoryView extends FrameLayout {
    public InputAccessoryView(Context context) {
        this(context, null);
    }

    public InputAccessoryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InputAccessoryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(0xFF7B7B7B);
        LayoutTransition transition = new LayoutTransition();
        transition.setDuration(150L);
        setLayoutTransition(transition);
    }

    public void resetChildren(View newChild) {
        removeAllViewsInLayout();
        if (newChild != null) {
            addView(newChild);
        }
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @TargetApi(value = Build.VERSION_CODES.KITKAT)
        public LayoutParams(FrameLayout.LayoutParams source) {
            super(source);
        }
    }
}
