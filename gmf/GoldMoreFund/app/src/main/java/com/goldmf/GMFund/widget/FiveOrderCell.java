package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.SpannableStringExtension;
import com.goldmf.GMFund.util.FormatUtil;

import yale.extension.common.Optional;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.*;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatStockCount;

/**
 * Created by yale on 16/2/23.
 */
public class FiveOrderCell extends RelativeLayout {
    private TextView priceLabel;
    private TextView countLabel;

    public FiveOrderCell(Context context) {
        this(context, null);
    }

    public FiveOrderCell(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FiveOrderCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FiveOrderCell, defStyleAttr, 0);
        String title = Optional.of(array.getString(R.styleable.MainTabView_title)).or("No Title");
        array.recycle();

        TextView firstLabel = new TextView(context);
        firstLabel.setTextSize(10);
        firstLabel.setTextColor(TEXT_GREY_COLOR);
        firstLabel.setText(title);
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.addRule(CENTER_VERTICAL);
            firstLabel.setLayoutParams(params);
        }
        addView(firstLabel);

        priceLabel = new TextView(context);
        priceLabel.setTextSize(10);
        priceLabel.setTextColor(TEXT_GREY_COLOR);
        priceLabel.setText("17.000");
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.addRule(CENTER_VERTICAL);
            params.leftMargin = dp2px(this, 25);
            priceLabel.setLayoutParams(params);
        }
        addView(priceLabel);


        countLabel = new TextView(context);
        countLabel.setTextSize(10);
        countLabel.setTextColor(TEXT_GREY_COLOR);
        countLabel.setText("65.5K");
        countLabel.setGravity(Gravity.RIGHT);
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.addRule(CENTER_VERTICAL);
            params.addRule(ALIGN_PARENT_RIGHT);
            countLabel.setLayoutParams(params);
        }
        addView(countLabel);

        if (isInEditMode()) {
            init(context);
        }
    }

    public void setPrice(Double price, Double prevClosePrice) {
        if (price != null && prevClosePrice != null) {
            priceLabel.setText(setColor(formatMoney(price, false, 2), getFiveOrderPriceColor(price, prevClosePrice)));
        } else {
            priceLabel.setText("");
        }
    }

    public void setCount(Long count) {
        if (count == null) {
            countLabel.setText("");
        } else {
            countLabel.setText(formatStockCount(count, 0, 2, true));
        }
    }
}
