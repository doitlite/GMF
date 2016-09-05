package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.manager.cashier.BankCard;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo;
import com.goldmf.GMFund.manager.cashier.ServerMsg;
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
public class RechargeProtocol extends ProtocolBase {

    public double amount;
    public String orderID;
    public boolean isInvest = false;//是否是投资充值

    private ServerMsg<RechargeDetailInfo> msg;


    public RechargeProtocol(ProtocolCallback callback) {
        super(callback);
    }

    public ServerMsg<RechargeDetailInfo> getMsg() {

        return msg;
    }

    @Override
    protected boolean parseData(JsonElement data) {
        if(super.returnCode == 0 && data.isJsonObject()) {
            JsonObject dicData = data.getAsJsonObject();

            this.orderID = GsonUtil.getAsString(dicData, "order_id");

            RechargeDetailInfo info = RechargeDetailInfo.translateFromJsonData(dicData);
            this.msg = ServerMsg.<RechargeDetailInfo>translateFromJsonData(dicData);
            if (this.msg != null)
                this.msg.setData(info);

        }
        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {
        return CHostName.HOST1 + "payment/create-hosting-deposit";
    }

    @Override
    protected Map<String, Object> getPostData() {

        HashMap<String, Object> params = new HashMap<>();
        params.put("is_deposit_invest", isInvest?"1":"0");

        if(this.orderID != null){
            params.put("order_id", this.orderID);
        }else {
            params.put("amount", this.amount);
        }
        return params;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
