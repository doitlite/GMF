package com.goldmf.GMFund.manager.discover;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import yale.extension.common.Optional;

/**
 * Created by cupide on 15/8/21.
 */
public class FocusInfo extends PageArray.PageItemIndex {

    public String imageUrl;
    public String tarLink;
    public boolean bShowTotal;    //需要添加total info的纯背景图
    public String info1;
    public String info2;
    public String iconUrl;
    public String tipsIconUrl;
    public String color;

    public static FocusInfo translateFromJsonData(JsonObject dic) {
        if (dic == null || dic.isJsonNull())return null;
        try {
            FocusInfo info = new FocusInfo();
            info.imageUrl = GsonUtil.getAsString(dic, "img_url");
            info.tarLink = GsonUtil.getAsString(dic, "tar_link");
            info.bShowTotal = (GsonUtil.getAsInt(dic, "image_type") == 1);
            info.info1 = GsonUtil.getAsString(dic, "info1");
            info.info2 = GsonUtil.getAsString(dic, "info2");
            info.iconUrl = GsonUtil.getAsString(dic, "icon_url");
            info.tipsIconUrl = GsonUtil.getAsString(dic, "tips_icon_url");
            info.color = GsonUtil.getAsString(dic, "color");
            return info;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static List<? extends FocusInfo> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }

    @Override
    public Object getKey() {
        return this.imageUrl;
    }

    @Override
    public long getTime() {
        return 0;
    }
}
