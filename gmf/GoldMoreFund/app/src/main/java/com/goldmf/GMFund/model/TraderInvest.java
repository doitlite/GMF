package com.goldmf.GMFund.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by cupide on 15/12/8.
 */
public class TraderInvest  implements Serializable {

    @SerializedName("total_share")
    public double totalShare;       //总分成

    @SerializedName("extracted_share")
    public double extractedShare;   //已提取分成

    public static TraderInvest translateFromJsonData(JsonObject dic){
        Gson gson = new Gson();
        try {
            return gson.fromJson(dic, TraderInvest.class);
        }
        catch (Exception ignored){
            return null;
        }
    }
}
