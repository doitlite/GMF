package com.goldmf.GMFund.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.goldmf.GMFund.widget.ChartController.ChartInfo;

import java.util.List;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_BACKGROUND_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;

/**
 * Created by Evan on 16/1/29 下午3:29.
 */
public class FiveDayChartRender extends KLineChartBaseRender {
    Rect textBounds = new Rect();

    @Override
    public void init() {
        upperLowerSpace = dp2px(14);
    }

    @Override
    public void drawXAxis(List<ChartInfo> currentPageData, List<ChartInfo> previousPageData, List<ChartInfo> nextPageData, Canvas canvas) {

        // 绘制X轴
        paint.setStrokeWidth(0);
        paint.setColor(TEXT_GREY_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(sp2px(8));

        int index = 0;
        for (int i = 0; i < currentPageData.size(); i++) {
            if (currentPageData.get(i).isDrawLine) {

                if (i != currentPageData.size() - 1) {
                    paint.setStrokeWidth(1);
                    paint.setColor(LINE_BACKGROUND_COLOR);
                    canvas.drawLine(chartLeft + candleWidth * i, upperTop, chartLeft + candleWidth * i, upperBottom, paint);
                    canvas.drawLine(chartLeft + candleWidth * i, lowerTop, chartLeft + candleWidth * i, lowerBottom, paint);
                }

                paint.setStrokeWidth(0);
                paint.setColor(TEXT_GREY_COLOR);
                String time = formatSecond(currentPageData.get(i).time, "MM-dd");
                paint.getTextBounds(time, 0, time.length(), textBounds);
                canvas.drawText(time, chartLeft + candleWidth * index + (candleWidth * (i - index) - (textBounds.right - textBounds.left)) / 2, lowerTop - (upperLowerSpace - (textBounds.bottom - textBounds.top)) / 2, paint);
                index = i;
            }
        }
    }
}
