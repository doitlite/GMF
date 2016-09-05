package com.goldmf.GMFund.controller.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.extension.MResultExtension;
import com.goldmf.GMFund.extension.UIControllerExtension;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.widget.CustomPasswordField;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.goldmf.GMFund.widget.keyboard.BasePad;
import com.goldmf.GMFund.widget.keyboard.NumberPad;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;

/**
 * Created by yale on 15/9/9.
 */
public class SetTransactionPasswordDialog extends BasicDialog {

    private CustomPasswordField mPasswordField;
    private BasePad mPad;
    private TextView mTitleLabel;
    private TextView mMessageLabel;

    private StringBuilder mPassword = new StringBuilder();
    private StringBuilder mConfirmPassword = new StringBuilder();
    private boolean mHasSetPassword = false;
    private Action0 mSuccessSetTransactionBlock;

    public SetTransactionPasswordDialog(Context context) {
        super(context, R.style.GMFDialog_FullScreen);
        setContentView(R.layout.dialog_transaction_password);
        getWindow().setLayout(-1, -1);

        // bind child views
        mPasswordField = v_findView(this, R.id.field_pwd);
        mPad = v_findView(this, R.id.pad);
        mTitleLabel = v_findView(this, R.id.label_title);
        mMessageLabel = v_findView(this, R.id.label_message);

        mTitleLabel.setText("首次设置交易密码");
        mMessageLabel.setText("6位数字密码，用于交易验证");
        mPasswordField.setCurrentNumCount(0);
        mPasswordField.setMaxNumCount(6);
        bindKeyboard();

        v_setClick(this, R.id.btn_close, this::dismiss);
    }

    public SetTransactionPasswordDialog setSuccessSetTransactionBlock(Action0 block) {
        mSuccessSetTransactionBlock = block;
        return this;
    }

    private void bindKeyboard() {
        v_setClick(mPasswordField, v -> mPad.show());

        mPad.setOnPadKeyActionListener(new BasePad.OnPadKeyActionListener() {
            @Override
            public void onKeyPressed(char ch) {
                if (!mHasSetPassword) {
                    if (mPassword.length() == 0)
                        beforeInputFirstChar();
                    appendPassword(ch, mPassword, this::afterInputFullPassword);
                } else {
                    appendPassword(ch, mConfirmPassword, this::afterInputFullConfirmPassword);
                }
            }

            @Override
            public void onActionTrigger(int action) {
                if (!mHasSetPassword) {
                    delPassword(action, mPassword);
                } else {
                    delPassword(action, mConfirmPassword);
                }
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
                        if (sb.length() > 0) {
                            sb.deleteCharAt(sb.length() - 1);
                        }
                    }
                }
            }

            private void beforeInputFirstChar() {
                mMessageLabel.setText("6位数字密码，用于交易验证");
                mPasswordField.setCurrentNumCount(0);
            }

            private void afterInputFullPassword() {
                mHasSetPassword = true;
                mMessageLabel.setText("再输入一次密码");
                mPasswordField.setCurrentNumCount(0);
            }

            private void afterInputFullConfirmPassword() {
                String password = mPassword.toString();
                String confirmPassword = mConfirmPassword.toString();
                if (password.equals(confirmPassword)) {
                    mMessageLabel.setText("设置成功");
                    performSetTransactionPassword(SetTransactionPasswordDialog.this, password);
                } else {
                    mHasSetPassword = false;
                    mMessageLabel.setText("两次密码不一致，请重新输入");
                    mPassword.setLength(0);
                    mConfirmPassword.setLength(0);
                }
            }
        });
    }

    private void performSetTransactionPassword(Dialog dialog, String password) {
        dialog.dismiss();
        Context context = dialog.getContext();
        GMFProgressDialog progressView = new GMFProgressDialog(context);
        progressView.setMessage("正在设置,请稍后");
        progressView.show();
        requestSetTransactionPassword(password)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(data -> progressView.dismiss())
                .subscribe(data -> {
                    if (isSuccess(data)) {
                        MineManager.getInstance().getmMe().setTraderPassword = true;
                        if (mSuccessSetTransactionBlock != null) {
                            mSuccessSetTransactionBlock.call();
                            mSuccessSetTransactionBlock = null;
                        }

                        UmengUtil.stat_set_transaction_pwd_event(context, true, Optional.of(data.errCode), Optional.of(data.msg));
                    } else {
                        UIControllerExtension.createAlertDialog(context, getErrorMessage(data)).show();
                    }
                });
    }

    private static Observable<MResults.MResultsInfo<Void>> requestSetTransactionPassword(String password) {
        return Observable.create(sub -> MineManager.getInstance().setTradePassword(password, MResultExtension.createObservableMResult(sub)));
    }
}
