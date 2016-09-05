package com.goldmf.GMFund.controller.business;

import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.manager.discover.DiscoverManager;
import com.goldmf.GMFund.manager.discover.UserManager;
import com.goldmf.GMFund.manager.fortune.FortuneInfo;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.manager.trader.TraderManager;
import com.goldmf.GMFund.model.Fund;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.manager.trader.FundInvestor;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.widget.AppBarLayoutBehavior;

import java.util.Collections;
import java.util.List;

import rx.Observable;

import static com.goldmf.GMFund.extension.MResultExtension.createObservableMResult;
import static com.goldmf.GMFund.extension.MResultExtension.createObservableListMResult;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;

/**
 * Created by yale on 16/2/17.
 */
public class FundController {
    private FundController() {
    }


    public static Observable<MResultsInfo<Void>> bookFund(Fund fund) {
        return Observable.create(sub -> TraderManager.getInstance().booking(fund, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Fund>> fetchFundInfo(final int fundId, boolean useCache) {
        Observable<MResultsInfo<Fund>> observable = Observable.create(sub -> TraderManager.getInstance().getPortfolio(useCache, fundId, createObservableMResult(sub)));
        return observable.map(it -> ObjectExtension.apply(it, response -> response.isSuccess = response.isSuccess && response.data != null));
    }

    public static Observable<MResultsInfo<List<FundInvestor>>> fetchInvestorInfoList(FundBrief fund) {
        return Observable.create(sub -> TraderManager.getInstance().getInvestors(fund, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<User>> fetchTraderInfo(int traderID) {
        User trader = new User();
        trader.index = traderID;
        return Observable.create(sub -> UserManager.getInstance().freshMoreInfo(trader, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<List<FundBrief>>> fetchMyFundList(boolean allowCache) {
        return Observable.create(sub -> TraderManager.getInstance().getFundList(allowCache, createObservableListMResult(sub)));
    }

    public static List<FundBrief> getCachedMyFundList() {
        return safeGet(() -> TraderManager.getInstance().getFundList(), Collections.emptyList());
    }

    public static Observable<MResultsInfo<FortuneInfo>> fetchMyInvestedFundList(int moneyType) {
        return Observable.create(sub -> FortuneManager.getInstance().freshHoldFunds(moneyType, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<List<FundBrief>>> fetchRecommendFundList(boolean bLocal) {
        return Observable.create(sub -> DiscoverManager.getInstance().freshRecommandFundList(bLocal, createObservableListMResult(sub)));
    }

}

