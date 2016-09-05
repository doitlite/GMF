package com.goldmf.GMFund.widget;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import com.goldmf.GMFund.base.KeepClassProtocol;

/**
 * Created by yale on 16/2/20.
 */
public class AppBarLayoutBehavior2 extends AppBarLayout.Behavior implements KeepClassProtocol {
    private static final int MARGIN_TOP = 16;
    private boolean isScrollingDown;

    public AppBarLayoutBehavior2() {
        super();
    }

    public AppBarLayoutBehavior2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY, boolean consumed) {
        if (velocityY > 0 && !isScrollingDown || velocityY < 0 && isScrollingDown) {
            velocityY = velocityY * -1;
        }

        if (target instanceof ScrollView && velocityY < 0) {
            final ScrollView scrollView = (ScrollView) target;
            final View firstChild = scrollView.getChildAt(0);
            consumed = firstChild.getTop() < MARGIN_TOP;
        }
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        isScrollingDown = dy > 0;
    }
}
