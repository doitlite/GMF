package com.goldmf.GMFund.widget;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.internal.SignalColorHolder;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getIncomeTextColor;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;

/**
 * Created by yale on 16/3/24.
 */
public class StockAccountProfileView extends RelativeLayout {
    private ImageView mIconImage;
    private TextView mNameLabel;
    private TextView mIncomeLabel;
    private TextView mHintLabel;

    public StockAccountProfileView(Context context) {
        this(context, null);
    }

    public StockAccountProfileView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StockAccountProfileView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (isInEditMode()) {
            SignalColorHolder.init(context);
        }

        {
            mIconImage = new ImageView(context);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dp2px(this, 32), dp2px(this, 32));
            params.topMargin = dp2px(this, 16);
            params.addRule(CENTER_HORIZONTAL);
            addView(mIconImage, params);
        }

        {
            mNameLabel = new TextView(context);
            mNameLabel.setTextSize(12);
            mNameLabel.setTextColor(TEXT_GREY_COLOR);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.topMargin = dp2px(this, 58);
            params.addRule(CENTER_HORIZONTAL);
            addView(mNameLabel, params);
        }

        {
            mIncomeLabel = new TextView(context);
            mIncomeLabel.setTextSize(16);
            mIncomeLabel.setTextColor(TEXT_BLACK_COLOR);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.topMargin = dp2px(this, 79);
            params.addRule(CENTER_HORIZONTAL);
            addView(mIncomeLabel, params);
        }

        {
            mHintLabel = new TextView(context);
            mHintLabel.setTextSize(10);
            mHintLabel.setTextColor(0xFFCCCCCC);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.topMargin = dp2px(this, 102);
            params.addRule(CENTER_HORIZONTAL);
            addView(mHintLabel, params);
        }


        if (isInEditMode()) {
            setIconResource(R.mipmap.ic_cn_fund);
            setAccountName("沪深账户");
//            setIncomeText(2324.45D, "尚未投资");
            setHintText("预期收益");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(dp2px(this, 132), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setIconResource(@DrawableRes int resId) {
        mIconImage.setImageResource(resId);
    }

    public void setAccountName(@Nullable String name) {
        mNameLabel.setText(opt(name).or("--"));
    }

    public void setIncome(@Nullable Double income) {
            mIncomeLabel.setText(setColor(formatMoney(income, true, 2), getIncomeTextColor(income)));
    }

    public void setIncomeText(@NonNull String defValue) {
            mIncomeLabel.setText(setColor(defValue, TEXT_BLACK_COLOR));
    }

    public void setHintText(@Nullable CharSequence hint) {
        mHintLabel.setText(opt(hint).or(""));
    }
}
