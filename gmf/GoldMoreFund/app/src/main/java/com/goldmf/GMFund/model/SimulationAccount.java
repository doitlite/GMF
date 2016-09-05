package com.goldmf.GMFund.model;

import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by cupide on 16/3/11.
 */
public class SimulationAccount {

    public double fortuneTurnover;       //资产市值(见GMFFundBrief)
    public double marketTurnover;        //证券市值
    public double cashBalance;           //账户余额
    public double curPosition;           //当前仓位

    public double totalIncome;           //总收益
    public double totalIncomeRatio;      //总收益百分比

    public double todayIncome;           //今日收益
    public double todayIncomeRatio;      //今日收益百分比

    public static SimulationAccount translateFromJsonData(JsonObject dic){
        if(dic == null || dic.isJsonNull())return null;
        try {
            SimulationAccount info = new SimulationAccount();
            info.fortuneTurnover = GsonUtil.getAsDouble(dic, "total_capital");
            info.marketTurnover = GsonUtil.getAsDouble(dic, "market_capital");
            info.cashBalance = GsonUtil.getAsDouble(dic, "cash_balance");
            info.curPosition = GsonUtil.getAsDouble(dic, "cur_position");
            info.totalIncome = GsonUtil.getAsDouble(dic, "total_income");
            info.totalIncomeRatio = GsonUtil.getAsDouble(dic, "total_income_ratio");
            info.todayIncome = GsonUtil.getAsDouble(dic, "today_income");
            info.todayIncomeRatio = GsonUtil.getAsDouble(dic, "today_income_ratio");
            return info;
        }
        catch (Exception ignored){
            return null;
        }
    }
}
