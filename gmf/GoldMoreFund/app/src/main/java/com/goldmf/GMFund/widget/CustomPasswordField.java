package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.util.DimensionConverter;

/**
 * Created by yale on 15/7/31.
 */
public class CustomPasswordField extends View {

    private static final int STROKE_WIDTH_IN_DP = 2;
    private static final int BORDER_COLOR = 0xFFCCCCCC;
    private static final int SEP_LINE_COLOR = 0xFFF2F2F2;
    private static final int NUM_COLOR = 0xFF000000;

    private int mNumViewSize;
    private int mDotRadius;
    private int mMaxNumCount;
    private int mCurrentNumCount;

    private Paint mPaint;

    public CustomPasswordField(Context context) {
        this(context, null);
    }

    public CustomPasswordField(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomPasswordField(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, 0);

        mNumViewSize = (int) dp2px(36);
        mMaxNumCount = 6;
        mDotRadius = (int) dp2px(6);
        {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomPasswordField, defStyleAttr, 0);
            if (a != null) {
                mNumViewSize = a.getDimensionPixelSize(R.styleable.CustomPasswordField_numViewSize, mNumViewSize);
                mMaxNumCount = a.getInteger(R.styleable.CustomPasswordField_maxNumCount, mMaxNumCount);
                mDotRadius = a.getDimensionPixelSize(R.styleable.CustomPasswordField_dotRadius, mDotRadius);

                a.recycle();
            }
        }

        // setup paint
        {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStrokeWidth(STROKE_WIDTH_IN_DP);
        }
    }

    public void increaseNumCount() {
        setCurrentNumCount(mCurrentNumCount + 1);
    }

    public void decreaseNumCount() {
        setCurrentNumCount(mCurrentNumCount - 1);
    }

    public void setCurrentNumCount(int numCount) {
        if (numCount < 0 || numCount > mMaxNumCount || numCount == mCurrentNumCount) {
            return;
        }

        mCurrentNumCount = numCount;
        invalidate();
    }

    public int getCurrentNumCount() {
        return mCurrentNumCount;
    }

    public void setMaxNumCount(int maxNumCount) {
        if (maxNumCount <= 0 && maxNumCount != mMaxNumCount) {
            return;
        } else {
            mCurrentNumCount = Math.min(maxNumCount, mCurrentNumCount);
            mMaxNumCount = maxNumCount;
            invalidate();
        }
    }

    public int getMaxNumCount() {
        return mMaxNumCount;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY) {
            int size = mNumViewSize * mMaxNumCount + STROKE_WIDTH_IN_DP * (mMaxNumCount - 1) + STROKE_WIDTH_IN_DP;
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        }

        if (heightMode != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mNumViewSize, MeasureSpec.EXACTLY) + STROKE_WIDTH_IN_DP;
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int width = getWidth();
        final int height = getHeight();
        final int centerX = width >> 1;
        final int centerY = height >> 1;
        final int numViewSize = mNumViewSize;
        final int halfNumViewSize = mNumViewSize >> 1;

        // compute border width
        int borderWidth = 0;
        {
            borderWidth += mNumViewSize * mMaxNumCount;
            borderWidth += STROKE_WIDTH_IN_DP * (mMaxNumCount - 1);
        }
        final int halfBorderWidth = borderWidth >> 1;

        // draw border
        {
            int previousColor = mPaint.getColor();
            mPaint.setColor(BORDER_COLOR);

            int left = centerX - halfBorderWidth;
            int top = centerY - halfNumViewSize;
            int right = left + borderWidth;
            int bottom = centerY + halfNumViewSize;

            Paint.Style previousStyle = mPaint.getStyle();
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(left, top, right, bottom, mPaint);
            mPaint.setStyle(previousStyle);

            mPaint.setColor(previousColor);
        }

        // draw sep-line
        {
            int previousColor = mPaint.getColor();
            mPaint.setColor(SEP_LINE_COLOR);

            int offsetX = centerX - halfBorderWidth;
            int count = mMaxNumCount - 1;
            for (int i = 0; i < count; i++) {
                offsetX += numViewSize;
                final int startX = offsetX;
                final int startY = centerY - halfNumViewSize;
                final int endX = startX;
                final int endY = centerY + halfNumViewSize;

                canvas.drawLine(startX, startY, endX, endY, mPaint);
            }

            mPaint.setColor(previousColor);
        }

        // draw current num
        {
            int previousColor = mPaint.getColor();
            Paint.Style previousStyle = mPaint.getStyle();
            mPaint.setColor(NUM_COLOR);
            mPaint.setStyle(Paint.Style.FILL);

            int offsetX = centerX - halfBorderWidth;
            for (int i = 0; i < mCurrentNumCount; i++) {
                final int cx = offsetX + halfNumViewSize;
                final int cy = centerY;
                offsetX += numViewSize;

                canvas.drawCircle(cx, cy, mDotRadius, mPaint);
            }

            mPaint.setColor(previousColor);
            mPaint.setStyle(previousStyle);
        }
    }

    private float dp2px(float dp) {
        return DimensionConverter.dp2px(getContext(), dp);
    }

    private float sp2px(float sp) {
        return DimensionConverter.sp2px(getContext(), sp);
    }
}
