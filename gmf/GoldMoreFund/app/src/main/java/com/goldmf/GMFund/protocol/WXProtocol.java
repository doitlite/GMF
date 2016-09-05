package com.goldmf.GMFund.protocol;


import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.protocol.base.ProtocolManager;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

import yale.extension.common.Optional;

/**
 * Created by cupide on 16/3/11.
 */


public class WXProtocol extends LoginProtocol {

    public static int WX_ACTION_BINK = 1;    //绑定微信
    public static int WX_ACTION_CLEAR = 2;   //清除微信绑定
    public static int WX_ACTION_LOGIN = 3;   //微信登录

    public int action;
    public String accessToken;
    public String openID;

    public WXProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {
        if (this.action == WX_ACTION_LOGIN) {
            return super.parseData(data);
        }
        return (returnCode == 0);
    }

    @Override
    protected String getUrl() {
        if (this.action == WX_ACTION_LOGIN) {
            return CHostName.HOST1 + "3rd_login/weixin";
        } else if (this.action == WX_ACTION_BINK || this.action == WX_ACTION_CLEAR) {
            return CHostName.HOST1 + "3rd_login/bind";
        } else {
            return "";
        }
    }

    @Override
    protected Map<String, Object> getPostData() {

        HashMap<String, Object> params = new HashMap<>();

        if (this.action == WX_ACTION_CLEAR) {
            params.put("access_token", "-1");
        } else {
            params.put("access_token", this.accessToken);
        }

        params.put("type", "wx");
        params.put("openid", Optional.of(this.openID).or(""));
        return params;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
