package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/7/27.
 */
public class RegisterProtocol extends LoginProtocol {


    public String verifyCode;
    public String nickName;
    public String invitedId;

    public RegisterProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {

        return super.parseData(data);
    }

    @Override
    protected String getUrl() {
        return CHostName.HOST1 + "user/register";
    }

    @Override
    protected Map<String, Object> getPostData() {


        Map<String, Object> param = super.getPostData();

        param.put("nick_name", nickName);
        param.put("verify_code", verifyCode);
        param.put("invited_id", invitedId);

        return param;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
