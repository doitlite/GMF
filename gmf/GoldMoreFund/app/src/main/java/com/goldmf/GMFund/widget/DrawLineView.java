package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.controller.QuotationFragments;
import com.goldmf.GMFund.widget.ChartController.ChartInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_FIVE_DAY_TLINE;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment.TYPE_TIMES_TLINE;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_WHITE_COLOR;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;
import static com.goldmf.GMFund.widget.KLineChartBaseRender.DEFAULT_LOWER_LATITUDE_NUM;
import static com.goldmf.GMFund.widget.KLineChartBaseRender.DEFAULT_UPPER_LATITUDE_NUM;

/**
 * Created by Evan on 16/4/15 下午5:04.
 */
public class DrawLineView extends View {

    private Paint mPaint;
    private boolean isShowLine;
    private float xPosition;
    private ChartInfo mChartInfo;
    private int mChartType;
    private Rect mTextBounds;
    private int mChartSize;

    private float latitudeSpacing;
    private float upperLowerSpace;
    private float upperHeight;
    private float lowerHeight;
    private int mWidth;
    private int mHeight;

    public double preClose;
    public double mTimesHalf;
    public double maxTimesLine;
    public float mTimesRate;
    public double maxValue;
    public double minValue;
    public float kLineRate;
    public List<ChartInfo> visiblePageData;

    public DrawLineView(Context context) {
        this(context, null);
    }

    public DrawLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mTextBounds = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isShowLine) {
            drawXLine(canvas);
            drawText(canvas);
        }
        isShowLine = false;
    }


    public void drawLine(float x, ChartInfo info, List<ChartInfo> data, int chartType, int chartSize) {
        isShowLine = true;
        xPosition = x;
        mChartInfo = info;
        visiblePageData = data;
        mChartType = chartType;
        mChartSize = chartSize;
        invalidate();
    }


    private void drawXLine(Canvas canvas) {
        mPaint.setColor(BLACK_COLOR);
        mPaint.setStrokeWidth(1);
        canvas.drawLine(xPosition, 0, xPosition, getHeight(), mPaint);
    }

    private void drawText(Canvas canvas) {
        mWidth = getWidth();
        mHeight = getHeight();
        if (mChartSize == QuotationFragments.StockDetailFragment.TYPE_CHART_SMALL) {
            upperLowerSpace = dp2px(14);
        } else {
            upperLowerSpace = dp2px(24);
        }
        latitudeSpacing = (mHeight - upperLowerSpace) / (DEFAULT_UPPER_LATITUDE_NUM + DEFAULT_LOWER_LATITUDE_NUM + 2);
        upperHeight = latitudeSpacing * (DEFAULT_UPPER_LATITUDE_NUM + 1);
        lowerHeight = latitudeSpacing * (DEFAULT_LOWER_LATITUDE_NUM + 1);

        if (mChartInfo == null || visiblePageData == null || visiblePageData.isEmpty())
            return;

        if (mChartType == TYPE_TIMES_TLINE || mChartType == TYPE_FIVE_DAY_TLINE) {

            preClose = mChartInfo.preClose;
            List<Double> priceData = Stream.of(visiblePageData).filter(info -> info.last > -1).map(info -> info.last).collect(Collectors.toList());
            Pair<Double, Double> valueRange = recomputeRange(priceData);
            maxValue = valueRange.first;
            minValue = valueRange.second;

            if (Math.abs(maxValue - preClose) > Math.abs(minValue - preClose)) {
                mTimesHalf = Math.abs(maxValue - preClose);
                maxTimesLine = maxValue;
            } else if (Math.abs(maxValue - preClose) < Math.abs(minValue - preClose)) {
                mTimesHalf = Math.abs(minValue - preClose);
                maxTimesLine = minValue + 2 * mTimesHalf;
            } else {
                if (maxValue > preClose) {
                    mTimesHalf = Math.abs(maxValue - preClose);
                    maxTimesLine = maxValue;
                } else if (maxValue == preClose || minValue == preClose) {
                    mTimesHalf = preClose;
                    maxTimesLine = 2 * preClose;
                } else {
                    mTimesHalf = Math.abs(minValue - preClose);
                    maxTimesLine = preClose + mTimesHalf;
                }
            }
            mTimesRate = (float) (upperHeight / (2 * mTimesHalf));
        } else {

            Pair<Double, Double> valueRange = resetKLineUpperRange(visiblePageData);
            maxValue = valueRange.first;
            minValue = valueRange.second;
            kLineRate = (float) (upperHeight / (maxValue - minValue));
        }
        drawYPoint(canvas);

        if (mChartSize == QuotationFragments.StockDetailFragment.TYPE_CHART_SMALL) {
            drawTime(canvas);
        }
    }

    private void drawYPoint(Canvas canvas) {
        mPaint.setColor(BLACK_COLOR);
        mPaint.setStrokeWidth(4);
        mPaint.setStyle(Paint.Style.STROKE);
        float startY;
        if (mChartType == TYPE_TIMES_TLINE) {
            startY = (float) ((maxTimesLine - mChartInfo.last) * mTimesRate);
        } else {
            startY = (float) ((maxValue - mChartInfo.close) * kLineRate + 1);
        }
        canvas.drawCircle(xPosition, startY, 2, mPaint);
    }

    private void drawTime(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(0);
        mPaint.setTextSize(sp2px(8));
        mPaint.setColor(TEXT_BLACK_COLOR);
        String text;
        if (mChartType == TYPE_TIMES_TLINE) {
            text = formatSecond(mChartInfo.time, "HH:mm");
        } else if (mChartType == TYPE_FIVE_DAY_TLINE) {
            text = formatSecond(mChartInfo.time, "MM-dd HH:mm");
        } else {
            text = formatSecond(mChartInfo.time, "yyyy-MM-dd");
        }
        mPaint.getTextBounds(text, 0, text.length(), mTextBounds);
        float x = xPosition - (mTextBounds.right - mTextBounds.left) / 2;
        if (xPosition < (mTextBounds.right - mTextBounds.left) / 2) {
            x = dp2px(4);
        } else if (xPosition > getWidth() - (mTextBounds.right - mTextBounds.left)) {
            x = getWidth() - (mTextBounds.right - mTextBounds.left) - dp2px(4);
        }
        float y = mTextBounds.bottom - mTextBounds.top + dp2px(4);
        canvas.drawRect(x - dp2px(4), mTextBounds.top + dp2px(4), x + (mTextBounds.right - mTextBounds.left) + dp2px(4), y + dp2px(4), mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(0);
        mPaint.setTextSize(sp2px(8));
        mPaint.setColor(TEXT_WHITE_COLOR);
        canvas.drawText(text, x, y, mPaint);
    }

    public Pair<Double, Double> resetKLineUpperRange(List<ChartInfo> data) {
        List<Double> maxList = Stream.of(data).map(info -> info.max).collect(Collectors.toList());
        List<Double> minList = Stream.of(data).map(info -> info.min).collect(Collectors.toList());
        List<Double> ma5List = Stream.of(data).map(info -> info.ma5).filter(info -> info > 0).collect(Collectors.toList());
        List<Double> ma10List = Stream.of(data).map(info -> info.ma10).filter(info -> info > 0).collect(Collectors.toList());
        List<Double> ma20List = Stream.of(data).map(info -> info.ma20).filter(info -> info > 0).collect(Collectors.toList());

        List<Double> datas = new ArrayList<>();
        datas.addAll(maxList);
        datas.addAll(minList);
        datas.addAll(ma5List);
        datas.addAll(ma10List);
        datas.addAll(ma20List);

        return recomputeRange(datas);
    }

    private Pair<Double, Double> recomputeRange(List<Double> data) {
        ArrayList<Double> subData = new ArrayList<>(data);
        Collections.sort(subData, (lhs, rhs) -> {
            double ret = rhs - lhs;
            if (ret > 0) {
                return 1;
            } else if (ret < 0) {
                return -1;
            } else
                return 0;
        });
        double maxValue = subData.get(0);

        Collections.sort(subData, (lhs, rhs) -> {
            double ret = lhs - rhs;
            if (ret > 0) {
                return 1;
            } else if (ret < 0) {
                return -1;
            } else
                return 0;
        });
        double minValue = subData.get(0);

        return Pair.create(maxValue, minValue);
    }
}
