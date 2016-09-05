package com.goldmf.GMFund.manager.score;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.model.GMFCommResult;
import com.goldmf.GMFund.model.TarLinkButton;
import com.goldmf.GMFund.protocol.DepositProtocol;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.ModelSerialization;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.util.GsonUtil.*;

/**
 * Created by cupide on 16/3/12.
 */
public class DayActionManager {

    private String aid;

    public List<DayInfo> dayActionInfoList;   //每日信息DayInfo
    public int todayPos;                      //当前到了第几天（0 表示 第一天，后面照推）
    public boolean todayGained;               //今天是否获得过
    public String desc;                       //活动描述


    public DayActionManager(String aid) {
        this.aid = aid;
    }

    /**
     * 刷新当前的每日信息
     *
     * @param results
     */
    public final void fresh(final MResults<Void> results) {

        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "page/activity-prize-list")
                .params(ComonProtocol.buildParams("id", this.aid, "check_login", "1"))
                .callback(new ComonProtocol.ComonCallback() {
                              @Override
                              public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                                  MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                              }

                              @Override
                              public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                                  if (ret.isJsonObject()) {
                                      dayActionInfoList = new ArrayList<>(DayInfo.translate(getAsJsonArray(ret, "prize_list")));
                                      desc = getAsString(ret, "tip");

                                      JsonObject prize_profile = getAsJsonObject(ret, "prize_profile");

                                      todayPos = getAsInt(prize_profile, "current_pos");
                                      todayGained = getAsInt(prize_profile, "current_gained") == 1;
                                  }

                                  MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                              }
                          }
                )
                .build()
                .startWork();
    }


    /**
     * 获取今日奖励
     *
     * @param results
     */
    public final void gainToday(final MResults<GMFCommResult> results) {

        ScoreManager.getInstance().gainActionScore(this.aid, result -> {
            MResults.MResultsInfo.SafeOnResult(results, result);
        });
    }


    public static class DayInfo {

        public String iconUrl;         //图片地址;
        public double amount;             //获得侠币数量
        public boolean gained;               //是否已经获得

        public static DayInfo translateFromJsonData(JsonObject dic) {
            if (dic == null || dic.isJsonNull()) return null;

            DayInfo info = new DayInfo();
            info.iconUrl = getAsString(dic, "icon");
            info.amount = getAsDouble(dic, "amount");
            info.gained = getAsBoolean(dic, "gained");
            return info;
        }

        public static List<? extends DayInfo> translate(JsonArray list) {
            return Stream.of(opt(list).or(new JsonArray()))
                    .map(it -> GsonUtil.getAsJsonObject(it))
                    .map(it -> translateFromJsonData(it))
                    .filter(it -> it != null)
                    .collect(Collectors.toList());
        }
    }
}
