package com.goldmf.GMFund.widget.keyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.SpannableStringExtension;
import com.goldmf.GMFund.util.FormatUtil;
import com.goldmf.GMFund.widget.TransactionSeekBar;

import java.math.BigDecimal;

/**
 * Created by yale on 15/8/1.
 */
public class TransactionPad extends BasePad {
    public static final int ACTION_CLEAR = 80002;
    public static final int ACTION_BUY = 80003;
    public static final int ACTION_SELL = 80004;
    private static final int FACTOR = 10000;

    private View.OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mPurchaseButton)
                onPurchaseButtonClick();
            else if (v == mSellButton)
                onSellButtonClick();
            else if (v == mSwitchLabel)
                onSwitchLabelClick();
            else if (v == mCloseButton)
                onCloseButtonClick();
        }
    };

    private TransactionSeekBar.OnProgressChangedListener mOnProgressChangedListener = new TransactionSeekBar.OnProgressChangedListener() {
        @Override
        public void onProgressChanged(int currentProgress) {
            mInfo.setExpectPercent((double) currentProgress / FACTOR);
            mExpectPercentLabel.setText(mInfo.getExpectPercentText());
            mDeltaPercentLabel.setText(mInfo.getDeltaPercentText());
            mOnValueChangedListener.onValueChanged(mInfo.getExpectHand());
        }
    };

    private CustomKeyboardView mKeyboardView;
    private TransactionSeekBar mSeekBar;
    private View mNumberSection;
    private View mPercentSection;
    private View mPurchaseButton;
    private View mSellButton;
    private View mSwitchLabel;
    private View mCloseButton;
    private TextView mMinPercentLabel;
    private TextView mMaxPercentLabel;
    private TextView mCurrentPercentLabel;
    private TextView mExpectPercentLabel;
    private TextView mDeltaPercentLabel;

    private OnValueChangedListener mOnValueChangedListener = OnValueChangedListener.NULL;
    private OnButtonClickListener mOnButtonClickListener = OnButtonClickListener.NULL;
    private TransactionInfo mInfo = new TransactionInfo();

    public TransactionPad(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransactionPad(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.transaction_pad, this, true);

        mNumberSection = findViewById(R.id.section_number);
        mPercentSection = findViewById(R.id.section_percent);

        mPurchaseButton = mNumberSection.findViewById(R.id.btn_purchase);
        mPurchaseButton.setOnClickListener(mOnClickListener);
        mSellButton = mNumberSection.findViewById(R.id.btn_sell);
        mSellButton.setOnClickListener(mOnClickListener);
        mSwitchLabel = findViewById(R.id.label_switch);
        mSwitchLabel.setOnClickListener(mOnClickListener);
        mCloseButton = findViewById(R.id.btn_close);
        mCloseButton.setOnClickListener(mOnClickListener);
        mMinPercentLabel = (TextView) mPercentSection.findViewById(R.id.label_min_percent);
        mMaxPercentLabel = (TextView) mPercentSection.findViewById(R.id.label_max_percent);
        mCurrentPercentLabel = (TextView) mPercentSection.findViewById(R.id.label_current_percent);
        mExpectPercentLabel = (TextView) mPercentSection.findViewById(R.id.label_expect_percent);
        mDeltaPercentLabel = (TextView) mPercentSection.findViewById(R.id.label_delta_percent);

        mSeekBar = (TransactionSeekBar) mPercentSection.findViewById(R.id.seek_bar);
        mSeekBar.setOnProgressChangedListener(mOnProgressChangedListener);

        mKeyboardView = (CustomKeyboardView) mNumberSection.findViewById(R.id.keyboard);
        mKeyboardView.setAnotherKeyLabels(new String[]{"C", "Del"});
        mKeyboardView.setKeyboard(new Keyboard(getContext(), R.xml.qwerty_number));
        mKeyboardView.setOnKeyboardActionListener(createKeyboardActionListener());

        makePurchaseButtonSelected();

        mInfo.init(1000, 1000, 3288, 100);
    }

    public void setOnValueChangedListener(OnValueChangedListener listener) {
        mOnValueChangedListener = listener == null ? OnValueChangedListener.NULL : listener;
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        mOnButtonClickListener = listener == null ? OnButtonClickListener.NULL : listener;
    }

    public void initTransactionInfo(long originalHand, long currentHand, long maxHand, double handPerPercent) {
        mInfo.init(originalHand, currentHand, maxHand, handPerPercent);

        mCurrentPercentLabel.setText(mInfo.getCurrentPercentText());
        mDeltaPercentLabel.setText(mInfo.getDeltaPercentText());
        mExpectPercentLabel.setText(mInfo.getExpectPercentText());
        mSeekBar.setMaxProgress((int) (mInfo.maxPercent * FACTOR));
        mSeekBar.setCurrentProgress((int) (mInfo.expectPercent * FACTOR));
        mSeekBar.setOriginalProgress((int) (mInfo.currentPercent * FACTOR));
        mMinPercentLabel.setText(mInfo.getMinPercent());
        mMaxPercentLabel.setText(mInfo.getMaxPercent());
    }

    public void updateExpectHand(long currentHand) {
        mInfo.setExpectPercentWithHand(currentHand);
        mDeltaPercentLabel.setText(mInfo.getDeltaPercentText());
        mExpectPercentLabel.setText(mInfo.getExpectPercentText());
        mSeekBar.setCurrentProgress((int) (mInfo.expectPercent * 100));
    }

    public boolean isPurchaseButtonSelected() {
        return mPurchaseButton.isSelected();
    }

    public boolean isSellButtonSelected() {
        return mSellButton.isSelected();
    }

    protected void onSwitchLabelClick() {
        if (mPercentSection.isShown()) {
            mPercentSection.setVisibility(View.GONE);
            mNumberSection.setVisibility(View.VISIBLE);
        } else {
            mPercentSection.setVisibility(View.VISIBLE);
            mNumberSection.setVisibility(View.GONE);
        }
    }

    protected void onCloseButtonClick() {
        mOnButtonClickListener.onCloseButtonClick(this);
    }

    protected void onPurchaseButtonClick() {
        makePurchaseButtonSelected();
        this.mPadActionListener.onActionTrigger(ACTION_BUY);
    }

    protected void onSellButtonClick() {
        makeSellButtonSelected();
        this.mPadActionListener.onActionTrigger(ACTION_SELL);
    }


    private void makePurchaseButtonSelected() {
        mPurchaseButton.setSelected(true);
        mSellButton.setSelected(false);
    }

    private void makeSellButtonSelected() {
        mPurchaseButton.setSelected(false);
        mSellButton.setSelected(true);
    }

    public static class TransactionInfo {
        private double currentPercent;
        private double expectPercent;
        private double maxPercent;
        private double handPerPercent;

        private TransactionInfo() {
            this.currentPercent = 0.00f;
            this.expectPercent = 0.0f;
            this.maxPercent = 100.0f;
            this.handPerPercent = 10.0f;
        }

        private void init(long originalHand, long currentHand, long maxHand, double handPerPercent) {
            this.handPerPercent = handPerPercent;
            this.currentPercent = (double) originalHand / handPerPercent;
            this.expectPercent = (double) currentHand / handPerPercent;
            this.maxPercent = (double) maxHand / handPerPercent;
        }

        private int getExpectHand() {
            return new BigDecimal(expectPercent * handPerPercent).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        }

        private void setExpectPercent(double expectPercent) {
            this.expectPercent = expectPercent;
        }

        private void setExpectPercentWithHand(long expectHand) {
            this.expectPercent = (double) expectHand / handPerPercent;
        }


        private CharSequence getMinPercent() {
            return newFormatPercentCharSequence(0);
        }

        private CharSequence getMaxPercent() {
            return newFormatPercentCharSequence(maxPercent);
        }

        private CharSequence getCurrentPercentText() {
            return newFormatPercentCharSequence(currentPercent);
        }

        private CharSequence getExpectPercentText() {
            if (expectPercent < 0 || expectPercent > maxPercent)
                return newErrorCharSequence("Error");

            return newFormatPercentCharSequence(expectPercent);
        }

        private CharSequence getDeltaPercentText() {
            if (expectPercent < 0 || expectPercent > maxPercent)
                return newErrorCharSequence("Error");

            return newFormatPercentCharSequence(expectPercent - currentPercent);
        }

        private CharSequence newFormatPercentCharSequence(double percent) {
            return FormatUtil.formatRatio(percent, false, 2);
        }

        private CharSequence newErrorCharSequence(String text) {
            SpannableString ss = new SpannableString(text);
            SpannableStringExtension.setColor(ss, 0xFFE74C3C);
            return ss;
        }
    }

    public interface OnValueChangedListener {
        void onValueChanged(int currentHand);

        OnValueChangedListener NULL = new OnValueChangedListener() {
            @Override
            public void onValueChanged(int expectHand) {
            }
        };
    }

    public interface OnButtonClickListener {
        void onCloseButtonClick(TransactionPad pad);

        OnButtonClickListener NULL = new OnButtonClickListener() {
            @Override
            public void onCloseButtonClick(TransactionPad pad) {
            }
        };
    }
}
