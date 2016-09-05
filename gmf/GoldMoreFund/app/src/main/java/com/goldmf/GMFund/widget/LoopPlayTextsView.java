package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.goldmf.GMFund.R;

import java.util.List;

import rx.functions.Action2;
import yale.extension.common.Range;

import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static java.lang.Math.max;

/**
 * Created by yale on 16/2/29.
 */
public class LoopPlayTextsView extends ViewGroup {
    private Range mHorizontalScrollRange;
    private Handler mAnimationHandler = new Handler();

    private int mTextSizeInPx;
    private int mTextColor;
    private int mScrollIntervalInMills;
    private int mScrollStepInPx;
    private int mChildMargin;
    private Action2<View, Integer> mOnChildClickListener = null;
    private int mUniqueTextCount;
    private boolean mNeedToLoop = false;

    public LoopPlayTextsView(Context context) {
        this(context, null);
    }

    public LoopPlayTextsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("deprecation")
    public LoopPlayTextsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTextSizeInPx = sp2px(this, 16);
        mTextColor = context.getResources().getColor(R.color.gmf_text_black);
        mScrollIntervalInMills = 16;
        mScrollStepInPx = dp2px(this, 1);
        mChildMargin = dp2px(this, 32);

        TypedArray typed = context.obtainStyledAttributes(attrs, R.styleable.LoopPlayTextsView, defStyleAttr, 0);
        mTextSizeInPx = typed.getDimensionPixelSize(R.styleable.LoopPlayTextsView_textSize, mTextSizeInPx);
        mTextColor = typed.getColor(R.styleable.LoopPlayTextsView_textColor, mTextColor);
        mScrollIntervalInMills = typed.getInt(R.styleable.LoopPlayTextsView_scrollIntervalInMills, mScrollIntervalInMills);
        mScrollStepInPx = typed.getDimensionPixelSize(R.styleable.LoopPlayTextsView_scrollStepInPx, mScrollStepInPx);
        mChildMargin = typed.getDimensionPixelOffset(R.styleable.LoopPlayTextsView_childMargin, mChildMargin);
        typed.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(MeasureSpec.makeMeasureSpec(100000, MeasureSpec.AT_MOST), heightMeasureSpec);
        mHorizontalScrollRange = computeContentHorizontalRange();

        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            int maxHeightOfChildren = 0;
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                maxHeightOfChildren = max(getChildAt(i).getMeasuredHeight(), maxHeightOfChildren);
            }
            setMeasuredDimension(getDefaultSize(getSuggestedMinimumHeight(), widthMeasureSpec), maxHeightOfChildren);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childMargin = mChildMargin;
        int childCount = getChildCount();
        int offsetX = 0;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int offsetY = (getMeasuredHeight() - child.getMeasuredHeight()) >> 1;
            child.layout(offsetX, offsetY, offsetX + child.getMeasuredWidth(), offsetY + child.getMeasuredHeight());
            offsetX += child.getMeasuredWidth() + childMargin;
        }

        if (!isLooping()) {
            restartLoop();
        }
    }

    public void onResume() {
        resumeLoop();
    }

    public void onPause() {
        pauseLoop();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopLoop();
    }

    public void setOnChildClickListener(Action2<View, Integer> onChildClickListener) {
        this.mOnChildClickListener = onChildClickListener;
    }

    public void resetChildrenWithTexts(List<String> texts) {
        stopLoop();
        mNeedToLoop = false;
        post(() -> {
            removeAllViewsInLayout();
            mUniqueTextCount = (texts == null || texts.isEmpty()) ? 0 : texts.size();

            if (texts == null || texts.size() == 0) return;

            {
                int index = 0;
                for (String text : texts) {
                    addChildWithText(text, index);
                    index++;
                }
            }

            measureChildren(getMeasuredWidthAndState(), getMeasuredHeightAndState());
            int visibleChildCount = computeFullVisibleChildCount();
            if (visibleChildCount < getChildCount()) {
                int expectExtraSpace = getWidth();
                int currentExtraSpace = 0;
                while (currentExtraSpace < expectExtraSpace) {
                    int index = 0;
                    for (String text : texts) {
                        addChildWithText(text, index);
                        index++;
                        View child = getChildAt(getChildCount() - 1);
                        measureChild(child, getMeasuredWidthAndState(), getMeasuredHeightAndState());
                        currentExtraSpace += child.getMeasuredWidth() + mChildMargin;
                        if (currentExtraSpace >= expectExtraSpace) {
                            break;
                        }
                    }
                }
                mNeedToLoop = true;
            }
        });
    }

    private Range computeContentHorizontalRange() {
        int childMargin = mChildMargin;
        int contentWidth = -childMargin;
        for (int i = 0; i < mUniqueTextCount; i++) {
            contentWidth += childMargin + getChildAt(i).getMeasuredWidth();
        }
        return new Range(0, contentWidth + mChildMargin);
    }

    private int computeFullVisibleChildCount() {
        int width = getMeasuredWidth();
        int childMargin = mChildMargin;
        int offsetX = -childMargin;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            offsetX += childMargin + getChildAt(i).getMeasuredWidth();
            if (offsetX > width) {
                return i;
            }
        }

        return getChildCount();
    }

    private void addChildWithText(String text, int relativePosition) {
        TextView child = new TextView(getContext());
        child.setText(text);
        child.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSizeInPx);
        child.setTextColor(mTextColor);
        addView(child);

        child.setOnClickListener(v -> {
            if (mOnChildClickListener != null)
                mOnChildClickListener.call(child, relativePosition);
        });
    }

    private Runnable mAnimationTask = null;
    private boolean mIsPaused = false;

    private boolean isLooping() {
        return mAnimationTask != null;
    }

    private void pauseLoop() {
        mIsPaused = true;
    }

    private void resumeLoop() {
        if (mIsPaused && mAnimationTask != null) {
            mIsPaused = false;
            mAnimationTask.run();
        }
    }

    private void restartLoop() {
        if (!mNeedToLoop) return;
        if (isInEditMode()) return;
        if (mHorizontalScrollRange == null || mHorizontalScrollRange.max <= 0) return;

        stopLoop();
        mIsPaused = false;
        Handler handler = mAnimationHandler;
        final int startX = 0;
        final int endX = max(mHorizontalScrollRange.max, 0);
        final int interval = mScrollIntervalInMills;
        final int step = mScrollStepInPx;
        if (startX < endX) {
            mAnimationTask = new Runnable() {
                int mCacheScrollX = getScrollX();

                @Override
                public void run() {
                    int nextX = mCacheScrollX + step;
                    if (nextX > endX) {
                        nextX = (nextX - endX);
                    }
                    mCacheScrollX = nextX;
                    scrollTo(nextX, getScrollY());
                    if (!mIsPaused) {
                        handler.postDelayed(this, interval);
                    }
                }
            };
            mAnimationTask.run();
        }
    }

    private void stopLoop() {
        if (isInEditMode()) return;

        Handler handler = mAnimationHandler;
        if (mAnimationTask != null) {
            handler.removeCallbacks(mAnimationTask);
            mAnimationTask = null;
        }
    }
}
