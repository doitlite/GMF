package com.goldmf.GMFund.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.util.DimensionConverter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.*;

/**
 * Created by yale on 15/8/5.
 */
public class StringFlow extends View {
    private List<String> mDataSet = new LinkedList<>();
    private int mSelectedPosition = 3;
    private float mTextRectHeight;
    private float mHalfTextRectHeight;
    private float mTextSize;
    private float mTextHeight;
    private TextPaint mPaint;
    private float mExtraOffsetY = 0;
    private DisplayMetrics mDisplayMetrics;
    private int mVisibleElementCount = 3;
    private OnItemSelectedListener mOnItemSelectedListener = OnItemSelectedListener.NULL;

    public StringFlow(Context context) {
        this(context, null);
    }

    public StringFlow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StringFlow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StringFlow, defStyleAttr, 0);
        if (a != null) {
            mVisibleElementCount = a.getInteger(R.styleable.StringFlow_visibleElementCount, mVisibleElementCount);
            a.recycle();
        }

        mDataSet = new LinkedList<>(Arrays.asList("2012", "2013", "2014", "2015", "2016", "2017", "2018"));
        mDisplayMetrics = new DisplayMetrics();
        mTextRectHeight = dp2px(40);
        mHalfTextRectHeight = mTextRectHeight / 2;
        mTextSize = sp2px(14);

        mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(0xFF000000);
        mPaint.setTextSize(mTextSize);
        mPaint.setStyle(Paint.Style.STROKE);
        mTextHeight = measureTextHeight();
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener == null ? OnItemSelectedListener.NULL : listener;
    }

    public void setDataSet(List<String> dataSet) {
        mDataSet = dataSet;
        mSelectedPosition = 0;
        invalidate();
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    protected void setExtraOffsetY(float arg) {
        mExtraOffsetY = arg;
        invalidate();
    }

    private static final float mFraction = 1.0f;
    private float mDownY;
    private float mMoveY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        final int action = e.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            mMoveY = mDownY = e.getY();
            return true;
        } else if (action == MotionEvent.ACTION_UP) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(this, "extraOffsetY", mExtraOffsetY, 0);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.setDuration(300L);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
            return true;
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (mDownY != mMoveY) {
                mExtraOffsetY += (e.getY() - mMoveY) * mFraction;
                invalidate();
            }
            mMoveY = e.getY();
            return true;
        }
        return super.onTouchEvent(e);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY) {
            final WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(mDisplayMetrics);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mDisplayMetrics.widthPixels / 3, MeasureSpec.EXACTLY);
        }

        if (heightMode != MeasureSpec.EXACTLY) {
            final int heightSize = (int) (getPaddingTop() + getPaddingBottom() + mTextRectHeight * mVisibleElementCount);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int dataCount = mDataSet.size();
        if (mSelectedPosition >= 0 && mSelectedPosition < dataCount) {
            final int w = getWidth() - getPaddingLeft() - getPaddingRight();
            final int h = getHeight() - getPaddingTop() - getPaddingBottom();
            final int cx = w >> 1;
            final int cy = h >> 1;
            int aspectSelectedPosition = mSelectedPosition;
            float absMinOffsetY;

            // draw center rect
            {
                int previousColor = mPaint.getColor();

                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(0xFFCDCDCD);
                canvas.drawRect(getPaddingLeft(), (int) (cy - mHalfTextRectHeight), getPaddingLeft() + w, (int) (cy + mHalfTextRectHeight), mPaint);
                mPaint.setStyle(Paint.Style.FILL);

                mPaint.setColor(previousColor);
            }

            // draw selected text
            {
                float offsetY = mExtraOffsetY;
                final String text = mDataSet.get(mSelectedPosition);
                drawText(canvas, text, offsetY, cx, cy, 255);
                absMinOffsetY = abs(offsetY);
            }

            // draw before texts
            if (mSelectedPosition != 0) {
                final int count = mSelectedPosition;
                int index = 0;
                float offsetY = -(mTextRectHeight * count) + mExtraOffsetY;
                final Iterator<String> iterator = mDataSet.iterator();
                int alpha = 255 - (100 * count);
                while (iterator.hasNext() && index < mSelectedPosition) {
                    final String text = iterator.next();
                    drawText(canvas, text, offsetY, cx, cy, Math.max(0, alpha));
                    alpha += 100;

                    if (absMinOffsetY > abs(offsetY)) {
                        absMinOffsetY = abs(offsetY);
                        aspectSelectedPosition = index;
                    }

                    offsetY += mTextRectHeight;
                    index++;
                }
            }

            // draw after texts
            if (mSelectedPosition != dataCount - 1) {
                float offsetY = mTextRectHeight + mExtraOffsetY;
                int index = mSelectedPosition + 1;
                List<String> afterTexts = mDataSet.subList(mSelectedPosition + 1, dataCount);
                final Iterator<String> iterator = afterTexts.iterator();
                int alpha = 255;
                while (iterator.hasNext() && index < dataCount) {
                    alpha -= 100;
                    alpha = max(0, alpha);
                    final String text = iterator.next();
                    drawText(canvas, text, offsetY, cx, cy, alpha);

                    if (absMinOffsetY > abs(offsetY)) {
                        absMinOffsetY = abs(offsetY);
                        aspectSelectedPosition = index;
                    }

                    offsetY += mTextRectHeight;
                    index++;
                }
            }

            if (mSelectedPosition != aspectSelectedPosition) {
                mOnItemSelectedListener.onItemSelected(aspectSelectedPosition, mDataSet.get(aspectSelectedPosition));
            }
            mExtraOffsetY = mExtraOffsetY + (aspectSelectedPosition - mSelectedPosition) * mTextRectHeight;
            mSelectedPosition = aspectSelectedPosition;
        }
    }

    private void drawText(Canvas canvas, String text, float offsetY, int cx, int cy, int alpha) {
        final float textWidth = measureTextWidth(text);
        final float x = cx - textWidth / 2 - mPaint.getFontMetrics().descent;
        final float y = cy + mTextHeight / 2 + offsetY;
        mPaint.setAlpha(alpha);
        canvas.drawText(text, x, y, mPaint);
        mPaint.setAlpha(255);
    }

    private int dp2px(float dp) {
        return (int) DimensionConverter.dp2px(getContext(), dp);
    }

    private int sp2px(float sp) {
        return (int) DimensionConverter.sp2px(getContext(), sp);
    }

    private float measureTextWidth(String text) {
        return mPaint.measureText(text);
    }

    private float measureTextHeight() {
        Paint.FontMetrics metrics = mPaint.getFontMetrics();
        return metrics.descent - metrics.ascent;
    }

    public interface OnItemSelectedListener {
        void onItemSelected(int index, String item);

        OnItemSelectedListener NULL = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index, String item) {
            }
        };
    }
}
