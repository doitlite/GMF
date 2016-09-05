package com.goldmf.GMFund.manager.fortune;

import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by cupide on 15/10/28.
 */
public class BountyRuleInfo {

//    @SerializedName("name")
//    public String name;
//
//    @SerializedName("start_time")
//    public long startTime;
//
//    @SerializedName("stop_time")
//    public long stopTime;
//
//    @SerializedName("desc")
//    public String desc;
//
//    public String buttonName;
//    public String buttonType;
//    public String tarUrl;
//
//    public ShareInfo shareInfo;
//
//    public boolean enable;
//
//    @SerializedName("image_url")
//    private String normalImageUrl;
//    @SerializedName("disable_image_url")
//    private String disableImageUrl;
//
//    @SerializedName("jump_url")
//    public String jumpUrl;
//
//    /**
//     * 返回图片地址
//     * @return
//     */
//    public final String getImageUrl(){
//        if(this.enable)
//            return normalImageUrl;
//        else
//            return disableImageUrl;
//    }
//
//    public static BountyRuleInfo translateFromJsonData(JsonObject dic) {
//        Gson gson = new Gson();
//        try {
//            BountyRuleInfo info = gson.fromJson(dic, BountyRuleInfo.class);
//
//            info.enable = (GsonUtil.getAsInt(dic, "enabled") == 1);
//
//            JsonObject buttonObj = GsonUtil.getAsJsonObject(dic, "button");
//            info.buttonName = GsonUtil.getAsString(buttonObj, "name");
//            info.buttonType = GsonUtil.getAsString(buttonObj, "type");
//            info.tarUrl = GsonUtil.getAsString(buttonObj, "tar_link");
//            info.shareInfo = ShareInfo.translateFromJsonData(GsonUtil.getAsJsonObject(buttonObj, "share_info"));
//
//            return info;
//        } catch (Exception ignored) {
//            return null;
//        }
//    }
}
