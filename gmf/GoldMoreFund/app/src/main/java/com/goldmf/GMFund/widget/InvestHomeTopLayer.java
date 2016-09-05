package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.ViewExtension;

/**
 * Created by yale on 16/3/15.
 */
public class InvestHomeTopLayer extends View {
    private Paint mPaint;
    private Path mPath;

    public InvestHomeTopLayer(Context context) {
        this(context, null);
    }

    public InvestHomeTopLayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InvestHomeTopLayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(0xFFF2F2F2);
        mPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(ViewExtension.dp2px(this, 120), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Path path = mPath;
        path.moveTo(0, getMeasuredHeight() / 3 * 2);
        path.lineTo(getMeasuredWidth() / 2, getMeasuredHeight());
        path.lineTo(getMeasuredWidth(), getMeasuredHeight() / 3 * 2);
        path.lineTo(getMeasuredWidth(), getMeasuredHeight());
        path.lineTo(0, getMeasuredHeight());
        path.close();
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = mPaint;
        Path path = mPath;
        canvas.drawPath(path, paint);
    }
}
