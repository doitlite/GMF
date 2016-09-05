package com.goldmf.GMFund.manager.stock;

import com.goldmf.GMFund.model.StockInfo.StockSimple;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonObject;

import java.io.Serializable;

/**
 * Created by cupide on 15/8/22.
 */
public class StockTradeQualify  implements Serializable {

    public StockSimple stock;//购买股票
    public int fundID;    //购入的Fund ID
    public int maxBuyCount;  //最大可买
    public int maxSellCount; //最大可卖

    public StockTradeQualify(StockSimple stock, int fundID){
        this.stock = stock;
        this.fundID = fundID;
    }

    public  void readFromeJsonData(JsonObject dic){
        this.maxBuyCount = GsonUtil.getAsInt(dic, "max_buy_amount");
        this.maxSellCount = GsonUtil.getAsInt(dic, "max_sell_amount");
    }
}
