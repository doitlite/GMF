package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/10/5.
 */
public class ContactUsProtocol extends ProtocolBase {

    public String phone;
    public String msg;

    public ContactUsProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {
        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {
        return CHostName.HOST1 + "feedback";
    }

    @Override
    protected Map<String, Object> getPostData() {

        HashMap<String, Object> params = new HashMap<>();
        params.put("cellphone", this.phone);
        params.put("cont", this.msg);
        return params;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
