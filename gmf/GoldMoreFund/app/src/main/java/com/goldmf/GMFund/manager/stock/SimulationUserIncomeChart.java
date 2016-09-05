package com.goldmf.GMFund.manager.stock;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.model.GMFMatch;
import com.goldmf.GMFund.model.GMFRankUser;
import com.goldmf.GMFund.model.Rank;
import com.goldmf.GMFund.model.SimulationAccount;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import yale.extension.common.Optional;

/**
 * Created by cupide on 16/3/12.
 */
public class SimulationUserIncomeChart extends UserIncomeChart {

    public int rankPosition;   //排名
    public double winRatio;    //胜率
    public int tradeCount;     //月交易次数

    public final List<BestMatch> bestMatchList = new ArrayList<>(); //比赛排名
    public User user;
    public SimulationAccount account;

    public void readFromeJsonData(JsonObject dic) {
        super.readFromeJsonData(dic);

        this.rankPosition = GsonUtil.getAsInt(dic, "stats", "rank_position");
        this.winRatio = GsonUtil.getAsDouble(dic, "stats", "history_win_ratio");
        this.tradeCount = GsonUtil.getAsInt(dic, "stats", "trade_times");

        this.bestMatchList.clear();
        if(GsonUtil.has(dic, "best_match_list")){
            this.bestMatchList.addAll(BestMatch.translate(GsonUtil.getAsJsonArray(dic, "best_match_list")));
        }

        this.user = User.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "user"));
        this.account = SimulationAccount.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "accout_balance"));
    }

    public static SimulationUserIncomeChart translateFromJsonData(JsonObject dic) {
        if (dic == null)
            return null;

        SimulationUserIncomeChart chart = new SimulationUserIncomeChart();
        chart.readFromeJsonData(dic);
        return chart;
    }

    public static class BestMatch{
        public GMFMatch matchInfo;  //比赛详情
        public GMFRankUser result;  //比赛结果

        public static BestMatch translateFromJsonData(JsonObject dic) {
            if (dic == null || dic.isJsonNull())
                return null;

            BestMatch info = new BestMatch();
            info.matchInfo = GMFMatch.translateFromJsonData(dic);
            info.result = GMFRankUser.translateFromJsonData(GsonUtil.getAsJsonObject(dic, "user_pos"));
            return info;
        }

        public static List<? extends BestMatch> translate(JsonArray list) {
            return Stream.of(Optional.of(list).or(new JsonArray()))
                    .map(it -> GsonUtil.getAsJsonObject(it))
                    .map(it -> translateFromJsonData(it))
                    .filter(it -> it != null)
                    .collect(Collectors.toList());
        }
    }
}
