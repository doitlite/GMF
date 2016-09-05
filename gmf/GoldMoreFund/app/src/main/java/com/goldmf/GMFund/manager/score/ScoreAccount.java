package com.goldmf.GMFund.manager.score;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.model.TarLinkButton;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.List;

import yale.extension.common.Optional;

/**
 * Created by cupide on 16/3/12.
 */
public class ScoreAccount {

    public int cashBalance;//余额？
    public int totalAmount;//累计数量

    public void readFromeJsonData(JsonObject dic) {

        cashBalance = GsonUtil.getAsInt(dic, "balance");
        totalAmount = GsonUtil.getAsInt(dic, "total_amount");
    }

    //界面不要调用
    public void localSubtractScore(int score)
    {
        cashBalance -= score;
        cashBalance = Math.max(cashBalance, 0);
    }

    public static class ScoreAccountInfo implements Serializable {

        public long time;
        public String desc;
        public double amount;

        public static ScoreAccountInfo translateFromJsonData(JsonObject dic) {
            if (dic == null || dic.isJsonNull())
                return null;
            try {
                ScoreAccountInfo info = new ScoreAccountInfo();
                info.time = GsonUtil.getAsLong(dic, "trade_time");
                info.desc = GsonUtil.getAsString(dic, "operation_desc");
                info.amount = GsonUtil.getAsDouble(dic, "amount");

                int direction = GsonUtil.getAsInt(dic, "direction");
                if (direction == 2) {
                    info.amount = 0 - info.amount;
                }
                return info;
            } catch (Exception ignored) {
                return null;
            }
        }

        public static List<? extends ScoreAccountInfo> translate(JsonArray list) {
            return Stream.of(Optional.of(list).or(new JsonArray()))
                    .map(it -> GsonUtil.getAsJsonObject(it))
                    .map(it -> translateFromJsonData(it))
                    .filter(it -> it != null)
                    .collect(Collectors.toList());
        }
    }

    public static class ScoreAction implements Serializable {
        public static final int ScoreAction_type_normal = 0;//这个有三种状态
        public static final int ScoreAction_type_SignIn = 1;


        public static final int ScoreAction_status_noFinish = 1;  //未完成
        public static final int ScoreAction_status_receive = 2;   //待领取
        public static final int ScoreAction_status_close = 3;     //关闭

        public String aID;               //id
        public int actionType;
        public String imgUrl;
        public String title;
        public String tip;
        public double amount;
        public int status;
        public TarLinkButton button;


        public static ScoreAction translateFromJsonData(JsonObject dic) {
            if (dic == null || dic.isJsonNull())
                return null;
            try {
                ScoreAction info = new ScoreAction();
                info.aID = GsonUtil.getAsString(dic, "id");
                info.actionType = GsonUtil.getAsInt(dic, "type");
                info.imgUrl = GsonUtil.getAsString(dic, "icon");
                info.title = GsonUtil.getAsString(dic, "title");
                info.tip = GsonUtil.getAsString(dic, "tip");
                info.amount = GsonUtil.getAsDouble(dic, "amount");
                info.status = GsonUtil.getAsInt(dic, "status");
                info.button = TarLinkButton.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "button"));
                return info;
            } catch (Exception ignored) {
                return null;
            }
        }

        public static List<? extends ScoreAction> translate(JsonArray list) {
            return Stream.of(Optional.of(list).or(new JsonArray()))
                    .map(it -> GsonUtil.getAsJsonObject(it))
                    .map(it -> translateFromJsonData(it))
                    .filter(it -> it != null)
                    .collect(Collectors.toList());
        }

    }
}
