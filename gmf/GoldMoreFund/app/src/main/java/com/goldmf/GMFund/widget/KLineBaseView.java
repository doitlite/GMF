package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.model.LineData.KLineData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.GREEN_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.util.FormatUtil.formatBigNumber;

/**
 * Created by Evan on 16/3/23 下午4:50.
 */
public class KLineBaseView extends View {

    public static final PathEffect DEFAULT_DASH_EFFECT = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
    public static final int MIN_SHOW_DATA_COUNT = 80;
    public static final int DEFAULT_UPPER_LOWER_SPACE = dp2px(14);
    public static final int DEFAULT_UPER_LATITUDE_NUM = 2;
    public static final int DEFAULT_LOWER_LATITUDE_NUM = 0;

    public int mMinCount;
    public float candleWidth;
    public float latitudeSpacing;
    public float longitudeSpacing;
    public int mLogitudeNumber;
    public float upperLowerSpace = DEFAULT_UPPER_LOWER_SPACE;


    public double maxValue;
    public double minValue;
    public float upperRate;
    public double maxAvg;
    public double minAvg;
    public float avgRate;
    public double maxVolume;
    public double minVolume;
    public float lowerRate;

    public List<KLineData> mKLineDatas;

    public float upperTop;
    public float upperBottom;
    public float upperHeight;
    public float lowerTop;
    public float lowerBottom;
    public float lowerHeight;
    public float chartLeft;
    public float chartRight;


    public KLineBaseView(Context context) {
        this(context, null);
    }

    public KLineBaseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KLineBaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mKLineDatas == null || mKLineDatas.isEmpty())
            return;

        int width = getWidth();
        int height = getHeight();
        chartLeft = 0;
        upperTop = 0;
        chartRight = width - 1;
        lowerBottom = height - 1;

        initData();

        latitudeSpacing = (height - upperLowerSpace) / (DEFAULT_UPER_LATITUDE_NUM + DEFAULT_LOWER_LATITUDE_NUM + 2);
        upperHeight = latitudeSpacing * (DEFAULT_UPER_LATITUDE_NUM + 1);
        upperBottom = upperHeight;
        lowerHeight = latitudeSpacing * (DEFAULT_LOWER_LATITUDE_NUM + 1);
        lowerTop = height - lowerHeight;

        candleWidth = (chartRight - chartLeft) * 10.0f / 10.0f / mMinCount;
        upperRate = (float) (upperHeight / (maxValue - minValue));
        avgRate = (float) (upperHeight / (maxAvg - minAvg));
        lowerRate = (float) (lowerHeight / maxVolume);

        drawKLineRegion(canvas);
        drawMAAvgRegion(canvas);
        drawLowerRegion(canvas);
        drawLongitudeLine(canvas);
        drawYAxis(canvas);
        drawXAxis(canvas);
        drawBorders(canvas);
    }

    public void initData() {
        mMinCount = mKLineDatas.size();
    }

    public void setPriceData(List<KLineData> data, int startIdx, int count) {
        mKLineDatas = data;
        if (data != null && !data.isEmpty()) {
            resetValueRange(startIdx, count);
            resetMARange(startIdx, count);
            resetVolumeRange(startIdx, count);
        }
    }

    public void resetValueRange(int startIdx, int count) {
        Pair<Double, Double> valueRange = recomputeValueRange(mKLineDatas, startIdx, count);
        maxValue = valueRange.first;
        minValue = valueRange.second;
    }

    private Pair<Double, Double> recomputeValueRange(List<KLineData> data, int startIdx, int count) {
        List<KLineData> subData = new ArrayList<>(data.subList(startIdx, startIdx + count));
        Collections.sort(subData, (lhs, rhs) -> {
            double ret = rhs.max - lhs.max;
            if (ret > 0) {
                return 1;
            } else if (ret < 0) {
                return -1;
            } else
                return 0;
        });
        double maxValue = subData.get(0).max;

        Collections.sort(subData, (lhs, rhs) -> {
            double ret = lhs.min - rhs.min;
            if (ret > 0) {
                return 1;
            } else if (ret < 0) {
                return -1;
            } else
                return 0;
        });
        double minValue = subData.get(0).min;

        for (int i = 0; i < data.size(); i++) {
            KLineData info = data.get(i);
//            Log.e("AAAAAA", String.format("index %d prevClose %f max %f min %f open %f close %f", i, info.prevClose, info.max, info.min, info.open, info.close));
        }
//        Log.e("AAAAAA", String.format("mMaxValue %f", mMaxValue));
//        Log.e("AAAAAA", String.format("mMinValue %f", mMinValue));

        return Pair.create(maxValue, minValue);
    }

    public void resetMARange(int startIdx, int count) {
        List<Double> ma5List = Stream.of(mKLineDatas).map(info -> info.ma5).collect(Collectors.toList());
        List<Double> ma10List = Stream.of(mKLineDatas).map(info -> info.ma10).collect(Collectors.toList());
        List<Double> ma20List = Stream.of(mKLineDatas).map(info -> info.ma20).collect(Collectors.toList());
        List<List<Double>> data = new ArrayList<>();
        data.add(ma5List);
        data.add(ma10List);
        data.add(ma20List);

        List<Pair<Double, Double>> rangeData = new ArrayList<>();
        rangeData.add(computeRange(ma5List, startIdx, startIdx + count));
        rangeData.add(computeRange(ma10List, startIdx, startIdx + count));
        rangeData.add(computeRange(ma20List, startIdx, startIdx + count));

        Pair<Double, Double> valueRange = computeMARange(rangeData);
        maxAvg = valueRange.first;
        minAvg = valueRange.second;
    }

    private static Pair<Double, Double> computeMARange(List<Pair<Double, Double>> data) {
        ArrayList<Pair<Double, Double>> subData = new ArrayList<>(data);

        Collections.sort(subData, (lhs, rhs) -> {
            double ret = rhs.first - lhs.first;
            if (ret > 0) {
                return 1;
            } else if (ret < 0) {
                return -1;
            } else
                return 0;
        });
        double maxValue = subData.get(0).first;

        Collections.sort(subData, (lhs, rhs) -> {
            double ret = lhs.second - rhs.second;
            if (ret > 0) {
                return 1;
            } else if (ret < 0) {
                return -1;
            } else
                return 0;
        });
        double minValue = subData.get(0).second;

        return Pair.create(maxValue, minValue);
    }

    public void resetVolumeRange(int startIdx, int count) {
        List<Double> data = Stream.of(mKLineDatas).map(info -> info.volume).collect(Collectors.toList());

        Pair<Double, Double> valueRange = computeRange(data, startIdx, count);
        maxVolume = valueRange.first;
        minVolume = valueRange.second;
    }

    private static Pair<Double, Double> computeRange(List<Double> data, int startIdx, int count) {
        ArrayList<Double> subData = new ArrayList<>(data.subList(startIdx, startIdx + count));

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

    private void drawKLineRegion(Canvas canvas) {

        Paint paint = new Paint();

        for (int i = 0; i < mKLineDatas.size() && i < mMinCount; i++) {
            KLineData info = mKLineDatas.get(i);
            float open = (float) ((maxValue - info.open) * upperRate + 1);
            float close = (float) ((maxValue - info.close) * upperRate + 1);
            float high = (float) ((maxValue - info.max) * upperRate + 1);
            float low = (float) ((maxValue - info.min) * upperRate + 1);

            float left = candleWidth * (i);
            float right = candleWidth * (i + 1) - candleWidth / 4;
            float startX = candleWidth * (i + 1) - candleWidth / 4 - (candleWidth - candleWidth / 4) / 2;
            if (open < close) {
                paint.setColor(GREEN_COLOR);
                canvas.drawRect(left, open, right, close, paint);
                canvas.drawLine(startX, high, startX, low, paint);
            } else if (open == close) {
                paint.setColor(RED_COLOR);
                canvas.drawRect(left, open, right, open, paint);
                canvas.drawLine(startX, high, startX, low, paint);
            } else if (open > close) {
                paint.setColor(RED_COLOR);
                canvas.drawRect(left, close, right, open, paint);
                canvas.drawLine(startX, high, startX, low, paint);
            }
        }
    }

    public void drawMAAvgRegion(Canvas canvas) {
        Paint paint = new Paint();

        List<Double> ma5List = Stream.of(mKLineDatas).map(info -> info.ma5).collect(Collectors.toList());
        List<Double> ma10List = Stream.of(mKLineDatas).map(info -> info.ma10).collect(Collectors.toList());
        List<Double> ma20List = Stream.of(mKLineDatas).map(info -> info.ma20).collect(Collectors.toList());

        // init ma5
        {
            paint.setColor(Color.parseColor("#3498DB"));
            paint.setStrokeWidth(2);
            float startX = chartLeft;
            float startY = 0;
            float endY = 0;
            for (int i = 0; i < ma5List.size(); i++) {
                endY = (float) ((maxAvg - ma5List.get(i)) * avgRate);
                if (i != 0) {
                    canvas.drawLine(startX, startY, chartLeft + candleWidth * (i + 1) - candleWidth / 2, endY, paint);
                }
                startX = chartLeft + candleWidth * (i + 1) - candleWidth / 2;
                startY = endY;
            }
        }

        // init ma10
        {
            paint.setColor(Color.parseColor("#FFDE00"));
            paint.setStrokeWidth(2);
            float startX = chartLeft;
            float startY = 0;
            float endY = 0;
            for (int i = 0; i < ma10List.size(); i++) {
                endY = (float) ((maxAvg - ma10List.get(i)) * avgRate);
                if (i != 0) {
                    canvas.drawLine(startX, startY, chartLeft + candleWidth * (i + 1) - candleWidth / 2, endY, paint);
                }
                startX = chartLeft + candleWidth * (i + 1) - candleWidth / 2;
                startY = endY;

            }
        }

        // init ma20
        {
            paint.setColor(Color.parseColor("#9B59B6"));
            paint.setStrokeWidth(2);
            float startX = chartLeft;
            float startY = 0;
            float endY = 0;
            for (int i = 0; i < ma20List.size(); i++) {
                endY = (float) ((maxAvg - ma20List.get(i)) * avgRate);
                if (i != 0) {
                    canvas.drawLine(startX, startY, chartLeft + candleWidth * (i + 1) - candleWidth / 2, endY, paint);
                }
                startX = chartLeft + candleWidth * (i + 1) - candleWidth / 2;
                startY = endY;
            }
        }
    }

    public void drawLowerRegion(Canvas canvas) {

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < mKLineDatas.size(); i++) {
            KLineData info = mKLineDatas.get(i);
            float left = chartLeft + candleWidth * i;
            float top = (float) (lowerTop + (maxVolume - info.volume) * lowerRate);
            float right = candleWidth * (i + 1) - candleWidth / 4;
            float bottom = lowerBottom;

            paint.setColor(info.open > info.close ? Color.parseColor("#E74C3C") : Color.parseColor("#2ECC71"));
            canvas.drawRect(left, top, right, bottom, paint);
        }
    }


    public void drawBorders(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(GREY_COLOR);
        paint.setStrokeWidth(1);
        canvas.drawLine(chartLeft, upperTop, chartLeft, upperBottom, paint);
        canvas.drawLine(chartLeft, upperTop, chartRight, upperTop, paint);
        canvas.drawLine(chartRight, upperTop, chartRight, upperBottom, paint);
        canvas.drawLine(chartLeft, upperBottom, chartRight, upperBottom, paint);

        canvas.drawLine(chartLeft, lowerTop, chartLeft, lowerBottom, paint);
        canvas.drawLine(chartLeft, lowerTop, chartRight, lowerTop, paint);
        canvas.drawLine(chartRight, lowerTop, chartRight, lowerBottom, paint);
        canvas.drawLine(chartLeft, lowerBottom, chartRight, lowerBottom, paint);
    }

    public void drawLongitudeLine(Canvas canvas) {

    }

    public void drawXAxis(Canvas canvas) {

    }

    public void drawYAxis(Canvas canvas) {

        // 绘制Y轴
        Paint paint = new Paint();
        paint.setTextSize(sp2px(8));
        paint.setColor(TEXT_BLACK_COLOR);
        Rect textBounds = new Rect();

        String text = formatBigNumber(minValue, false, 2, 2, false);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, dp2px(2), upperBottom - dp2px(2), paint);

        text = formatBigNumber(maxValue, false, 2, 2, false);
        paint.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, dp2px(2), (textBounds.bottom - textBounds.top) + 2, paint);

    }


}
