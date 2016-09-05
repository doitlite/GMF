package com.goldmf.GMFund.controller.internal;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.controller.BaseFragment;
import com.goldmf.GMFund.controller.CommonProxyActivity;
import com.goldmf.GMFund.controller.PayFragments;
import com.goldmf.GMFund.controller.RechargeFragments;
import com.goldmf.GMFund.controller.business.CashController;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.manager.cashier.BankCard;
import com.goldmf.GMFund.manager.cashier.CashierManager;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo;
import com.goldmf.GMFund.manager.cashier.ServerMsg;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.util.GlobalVariableDic;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.GMFProgressDialog;

import java.lang.ref.WeakReference;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.controller.FragmentStackActivity.replaceTopFragment;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_RechargePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WithdrawPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.setupMessageTip;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.UIControllerExtension.showAlertDialogOrToastWithFragment;

/**
 * Created by yale on 15/10/13.
 */
public class CashUIController {
    private CashUIController() {
    }


    public static void performRecharge(BaseFragment fragment, Dialog dialog, boolean inInvest, double rechargeAmount) {
        fragment.consumeEventMR(CashController.sendRechargeProtocol(inInvest, rechargeAmount))
                .setTag("send_recharge_protocol")
                .onNextStart(response -> {
                    if (dialog != null)
                        dialog.dismiss();
                })
                .onNextSuccess(response -> {
                    ServerMsg<RechargeDetailInfo> msg = response.data;
                    RechargeDetailInfo info = msg.getData();
                    GlobalVariableDic.shareInstance().update(CommonProxyActivity.KEY_RECHARGE_DETAIL_INFO, info);
                    if (TextUtils.isEmpty(msg.tip)) {
                        performOneOrMultipleRecharge(fragment, info, response);
                        return;
                    }
                    setupMessageTip(fragment.getActivity(), msg, () -> {
                        performOneOrMultipleRecharge(fragment, info, response);
                    });
                })
                .onNextFail(response -> {
                    showAlertDialogOrToastWithFragment(new WeakReference<>(fragment), getErrorMessage(response));
                })
                .done();
    }

    private static void performOneOrMultipleRecharge(BaseFragment fragment, RechargeDetailInfo info, MResults.MResultsInfo<ServerMsg<RechargeDetailInfo>> response) {
        String bankName = CashierManager.getInstance().getCard().bank.bankName;
        if (info.multiple) {
            replaceTopFragment(fragment, new RechargeFragments.CNAccountMultipleRechargeFragment().init(info));
            UmengUtil.stat_continue_recharge_to_cn_account(fragment.getActivity(), true, bankName, info.rechargeTotalAmount, Optional.of(response.errCode), Optional.of(response.msg));
        } else {
            replaceTopFragment(fragment, new PayFragments.SinaPayFragment().init(info.rechargePayAction.url, true, true));
            UmengUtil.stat_once_recharge_to_cn_account(fragment.getActivity(), true, bankName, info.rechargeTotalAmount, Optional.of(response.errCode), Optional.of(response.msg));
        }
    }

    public static void performInvest(BaseFragment fragment, Dialog dialog, int fundId, double investAmount, String couponID) {
        fragment.consumeEventMR(CashController.investFund(fundId, investAmount, couponID))
                .setTag("request_invest_fund")
                .onNextStart(response -> {
                    if (dialog != null)
                        dialog.dismiss();
                })
                .onNextSuccess(response -> {
                    ServerMsg<Object> msg = response.data;
                    if (TextUtils.isEmpty(msg.tip)) {
                        performRechargeOrInvest(fragment, msg);
                        return;
                    }
                    setupMessageTip(fragment.getActivity(), msg, () -> {
                        performRechargeOrInvest(fragment, msg);
                    });
                })
                .onNextFail(response -> {
                    showAlertDialogOrToastWithFragment(new WeakReference<>(fragment), getErrorMessage(response));
                })
                .done();
    }

    private static void performRechargeOrInvest(BaseFragment fragment, ServerMsg<Object> investMsg) {
        if (investMsg.getData() instanceof Double) {
            GMFProgressDialog dialog = new GMFProgressDialog(fragment.getActivity());
            dialog.show();
            double rechargeAmount = (double) investMsg.getData();
            performRecharge(fragment, dialog, true, rechargeAmount);
        } else if (investMsg.getData() instanceof RechargeDetailInfo.PayAction) {
            RechargeDetailInfo.PayAction payAction = (RechargeDetailInfo.PayAction) investMsg.getData();
            GlobalVariableDic.shareInstance().update(CommonProxyActivity.KEY_RECHARGE_DETAIL_INFO, null);
            replaceTopFragment(fragment, new PayFragments.SinaPayFragment().init(payAction.url, true, true));
        }
    }


    /**
     * 执行提现的操作
     */
    public static void performWithdraw(Activity activity, Optional<Fragment> fragmentHolder, int moneyType) {
        if (moneyType == Money_Type.CN) {
            BankCard cnCard = CashierManager.getInstance().getCard();
            if (cnCard.status == BankCard.Card_Status_Normal) {
                showActivity(activity, an_WithdrawPage(Money_Type.CN));
            } else {
                GMFDialog.Builder builder = new GMFDialog.Builder(activity);
                builder.setTitle("提示");
                builder.setMessage("你还没有绑定国内银行卡，需先进行绑卡验证，如有疑问请在消息中联系客服");
                builder.setPositiveButton("去绑卡", (dialog, which) -> {
                    dialog.dismiss();
                    showActivity(activity, an_WithdrawPage(Money_Type.CN));
                });
                builder.setNegativeButton("取消");
                builder.create().show();
            }
            UmengUtil.stat_withdraw_from_cn_account_event(activity, fragmentHolder);
        } else if (moneyType == Money_Type.HK) {
            BankCard hkCard = CashierManager.getInstance().getHkCard();
            if (hkCard.status == BankCard.Card_Status_Normal) {
                showActivity(activity, an_WithdrawPage(Money_Type.HK));
            } else {
                GMFDialog.Builder builder = new GMFDialog.Builder(activity);
                builder.setTitle("提示");
                builder.setMessage("你还没有绑定港股账号，需先进行充值验证，如有疑问请在消息中联系客服");
                builder.setPositiveButton("去充值", (dialog, which) -> {
                    dialog.dismiss();
                    showActivity(activity, an_RechargePage(Money_Type.HK, 0D));
                });
                builder.setNegativeButton("取消");
                builder.create().show();
            }
            UmengUtil.stat_withdraw_from_hk_account_event(activity, fragmentHolder);
        }
    }
}
