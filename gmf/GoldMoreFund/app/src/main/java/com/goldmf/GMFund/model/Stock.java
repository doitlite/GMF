package com.goldmf.GMFund.model;

import android.support.annotation.Nullable;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.model.StockInfo.*;

/**
 * Created by yale on 15/7/20.
 * 股票
 */

public class Stock extends StockBrief {

    public boolean suspension;      //是否停牌

    public Double open;           //当日开盘价

    public Double rangeMax;       //当日最高
    public Double rangeMin;       //当日最低


    public Double volume;         //当日成交量
    public Double turnover;       //当日成交金额


    public @Nullable Double PE;          //市盈率
    public @Nullable  Double mktCap;               //市值
    public Double priceLevel;           //价格档

    public @Nullable Double incomePer;     //每股收益

    public int numberPerHand;    //每手多少股

    public Double surgedLimit;   //涨停板价格
    public Double declineLimit;   //跌停板价格

    public long lastFreshTime;   //上一次行情的更新时间

    public Double heghest52W;     //52周最高
    public Double lowest52W;     //52周最低
    public Double fundUnitValue; //基金单位净值
    public Double fundAccuValue; //基金累计净值
    public String fundNetDate; //净值日期
    public Double amplitudeValue; //当日振幅


    public List<Quotation> bids; //五档买入
    public List<Quotation> asks; //五档卖出

    public ShareInfo shareImageInfo; //分享图片详情

    public transient StockPosition stockPosition;

    @Override
    public void readFromeJsonData(JsonObject dic) {
        super.readFromeJsonData(dic);

        this.open = GsonUtil.getAsNullableDouble(dic, "open_price");
        this.rangeMax = GsonUtil.getAsNullableDouble(dic, "high_price");
        this.rangeMin = GsonUtil.getAsNullableDouble(dic, "low_price");

        this.volume = GsonUtil.getAsNullableDouble(dic, "volume");
        this.turnover = GsonUtil.getAsNullableDouble(dic, "turnover");

        this.mktCap = GsonUtil.getAsNullableDouble(dic, "market_capital");
        this.PE = GsonUtil.getAsNullableDouble(dic, "pe");
        this.incomePer = GsonUtil.getAsNullableDouble(dic, "income_per");

        this.priceLevel = GsonUtil.getAsNullableDouble(dic, "price_level");

        this.numberPerHand = GsonUtil.getAsInt(dic, "share_per_hand");    //每手多少股

        this.surgedLimit = GsonUtil.getAsNullableDouble(dic, "surged_limit_price");
        this.declineLimit = GsonUtil.getAsNullableDouble(dic, "decline_limit_price");

        this.lastFreshTime = GsonUtil.getAsLong(dic, "sec_timestamp");

        this.heghest52W = GsonUtil.getAsNullableDouble(dic, "52w_highest");
        this.lowest52W = GsonUtil.getAsNullableDouble(dic, "52w_lowest");
        this.fundUnitValue = GsonUtil.getAsNullableDouble(dic, "unit_net_value");
        this.fundAccuValue = GsonUtil.getAsNullableDouble(dic, "accu_net_value");
        this.fundNetDate = GsonUtil.getAsNullableString(dic, "net_value_updated_date");
        this.amplitudeValue = GsonUtil.getAsNullableDouble(dic, "amplitude");

        this.suspension = (GsonUtil.getAsInt(dic, "is_suspension") == 1);

        this.bids = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Quotation bid = new Quotation();
            bid.amount = GsonUtil.getAsDouble(dic, "bid" + String.valueOf(i) + "_volume");
            bid.price = GsonUtil.getAsDouble(dic, "bid" + String.valueOf(i) + "_price");
            this.bids.add(bid);
        }

        this.asks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Quotation ask = new Quotation();
            ask.amount = GsonUtil.getAsDouble(dic, "ask" + String.valueOf(i) + "_volume");
            ask.price = GsonUtil.getAsDouble(dic, "ask" + String.valueOf(i) + "_price");
            this.asks.add(ask);
        }

        this.shareImageInfo = ShareInfo.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "share_info"));
    }

    public static Stock translateFromJsonData(JsonObject dic) {

            Stock stock = new Stock();
            stock.readFromeJsonData(dic);
            return stock;
    }

    public static List<? extends Stock> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }

    public static class Quotation implements Serializable{
        public double price;
        public double amount;

        public Quotation init(double price, double amount) {
            this.price = price;
            this.amount = amount;
            return this;
        }
    }
}
