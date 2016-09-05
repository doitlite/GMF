package com.goldmf.GMFund.controller.internal;

import com.goldmf.GMFund.controller.internal.KLineProtocol.ChartViewMode;
import com.goldmf.GMFund.model.LineData.TLineData;

import java.util.List;

import static com.goldmf.GMFund.util.FormatUtil.formatSecond;

/**
 * Created by Evan on 16/3/1 下午3:16.
 */
public class ChartViewHelp {

    private ChartViewHelp() {

    }

    public static class TimesDetailChartInfo {
        public String time;
        public double value;
        public double turnover;
        public double averagePrice;
        public double volume;

        public TimesDetailChartInfo(TLineData data) {
            this.time = formatSecond(data.traderTime, "HH:mm");
            this.value = data.last;
            this.averagePrice = data.avg;
            this.volume = data.volume;
            this.turnover = data.turnover;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public double getTurnover() {
            return turnover;
        }

        public void setTurnover(double turnover) {
            this.turnover = turnover;
        }

        public double getAveragePrice() {
            return averagePrice;
        }

        public void setAveragePrice(double averagePrice) {
            this.averagePrice = averagePrice;
        }

        public double getVolume() {
            return volume;
        }

        public void setVolume(double volume) {
            this.volume = volume;
        }
    }


    public static class OHLCViewMode {
        public float open;
        public float high;
        public float low;
        public float close;
        public double volume;
        public String date;

        public OHLCViewMode(ChartViewMode data) {
            this.open = (float) data.open;
            this.high = (float) data.high;
            this.low = (float) data.low;
            this.close = (float) data.close;
            this.date = data.day;
        }

        public double getOpen() {
            return open;
        }

        public void setOpen(float open) {
            this.open = open;
        }

        public double getHigh() {
            return high;
        }

        public void setHigh(float high) {
            this.high = high;
        }

        public double getLow() {
            return low;
        }

        public void setLow(float low) {
            this.low = low;
        }

        public double getClose() {
            return close;
        }

        public void setClose(float close) {
            this.close = close;
        }

        public double getVolume() {
            return volume;
        }

        public void setVolume(double volume) {
            this.volume = volume;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    public static class MALineViewMode {

        private List<Float> lineData;
        private String title;
        private int lineColor;

        public MALineViewMode() {

        }

        public MALineViewMode(List<Float> lineData, String title, int lineColor) {
            this.lineData = lineData;
            this.title = title;
            this.lineColor = lineColor;
        }

        public List<Float> getLineData() {
            return lineData;
        }

        public void setLineData(List<Float> lineData) {
            this.lineData = lineData;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getLineColor() {
            return lineColor;
        }

        public void setLineColor(int lineColor) {
            this.lineColor = lineColor;
        }
    }


}
