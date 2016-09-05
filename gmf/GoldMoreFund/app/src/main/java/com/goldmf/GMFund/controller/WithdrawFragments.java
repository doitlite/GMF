package com.goldmf.GMFund.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.controller.business.CashController;
import com.goldmf.GMFund.controller.dialog.EnterTransactionPasswordDialog;
import com.goldmf.GMFund.controller.internal.ActivityNavigation;
import com.goldmf.GMFund.manager.cashier.BankCard;
import com.goldmf.GMFund.manager.cashier.CashierManager;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo.PayAction;
import com.goldmf.GMFund.manager.cashier.ServerMsg;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.BasicCell;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.goldmf.GMFund.widget.ProgressButton;
import com.goldmf.GMFund.widget.TopNotificationView;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.subjects.PublishSubject;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.FragmentStackActivity.replaceTopFragment;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.setupMessageTip;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.YELLOW_COLOR;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.StringExtension.formatBankCardNoTransformer;
import static com.goldmf.GMFund.extension.StringExtension.map;
import static com.goldmf.GMFund.extension.StringExtension.normalMoneyTransformer;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showAlertDialogOrToastWithFragment;
import static com.goldmf.GMFund.extension.UIControllerExtension.showKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.v_addTextChangedListener;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_moneyFormatterFormatter;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setHint;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static yale.extension.common.PreCondition.checkNotNull;

/**
 * Created by yale on 15/10/14.
 */
public class WithdrawFragments {
    public static PublishSubject<Pair<Integer, String>> sWebWithdrawSuccessSubject = PublishSubject.create();

    private WithdrawFragments() {
    }

    public static class CNAccountWithdrawFragment extends BaseFragment {
        private EditText mAmountField;
        private Button mWithdrawButton;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_withdraw_cn_account, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupBackButton(getActivity(), findToolbar(this), R.drawable.ic_close_light);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);

            if (!MineManager.getInstance().isLoginOK()) {
                goBack(this);
            }

            // bind child view
            mAmountField = v_findView(view, R.id.field_amount);
            mWithdrawButton = v_findView(view, R.id.btn_withdraw);
            mWithdrawButton.setEnabled(false);

            // init child view
            v_setHint(view, R.id.field_amount, "请输入提现金额,账户余额 " + formatMoney(CashierManager.getInstance().getCnCashBalance(), false, 0, 2) + "元");
            v_addTextChangedListener(mAmountField, v_moneyFormatterFormatter(v_findView(view, R.id.field_amount), false, 0, 2));
            v_addTextChangedListener(mAmountField, editable -> mWithdrawButton.setEnabled(editable.length() > 0));

            v_setClick(view, R.id.btn_withdraw, v -> performWithdraw());

            mAmountField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200);

            consumeEventMR(CashController.fetchBankTips())
                    .setTag("fresh_bank_tips")
                    .onNextSuccess(response -> {
                        TopNotificationView notificationView = v_findView(view, R.id.section_notification);
                        notificationView.setupWithTarLinkText(response.data, true);
                    })
                    .done();
        }

        private void performWithdraw() {
            GMFProgressDialog dialog = new GMFProgressDialog(getActivity());
            dialog.show();
            String withdrawAmountStr = map(mAmountField, normalMoneyTransformer());
            double withdrawAmount = Double.valueOf(withdrawAmountStr);
            consumeEventMR(CashController.withdraw(Money_Type.CN, withdrawAmount))
                    .setTag("request_withdraw")
                    .onNextStart(response -> {
                        dialog.dismiss();
                    })
                    .onNextSuccess(response -> {
                        NotificationCenter.cashChangedSubject.onNext(null);
                        ServerMsg<PayAction> msg = response.data;
                        if (msg != null) {
                            PayAction payAction = msg.getData();
                            if (TextUtils.isEmpty(msg.tip)) {
                                replaceTopFragment(this, new PayFragments.SinaPayFragment().init(payAction.url, true, true));
                                return;
                            }
                            setupMessageTip(getActivity(), msg, () -> {
                                replaceTopFragment(this, new PayFragments.SinaPayFragment().init(payAction.url, true, true));
                            });
                        }
                    })
                    .onNextFail(response -> {
                        showAlertDialogOrToastWithFragment(new WeakReference<>(this), getErrorMessage(response));
                    })
                    .done();
        }
    }

    public static class CNAccountWithdrawSuccessFragment extends BaseFragment {

        private String mMessage;

        public CNAccountWithdrawSuccessFragment init(String message) {
            Bundle argument = new Bundle();
            argument.putString(ActivityNavigation.KEY_CN_WITHDRAW_SUCCESS_MESSAGE, message);
            setArguments(argument);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mMessage = getArguments().getString(ActivityNavigation.KEY_CN_WITHDRAW_SUCCESS_MESSAGE, "");
            return inflater.inflate(R.layout.frag_withdraw_cn_account_success, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this,findToolbar(this),R.drawable.ic_close_light);
            v_setText(view, R.id.label_amount, mMessage);
            v_setClick(view, R.id.btn_done, this::onDoneButtonClick);
        }

        protected void onDoneButtonClick(View view) {
            goBack(this);
        }
    }

    public static class HKAccountWithdrawFragment extends BaseFragment {
        private TextView mCashierBankNameLabel;
        private TextView mCashierUserNameLabel;
        private TextView mCashierCardIDLabel;
        private TextView mSymbolLabel;
        private EditText mWithdrawAmountField;
        private ProgressButton mWithdrawButton;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_withdraw_hk_account, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_dark);
            setStatusBarBackgroundColor(this, YELLOW_COLOR);

            // bind child views
            mCashierBankNameLabel = ((BasicCell) v_findView(this, R.id.cell_cashier_bank)).getExtraTitleLabel();
            mCashierUserNameLabel = ((BasicCell) v_findView(this, R.id.cell_cashier_user_name)).getExtraTitleLabel();
            mCashierCardIDLabel = ((BasicCell) v_findView(this, R.id.cell_cashier_card_id)).getExtraTitleLabel();
            mSymbolLabel = v_findView(this, R.id.label_symbol);
            mWithdrawAmountField = v_findView(this, R.id.field_amount);
            mWithdrawButton = v_findView(this, R.id.btn_withdraw);
            mWithdrawButton.setEnabled(false);

            // init child views
            v_setClick(this, R.id.btn_balance, this::onBalanceButtonClick);
            v_setClick(mWithdrawButton, v -> performWithdraw(false));
            v_setText(mSymbolLabel, Money_Type.getSymbol(Money_Type.HK));
            v_setHint(mWithdrawAmountField, "账户余额 " + formatMoney(CashierManager.getInstance().getHkCashBalance(), false, 2) + Money_Type.getUnit(Money_Type.HK));
            v_addTextChangedListener(mWithdrawAmountField, editable -> mWithdrawButton.setEnabled(editable.length() > 0));

            BankCard card = CashierManager.getInstance().getHkCard();
            checkNotNull(card);
            mCashierBankNameLabel.setText(card.bank.bankName);
            mCashierUserNameLabel.setText(card.cardUserName);
            mCashierCardIDLabel.setText(map(card.cardNO, formatBankCardNoTransformer()));

            mWithdrawAmountField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200);
        }

        private void onBalanceButtonClick() {
            double balance = CashierManager.getInstance().getHkCashBalance();
            if (balance <= 0) {
                mWithdrawAmountField.setText("");
            } else {
                mWithdrawAmountField.setText(formatMoney(balance, false, 2));
            }
        }

        @SuppressWarnings("ConstantConditions")
        private void performWithdraw(boolean hasInputTraderPassword) {
            mWithdrawButton.setMode(ProgressButton.Mode.Loading);

            double money = Double.parseDouble(map(mWithdrawAmountField, normalMoneyTransformer()));
            if (hasInputTraderPassword) {
                mIsOperation = true;
                consumeEventMR(requestWithdrawHKAccountMoney(new WeakReference<>(this), money))
                        .onNextStart(response -> {
                            mIsOperation = false;
                        })
                        .onNextSuccess(response -> {
                            mWithdrawButton.setMode(ProgressButton.Mode.Normal);
                            String bankName = safeGet(() -> CashierManager.getInstance().getHkCard().bank.bankName, "");
                            FragmentStackActivity.replaceTopFragment(this, new HKAccountWithdrawSuccessFragment().init(response.msg, Money_Type.HK, money));
                            UmengUtil.stat_withdraw_from_hk_account(getActivity(), true, bankName, money, Optional.of(response.errCode), Optional.of(response.msg));
                        })
                        .onNextFail(callback -> {
                            mWithdrawButton.setMode(ProgressButton.Mode.Normal);
                        })
                        .done();
            } else {
                BankCard hkBankCard = CashierManager.getInstance().getHkCard();
                EnterTransactionPasswordDialog dialog = new EnterTransactionPasswordDialog(getActivity(), hkBankCard, Money_Type.HK, money);
                dialog.setSuccessEnterTransactionPwdBlock(() -> performWithdraw(true));
                dialog.show();
            }
        }

        private static Observable<MResults.MResultsInfo<ServerMsg<PayAction>>> requestWithdrawHKAccountMoney(WeakReference<BaseFragment> fragRef, double amount) {
            return CashController.submitWithdrawRequestOfHKAccount(amount, callback -> {
                if (isSuccess(callback)) {
                    showToast(MyApplication.SHARE_INSTANCE, "你的提现 " + Money_Type.getSymbol(Money_Type.HK) + " " + formatMoney(amount, false, 2) + " 已提交系统");
                } else {
                    showAlertDialogOrToastWithFragment(fragRef, getErrorMessage(callback));
                }
            });
        }
    }

    public static class HKAccountWithdrawSuccessFragment extends BaseFragment {
        public static final String KEY_RETURN_MESSAGE = "return_Message";
        public static final String KEY_MONEY_TYPE = "money_type";
        public static final String KEY_MONEY = "money";
        private String mReturnMessage;
        private int mMoneyType;
        private double mMoney;

        public HKAccountWithdrawSuccessFragment init(String returnMessage, int moneyType, double money) {
            Bundle arguments = new Bundle();
            arguments.putString("return_Message", returnMessage);
            arguments.putInt("money_type", moneyType);
            arguments.putDouble("money", money);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mReturnMessage = getArguments().getString(KEY_RETURN_MESSAGE);
            mMoneyType = getArguments().getInt(KEY_MONEY_TYPE);
            mMoney = getArguments().getDouble(KEY_MONEY);
            return inflater.inflate(R.layout.frag_withdraw_hk_account_success, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            v_setText(this, R.id.label_amount, "你的提现 " + Money_Type.getSymbol(mMoneyType) + " " + formatMoney(mMoney, false, 2) + " 已提交系统");
            v_setClick(this, R.id.btn_done, v -> FragmentStackActivity.goBack(this));
        }
    }
}
