package com.goldmf.GMFund.model;

import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by cupide on 15/11/7.
 */
public class RichTextUrl implements Serializable {

    @SerializedName("name")
    public String text;

    @SerializedName("image")
    public String image;

    @SerializedName("url")
    public String url;

    public static RichTextUrl translateFromJsonData(JsonElement dic) {
        Gson gson = new Gson();
        try {
            RichTextUrl rUrl = gson.fromJson(dic, RichTextUrl.class);
            return rUrl;
        } catch (Exception ignored) {
            return null;
        }
    }

    public RichTextUrl() {
    }

    public RichTextUrl(String text, String url) {
        this.text = text;
        this.url = url;
    }

    public static RichTextUrl buildWithImage(String image, String url) {
        RichTextUrl rUrl = new RichTextUrl();
        rUrl.image = image;
        rUrl.url = url;
        return rUrl;
    }
}
