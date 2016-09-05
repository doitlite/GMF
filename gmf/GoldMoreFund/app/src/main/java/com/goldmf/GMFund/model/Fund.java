package com.goldmf.GMFund.model;

import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by cupide on 15/7/31.
 */
public class Fund extends FundBrief {


    public enum Fund_Button {
        NoDefine(0),      //未定义，不用做任何事情
        Cancel(1),      //取消申请按钮 - 不支持
        Delete(2),      //删除按钮  -不支持
        ReAdd(3),       //重新申请按钮 -不支持
        Invest(4),       //立即投资按钮
        Invite(5),       //邀请按钮
        Booking(6),       //预约按钮
        Preferential(7);       //立即加息按钮

        private int btn;

        Fund_Button(int _btn) {
            this.btn = _btn;
        }

        public int toInt() {
            return this.btn;
        }

        public static Fund_Button getInstance(int _btn) {
            switch (_btn) {
                case 1:
                    return Cancel;
                case 2:
                    return Delete;
                case 3:
                    return ReAdd;
                case 4:
                    return Invest;
                case 5:
                    return Invite;
                case 6:
                    return Booking;
                case 7:
                    return Preferential;
                default:
                    return NoDefine;
            }
        }
    }

    public List<TarLinkButton> buttons = new ArrayList<>();

    public FundSharing sharing;        //收益分成

    public User traderUserOrNull;      //操盘手
    public double traderInvest;  //操盘手出资

    public ShareInfo shareInfo;    //分享信息

    public List<ImageTarLinkInfo> imageList = new ArrayList<>(); //ImageTarLinkInfo 的数组

    public String agreementName;    //投资协议名称

    private void processButton(JsonObject dic) {
        buttons = new LinkedList<>();
        JsonArray jsonArray = GsonUtil.getAsJsonArray(dic, "fund_button_style");
        if (jsonArray != null) {
            for (JsonElement element : jsonArray) {
                int buttonTag = GsonUtil.getAsInt(element);

                if (buttonTag == Fund_Button.NoDefine.toInt() || Fund_Button.ReAdd.toInt() == buttonTag) {
                    //第一个版本，去掉 重新申请按钮
                    continue;
                } else if (buttonTag == Fund_Button.Cancel.toInt()) {
//                    buttons.add(TarLinkButton.build(buttonTag, "撤销申请", ""));
                    continue;
                } else if (buttonTag == Fund_Button.Delete.toInt()) {
//                    buttons.add(TarLinkButton.build(buttonTag, "删除组合", ""));
                    continue;
                } else if (buttonTag == Fund_Button.ReAdd.toInt()) {
//                    buttons.add(TarLinkButton.build(buttonTag, "重新申请", ""));
                    continue;
                } else if (buttonTag == Fund_Button.Invest.toInt()) {
                    buttons.add(TarLinkButton.build(buttonTag, "立即投资", ""));
                } else if (buttonTag == Fund_Button.Invite.toInt()) {
                    buttons.add(TarLinkButton.build(buttonTag, "分享", this.shareInfo));
                } else if (buttonTag == Fund_Button.Booking.toInt()) {
                    buttons.add(TarLinkButton.build(buttonTag, "预约", ""));
                } else if (buttonTag == Fund_Button.Preferential.toInt()) {
                    JsonObject obj = GsonUtil.getAsJsonObject(GsonUtil.getAsJsonObject(dic, "fund_button"), String.valueOf(buttonTag));
                    TarLinkButton button = TarLinkButton.translateFromJsonData(obj);
                    button.tag = buttonTag;
                    buttons.add(button);
                }
            }
        }
    }

    public void readFromeJsonData(JsonObject dic) {

        super.readFromeJsonData(dic);//读取brief相关

        JsonObject shareDic = GsonUtil.getAsJsonObject(dic, "share_info");
        this.shareInfo = ShareInfo.translateFromJsonData(shareDic);
        this.processButton(dic);

        this.traderInvest = GsonUtil.getAsDouble(dic, "operator_invest");
        if (GsonUtil.has(dic, "operator")) {
            this.traderUserOrNull = User.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "operator"));
        }

        if (GsonUtil.has(dic, "profit_sharing_threshhold")) {
            this.sharing = FundSharing.translateFromJsonData(dic);
        }

        ImageTarLinkInfo.listFromJsonArray(this.imageList, GsonUtil.getAsJsonArray(dic, "app_ad_images_list"));
        this.agreementName = GsonUtil.getAsString(dic, "agreement_name");
    }


    public static Fund translateFromJsonData(JsonObject dic) {
        Fund fund = new Fund();
        fund.readFromeJsonData(dic);
        return fund;
    }

    public static class FundSharing implements Serializable {

        @SerializedName("profit_sharing_threshhold")
        public double profitSharingThreshhold;  //收益分成线

        @SerializedName("profit_sharing_ratio")
        public double profitSharingRatio;       //收益分成比例

        public static FundSharing translateFromJsonData(JsonObject dic) {

            Gson gson = new Gson();
            try {
                return gson.fromJson(dic, FundSharing.class);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    public static class ImageTarLinkInfo implements Serializable {
        @SerializedName("image_url")
        public String imageUrl;        //当前加息比例（0.5%）

        @SerializedName("tar_link")
        public String tarlink;

        public static ImageTarLinkInfo translateFromJsonData(JsonObject dic) {

            try {
                Gson gson = new Gson();
                ImageTarLinkInfo info = gson.fromJson(dic, ImageTarLinkInfo.class);
                return info;

            } catch (Exception ignored) {
                return null;
            }
        }

        public static void listFromJsonArray(List<ImageTarLinkInfo> list, JsonArray array) {
            if (array == null || list == null)
                return;

            list.clear();
            for (JsonElement temp : array) {
                JsonObject obj = GsonUtil.getAsJsonObject(temp);
                if (obj != null) {
                    ImageTarLinkInfo info = ImageTarLinkInfo.translateFromJsonData(obj);
                    if (info != null)
                        list.add(info);
                }
            }
        }
    }


}
