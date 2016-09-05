package com.goldmf.GMFund.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.view.View;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment;
import com.goldmf.GMFund.controller.StockChartFragments;
import com.goldmf.GMFund.util.SecondUtil;
import com.goldmf.GMFund.widget.ChartController.ChartInfo;
import com.goldmf.GMFund.widget.ChartController.EnvDataPager;

import java.util.ArrayList;
import java.util.List;

import io.yale.infinitychartview.lib.ChartViewContainer.RenderDelegate;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.GREEN_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_BACKGROUND_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_BLUE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_PURPLE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_YELLOW_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.model.LineData.KLineData.Spec_Type_BOLL;
import static com.goldmf.GMFund.model.LineData.KLineData.Spec_Type_KDJ;
import static com.goldmf.GMFund.model.LineData.KLineData.Spec_Type_MACD;
import static com.goldmf.GMFund.model.LineData.KLineData.Spec_Type_NONE;
import static com.goldmf.GMFund.model.LineData.KLineData.Spec_Type_RSI;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;


/**
 * Created by Evan on 16/1/29 下午3:29.
 */
public class KLineChartBaseRender implements RenderDelegate<ChartController.ChartInfo> {

    public static final PathEffect DEFAULT_DASH_EFFECT = new DashPathEffect(new float[]{5, 5, 5, 5}, 1);
    public static final int DEFAULT_UPPER_LOWER_SPACE = dp2px(24);
    public static final int DEFAULT_UPPER_LATITUDE_NUM = 2;
    public static final int DEFAULT_LOWER_LATITUDE_NUM = 0;
    public static final String CHART_CACAHE_IMAGE_NAME = "chart_image_cache";

    public EnvDataPager mEnvDataPager;
    public List<ChartController.ChartInfo> mVisibleData;
    public List<Double> mPriceData;
    public List<Double> mVolumeData;
    public int mPagerDataCount;

    public int mWidth;
    public int mHeight;
    public float upperTop;
    public float upperBottom;
    public float upperHeight;
    public float lowerTop;
    public float lowerBottom;
    public float lowerHeight;
    public float chartLeft;
    public float chartRight;

    public float candleWidth;
    public float latitudeSpacing;
    public float upperLowerSpace = DEFAULT_UPPER_LOWER_SPACE;

    public Paint paint;
    public Path path;
    public Bitmap cacheBitmap;
    public Bitmap newBitmap;

    public double maxValue;
    public double minValue;
    public float kLineRate;
    public double maxVolume;
    public double minVolume;
    public float lowerRate;
    public double maxMACD;
    public double minMACD;
    public float macdRate;
    public double mMACDHalf;
    public double maxMACDLine;
    public double maxKDJ;
    public double minKDJ;
    public float kdjRate;
    public double maxRSI;
    public double minRSI;
    public float rsiRate;
    public double maxBoll;
    public double minBoll;
    public float bollRate;
    public float boll2Rate;

    public double maxPrice;
    public double minPrice;
    public double preClose;
    public double openPrice;
    public double mTimesHalf;
    public double maxTimesLine;
    public float mTimesRate;
    public View mView;
    public Matrix matrix;

    public long lastTime;
    private boolean isDrawingNew = false;

    public KLineChartBaseRender() {
        paint = new Paint();
        path = new Path();
        matrix = new Matrix();
        paint.setColor(LINE_BACKGROUND_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setAntiAlias(true);
    }

    public void setEnvData(EnvDataPager envDataPager) {
        mEnvDataPager = envDataPager;
    }

    @Override
    public void onDraw(List<ChartController.ChartInfo> visibleData, List<ChartController.ChartInfo> currentPageData,
                       List<ChartController.ChartInfo> previousPageData, List<ChartController.ChartInfo> nextPageData, int pagerDataCount, View view, Canvas canvas) {

        if (currentPageData.isEmpty()) {
            mVisibleData = null;
            return;
        }
        mVisibleData = visibleData;
        mPagerDataCount = pagerDataCount;
        mView = view;
        view.setBackgroundColor(WHITE_COLOR);
        mWidth = view.getWidth();
        mHeight = view.getHeight();
        chartLeft = 0;
        upperTop = 0;
        chartRight = mWidth;
        lowerBottom = mHeight;

        init();
        latitudeSpacing = (mHeight - upperLowerSpace) / (DEFAULT_UPPER_LATITUDE_NUM + DEFAULT_LOWER_LATITUDE_NUM + 2);
        upperHeight = latitudeSpacing * (DEFAULT_UPPER_LATITUDE_NUM + 1);
        upperBottom = upperHeight;
        lowerHeight = latitudeSpacing * (DEFAULT_LOWER_LATITUDE_NUM + 1);
        lowerTop = mHeight - lowerHeight;

        if (mEnvDataPager != null) {
            if (mEnvDataPager.isCacheBitmap) {
                if (cacheBitmap == null) {
                    cacheBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                    Canvas cacheCanvas = new Canvas(cacheBitmap);
                    drawCacheChart(visibleData, currentPageData, previousPageData, nextPageData, pagerDataCount, cacheCanvas);
                } else {
                    if (SecondUtil.currentSecond() - lastTime >= 10 && !isDrawingNew) {
                        isDrawingNew = true;
                        ArrayList<ChartInfo> visibleDataCopy = new ArrayList<>(visibleData);
                        ArrayList<ChartInfo> currentDataCopy = new ArrayList<>(currentPageData);
                        ArrayList<ChartInfo> previousDataCopy = new ArrayList<>(previousPageData);
                        ArrayList<ChartInfo> nextDataCopy = new ArrayList<>(nextPageData);
                        Observable.create(sub -> {
                            newBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                            Canvas newCanvas = new Canvas(newBitmap);
                            drawCacheChart(visibleDataCopy, currentDataCopy, previousDataCopy, nextDataCopy, pagerDataCount, newCanvas);
                            cacheBitmap = newBitmap;
                        })
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(nil -> {
                                    view.invalidate();
                                    isDrawingNew = false;
                                });
                    }
                }
                canvas.drawBitmap(cacheBitmap, matrix, paint);
            } else {
                drawChart(visibleData, currentPageData, previousPageData, nextPageData, pagerDataCount, canvas);
            }
        }
    }

    public void drawChart(List<ChartController.ChartInfo> visibleData, List<ChartController.ChartInfo> currentPageData,
                          List<ChartController.ChartInfo> previousPageData, List<ChartController.ChartInfo> nextPageData, int pagerDataCount, Canvas canvas) {
        if (mEnvDataPager.mChartType == StockDetailFragment.TYPE_TIMES_TLINE) {

            candleWidth = (chartRight - chartLeft) / (pagerDataCount - 1);
            preClose = currentPageData.get(0).preClose;
            openPrice = currentPageData.get(0).last;
            mPriceData = Stream.of(currentPageData).filter(info -> info.last > -1).map(info -> info.last).collect(Collectors.toList());
            mVolumeData = Stream.of(currentPageData).filter(info -> info.last > -1).map(info -> info.volume).collect(Collectors.toList());

            mEnvDataPager.recomputePriceRange(mPriceData);
            maxValue = mEnvDataPager.mMaxValue;
            minValue = mEnvDataPager.mMinValue;
            if (Math.abs(maxValue - preClose) > Math.abs(minValue - preClose)) {
                mTimesHalf = Math.abs(maxValue - preClose);
                maxTimesLine = maxValue;
                maxPrice = maxValue;
                minPrice = maxPrice - 2 * mTimesHalf;
            } else if (Math.abs(maxValue - preClose) < Math.abs(minValue - preClose)) {
                mTimesHalf = Math.abs(minValue - preClose);
                maxTimesLine = minValue + 2 * mTimesHalf;
                minPrice = minValue;
                maxPrice = maxTimesLine;
            } else {
                if (maxValue > preClose) {
                    mTimesHalf = Math.abs(maxValue - preClose);
                    maxTimesLine = maxValue;
                    maxPrice = maxValue;
                    minPrice = maxValue - 2 * mTimesHalf;
                } else if (maxValue == preClose || minValue == preClose) {
                    mTimesHalf = preClose;
                    maxTimesLine = 2 * preClose;
                    minPrice = minValue;
                    maxPrice = maxValue;
                } else {
                    mTimesHalf = Math.abs(minValue - preClose);
                    maxTimesLine = preClose+mTimesHalf;
                    minPrice = minValue;
                    maxPrice = maxValue;
                }
            }
            mTimesRate = (float) (upperHeight / (2 * mTimesHalf));

            mEnvDataPager.resetTimesVolumeRange(mVolumeData);
            maxVolume = mEnvDataPager.mMaxVolume;
            minVolume = mEnvDataPager.mMinVolume;
            lowerRate = (float) (lowerHeight / maxVolume);

            drawTimesLineRegion(mPriceData, canvas);
            drawPreCloseLine(canvas);
            drawAverageLine(currentPageData, canvas);
            drawTimesVolumeRegion(mVolumeData, mPriceData, preClose, openPrice, canvas);

        } else if (mEnvDataPager.mChartType == StockDetailFragment.TYPE_KLINE) {
            candleWidth = (chartRight - chartLeft) / pagerDataCount;

            mEnvDataPager.resetKLineUpperRange(visibleData);
            maxValue = mEnvDataPager.mMaxValue;
            minValue = mEnvDataPager.mMinValue;
            kLineRate = (float) (upperHeight / (maxValue - minValue));

            if (mEnvDataPager.mSpecType == Spec_Type_NONE) {
                mEnvDataPager.resetKLineVolumeRange(visibleData);
                maxVolume = mEnvDataPager.mMaxVolume;
                minVolume = mEnvDataPager.mMinVolume;
                lowerRate = (float) (lowerHeight / maxVolume);

            } else if (mEnvDataPager.mSpecType == Spec_Type_MACD) {
                mEnvDataPager.resetMACDRange(visibleData);
                maxMACD = mEnvDataPager.mMaxMACD;
                minMACD = mEnvDataPager.mMinMACD;
                mMACDHalf = Math.abs(maxMACD) > Math.abs(minMACD) ? Math.abs(maxMACD) : Math.abs(minMACD);
                maxMACDLine = Math.abs(maxMACD) > Math.abs(minMACD) ? Math.abs(maxMACD) : Math.abs(minMACD + 2 * mMACDHalf);
                macdRate = (float) (lowerHeight / (2 * mMACDHalf));
            } else if (mEnvDataPager.mSpecType == Spec_Type_KDJ) {
                mEnvDataPager.resetKDJRange(visibleData);
                maxKDJ = mEnvDataPager.mMaxKDJ;
                minKDJ = mEnvDataPager.mMinKDJ;
                maxKDJ = maxKDJ > 100 ? maxKDJ : 100;
                minKDJ = minKDJ < 0 ? minKDJ : 0;
                kdjRate = (float) (lowerHeight / (maxKDJ - minKDJ));
            } else if (mEnvDataPager.mSpecType == Spec_Type_RSI) {
                mEnvDataPager.resetRSIRange(visibleData);
                maxRSI = mEnvDataPager.mMaxRSI;
                minRSI = mEnvDataPager.mMinRSI;
                rsiRate = (float) (lowerHeight / (maxRSI - minRSI));
            } else if (mEnvDataPager.mSpecType == Spec_Type_BOLL) {
                mEnvDataPager.resetBOLLRange(visibleData);
                maxBoll = mEnvDataPager.mMaxBoll;
                minBoll = mEnvDataPager.mMinBoll;
                bollRate = (float) (lowerHeight / (maxBoll - minBoll));
                boll2Rate = (float) (lowerHeight / (maxValue - minValue));
            }
            drawKLineRegion(currentPageData, canvas);
            drawMAAvgRegion(currentPageData, previousPageData, nextPageData, canvas);
            drawLowerRegion(currentPageData, previousPageData, nextPageData, canvas);
        }

        drawBorders(canvas);
        drawXAxis(currentPageData, previousPageData, nextPageData, canvas);
        drawLongitudeLine(currentPageData, previousPageData, nextPageData, canvas);
        StockChartFragments.StockChartDetailFragment.sRefreshChartValueSubject.onNext(null);
    }

    public void drawCacheChart(List<ChartController.ChartInfo> visibleData, List<ChartController.ChartInfo> currentPageData,
                               List<ChartController.ChartInfo> previousPageData, List<ChartController.ChartInfo> nextPageData, int pagerDataCount, Canvas cacheCanvas) {
        lastTime = SecondUtil.currentSecond();
        if (mEnvDataPager.mChartType == StockDetailFragment.TYPE_TIMES_TLINE) {

            candleWidth = (chartRight - chartLeft) / (pagerDataCount - 1);
            preClose = currentPageData.get(0).preClose;
            openPrice = currentPageData.get(0).last;
            mPriceData = Stream.of(currentPageData).filter(info -> info.last > -1).map(info -> info.last).collect(Collectors.toList());
            mVolumeData = Stream.of(currentPageData).filter(info -> info.last > -1).map(info -> info.volume).collect(Collectors.toList());

            mEnvDataPager.recomputePriceRange(mPriceData);
            maxValue = mEnvDataPager.mMaxValue;
            minValue = mEnvDataPager.mMinValue;
            if (Math.abs(maxValue - preClose) > Math.abs(minValue - preClose)) {
                mTimesHalf = Math.abs(maxValue - preClose);
                maxTimesLine = maxValue;
                maxPrice = maxValue;
                minPrice = maxPrice - 2 * mTimesHalf;
            } else if (Math.abs(maxValue - preClose) < Math.abs(minValue - preClose)) {
                mTimesHalf = Math.abs(minValue - preClose);
                maxTimesLine = minValue + 2 * mTimesHalf;
                minPrice = minValue;
                maxPrice = maxTimesLine;
            } else {
                if (maxValue > preClose) {
                    mTimesHalf = Math.abs(maxValue - preClose);
                    maxTimesLine = maxValue;
                    maxPrice = maxValue;
                    minPrice = maxValue - 2 * mTimesHalf;
                } else if (maxValue == preClose || minValue == preClose) {
                    mTimesHalf = preClose;
                    maxTimesLine = 2 * preClose;
                    minPrice = minValue;
                    maxPrice = maxValue;
                } else {
                    mTimesHalf = Math.abs(minValue - preClose);
                    maxTimesLine = preClose+mTimesHalf;
                    minPrice = minValue;
                    maxPrice = maxValue;
                }
            }
            mTimesRate = (float) (upperHeight / (2 * mTimesHalf));

            mEnvDataPager.resetTimesVolumeRange(mVolumeData);
            maxVolume = mEnvDataPager.mMaxVolume;
            minVolume = mEnvDataPager.mMinVolume;
            lowerRate = (float) (lowerHeight / maxVolume);

            drawTimesLineRegion(mPriceData, cacheCanvas);
            drawPreCloseLine(cacheCanvas);
            drawAverageLine(currentPageData, cacheCanvas);
            drawBorders(cacheCanvas);
            drawXAxis(currentPageData, previousPageData, nextPageData, cacheCanvas);
            drawLongitudeLine(currentPageData, previousPageData, nextPageData, cacheCanvas);
            drawTimesVolumeRegion(mVolumeData, mPriceData, preClose, openPrice, cacheCanvas);

        } else if (mEnvDataPager.mChartType == StockDetailFragment.TYPE_KLINE) {

            candleWidth = (chartRight - chartLeft) / pagerDataCount;

            mEnvDataPager.resetKLineUpperRange(visibleData);
            maxValue = mEnvDataPager.mMaxValue;
            minValue = mEnvDataPager.mMinValue;
            kLineRate = (float) (upperHeight / (maxValue - minValue));

            if (mEnvDataPager.mSpecType == Spec_Type_NONE) {
                mEnvDataPager.resetKLineVolumeRange(visibleData);
                maxVolume = mEnvDataPager.mMaxVolume;
                minVolume = mEnvDataPager.mMinVolume;
                lowerRate = (float) (lowerHeight / maxVolume);

            } else if (mEnvDataPager.mSpecType == Spec_Type_MACD) {
                mEnvDataPager.resetMACDRange(visibleData);
                maxMACD = mEnvDataPager.mMaxMACD;
                minMACD = mEnvDataPager.mMinMACD;
                mMACDHalf = Math.abs(maxMACD) > Math.abs(minMACD) ? Math.abs(maxMACD) : Math.abs(minMACD);
                maxMACDLine = Math.abs(maxMACD) > Math.abs(minMACD) ? Math.abs(maxMACD) : Math.abs(minMACD + 2 * mMACDHalf);
                macdRate = (float) (lowerHeight / (2 * mMACDHalf));
            } else if (mEnvDataPager.mSpecType == Spec_Type_KDJ) {
                mEnvDataPager.resetKDJRange(visibleData);
                maxKDJ = mEnvDataPager.mMaxKDJ;
                minKDJ = mEnvDataPager.mMinKDJ;
                maxKDJ = maxKDJ > 100 ? maxKDJ : 100;
                minKDJ = minKDJ < 0 ? minKDJ : 0;
                kdjRate = (float) (lowerHeight / (maxKDJ - minKDJ));
            } else if (mEnvDataPager.mSpecType == Spec_Type_RSI) {
                mEnvDataPager.resetRSIRange(visibleData);
                maxRSI = mEnvDataPager.mMaxRSI;
                minRSI = mEnvDataPager.mMinRSI;
                rsiRate = (float) (lowerHeight / (maxRSI - minRSI));
            } else if (mEnvDataPager.mSpecType == Spec_Type_BOLL) {
                mEnvDataPager.resetBOLLRange(visibleData);
                maxBoll = mEnvDataPager.mMaxBoll;
                minBoll = mEnvDataPager.mMinBoll;
                bollRate = (float) (lowerHeight / (maxBoll - minBoll));
                boll2Rate = (float) (lowerHeight / (maxValue - minValue));
            }

            drawKLineRegion(currentPageData, cacheCanvas);
            drawMAAvgRegion(currentPageData, previousPageData, nextPageData, cacheCanvas);
            drawBorders(cacheCanvas);
            drawXAxis(currentPageData, previousPageData, nextPageData, cacheCanvas);
            drawLongitudeLine(currentPageData, previousPageData, nextPageData, cacheCanvas);
            drawLowerRegion(currentPageData, previousPageData, nextPageData, cacheCanvas);
        }
        StockChartFragments.StockChartDetailFragment.sRefreshChartValueSubject.onNext(null);
    }

    public void init() {

    }

    public View getView() {
        return mView;
    }

    public void drawPreCloseLine(Canvas canvas) {
        paint.setColor(LINE_BACKGROUND_COLOR);
        paint.setPathEffect(DEFAULT_DASH_EFFECT);
        paint.setStyle(Paint.Style.STROKE);
        path.reset();
        path.moveTo(chartLeft, upperHeight / 2);
        path.lineTo(chartRight, upperHeight / 2);
        canvas.drawPath(path, paint);
        paint.setPathEffect(null);
    }

    private void drawTimesLineRegion(List<Double> data, Canvas canvas) {
        float startX = chartLeft;
        float startY = 0;
        float endY = 0;
        float offSet = 0;

        paint.setColor(Color.parseColor("#3498DB"));
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);
        path.reset();

        for (int i = 0; i < data.size(); i++) {
            offSet = candleWidth * i;
            endY = (float) ((maxTimesLine - data.get(i)) * mTimesRate);
            if (i == 0) {
                path.moveTo(startX, endY);
            } else {
                path.lineTo(chartLeft + offSet, endY);
                canvas.drawLine(startX, startY, chartLeft + offSet, endY, paint);
            }
            startX = chartLeft + offSet;
            startY = endY;
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#E3EFFF"));
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
        path.lineTo(chartLeft + offSet, upperHeight);
        path.lineTo(chartLeft, upperHeight);
        path.close();
        canvas.drawPath(path, paint);
    }

    public void drawAverageLine(List<ChartController.ChartInfo> data, Canvas canvas) {
        List<Double> averageData = Stream.of(data).filter(info -> info.last > -1).map(info -> info.average).collect(Collectors.toList());
        float startX = chartLeft;
        float startY = 0;
        float endY = 0;
        float offSet = 0;

        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#FFDE00"));
        for (int i = 0; i < averageData.size(); i++) {
            offSet = candleWidth * i;
            endY = (float) ((maxTimesLine - averageData.get(i)) * mTimesRate);
            if (i != 0 && formatSecond(data.get(i).time, "MM-dd").equals(formatSecond(data.get(i - 1).time, "MM-dd"))) {
                canvas.drawLine(startX, startY, chartLeft + offSet, endY, paint);
            }
            startX = chartLeft + offSet;
            startY = endY;
        }
    }

    private void drawTimesVolumeRegion(List<Double> volumeData, List<Double> priceData, double preClose, double openPrice, Canvas canvas) {
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < volumeData.size(); i++) {
            float left = chartLeft + candleWidth * i;
            float top = (float) (lowerTop + (maxVolume - volumeData.get(i)) * lowerRate);
            float right = chartLeft + candleWidth * (i + 1) - candleWidth / 4;
            float bottom = lowerBottom;

            if (i == 0) {
                paint.setColor(openPrice > preClose ? RED_COLOR : GREEN_COLOR);
                canvas.drawRect(left, top, right, bottom, paint);
            } else {
                paint.setColor(priceData.get(i) > priceData.get(i - 1) ? RED_COLOR : GREEN_COLOR);
                canvas.drawRect(left, top, right, bottom, paint);
            }
        }
    }

    private void drawKLineRegion(List<ChartController.ChartInfo> data, Canvas canvas) {

        float offSet = 0;
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < data.size(); i++) {
            ChartController.ChartInfo info = data.get(i);
            float open = (float) ((maxValue - info.open) * kLineRate + 1);
            float close = (float) ((maxValue - info.close) * kLineRate + 1);
            float high = (float) ((maxValue - info.max) * kLineRate + 1);
            float low = (float) ((maxValue - info.min) * kLineRate + 1);
            offSet = candleWidth * i;
            float left = chartRight - offSet - candleWidth;
            float right = chartRight - offSet - candleWidth / 4;
            float startX = (right + left) / 2;
            if (info.open > info.close) {
                paint.setColor(GREEN_COLOR);
                canvas.drawRect(left, open, right, close, paint);
                canvas.drawLine(startX, high, startX, low, paint);
            } else {
                paint.setColor(RED_COLOR);
                if (info.open == info.close && info.open == info.max && info.open == info.min && info.close == info.max && info.close == info.min && info.max == info.min) {
                    canvas.drawRect(left, (float) ((maxValue - info.open) * kLineRate + 1), right, (float) ((maxValue - info.open) * kLineRate + 2), paint);
                } else {
                    canvas.drawRect(left, close, right, open, paint);
                    canvas.drawLine(startX, high, startX, low, paint);
                }
            }
        }
    }

    public void drawMAAvgRegion(List<ChartController.ChartInfo> data, List<ChartController.ChartInfo> previousPageData, List<ChartController.ChartInfo> nextPageData, Canvas canvas) {
        List<Double> ma5List = Stream.of(data).map(info -> info.ma5).collect(Collectors.toList());
        List<Double> ma10List = Stream.of(data).map(info -> info.ma10).collect(Collectors.toList());
        List<Double> ma20List = Stream.of(data).map(info -> info.ma20).collect(Collectors.toList());

        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        // init ma5
        {
            paint.setColor(LINE_BLUE_COLOR);
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;
            float offSet = 0;

            if (!previousPageData.isEmpty() && previousPageData.get(previousPageData.size() - 1).ma5 > 0) {
                offSet = (candleWidth - candleWidth / 4) / 2;
                startX = chartRight + offSet;
                startY = (float) ((maxValue - previousPageData.get(previousPageData.size() - 1).ma5) * kLineRate);
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endX = chartRight - offSet;
                endY = (float) ((maxValue - ma5List.get(0)) * kLineRate);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            for (int i = 0; i < ma5List.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxValue - ma5List.get(i)) * kLineRate);
                if (i != 0 && ma5List.get(i - 1) > 0 && ma5List.get(i) > 0) {
                    canvas.drawLine(startX, startY, chartRight - offSet, endY, paint);
                }
                startX = chartRight - offSet;
                startY = endY;
            }

            if (!nextPageData.isEmpty() && nextPageData.get(0).ma5 > 0) {
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxValue - nextPageData.get(0).ma5) * kLineRate);
                canvas.drawLine(startX, startY, chartLeft - offSet, endY, paint);
            }
        }

        // init ma10
        {
            paint.setColor(LINE_YELLOW_COLOR);
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;
            float offSet = 0;

            if (!previousPageData.isEmpty() && previousPageData.get(previousPageData.size() - 1).ma10 > 0) {
                offSet = (candleWidth - candleWidth / 4) / 2;
                startX = chartRight + offSet;
                startY = (float) ((maxValue - previousPageData.get(previousPageData.size() - 1).ma10) * kLineRate);
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endX = chartRight - offSet;
                endY = (float) ((maxValue - ma10List.get(0)) * kLineRate);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            for (int i = 0; i < ma10List.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxValue - ma10List.get(i)) * kLineRate);
                if (i != 0 && ma10List.get(i - 1) > 0 && ma10List.get(i) > 0) {
                    canvas.drawLine(startX, startY, chartRight - offSet, endY, paint);
                }
                startX = chartRight - offSet;
                startY = endY;
            }

            if (!nextPageData.isEmpty() && nextPageData.get(0).ma10 > 0) {
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxValue - nextPageData.get(0).ma10) * kLineRate);
                canvas.drawLine(startX, startY, chartLeft - offSet, endY, paint);
            }
        }

        // init ma20
        {
            paint.setColor(LINE_PURPLE_COLOR);
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;
            float offSet = 0;

            if (!previousPageData.isEmpty() && previousPageData.get(previousPageData.size() - 1).ma20 > 0) {
                offSet = (candleWidth - candleWidth / 4) / 2;
                startX = chartRight + offSet;
                startY = (float) ((maxValue - previousPageData.get(previousPageData.size() - 1).ma20) * kLineRate);
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endX = chartRight - offSet;
                endY = (float) ((maxValue - ma20List.get(0)) * kLineRate);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            for (int i = 0; i < ma20List.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxValue - ma20List.get(i)) * kLineRate);
                if (i != 0 && ma20List.get(i - 1) > 0 && ma20List.get(i) > 0) {
                    canvas.drawLine(startX, startY, chartRight - offSet, endY, paint);
                }
                startX = chartRight - offSet;
                startY = endY;
            }

            if (!nextPageData.isEmpty() && nextPageData.get(0).ma20 > 0) {
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxValue - nextPageData.get(0).ma20) * kLineRate);
                canvas.drawLine(startX, startY, chartLeft - offSet, endY, paint);
            }
        }
    }

    private void drawLowerRegion(List<ChartController.ChartInfo> data, List<ChartController.ChartInfo> previousPageData, List<ChartController.ChartInfo> nextPageData, Canvas canvas) {
        if (mEnvDataPager.mSpecType == Spec_Type_NONE) {
            drawVolumeRegion(data, canvas);
        } else if (mEnvDataPager.mSpecType == Spec_Type_MACD) {
            drawMACDLineRegion(data, previousPageData, nextPageData, canvas);
        } else if (mEnvDataPager.mSpecType == Spec_Type_KDJ) {
            drawKDJLineRegion(data, previousPageData, nextPageData, canvas);
        } else if (mEnvDataPager.mSpecType == Spec_Type_RSI) {
            drawRSILineRegion(data, previousPageData, nextPageData, canvas);
        } else if (mEnvDataPager.mSpecType == Spec_Type_BOLL) {
            drawBOLLLineRegion(data, previousPageData, nextPageData, canvas);
        }
    }

    public void drawVolumeRegion(List<ChartController.ChartInfo> data, Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(2);
        float offSet = 0;
        for (int i = 0; i < data.size(); i++) {
            ChartInfo info = data.get(i);
            offSet = candleWidth * i;
            float left = chartRight - offSet - candleWidth;
            float right = chartRight - offSet - candleWidth / 4;
            float top = (float) (lowerTop + (maxVolume - info.volume) * lowerRate);
            float bottom = lowerBottom;

            paint.setColor(info.close >= info.open ? RED_COLOR : GREEN_COLOR);
            canvas.drawRect(left, top, right, bottom, paint);
        }
    }

    private void drawMACDLineRegion(List<ChartController.ChartInfo> data, List<ChartController.ChartInfo> previousPageData, List<ChartController.ChartInfo> nextPageData, Canvas canvas) {
        List<Double> diffList = Stream.of(data).map(info -> info.diff).collect(Collectors.toList());
        List<Double> deaList = Stream.of(data).map(info -> info.dea).collect(Collectors.toList());
        List<Double> macdList = Stream.of(data).map(info -> info.macd).collect(Collectors.toList());

        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        // init DIFF
        {
            paint.setColor(LINE_BLUE_COLOR);
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;
            float offSet = 0;

            if (!previousPageData.isEmpty()) {
                offSet = (candleWidth - candleWidth / 4) / 2;
                startX = chartRight + offSet;
                startY = (float) ((maxMACDLine - previousPageData.get(previousPageData.size() - 1).diff) * macdRate + lowerTop);
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endX = chartRight - offSet;
                endY = (float) ((maxMACDLine - diffList.get(0)) * macdRate + lowerTop);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            for (int i = 0; i < diffList.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxMACDLine - diffList.get(i)) * macdRate + lowerTop);
                if (i != 0) {
                    canvas.drawLine(startX, startY, chartRight - offSet, endY, paint);
                }
                startX = chartRight - offSet;
                startY = endY;
            }

            if (!nextPageData.isEmpty()) {
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxMACDLine - nextPageData.get(0).diff) * macdRate + lowerTop);
                canvas.drawLine(startX, startY, chartLeft - offSet, endY, paint);
            }
        }

        // init DEA
        {
            paint.setColor(LINE_YELLOW_COLOR);
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;
            float offSet = 0;

            if (!previousPageData.isEmpty()) {
                offSet = (candleWidth - candleWidth / 4) / 2;
                startX = chartRight + offSet;
                startY = (float) ((maxMACDLine - previousPageData.get(previousPageData.size() - 1).dea) * macdRate + lowerTop);
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endX = chartRight - offSet;
                endY = (float) ((maxMACDLine - deaList.get(0)) * macdRate + lowerTop);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            for (int i = 0; i < deaList.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxMACDLine - deaList.get(i)) * macdRate + lowerTop);
                if (i != 0) {
                    canvas.drawLine(startX, startY, chartRight - offSet, endY, paint);
                }
                startX = chartRight - offSet;
                startY = endY;
            }

            if (!nextPageData.isEmpty()) {
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxMACDLine - nextPageData.get(0).dea) * macdRate + lowerTop);
                canvas.drawLine(startX, startY, chartLeft - offSet, endY, paint);
            }
        }

        // init MACD
        {
            paint.setColor(LINE_PURPLE_COLOR);
            float offSet = 0;
            for (int i = 0; i < macdList.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                float x = chartRight - offSet;
                float y = (float) ((maxMACDLine - macdList.get(i)) * macdRate + lowerTop);
                if (macdList.get(i) >= 0) {
                    paint.setColor(RED_COLOR);
                    canvas.drawLine(x, y, x, (float) (lowerTop + mMACDHalf * macdRate), paint);
                } else {
                    paint.setColor(GREEN_COLOR);
                    canvas.drawLine(x, (float) (lowerTop + mMACDHalf * macdRate), x, y, paint);
                }
            }
        }
    }

    private void drawKDJLineRegion(List<ChartController.ChartInfo> data, List<ChartController.ChartInfo> previousPageData, List<ChartController.ChartInfo> nextPageData, Canvas canvas) {
        List<Double> kList = Stream.of(data).map(info -> info.kValue).collect(Collectors.toList());
        List<Double> dList = Stream.of(data).map(info -> info.dValue).collect(Collectors.toList());
        List<Double> jList = Stream.of(data).map(info -> info.jValue).collect(Collectors.toList());

        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        // init 20 50 80
        {
            paint.setColor(LINE_BACKGROUND_COLOR);
            canvas.drawLine(chartLeft, (float) (maxKDJ - 20) * kdjRate + lowerTop, chartRight, (float) ((maxKDJ - 20) * kdjRate + lowerTop), paint);
            canvas.drawLine(chartLeft, (float) (maxKDJ - 50) * kdjRate + lowerTop, chartRight, (float) ((maxKDJ - 50) * kdjRate + lowerTop), paint);
            canvas.drawLine(chartLeft, (float) (maxKDJ - 80) * kdjRate + lowerTop, chartRight, (float) ((maxKDJ - 80) * kdjRate + lowerTop), paint);
        }

        paint.setStrokeWidth(2);
        // init k
        {
            paint.setColor(LINE_BLUE_COLOR);
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;
            float offSet = 0;

            if (!previousPageData.isEmpty()) {
                offSet = (candleWidth - candleWidth / 4) / 2;
                startX = chartRight + offSet;
                startY = (float) ((maxKDJ - previousPageData.get(previousPageData.size() - 1).kValue) * kdjRate + lowerTop);
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endX = chartRight - offSet;
                endY = (float) ((maxKDJ - kList.get(0)) * kdjRate + lowerTop);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            for (int i = 0; i < kList.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxKDJ - kList.get(i)) * kdjRate + lowerTop);
                if (i != 0) {
                    canvas.drawLine(startX, startY, chartRight - offSet, endY, paint);
                }
                startX = chartRight - offSet;
                startY = endY;
            }

            if (!nextPageData.isEmpty()) {
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxKDJ - nextPageData.get(0).kValue) * kdjRate + lowerTop);
                canvas.drawLine(startX, startY, chartLeft - offSet, endY, paint);
            }
        }

        // init d
        {
            paint.setColor(LINE_YELLOW_COLOR);
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;
            float offSet = 0;

            if (!previousPageData.isEmpty()) {
                offSet = (candleWidth - candleWidth / 4) / 2;
                startX = chartRight + offSet;
                startY = (float) ((maxKDJ - previousPageData.get(previousPageData.size() - 1).dValue) * kdjRate + lowerTop);
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endX = chartRight - offSet;
                endY = (float) ((maxKDJ - dList.get(0)) * kdjRate + lowerTop);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            for (int i = 0; i < dList.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxKDJ - dList.get(i)) * kdjRate + lowerTop);
                if (i != 0) {
                    canvas.drawLine(startX, startY, chartRight - offSet, endY, paint);
                }
                startX = chartRight - offSet;
                startY = endY;
            }

            if (!nextPageData.isEmpty() && nextPageData.get(0).dValue > 0) {
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxKDJ - nextPageData.get(0).dValue) * kdjRate + lowerTop);
                canvas.drawLine(startX, startY, chartLeft - offSet, endY, paint);
            }
        }

        // init j
        {
            paint.setColor(LINE_PURPLE_COLOR);
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;
            float offSet = 0;

            if (!previousPageData.isEmpty()) {
                offSet = (candleWidth - candleWidth / 4) / 2;
                startX = chartRight + offSet;
                startY = (float) ((maxKDJ - previousPageData.get(previousPageData.size() - 1).jValue) * kdjRate + lowerTop);
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endX = chartRight - offSet;
                endY = (float) ((maxKDJ - jList.get(0)) * kdjRate + lowerTop);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            for (int i = 0; i < jList.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxKDJ - jList.get(i)) * kdjRate + lowerTop);
                if (i != 0) {
                    canvas.drawLine(startX, startY, chartRight - offSet, endY, paint);
                }
                startX = chartRight - offSet;
                startY = endY;
            }

            if (!nextPageData.isEmpty()) {
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxKDJ - nextPageData.get(0).jValue) * kdjRate + lowerTop);
                canvas.drawLine(startX, startY, chartLeft - offSet, endY, paint);
            }
        }
    }

    private void drawRSILineRegion(List<ChartController.ChartInfo> data, List<ChartController.ChartInfo> previousPageData, List<ChartInfo> nextPageData, Canvas canvas) {
        List<Double> rsi6List = Stream.of(data).map(info -> info.rsi6).collect(Collectors.toList());
        List<Double> rsi12List = Stream.of(data).map(info -> info.rsi12).collect(Collectors.toList());
        List<Double> rsi24List = Stream.of(data).map(info -> info.rsi24).collect(Collectors.toList());

        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        // init rsi6
        {
            paint.setColor(LINE_BLUE_COLOR);
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;
            float offSet = 0;

            if (!previousPageData.isEmpty() && previousPageData.get(previousPageData.size() - 1).rsi6 > 0) {
                offSet = (candleWidth - candleWidth / 4) / 2;
                startX = chartRight + offSet;
                startY = (float) ((maxRSI - previousPageData.get(previousPageData.size() - 1).rsi6) * rsiRate + lowerTop);
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endX = chartRight - offSet;
                endY = (float) ((maxRSI - rsi6List.get(0)) * rsiRate + lowerTop);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            for (int i = 0; i < rsi6List.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxRSI - rsi6List.get(i)) * rsiRate + lowerTop);
                if (i != 0 && rsi6List.get(i - 1) > 0 && rsi6List.get(i) > 0) {
                    canvas.drawLine(startX, startY, chartRight - offSet, endY, paint);
                }
                startX = chartRight - offSet;
                startY = endY;
            }

            if (!nextPageData.isEmpty() && nextPageData.get(0).rsi6 > 0) {
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxRSI - nextPageData.get(0).rsi6) * rsiRate + lowerTop);
                canvas.drawLine(startX, startY, chartLeft - offSet, endY, paint);
            }
        }

        // init rsi12
        {
            paint.setColor(LINE_YELLOW_COLOR);
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;
            float offSet = 0;

            if (!previousPageData.isEmpty() && previousPageData.get(previousPageData.size() - 1).rsi12 > 0) {
                offSet = (candleWidth - candleWidth / 4) / 2;
                startX = chartRight + offSet;
                startY = (float) ((maxRSI - previousPageData.get(previousPageData.size() - 1).rsi12) * rsiRate + lowerTop);
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endX = chartRight - offSet;
                endY = (float) ((maxRSI - rsi12List.get(0)) * rsiRate + lowerTop);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            for (int i = 0; i < rsi12List.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxRSI - rsi12List.get(i)) * rsiRate + lowerTop);
                if (i != 0 && rsi12List.get(i - 1) > 0 && rsi12List.get(i) > 0) {
                    canvas.drawLine(startX, startY, chartRight - offSet, endY, paint);
                }
                startX = chartRight - offSet;
                startY = endY;
            }

            if (!nextPageData.isEmpty() && nextPageData.get(0).rsi12 > 0) {
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxRSI - nextPageData.get(0).rsi12) * rsiRate + lowerTop);
                canvas.drawLine(startX, startY, chartLeft - offSet, endY, paint);
            }
        }

        // init rsi24
        {
            paint.setColor(LINE_PURPLE_COLOR);
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;
            float offSet = 0;

            if (!previousPageData.isEmpty() && previousPageData.get(previousPageData.size() - 1).rsi24 > 0) {
                offSet = (candleWidth - candleWidth / 4) / 2;
                startX = chartRight + offSet;
                startY = (float) ((maxRSI - previousPageData.get(previousPageData.size() - 1).rsi24) * rsiRate + lowerTop);
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endX = chartRight - offSet;
                endY = (float) ((maxRSI - rsi24List.get(0)) * rsiRate + lowerTop);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            for (int i = 0; i < rsi24List.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxRSI - rsi24List.get(i)) * rsiRate + lowerTop);
                if (i != 0 && rsi24List.get(i - 1) > 0 && rsi24List.get(i) > 0) {
                    canvas.drawLine(startX, startY, chartRight - offSet, endY, paint);
                }
                startX = chartRight - offSet;
                startY = endY;
            }

            if (!nextPageData.isEmpty() && nextPageData.get(0).rsi24 > 0) {
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxRSI - nextPageData.get(0).rsi24) * rsiRate + lowerTop);
                canvas.drawLine(startX, startY, chartLeft - offSet, endY, paint);
            }
        }
    }

    private void drawBOLLLineRegion(List<ChartController.ChartInfo> data, List<ChartController.ChartInfo> previousPageData, List<ChartController.ChartInfo> nextPageData, Canvas canvas) {
        List<Double> upperList = Stream.of(data).map(info -> info.upper).collect(Collectors.toList());
        List<Double> midList = Stream.of(data).map(info -> info.mid).collect(Collectors.toList());
        List<Double> lowerList = Stream.of(data).map(info -> info.lower).collect(Collectors.toList());

        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        // init upper
        {
            paint.setColor(LINE_BLUE_COLOR);
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;
            float offSet = 0;

            if (!previousPageData.isEmpty() && previousPageData.get(previousPageData.size() - 1).upper > 0) {
                offSet = (candleWidth - candleWidth / 4) / 2;
                startX = chartRight + offSet;
                startY = (float) ((maxBoll - previousPageData.get(previousPageData.size() - 1).upper) * bollRate + lowerTop);
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endX = chartRight - offSet;
                endY = (float) ((maxBoll - upperList.get(0)) * bollRate + lowerTop);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            for (int i = 0; i < upperList.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxBoll - upperList.get(i)) * bollRate + lowerTop);
                if (i != 0 && upperList.get(i - 1) > 0 && upperList.get(i) > 0) {
                    canvas.drawLine(startX, startY, chartRight - offSet, endY, paint);
                }
                startX = chartRight - offSet;
                startY = endY;
            }

            if (!nextPageData.isEmpty() && nextPageData.get(0).upper > 0) {
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxBoll - nextPageData.get(0).upper) * bollRate + lowerTop);
                canvas.drawLine(startX, startY, chartLeft - offSet, endY, paint);
            }
        }

        // init mid
        {
            paint.setColor(LINE_YELLOW_COLOR);
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;
            float offSet = 0;

            if (!previousPageData.isEmpty() && previousPageData.get(previousPageData.size() - 1).mid > 0) {
                offSet = (candleWidth - candleWidth / 4) / 2;
                startX = chartRight + offSet;
                startY = (float) ((maxBoll - previousPageData.get(previousPageData.size() - 1).mid) * bollRate + lowerTop);
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endX = chartRight - offSet;
                endY = (float) ((maxBoll - midList.get(0)) * bollRate + lowerTop);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            for (int i = 0; i < midList.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxBoll - midList.get(i)) * bollRate + lowerTop);
                if (i != 0 && midList.get(i - 1) > 0 && midList.get(i) > 0) {
                    canvas.drawLine(startX, startY, chartRight - offSet, endY, paint);
                }
                startX = chartRight - offSet;
                startY = endY;
            }

            if (!nextPageData.isEmpty() && nextPageData.get(0).mid > 0) {
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxBoll - nextPageData.get(0).mid) * bollRate + lowerTop);
                canvas.drawLine(startX, startY, chartLeft - offSet, endY, paint);
            }
        }

        // init lower
        {
            paint.setColor(LINE_PURPLE_COLOR);
            float startX = 0;
            float startY = 0;
            float endX = 0;
            float endY = 0;
            float offSet = 0;

            if (!previousPageData.isEmpty() && previousPageData.get(previousPageData.size() - 1).lower > 0) {
                offSet = (candleWidth - candleWidth / 4) / 2;
                startX = chartRight + offSet;
                startY = (float) ((maxBoll - previousPageData.get(previousPageData.size() - 1).lower) * bollRate + lowerTop);
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endX = chartRight - offSet;
                endY = (float) ((maxBoll - lowerList.get(0)) * bollRate + lowerTop);
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            for (int i = 0; i < lowerList.size(); i++) {
                offSet = candleWidth * i + candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxBoll - lowerList.get(i)) * bollRate + lowerTop);

                if (i != 0 && lowerList.get(i - 1) > 0 && lowerList.get(i) > 0) {
                    canvas.drawLine(startX, startY, chartRight - offSet, endY, paint);
                }
                startX = chartRight - offSet;
                startY = endY;
            }

            if (!nextPageData.isEmpty() && nextPageData.get(0).lower > 0) {
                offSet = candleWidth / 4 + (candleWidth - candleWidth / 4) / 2;
                endY = (float) ((maxBoll - nextPageData.get(0).lower) * bollRate + lowerTop);
                canvas.drawLine(startX, startY, chartLeft - offSet, endY, paint);
            }
        }

        // init other
        {
            float offSet = 0;
            for (int i = 0; i < data.size(); i++) {
                ChartController.ChartInfo info = data.get(i);
                float open = (float) ((maxValue - info.open) * boll2Rate + lowerTop);
                float close = (float) ((maxValue - info.close) * boll2Rate + lowerTop);
                float high = (float) ((maxValue - info.max) * boll2Rate + lowerTop);
                float low = (float) ((maxValue - info.min) * boll2Rate + lowerTop);
                offSet = candleWidth * i;
                float left = chartRight - offSet - candleWidth;
                offSet = candleWidth * i + candleWidth / 4;
                float right = chartRight - offSet;
                float startX = (left + right) / 2;
                if (info.open >= info.close) {
                    paint.setColor(GREEN_COLOR);
                } else {
                    paint.setColor(RED_COLOR);
                }
                canvas.drawLine(left, open, startX, open, paint);
                canvas.drawLine(startX, close, right, close, paint);
                canvas.drawLine(startX, high, startX, low, paint);
            }
        }
    }

    public void drawBorders(Canvas canvas) {

        paint.setColor(LINE_BACKGROUND_COLOR);
        paint.setStrokeWidth(1);
        if (mEnvDataPager.mChartType == StockDetailFragment.TYPE_KLINE) {
            canvas.drawLine(chartLeft, upperHeight / 2, chartRight, upperHeight / 2, paint);
        }

        canvas.drawLine(chartLeft, upperTop, chartRight, upperTop, paint);
        canvas.drawLine(chartLeft, upperBottom, chartRight, upperBottom, paint);

        canvas.drawLine(chartLeft, lowerTop, chartRight, lowerTop, paint);
        canvas.drawLine(chartLeft, lowerBottom, chartRight, lowerBottom, paint);

        //        paint.setColor(RED_COLOR);
        //        canvas.drawLine(chartRight, upperTop, chartRight, lowerBottom, paint);
        //        paint.setColor(LINE_BACKGROUND_COLOR);
        //        paint.setStyle(Paint.Style.STROKE);
    }

    public void drawXAxis(List<ChartController.ChartInfo> currentPageData, List<ChartController.ChartInfo> previousPageData, List<ChartController.ChartInfo> nextPageData, Canvas canvas) {

    }

    public void drawLongitudeLine(List<ChartController.ChartInfo> currentPageData, List<ChartController.ChartInfo> previousPageData, List<ChartController.ChartInfo> nextPageData, Canvas canvas) {

    }
}
