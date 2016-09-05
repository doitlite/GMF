package com.goldmf.GMFund.controller.business;

import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.manager.match.MatchManger;
import com.goldmf.GMFund.model.GMFMatch;
import com.goldmf.GMFund.protocol.base.CommandPageArray;

import rx.Observable;

import static com.goldmf.GMFund.extension.MResultExtension.createObservableMResult;
import static com.goldmf.GMFund.extension.MResultExtension.createObservablePageArrayMResult;

/**
 * Created by Evan on 16/3/16 下午5:01.
 */
public class MatchController {

    private MatchController() {

    }

    public static Observable<MResultsInfo<CommandPageArray<GMFMatch>>> refreshStockMatch() {
        return Observable.create(sub -> MatchManger.getInstance().frechMatchWithMine(false, createObservablePageArrayMResult(sub)));
    }

    public static Observable<MResultsInfo<CommandPageArray<GMFMatch>>> refreshStockMatchWithMine() {
        return Observable.create(sub -> MatchManger.getInstance().frechMatchWithMine(true, createObservablePageArrayMResult(sub)));
    }

    public static Observable<MResultsInfo<GMFMatch>> refreshStockMatchDetail(String matchId) {
        return Observable.create(sub -> MatchManger.getInstance().getMatchDetailWithMatchID(matchId, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> signupMatch(String matchId) {
        return Observable.create(sub -> MatchManger.getInstance().signupMatchID(matchId, createObservableMResult(sub)));
    }

}
