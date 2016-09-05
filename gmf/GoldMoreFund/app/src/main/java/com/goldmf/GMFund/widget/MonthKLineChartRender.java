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
public class MonthKLineChartRender extends KLineChartBaseRender {
    Rect textBounds = new Rect();

    @Override
    public void init() {
        upperLowerSpace = dp2px(14);
    }

    @Override
    public void drawLongitudeLine(List<ChartInfo> currentPageData, List<ChartInfo> previousPageData, List<ChartInfo> nextPageData, Canvas canvas) {
        paint.setColor(LINE_BACKGROUND_COLOR);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < currentPageData.size(); i++) {
            ChartController.ChartInfo info = currentPageData.get(i);
            if (info.isDrawLine) {
                float startX = chartRight - candleWidth * i - candleWidth / 4 - (candleWidth - candleWidth / 4) / 2;
                canvas.drawLine(startX, upperTop, startX, upperBottom, paint);
                canvas.drawLine(startX, lowerTop, startX, lowerBottom, paint);
            }
        }
    }

    @Override
    public void drawXAxis(List<ChartInfo> currentPageData, List<ChartInfo> previousPageData, List<ChartInfo> nextPageData, Canvas canvas) {

        paint.setStrokeWidth(0);
        paint.setColor(TEXT_GREY_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(sp2px(8));
        String time = formatSecond(currentPageData.get(0).time, "yyyy");
        Rect textBounds = new Rect();
        paint.getTextBounds(time, 0, time.length(), textBounds);
        float textWidth = textBounds.right - textBounds.left;
        int index = Math.round(textWidth / candleWidth);
        int prevPosition = -1;

        for (int i = 0; i < currentPageData.size(); i++) {
            ChartInfo info = currentPageData.get(i);
            if (info.isDrawLine) {
                time = formatSecond(info.time, "yyyy");
                paint.getTextBounds(time, 0, time.length(), textBounds);
                float startX = chartRight - candleWidth * i - candleWidth / 4 - (candleWidth - candleWidth / 4) / 2 - (textBounds.right - textBounds.left) / 2;
                float startY = lowerTop - (upperLowerSpace - (textBounds.bottom - textBounds.top)) / 2;

                if (prevPosition != -1) {
                    if (candleWidth * (i - prevPosition) >= textWidth) {
                        canvas.drawText(time, startX, startY, paint);
                    }
                } else {
                    canvas.drawText(time, startX, startY, paint);
                }
                prevPosition = i;
            }
        }

        List<ChartInfo> subPreChartInfo;
        if (previousPageData != null && !previousPageData.isEmpty()) {
            if (previousPageData.size() >= index) {
                subPreChartInfo = previousPageData.subList(previousPageData.size() - index, previousPageData.size());
            } else {
                subPreChartInfo = previousPageData.subList(index - previousPageData.size(), previousPageData.size());
            }
            for (int j = 0; j < subPreChartInfo.size(); j++) {
                ChartInfo data = subPreChartInfo.get(j);
                if (data.isDrawLine) {
                    time = formatSecond(data.time, "yyyy");
                    paint.getTextBounds(time, 0, time.length(), textBounds);
                    float startX = chartRight + candleWidth * (subPreChartInfo.size() - j) - candleWidth / 4 - (candleWidth - candleWidth / 4) / 2 - (textBounds.right - textBounds.left) / 2;
                    float startY = lowerTop - (upperLowerSpace - (textBounds.bottom - textBounds.top)) / 2;
                    canvas.drawText(time, startX, startY, paint);
                }
            }
        }

        List<ChartInfo> subNextChartInfo;
        if (nextPageData != null && !nextPageData.isEmpty()) {
            if (nextPageData.size() >= index) {
                subNextChartInfo = nextPageData.subList(0, index);
            } else {
                subNextChartInfo = nextPageData.subList(0, nextPageData.size());
            }
            for (int j = 0; j < subNextChartInfo.size(); j++) {
                ChartInfo data = subNextChartInfo.get(j);
                if (data.isDrawLine) {
                    time = formatSecond(data.time, "yyyy");
                    float startX = chartLeft - 1 - candleWidth * (j) - candleWidth / 4 - (candleWidth - candleWidth / 4) / 2 - (textBounds.right - textBounds.left) / 2;
                    float startY = lowerTop - (upperLowerSpace - (textBounds.bottom - textBounds.top)) / 2;
                    canvas.drawText(time, startX, startY, paint);
                }
            }
        }
    }
}
