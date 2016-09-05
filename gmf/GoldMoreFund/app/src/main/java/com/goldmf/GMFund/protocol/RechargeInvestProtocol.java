package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.manager.fortune.Coupon;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/10/9.
 */
public class RechargeInvestProtocol extends ProtocolBase {

    public int fundID;
    public double amount;
    public String couponID;

    private String investTr;

    public String getInvestTr(){return  investTr;}

    public RechargeInvestProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override

    protected boolean parseData(JsonElement data) {
        if (this.returnCode == 0 && data.isJsonObject()) {
            this.investTr = GsonUtil.getAsString(data, "invest_tr");
        }
        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {
        return CHostName.HOST1 + "cashier/create_invest_tr";
    }

    @Override
    protected Map<String, Object> getPostData() {

        HashMap<String, Object> params = new HashMap<>();

        params.put("amount", String.valueOf(this.amount));
        params.put("product_id", this.fundID);
        if(couponID != null )
            params.put("coupon_id", this.couponID);

        return params;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
