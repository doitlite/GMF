package com.goldmf.GMFund.widget;

import android.util.Pair;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.model.LineData.KLineData;
import com.goldmf.GMFund.model.LineData.TLineData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Evan on 16/3/23 上午11:42.
 */
public class ChartController {

    private ChartController() {

    }

    public static class EnvDataPager {

        public static final int DEFAULT_COUNT = 80;
        public List<ChartInfo> mChartData;
        public int mChartType;
        public int mSpecType;
        public boolean isCacheBitmap;
        public double mMaxValue;
        public double mMinValue;
        public double mMaxVolume;
        public double mMinVolume;
        public double mMaxMACD;
        public double mMinMACD;
        public double mMaxKDJ;
        public double mMinKDJ;
        public double mMaxRSI;
        public double mMinRSI;
        public double mMaxBoll;
        public double mMinBoll;

        public void setChartData(List<ChartInfo> data, int chartType, int specType, boolean isCache) {
            mChartData = data;
            mChartType = chartType;
            mSpecType = specType;
            isCacheBitmap = isCache;
        }

        public void recomputePriceRange(List<Double> data) {
            Pair<Double, Double> valueRange = recomputeRange(data);
            mMaxValue = valueRange.first;
            mMinValue = valueRange.second;
        }

        public void resetKLineUpperRange(List<ChartInfo> data) {
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

            Pair<Double, Double> valueRange = recomputeRange(datas);
            mMaxValue = valueRange.first;
            mMinValue = valueRange.second;
        }

        public void resetTimesVolumeRange(List<Double> data) {
            Pair<Double, Double> valueRange = recomputeRange(data);
            mMaxVolume = valueRange.first;
            mMinVolume = valueRange.second;
        }

        public void resetKLineVolumeRange(List<ChartInfo> data) {
            List<Double> datas = Stream.of(data).map(info -> info.volume).collect(Collectors.toList());

            Pair<Double, Double> valueRange = recomputeRange(datas);
            mMaxVolume = valueRange.first;
            mMinVolume = valueRange.second;
        }

        public void resetMACDRange(List<ChartInfo> data) {
            List<Double> difList = Stream.of(data).map(info -> info.diff).collect(Collectors.toList());
            List<Double> deaList = Stream.of(data).map(info -> info.dea).collect(Collectors.toList());
            List<Double> macdList = Stream.of(data).map(info -> info.macd).collect(Collectors.toList());

            List<Double> datas = new ArrayList<>();
            datas.addAll(difList);
            datas.addAll(deaList);
            datas.addAll(macdList);

            Pair<Double, Double> valueRange = recomputeRange(datas);
            mMaxMACD = valueRange.first;
            mMinMACD = valueRange.second;
        }

        public void resetKDJRange(List<ChartInfo> data) {
            List<Double> kList = Stream.of(data).map(info -> info.kValue).collect(Collectors.toList());
            List<Double> dList = Stream.of(data).map(info -> info.dValue).collect(Collectors.toList());
            List<Double> jList = Stream.of(data).map(info -> info.jValue).collect(Collectors.toList());

            List<Double> datas = new ArrayList<>();
            datas.addAll(kList);
            datas.addAll(dList);
            datas.addAll(jList);

            Pair<Double, Double> valueRange = recomputeRange(datas);
            mMaxKDJ = valueRange.first;
            mMinKDJ = valueRange.second;
        }

        public void resetRSIRange(List<ChartInfo> data) {
            List<Double> rsi6List = Stream.of(data).filter(info -> info.rsi6 > 0).map(info -> info.rsi6).collect(Collectors.toList());
            List<Double> rsi12List = Stream.of(data).filter(info -> info.rsi12 > 0).map(info -> info.rsi12).collect(Collectors.toList());
            List<Double> rsi24List = Stream.of(data).filter(info -> info.rsi24 > 0).map(info -> info.rsi24).collect(Collectors.toList());

            List<Double> datas = new ArrayList<>();
            datas.addAll(rsi6List);
            datas.addAll(rsi12List);
            datas.addAll(rsi24List);

            Pair<Double, Double> valueRange = recomputeRange(datas);
            mMaxRSI = valueRange.first;
            mMinRSI = valueRange.second;
        }

        public void resetBOLLRange(List<ChartInfo> data) {
            List<Double> upperList = Stream.of(data).filter(info -> info.upper > 0).map(info -> info.upper).collect(Collectors.toList());
            List<Double> midList = Stream.of(data).filter(info -> info.mid > 0).map(info -> info.mid).collect(Collectors.toList());
            List<Double> lowerList = Stream.of(data).filter(info -> info.lower > 0).map(info -> info.lower).collect(Collectors.toList());

            List<Double> datas = new ArrayList<>();
            datas.addAll(upperList);
            datas.addAll(midList);
            datas.addAll(lowerList);

            Pair<Double, Double> valueRange = recomputeRange(datas);
            mMaxBoll = valueRange.first;
            mMinBoll = valueRange.second;
        }

        private Pair<Double, Double> recomputeRange(List<Double> data) {
            if (data == null || data.isEmpty()) {
                return Pair.create(0D, 0D);
            }

            if (data.size() == 1) {
                return Pair.create(data.get(0), data.get(0));
            }

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

    public static class ChartInfo {

        public double last;
        public double average;
        public boolean isDrawLine;

        public long time;
        public double preClose;
        public double open;
        public double close;
        public double max;
        public double min;
        public double volume;
        public double turnover;

        public double ma5;
        public double ma10;
        public double ma20;
        public double diff;
        public double dea;
        public double macd;
        public double kValue;
        public double dValue;
        public double jValue;
        public double rsi6;
        public double rsi12;
        public double rsi24;
        public double upper;
        public double mid;
        public double lower;

        public ChartInfo(KLineData data, boolean isShowLine) {
            time = data.traderTime;
            preClose = data.prevClose;
            open = data.open;
            close = data.close;
            max = data.max;
            min = data.min;
            volume = data.volume;
            turnover = data.turnover;

            ma5 = data.ma5;
            ma10 = data.ma10;
            ma20 = data.ma20;
            diff = data.DIFF;
            dea = data.DEA;
            macd = data.MACD;
            kValue = data.K;
            dValue = data.D;
            jValue = data.J;
            rsi6 = data.RSI6;
            rsi12 = data.RSI12;
            rsi24 = data.RSI24;
            upper = data.UPPER;
            mid = data.MID;
            lower = data.LOWER;
            isDrawLine = isShowLine;
        }

        public ChartInfo(TLineData data, boolean isShowLine) {
            last = data.last;
            average = data.avg;
            preClose = data.prevClose;
            volume = data.volume;
            turnover = data.turnover;
            time = data.traderTime;
            isDrawLine = isShowLine;
        }
    }

}

