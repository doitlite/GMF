package com.goldmf.GMFund.manager.stock;

import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Created by cupide on 16/4/14.
 */
public class RealUserIncomeChart extends UserIncomeChart {

    public Double totalIncome;
    public Double totalIncomeRatio;
    public int investFundCount;

    public void readFromeJsonData(JsonObject dic) {
        super.readFromeJsonData(dic);

        this.totalIncome = GsonUtil.getAsDouble(dic, "stats", "total_income");
        this.totalIncomeRatio = GsonUtil.getAsDouble(dic, "stats", "total_income_ratio");
        this.investFundCount = GsonUtil.getAsInt(dic, "stats", "invest_portfolio_num");
    }

    public static RealUserIncomeChart translateFromJsonData(JsonObject dic) {
        if (dic == null)
            return null;

        RealUserIncomeChart chart = new RealUserIncomeChart();
        chart.readFromeJsonData(dic);
        return chart;
    }
}
