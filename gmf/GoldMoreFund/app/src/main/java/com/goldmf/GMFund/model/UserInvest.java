package com.goldmf.GMFund.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by cupide on 15/8/25.
 * 投资fund （组合、委托）收益类
 */

public class UserInvest implements Serializable{

    @SerializedName("invest_amount")
    public double investMoney;       //投资金额
    @SerializedName("invest_time")
    public long  redeemTime;   //赎回时间

    @SerializedName("income")
    public double totalIncome;       //合计收益
    @SerializedName("income_ratio")
    public double totalIncomeRatio;  //合计收益 百分比

    public static UserInvest translateFromJsonData(JsonObject dic){
        Gson gson = new Gson();
        try {
            return gson.fromJson(dic, UserInvest.class);
        }
        catch (Exception ignored){
            return null;
        }
    }

}
