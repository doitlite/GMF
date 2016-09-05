package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.goldmf.GMFund.R;

import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static java.lang.Math.*;

/**
 * Created by yale on 15/7/29.
 */
public class TransactionSeekBar extends View {
    private static final int CIRCLE_PADDING = 16;

    private int mAnchorHalfHeight;
    private int mLineWidth;
    private int mTextTopMargin;
    private int mCircleRadius;

    private Paint mPaint;
    private float mMaxProgress = 10000;
    private float mOriginalProgress = 2500;
    private float mCurrentProgress = 2500;
    private RectF mCircleFrame = new RectF();
    private Rect mTextBounds = new Rect();

    private OnProgressChangedListener mOnProgressChangedListener;

    public TransactionSeekBar(Context context) {
        this(context, null);
    }

    public TransactionSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransactionSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int fontSize = 12;
        mLineWidth = 4;
        int mAnchorHeight = dp2px(this, 8);
        mTextTopMargin = dp2px(this, 4);
        mCircleRadius = dp2px(this, 6);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TransactionSeekBar, defStyleAttr, 0);

            mLineWidth = a.getDimensionPixelSize(R.styleable.TransactionSeekBar_lineWidth, mLineWidth);
            fontSize = a.getDimensionPixelSize(R.styleable.TransactionSeekBar_fontSize, fontSize);
            mAnchorHeight = a.getDimensionPixelSize(R.styleable.TransactionSeekBar_anchorHeight, mAnchorHeight);
            mTextTopMargin = a.getDimensionPixelSize(R.styleable.TransactionSeekBar_textTopMargin, mTextTopMargin);
            mCircleRadius = a.getDimensionPixelSize(R.styleable.TransactionSeekBar_circleRadius, mCircleRadius);

            a.recycle();
        }
        mAnchorHalfHeight = mAnchorHeight >> 1;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(mLineWidth);
        mPaint.setTextSize(sp2px(this, fontSize));
    }

    public void setOnProgressChangedListener(OnProgressChangedListener listener) {
        mOnProgressChangedListener = listener == null ? OnProgressChangedListener.NULL : listener;
    }

    public void setMaxProgress(int maxProgress) {
        if (mMaxProgress > 0) {
            mMaxProgress = maxProgress;
            setOriginalProgress((int) mOriginalProgress);
            setCurrentProgress((int) mCurrentProgress);
        }
        invalidate();
    }

    public void setOriginalProgress(int originalProgress) {
        mOriginalProgress = min(originalProgress, mMaxProgress);
        invalidate();
    }

    public void setCurrentProgress(int progress) {
        mCurrentProgress = max(0, min(mMaxProgress, progress));
        invalidate();
    }

    private float mMoveRawX;
    private static final float FRACTION = 1.0f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            if (!mCircleFrame.contains(event.getX(), event.getY())) {
                return false;
            } else {
                mMoveRawX = event.getRawX();
                return true;
            }
        } else if (action == MotionEvent.ACTION_UP) {
            return true;
        } else if (action == MotionEvent.ACTION_MOVE) {
            float deltaInPx = (event.getRawX() - mMoveRawX) * FRACTION;
            float deltaInVal = deltaInPx * mMaxProgress / getWidth();
            float newValue = mCurrentProgress + deltaInVal;
            if (newValue <= mMaxProgress && newValue >= 0) {
                mCurrentProgress += deltaInVal;
                mOnProgressChangedListener.onProgressChanged((int) mCurrentProgress);
                mMoveRawX = event.getRawX();
                postInvalidate();
            } else {
                mCurrentProgress = (newValue < 0) ? 0 : mMaxProgress;
                mOnProgressChangedListener.onProgressChanged((int) mCurrentProgress);
                postInvalidate();
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth() - (mCircleRadius << 1);
        int height = getHeight();
        int offsetX = mCircleRadius;
        int centerY = height >> 1;

        // draw bottom line
        {
            mPaint.setColor(0xFFD2D5DB);
            int startX = offsetX;
            int endX = startX + width;
            canvas.drawLine(startX, centerY, endX, centerY, mPaint);
        }

        float originalValInPx = mOriginalProgress * width / mMaxProgress;
        float currentValInPx = mCurrentProgress * width / mMaxProgress;

        // draw original value anchor
        {
            mPaint.setColor(0xFF000000);
            canvas.drawLine(originalValInPx + offsetX, centerY - mAnchorHalfHeight, originalValInPx + offsetX, centerY + mAnchorHalfHeight, mPaint);
        }

        // draw text under original value anchor
        {
            String text = "持仓";
            mPaint.getTextBounds(text, 0, text.length(), mTextBounds);

            int previousColor = mPaint.getColor();
            mPaint.setColor(0xFF999999);
            int textHalfWidth = mTextBounds.width() >> 1;
            float x = originalValInPx - textHalfWidth + offsetX;
            float y = centerY + mAnchorHalfHeight + mTextBounds.height() + mTextTopMargin;
            canvas.drawText(text, 0, text.length(), x, y, mPaint);
            mPaint.setColor(previousColor);
        }

        // draw line from original value to current value
        {
            mPaint.setColor(0xFF000000);
            float startX = originalValInPx + offsetX;
            float endX = currentValInPx;
            canvas.drawLine(startX, centerY, endX, centerY, mPaint);
        }

        // draw circle with current value
        {
            mPaint.setColor(0xFF000000);
            Paint.Style previousStyle = mPaint.getStyle();
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(currentValInPx + offsetX, centerY, mCircleRadius - (mLineWidth >> 1), mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(0xFFFFFFFF);
            canvas.drawCircle(currentValInPx + offsetX, centerY, mCircleRadius - mLineWidth, mPaint);

            mPaint.setStyle(previousStyle);
            mCircleFrame.set(currentValInPx - mCircleRadius - CIRCLE_PADDING + offsetX, centerY - mCircleRadius - CIRCLE_PADDING, currentValInPx + mCircleRadius + CIRCLE_PADDING + offsetX, centerY + mCircleRadius + CIRCLE_PADDING);
        }
    }

    public interface OnProgressChangedListener {
        void onProgressChanged(int currentProgress);

        OnProgressChangedListener NULL = new OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int currentProgress) {
            }
        };
    }
}
