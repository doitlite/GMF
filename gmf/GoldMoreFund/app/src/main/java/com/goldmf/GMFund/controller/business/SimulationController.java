package com.goldmf.GMFund.controller.business;

import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.extension.MResultExtension;
import com.goldmf.GMFund.manager.discover.UserManager;
import com.goldmf.GMFund.manager.stock.SimulationUserIncomeChart;
import com.goldmf.GMFund.manager.stock.StockManager;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.protocol.base.CommandPageArray;

import rx.Observable;

/**
 * Created by Evan on 16/3/15 下午2:46.
 */
public class SimulationController {
    private SimulationController() {

    }

    /**
     * 获取某个用户的模拟业绩
     */
    public static Observable<MResultsInfo<SimulationUserIncomeChart>> fetchSimulationPerformance(int userID) {
        return Observable.create(sub -> StockManager.getInstance().freshIncomeChart(userID, MResultExtension.createObservableMResult(sub)));
    }

    /**
     * 刷新关注或粉丝列表
     */
    public static Observable<MResultsInfo<CommandPageArray<User>>> freshFollowUser(User user, int type) {
        return Observable.create(sub -> UserManager.getInstance().freshFollowUser(user, type, MResultExtension.createObservableMResult(sub)));
    }

    /**
     * 添加关注
     */
    public static Observable<MResultsInfo<Void>> addFollowUser(User user) {
        return Observable.create(sub -> UserManager.getInstance().addFollowUser(user, MResultExtension.createObservableMResult(sub)));
    }

    /**
     * 删除关注
     */
    public static Observable<MResultsInfo<Void>> deleteFollowUser(User user) {
        return Observable.create(sub -> UserManager.getInstance().delFollowUser(user, MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<User>> freshUserDetail(User user) {
        return Observable.create(sub -> UserManager.getInstance().freshUserDetail(user, MResultExtension.createObservableMResult(sub)));
    }

}
