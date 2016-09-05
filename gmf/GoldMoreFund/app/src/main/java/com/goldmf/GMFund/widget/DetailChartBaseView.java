package com.goldmf.GMFund.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;

/**
 * Created by Evan on 16/1/29 下午3:35.
 */
public class DetailChartBaseView extends View {
    public static final PathEffect DEFAULT_DASH_EFFECT = new DashPathEffect(new float[]{10, 10, 10, 10}, 10);

    public static final int DEFAULT_BACKGROUND = WHITE_COLOR;
    public static final int DEFAULT_AXIS_TITLE_SIZE = sp2px(10);

    public static final int DEFAULT_UPPER_LOWER_SPACE = dp2px(24);
    public static final int DEFAULT_UPER_LATITUDE_NUM = 2;
    private static final int DEFAULT_LOWER_LATITUDE_NUM = 0;
    public static final int DEFAULT_LOGITUDE_NUM = 3;

    public float mUpperChartHeight;
    public float mUpperChartTop;
    public float mUpperChartBottom;
    public float mLowerChartHeight;
    public float mLowerChartTop;
    public float mLowerChartBottom;

    public float latitudeSpacing;

    public DetailChartBaseView(Context context) {
        this(context, null);
    }

    public DetailChartBaseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DetailChartBaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setBackgroundColor(DEFAULT_BACKGROUND);
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        latitudeSpacing = (viewHeight - DEFAULT_UPPER_LOWER_SPACE) / (DEFAULT_UPER_LATITUDE_NUM + DEFAULT_LOWER_LATITUDE_NUM + 2);

        mUpperChartHeight = latitudeSpacing * (DEFAULT_UPER_LATITUDE_NUM + 1);
        mUpperChartTop = 0;
        mUpperChartBottom = mUpperChartHeight;
        mLowerChartHeight = latitudeSpacing * (DEFAULT_LOWER_LATITUDE_NUM + 1);
        mLowerChartTop = viewHeight - mLowerChartHeight;
        mLowerChartBottom = viewHeight;

        drawBorders(canvas, viewHeight, viewWidth);
    }

    private void drawBorders(Canvas canvas, int viewHeight, int viewWidth) {
        Paint paint = new Paint();
        paint.setColor(GREY_COLOR);
        paint.setStrokeWidth(1);
        canvas.drawLine(40, 0, 40, mUpperChartBottom, paint);
        canvas.drawLine(40, 0, viewWidth - 40, 0, paint);
        canvas.drawLine(viewWidth - 40, 0, viewWidth - 40, mUpperChartBottom, paint);
        canvas.drawLine(viewWidth - 40, mUpperChartBottom, 40, mUpperChartBottom, paint);
        canvas.drawLine(40, mLowerChartTop, 40, viewHeight - 1, paint);
        canvas.drawLine(40, mLowerChartTop, viewWidth - 40, mLowerChartTop, paint);
        canvas.drawLine(viewWidth - 40, mLowerChartTop, viewWidth - 40, viewHeight - 1, paint);
        canvas.drawLine(40, viewHeight - 1, viewWidth - 40, viewHeight - 1, paint);
    }

}
