package com.goldmf.GMFund.manager.cashier;

import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by cupide on 15/9/3.
 */
public class BankInfo implements Serializable{

    @SerializedName("bank_code")
    public String bankCode;

    @SerializedName("bank_name")
    public String bankName;

    @SerializedName("bank_icon")
    public String bankIcon;

    @SerializedName("deposit_single_limit")
    public double limit;        //每单限额

    @SerializedName("deposit_day_limit")
    public double dayLimit;     //每日限额

    public String payChannel;

    @Override
    public String toString() {
        return "BankInfo{" +
                "bankCode='" + bankCode + '\'' +
                ", bankName='" + bankName + '\'' +
                ", bankIcon='" + bankIcon + '\'' +
                ", limit=" + limit +
                ", dayLimit=" + dayLimit +
                ", payChannel='" + payChannel + '\'' +
                '}';
    }

    public static BankInfo translateFromJsonData(JsonObject dic) {
        Gson gson = new Gson();
        try {
            BankInfo info = gson.fromJson(dic, BankInfo.class);
            info.payChannel = RechargeDetailInfo.PayAction.getAsPayChannelString(dic, "pay_channel");
            return info;
        } catch (Exception ignored) {
            return null;
        }
    }
}
