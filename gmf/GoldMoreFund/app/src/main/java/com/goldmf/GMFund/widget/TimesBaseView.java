package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.controller.StockChartFragments.TimesChartInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.GREEN_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_BACKGROUND_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;

/**
 * Created by Evan on 16/3/23 下午4:50.
 */
public class TimesBaseView extends View {

    public static final PathEffect DEFAULT_DASH_EFFECT = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
    public static final int MIN_SHOW_DATA_COUNT = 4 * 60;
    public static final int DEFAULT_UPPER_LOWER_SPACE = dp2px(14);
    public static final int DEFAULT_UPER_LATITUDE_NUM = 2;
    private static final int DEFAULT_LOWER_LATITUDE_NUM = 0;

    public int mDataCount;
    public float dataSpacing;
    public float latitudeSpacing;
    public int mLogitudeNumber;
    public float upperLowerSpace = DEFAULT_UPPER_LOWER_SPACE;

    public double mMaxPrice;
    public double mMinPrice;
    public float upperRate;
    public double mMaxPriceLine;
    public double mMaxVolume;
    public double mMinVolume;
    public float lowerRate;
    public double mPreClose;
    public double mOpenPrice;
    public double mUpperHalfHigh;

    protected List<TimesChartInfo> mData;
    protected List<Double> mPriceData;
    protected List<Double> mAverageData;
    protected List<Double> mVolumeData;

    public float upperTop;
    public float upperBottom;
    public float upperHeight;
    public float lowerTop;
    public float lowerBottom;
    public float lowerHeight;
    public float chartLeft;
    public float chartRight;
    public Paint paint;
    public Path path;
    private float mDownX;
    private float mMoveX;


    public TimesBaseView(Context context) {
        this(context, null);
    }

    public TimesBaseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimesBaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        path = new Path();
        paint.setColor(LINE_BACKGROUND_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mData == null || mData.isEmpty())
            return;

        int width = getWidth();
        int height = getHeight();
        chartLeft = 0;
        upperTop = 0;
        chartRight = width - 1;
        lowerBottom = height - 1;

        init();

        mDataCount = mData.size();
        dataSpacing = (chartRight - chartLeft) * 1.0f / mDataCount;
        latitudeSpacing = (height - upperLowerSpace) / (DEFAULT_UPER_LATITUDE_NUM + DEFAULT_LOWER_LATITUDE_NUM + 2);

        upperHeight = latitudeSpacing * (DEFAULT_UPER_LATITUDE_NUM + 1);
        upperBottom = upperHeight;
        lowerHeight = latitudeSpacing * (DEFAULT_LOWER_LATITUDE_NUM + 1);
        lowerTop = height - lowerHeight;

        mPreClose = mData.get(0).prevClose;
        mOpenPrice = mData.get(0).last;
        mPriceData = Stream.of(mData).filter(info -> info.last > -1).map(info -> info.last).collect(Collectors.toList());
        mAverageData = Stream.of(mData).filter(info -> info.last > -1).map(info -> info.average).collect(Collectors.toList());
        mVolumeData = Stream.of(mData).filter(info -> info.last > -1).map(info -> info.volume).collect(Collectors.toList());

        recomputePriceRange(mPriceData, mPreClose);
        resetVolumeRange(mVolumeData);

        if (upperHeight > 0)
            upperRate = (float) (upperHeight / (2 * mUpperHalfHigh));

        if (lowerHeight > 0)
            lowerRate = (float) (lowerHeight / mMaxVolume);

        drawUpperLines(canvas);
        drawPreCloseLine(canvas);
        drawAverageLine(canvas);
        drawLowerRegion(canvas);
        drawYAxis(canvas);
        drawXAxis(canvas);
        drawBorders(canvas);
    }

    public void init() {

    }

    public void setData(List<TimesChartInfo> data) {
        mData = data;
    }

    private void recomputePriceRange(List<Double> data, double preClose) {
        Pair<Double, Double> pair = recomputeRange(data);
        mMaxPrice = pair.first;
        mMinPrice = pair.second;
        mUpperHalfHigh = Math.abs(mMaxPrice - preClose) > Math.abs(mMinPrice - preClose) ? Math.abs(mMaxPrice - preClose) : Math.abs(mMinPrice - preClose);
        mMaxPriceLine = Math.abs(mMaxPrice - preClose) > Math.abs(mMinPrice - preClose) ? mMaxPrice : mMinPrice + 2 * mUpperHalfHigh;
        Log.e("CCCC", String.format("preClose %f maxPrice %f minPrice %f mMaxPriceLine %f mUpperHalfHigh %f",preClose, mMaxPrice, mMinPrice, mMaxPriceLine, mUpperHalfHigh));
    }

    private void resetVolumeRange(List<Double> data) {
        Pair<Double, Double> valueRange = recomputeRange(data);
        mMaxVolume = valueRange.first;
        mMinVolume = valueRange.second;
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

    public void drawPreCloseLine(Canvas canvas) {
        paint.setColor(LINE_BACKGROUND_COLOR);
        paint.setPathEffect(DEFAULT_DASH_EFFECT);
        paint.setStyle(Paint.Style.STROKE);
        path.reset();
        path.moveTo(chartLeft, (float) (mUpperHalfHigh * upperRate));
        path.lineTo(chartRight, (float) (mUpperHalfHigh * upperRate));
        canvas.drawPath(path, paint);
        paint.setPathEffect(null);
    }

    public void drawUpperLines(Canvas canvas) {
        float startX = chartLeft;
        float startY = 0;
        float endY = 0;

        paint.setColor(Color.parseColor("#3498DB"));
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);
        path.reset();

        for (int i = 0; i < mPriceData.size(); i++) {
            endY = (float) ((mMaxPriceLine - mPriceData.get(i)) * upperRate);
            if (i == 0) {
                path.moveTo(startX, endY);
            } else {
                path.lineTo(chartLeft + dataSpacing * i, endY);
                canvas.drawLine(startX, startY, chartLeft + dataSpacing * i, endY, paint);
            }
            startX = chartLeft + dataSpacing * i;
            startY = endY;
        }

        paint.setColor(Color.parseColor("#E3EFFF"));
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        path.lineTo(chartLeft + dataSpacing * (mPriceData.size() - 1), upperHeight);
        path.lineTo(chartLeft, upperHeight);
        path.close();
        canvas.drawPath(path, paint);
    }

    public void drawAverageLine(Canvas canvas) {
        float startX = chartLeft;
        float startY = 0;
        float endY = 0;

        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#FFDE00"));
        for (int i = 0; i < mAverageData.size(); i++) {
            endY = (float) ((mMaxPriceLine - mAverageData.get(i)) * upperRate);
            if (i != 0) {
                canvas.drawLine(startX, startY, chartLeft + dataSpacing * i, endY, paint);
            }
            startX = chartLeft + dataSpacing * i;
            startY = endY;
        }
    }

    public void drawLowerRegion(Canvas canvas) {
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < mVolumeData.size(); i++) {
            float left = chartLeft + dataSpacing * i;
            float top = (float) (lowerTop + (mMaxVolume - mVolumeData.get(i)) * lowerRate);
            float right = chartLeft + dataSpacing * (i + 1) - dataSpacing / 4;
            float bottom = lowerBottom;

            if (i == 0) {
                paint.setColor(mOpenPrice > mPreClose ? RED_COLOR : GREEN_COLOR);
                canvas.drawRect(left, top, right, bottom, paint);
            } else {
                paint.setColor(mPriceData.get(i) > mPriceData.get(i - 1) ? RED_COLOR : GREEN_COLOR);
                canvas.drawRect(left, top, right, bottom, paint);
            }
        }
    }

    public void drawBorders(Canvas canvas) {
        paint.setColor(LINE_BACKGROUND_COLOR);
        paint.setStyle(Paint.Style.STROKE);
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

    public void drawXAxis(Canvas canvas) {

    }

    public void drawYAxis(Canvas canvas) {

    }

}
