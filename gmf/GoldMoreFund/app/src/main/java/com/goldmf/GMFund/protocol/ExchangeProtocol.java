package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/10/28.
 */
public class ExchangeProtocol extends ProtocolBase {

    public int moneyType;
    public double amount;

    public ExchangeProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {

        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {
        return CHostName.HOST1 + "bounty/exchange";
    }

    @Override
    protected Map<String, Object> getPostData() {

        HashMap<String, Object> params = new HashMap<>();
        params.put("market", String.valueOf(this.moneyType));
        params.put("amount", String.valueOf(this.amount));

        return params;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
