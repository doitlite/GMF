package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/9/2.
 */
public class AuthenticateProtocol extends ProtocolBase {
    public String name;
    public String ID;

    public AuthenticateProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {
        return (returnCode == 0);
    }

    @Override
    protected String getUrl() {
        return CHostName.HOST1 + "user/real-name-check";
    }

    @Override
    protected Map<String, Object> getPostData() {

        HashMap<String, Object> params = new HashMap<>();
        params.put("real_name", this.name);
        params.put("identity_card", this.ID);

        return params;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
