package com.goldmf.GMFund.widget;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.goldmf.GMFund.base.KeepClassProtocol;

/**
 * Created by yale on 16/2/20.
 */
public class AppBarLayoutBehavior extends AppBarLayout.Behavior implements KeepClassProtocol {
    private static final int TOP_CHILD_FLING_THRESHOLD = 3;
    private boolean isScrollingDown;

    public AppBarLayoutBehavior() {
        super();
    }

    public AppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY, boolean consumed) {
        if (velocityY > 0 && !isScrollingDown || velocityY < 0 && isScrollingDown) {
            velocityY = velocityY * -1;
        }

        if (target instanceof RecyclerView && velocityY < 0) {
            final RecyclerView recyclerView = (RecyclerView) target;
            final View firstChild = recyclerView.getChildAt(0);
            final int childPos = recyclerView.getChildAdapterPosition(firstChild);
            consumed = childPos > TOP_CHILD_FLING_THRESHOLD;
        }
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        isScrollingDown = dy > 0;
    }
}
