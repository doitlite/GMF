package com.goldmf.GMFund.widget;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.goldmf.GMFund.base.KeepClassProtocol;

/**
 * Created by yale on 16/3/3.
 */
public class DynamicHeightViewBehavior extends AppBarLayout.ScrollingViewBehavior implements KeepClassProtocol {

    public DynamicHeightViewBehavior() {
    }

    public DynamicHeightViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        ViewGroup.LayoutParams params = child.getLayoutParams();
        params.height = parent.getMeasuredHeight() - dependency.getBottom();
        child.setLayoutParams(params);
        child.requestLayout();
        return super.onDependentViewChanged(parent, child, dependency);
    }
}
