package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo.PayAction;
import com.goldmf.GMFund.manager.cashier.ServerMsg;
import com.goldmf.GMFund.manager.fortune.Coupon;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/8/22.
 */
public class InvestProtocol extends ProtocolBase {

    public int fundID;
    public double investAmount;
    public String couponID;

    private ServerMsg<Object> msg;
    public double rechargeAmount;
    public ServerMsg<Object> getMsg() {

        return msg;
    }

    public InvestProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {
        if(super.returnCode == 0 && data.isJsonObject()) {
            JsonObject dicData = data.getAsJsonObject();

            this.rechargeAmount = GsonUtil.getAsDouble(dicData, "deposit_amount");

            this.msg = ServerMsg.translateFromJsonData(dicData);
            if (this.msg != null) {
                if(this.rechargeAmount > 0){
                    this.msg.setData(this.rechargeAmount);
                }else{
                    PayAction action = PayAction.translateFromJsonData(GsonUtil.getAsJsonObject(dicData, "action"));
                    this.msg.setData(action);
                }
            }
        }
        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {
        return CHostName.HOST1 + "payment/create-invest";
    }

    @Override
    protected Map<String, Object> getPostData() {

        HashMap<String, Object> params = new HashMap<>();
        params.put("product_id", String.valueOf(this.fundID));
        params.put("amount", String.valueOf(this.investAmount));
        if(couponID != null )
            params.put("coupon_id", this.couponID);

        return params;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
