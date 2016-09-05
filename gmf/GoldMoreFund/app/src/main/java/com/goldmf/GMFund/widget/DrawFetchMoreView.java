package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_BACKGROUND_COLOR;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;

/**
 * Created by Evan on 16/4/18 下午7:28.
 */
public class DrawFetchMoreView extends View {

    public static final int DEFAULT_UPPER_LOWER_SPACE = dp2px(24);
    public static final int DEFAULT_UPER_LATITUDE_NUM = 2;
    public static final int DEFAULT_LOWER_LATITUDE_NUM = 0;

    public float candleWidth;
    public float latitudeSpacing;
    public float upperLowerSpace = DEFAULT_UPPER_LOWER_SPACE;

    public float upperTop;
    public float upperBottom;
    public float upperHeight;
    public float lowerTop;
    public float lowerBottom;
    public float lowerHeight;
    public float chartLeft;
    public float chartRight;

    public Paint paint;
    public Rect textBounds;
    private String mTitle = "";

    public DrawFetchMoreView(Context context) {
        this(context, null);
    }

    public DrawFetchMoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawFetchMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        textBounds = new Rect();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
    }

    public void setText(String title) {
        mTitle = title;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight();
        int width = getWidth();
        chartLeft = 0;
        upperTop = 0;
        chartRight = width - 1;
        lowerBottom = height - 1;

        latitudeSpacing = (height - upperLowerSpace) / (DEFAULT_UPER_LATITUDE_NUM + DEFAULT_LOWER_LATITUDE_NUM + 2);
        upperHeight = latitudeSpacing * (DEFAULT_UPER_LATITUDE_NUM + 1);
        upperBottom = upperHeight;
        lowerHeight = latitudeSpacing * (DEFAULT_LOWER_LATITUDE_NUM + 1);
        lowerTop = height - lowerHeight;

        drawBorders(canvas);
        drawText(canvas);
    }

    public void drawBorders(Canvas canvas) {
        paint.setColor(LINE_BACKGROUND_COLOR);
        paint.setStrokeWidth(1);

        canvas.drawLine(chartLeft, upperHeight / 2, chartRight, upperHeight / 2, paint);
        canvas.drawLine((chartRight - chartLeft) / 2, upperTop, (chartRight - chartLeft) / 2, upperBottom, paint);
        canvas.drawLine((chartRight - chartLeft) / 2, lowerTop, (chartRight - chartLeft) / 2, lowerBottom, paint);

        canvas.drawLine(chartLeft, upperTop, chartRight, upperTop, paint);
        canvas.drawLine(chartLeft, upperBottom, chartRight, upperBottom, paint);

        canvas.drawLine(chartLeft, lowerTop, chartRight, lowerTop, paint);
        canvas.drawLine(chartLeft, lowerBottom, chartRight, lowerBottom, paint);
    }

    private void drawText(Canvas canvas) {
        paint.setColor(GREY_COLOR);
        paint.setStrokeWidth(0);
        paint.setTextSize(sp2px(10));
        paint.getTextBounds(mTitle, 0, mTitle.length(), textBounds);
        float x = (chartRight - chartLeft) / 2 - (textBounds.right - textBounds.left) / 2;
        float y = upperHeight / 2 - (textBounds.top - textBounds.bottom) / 2;
        canvas.drawText(mTitle, x, y, paint);
    }
}
