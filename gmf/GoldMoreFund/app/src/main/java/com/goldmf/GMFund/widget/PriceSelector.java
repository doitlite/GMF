package com.goldmf.GMFund.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.util.FormatUtil;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;

/**
 * Created by yale on 15/8/17.
 */
public class PriceSelector extends LinearLayout {

    private View.OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mMinusButton)
                onMinusButtonClick();
            else if (v == mPlusButton)
                onPlusButtonClick();
        }
    };

    private ImageView mMinusButton;
    private ImageView mPlusButton;
    private TextView mValueLabel;
    private OnPriceChangedListener mOnPriceChangedListener = OnPriceChangedListener.NULL;

    private double mCurrentValue = 18.65f;
    private double mStepValue = 0.1f;

    public PriceSelector(Context context) {
        this(context, null);
    }

    public PriceSelector(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PriceSelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);

        {
            mMinusButton = new ImageView(context);
            mMinusButton.setImageResource(R.mipmap.ic_minus);
            v_setClick(mMinusButton, mOnClickListener);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;

            addView(mMinusButton, params);
        }

        {
            mValueLabel = new TextView(context);
            mValueLabel.setText(formatValue(mCurrentValue));
            mValueLabel.setTextSize(14);
            mValueLabel.setTextColor(getResources().getColor(R.color.gmf_text_black));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;
            params.leftMargin = dp2px(this, 16);
            params.rightMargin = dp2px(this, 16);

            addView(mValueLabel, params);
        }

        {
            mPlusButton = new ImageView(context);
            mPlusButton.setImageResource(R.mipmap.ic_plus);
            v_setClick(mPlusButton, mOnClickListener);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;

            addView(mPlusButton, params);
        }
    }

    public void setOnPriceChangedListener(OnPriceChangedListener listener) {
        mOnPriceChangedListener = listener == null ? OnPriceChangedListener.NULL : listener;
    }

    public double getCurrentPrice() {
        return mCurrentValue;
    }

    public void setCurrentPrice(double price) {
        mCurrentValue = Math.max(0, price);
        mValueLabel.setText(formatValue(mCurrentValue));
    }

    protected void onPlusButtonClick() {
        mCurrentValue += mStepValue;
        mValueLabel.setText(formatValue(mCurrentValue));
        mOnPriceChangedListener.onPriceChanged(mCurrentValue);
    }

    protected void onMinusButtonClick() {
        mCurrentValue -= mStepValue;
        mCurrentValue = Math.max(mCurrentValue, 0);
        mValueLabel.setText(formatValue(mCurrentValue));
        mOnPriceChangedListener.onPriceChanged(mCurrentValue);
    }

    private static CharSequence formatValue(double value) {
        return FormatUtil.formatMoney(value, false, 2);
    }

    public interface OnPriceChangedListener {
        void onPriceChanged(double newPrice);

        OnPriceChangedListener NULL = new OnPriceChangedListener() {
            @Override
            public void onPriceChanged(double newPrice) {
            }
        };
    }
}
