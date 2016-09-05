package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.manager.cashier.BankCard;
import com.goldmf.GMFund.manager.cashier.BankInfo;
import com.goldmf.GMFund.manager.cashier.CashierManager;
import com.goldmf.GMFund.manager.fortune.FortuneManager;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_LIGHT_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.init;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.util.FormatUtil.formatBigNumber;

/**
 * Created by yale on 15/12/28.
 */
public class BindCNCardInfoCell extends RelativeLayout {
    public static final int ACTION_TYPE_WITHDRAW_FROM_BANK = 0;
    public static final int ACTION_TYPE_WITHDRAW_FROM_SINA = 1;


    private SimpleDraweeView mIconImage;
    private TextView mTitleLabel;
    private TextView mLimitLabel;
    private int mActionType = ACTION_TYPE_WITHDRAW_FROM_BANK;

    public BindCNCardInfoCell(Context context) {
        this(context, null);
    }

    public BindCNCardInfoCell(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BindCNCardInfoCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (isInEditMode()) {
            init(context);
        }

        if (attrs != null) {
            TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.BindCNCardInfoCell, defStyleAttr, 0);

            mActionType = arr.getInt(R.styleable.BindCNCardInfoCell_action_type, mActionType);

            arr.recycle();
        }

        setBackgroundColor(STATUS_BAR_BLACK);

        {
            GenericDraweeHierarchy hierarchy = GenericDraweeHierarchyBuilder.newInstance(getResources())
                    .setPlaceholderImage(getResources().getDrawable(R.mipmap.ic_bank_placeholder))
                    .build();
            SimpleDraweeView draweeView = new SimpleDraweeView(context, hierarchy);
            draweeView.setId(R.id.icon1);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dp2px(this, 24), dp2px(this, 24));
            params.addRule(CENTER_VERTICAL);
            params.leftMargin = dp2px(this, 20);
            addView(draweeView, params);
            mIconImage = draweeView;
        }

        {
            TextView titleLabel = new TextView(context);
            titleLabel.setTextSize(14);
            titleLabel.setTextColor(TEXT_WHITE_COLOR);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.topMargin = dp2px(this, 12);
            params.leftMargin = dp2px(this, 12);
            params.addRule(RIGHT_OF, R.id.icon1);
            addView(titleLabel, params);
            mTitleLabel = titleLabel;
        }

        {
            TextView limitLabel = new TextView(context);
            limitLabel.setTextSize(12);
            limitLabel.setTextColor(TEXT_GREY_LIGHT_COLOR);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
            params.addRule(RIGHT_OF, R.id.icon1);
            params.addRule(ALIGN_PARENT_BOTTOM);
            params.bottomMargin = dp2px(this, 12);
            params.leftMargin = dp2px(this, 12);
            addView(limitLabel, params);
            mLimitLabel = limitLabel;
        }

        updateContent();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(dp2px(this, 60), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void updateContent() {
        if (mActionType == ACTION_TYPE_WITHDRAW_FROM_BANK) {
            if (isInEditMode()) {
                mTitleLabel.setText("招商银行 尾号8888");
                mLimitLabel.setText("单笔限额 5千 单日限额 10万 今日可用 10万");
            } else {
                BankCard card = CashierManager.getInstance().getCard();
                double todayDeposit = FortuneManager.getInstance().cnAccount != null ? FortuneManager.getInstance().cnAccount.todayDeposit : 0;
                if (card == null || card.bank == null) {
                    mTitleLabel.setText("这里应该有银行信息");
                    mLimitLabel.setText("这里应该有限额信息");
                } else {
                    v_setImageUri(mIconImage, card.bank.bankIcon);
                    mTitleLabel.setText(card.bank.bankName + " 尾号" + card.cardMsg);
                    mLimitLabel.setText("单笔限额 " + formatBigNumber(card.bank.limit, false, 0, 2) + " 单日限额 " + formatBigNumber(card.bank.dayLimit, false, 0, 2) + " 今日可用 " + formatBigNumber(Math.max(card.bank.dayLimit - todayDeposit, 0), false, 0, 2));
                }
            }
        } else if (mActionType == ACTION_TYPE_WITHDRAW_FROM_SINA) {
            if (isInEditMode()) {
                mTitleLabel.setText("招商银行 尾号8888");
                mLimitLabel.setText("限额 单笔5万 单日50万");
            } else {
                BankCard card = CashierManager.getInstance().getCard();
                if (card == null || card.bank == null) {
                    mTitleLabel.setText("这里应该有银行信息");
                    mLimitLabel.setText("这里应该有限额信息");
                } else {
                    v_setImageUri(mIconImage, card.bank.bankIcon);
                    mTitleLabel.setText(card.bank.bankName + " 尾号" + card.cardMsg);
                    mLimitLabel.setText("限额 单笔" + formatBigNumber(card.withdrawSingleLimit, false, 0, 2) + " 单日" + formatBigNumber(card.withdrawDayLimit, false, 0, 2) );
                }
            }
        }
    }


    public void updateContent(String encryptedCardNo, BankInfo card) {
        if (mActionType == ACTION_TYPE_WITHDRAW_FROM_BANK) {
            if (isInEditMode()) {
                mTitleLabel.setText("招商银行 尾号8375");
                mLimitLabel.setText("单笔限额 5千 单日限额 10万 今日可用 10万");
            } else {
                if (card == null) {
                    mTitleLabel.setText("这里应该有银行信息");
                    mLimitLabel.setText("这里应该有限额信息");
                } else {
                    v_setImageUri(mIconImage, card.bankIcon);
                    mTitleLabel.setText(card.bankName + " 尾号" + encryptedCardNo.substring(Math.max(encryptedCardNo.length() - 4, 0)));
                    mLimitLabel.setText("单笔限额 " + formatBigNumber(card.limit, false, 0, 2) + " 单日限额 " + formatBigNumber(card.dayLimit, false, 0, 2) + " 今日可用 " + formatBigNumber(Math.max(card.dayLimit, 0), false, 0, 2));
                }
            }
        } else if (mActionType == ACTION_TYPE_WITHDRAW_FROM_SINA) {
            if (isInEditMode()) {
                mTitleLabel.setText("招商银行 尾号8888");
                mLimitLabel.setText("限额 单笔5万 单日50万");
            } else {
                if (card == null) {
                    mTitleLabel.setText("这里应该有银行信息");
                    mLimitLabel.setText("这里应该有限额信息");
                } else {
                    v_setImageUri(mIconImage, card.bankIcon);
                    mTitleLabel.setText(card.bankName + " 尾号" + encryptedCardNo);
                    mLimitLabel.setText("限额 单笔5万 单日50万");
                }
            }
        }
    }

}
