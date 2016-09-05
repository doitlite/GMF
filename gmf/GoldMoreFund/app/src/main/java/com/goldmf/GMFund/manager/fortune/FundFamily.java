package com.goldmf.GMFund.manager.fortune;

import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.ModelSerialization;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by cupide on 15/9/2.
 */
public class FundFamily implements Serializable{

    @SerializedName("market")
    public int moneyType; // 持有的组合类别;
    @SerializedName("running_num")
    private int runingNumber; // 持有组合的runing个数
    @SerializedName("collecting_num")
    private int capitalNumber; // 持有的组合的募集个数

    @SerializedName("income")
    public double income ;      // 累计收益
    @SerializedName("income_ratio")
    public double incomeRatio;  // 收益率

    @SerializedName("my_total_capital")
    public double totalCapital;  // 所有金额

    public double investMoney;  // 已投资金额

    @SerializedName("cash_remain")
    public double cashBalance;  // 结余现金


    @SerializedName("today_deposit")
    public double todayDeposit;//今天已经冲的金额

    /**
     * 持有的组合个数
     * @return
     */
    public int number(){
        return this.runingNumber + this.capitalNumber;
    }


    public static FundFamily translateFromJsonData(JsonObject dic){
        Gson gson = new Gson();
        try {
            FundFamily info = gson.fromJson(dic, FundFamily.class);

            info.investMoney = (GsonUtil.getAsDouble(dic, "collecting_capital")
                    + GsonUtil.getAsDouble(dic, "running_capital"));

            return info;
        }
        catch (Exception ignored){
            return null;
        }
    }


    transient ModelSerialization<FundFamily> mSerialization = new ModelSerialization<FundFamily>(this);

    public static FundFamily loadData(String key) {
        return ModelSerialization.loadByKey(key, FundFamily.class, true);
    }

    public void save(String key) {
        mSerialization.saveByKey(key, true);
    }

    public void remove(String key){
        mSerialization.removeByKey(key, true);
    }

}
