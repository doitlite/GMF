package com.goldmf.GMFund.controller.dialog;

import android.content.Context;
import android.view.Gravity;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.model.CommonDefine.PlaceHolder;
import com.goldmf.GMFund.model.GMFRankUser;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getIncomeTextColor;
import static com.goldmf.GMFund.extension.ObjectExtension.*;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;

/**
 * Created by yale on 16/4/5.
 */
public class StockMatchExchangeDialog extends BasicDialog {
    public StockMatchExchangeDialog(Context context, GMFRankUser rankUser) {
        super(context, R.style.GMFDialog);
        setContentView(R.layout.dialog_match_exchange);
        getWindow().setLayout(-1, -2);
        getWindow().setGravity(Gravity.BOTTOM);

        v_setClick(this, R.id.btn_close, v -> dismiss());

        VM vm = new VM(rankUser);
        v_setText(this, R.id.label_formula, vm.formula);
        v_setText(this, R.id.label_begin_capital, vm.beginCapital);
        v_setText(this, R.id.label_end_capital, vm.endCapital);
        v_setText(this, R.id.label_income_amount, vm.incomeAmount);
        v_setText(this, R.id.label_exchange_amount, vm.exchangeAmount);
    }

    @Override
    public void show() {
        super.show();
        animateToShow(v_findView(this, R.id.rootView), null);
    }

    @Override
    public void dismiss() {
        animateToHide(v_findView(this, R.id.rootView), () -> super.dismiss());
    }

    private static class VM {
        private CharSequence formula;
        private CharSequence beginCapital;
        private CharSequence endCapital;
        private CharSequence incomeAmount;
        private CharSequence exchangeAmount;


        public VM(GMFRankUser user) {
            this.formula = safeGet(() -> user.exchange.desc, PlaceHolder.NULL_VALUE);
            this.beginCapital = safeGet(() -> formatMoney(user.exchange.beginCapital, false, 2), PlaceHolder.NULL_VALUE);
            this.endCapital = safeGet(() -> formatMoney(user.exchange.endCapital, false, 2), PlaceHolder.NULL_VALUE);
            this.incomeAmount = safeGet(() -> setColor(formatMoney(user.exchange.income, false, 2), getIncomeTextColor(user.exchange.income, TEXT_BLACK_COLOR)), PlaceHolder.NULL_VALUE);
            this.exchangeAmount = safeGet(() -> formatMoney(user.exchange.exchange, false, 2), PlaceHolder.NULL_VALUE);
        }
    }
}
