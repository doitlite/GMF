package com.goldmf.GMFund.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.model.StockInfo.StockSimple;
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

/**
 * 股票持仓信息
 */
public class StockPosition implements Serializable {

    public String range;         //position range
    public StockSimple stock;     //股票

    public double currentPrice;         //当前价格
    public double holdPrice;            //买入价（成本）
    public double holdCapital;         //持有市值
    public double curPosition;         //持有仓位
    public double holdAmount;          //持有数量
    public double validAmount;         //可卖数量

    public double sellPrice; //卖出（卖出成本）
    public String holdTimeStr;            //持有时间
    public double totalBuyAmount;      //累计买入数量
    public double totalSellAmount;     //累计卖出数量

    public double income;              //总收益
    public double incomeRatio;         //总收益 百分比

    public double todayIncome;              //今日收益
    public double todayIncomeRatio;         //今日收益 百分比

    public List<Order> orderList;          //该持仓的订单list详情

    public static StockPosition translateFromJsonData(JsonObject dic){
        if(dic == null || dic.isJsonNull())return null;
        try {
            StockPosition position =  new StockPosition();
            position.stock = StockSimple.translateFromJsonData(dic);
            position.range = GsonUtil.getAsString(dic, "range");
            position.currentPrice = GsonUtil.getAsDouble(dic, "last_price");
            position.holdPrice = GsonUtil.getAsDouble(dic, "cost_price");
            position.holdCapital = GsonUtil.getAsDouble(dic, "market_captial");
            position.curPosition = GsonUtil.getAsDouble(dic, "weight");
            position.holdAmount = GsonUtil.getAsDouble(dic, "total_hold_amount");
            position.validAmount = GsonUtil.getAsDouble(dic, "enable_sell_volume");
            position.sellPrice = GsonUtil.getAsDouble(dic, "sell_price");
            position.holdTimeStr = GsonUtil.getAsString(dic, "hold_time");
            position.totalBuyAmount = GsonUtil.getAsDouble(dic, "total_buy_amount");
            position.totalSellAmount = GsonUtil.getAsDouble(dic, "total_sell_amount");
            position.income = GsonUtil.getAsDouble(dic, "earning");
            position.incomeRatio = GsonUtil.getAsDouble(dic, "earning_ratio");
            position.todayIncome = GsonUtil.getAsDouble(dic, "today_earning");
            position.todayIncomeRatio = GsonUtil.getAsDouble(dic, "today_earning_ratio");
            return  position;
        }
        catch (Exception ignored){
            ignored.printStackTrace();
            return null;
        }
    }

    public static List<? extends StockPosition> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }

    public static class StockPositionV2 extends StockPosition{

        public static StockPositionV2 translateFromJsonData(JsonObject dic){
            if(dic == null || dic.isJsonNull())return null;
            try {
                StockPositionV2 position =  new StockPositionV2();

                position.stock = StockSimple.translateFromJsonData(dic);

                position.currentPrice = GsonUtil.getAsDouble(dic, "market_price");
                position.holdPrice = GsonUtil.getAsDouble(dic, "cost_price");

                position.holdCapital = GsonUtil.getAsDouble(dic, "market_value");
                position.curPosition = GsonUtil.getAsDouble(dic, "weight");
                position.holdAmount = GsonUtil.getAsDouble(dic, "total_amount");
                position.validAmount = GsonUtil.getAsDouble(dic, "enable_sell_volume");

                position.income = GsonUtil.getAsDouble(dic, "earning");
                position.incomeRatio = GsonUtil.getAsDouble(dic, "earning_ratio");
                position.todayIncome = GsonUtil.getAsDouble(dic, "today_earning");
                position.todayIncomeRatio = GsonUtil.getAsDouble(dic, "today_earning_ratio");
                return  position;
            }
            catch (Exception ignored){
                ignored.printStackTrace();
                return null;
            }
        }

        public static List<? extends StockPositionV2> translate(JsonArray list) {
            return Stream.of(Optional.of(list).or(new JsonArray()))
                    .map(it -> GsonUtil.getAsJsonObject(it))
                    .map(it -> translateFromJsonData(it))
                    .filter(it -> it != null)
                    .collect(Collectors.toList());
        }
    }
}
