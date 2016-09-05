package com.goldmf.GMFund.manager.match;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.model.GMFMatch;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.CommonPostProtocol;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.ComonProtocol.ParamParse.ParamBuilder;
import com.goldmf.GMFund.protocol.base.PageArray;
import com.goldmf.GMFund.protocol.base.PageProtocol;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonElement;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.base.MResults.MResultsInfo.SafeOnResult;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;

/**
 * Created by yale on 16/3/14.
 */
public class MatchManger {
    private static final int COUNT_MATCH = 20;

    private static MatchManger sInstance = new MatchManger();

    private CommandPageArray minePageArray;
    private CommandPageArray allPageArray;

    private MatchManger() {
    }

    public static MatchManger getInstance() {
        return sInstance;
    }

    public void frechMatchWithMine(boolean mineData, MResults<CommandPageArray<GMFMatch>> callback) {

        CommandPageArray<GMFMatch> matches = new CommandPageArray.Builder<GMFMatch>()
                .classOfT(GMFMatch.class)
                .cgiUrl(CHostName.HOST2 + (mineData?"vtc/page/match-mine":"vtc/page/match-home"))
                .commandPage(COUNT_MATCH)
                .build();

        if (mineData)
            this.minePageArray = matches;
        else
            this.allPageArray = matches;

        matches.getNextPage(result -> SafeOnResult(callback, result));
    }

    public void getMatchDetailWithMatchID(String matchId, MResults<GMFMatch> callback) {
        new ComonProtocol.Builder()
                .url(CHostName.formatUrl(CHostName.HOST2, "vtc/page/match-detail"))
                .params(new ParamBuilder().add("match_id", matchId))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(callback, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        GMFMatch match = opt(ret)
                                .let(it -> GsonUtil.getAsJsonObject(it, "match_info"))
                                .let(it -> GMFMatch.translateFromJsonData(it)).orNull();
                        SafeOnResult(callback, new MResults.MResultsInfo<GMFMatch>().setData(match));
                    }
                })
                .build()
                .startWork();
    }

    public void signupMatchID(String matchID, MResults<Void> callback) {
        new CommonPostProtocol.Builder()
                .url(CHostName.formatUrl(CHostName.HOST2, "vtc/match-signup"))
                .postParams(new ParamBuilder().add("match_id", matchID))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        SafeOnResult(callback, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        SafeOnResult(callback, protocol.buildRet());
                    }
                })
                .build().startWork();
    }
}
