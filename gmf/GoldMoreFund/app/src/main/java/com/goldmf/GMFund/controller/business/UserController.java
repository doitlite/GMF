package com.goldmf.GMFund.controller.business;

import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.extension.MResultExtension;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.mine.PhoneState;
import com.goldmf.GMFund.manager.stock.RealUserIncomeChart;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.UmengUtil;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;

import static com.goldmf.GMFund.extension.MResultExtension.createObservableListMResult;

/**
 * Created by yale on 16/2/17.
 */
public class UserController {
    private UserController() {
    }

    public static Observable<MResultsInfo<PhoneState>> verifyPhone(final String phone) {
        return Observable.create(sub -> MineManager.getInstance().verifyPhone(phone, MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<User>> login(final String phone, final String pwd) {
        return Observable.create(sub -> MineManager.getInstance().login(MineManager.PhoneLogin, phone, pwd, MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<User>> loginByWXAccount(UmengUtil.WXLoginInfo loginInfo) {
        return Observable.create(sub -> MineManager.getInstance().login(MineManager.WeChatLogin, loginInfo.accessToken, loginInfo.openId, MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> bindWXAccount(UmengUtil.WXLoginInfo loginInfo) {
        return Observable.create(sub -> MineManager.getInstance().binkWX(loginInfo.accessToken, loginInfo.openId, MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> unbindWXAccount() {
        return Observable.create(sub -> MineManager.getInstance().clearWX(MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Boolean>> sendRegistrationCode(String phone) {
        return Observable.create(sub -> MineManager.getInstance().sendPhoneVerifyCode(phone, MineManager.VerifyCode.Regist, MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<User>> register(String phone, String pwd, String name, String code, String inviteUserId) {
        Observable<MResultsInfo<User>> observable = Observable.create(sub -> MineManager.getInstance().register(phone, pwd, name, code, inviteUserId, MResultExtension.createObservableMResult(sub)));
        observable = observable.map(it -> {
            it.isSuccess = it.data != null;
            return it;
        });
        return observable;
    }

    public static Observable<MResultsInfo<Void>> modifyName(String name) {
        return Observable.create(sub -> MineManager.getInstance().modifyInfo(1, name, MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> modifyAvatar(String newAvatar, boolean uploadToQiNiu) {
        return Observable.create(sub -> MineManager.getInstance().modifyInfo(2, newAvatar, MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> modifyShippingAddress(Mine.ShippingAddress address) {
        return Observable.create(sub -> MineManager.getInstance().modifyInfo(4, address, MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> modifyHideVtcProfile(boolean isHide) {
        return Observable.create(sub -> MineManager.getInstance().modifyInfo(5, isHide, MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<List<String>>> fetchAvatarList() {
        return Observable.create(sub -> MineManager.getInstance().getAvatarLis(createObservableListMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> modifyLoginPassword(String oldPassword, String newPassword, String confirmPassword, Action1<MResultsInfo<Void>> doOnCall) {
        return Observable.create(sub -> MineManager.getInstance().ModifyPassword(false, oldPassword, newPassword, confirmPassword, MResultExtension.createObservableMResult(sub, doOnCall)));
    }

    public static Observable<MResultsInfo<Void>> modifyTransactionPassword(String oldPassword, String newPassword, Action1<MResultsInfo<Void>> doOnCall) {
        return Observable.create(sub -> MineManager.getInstance().modifyTradePassword(oldPassword, newPassword, MResultExtension.createObservableMResult(sub, doOnCall)));
    }

    public static Observable<MResultsInfo<Void>> resetTransactionPwd(String code, String newPassword) {
        return Observable.create(sub -> MineManager.getInstance().resetTradePassword(code, newPassword, MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> resetLoginPassword(String code, String newPwd, String confirmPwd, Action1<MResultsInfo<Void>> doOnCall) {
        return Observable.create(sub -> MineManager.getInstance().ModifyPassword(true, code, newPwd, confirmPwd, MResultExtension.createObservableMResult(sub, doOnCall)));
    }

    public static Observable<MResultsInfo<Boolean>> sendCode(String phone, MineManager.VerifyCode codeType) {
        return Observable.create(sub -> MineManager.getInstance().sendPhoneVerifyCode(phone, codeType, MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> verifyOldPhone(String code) {
        return Observable.create(sub -> MineManager.getInstance().modifyPhone(null, code, MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<Void>> bindNewPhone(String phone, String code, Action1<MResultsInfo<Void>> doOnCall) {
        return Observable.create(sub -> MineManager.getInstance().modifyPhone(phone, code, MResultExtension.createObservableMResult(sub, doOnCall)));
    }

    public static Observable<MResultsInfo<Void>> authentic(String name, String idCard) {
        return Observable.create(sub -> MineManager.getInstance().authenticate(name, idCard, MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<User>> refreshUserInfo(boolean bLocal) {
        if (bLocal && MineManager.getInstance().getmMe() != null) {
            MineManager.getInstance().getPro(null);
            return Observable.just(new MResultsInfo<User>().setData(MineManager.getInstance().getmMe()));
        }
        return Observable.create(sub -> MineManager.getInstance().getPro(MResultExtension.createObservableMResult(sub)));
    }

    public static Observable<MResultsInfo<List<RealUserIncomeChart>>> fetchRealPerformance(int userID) {
        return Observable.create(sub -> FortuneManager.getInstance().freshIncomeChart(userID, createObservableListMResult(sub)));
    }
}
