package com.goldmf.GMFund.model;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import yale.extension.common.Optional;

/**
 * Created by cupide on 16/3/12.
 */
public class Rank  implements Serializable {

    public String rankID;          //索引值
    public String title;           //标题
    public String context;         //内容
    public long updateTime;     //更新时间戳

    public transient List<GMFRankUser> userList;     //更新时间戳GMFRankUser

    public static Rank translateFromJsonData(JsonObject dic){
        if(dic == null)
            return null;

        try {
            Rank rank = new Rank();
            rank.rankID = GsonUtil.getAsString(dic, "leaderboard_id");
            rank.title = GsonUtil.getAsString(dic, "title");
            rank.context = GsonUtil.getAsString(dic, "description");
            rank.updateTime = GsonUtil.getAsLong(dic, "last_update_time");

            rank.userList = new ArrayList<>(GMFRankUser.translate(GsonUtil.getAsJsonArray(dic, "list")));
            return  rank;
        }
        catch (Exception ignored){
            ignored.printStackTrace();
            return null;
        }
    }
}
