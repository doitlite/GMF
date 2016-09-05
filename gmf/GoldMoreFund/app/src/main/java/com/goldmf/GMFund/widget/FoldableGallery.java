package com.goldmf.GMFund.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.extension.ViewExtension;
import com.goldmf.GMFund.extension.ViewGroupExtension;
import com.orhanobut.logger.Logger;

import java.util.Arrays;
import java.util.Comparator;

import static com.goldmf.GMFund.extension.ViewGroupExtension.v_forEach;
import static java.lang.Math.*;

/**
 * Created by yale on 16/2/23.
 */
public class FoldableGallery extends ViewGroup {
    private int mChildMargin = 0;
    private ViewConfiguration mConfiguration;
    private Scroller mScroller;
    private float mMinScrollX = 0;
    private float mMaxScrollX = 0;
    private ViewTransformer mTransformer = ViewTransformer.EXPANDED;
    private int mShortAnimTime;
    private boolean mIgnoreAllTouchEvent = false;
    private boolean mExpand = true;

    public FoldableGallery(Context context) {
        this(context, null);
    }

    public FoldableGallery(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FoldableGallery(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mConfiguration = ViewConfiguration.get(context);
        mScroller = new Scroller(context);
        mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        setChildrenDrawingOrderEnabled(true);
    }

    public boolean isExpand() {
        return mExpand;
    }

    private boolean mIsScrollingBack = false;

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            int currX = mScroller.getCurrX();
            int currY = mScroller.getCurrY();

            scrollTo(currX, currY);
            invalidate();

            if (!mIsScrollingBack) {
                if (currX < mMinScrollX || currX > mMaxScrollX) {
                    mScroller.abortAnimation();
                    mIsScrollingBack = true;
                }

                if (currX < mMinScrollX) {
                    float dx = getScrollX() - mMinScrollX;
                    mScroller.startScroll(getScrollX(), getScrollY(), (int) -dx, 0, mShortAnimTime);
                    invalidate();
                } else if (currX > mMaxScrollX) {
                    int dx = (int) (getScrollX() - mMaxScrollX);
                    mScroller.startScroll(getScrollX(), getScrollY(), -dx, 0, mShortAnimTime);
                    invalidate();
                }
            }
        } else {
            mIsScrollingBack = false;
        }
    }


    private PointF mCenterPoint = new PointF();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        measureScrollRange();
        setPivotX(getMeasuredWidth() >> 1);
        setPivotY(0);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        measureCenterPoint(mCenterPoint);
        int offsetX = left;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.layout(offsetX, top, offsetX + child.getMeasuredWidth(), top + child.getMeasuredHeight());
            offsetX += mChildMargin + child.getMeasuredWidth();
            mTransformer.onTransform(this, child, mCenterPoint);
        }
//        mPrevCenterChildIdx = computeNearestChildIndex(null);
        scrollToChildWithIndex(mPrevCenterChildIdx, false);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        measureCenterPoint(mCenterPoint);

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            mTransformer.onTransform(this, child, mCenterPoint);
        }
    }

    private Integer[] mDrawingOrderArray = null;

    @Override
    protected int getChildDrawingOrder(int childCount, int pos) {
        if (mDrawingOrderArray == null || mDrawingOrderArray.length != childCount)
            mDrawingOrderArray = computeDrawingOrderArray();

        return mDrawingOrderArray[pos];
    }

    private Integer[] computeDrawingOrderArray() {
        int childCount = getChildCount();
        Integer[] indexes = new Integer[childCount];
        for (int i = 0; i < childCount; i++) {
            indexes[i] = i;
        }
        Arrays.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                int lDis = lhs - mPrevCenterChildIdx;
                int rDis = rhs - mPrevCenterChildIdx;
                if (abs(lDis) > abs(rDis)) {
                    return -1;
                } else if (abs(lDis) < abs(rDis)) {
                    return 1;
                } else {
                    if (lDis < 0) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
        });
        return indexes;
    }

    public void setChildMargin(int childMargin) {
        if (mChildMargin != childMargin) {
            mChildMargin = childMargin;
            mPrevCenterChildIdx = computeNearestChildIndex(null);
            mDrawingOrderArray = computeDrawingOrderArray();
            requestLayout();
            invalidate();
        }
    }

    public int getChildMargin() {
        return mChildMargin;
    }

    public void expand(boolean animate) {

        if (animate) {
            ObjectAnimator animator1 = ObjectAnimator.ofInt(this, "childMargin", 0);
            animator1.addListener(new AnimatorListenerAdapter() {
                private void updateValue() {
                    int childCount = getChildCount();
                    if (childCount > 3) {
                        for (int i = 3; i < childCount; i++) {
                            getChildAt(3).setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    updateValue();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    updateValue();
                }
            });
            Animator animator2 = newScaleAnimator(this, 1.0f, 1.0f);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(animator1, animator2);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mIgnoreAllTouchEvent = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mIgnoreAllTouchEvent = false;
                }
            });
            set.start();
        } else {
            setChildMargin(0);
            int childCount = getChildCount();
            if (childCount > 3) {
                for (int i = 3; i < childCount; i++) {
                    getChildAt(3).setVisibility(View.VISIBLE);
                }
            }
            setScaleX(1.0f);
            setScaleY(1.0f);
            mIgnoreAllTouchEvent = false;
        }
        mExpand = true;
    }

    private boolean isCollapsing = false;

    public void collapse(boolean animate) {
        if (getChildCount() == 0) {
            return;
        }

        if (isCollapsing) return;

        isCollapsing = true;
        mIgnoreAllTouchEvent = true;
        mPrevCenterChildIdx = max(min(1, getChildCount() - 1), 0);
        int destScrollX = computeScrollXWhenScrollToChild(mPrevCenterChildIdx);

        v_forEach(this, (pos, child) -> child.setScrollY(0));
        if (animate) {
            ObjectAnimator scrollAnimator = ObjectAnimator.ofInt(this, "scrollX", destScrollX).setDuration(mShortAnimTime);
            scrollAnimator.addListener(new AnimatorListenerAdapter() {
                private void updateValue() {
                    int childCount = getChildCount();
                    if (childCount > 3) {
                        for (int i = 3; i < childCount; i++) {
                            getChildAt(i).setVisibility(View.INVISIBLE);
                        }
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    updateValue();
                    isCollapsing = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    updateValue();
                    isCollapsing = false;
                }
            });

            int childMargin = getChildAt(0).getMeasuredWidth() / 5 * 4;
            ObjectAnimator childMarginAnimator = ObjectAnimator.ofInt(this, "childMargin", -childMargin).setDuration(mShortAnimTime);
            Animator extraScaleAnimator = newScaleAnimator(this, 0.7f, 0.7f);

            AnimatorSet set0 = new AnimatorSet();
            set0.playTogether(childMarginAnimator, extraScaleAnimator);

            AnimatorSet set = new AnimatorSet();
            set.playSequentially(scrollAnimator, set0);
            set.start();
        } else {
            setScrollX(destScrollX);
            int childCount = getChildCount();
            if (childCount > 3) {
                for (int i = 3; i < childCount; i++) {
                    getChildAt(i).setVisibility(View.INVISIBLE);
                }
            }
            View firstChild = getChildAt(0);
            if (firstChild.getMeasuredWidth() == 0) {
                measureChildren(getMeasuredWidthAndState(), getMeasuredHeightAndState());
            }
            int childMargin = firstChild.getMeasuredWidth() / 5 * 4;
            setChildMargin(-childMargin);
            setScaleX(0.7F);
            setScaleY(0.7F);
            mDrawingOrderArray = null;
            requestLayout();
            invalidate();
            isCollapsing = false;
        }
        mExpand = false;
    }

    private AnimatorSet newScaleAnimator(View view, float destScaleX, float destScaleY) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", destScaleX), ObjectAnimator.ofFloat(view, "scaleY", destScaleY));
        return set;
    }

    private void measureCenterPoint(PointF centerPoint) {
        centerPoint.set(getScrollX() + (getMeasuredWidth() >> 1), getMeasuredHeight() >> 1);
    }

    private void measureScrollRange() {
        if (getChildCount() <= 0) return;

        int maxScrollX = -mChildMargin;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            maxScrollX += getChildAt(i).getMeasuredWidth() + mChildMargin;
        }
        maxScrollX -= getMeasuredWidth();
        maxScrollX += (getMeasuredWidth() >> 1) - (getChildAt(childCount - 1).getMeasuredWidth() >> 1);
        mMaxScrollX = maxScrollX;
        mMinScrollX -= getMeasuredWidth() >> 1;
    }


    private int mPrevCenterChildIdx = 0;

    private int computeNearestChildIndex(VelocityTracker tracker) {

        int childCount = getChildCount();
        int nearestChildIdx = childCount - 1;
        float prevDx = Integer.MAX_VALUE;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int childCenterX = child.getLeft() + (child.getMeasuredWidth() >> 1);
            float dx = abs(mCenterPoint.x - childCenterX);
            if (dx > prevDx) {
                nearestChildIdx = i - 1;
                break;
            }
            prevDx = dx;
        }

        int reviseX = 0;
        if (tracker != null) {
            mScroller.fling(0, 0, (int) tracker.getXVelocity(), 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
            reviseX = -mScroller.getFinalX() / 2;
            mScroller.forceFinished(true);
        }

        View child = getChildAt(nearestChildIdx);
        int childCenterX = child.getLeft() + (child.getMeasuredWidth() >> 1);
        float dx = mCenterPoint.x - childCenterX + reviseX;
        if (abs(dx) > child.getMeasuredWidth()) {
            nearestChildIdx += dx > 0 ? 1 : -1;
        }

        int ret = min(max(nearestChildIdx, mPrevCenterChildIdx - 1), mPrevCenterChildIdx + 1);
        return min(max(ret, 0), childCount - 1);
    }

    private void scrollToNearestChild(VelocityTracker tracker) {
        int nearestChildIdx = computeNearestChildIndex(tracker);
        mDrawingOrderArray = computeDrawingOrderArray();

        View child = getChildAt(nearestChildIdx);
        int childCenterX = child.getLeft() + (child.getMeasuredWidth() >> 1);
        float dx = mCenterPoint.x - childCenterX;
        mScroller.startScroll(getScrollX(), getScrollY(), (int) -dx, 0, mShortAnimTime);
        invalidate();
    }

    private void scrollToChildWithIndex(int index, boolean animate) {
        if (index >= getChildCount()) {
            return;
        }

        View child = getChildAt(index);
        int childCenterX = child.getLeft() + (child.getMeasuredWidth() >> 1);
        float dx = mCenterPoint.x - childCenterX;
        if (animate) {
            mScroller.startScroll(getScrollX(), getScrollY(), (int) -dx, 0, mShortAnimTime);
        } else {
            scrollBy((int) -dx, 0);
        }

        invalidate();
    }

    private int computeScrollXWhenScrollToChild(int index) {
        if (index >= 0 && index < getChildCount()) {
            View child = getChildAt(index);
            int childCenterX = child.getLeft() + (child.getMeasuredWidth() >> 1);
            float dx = mCenterPoint.x - childCenterX;
            return getScrollX() - (int) dx;
        }
        return getScrollX();
    }


    private PointF mDownRawPoint = new PointF();
    private PointF mLastRawPoint = new PointF();
    private VelocityTracker mTracker;

    private boolean mHasTryIntercept = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mIgnoreAllTouchEvent) {
            return true;
        }

        int action = event.getAction();
        if (mTracker == null) {
            mTracker = VelocityTracker.obtain();
        }
        mTracker.addMovement(event);
        if (action == MotionEvent.ACTION_DOWN) {
            mHasTryIntercept = false;
            mPrevCenterChildIdx = computeNearestChildIndex(null);
            mDrawingOrderArray = computeDrawingOrderArray();
            if (mScroller.computeScrollOffset()) {
                mScroller.abortAnimation();
            }
            mDownRawPoint.set(event.getRawX(), event.getRawY());
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (!mHasTryIntercept) {
                int dx = (int) (event.getRawX() - mLastRawPoint.x);
                int dy = (int) (event.getRawY() - mLastRawPoint.y);
                if (abs(dy) > mConfiguration.getScaledTouchSlop()) {
                    mHasTryIntercept = true;
                    return false;
                }
                if (abs(dx) > mConfiguration.getScaledTouchSlop()) {
                    return true;
                }
            }
        }

        mLastRawPoint.set(event.getRawX(), event.getRawY());
        return super.onInterceptTouchEvent(event);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mIgnoreAllTouchEvent) {
            return true;
        }

        int action = event.getAction();
        if (mTracker == null) {
            mTracker = VelocityTracker.obtain();
        }
        mTracker.addMovement(event);
        if (action == MotionEvent.ACTION_DOWN) {
            mHasTryIntercept = false;
            mPrevCenterChildIdx = computeNearestChildIndex(null);
            mDrawingOrderArray = computeDrawingOrderArray();
            if (mScroller.computeScrollOffset()) {
                mScroller.abortAnimation();
            }
            mDownRawPoint.set(event.getRawX(), event.getRawY());
        } else if (action == MotionEvent.ACTION_MOVE) {
            int dx = (int) (event.getRawX() - mLastRawPoint.x);
            scrollBy(-dx, 0);
        } else if (action == MotionEvent.ACTION_UP) {
            mTracker.computeCurrentVelocity(1000);
            scrollToNearestChild(mTracker);
            if (mTracker != null) {
                mTracker.recycle();
                mTracker = null;
            }
            mPrevCenterChildIdx = computeNearestChildIndex(null);
            mDrawingOrderArray = computeDrawingOrderArray();
            invalidate();
        }
        mLastRawPoint.set(event.getRawX(), event.getRawY());
        return true;
    }

    public interface ViewTransformer {

        void onTransform(ViewGroup parent, View child, PointF center);

        ViewTransformer EXPANDED = new ViewTransformer() {
            @Override
            public void onTransform(ViewGroup parent, View child, PointF center) {
                float childCenterX = child.getLeft() + (child.getMeasuredWidth() >> 1);
                float dx = childCenterX - center.x;
                float scaleFactor = 1;
                float fraction = 0.1f;
                scaleFactor -= (abs(dx) / parent.getMeasuredWidth()) * fraction;
                scaleFactor = max(scaleFactor, 0.7f);
                child.setScaleX(scaleFactor);
                child.setScaleY(scaleFactor);
//                child.setTranslationY(max(abs(1 - scaleFactor), 0.1f) * child.getMeasuredHeight());
            }
        };
    }
}
