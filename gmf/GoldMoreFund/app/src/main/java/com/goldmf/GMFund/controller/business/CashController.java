package com.goldmf.GMFund.controller.business;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.manager.cashier.BankCard;
import com.goldmf.GMFund.manager.cashier.BankInfo;
import com.goldmf.GMFund.manager.cashier.CashierManager;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo.PayAction;
import com.goldmf.GMFund.manager.cashier.ServerMsg;
import com.goldmf.GMFund.manager.fortune.AccountTradeInfo;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.manager.score.ScoreManager;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.model.TarLinkButton;
import com.goldmf.GMFund.protocol.base.CommandPageArray;

import rx.Observable;
import rx.functions.Action1;

import static com.goldmf.GMFund.extension.MResultExtension.createObservableMResult;

/**
 * Created by yale on 16/2/17.
 */
public class CashController {
    private CashController() {
    }


    public static Observable<MResultsInfo<Void>> refreshBank() {
        return Observable.create(sub -> CashierManager.getInstance().freshBanks(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<CommandPageArray<AccountTradeInfo>>> fetchTradeJournal(int index) {
        return Observable.create(sub -> FortuneManager.getInstance().freshAccountTraderList(index, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<BankCard>> refreshBankCard(boolean bLocal, int moneyType) {

        if (bLocal) {
            BankCard data = null;
            if (moneyType == Money_Type.CN)
                data = CashierManager.getInstance().getCard();
            else if (moneyType == Money_Type.HK)
                data = CashierManager.getInstance().getHkCard();

            if (data.status != BankCard.Card_Status_Fail) {

                CashierManager.getInstance().freshMyBankCard(moneyType, null);

                MResults.MResultsInfo<BankCard> info = new MResultsInfo<BankCard>().setData(data);
                return Observable.just(info);
            }
        }

        return Observable.create(sub -> CashierManager.getInstance().freshMyBankCard(moneyType, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> sendBindBankCardCode(String bankCardNo, String phone, BankInfo bankInfo, String province, String city) {

        BankCard bindInfo = BankCard.buildWatingCard(new BankCard.Builder()
                .bank(bankInfo)
                .cardNO(bankCardNo)
                .cardPhone(phone)
                .province(province)
                .city(city)
                .build());
        return Observable.create(sub -> CashierManager.getInstance().bindBankCard(bindInfo, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> verifyBindBankCardCode(String code) {
        return Observable.create(sub -> CashierManager.getInstance().bindBankCardNext(code, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<ServerMsg<Boolean>>> queryChangeCard() {
        return Observable.create(sub -> CashierManager.getInstance().changeCard(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<ServerMsg<RechargeDetailInfo>>> sendRechargeProtocol(boolean inInvest,double amount) {
        return Observable.create(sub -> CashierManager.getInstance().beginRecharge(inInvest,amount, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<ServerMsg<RechargeDetailInfo>>> sendContinueRechargeProtocol(String orderId) {
        return Observable.create(sub -> CashierManager.getInstance().continueRecharge(orderId, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<ServerMsg<Object>>> investFund(int fundID, double amount, String couponID) {
        return Observable.create(sub -> CashierManager.getInstance().invest(fundID, amount, couponID, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> finishRechargeOrInvest() {
        return Observable.create(sub -> CashierManager.getInstance().finishRechargeOrInvest(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<ServerMsg<PayAction>>> withdraw(int moneyType, double amount) {
        return Observable.create(sub -> CashierManager.getInstance().withdraw(moneyType, amount, createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> withdrawSuccess() {
        return Observable.create(sub -> CashierManager.getInstance().withdrawSuccess(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> scoreBuySuccess() {
        return Observable.create(sub -> CashierManager.getInstance().costSuccess(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> refreshAccount(boolean bLocal) {
        if (bLocal && (FortuneManager.getInstance().cnAccount != null || FortuneManager.getInstance().hkAccount != null)) {
            FortuneManager.getInstance().freshAccount(null);
            MResults.MResultsInfo<Void> info = new MResultsInfo<>();
            return Observable.just(info);
        }
        return Observable.create(sub -> FortuneManager.getInstance().freshAccount(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<ServerMsg<PayAction>>> submitWithdrawRequestOfHKAccount(double amount, Action1<MResultsInfo<ServerMsg<PayAction>>> doOnCall) {
        return Observable.create(sub -> CashierManager.getInstance().withdraw(Money_Type.HK, amount, createObservableMResult(sub, doOnCall)));
    }

    public static Observable<MResultsInfo<Void>> submitRechargeRecord(BankCard bankCard, String certificate, double amount, String remark, Action1<MResultsInfo<Void>> doOnCall) {
        return Observable.create(sub -> CashierManager.getInstance().depositRecharge(bankCard, certificate, amount, remark, createObservableMResult(sub, doOnCall)));
    }

    public static Observable<MResults.MResultsInfo<TarLinkButton.TarLinkText>> fetchBankTips() {
        return Observable.create(sub -> CashierManager.getInstance().freshBankTips(createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<PayAction>> fetchBuyScore(double amount) {
        return Observable.create(sub -> ScoreManager.getInstance().buyScore(amount, createObservableMResult(sub)));
    }
}
