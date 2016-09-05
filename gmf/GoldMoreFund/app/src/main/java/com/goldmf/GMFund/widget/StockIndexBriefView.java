package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.internal.SignalColorHolder;

import yale.extension.common.shape.RoundCornerShape;

import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatNumber;
import static com.goldmf.GMFund.util.FormatUtil.formatRatio;

/**
 * Created by yale on 16/2/20.
 */
public class StockIndexBriefView extends FrameLayout {
    private CharSequence mIndexName;
    private Double mCurrentPrice;
    private Double mChangePrice;
    private Double mChangePriceRatio;

    private TextView mTitleLabel;
    private TextView mPriceLabel;
    private TextView mChangePriceLabel;
    private TextView mChangePriceRatioLabel;
    private int mTextColor;

    public StockIndexBriefView(Context context) {
        this(context, null);
    }

    public StockIndexBriefView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StockIndexBriefView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final Resources res = getResources();

        StateListDrawable bg = new StateListDrawable();
        bg.addState(PRESSED_ENABLED_STATE_SET, new ShapeDrawable(new RoundCornerShape(res.getColor(R.color.cell_bg_default_pressed),dp2px(this,4))));
        bg.addState(ENABLED_STATE_SET,new ShapeDrawable(new RoundCornerShape(res.getColor(R.color.cell_bg_default_normal),dp2px(this,4))));
        setBackgroundDrawable(bg);

        if (isInEditMode()) {
            SignalColorHolder.init(context);
        }

        mPriceLabel = new TextView(context);
        mPriceLabel.setTextSize(20);
        mPriceLabel.setTextColor(res.getColor(R.color.gmf_text_white));
        {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, -2);
            params.topMargin = dp2px(this, 10);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            mPriceLabel.setLayoutParams(params);
        }
        addView(mPriceLabel);

        LinearLayout priceContainer = new LinearLayout(context);
        priceContainer.setOrientation(LinearLayout.HORIZONTAL);
        {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, -2);
            params.topMargin = dp2px(this, 34);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            priceContainer.setLayoutParams(params);
        }
        addView(priceContainer);

        mChangePriceLabel = new TextView(context);
        mChangePriceLabel.setTextSize(10);
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
            params.gravity = Gravity.CENTER_VERTICAL;
            mChangePriceLabel.setLayoutParams(params);
        }
        priceContainer.addView(mChangePriceLabel);

        mChangePriceRatioLabel = new TextView(context);
        mChangePriceRatioLabel.setTextSize(10);
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
            params.gravity = Gravity.CENTER_VERTICAL;
            params.leftMargin = dp2px(this, 4);
            mChangePriceRatioLabel.setLayoutParams(params);
        }
        priceContainer.addView(mChangePriceRatioLabel);

        mTitleLabel = new TextView(context);
        mTitleLabel.setTextSize(12);
        mTitleLabel.setTextColor(res.getColor(R.color.gmf_text_grey));
        {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, -2);
            params.topMargin = dp2px(this, 48);
            params.bottomMargin = dp2px(this, 6);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            mTitleLabel.setLayoutParams(params);
        }
        addView(mTitleLabel);


        setIndexName("上证指数");
        setCurrentPrice(2749.79);
        setChangePrice(-188.73);
        setChangePriceRatio(-0.0642);
        setTextColor(res.getColor(R.color.gmf_text_grey));

        updateView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(dp2px(this, 70), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void updateView() {
        onUpdateView();
    }

    private void onUpdateView() {

        mTitleLabel.setText(mIndexName);
        mPriceLabel.setText(formatNumber(mCurrentPrice, false, 2));
        mPriceLabel.setTextColor(mTextColor);
        mChangePriceLabel.setText(formatMoney(mChangePrice, true, 2));
        mChangePriceLabel.setTextColor(mTextColor);
        mChangePriceRatioLabel.setText(formatRatio(mChangePriceRatio, true, 2));
        mChangePriceRatioLabel.setTextColor(mTextColor);
    }

    public StockIndexBriefView setIndexName(CharSequence indexName) {
        mIndexName = indexName;
        return this;
    }

    public StockIndexBriefView setCurrentPrice(Double price) {
        mCurrentPrice = price;
        return this;
    }

    public StockIndexBriefView setChangePrice(Double price) {
        mChangePrice = price;
        return this;
    }

    public StockIndexBriefView setChangePriceRatio(Double mRatio) {
        mChangePriceRatio = mRatio;
        return this;
    }

    public StockIndexBriefView setTextColor(int color) {
        mTextColor = color;
        return this;
    }
}
