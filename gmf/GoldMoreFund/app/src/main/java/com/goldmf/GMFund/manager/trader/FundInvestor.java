package com.goldmf.GMFund.manager.trader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by cupide on 15/8/22.
 * 融资成员类
 */
public class FundInvestor implements Serializable{

    @SerializedName("id")
    public int index;

    @SerializedName("invest_time")
    public long  investTime;

    @SerializedName("investor")
    public String investorName;

    @SerializedName("cellphone")
    public String  cellphone;

    @SerializedName("invest_amount")
    public double investAmount;

    @SerializedName("status")
    public int  status;

    @SerializedName("income")
    public double income;

    public static FundInvestor translateFromJsonData(JsonObject dic){
        Gson gson = new Gson();
        try {
            return gson.fromJson(dic, FundInvestor.class);
        }
        catch (Exception ignored){
            return null;
        }
    }
}
