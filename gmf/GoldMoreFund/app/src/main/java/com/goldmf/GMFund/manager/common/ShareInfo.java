package com.goldmf.GMFund.manager.common;

import android.os.Parcel;
import android.os.Parcelable;

import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by cupide on 15/10/28.
 */
public class ShareInfo implements Serializable, Parcelable {

    public static int SHARE_SOURE_REAL_INCOMESHOW = 1; //真实账户嗮收益
    public static int SHARE_SOURE_SIMU_INCOMESHOW = 2; //模拟账户嗮收益

    public String url;
    public String imageUrl;
    public String title;
    public String msg;

    transient public int shareIncomeShowType = 0;

    public static final Creator<ShareInfo> CREATOR = new Creator<ShareInfo>() {
        @Override
        public ShareInfo createFromParcel(Parcel source) {
            return new ShareInfo(source);
        }

        @Override
        public ShareInfo[] newArray(int size) {
            return new ShareInfo[size];
        }
    };


    public ShareInfo() {
    }

    protected ShareInfo(Parcel in) {
        this.url = in.readString();
        this.imageUrl = in.readString();
        this.title = in.readString();
        this.msg = in.readString();
    }

    public static ShareInfo translateFromJsonData(JsonObject dic) {
        if(dic == null || dic.isJsonNull())return null;

        try {
            ShareInfo info = new ShareInfo();
            info.url = GsonUtil.getAsString(dic, "share_url");
            info.imageUrl = GsonUtil.getAsString(dic, "share_image");
            info.title = GsonUtil.getAsString(dic, "share_title");
            info.msg = GsonUtil.getAsString(dic, "share_msg");
            return info;
        } catch (Exception ignored) {
            return null;
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.imageUrl);
        dest.writeString(this.title);
        dest.writeString(this.msg);
    }

}
