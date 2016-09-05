package com.goldmf.GMFund.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.protocol.base.PageArray.PageItemIndex;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import yale.extension.common.Optional;

/**
 * Created by cupide on 16/3/11.
 */
public class LineData {

    public static final int GMFStockAnswer_Authority_No = 0;         //不复权
    public static final int GMFStockAnswer_Authority_Forward = 1;    //前复权
    public static final int GMFStockAnswer_Authority_Backward = 2;   //后复权

    public static class TLineData implements Serializable {

        public static int Tline_Type_Day = 301;     //日T线
        public static int Tline_Type_5Day = 305;    //5日T线

        public long traderTime;

        public double prevClose;      //昨日收盘价
        public double last;           //价格
        public double volume;         //成交量
        public double turnover;       //成交金额

        public double avg;            //均价（黄色线）

        public static TLineData translateFromJsonData(JsonElement dic) {
            if(dic == null || dic.isJsonNull())
                return null;

            TLineData data = new TLineData();
            data.traderTime = GsonUtil.getChildAsLong(dic, 0);
            data.last = GsonUtil.getChildAsDouble(dic, 1);
            data.volume = GsonUtil.getChildAsDouble(dic, 2);
            data.turnover = GsonUtil.getChildAsDouble(dic, 3);
            data.avg = GsonUtil.getChildAsDouble(dic, 4);
            return data;
        }

        public static List<? extends TLineData> translate(JsonArray list) {
            return Stream.of(Optional.of(list).or(new JsonArray()))
                    .map(it -> GsonUtil.getAsJsonObject(it))
                    .map(it -> translateFromJsonData(it))
                    .filter(it -> it != null)
                    .collect(Collectors.toList());
        }
    }

    public static class KLineData extends PageItemIndex {

        public static final int Kline_Type_5M = 105;        //5分钟线
        public static final int Kline_Type_15M = 115;       //15分钟线
        public static final int Kline_Type_60M = 201;       //60分钟线
        public static final int Kline_Type_Day = 301;       //日线
        public static final int Kline_Type_Week = 307;      //周线
        public static final int Kline_Type_Month = 401;     //月线

        public static final int Spec_Type_NONE = 10200;
        public static final int Spec_Type_MACD = 10201;
        public static final int Spec_Type_KDJ = 10202;
        public static final int Spec_Type_RSI = 10203;
        public static final int Spec_Type_BOLL = 10204;

        public long traderTime;
        public double prevClose;       //昨日收盘价
        public double close;           //当日收盘价
        public double open;      //当日开盘价
        public double max;       //当日最高
        public double min;       //当日最低
        public double volume;          //成交量
        public double turnover;        //成交金额
        public double ma5;          //5个单位均线
        public double ma10;         //10个单位均线
        public double ma20;         //20个单位均线

        public double DIFF;
        public double DEA;
        public double MACD;

        public double K;
        public double D;
        public double J;

        public double RSI6;
        public double RSI12;
        public double RSI24;

        public double UPPER;
        public double MID;
        public double LOWER;

        public static KLineData translateFromJsonData(JsonObject dic) {
            if (dic == null || dic.isJsonNull())return null;
            try {
                KLineData data = new KLineData();
                data.traderTime = GsonUtil.getAsLong(dic, "d");
                data.prevClose = GsonUtil.getAsDouble(dic, "p");
                data.close = GsonUtil.getAsDouble(dic, "c");
                data.open = GsonUtil.getAsDouble(dic, "o");
                data.max = GsonUtil.getAsDouble(dic, "h");
                data.min = GsonUtil.getAsDouble(dic, "l");
                data.volume = GsonUtil.getAsDouble(dic, "v");
                data.turnover = GsonUtil.getAsDouble(dic, "t");
                data.ma5 = GsonUtil.getAsDouble(dic, "MA5");
                data.ma10 = GsonUtil.getAsDouble(dic, "MA10");
                data.ma20 = GsonUtil.getAsDouble(dic, "MA20");
                data.DIFF = GsonUtil.getAsDouble(dic, "DIF");
                data.DEA = GsonUtil.getAsDouble(dic, "DEA");
                data.MACD = GsonUtil.getAsDouble(dic, "MACD");

                data.K = GsonUtil.getAsDouble(dic, "K");
                data.D = GsonUtil.getAsDouble(dic, "D");
                data.J = GsonUtil.getAsDouble(dic, "J");

                data.RSI6 = GsonUtil.getAsDouble(dic, "RSI1");
                data.RSI12 = GsonUtil.getAsDouble(dic, "RSI2");
                data.RSI24 = GsonUtil.getAsDouble(dic, "RSI3");

                data.UPPER = GsonUtil.getAsDouble(dic, "UP");
                data.MID = GsonUtil.getAsDouble(dic, "MB");
                data.LOWER = GsonUtil.getAsDouble(dic, "DN");
                return data;
            } catch (Exception ignored) {
                return null;
            }
        }

        public static List<? extends KLineData> translate(JsonArray list) {
            return Stream.of(Optional.of(list).or(new JsonArray()))
                    .map(it -> GsonUtil.getAsJsonObject(it))
                    .map(it -> translateFromJsonData(it))
                    .filter(it -> it != null)
                    .collect(Collectors.toList());
        }

        @Override
        public Object getKey() {
            return this.traderTime;
        }

        @Override
        public long getTime() {
            return this.traderTime;
        }


        public static String getSpecString(int specType){

            String specName = "";
            switch (specType) {
                case KLineData.Spec_Type_MACD:
                    specName = "MACD";
                    break;
                case KLineData.Spec_Type_KDJ:
                    specName = "KDJ";
                    break;
                case KLineData.Spec_Type_RSI:
                    specName = "RSI";
                    break;
                case KLineData.Spec_Type_BOLL:
                    specName = "BOLL";
                    break;
                default:
                    break;
            }
            return specName;
        }

        public void readFromJsonData(JsonElement dic, int specType) {
            JsonObject detail = GsonUtil.getAsJsonObject(dic, "detail");
            if(detail == null){
                detail = GsonUtil.getAsJsonObject(dic);
            }
                if (specType == Spec_Type_MACD) {
                    this.DIFF = GsonUtil.getAsDouble(detail, "DIF");
                    this.DEA = GsonUtil.getAsDouble(detail, "DEA");
                    this.MACD = GsonUtil.getAsDouble(detail, "MACD");
                } else if (specType == Spec_Type_KDJ) {
                    this.K = GsonUtil.getAsDouble(detail, "K");
                    this.D = GsonUtil.getAsDouble(detail, "D");
                    this.J = GsonUtil.getAsDouble(detail, "J");
                } else if (specType == Spec_Type_RSI) {
                    this.RSI6 = GsonUtil.getAsDouble(detail, "RSI1");
                    this.RSI12 = GsonUtil.getAsDouble(detail, "RSI2");
                    this.RSI24 = GsonUtil.getAsDouble(detail, "RSI3");
                } else if (specType == Spec_Type_BOLL) {
                    this.UPPER = GsonUtil.getAsDouble(detail, "UP");
                    this.MID = GsonUtil.getAsDouble(detail, "MB");
                    this.LOWER = GsonUtil.getAsDouble(detail, "DN");
                }
        }
    }
}
