package com.goldmf.GMFund.model;

import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.SecondUtil;
import com.google.gson.JsonObject;

/**
 * Created by cupide on 16/8/3.
 */
public class LoadingInfo  {

    private final static String KeyLastShowTime = "lastShowTime";

    public String InfoID;        //闪屏id
    public String imageUrl;      //图片地址
    public String tarLink;       //tarLink地址

    public long startTime;            //开始日期
    public long stopTime;             //结束日期
    public int cotinueSecond;         //持续时间

    public long lastShowTime;

    private JsonObject priJsonData;

    public JsonObject getJsonData(){
        priJsonData.addProperty(KeyLastShowTime, lastShowTime);
        return priJsonData;
    }

    public void readFromeJsonData(JsonObject dic) {
        this.InfoID = GsonUtil.getAsString(dic, "id");
        this.imageUrl = GsonUtil.getAsString(dic, "img_url");
        this.tarLink = GsonUtil.getAsString(dic, "tar_link");
        this.startTime = GsonUtil.getAsLong(dic, "start_time");
        this.stopTime = GsonUtil.getAsLong(dic, "stop_time");
        this.cotinueSecond = GsonUtil.getAsInt(dic, "time");

        this.priJsonData = dic;

        if(dic.has(KeyLastShowTime)){
            this.lastShowTime = GsonUtil.getAsLong(dic, KeyLastShowTime);
        }
    }

    public boolean isValidTime(){

        return (this.startTime <= SecondUtil.currentSecond())
        &&(this.stopTime >=SecondUtil.currentSecond());
    }

    public static LoadingInfo translateFromJsonData(JsonObject dic){
        if (dic == null || dic.isJsonNull())
            return null;

        try {
            LoadingInfo info = new LoadingInfo();
            info.readFromeJsonData(dic);
            return info;
        }
        catch (Exception ignored){
            return null;
        }
    }
}
