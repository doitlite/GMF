package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.google.gson.JsonElement;

import java.util.Map;

/**
 * Created by cupide on 15/7/24.
 */
public class GetProProtocol extends ProtocolBase {

    public Mine mMe;

    public String mData;

    public GetProProtocol(Mine me,ProtocolCallback callback) {
        super(callback);
        assert (me!= null);

        mMe = me;
    }

    @Override
    protected boolean parseData(JsonElement data) {

        if (super.returnCode == 0 && data.isJsonObject()) {
            mMe.readFromJsonData(data.getAsJsonObject());
            mData = data.toString();
        }

        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {
        return CHostName.HOST1 + "user/profile";
    }

    @Override
    protected Map<String, Object> getPostData() {
        return null;
    }

    @Override
    protected Map<String,String> getParam() {
        return null;
    }

}
