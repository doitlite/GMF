package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.goldmf.GMFund.R;

/**
 * Created by yale on 15/11/26.
 */
public class AppleCircularProgressBar extends View {
    private static int UPDATE_INTERVAL = 83;
    private Bitmap mRoateBitmap;
    private Paint mPaint;
    private Matrix mMatrix;
    private Handler mHandler = new Handler();
    private Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            invalidate();
            mHandler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    public AppleCircularProgressBar(Context context) {
        this(context, null, 0);
    }

    public AppleCircularProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("ConstantConditions")
    public AppleCircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mRoateBitmap = ((BitmapDrawable) getResources().getDrawable(R.mipmap.ic_spinner)).getBitmap();
        mMatrix = new Matrix();
        mMatrix.reset();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mRoateBitmap.getWidth(), MeasureSpec.EXACTLY);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mRoateBitmap.getHeight(), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMatrix.reset();
        mMatrix.setTranslate((getMeasuredWidth() - mRoateBitmap.getWidth()) / 2, (getMeasuredHeight() - mRoateBitmap.getHeight()) / 2);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler.removeCallbacksAndMessages(null);
        updateTask.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mMatrix.postRotate(30, getWidth() >> 1, getHeight() >> 1);
        canvas.drawBitmap(mRoateBitmap, mMatrix, mPaint);
    }
}
