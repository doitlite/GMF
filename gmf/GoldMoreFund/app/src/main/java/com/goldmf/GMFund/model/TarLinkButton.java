package com.goldmf.GMFund.model;

import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonObject;

import java.io.Serializable;

/**
 * Created by cupide on 16/1/16.
 */
public class TarLinkButton implements Serializable{

    public int tag;
    public String name;

    public String tarLink;

    public ShareInfo shareInfo;

    public static final int TarLinkButton_Type_Tarlink = 0;     //Tarlink按钮
    public static final int TarLinkButton_Type_Share = 1;       //分享按钮
    public static final int TarLinkButton_Type_Cancel = 2;      //取消按钮
    public static final int TarLinkButton_Type_Continue = 3;    //继续按钮

    public int type;

    public static TarLinkButton build(int tag, String name, String tarLink) {
        TarLinkButton button = new TarLinkButton();
        button.tag = tag;
        button.name = name;
        button.type = TarLinkButton_Type_Tarlink;
        button.tarLink = tarLink;
        return button;
    }

    public static TarLinkButton build(int tag, String name, ShareInfo shareInfo) {

        TarLinkButton button = new TarLinkButton();
        button.tag = tag;
        button.name = name;
        button.type = TarLinkButton_Type_Share;
        button.shareInfo = shareInfo;
        return button;
    }


    public static TarLinkButton translateFromJsonData(JsonObject dic) {
        try {
            TarLinkButton info = new TarLinkButton();
            info.tag = GsonUtil.getAsInt(dic, "tag");
            info.name = GsonUtil.getAsString(dic, "name");
            info.tarLink = GsonUtil.getAsString(dic, "tar_link");
            info.shareInfo = ShareInfo.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "share_info"));

            String type = GsonUtil.getAsString(dic, "type");
            if(type.equals("share"))
                info.type = TarLinkButton_Type_Share;
            else if(type.equals("cancel"))
                info.type = TarLinkButton_Type_Cancel;
            else if(type.equals("continue"))
                info.type = TarLinkButton_Type_Continue;
            else
                info.type = TarLinkButton_Type_Tarlink;

            return info;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static class TarLinkText implements Serializable{
        public String content;
        public String tarLink;

        public static TarLinkText translateFromJsonData(JsonObject dic) {
            try {
                TarLinkText info = new TarLinkText();
                info.content = GsonUtil.getAsString(dic, "content");
                info.tarLink = GsonUtil.getAsString(dic, "tar_link");
                return info;
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
