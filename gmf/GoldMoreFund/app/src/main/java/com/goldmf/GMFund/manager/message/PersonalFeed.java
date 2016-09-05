package com.goldmf.GMFund.manager.message;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.model.Feed;
import com.goldmf.GMFund.model.FeedOrder;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.protocol.base.ComonProtocol.buildParams;
import static com.goldmf.GMFund.protocol.base.PageArray.*;

/**
 * Created by cupide on 16/5/13.
 */
public class PersonalFeed extends Feed {

    public static final int PersonalFeed_Message = 301; //用户发出的messageFeed


    public MessageSession sesion;           //sesion,可能为nil
    public GMFMessage message;              //message,可能为nil


    public static List<? extends PersonalFeed> translate(JsonArray list) {
        return Stream.of(Optional.of(list).or(new JsonArray()))
                .map(it -> GsonUtil.getAsJsonObject(it))
                .map(it -> translateFromJsonData(it))
                .filter(it -> it != null)
                .collect(Collectors.toList());
    }

    public static PersonalFeed translateFromJsonData(JsonObject dic) {
        try {
            PersonalFeed info = new PersonalFeed();
            info.readFromeJsonData(dic);
            info.sesion = MessageSession.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "session_brief"));
            info.message = GMFMessage.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "message_brief"));
            return info;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static class PersonalCommand extends CommandPageArray<Feed> {

        public User user;

        PersonalCommand(int userID) {
            super(new CommandPageArray.Builder<Feed>()
//                    .classOfT(PersonalFeed.class)
                    .translateItem(data ->{
                        Feed feed = null;
                        int feedType = GsonUtil.getAsInt(data, "feed_type");

                        switch (feedType){
                            case FeedOrder.Feed_Type_buy:
                            case FeedOrder.Feed_Type_sell:
                                feed = FeedOrder.translateFromJsonData(data);
                                break;
                            case PersonalFeed.PersonalFeed_Message:
                                feed = PersonalFeed.translateFromJsonData(data);
                                break;
                        }
                        return feed;
                    })
                    .cgiParam(buildParams("user_id", String.valueOf(userID)))
                    .cgiUrl(CHostName.HOST2 + "feed/my-active")
                    .commandTS());
        }

        @Override
        public void getNextPage(MResults<CommandPageArray<Feed>> results) {
            super.getNextPage(result -> {
                if (result.isSuccess && result.ret != null) {
                    if (result.ret.isJsonObject()) {
                        PersonalCommand.this.user = User.translateFromJsonData(GsonUtil.getAsJsonObject(result.ret, "user"));
                    }
                }

                //返回
                MResults.MResultsInfo.SafeOnResult(results, result);
            });
        }

        @Override
        public void getPrePage(MResults<CommandPageArray<Feed>> results) {
            super.getPrePage(result -> {
                if (result.isSuccess && result.ret != null) {
                    if (result.ret.isJsonObject()) {
                        PersonalCommand.this.user = User.translateFromJsonData(GsonUtil.getAsJsonObject(result.ret, "user"));
                    }
                }

                //返回
                MResults.MResultsInfo.SafeOnResult(results, result);
            });
        }
    }
}
