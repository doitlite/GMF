package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Evan on 15/11/9 下午2:11.
 */
public class SemiCircleView extends View {

    private Paint mPaint = new Paint();
    private RectF mRectF = new RectF();
    private float mLeft = 0;
    private float mTop = 0;
    private float mRight;
    private float mBottom;


    public SemiCircleView(Context context) {
        this(context, null);
    }

    public SemiCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SemiCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRight = getMeasuredWidth();
        mBottom = getMeasuredHeight() * 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mRectF.set(mLeft, mTop, mRight, mBottom);
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        canvas.drawArc(mRectF, 180, 180, true, mPaint);

    }


}
