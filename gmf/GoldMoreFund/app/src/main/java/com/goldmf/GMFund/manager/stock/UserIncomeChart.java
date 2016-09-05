package com.goldmf.GMFund.manager.stock;

import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by cupide on 16/3/12.
 */
public class UserIncomeChart {

    @SerializedName("market")
    public int money;

    public String timeName;        //时间轴的名字
    public List<String> timeData;         //时间轴的数据

    public String valueName;       //第一条线的名字
    public List<Double> valueData;        //第一条线的数据

    public String contrastName;    //比对线的名字
    public List<Double> contrastData;     //比对线的数据


    public void readFromeJsonData(JsonObject dic) {
        //TODO 这个market是个什么鬼!!!!!
        this.money = GsonUtil.getAsInt(dic, "market");

        JsonElement curve = GsonUtil.getAsJsonElement(dic, "curve");
        this.timeName = GsonUtil.getAsString(curve, "time_name");
        this.timeData = GsonUtil.getAsStringList(GsonUtil.getAsJsonArray(curve, "time_dots"));

        this.valueName = GsonUtil.getAsString(curve, "user_name");
        this.valueData = GsonUtil.getAsDoubleList(GsonUtil.getAsJsonArray(curve, "user_dots"));

        this.contrastName = GsonUtil.getAsString(curve, "compare_index_name");
        this.contrastData = GsonUtil.getAsDoubleList(GsonUtil.getAsJsonArray(curve, "compare_index_dots"));

    }

    public static UserIncomeChart translateFromJsonData(JsonObject dic) {
        if (dic == null)
            return null;

        UserIncomeChart chart = new UserIncomeChart();
        chart.readFromeJsonData(dic);
        return chart;
    }
}
