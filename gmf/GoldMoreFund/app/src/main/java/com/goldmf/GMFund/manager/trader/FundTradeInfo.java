package com.goldmf.GMFund.manager.trader;

import com.goldmf.GMFund.model.Order;
import com.goldmf.GMFund.model.SimulationAccount;
import com.goldmf.GMFund.model.StockPosition;
import com.goldmf.GMFund.model.StockPosition.StockPositionV2;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cupide on 16/8/5.
 */
public class FundTradeInfo {
    public double totalCapital;      //总资产
    public double capitalValue;      //股票市值
    public double cashBanlance;      //可用资金

    public double income;            //总盈亏、总收益
    public double incomeRatio;       //总收益率

    public double position;          //仓位
    public double dayIncome;         //当日盈亏
    public double dayIncomeRatio;    //当日盈亏率

    public List<StockPositionV2> holdStocks = new ArrayList<>(); //持仓股票信息StockPositionV2

    public void readFromeJsonData(JsonObject dic) {
        this.totalCapital = GsonUtil.getAsDouble(dic, "total_assets");
        this.capitalValue = GsonUtil.getAsDouble(dic, "security");
        this.cashBanlance = GsonUtil.getAsDouble(dic, "balance_amount");

        this.income = GsonUtil.getAsDouble(dic, "net_profite");
        this.incomeRatio = GsonUtil.getAsDouble(dic, "profit_rate");

        this.position = GsonUtil.getAsDouble(dic, "position");
        this.dayIncome = GsonUtil.getAsDouble(dic, "profit_of_today");
        this.dayIncomeRatio = GsonUtil.getAsDouble(dic, "profit_rate_of_day");
    }
}
