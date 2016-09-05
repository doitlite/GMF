package com.goldmf.GMFund.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.manager.mine.Trader;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import yale.extension.common.Optional;

/**
 * Created by yale on 15/7/20.
 * C类用户
 */
public class User extends PageArray.PageItemIndex {
    public static User NULL = new User();

    public static class User_Type {

        public final static int Custom = 0;       //普通用户
        public final static int Trader = 1;       //B类用户
        public final static int Talent = 2;       //牛人
        public final static int NoDefine = 3;       //不支持的类型

        private final static int MIN = Custom;
        private final static int MAX = Talent;

        public static boolean isTrader(int type) {
            return type == Trader;
        }
    }

    @SerializedName("user_id")
    public int index;                     //server索引信息

    @SerializedName("account_type")
    public int type;                      //用户类型

    @SerializedName("nick_name")
    private String name;                   //昵称

    @SerializedName("avatar_url")
    private String photoUrl;               //头像地址

    @SerializedName("title")
    public String title;                  //头衔
    public boolean hasFollow;             //是否关注（主人态）

    transient public Trader trader;       //B类用户信息
    transient public UserDetail more = new UserDetail();       //更多详情

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    @Override
    public Object getKey() {
        return this.index;
    }

    @Override
    public long getTime() {
        return 0;
    }

    public static User translateFromJsonData(JsonObject dic) {
        if (dic == null || !dic.isJsonObject())
            return null;

        User user = new User();
        user.readFromJsonData(dic);
        return user;
    }

    public static List<? extends User> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }

    public void readFromJsonData(JsonObject dic) {
        this.index = GsonUtil.getAsInt(dic, "user_id");
        this.type = GsonUtil.getAsInt(dic, "account_type");

        if (this.type == User_Type.Custom) {
            boolean isTalent = GsonUtil.getAsBoolean(dic, "is_talent");
            if (isTalent) {
                this.type = User_Type.Talent;
            }
        }

        //处理如果是不兼容的类型，直接返回GMFUser_Type_Custom
        if (this.type < User_Type.MIN || this.type > User_Type.MAX)
            this.type = User_Type.Custom;

        this.name = GsonUtil.getAsString(dic, "nick_name");
        this.photoUrl = GsonUtil.getAsString(dic, "avatar_url");
        this.title = GsonUtil.getAsString(dic, "title");

        this.hasFollow = GsonUtil.getAsBoolean(dic, "has_follow");

        if (this.type == User_Type.Trader || this.type == User_Type.Talent) {
            this.trader = Trader.translateFromJsonData(dic);
        }

        if (GsonUtil.has(dic, "follow_num")) {
            this.more.readFromJsonData(dic);
        }
    }

//    public transient ModelSerialization<User> mSerialization = new ModelSerialization<User>(this);
//
//    public static User loadData(String key){
//        return ModelSerialization.loadByKey(key, User.class);
//    }

    public static class UserDetail implements Serializable{
        public int followNum;           //关注人数
        public int fansNum;             //粉丝人数

        public void readFromJsonData(JsonObject dic) {

            this.followNum = GsonUtil.getAsInt(dic, "follow_num");
            this.fansNum = GsonUtil.getAsInt(dic, "fans_num");
        }
    }

}

