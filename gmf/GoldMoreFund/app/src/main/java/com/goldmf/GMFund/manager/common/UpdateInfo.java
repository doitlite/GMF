package com.goldmf.GMFund.manager.common;

import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.ModelSerialization;
import com.goldmf.GMFund.util.SecondUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by cupide on 15/9/7.
 */
public class UpdateInfo implements Serializable {

    private static String sUpdateInfoKey = "UpdateInfoKey";

    public boolean showAlert;               //是否需要alert
    public boolean needForceUpdate;         //是否需要强制升级

    @SerializedName("url")
    public String url;                  //升级地址

    @SerializedName("ver")
    public String updateVersion;      //升级到的版本号
    @SerializedName("title")
    public String updateTitle;        //升级的标题
    @SerializedName("msg")
    public String updateMsg;          //升级到对应版本的信息

    @SerializedName("md5")
    public String md5;              //url的md5值

    private long minNextRemind;    //下一次提醒时间

    public static UpdateInfo translateFromJsonData(JsonObject dic) {
        Gson gson = new Gson();
        try {
            UpdateInfo info = gson.fromJson(dic, UpdateInfo.class);
            info.needForceUpdate = (GsonUtil.getAsInt(dic, "new_type") == 2);
            info.showAlert = true;
            info.minNextRemind = 0;
            return info;
        } catch (Exception ignored) {
            return null;
        }
    }

    public void delayUpdateAlert() {
        this.minNextRemind = SecondUtil.currentSecond() + 24 * 3600;
        this.save();
    }

    transient ModelSerialization<UpdateInfo> mSerialization = new ModelSerialization<UpdateInfo>(this);

    public static UpdateInfo loadData() {
        UpdateInfo info = ModelSerialization.loadByKey(sUpdateInfoKey, UpdateInfo.class);
        if (info != null)
            info.showAlert = (SecondUtil.currentSecond() > info.minNextRemind);
        return info;
    }

    public void save() {
        mSerialization.saveByKey(sUpdateInfoKey);
    }

    public void remove() {
        mSerialization.removeByKey(sUpdateInfoKey);
    }
}
