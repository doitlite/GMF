package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.manager.cashier.BankCard;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/9/3.
 */
public class BindBankCardProtocol extends ProtocolBase {
    public BankCard bindBankCard;
    public String verifyCode;

    public String ticket;

    public BindBankCardProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {

        if(this.ticket == null && data.isJsonObject())
        {
            this.ticket = GsonUtil.getAsString(data, "ticket");
        }
        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {
        if(this.ticket == null)
        {
            return CHostName.HOST1 + "payment/bind-card";
        }
        else
        {
            return CHostName.HOST1 + "payment/bind-card-advance";
        }
    }

    @Override
    protected Map<String, Object> getPostData() {
        HashMap<String, Object> params = new HashMap<>();
        if(this.ticket != null){
            params.put("ticket", this.ticket);
            params.put("valid_code", this.verifyCode);
        }
        else {
            assert (this.bindBankCard != null);
            params.putAll(this.bindBankCard.formatPostData());
        }

        return params;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
