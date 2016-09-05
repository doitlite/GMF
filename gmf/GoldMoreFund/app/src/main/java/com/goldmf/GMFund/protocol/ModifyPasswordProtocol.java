package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/7/28.
 */
public class ModifyPasswordProtocol extends ProtocolBase {

    public boolean reset;
    public String certificate;
    public  String newPassword;
    public  String confirmPassword;

    public ModifyPasswordProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {
        return (returnCode == 0);
    }

    @Override
    protected String getUrl() {
        return reset? CHostName.HOST1 + "user/pwd-reset":CHostName.HOST1 + "user/pwd-modify";
    }

    @Override
    protected Map<String, Object> getPostData() {

        HashMap<String,Object> param = new HashMap<>();
        param.put(reset?"verify_code":"old_passwd",  certificate);
        param.put("new_passwd", newPassword);
        param.put("confirm_passwd", confirmPassword);

        return param;
    }

    @Override
    protected Map<String, String> getParam() {

        return null;
    }
}
