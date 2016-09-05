package com.goldmf.GMFund.controller;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.controller.business.CashController;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo;
import com.goldmf.GMFund.manager.cashier.ServerMsg;
import com.goldmf.GMFund.model.Fund;
import com.goldmf.GMFund.util.GlobalVariableDic;

import java.lang.ref.WeakReference;

import static com.goldmf.GMFund.controller.FragmentStackActivity.replaceTopFragment;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.UIControllerExtension.createErrorDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;

/**
 * Created by Evan on 16/8/10 上午9:58.
 */
public class PayFragments {

    public static class SinaPayFragment extends WebViewFragments.WebViewFragment {

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            findToolbar(this).setNavigationOnClickListener(v -> {
                showExitDialog(new WeakReference<>(this));
            });

            consumeEvent(RechargeFragments.sWebRechargeSuccessSubject)
                    .setTag("web_recharge_success")
                    .onNextFinish(pair -> {
                        int resultCode = pair.first;
                        String message = pair.second;
                        if (resultCode == 1) {
                            consumeEventMR(CashController.finishRechargeOrInvest())
                                    .onNextFinish(ignored -> {
                                    })
                                    .done();
                            NotificationCenter.cashChangedSubject.onNext(null);
                            RechargeDetailInfo info = GlobalVariableDic.shareInstance().get(CommonProxyActivity.KEY_RECHARGE_DETAIL_INFO);
                            if (info != null) {
                                if (info.isFinish()) {
                                    replaceTopFragment(this, new RechargeFragments.CNAccountRechargeSuccessFragment().init(message));
                                } else {
                                    consumeEventMR(CashController.sendContinueRechargeProtocol(safeGet(() -> info.orderID, "")))
                                            .setTag("continue_recharge")
                                            .onNextFinish(response -> {
                                                if (isSuccess(response)) {
                                                    ServerMsg<RechargeDetailInfo> msg = response.data;
                                                    RechargeDetailInfo detailInfo = msg.getData();
                                                    GlobalVariableDic.shareInstance().update(CommonProxyActivity.KEY_RECHARGE_DETAIL_INFO, detailInfo);
                                                    replaceTopFragment(this, new RechargeFragments.CNAccountMultipleRechargeFragment().init(detailInfo));
                                                } else {
                                                    createErrorDialog(this, getErrorMessage(response)).show();
                                                }
                                            })
                                            .done();
                                }
                            }
                        } else if (resultCode == 0) {
                            replaceTopFragment(this, new RechargeFragments.CNAccountFailurePageFragment().init(message));
                        }
                    })
                    .done();

            consumeEvent(InvestFragments.InvestFundFragment.sWebInvestSuccessSubject)
                    .setTag("web_invest_success")
                    .onNextFinish(pair -> {
                        int resultCode = pair.first;
                        String message = pair.second;
                        if (resultCode == 1) {
                            consumeEventMR(CashController.finishRechargeOrInvest())
                                    .onNextFinish(ignored -> {
                                    })
                                    .done();
                            Fund fund = GlobalVariableDic.shareInstance().get(CommonProxyActivity.KEY_FUND);
                            NotificationCenter.cashChangedSubject.onNext(null);
                            NotificationCenter.investedFundSubject.onNext(fund.index);
                            replaceTopFragment(this, new InvestFragments.InvestFundSuccessFragment().init(message, fund.shareInfo));
                        } else if (resultCode == 0) {
                            replaceTopFragment(this, new RechargeFragments.CNAccountFailurePageFragment().init(message));
                        }
                    })
                    .done();

            consumeEvent(WithdrawFragments.sWebWithdrawSuccessSubject)
                    .setTag("web_withdraw_success")
                    .onNextFinish(pair -> {
                        int resultCode = pair.first;
                        String message = pair.second;
                        if (resultCode == 1) {
                            consumeEventMR(CashController.withdrawSuccess())
                                    .onNextFinish(ignored -> {
                                    })
                                    .done();
                            NotificationCenter.cashChangedSubject.onNext(null);
                            replaceTopFragment(this, new WithdrawFragments.CNAccountWithdrawSuccessFragment().init(message));
                        } else if (resultCode == 0) {
                            replaceTopFragment(this, new RechargeFragments.CNAccountFailurePageFragment().init(message));
                        }
                    })
                    .done();

            consumeEvent(ScoreFragments.sWebBuyScoreSuccessSubject)
                    .setTag("web_score_buy_success")
                    .onNextFinish(pair -> {
                        int resultCode = pair.first;
                        String message = pair.second;
                        if (resultCode == 1) {
                            consumeEventMR(CashController.scoreBuySuccess())
                                    .onNextFinish(ignored -> {
                                    })
                                    .done();
                            NotificationCenter.scoreChangedSubject.onNext(null);
                            replaceTopFragment(this, new ScoreFragments.ScoreBuySuccessFragment().init(message));
                        } else if (resultCode == 0) {
                            replaceTopFragment(this, new RechargeFragments.CNAccountFailurePageFragment().init(message));
                        }
                    })
                    .done();
        }

        @Override
        protected boolean onInterceptGoBack() {
            return super.onInterceptGoBack();
        }

        @Override
        protected boolean onInterceptKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                showExitDialog(new WeakReference<>(this));
                return true;
            }
            return super.onInterceptKeyDown(keyCode, event);
        }

        private static void showExitDialog(WeakReference<BaseFragment> fragmentRef) {
            GMFDialog.Builder builder = new GMFDialog.Builder(fragmentRef.get().getActivity());
            builder.setTitle("提示");
            builder.setMessage("处理尚未完成，确认离开当前流程?");
            builder.setPositiveButton("确认离开", (dialog, which) -> {
                dialog.dismiss();
                if (fragmentRef != null) {
                    if (fragmentRef.get().getActivity() instanceof Activity)
                        fragmentRef.get().getActivity().finish();
                }
            });
            builder.setNegativeButton("取消", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.create().show();
        }
    }

}
