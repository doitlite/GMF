package com.goldmf.GMFund.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

import yale.extension.common.Optional;

/**
 * Created by cupide on 16/3/12.
 */
public class FeedOrder extends Feed {

    public static final int Feed_Type_begin = 0;
    public static final int Feed_Type_buy = 101; //证券买入成交
    public static final int Feed_Type_sell = 102; //证券卖出成交

    public Order orderInfo;
    public User user;             //操作的用户
    public SimulationAccount stockAccount; //操作的用户的账号

    public void readFromeJsonData(JsonObject dic) {
        super.readFromeJsonData(dic);
        this.orderInfo = Order.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "order_brief"));
        this.user = User.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "user"));
        this.stockAccount = SimulationAccount.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "account_balance"));
    }

    public static FeedOrder translateFromJsonData(JsonObject dic){
        if (dic == null || dic.isJsonNull())
            return null;

        try {
            FeedOrder info = new FeedOrder();
            info.readFromeJsonData(dic);
            return info;
        }
        catch (Exception ignored){
            return null;
        }
    }

    public static List<? extends FeedOrder> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }
}
