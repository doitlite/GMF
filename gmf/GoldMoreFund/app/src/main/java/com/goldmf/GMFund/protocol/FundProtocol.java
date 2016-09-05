package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.model.Fund;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/8/3.
 */
public class FundProtocol extends ProtocolBase {

    public int fundID;

    public Fund fund;

    public FundProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override

    protected boolean parseData(JsonElement data) {

        if(super.returnCode == 0 && data.isJsonObject())
        {
            this.fund = Fund.translateFromJsonData(data.getAsJsonObject());
        }

        return (super.returnCode == 0 && data.isJsonObject());
    }

    @Override
    protected String getUrl() {
        return CHostName.HOST1 + "product/detail";
    }

    @Override
    protected Map<String, Object> getPostData() {
        return null;
    }

    @Override
    protected Map<String, String> getParam() {

        HashMap<String,String> param = new HashMap<>();
        param.put("product_id", Integer.toString(this.fundID));

        return param;
    }
}
