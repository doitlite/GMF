package com.goldmf.GMFund.controller.business;

import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.extension.MResultExtension;
import com.goldmf.GMFund.manager.fortune.Coupon;
import com.goldmf.GMFund.manager.fortune.FortuneManager;

import java.util.List;

import rx.Observable;

/**
 * Created by yalez on 2016/7/15.
 */
public class CouponController {
    private CouponController() {
    }

    public static Observable<MResultsInfo<List<Coupon>>> fetchCouponList() {
        return Observable.create(sub -> FortuneManager.getInstance().freshCouponList(MResultExtension.createObservableMResult(sub)));
    }
}
