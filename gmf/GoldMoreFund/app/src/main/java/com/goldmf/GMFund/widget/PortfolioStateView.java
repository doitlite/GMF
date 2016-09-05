package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.goldmf.GMFund.util.DimensionConverter;

/**
 * Created by yale on 15/7/21.
 */
public class PortfolioStateView extends View {

    private static final String[] STATE_DESC = new String[]{"创建组合", "系统审核", "众筹募资", "封闭建仓"};
    private static final int CIRCLE_RADIUS_IN_DIP = 4;
    private static final int COLOR_DEFAULT = Color.parseColor("#cccccc");
    private static final int COLOR_SELECTED = Color.parseColor("#000000");

    private Paint mPaint;
    private Rect mTextBound;
    private int mSelectedIndex = -1;

    public PortfolioStateView(Context context) {
        this(context, null);
    }

    public PortfolioStateView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PortfolioStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(DimensionConverter.sp2px(getContext(), 12));
        mTextBound = new Rect();
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < STATE_DESC.length) {
            mSelectedIndex = index;
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int centerY = getHeight() >> 1;

        mPaint.setStrokeWidth(DimensionConverter.dp2px(getContext(), 2));
        mPaint.setColor(COLOR_DEFAULT);
        canvas.drawLine(0, centerY, width, centerY, mPaint);

        int count = STATE_DESC.length;
        int circleSpan = width / (count + 1);
        String text;
        for (int i = 0; i < count; i++) {
            mPaint.setColor((i == mSelectedIndex) ? COLOR_SELECTED : COLOR_DEFAULT);
            text = STATE_DESC[i];
            int circleCenterX = circleSpan * (i + 1);
            canvas.drawCircle(circleCenterX, centerY, DimensionConverter.dp2px(getContext(), CIRCLE_RADIUS_IN_DIP), mPaint);
            mPaint.getTextBounds(text, 0, text.length(), mTextBound);
            canvas.drawText(text, circleCenterX - (mTextBound.width() >> 1), centerY + mTextBound.height() + DimensionConverter.dp2px(getContext(), CIRCLE_RADIUS_IN_DIP + 4), mPaint);
        }
    }
}
