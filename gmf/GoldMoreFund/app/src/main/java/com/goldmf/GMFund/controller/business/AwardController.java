package com.goldmf.GMFund.controller.business;

import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.manager.fortune.BountyAccount;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.manager.fortune.RegisterBounty;

import rx.Observable;

import static com.goldmf.GMFund.extension.MResultExtension.createObservableMResult;


/**
 * Created by yale on 16/2/18.
 */
public class AwardController {
    private AwardController() {
    }

    public static Observable<MResultsInfo<Void>> refreshAccount() {
        return Observable.create(sub -> FortuneManager.getInstance().freshBountyAccount(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> fetchAwardList() {
        return Observable.create(sub -> FortuneManager.getInstance().freshBountyList(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> withdraw(BountyAccount account) {
        return Observable.create(sub -> FortuneManager.getInstance().exchange(account.moneyType, account.validAmount, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<RegisterBounty>> fetchRegistBounity() {
        return Observable.create(sub -> FortuneManager.getInstance().getRegisterBounty(createObservableMResult(sub)));
    }
}
