package com.goldmf.GMFund.controller.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.extension.MResultExtension;
import com.goldmf.GMFund.manager.cashier.BankCard;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.widget.CustomPasswordField;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.goldmf.GMFund.widget.keyboard.BasePad;
import com.goldmf.GMFund.widget.keyboard.NumberPad;

import java.util.Locale;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ResetTransactionPasswordPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.createAlertDialog;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;

/**
 * Created by yale on 15/9/9.
 */
public class EnterTransactionPasswordDialog extends BasicDialog {

    private CustomPasswordField mPasswordField;
    private BasePad mPad;
    private TextView mTitleLabel;
    private TextView mMessageLabel;
    private TextView mExtraLabel;

    private Action0 mSuccessEnterTransactionPwdBlock;

    private StringBuilder mPassword = new StringBuilder();

    public EnterTransactionPasswordDialog(Context context, int moneyType, double buyAmount) {
        super(context, R.style.GMFDialog_FullScreen);
        init(setViewOfBuy(moneyType, buyAmount));
    }

    public EnterTransactionPasswordDialog(Context context, FundBrief fund, double investAmount, double discountAmount) {
        super(context, R.style.GMFDialog_FullScreen);
        init(setViewOfInvest(fund, investAmount, discountAmount));
    }

    public EnterTransactionPasswordDialog(Context context, BankCard cnBankCard, int moneyType, double withdrawAmount) {
        super(context, R.style.GMFDialog_FullScreen);
        init(setViewOfWithdraw(cnBankCard, moneyType, withdrawAmount));
    }

    private void init(Action0 afterInitHandler) {
        setContentView(R.layout.dialog_transaction_password);
        // bind child views
        mPasswordField = v_findView(this, R.id.field_pwd);
        mPad = v_findView(this, R.id.pad);
        mTitleLabel = v_findView(this, R.id.label_title);
        mMessageLabel = v_findView(this, R.id.label_message);
        mExtraLabel = v_findView(this, R.id.label_extra);
        v_setClick(this, R.id.btn_close, v -> dismiss());

        afterInitHandler.call();
    }

    private Action0 setViewOfBuy(int moneyType, double buyAmount) {
        return () -> {
            mTitleLabel.setText("请输入交易密码");
            mMessageLabel.setText("购买积分" + formatMoney(buyAmount, false, 2) + Money_Type.getUnit(moneyType));
            mPasswordField.setCurrentNumCount(0);
            mPasswordField.setMaxNumCount(6);
            bindKeyboard();
        };
    }

    private Action0 setViewOfInvest(FundBrief fund, double investAmount, double discountAmount) {

        return () -> {
            mTitleLabel.setText("请输入交易密码");
            CharSequence text = concat("投资到" + fund.name,
                    formatMoney(investAmount, false, 0));
            mMessageLabel.setText(text);
            if (discountAmount > 0) {
                CharSequence formatDiscountAmount = formatMoney(discountAmount, false, 0, 2);
                CharSequence formatPayAmount = formatMoney(investAmount - discountAmount, false, 0, 2);
                CharSequence extraTitle = setColor(String.format(Locale.getDefault(), "红包抵扣 %s 仅需支付%s", formatDiscountAmount, formatPayAmount), TEXT_RED_COLOR);
                v_setText(mExtraLabel, extraTitle);
            }
            v_setVisibility(mExtraLabel, discountAmount > 0 ? View.VISIBLE : View.GONE);
            mPasswordField.setCurrentNumCount(0);
            mPasswordField.setMaxNumCount(6);
            bindKeyboard();
        };
    }

    private Action0 setViewOfWithdraw(BankCard bankCard, int moneyType, double withdrawAmount) {
        return () -> {
            mTitleLabel.setText("请输入交易密码");
            mMessageLabel.setText("提现到" + bankCard.bank.bankName + " (" + "尾号" + bankCard.cardMsg + ")\n" + Money_Type.getSymbol(moneyType) + formatMoney(withdrawAmount, false, 2) + Money_Type.getUnit(moneyType));
            mPasswordField.setCurrentNumCount(0);
            mPasswordField.setMaxNumCount(6);
            bindKeyboard();
        };
    }

    @Override
    public void show() {
        super.show();
        mPassword.setLength(0);
        mPasswordField.setCurrentNumCount(0);
    }

    public EnterTransactionPasswordDialog setSuccessEnterTransactionPwdBlock(Action0 block) {
        mSuccessEnterTransactionPwdBlock = block;
        return this;
    }

    private void bindKeyboard() {
        v_setClick(mPasswordField, v -> mPad.show());

        mPad.setOnPadKeyActionListener(new BasePad.OnPadKeyActionListener() {
            @Override
            public void onKeyPressed(char ch) {
                appendPassword(ch, mPassword, this::afterInputPassword);
            }

            @Override
            public void onActionTrigger(int action) {
                delPassword(action, mPassword);
            }

            private void appendPassword(char ch, StringBuilder sb, Action0 finishInputCallback) {
                if (mPasswordField.getCurrentNumCount() < mPasswordField.getMaxNumCount()) {
                    mPasswordField.increaseNumCount();
                    sb.append(ch);
                    if (mPasswordField.getCurrentNumCount() == mPasswordField.getMaxNumCount()) {
                        finishInputCallback.call();
                    }
                }
            }

            private void delPassword(int action, StringBuilder sb) {
                if (action == NumberPad.ACTION_CODE_DEL) {
                    if (mPasswordField.getCurrentNumCount() > 0) {
                        mPasswordField.decreaseNumCount();
                        sb.deleteCharAt(sb.length() - 1);
                    }
                }
            }

            private void afterInputPassword() {
                String password = mPassword.toString();
                performEnterTransactionPassword(password);
            }
        });
    }

    private void performEnterTransactionPassword(String password) {
        dismiss();
        GMFProgressDialog progressView = new GMFProgressDialog(getContext());
        progressView.setMessage("正在校验，请稍后");
        progressView.show();
        requestEnterTransactionPassword(password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    progressView.dismiss();
                    if (isSuccess(data)) {
                        if (mSuccessEnterTransactionPwdBlock != null) {
                            mSuccessEnterTransactionPwdBlock.call();
                            mSuccessEnterTransactionPwdBlock = null;
                        }
                    } else {
                        if (data.errCode == 5022104) {
                            //交易密码错误
                            GMFDialog.Builder builder = new GMFDialog.Builder(getContext());
                            builder.setMessage("交易密码错误");
                            builder.setPositiveButton("重新输入", (dialog, which) -> {
                                dialog.dismiss();
                                EnterTransactionPasswordDialog.this.show();
                            });
                            builder.setNegativeButton("忘记密码", (dialog, which) -> {
                                dialog.dismiss();
                                showActivity(getContext(), an_ResetTransactionPasswordPage());
                            });
                            builder.create().show();
                        } else {
                            createAlertDialog(getContext(), getErrorMessage(data)).show();
                        }
                    }
                });
    }

    private static Observable<MResults.MResultsInfo<Void>> requestEnterTransactionPassword(String password) {
        return Observable.create(sub -> MineManager.getInstance().enterTraderPassword(password, MResultExtension.createObservableMResult(sub)));
    }
}
