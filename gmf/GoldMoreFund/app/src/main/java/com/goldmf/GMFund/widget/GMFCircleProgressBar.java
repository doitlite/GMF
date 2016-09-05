package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.goldmf.GMFund.R;

import static com.goldmf.GMFund.extension.ViewExtension.dp2px;

/**
 * Created by Evan on 16/1/27 下午2:57.
 */
public class GMFCircleProgressBar extends View {

    private int mOffSetStartAngle;
    private int mBorderWidth;
    private int mBorderColor;
    private int mFillColor;
    private RectF mRect;
    private Paint mPaint;
    private float mSweepAngle;

    public GMFCircleProgressBar(Context context) {
        this(context, null);
    }

    public GMFCircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GMFCircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mOffSetStartAngle = 0;
        mSweepAngle = 0;
        mBorderColor = 0xFFEEB7B0;
        mFillColor = 0xFFD65645;
        mBorderWidth = dp2px(this, 4);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.GMFCircleProgressBar, defStyleAttr, 0);
        if (arr != null) {
            mSweepAngle = arr.getFloat(R.styleable.GMFCircleProgressBar_gmf_cpb_progress, mSweepAngle);
            mBorderColor = arr.getColor(R.styleable.GMFCircleProgressBar_gmf_cpb_border_color, mBorderColor);
            mFillColor = arr.getColor(R.styleable.GMFCircleProgressBar_gmf_cpb_fill_color, mFillColor);
            arr.recycle();
        }

        mRect = new RectF();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    public void setBorderColor(int color) {
        mBorderColor = color;
    }

    public void setFillColor(int color) {
        mFillColor = color;
    }

    public void setProgress(float sweepAngle) {
        mSweepAngle = sweepAngle;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(dp2px(this, 42), MeasureSpec.EXACTLY);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(dp2px(this, 42), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int radius = Math.min(getWidth(), getHeight()) >> 1;
        final int cx = getWidth() >> 1;
        final int cy = getHeight() >> 1;

        mRect.set(0, 0, getWidth(), getHeight());
        mRect.inset(mBorderWidth, mBorderWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBorderWidth);
        mPaint.setColor(mBorderColor);
        canvas.drawCircle(cx, cy, radius - mBorderWidth, mPaint);

        mPaint.setColor(mFillColor);
        mPaint.setStrokeWidth(mBorderWidth);
        canvas.drawArc(mRect, -90 + mOffSetStartAngle, mSweepAngle * 360 / 100, false, mPaint);
    }
}
