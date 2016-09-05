package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_BACKGROUND_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.util.FormatUtil.formatBigNumber;
import static com.goldmf.GMFund.util.FormatUtil.formatRatio;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;


/**
 * Created by Evan on 16/2/2 上午10:10.
 */
public class FiveDayDetailChartView extends TimesBaseView {


    public FiveDayDetailChartView(Context context) {
        this(context, null);
    }

    public FiveDayDetailChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FiveDayDetailChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init() {
        chartLeft = dp2px(40);
        chartRight = getWidth() - dp2px(40);
        upperLowerSpace = dp2px(24);
    }

    @Override
    public void drawXAxis(Canvas canvas) {
        paint.setTextSize(sp2px(10));
        paint.setStyle(Paint.Style.STROKE);
        Rect textBounds = new Rect();

        int index = 0;
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).isDrawLine) {

                if (i != mData.size() - 1) {
                    paint.setStrokeWidth(1);
                    paint.setColor(LINE_BACKGROUND_COLOR);
                    canvas.drawLine(chartLeft + dataSpacing * i, upperTop, chartLeft + dataSpacing * i, upperBottom, paint);
                    canvas.drawLine(chartLeft + dataSpacing * i, lowerTop, chartLeft + dataSpacing * i, lowerBottom, paint);
                }

                paint.setStrokeWidth(0);
                paint.setColor(TEXT_GREY_COLOR);
                String time = formatSecond(mData.get(i).time, "MM-dd");
                paint.getTextBounds(time, 0, time.length(), textBounds);
                canvas.drawText(time, chartLeft + dataSpacing * index + (dataSpacing * (i - index) - (textBounds.right - textBounds.left)) / 2, lowerTop - (upperLowerSpace - (textBounds.bottom - textBounds.top)) / 2, paint);
                index = i;
            }
        }
    }

    @Override
    public void drawYAxis(Canvas canvas) {

        paint.setTextSize(sp2px(10));
        paint.setColor(TEXT_BLACK_COLOR);
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.STROKE);
        Rect textBounds = new Rect();

        String text = formatBigNumber(mMaxPriceLine - 2 * mUpperHalfHigh, false, 2, 2, false);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, chartLeft - (textBounds.right - textBounds.left) - dp2px(2), upperBottom - dp2px(2), paint);

        text = formatRatio((mMaxPriceLine - 2 * mUpperHalfHigh - mPreClose) / mPreClose, false, 2);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, chartRight + dp2px(2), upperBottom - dp2px(2), paint);

        text = formatBigNumber(mPreClose, false, 2, 2, false);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, chartLeft - (textBounds.right - textBounds.left) - dp2px(2), (float) (mUpperHalfHigh * upperRate + (textBounds.bottom - textBounds.top) / 2), paint);

        text = formatRatio((mPreClose - mPreClose) / mPreClose, false, 2);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, chartRight + dp2px(2), (float) (mUpperHalfHigh * upperRate + (textBounds.bottom - textBounds.top) / 2), paint);

        text = formatBigNumber(mMaxPriceLine, false, 2, 2, false);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, chartLeft - (textBounds.right - textBounds.left) - dp2px(2), (textBounds.bottom - textBounds.top) + dp2px(2), paint);

        text = formatRatio((mMaxPriceLine - mPreClose) / mPreClose, false, 2);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, chartRight + dp2px(2), (textBounds.bottom - textBounds.top) + dp2px(2), paint);

        text = formatBigNumber(mMaxVolume, 10000, 2);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, chartLeft - (textBounds.right - textBounds.left) - dp2px(2), lowerTop + (textBounds.bottom - textBounds.top) + dp2px(2), paint);

        text = "万手";
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, chartLeft - (textBounds.right - textBounds.left) - dp2px(2), lowerBottom - dp2px(2), paint);
    }


}
