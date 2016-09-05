package com.goldmf.GMFund.controller.business;

import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.extension.MResultExtension;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.discover.DiscoverManager;
import com.goldmf.GMFund.manager.score.ScoreAccount;
import com.goldmf.GMFund.manager.score.ScoreAccount.ScoreAccountInfo;
import com.goldmf.GMFund.manager.score.ScoreAccount.ScoreAction;
import com.goldmf.GMFund.manager.score.ScoreManager;
import com.goldmf.GMFund.model.GMFCommResult;

import java.util.List;

import rx.Observable;

import static com.goldmf.GMFund.extension.MResultExtension.createObservableListMResult;
import static com.goldmf.GMFund.extension.MResultExtension.createObservableMResult;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;

/**
 * Created by Evan on 16/3/15 下午2:39.
 */
public class ScoreController {
    private ScoreController() {

    }

    public static Observable<MResultsInfo<ScoreAccount>> refreshScoreAccount() {
        return Observable.create(sub -> ScoreManager.getInstance().freshAccount(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<List<ScoreAction>>> refreshScoreActions() {
        return Observable.create(sub -> ScoreManager.getInstance().freshActions(createObservableListMResult(sub)));
    }

    public static Observable<MResultsInfo<List<ScoreAccountInfo>>> refreshScoreRecord() {
        return Observable.create(sub -> ScoreManager.getInstance().freshAccountRecord(createObservableListMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> refreshDayAction() {
        return Observable.create(sub -> ScoreManager.getInstance().checkin.fresh(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<GMFCommResult>> gainTodayScore() {
        return Observable.create(sub -> ScoreManager.getInstance().checkin.gainToday(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<GMFCommResult>> gainActionScore(String actionID) {
        return Observable.create(sub -> ScoreManager.getInstance().gainActionScore(actionID, createObservableMResult(sub)));
    }
}
