package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/11/7.
 */
public class BookFundProtocol extends ProtocolBase {

    public FundBrief sFund;

    public BookFundProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override

    protected boolean parseData(JsonElement data) {
        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {
        return CHostName.HOST1 + "product/book";
    }

    @Override
    protected Map<String, Object> getPostData() {

        HashMap<String, Object> params = new HashMap<>();
        params.put("product_id", String.valueOf(this.sFund.index));
        return params;

    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
