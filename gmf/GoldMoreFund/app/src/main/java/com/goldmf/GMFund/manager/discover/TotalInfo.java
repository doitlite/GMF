package com.goldmf.GMFund.manager.discover;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by cupide on 15/9/28.
 */
public class TotalInfo implements Serializable {

    @SerializedName("total_invest_amount")
    public double investMoney;     //投资金额

    @SerializedName("total_invest_count")
    public long investCount;     //投资人次

    @SerializedName("max_income_ratio")
    public double maxIncomeRatio;  //投资最高收益

    @SerializedName("running_product_count")
    public int runningFund;  //正在运行组合个数
    @SerializedName("total_manager_product_count")
    public int totalFund;  //累计管理组合个数
    @SerializedName("total_trader_count")
    public int totalTrader;  //已上线操盘手

    public static TotalInfo translateFromJsonData(JsonObject dic){
        Gson gson = new Gson();
        try {
            return gson.fromJson(dic, TotalInfo.class);
        }
        catch (Exception ignored){
            return null;
        }
    }
}
