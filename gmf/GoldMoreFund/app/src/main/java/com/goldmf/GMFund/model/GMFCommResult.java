package com.goldmf.GMFund.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by cupide on 16/3/12.
 */
public class GMFCommResult {

    @SerializedName("icon")
    public String imgUrl;     //图片

    @SerializedName("title")
    public String title;      //标题

    @SerializedName("tip")
    public String content;    //信息


    public static GMFCommResult translateFromJsonData(JsonObject dic){
        Gson gson = new Gson();
        try {
            return gson.fromJson(dic, GMFCommResult.class);
        }
        catch (Exception ignored){
            return null;
        }
    }
}
