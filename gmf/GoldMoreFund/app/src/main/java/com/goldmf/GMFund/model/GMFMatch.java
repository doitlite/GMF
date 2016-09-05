package com.goldmf.GMFund.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.protocol.base.PageArray.PageItemIndex;

/**
 * Created by yale on 16/3/14.
 */
public class GMFMatch extends PageItemIndex {

    public static final int STATE_SIGNUP = 0;   //报名中
    public static final int STATE_ING = 1;      //进行中
    public static final int STATE_OVER = 2;     //结束

    public String mid;
    public String imgUrl;
    public String tarLink;
    public String title;
    public int count;
    public int state;
    public String stateDesc;
    public String maxAward;
    public String maxAwardDesc;
    public String pageUrl;

    public long startTime;    //比赛开始时间
    public long stopTime;     //比赛结束时间
    public String shareURL; //分享页面地址
    public String shareButtonName;  //分享按钮的名字(运营ing)

    public boolean bSignUp;                     //是否已经报名

    public GMFRankUser result;     //自己的比赛结果
    public List<GMFRankUser> userList;                 //比赛用户排行榜
    public ShareInfo shareInfo;


    public static GMFMatch translateFromJsonData(JsonObject dic) {
        try {
            GMFMatch match = new GMFMatch();
            match.mid = GsonUtil.getAsString(dic, "mid");
            match.imgUrl = GsonUtil.getAsString(dic, "img_url");
            match.tarLink = GsonUtil.getAsString(dic, "tar_link");
            match.title = GsonUtil.getAsString(dic, "title");
            match.count = GsonUtil.getAsInt(dic, "user_count");
            match.state = GsonUtil.getAsInt(dic, "state");
            match.stateDesc = GsonUtil.getAsString(dic, "state_desc");
            match.maxAward = GsonUtil.getAsString(dic, "award");
            match.maxAwardDesc = GsonUtil.getAsString(dic, "award_desc");
            match.pageUrl = GsonUtil.getAsString(dic, "desc_page_url");
            match.startTime = GsonUtil.getAsLong(dic, "do_start_time");
            match.stopTime = GsonUtil.getAsLong(dic, "do_stop_time");
            match.shareURL = GsonUtil.getAsString(dic, "share_url");
            match.shareButtonName = GsonUtil.getAsString(dic, "share_button_name");

            match.bSignUp = GsonUtil.getAsInt(dic, "have_signup") == 1;
            match.result = GMFRankUser.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "current_user_point"));
            match.shareInfo = ShareInfo.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "share_info"));
            match.userList = Stream.of(opt(GsonUtil.getAsJsonArray(dic, "user_list")).or(new JsonArray()))
                    .map(it -> GsonUtil.getAsJsonObject(it))
                    .map(it -> GMFRankUser.translateFromJsonData(it))
                    .collect(Collectors.toList());
            return match;
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return null;
        }
    }

    public static List<? extends GMFMatch> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }

    public void editSignUp(boolean bSignUp) {
        this.bSignUp = bSignUp;
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
