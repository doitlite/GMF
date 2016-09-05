package com.goldmf.GMFund.manager.stock;

import com.goldmf.GMFund.model.SimulationAccount;
import com.goldmf.GMFund.model.StockPosition;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cupide on 16/3/12.
 */
public class SimulationAccountMore {

    public User user;                   //用户信息
    public SimulationAccount account;    //新增加的虚拟股票账户

    public List<StockPosition> holdStocks = new ArrayList<>();      //持仓股票信息GMFStockPosition
    public List<StockPosition> historyStocks = new ArrayList<>();   //历史股票持仓信息GMFStockPosition

    public void readFromeJsonData(JsonObject dic) {
        this.user = User.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "user_info"));
        this.account = SimulationAccount.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "accout_balance"));

        this.holdStocks.addAll(StockPosition.translate(GsonUtil.getAsJsonArray(dic, "hold_stocks")));
        this.historyStocks.addAll(StockPosition.translate(GsonUtil.getAsJsonArray(dic, "history_stocks")));
    }
}
