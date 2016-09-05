package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.protocol.base.ProtocolManager;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/8/22.
 */
public class TraderPSProtocol extends ProtocolBase {

    public String certificate = null;
    public String PSOld = null;
    public String PSNew = null;
    public String PSEnter = null;
    public boolean set;   //设置密码： true：第一次设置交易密码，false：后面的输入交易密码

    private Boolean isResetPS(){
        return this.certificate != null;
    }

    private Boolean isEnterPS(){
        return this.PSEnter != null;
    }

    public TraderPSProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {
        if(super.returnCode == 0)
        {
            if(this.set == false && this.isEnterPS()  && data.isJsonObject())
            {
                String traderToken = GsonUtil.getAsString(data.getAsJsonObject(), "trade_token");
                ProtocolManager.getInstance().setTradeToken(traderToken);
            }
        }
        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {

        if(this.isEnterPS())
        {
            if(this.set)
                return CHostName.HOST1 + "user/trade-pwd-set";
            else
                return CHostName.HOST1 + "trade/auth";
        }
        if (this.isResetPS())
        {
            return CHostName.HOST1 + "user/trade-pwd-reset";
        }

        return CHostName.HOST1 + "user/trade-pwd-modify";

    }

    @Override
    protected Map<String, Object> getPostData() {

        HashMap<String,Object> param = new HashMap<>();

        if(this.isEnterPS()){
            param.put("trade_passwd", this.PSEnter);
        }
        if(this.isResetPS()){
            param.put("verify_code", this.certificate);
            param.put("new_trade_passwd", this.PSNew);
        }
        else
        {
            param.put("old_trade_passwd", this.PSOld);
            param.put("new_trade_passwd", this.PSNew);
        }

        return param;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
