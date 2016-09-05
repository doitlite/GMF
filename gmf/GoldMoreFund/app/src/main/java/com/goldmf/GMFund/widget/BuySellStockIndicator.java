package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.goldmf.GMFund.R;

import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;

/**
 * Created by yale on 16/2/23.
 */
public class BuySellStockIndicator extends TextView {
    public static final int STATE_BUY = 0;
    public static final int STATE_SELL = 1;

    public BuySellStockIndicator(Context context) {
        this(context, null);
    }

    public BuySellStockIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BuySellStockIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setTextSize(10);
        setLineSpacing(16, 1);
        setTextColor(getResources().getColor(R.color.gmf_text_white));
        setGravity(Gravity.CENTER);

        int state = STATE_BUY;
        TypedArray typed = context.obtainStyledAttributes(attrs, R.styleable.BuySellStockIndicator, defStyleAttr, 0);
        state = typed.getInt(R.styleable.BuySellStockIndicator_gmf_state, state);
        typed.recycle();

        setState(state);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(dp2px(this, 20), MeasureSpec.EXACTLY);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(dp2px(this, 48), MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setState(int state) {
        if (state == STATE_BUY) {
            setBackgroundColor(0xFF4A90E2);
            setText(concat("买", "入"));
        } else if (state == STATE_SELL) {
            setBackgroundColor(0xFFEF9F1D);
            setText(concat("卖", "出"));
        }
    }
}
