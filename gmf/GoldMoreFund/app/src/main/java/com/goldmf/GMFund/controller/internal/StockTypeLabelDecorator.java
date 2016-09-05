package com.goldmf.GMFund.controller.internal;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.view.ViewGroup;
import android.widget.TextView;

import com.goldmf.GMFund.model.FundBrief;

import yale.extension.common.shape.RoundCornerShape;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.*;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static yale.extension.common.PreCondition.checkNotNull;

/**
 * Created by yale on 15/10/7.
 */
public class StockTypeLabelDecorator {
    private StockTypeLabelDecorator() {
    }

    public static void decorate(Context ctx, TextView label, int moneyType) {
        checkNotNull(ctx, label, moneyType);

        final int radius = dp2px(2);
        final int borderWidth = dp2px(1);

        label.setSingleLine();
        label.setTextSize(10);
        label.setPadding(dp2px(3), dp2px(1), dp2px(3), dp2px(1));

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)label.getLayoutParams();
        params.bottomMargin =  dp2px(1.5f);
        label.setLayoutParams(params);

        label.setText(FundBrief.Money_Type.getStockType(moneyType));
        if (moneyType == FundBrief.Money_Type.CN) {
            label.setTextColor(TEXT_RED_COLOR);
            label.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0, radius).border(RED_COLOR, borderWidth)));
        } else if (moneyType == FundBrief.Money_Type.HK) {
            label.setTextColor(BLUE_COLOR);
            label.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0, radius).border(BLUE_COLOR, borderWidth)));
        } else if (moneyType == FundBrief.Money_Type.US) {
            label.setTextColor(BLUE_COLOR);
            label.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0, radius).border(BLUE_COLOR, borderWidth)));
        }
    }
}
