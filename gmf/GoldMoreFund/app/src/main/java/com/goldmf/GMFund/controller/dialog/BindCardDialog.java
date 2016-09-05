package com.goldmf.GMFund.controller.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;

import com.goldmf.GMFund.R;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;

/**
 * Created by Evan on 16/8/13 下午3:48.
 */
public class BindCardDialog extends BasicDialog {


    private Context mContext;

    public BindCardDialog(Context context) {
        super(context, R.style.GMFDialog_FullScreen);
        mContext = context;
        getWindow().setLayout(-1, -2);
        setContentView(R.layout.dialog_bind_card);
        setCancelable(false);
        updateContent();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            dismiss();
            if (mContext instanceof Activity)
                ((Activity) mContext).finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateContent() {
        v_setText(this, R.id.label_title, concat(setColor("每一个注册账户，实名认证后只能绑定一张名下安全银行卡，绑定后进行的充值和提现等操作，只能通过这一张银行卡来完成。", TEXT_BLACK_COLOR),
                "",
                "同卡进出安全策略能够在最大程度保障您的资金安全，因为您的所有资金只能提现到充值银行卡，即使发生手机丢失，或银行卡账号泄露等情况，资金也不能被提取到其他银行卡上，这样不法分子就无法盗取您的资金。因此，从安全性上，做了充分的保障，这也是操盘侠选择资金同卡进出安全策略的原因。",
                "",
                "如需更换卡片或其他相关功能帮助，请联系客服。"));
        v_setClick(this, R.id.btn_bind, this::performBindCard);
    }

    private void performBindCard() {
        dismiss();
    }
}
