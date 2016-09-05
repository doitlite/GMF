package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

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
public class TimesDetailChartView extends TimesBaseView {

    public TimesDetailChartView(Context context) {
        this(context, null);
    }

    public TimesDetailChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimesDetailChartView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        // 绘制X轴
        paint.setTextSize(sp2px(10));
        paint.setColor(TEXT_GREY_COLOR);
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.STROKE);
        Rect textBounds = new Rect();

        for (int i = 0; i < mData.size(); i++) {
            String time = formatSecond(mData.get(i).time, "HH:mm");
            paint.getTextBounds(time, 0, time.length(), textBounds);
            if (i == 0) {
                canvas.drawText(time, chartLeft + dp2px(6), lowerTop - (upperLowerSpace - (textBounds.bottom - textBounds.top)) / 2, paint);
            } else if (mData.get(i).time - mData.get(i - 1).time > 3600) {
                time = formatSecond(mData.get(i).time, "HH:mm") + "/" + formatSecond(mData.get(i - 1).time, "HH:mm");
                paint.getTextBounds(time, 0, time.length(), textBounds);
                canvas.drawText(time, chartLeft + dataSpacing * i - (textBounds.right - textBounds.left) / 2, lowerTop - (upperLowerSpace - (textBounds.bottom - textBounds.top)) / 2, paint);
            } else if (i == mData.size() - 1) {
                canvas.drawText(time, chartRight - dp2px(6) - (textBounds.right - textBounds.left), lowerTop - (upperLowerSpace - (textBounds.bottom - textBounds.top)) / 2, paint);
            }
        }
    }

    @Override
    public void drawYAxis(Canvas canvas) {

        // 绘制Y轴
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
