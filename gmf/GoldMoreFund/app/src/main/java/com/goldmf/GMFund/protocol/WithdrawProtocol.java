package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.manager.cashier.BankCard;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo.PayAction;
import com.goldmf.GMFund.manager.cashier.ServerMsg;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/9/7.
 */
public class WithdrawProtocol extends ProtocolBase {

    public int moneyType;
    public double amount;

    public ServerMsg<PayAction> msg;

    public WithdrawProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {
        if (this.moneyType == Money_Type.CN) {
            if (this.returnCode == 0 && data.isJsonObject()) {

                JsonObject dic = GsonUtil.getAsJsonObject(data);
                PayAction action = PayAction.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "action"));
                this.msg = ServerMsg.<PayAction>translateFromJsonData(dic);
                if(this.msg != null) {
                    this.msg.setData(action);
                }
            }
        }
        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {
        if(moneyType == Money_Type.CN)
            return CHostName.HOST1 + "payment/create-hosting-withdraw";
        else
            return CHostName.HOST1 + "cashier/withdraw/foreign";
    }

    @Override
    protected Map<String, Object> getPostData() {

        HashMap<String, Object> params = new HashMap<>();

        if(moneyType == FundBrief.Money_Type.CN) {
        }
        else{
            params.put("market", String.valueOf(this.moneyType));
        }

        params.put("amount", String.valueOf(this.amount));

        return params;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
