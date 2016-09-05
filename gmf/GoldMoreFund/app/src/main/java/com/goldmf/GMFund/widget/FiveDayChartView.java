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
public class FiveDayChartView extends TimesBaseView {


    public FiveDayChartView(Context context) {
        this(context, null);
    }

    public FiveDayChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FiveDayChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void init() {
        upperLowerSpace = dp2px(14);
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

    public void drawYAxis(Canvas canvas) {

        Paint paint = new Paint();
        paint.setTextSize(sp2px(8));
        paint.setColor(TEXT_BLACK_COLOR);
        Rect textBounds = new Rect();

        String text = formatBigNumber(mMaxPriceLine - 2 * mUpperHalfHigh, false, 2, 2, false);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, dp2px(2), upperBottom - dp2px(2), paint);

        text = formatRatio((mMaxPriceLine - 2 * mUpperHalfHigh - mPreClose) / mPreClose, false, 2);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, chartRight - dp2px(6) - (textBounds.right - textBounds.left), upperBottom - dp2px(2), paint);

        text = formatBigNumber(mMaxPriceLine, false, 2, 2, false);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, dp2px(2), (textBounds.bottom - textBounds.top) + 2, paint);

        text = formatRatio((mMaxPriceLine - mPreClose) / mPreClose, false, 2);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, chartRight - dp2px(6) - (textBounds.right - textBounds.left), (textBounds.bottom - textBounds.top) + 2, paint);
    }

}
