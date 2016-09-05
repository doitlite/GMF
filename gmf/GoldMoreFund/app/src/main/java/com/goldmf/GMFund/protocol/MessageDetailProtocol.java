package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.protocol.base.PageProtocol;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.google.gson.JsonElement;

/**
 * Created by cupide on 16/5/23.
 */
public class MessageDetailProtocol extends PageProtocol {
    public MessageDetailProtocol(ProtocolCallback callback) {
        super(callback);
    }

    public GMFMessage message;


    @Override
    protected boolean parseData(JsonElement dic) {
        if (super.parseData(dic) && dic!= null && dic.isJsonObject()) {

            this.message.readFromJsonData(dic.getAsJsonObject());
        }
        return (super.returnCode == 0);
    }

}
