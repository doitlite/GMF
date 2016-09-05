package com.goldmf.GMFund.manager.fortune;

import android.text.TextUtils;

import com.goldmf.GMFund.manager.ISafeModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by yale on 16/3/19.
 */
public class RegisterBounty implements ISafeModel {
    @SerializedName("amount")
    public double amount;
    @SerializedName("img_url")
    public String imageURL;
    @SerializedName("bounty_desc")
    public String bountyDesc;

    public static RegisterBounty translateFromJsonData(JsonObject dic) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(dic, RegisterBounty.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public boolean isValid() {
        return amount > 0 && !TextUtils.isEmpty(imageURL) && TextUtils.isEmpty(bountyDesc);
    }
}
