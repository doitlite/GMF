package com.goldmf.GMFund.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.protocol.base.PageArray.*;

/**
 * Created by cupide on 16/5/10.
 */
public class RemaindFeed extends Feed {

    public static int MessageAction_Comment = 1;    //评论
    public static int MessageAction_Donate = 2;     //打赏
    public static int MessageAction_Unlock = 3;     //解锁
    public static int MessageAction_Judge = 4;      //评价

    public String text;   //text
    public int score;                 //score
    public int action;                //action

    public User user;                 //user
    public GMFMessage message;       //message,可能为nil

    public static boolean isValid(RemaindFeed feed) {
        return feed != null;
    }

    public static RemaindFeed translateFromJsonData(JsonObject dic) {
        if (dic == null) return null;

        RemaindFeed info = new RemaindFeed();
        info.readFromJsonData(dic);
        return info;
    }

    public static List<? extends RemaindFeed> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }

    public static RemaindFeed translateFromJsonData(JsonObject dic, int messageAction) {
        RemaindFeed info = translateFromJsonData(dic);
        if (info != null)
            info.feedType = messageAction;
        return info;
    }

    public static RemaindFeed buildMineRemaindFeedWithDonate(int score) {
        RemaindFeed info = new RemaindFeed();
        info.feedType = MessageAction_Donate;
        info.user = MineManager.getInstance().getmMe();
        info.score = score;
        return info;
    }

    public static RemaindFeed buildMineRemaindFeedWithComment(String str) {
        RemaindFeed info = new RemaindFeed();
        info.feedType = MessageAction_Comment;
        info.user = MineManager.getInstance().getmMe();
        info.text = str;
        return info;
    }

    public void readFromJsonData(JsonObject dic){
        this.readFromeJsonData(dic);
        this.fid = GsonUtil.getAsString(dic, "id");
        this.feedType = GsonUtil.getAsInt(dic, "type");
        this.score = GsonUtil.getAsInt(dic, "score");
        this.action = GsonUtil.getAsInt(dic, "action");
        this.text = GsonUtil.getAsString(dic, "text");

        if(this.feedType == 0 && this.text != null && this.text.length()>0){
            this.feedType = MessageAction_Comment;
        }

        this.user = User.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "user"));
        this.message = GMFMessage.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "message_brief"));
    }

    @Override
    public Object getKey() {
        return null;
    }

    @Override
    public long getTime() {
        return 0;
    }
}
