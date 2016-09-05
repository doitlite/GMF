package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.List;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;


/**
 * Created by Evan on 16/1/29 下午3:29.
 */
public class DayKLineChartView extends KLineBaseView {


    public DayKLineChartView(Context context) {
        this(context, null);
    }

    public DayKLineChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DayKLineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void initData() {
        upperLowerSpace = dp2px(14);
        mMinCount = mKLineDatas.size() < MIN_SHOW_DATA_COUNT ? mKLineDatas.size() : MIN_SHOW_DATA_COUNT;
    }

    @Override
    public void drawXAxis(Canvas canvas) {
        Paint paint = new Paint();
        paint.setTextSize(sp2px(10));
        paint.setColor(TEXT_GREY_COLOR);
        Rect textBounds = new Rect();
        List<Long> timeList = Stream.of(mKLineDatas).map(data -> data.traderTime).collect(Collectors.toList());
        for (int i = 0; i < mKLineDatas.size(); i++) {
            String time = formatSecond(timeList.get(i), "yyyy-MM");
            paint.getTextBounds(time, 0, time.length(), textBounds);
            if (i != 0 && !formatSecond(timeList.get(i), "yyyy-MM").equals(formatSecond(timeList.get(i - 1), "yyyy-MM"))) {
                canvas.drawText(time, chartLeft + candleWidth * i + (longitudeSpacing - (textBounds.right - textBounds.left)) / 2, lowerTop - (upperLowerSpace - (textBounds.bottom - textBounds.top)) / 2, paint);
                canvas.drawLine(chartLeft + candleWidth * i - (textBounds.right - textBounds.left) / 2, upperTop, chartLeft + candleWidth * i - (textBounds.right - textBounds.left) / 2, upperBottom, paint);
                canvas.drawLine(chartLeft + candleWidth * i - (textBounds.right - textBounds.left) / 2, lowerTop, chartLeft + candleWidth * i - (textBounds.right - textBounds.left) / 2, lowerBottom, paint);
            }
        }
    }


}
