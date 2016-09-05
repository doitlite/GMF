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
public class ModifyPhoneProtocol extends ProtocolBase {

    public String phoneNew;
    public String verifyCode;


    public ModifyPhoneProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override

    protected boolean parseData(JsonElement data) {
        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {

        if(this.phoneNew != null)
        {
            return CHostName.HOST1 + "user/cellphone-modify-check-new";
        }
        else
        {
            return CHostName.HOST1 + "/user/cellphone-modify-check-origin";
        }
    }

    @Override
    protected Map<String, Object> getPostData() {


        HashMap<String, Object> params = new HashMap<>();
        if(this.phoneNew != null) {
            params.put("new_cellphone", this.phoneNew);
            params.put("new_verify_code", this.verifyCode);
        }else {
            params.put("origin_verify_code", this.verifyCode);
        }
        return params;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
