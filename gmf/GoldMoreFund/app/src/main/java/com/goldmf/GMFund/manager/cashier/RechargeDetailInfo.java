package com.goldmf.GMFund.manager.cashier;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonObject;

import java.io.Serializable;

import static com.goldmf.GMFund.manager.cashier.BankCard.Pay_Channel_Fuyou;
import static com.goldmf.GMFund.manager.cashier.BankCard.Pay_Channel_No;
import static com.goldmf.GMFund.manager.cashier.BankCard.Pay_Channel_Sina;

/**
 * Created by cupide on 16/5/26.
 */
public class RechargeDetailInfo implements Serializable {
    public String orderID;   //订单id
    public double rechargeTotalAmount;   //总金额
    public double rechargeFinishAmount;  //已经完成金额
    public boolean multiple;        //是否多次？
    public int totalCount;       //总次数

    public int currentCount;     //当前次数
    public double currentRechargeAmount;     //当前充值金额
    public double cashBalance;               //用户当前余额
    public PayAction rechargePayAction;   //充值方式
    public String rechargeDepositTips; //充值提示


    private void readFromJsonData(JsonObject dic) {
        this.orderID = GsonUtil.getAsString(dic, "order_id");
        this.rechargeTotalAmount = GsonUtil.getAsDouble(dic, "total_amount");
        this.rechargeFinishAmount = GsonUtil.getAsDouble(dic, "finish_amount");
        this.multiple = GsonUtil.getAsBoolean(dic, "multiple_type");
        this.totalCount = GsonUtil.getAsInt(dic, "total_count");

        JsonObject current = GsonUtil.getAsJsonObject(dic, "current");
        this.currentCount = GsonUtil.getAsInt(current, "current_count");
        this.currentRechargeAmount = GsonUtil.getAsDouble(current, "deposit_amount");
        this.cashBalance = GsonUtil.getAsDouble(current, "cash_balance");
        this.rechargePayAction = PayAction.translateFromJsonData(current);
        this.rechargeDepositTips = GsonUtil.getAsString(current, "deposit_tips");
    }

    /**
     * 判断充值是否完成
     *
     * @return
     */
    public final boolean isFinish() {

        return this.currentCount >= this.totalCount;
    }

    public static RechargeDetailInfo translateFromJsonData(JsonObject dic) {
        try {
            RechargeDetailInfo info = new RechargeDetailInfo();
            info.readFromJsonData(dic);
            return info;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static class PayAction implements Serializable {

        public interface SuccessCallBack {

            void onRun(final MResults<Void> results);
        }

        public String payChannel;       //本次充值方式
        public String url;              //支付方url地址

        public SuccessCallBack success; //成功回调

        /**
         * web调用成功后的回调
         * @param results
         */
        public final void successCall(final MResults<Void> results){

            if(success != null){
                success.onRun(results);
            }
        }

        public static PayAction translateFromJsonData(JsonObject dic) {
            if(dic == null || dic.isJsonNull())
                return null;

            try {
                PayAction action = new PayAction();
                action.payChannel = getAsPayChannelString(dic, "pay_channel");
                action.url = GsonUtil.getAsString(dic, "pay_url");
                return action;
            } catch (Exception ignored) {
                return null;
            }
        }

        public static String getAsPayChannelString(JsonObject dic, String key) {
            String string = GsonUtil.getAsString(dic, key);
            if (string.equals(Pay_Channel_Sina))
                return Pay_Channel_Sina;
            else if (string.equals(Pay_Channel_Fuyou))
                return Pay_Channel_Fuyou;
            else
                return Pay_Channel_No;
        }
    }
}
