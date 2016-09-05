package com.goldmf.GMFund.manager.fortune;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.protocol.base.PageArray.PageItemIndex;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import yale.extension.common.Optional;

/**
 * Created by cupide on 15/10/5.
 */
public class AccountTradeInfo extends PageItemIndex {

    private String orderId;
    public int moneyType;
    public int transactType;
    public long transactTime;
    public String transactText;
    public String detail;
    public String statusText;
    public double amount;

    private void readFromeJsonData(JsonObject dic) {
        this.orderId = GsonUtil.getAsString(dic, "order_id");
        this.moneyType = GsonUtil.getAsInt(dic, "market");
        this.transactTime = GsonUtil.getAsLong(dic, "transact_time");
        this.transactText = GsonUtil.getAsString(dic, "transact_type_text");
        this.transactType = GsonUtil.getAsInt(dic, "transact_type");
        this.detail = GsonUtil.getAsString(dic, "detail");
        this.statusText = GsonUtil.getAsString(dic, "status_text");
        this.amount = GsonUtil.getAsDouble(dic, "amount");

        int type = GsonUtil.getAsInt(dic, "type");
        if(type == 2)
            this.amount = 0 - Math.abs(this.amount);
    }


    public static AccountTradeInfo translateFromJsonData(JsonObject dic){
        try {
            AccountTradeInfo info = new AccountTradeInfo();
            info.readFromeJsonData(dic);
            return info;
        }
        catch (Exception ignored){
            return null;
        }
    }

    public static List<? extends AccountTradeInfo> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }

    @Override
    public Object getKey() {
        return orderId;
    }

    @Override
    public long getTime() {
        return transactTime;
    }


}
