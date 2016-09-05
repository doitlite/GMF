package com.goldmf.GMFund.manager.common;

import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by cupide on 15/10/5.
 */
public class QiniuToken {

    public String token;
    public String domain;
    public String name;

    public static QiniuToken translateFromJsonData(JsonObject dic) {
        if(dic == null || dic.isJsonNull())return null;
        try {
            QiniuToken info = new QiniuToken();
            info.token = GsonUtil.getAsString(dic, "token");
            info.domain = GsonUtil.getAsString(dic, "domain");
            info.name = GsonUtil.getAsString(dic, "name");
            return info;
        } catch (Exception ignored) {
            return null;
        }
    }
}
