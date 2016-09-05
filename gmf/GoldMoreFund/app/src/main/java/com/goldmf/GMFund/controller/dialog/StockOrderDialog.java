package com.goldmf.GMFund.controller.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.internal.SignalColorHolder;
import com.goldmf.GMFund.util.FormatUtil;
import com.goldmf.GMFund.widget.ProgressButton;

import java.lang.ref.WeakReference;
import java.net.NoRouteToHostException;

import rx.functions.Action1;
import rx.functions.Action2;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.*;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_preDraw;
import static com.goldmf.GMFund.extension.ViewExtension.v_setBackgroundColor;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;

/**
 * Created by yale on 16/2/25.
 */
public class StockOrderDialog extends BasicDialog {
    public static final int STYLE_BUY = 0;
    public static final int STYLE_SELL = 1;
    public static final int STYLE_CANCEL = 2;

    private View mCloseBtn;
    private ProgressButton mConfirmBtn;
    private TextView mErrorLabel;
    private Action2<Dialog, ProgressButton> mDelegate;

    public StockOrderDialog(Context context, int style, Item item) {
        super(context, R.style.GMFDialog);
        setContentView(R.layout.dialog_buy_order_confirm);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setLayout(-1, -2);

        mConfirmBtn = v_findView(this, R.id.btn_confirm);
        mErrorLabel = v_findView(this, R.id.label_error);
        mCloseBtn = v_findView(this, R.id.btn_close);
        if (style == STYLE_BUY) {
            v_setText(this, R.id.label_title, "委托买入确认");
            mConfirmBtn.setText("确认买入", ProgressButton.Mode.Normal);
            mConfirmBtn.setButtonTheme(mConfirmBtn.newButtonTheme(ProgressButton.BUTTON_THEME_BLUE));
//            v_setBackgroundColor(this, R.id.btn_confirm, BLUE_COLOR);
        } else if (style == STYLE_SELL) {
            v_setText(this, R.id.label_title, "委托卖出确认");
            mConfirmBtn.setText("确认卖出", ProgressButton.Mode.Normal);
            mConfirmBtn.setButtonTheme(mConfirmBtn.newButtonTheme(ProgressButton.BUTTON_THEME_ORANGE));
        } else if (style == STYLE_CANCEL) {
            v_setText(this, R.id.label_title, "委托撤单确认");
            mConfirmBtn.setText("确认撤单", ProgressButton.Mode.Normal);
            mConfirmBtn.setButtonTheme(mConfirmBtn.newButtonTheme(ProgressButton.BUTTON_THEME_BLUE));
        }

        v_setClick(mCloseBtn, v -> dismiss());

        v_setText(this, R.id.label_stock_name_and_code, item.stockCode + " " + item.stockName);
        v_setText(this, R.id.label_way, item.way);
        v_setText(this, R.id.label_price, item.price == null ? "--" : FormatUtil.formatMoney(item.price, false, 2));
        v_setText(this, R.id.label_count, item.count == null ? "-- 股" : item.count + "股");

        v_setClick(mConfirmBtn, v -> {
            if (mDelegate != null) mDelegate.call(this, mConfirmBtn);
        });
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

    @Override
    public void setCancelable(boolean flag) {
        super.setCancelable(flag);
        mCloseBtn.setEnabled(flag);
    }

    public void setDelegate(Action2<Dialog, ProgressButton> delegate) {
        mDelegate = delegate == null ? (dialog,btn) -> dialog.dismiss() : delegate;
    }

    public void setErrorText(CharSequence text) {
        mErrorLabel.setText(text);
    }

    public static class Item {
        public String stockName;
        public String stockCode;
        public String way;
        public Double price;
        public Long count;

    }
}
