package com.goldmf.GMFund.widget;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.orhanobut.logger.Logger;

import rx.functions.Action1;
import yale.extension.common.Optional;
import yale.extension.common.RangeD;
import yale.extension.common.shape.RoundCornerShape;
import yale.extension.system.SimpleOnTouchEventHandler;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.*;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BLUE_COLOR;
import static com.goldmf.GMFund.extension.ObjectExtension.*;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_addTextChangedListener;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;

/**
 * Created by yale on 16/2/23.
 */
public class PriceControlView extends RelativeLayout {

    public static final int THEME_BLUE = 0;
    public static final int THEME_ORANGE = 1;

    private TextView mPlusButton;
    private TextView mMinusButton;
    private TextView mPriceLabel;

    private RangeD mPriceRange = new RangeD(Double.MIN_VALUE, Double.MAX_VALUE);
    private Double mCurrentPrice;
    public Optional<Action1<Double>> mOnPriceChangedListener = Optional.empty();

    public PriceControlView(Context context) {
        this(context, null);
    }

    public PriceControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("deprecation")
    public PriceControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0, dp2px(this, 4)).border(0xFFE2E2E2, dp2px(this, 1))));

        if (isInEditMode()) {
            init(context);
        }

        mPlusButton = new TextView(context);
        mPlusButton.setId(R.id.text1);
        mPlusButton.setText("+");
        mPlusButton.setTextSize(30);
        mPlusButton.setTextColor(0xFFFFFFFF);
        mPlusButton.setGravity(Gravity.CENTER);
        mPlusButton.setBackgroundColor(BLUE_COLOR);
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dp2px(this, 41), dp2px(this, 41));
            params.addRule(CENTER_VERTICAL);
            params.addRule(ALIGN_PARENT_RIGHT);
            mPlusButton.setLayoutParams(params);
            mPlusButton.setOnTouchListener(new OnTouchListener() {
                private SimpleOnTouchEventHandler mEventHandler = new SimpleOnTouchEventHandler(context) {
                    @Override
                    public void onPress(MotionEvent event) {
                        super.onPress(event);
                        if (mCurrentPrice != null)
                            setCurrentPrice(mCurrentPrice + 0.01);
                    }

                    @Override
                    public void onRepeat() {
                        super.onRepeat();
                        if (mCurrentPrice != null)
                            setCurrentPrice(mCurrentPrice + 0.01);
                    }
                };

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mPriceLabel.setCursorVisible(false);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            mPriceLabel.setCursorVisible(true);
                            break;

                    }
                    return mEventHandler.onTouchEvent(v, event);
                }
            });
        }
        addView(mPlusButton);

        mMinusButton = new TextView(context);
        mMinusButton.setId(R.id.text2);
        mMinusButton.setText("-");
        mMinusButton.setTextSize(30);
        mMinusButton.setTextColor(0xFFFFFFFF);
        mMinusButton.setBackgroundColor(BLUE_COLOR);
        mMinusButton.setGravity(Gravity.CENTER);
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dp2px(this, 41), dp2px(this, 41));
            params.addRule(CENTER_VERTICAL);
            mMinusButton.setLayoutParams(params);
            mMinusButton.setOnTouchListener(new OnTouchListener() {
                private SimpleOnTouchEventHandler mEventHandler = new SimpleOnTouchEventHandler(context) {
                    @Override
                    public void onPress(MotionEvent event) {
                        super.onPress(event);
                        if (mCurrentPrice != null) {
                            setCurrentPrice(mCurrentPrice - 0.01);
                        }
                    }

                    @Override
                    public void onRepeat() {
                        super.onRepeat();
                        if (mCurrentPrice != null) {
                            setCurrentPrice(mCurrentPrice - 0.01);
                        }
                    }
                };

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mPriceLabel.setCursorVisible(false);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            mPriceLabel.setCursorVisible(true);
                            break;

                    }
                    return mEventHandler.onTouchEvent(v, event);
                }
            });
        }
        addView(mMinusButton);

        mPriceLabel = new EditText(context);
        mPriceLabel.setTextSize(15);
        mPriceLabel.setTextColor(0xFF000000);
        mPriceLabel.setGravity(Gravity.CENTER);
        mPriceLabel.setHint("价格");
        mPriceLabel.setBackgroundResource(0);

        mPriceLabel.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                refreshCurrentPrice();
            }
        });

        v_addTextChangedListener(mPriceLabel, editable -> {
        });
        {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
            params.addRule(CENTER_VERTICAL);
            params.addRule(ALIGN_PARENT_BOTTOM);
            params.addRule(RIGHT_OF, mMinusButton.getId());
            params.addRule(LEFT_OF, mPlusButton.getId());
            mPriceLabel.setLayoutParams(params);
        }
        addView(mPriceLabel);
    }

    public void updateTheme(int theme) {
        int primaryColor = BLUE_COLOR;
        if (theme == THEME_ORANGE) {
            primaryColor = ORANGE_COLOR;
        }

        setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0, dp2px(this, 4)).border(primaryColor, dp2px(this, 1))));

        mPlusButton.setBackgroundColor(primaryColor);
        mMinusButton.setBackgroundColor(primaryColor);
    }

    public void setCurrentPrice(Double price) {
        final Double revisePrice = price != null ? Math.min(Math.max(price, mPriceRange.min), mPriceRange.max) : null;

        if (mCurrentPrice == null || !mCurrentPrice.equals(revisePrice)) {
            mCurrentPrice = revisePrice;
        }

        if (mCurrentPrice == null) {
            mPriceLabel.setText("");
        } else {
            mPriceLabel.setText("");
            mPriceLabel.append(formatMoney(revisePrice, false, 2));
            mOnPriceChangedListener.apply(it -> it.call(revisePrice));
        }
    }

    public void refreshCurrentPrice() {
        Double price = safeGet(() -> Double.valueOf(mPriceLabel.getText().toString()), null);
        if (price == null) {
            setCurrentPrice(mCurrentPrice);
        } else {
            setCurrentPrice(price);
        }
    }

    public void setPriceLabelOnFocusChangeListener(OnFocusChangeListener listener) {
        mPriceLabel.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                refreshCurrentPrice();
                safeCall(() -> listener.onFocusChange(v, hasFocus));
            }
        });
    }

    public boolean isPriceLabelGainFocus() {
        return mPriceLabel.hasFocus();
    }

    public void setPriceRange(RangeD range) {
        mPriceRange = range != null ? range : new RangeD(Double.MIN_VALUE, Double.MAX_VALUE);
    }

    public void setOnPriceChangedListener(Action1<Double> onPriceChangedListener) {
        mOnPriceChangedListener = opt(onPriceChangedListener);
    }

    public Double getCurrentPrice() {
        return mCurrentPrice;
    }
}
