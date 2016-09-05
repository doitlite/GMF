package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.protocol.base.ProtocolManager;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

import yale.extension.common.Optional;

/**
 * Created by cupide on 15/7/28.
 */
public class LoginProtocol extends ProtocolBase {


    public String cellphone;
    public String password;

    public LoginProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {
        if(returnCode == 0 && data.isJsonObject()){

            String appToken = GsonUtil.getAsString(data, "app_token");
            ProtocolManager.getInstance().setAppToken(appToken);

            String snsToken = GsonUtil.getAsString(data, "sns_token");
            ProtocolManager.getInstance().setSNSToken(snsToken);
        }
        return (returnCode == 0);
    }

    @Override
    protected String getUrl() {
        return CHostName.HOST1 + "user/login";
    }

    @Override
    protected Map<String, Object> getPostData() {

        HashMap<String, Object> params = new HashMap<>();
        params.put("cellphone", cellphone);
        params.put("password", password);

        return params;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
