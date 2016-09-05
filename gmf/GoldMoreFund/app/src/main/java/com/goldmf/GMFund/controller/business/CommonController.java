package com.goldmf.GMFund.controller.business;

import com.goldmf.GMFund.BuildConfig;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.common.RedPoint;
import com.goldmf.GMFund.manager.common.UpdateInfo;
import com.goldmf.GMFund.manager.discover.DiscoverManager;
import com.goldmf.GMFund.manager.discover.FocusInfo;
import com.goldmf.GMFund.manager.discover.UserManager;
import com.goldmf.GMFund.model.User;
import com.google.gson.JsonArray;

import java.util.List;

import rx.Observable;

import static com.goldmf.GMFund.extension.MResultExtension.createObservableMResult;
import static com.goldmf.GMFund.extension.MResultExtension.createObservableListMResult;

/**
 * Created by yale on 16/2/17.
 */
public class CommonController {
    private CommonController() {
    }

    public static Observable<MResultsInfo<UpdateInfo>> checkUpdate() {
        return Observable.create(sub -> CommonManager.getInstance().getNewVersion(createObservableMResult(sub)));
    }

//    public static Observable<MResultsInfo<List<FocusInfo>>> fetchGridItemList(boolean bLocal) {
//        return Observable.create(sub -> DiscoverManager.getInstance().freshPromotionList(bLocal, createObservableListMResult(sub)));
//    }

//    public static Observable<MResultsInfo<List<FocusInfo>>> fetchFundFocusList() {
//        return Observable.create(sub -> DiscoverManager.getInstance().freshFocusList(createObservableListMResult(sub)));
//    }

//    public static Observable<MResultsInfo<List<FocusInfo>>> fetchTraderFocusList(boolean bLocal) {
//        return Observable.create(sub -> DiscoverManager.getInstance().freshNewFocusList(bLocal, createObservableListMResult(sub)));
//    }

    public static Observable<MResultsInfo<Void>> fetchPromotion(boolean bLoad) {
        return Observable.create(sub -> DiscoverManager.getInstance().freshPromotion(bLoad, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<List<FocusInfo>>> fetchInvestFocusList(boolean bLoad) {
        return Observable.create(sub -> DiscoverManager.getInstance().freshInvestFocusList(bLoad, createObservableListMResult(sub)));
    }

    public static Observable<MResultsInfo<UserManager.TraderUserPage>> fetchTraderList() {
        return Observable.create(sub -> UserManager.getInstance().freshAllTraderList(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<UserManager.TraderUserPage>> fetchTalentList() {
        return Observable.create(sub -> UserManager.getInstance().freshAllTalentPage(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<JsonArray>> fetchQuestionList(boolean bLocal) {
        return Observable.create(sub -> CommonManager.getInstance().getNormalQuestion(bLocal, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<RedPoint>> refreshRedPoint(boolean bLocal, RedPoint redPoint) {
        if (bLocal) {
            return Observable.just(new MResultsInfo<>());
        }
        return Observable.create(sub -> redPoint.freshRedPoint(createObservableMResult(sub)));
    }
}
