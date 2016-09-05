package com.goldmf.GMFund.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import yale.extension.common.Optional;

/**
 * Created by yale on 16/3/14.
 */
public class GMFRankUser {

    public static class UserPoint {
        public static final int TYPE_FLAT = 0;    //平
        public static final int TYPE_UP = 1;      //升
        public static final int TYPE_DOWN = 2;    //降

        public double value;
        public int type;
        public String desc;

        public static UserPoint translateFromJsonData(JsonObject dic) {
            try {
                UserPoint point = new UserPoint();
                point.value = GsonUtil.getAsDouble(dic,"value");
                point.type = GsonUtil.getAsInt(dic, "point_type");
                point.desc = GsonUtil.getAsString(dic, "desc");
                return point;
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    public static class UserAction{
        public static final int TYPE_NO = 0;
        public static final int TYPE_BUY = 1;
        public static final int TYPE_SELL = 2;

        public long time;
        public int type;
        public String desc;

        public static UserAction translateFromJsonData(JsonObject dic) {
            try {
                UserAction action = new UserAction();
                action.time = GsonUtil.getAsLong(dic, "created_at");
                action.type = GsonUtil.getAsInt(dic, "action_type");
                action.desc = GsonUtil.getAsString(dic, "action_desc");
                return action;
            } catch (Exception ignored) {
                return null;
            }
        }
    }
    public static class UserExchange{

        public double beginCapital;
        public double endCapital;
        public double income;

        public double exchange;
        public boolean isExchanged;
        public String desc;

        public static UserExchange translateFromJsonData(JsonObject dic) {
            if(dic == null)
                return null;

            try {
                UserExchange exchange = new UserExchange();
                exchange.beginCapital = GsonUtil.getAsDouble(dic, "begin_captial");
                exchange.endCapital = GsonUtil.getAsDouble(dic, "end_captial");
                exchange.income = GsonUtil.getAsDouble(dic, "income");
                exchange.exchange = GsonUtil.getAsDouble(dic, "exchange");
                exchange.isExchanged = GsonUtil.getAsBoolean(dic, "have_exchange");
                exchange.desc = GsonUtil.getAsString(dic, "desc");
                return exchange;
            } catch (Exception ignored) {
                return null;
            }
        }
    }


    private User user;
    public User getUser(){
        if (user == null)
            return MineManager.getInstance().getmMe();
        return user;
    }

    public int position;

    public UserPoint point;
    public UserAction lastAction;
    public UserExchange exchange;

    public static GMFRankUser translateFromJsonData(JsonObject dic) {
        try {
            GMFRankUser rankUser = new GMFRankUser();
            rankUser.position = GsonUtil.getAsInt(dic, "position");
            rankUser.user = User.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "user_info"));
            rankUser.point = UserPoint.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "user_point"));
            rankUser.lastAction = UserAction.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "last_action"));
            rankUser.exchange = UserExchange.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "user_exchange"));
            return rankUser;
        } catch (Exception ignored) {
            return null;
        }
    }


    public static List<? extends GMFRankUser> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }
}
