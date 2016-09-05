package com.goldmf.GMFund.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.manager.stock.StockCache;
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

import static com.goldmf.GMFund.extension.ObjectExtension.*;

/**
 * Created by cupide on 15/7/29.
 */

public class StockInfo {

    public static class StockSimple implements Serializable {

        public String index;               //股票标示码（唯一id）
        public String name;                //股票名称
        public String code;                //股票代码
        public String type;                //股票类型，SZ

        @Override
        public int hashCode() {
            return name.hashCode() + code.hashCode() + type.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof StockBrief && this.hashCode() == o.hashCode();
        }

        public void readFromeJsonData(JsonObject dic) {
            this.index = GsonUtil.getAsString(dic, "stock_id");
            this.name = GsonUtil.getAsString(dic, "stock_name");
            this.code = GsonUtil.getAsString(dic, "stock_code");
            this.type = GsonUtil.getAsString(dic, "exchange");
        }


        public static StockSimple getStockSimpleByIndex(String index) {
            return StockCache.getInstance().getStockSimple(index);
        }

        public static StockSimple translateFromJsonData(JsonObject dic) {
            try {
                StockSimple info = new StockSimple();
                info.readFromeJsonData(dic);
                return info;
            }
            catch (Exception ignored){
                return null;
            }
        }

        public static List<? extends StockSimple> translate(JsonArray list) {
            return Stream.of(Optional.of(list).or(new JsonArray()))
                    .map(it -> GsonUtil.getAsJsonObject(it))
                    .map(it -> translateFromJsonData(it))
                    .filter(it -> it != null)
                    .collect(Collectors.toList());
        }

    }

    public static class StockBrief extends StockSimple {

        public static int STOCK_CLASS_TYPE_NORMAL = 0;              //0: 股票
        public static int STOCK_CLASS_TYPE_FUND = 1;                //1: 基金
        public static int STOCK_CLASS_TYPE_GOVERNMENT_LOAN = 2;     //2: 国债回购
        public static int STOCK_CLASS_TYPE_SPEC = 3;                //3: 指数

        public int moneyType;               //股票对应的money类型
        public double last;                 //现价
        public double change;              //升跌
        public double changeRatio;         //升跌%

        public Double prevClose;           //昨日收盘价, 可能为空
        public int stockClassType;          //股票class类型，见STOCK_CLASS_TYPE

        public int marketSatus;             //市场状态int
        public String marketInfo;           //市场状态string

        @Override
        public void readFromeJsonData(JsonObject dic) {
            super.readFromeJsonData(dic);
            this.moneyType = GsonUtil.getAsInt(dic, "market");
            this.last = GsonUtil.getAsDouble(dic, "last_price");
            this.change = GsonUtil.getAsDouble(dic, "change");
            this.changeRatio = GsonUtil.getAsDouble(dic, "change_ratio");

            this.prevClose = GsonUtil.getAsNullableDouble(dic, "prev_close_price");
            this.stockClassType = GsonUtil.getAsInt(dic, "asset_class");

            this.marketSatus = GsonUtil.getAsInt(dic, "market_status");
            this.marketInfo = GsonUtil.getAsString(dic, "market_info");
        }

        public static StockBrief translateFromJsonData(JsonObject dic) {
            if (dic == null || dic.isJsonNull()) return null;
            try {
                StockBrief stock = new StockBrief();
                stock.readFromeJsonData(dic);
                return stock;
            } catch (Exception ignored) {
                return null;
            }
        }

        public static List<? extends StockBrief> translate(JsonArray list) {
            return Stream.of(Optional.of(list).or(new JsonArray()))
                    .map(it -> GsonUtil.getAsJsonObject(it))
                    .map(it -> translateFromJsonData(it))
                    .filter(it -> it != null)
                    .collect(Collectors.toList());
        }
    }
}

