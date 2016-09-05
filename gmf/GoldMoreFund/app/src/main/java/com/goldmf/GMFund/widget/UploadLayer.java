package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static java.lang.Math.*;

/**
 * Created by yale on 15/11/25.
 */
public class UploadLayer extends View {

    private int WIDTH = 0;
    private int HEIGHT = 0;
    private int HALF_WIDTH = 0;
    private int HALF_HEIGHT = 0;

    private int mCornerInPixel;
    private int mBackColor;
    private int mFrontColor;
    private int mProgress;
    private Paint mPaint = new Paint();
    private RectF mRectF = new RectF();
    private Path mPath = new Path();

    public UploadLayer(Context context) {
        this(context, null, 0);
    }

    public UploadLayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UploadLayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mCornerInPixel = dp2px(this, 4);
        mBackColor = 0x66000000;
        mFrontColor = 0x33000000;
        mProgress = 75;

        mPaint.setAntiAlias(true);
    }

    public void setProgress(int progress) {
        mProgress = max(0, min(100, progress));
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        WIDTH = getMeasuredWidth();
        HEIGHT = getMeasuredHeight();
        HALF_WIDTH = WIDTH >> 1;
        HALF_HEIGHT = HEIGHT >> 1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRectF.set(0, 0, WIDTH, HEIGHT);
        mPath.addRoundRect(mRectF, mCornerInPixel, mCornerInPixel, Path.Direction.CW);
        canvas.clipPath(mPath);

        int frontHeight = HEIGHT * mProgress / 100;
        int backHeight = HEIGHT - frontHeight;

        mPaint.setColor(mBackColor);
        mPaint.setStyle(Paint.Style.FILL);
        mRectF.set(0, 0, WIDTH, backHeight);
        canvas.drawRect(mRectF, mPaint);

        mPaint.setColor(mFrontColor);
        mPaint.setStyle(Paint.Style.FILL);
        mRectF.set(0, backHeight, WIDTH, HEIGHT);
        canvas.drawRect(mRectF, mPaint);
    }
}
