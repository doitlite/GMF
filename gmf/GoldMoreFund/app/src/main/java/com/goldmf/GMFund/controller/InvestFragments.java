package com.goldmf.GMFund.controller;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.StringPair;
import com.goldmf.GMFund.controller.business.CashController;
import com.goldmf.GMFund.controller.business.CouponController;
import com.goldmf.GMFund.controller.business.FundController;
import com.goldmf.GMFund.controller.dialog.ShareDialog.SharePlatform;
import com.goldmf.GMFund.controller.internal.CashUIController;
import com.goldmf.GMFund.controller.internal.SignalColorHolder;
import com.goldmf.GMFund.controller.protocol.UMShareHandlerProtocol;
import com.goldmf.GMFund.extension.StringExtension;
import com.goldmf.GMFund.manager.cashier.BankCard;
import com.goldmf.GMFund.manager.cashier.CashierManager;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.manager.fortune.Coupon;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.model.Fund;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.GlobalVariableDic;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.BasicCell;
import com.goldmf.GMFund.widget.GMFProgressDialog;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import rx.subjects.PublishSubject;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_FUND_ID_INT;
import static com.goldmf.GMFund.controller.CouponFragments.CouponListFragment.FLAG_HAS_SELECTED_POS;
import static com.goldmf.GMFund.controller.CouponFragments.CouponListFragment.FLAG_HIDE_EXCHANGE_BTN;
import static com.goldmf.GMFund.controller.CouponFragments.CouponListFragment.FLAG_HIDE_RULE;
import static com.goldmf.GMFund.controller.CouponFragments.CouponListFragment.FLAG_SET_SELECTABLE;
import static com.goldmf.GMFund.controller.CouponFragments.CouponListFragment.FLAG_USE_BLACK_THEME;
import static com.goldmf.GMFund.controller.CouponFragments.CouponListFragment.OFFSET_SELECTED_COUPON_POS;
import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.FragmentStackActivity.pushFragment;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.KEY_CN_RECHARGE_SUCCESS_MESSAGE;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.KEY_INVEST_TYPE_INT;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.KEY_OPERATION_TYPE_INT;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_CouponListPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_RechargePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.ObjectExtension.apply;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setClickEvent;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setStyle;
import static com.goldmf.GMFund.extension.StringExtension.map;
import static com.goldmf.GMFund.extension.StringExtension.normalMoneyTransformer;
import static com.goldmf.GMFund.extension.UIControllerExtension.createAlertDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_addTextChangedListener;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatRemainingTimeOverMonth;
import static java.lang.Math.max;

/**
 * Created by yale on 15/10/14.
 */
public class InvestFragments {

    private InvestFragments() {
    }

    public static class InvestFundFragment extends SimpleFragment {
        public static PublishSubject<Pair<Integer, String>> sWebInvestSuccessSubject = PublishSubject.create();
        private int mFundId;
        private Fund mFund;
        private double mExpectInvestAmount;

        private EditText mFlexibleAmountField;
        private TextView mFixAmountLabel;
        private Button mInvestButton;
        private TextView mBalanceLabel;
        private BasicCell mCouponCell;
        private TextView mProtocolLabel;
        private boolean mIntentToBindCNCard = false;
        private boolean mIntentToRechargeHKAccount = false;
        private Coupon mSelectedCoupon;

        public InvestFundFragment init(int fundId, Double expectInvestAmount) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(KEY_FUND_ID_INT, fundId);
            if (expectInvestAmount != null) {
                arguments.putDouble(CommonProxyActivity.KEY_AMOUNT_DOUBLE, expectInvestAmount);
            }
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mFundId = getArguments().getInt(KEY_FUND_ID_INT);
            mExpectInvestAmount = safeGet(() -> getArguments().getDouble(CommonProxyActivity.KEY_AMOUNT_DOUBLE), 0D);
            GlobalVariableDic.shareInstance().update(KEY_OPERATION_TYPE_INT, KEY_INVEST_TYPE_INT);
            return inflater.inflate(R.layout.frag_invest_fund, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);

            changeVisibleSection(TYPE_LOADING);
            v_setClick(mReloadSection, () -> fetchData(true));

            CouponController.fetchCouponList().subscribe();

            // bind child views
            mFlexibleAmountField = v_findView(this, R.id.field_amount_flexible);
            mFixAmountLabel = v_findView(this, R.id.label_amount_fix);
            mInvestButton = v_findView(this, R.id.btn_invest);
            mBalanceLabel = v_findView(this, R.id.label_balance);
            mProtocolLabel = v_findView(this, R.id.label_protocol);
            mCouponCell = v_findView(this, R.id.cell_coupon);
            v_setGone(mCouponCell);
            mInvestButton.setEnabled(false);

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }

            UmengUtil.stat_enter_invest_fund_page(getActivity(), Optional.of(this));
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                fetchData(false);
                if (mIntentToBindCNCard) {
                    mIntentToBindCNCard = false;
                    if (CashierManager.getInstance().getCard().status == BankCard.Card_Status_Normal) {
                        performInvest(true);
                    } else {
                        goBack(this);
                    }
                } else if (mIntentToRechargeHKAccount) {
                    mIntentToRechargeHKAccount = false;
                    goBack(this);
                }
            }
        }

        @Override
        protected boolean onInterceptGoBack() {
            UmengUtil.stat_fund_protocol_cancel_event(getActivity(), Optional.of(this));
            return super.onInterceptGoBack();
        }

        private void fetchData(boolean reload) {
            consumeEventMRUpdateUI(FundController.fetchFundInfo(mFundId, true), reload)
                    .setTag("fetch_data")
                    .onNextFinish(response -> {
                        if (isSuccess(response)) {
                            mFund = response.data;
                            updateContent();
                            updateCouponCell();
                            changeVisibleSection(TYPE_CONTENT);
                        } else {
                            v_setText(mReloadSection, R.id.label_title, getErrorMessage(response));
                            changeVisibleSection(TYPE_RELOAD);
                        }
                    })
                    .done();
        }

        private void updateContent() {

            int moneyType = mFund.moneyType;
            if (isInvestAmountFix() || mExpectInvestAmount > 0D) {
                v_setVisible(this, R.id.section_invest_fix);
                v_setGone(this, R.id.section_invest_flexible);
                mInvestButton.setEnabled(true);
                if (mExpectInvestAmount > 0D) {
                    String text = Money_Type.getSymbol(mFund.moneyType) + formatMoney(mExpectInvestAmount, false, 0, 2);
                    mFixAmountLabel.setText(text);
                } else {
                    String text = Money_Type.getSymbol(mFund.moneyType) + formatMoney(mFund.maxInvestLimit, false, 0, 2);
                    mFixAmountLabel.setText(text);
                }
            } else {
                v_setGone(this, R.id.section_invest_fix);
                v_setVisible(this, R.id.section_invest_flexible);
                if (mFund.minInvestLimit <= mFund.maxInvestLimit) {
                    mFlexibleAmountField.setHint("投资最低" + formatMoney(getMinLimitInvestAmount(), false, 0, 2) + Money_Type.getUnit(moneyType) + "，最高" + formatMoney(mFund.maxInvestLimit, false, 0, 2) + Money_Type.getUnit(moneyType));
                } else {
                    mFlexibleAmountField.setHint("投资最低" + formatMoney(getMinLimitInvestAmount(), false, 0, 2) + Money_Type.getUnit(moneyType));
                }
                v_addTextChangedListener(mFlexibleAmountField, editable -> {
                    String normalMoney = StringExtension.map(editable, StringExtension.normalMoneyTransformer());
                    if (!TextUtils.isEmpty(normalMoney) && !TextUtils.isDigitsOnly(normalMoney) && editable.length() > 0) {
                        editable.delete(editable.length() - 1, editable.length());
                        return;
                    }

                    String formatMoney = map(normalMoney, StringExtension.formatMoneyTransformer(false, 0));
                    if (!formatMoney.equalsIgnoreCase(editable.toString())) {
                        mFlexibleAmountField.setText(formatMoney);
                        mFlexibleAmountField.setSelection(formatMoney.length());
                        return;
                    }

                    if (editable.length() == 1 && editable.toString().equalsIgnoreCase("0")) {
                        editable.clear();
                        return;
                    }

                    safeCall(() -> {
                        if (normalMoney.length() > 0) {
                            mInvestButton.setEnabled(Double.parseDouble(normalMoney) >= getMinLimitInvestAmount());
                        } else {
                            mInvestButton.setEnabled(false);
                        }
                    });

                    if (Money_Type.getInstance(mFund.moneyType) == Money_Type.CN || hasEnoughAccountBalance(normalMoney)) {
                        mInvestButton.setText("投资");
                    } else {
                        mInvestButton.setText("余额不足，去充值");
                    }
                    updateCouponCell();
                });


            }
            if (mFund.moneyType == Money_Type.CN) {
                mBalanceLabel.setText("账户余额: " + formatMoney(CashierManager.getInstance().getCnCashBalance(), false, 0, 2) + Money_Type.getUnit(mFund.moneyType));
            } else if (mFund.moneyType == Money_Type.HK) {
                mBalanceLabel.setText("账户余额: " + formatMoney(CashierManager.getInstance().getHkCashBalance(), false, 0, 2) + Money_Type.getUnit(mFund.moneyType));
            }
            consumeEvent(NotificationCenter.selectCouponSubject)
                    .onNextFinish(coupon -> {
                        mSelectedCoupon = coupon;
                        updateCouponCell();
                    })
                    .done();

            mProtocolLabel.setText(concat("投资代表同意", concatNoBreak("<<", setColor(setClickEvent(mFund.agreementName, v -> pushFragment(this, new FundFragments.FundProtocolFragment().init(mFund.index))), 0xFF3498DB), ">>")));
            mProtocolLabel.setMovementMethod(new LinkMovementMethod());

            if (mFund.traderUserOrNull == null) {
                v_setText(this, R.id.label_fund_name_and_other_info, concat(mFund.name, setFontSize("组合期限: T+" + mFund.tradingDay, sp2px(14))));
            } else {
                v_setText(this, R.id.label_fund_name_and_other_info, concat(mFund.name, setFontSize(((mFund.traderUserOrNull.type == User.User_Type.Trader) ? "操盘手: " : ((mFund.traderUserOrNull.type == User.User_Type.Talent) ? "牛人: " : ""))
                        + mFund.traderUserOrNull.getName() + " | 组合期限: T+" + mFund.tradingDay, sp2px(14))));
            }
            v_setClick(mInvestButton, v -> {
                performInvest(true);
                UmengUtil.stat_invest_fund_event(getActivity(), Optional.of(this));
            });
        }

        private void updateCouponCell() {
            Double investAmount = safeGet(() -> Double.valueOf(getInvestAmountFromView()), 0D);
            if (mSelectedCoupon != null && (mSelectedCoupon.validAmount == 0 || investAmount >= mSelectedCoupon.validAmount)) {
                v_setText(mCouponCell.getTitleLabel(), "已选择投资红包");

                String unit = Money_Type.getUnit(mSelectedCoupon.moneyType);
                SpannableStringBuilder couponAmount = setStyle(setColor(formatMoney(mSelectedCoupon.amount, false, 0, 2) + unit, TEXT_RED_COLOR), Typeface.BOLD);
                String couponLimit;
                if (mSelectedCoupon.validAmount <= 0D) {
                    couponLimit = "单次投资任意金额可用";
                } else {
                    couponLimit = String.format(Locale.getDefault(), "单次投资满%s可用", formatMoney(mSelectedCoupon.validAmount, false, 0, 2) + unit);
                }
                CharSequence extraTitle = concat(couponAmount, couponLimit);
                TextView extraTitleLabel = mCouponCell.getExtraTitleLabel();
                extraTitleLabel.setSingleLine(false);
                extraTitleLabel.setLines(2);
                extraTitleLabel.setLineSpacing(dp2px(2), 1.0f);
                extraTitleLabel.setEllipsize(TextUtils.TruncateAt.END);
                v_setText(extraTitleLabel, extraTitle);
                extraTitleLabel.setGravity(Gravity.RIGHT);
                LinkedList<Coupon> availableCoupons = new LinkedList<>(Stream.of(FortuneManager.getInstance().couponList)
                        .filter(it -> it.isValid(investAmount, mFund))
                        .collect(Collectors.toList()));

                v_setClick(mCouponCell, v -> {
                    int flags = FLAG_HIDE_RULE | FLAG_HIDE_EXCHANGE_BTN | FLAG_USE_BLACK_THEME | FLAG_DISABLE_FORCE_SHOW_SWIPE_REFRESH_LAYOUT | FLAG_SET_SELECTABLE;
                    int selectPos = availableCoupons.indexOf(mSelectedCoupon);
                    if (selectPos >= 0) {
                        flags |= (selectPos << OFFSET_SELECTED_COUPON_POS);
                        flags |= FLAG_HAS_SELECTED_POS;
                    }
                    showActivity(this, an_CouponListPage(flags, availableCoupons));
                    UmengUtil.stat_click_event(UmengUtil.eEvENTIDInvestUseCoupons);
                });
            } else {
                mSelectedCoupon = null;
                List<Coupon> coupons = FortuneManager.getInstance().couponList;
                int count = (int) Stream.of(coupons)
                        .filter(it -> it.isValid(investAmount, mFund))
                        .count();
                if (count > 0) {
                    LinkedList<Coupon> availableCoupons = new LinkedList<>(Stream.of(coupons)
                            .filter(it -> it.isValid(investAmount, mFund))
                            .collect(Collectors.toList()));
                    CharSequence title = String.format(Locale.getDefault(), "选择红包（%d个可用红包）", count);
                    v_setText(mCouponCell.getTitleLabel(), title);
                    v_setClick(mCouponCell, v -> {
                        int flags = FLAG_HIDE_RULE | FLAG_HIDE_EXCHANGE_BTN | FLAG_USE_BLACK_THEME | FLAG_DISABLE_FORCE_SHOW_SWIPE_REFRESH_LAYOUT | FLAG_SET_SELECTABLE;
                        showActivity(this, an_CouponListPage(flags, availableCoupons));
                    });
                } else {
                    v_setClick(mCouponCell, v -> {
                    });
                }
                v_setText(mCouponCell.getExtraTitleLabel(), "");
                v_setVisibility(mCouponCell, count > 0 ? View.VISIBLE : View.GONE);
            }
        }


        private void performInvest(boolean refreshAccount) {

            GMFProgressDialog dialog = new GMFProgressDialog(getActivity(), "正在投资");
            dialog.show();

            if (refreshAccount) {
                consumeEventMR(CashController.refreshAccount(false))
                        .setTag("perform_refresh_account")
                        .onNextStart(response -> {
                            dialog.dismiss();
                        })
                        .onNextSuccess(response -> {
                            performInvest(false);
                        })
                        .onNextFail(response -> {
                            createAlertDialog(this, getErrorMessage(response));
                        })
                        .done();
                return;
            }

            String normalAmount = getInvestAmountFromView();
            double investAmount = Double.valueOf(normalAmount);

            if (anyMatch(mFund.innerType, FundBrief.Fund_Type.Bonus) && mFund.raisedCapital - mFund.targetCapital >= 0) {
                dialog.dismiss();
                createAlertDialog(this, "不可投资", "当前组合已全部投资完", "知道了").show();
                return;
            }

            double minLimitAmount = getMinLimitInvestAmount();
            double maxLimitAmount = getMaxLimitInvestAmount();
            if (minLimitAmount <= maxLimitAmount) {
                if (investAmount > maxLimitAmount) {
                    dialog.dismiss();
                    String message = String.format("总投资%s,超过组合最大投资金额", normalAmount);
                    createAlertDialog(this, message).show();
                    return;
                }
            }

            BankCard card = mFund.moneyType == Money_Type.CN ? CashierManager.getInstance().getCard() : CashierManager.getInstance().getHkCard();
            double maxAvailableInvestAmount = computeMaxAvailableInvestAmount();
            if (card.status == BankCard.Card_Status_Normal && investAmount > maxAvailableInvestAmount) {
                dialog.dismiss();
                String message = String.format("总投资%s,超过银行卡当日限额", normalAmount);
                createAlertDialog(this, message).show();
                return;
            }

            String couponID = safeGet(() -> mSelectedCoupon.sId, "");
            GlobalVariableDic.shareInstance().update(CommonProxyActivity.KEY_FUND, mFund);
            GlobalVariableDic.shareInstance().update(CommonProxyActivity.KEY_AMOUNT_DOUBLE, investAmount);
            GlobalVariableDic.shareInstance().update(CommonProxyActivity.KEY_COUPON_ID_STRING, couponID);
            CashUIController.performInvest(this, dialog, mFundId, investAmount, couponID);

        }

        private CharSequence computeFundDuration(FundBrief raw) {
            return safeGet(() -> {
                StringBuilder sb = new StringBuilder();
                StringPair[] pairs = formatRemainingTimeOverMonth(raw.stopTime - raw.startTime);
                for (StringPair pair : pairs) {
                    sb.append(pair.first).append(pair.second);
                }
                return sb.toString();
            }, "");
        }

        private boolean isInvestAmountFix() {
            return mFund.minInvestLimit == mFund.maxInvestLimit;
        }

        private String getInvestAmountFromView() {
            if (mExpectInvestAmount > 0D) {
                return String.valueOf(mExpectInvestAmount);
            }
            if (isInvestAmountFix())
                return "" + mFund.maxInvestLimit;
            else
                return StringExtension.map(mFlexibleAmountField, normalMoneyTransformer());
        }

        private boolean hasEnoughAccountBalance(String money) {
            if (money.length() == 0)
                return true;

            double accountBalance = (mFund.moneyType == Money_Type.CN) ? CashierManager.getInstance().getCnCashBalanceWithoutDecimal() : CashierManager.getInstance().getHkCashBalanceWithoutDecimal();
            double moneyInDouble = Double.valueOf(money);
            return accountBalance >= moneyInDouble;
        }

        private double computeMaxAvailableRechargeAmount() {
            if (mFund.moneyType == Money_Type.CN) {
                BankCard card = CashierManager.getInstance().getCard();
                if (card.bank != null) {
                    return max(card.bank.dayLimit - FortuneManager.getInstance().cnAccount.todayDeposit, 0);
                }
            } else if (mFund.moneyType == Money_Type.HK) {
                BankCard card = CashierManager.getInstance().getHkCard();
                if (card.bank != null) {
                    return max(card.bank.dayLimit - FortuneManager.getInstance().hkAccount.todayDeposit, 0);
                }
            }
            return 0;
        }

        private double computeMaxAvailableInvestAmount() {
            double ret = 0;
            double accountBalance = (mFund.moneyType == Money_Type.CN) ? CashierManager.getInstance().getCnCashBalance() : CashierManager.getInstance().getHkCashBalance();
            ret += accountBalance;
            if (mFund.moneyType == Money_Type.CN) {
                BankCard card = CashierManager.getInstance().getCard();
                if (card.bank != null) {
                    double availableRechargeAmount = max(card.bank.dayLimit - FortuneManager.getInstance().cnAccount.todayDeposit, 0);
                    ret += availableRechargeAmount;
                }
            } else if (mFund.moneyType == Money_Type.HK) {
                BankCard card = CashierManager.getInstance().getHkCard();
                if (card.bank != null) {
                    double availableRechargeAmount = max(card.bank.dayLimit - FortuneManager.getInstance().hkAccount.todayDeposit, 0);
                    ret += availableRechargeAmount;
                }
            }
            return ret;
        }

        private void performRecharge() {
            mIntentToRechargeHKAccount = mFund.moneyType == Money_Type.HK;

            if (mFund.moneyType == Money_Type.CN) {
                UmengUtil.stat_recharge_to_cn_account_event(getActivity());
            } else if (mFund.moneyType == Money_Type.HK) {
                UmengUtil.stat_recharge_to_hk_account_event(getActivity());
            }
            showActivity(this, an_RechargePage(mFund.moneyType, 0D));
        }

        /**
         * 获取最小投资金额
         */
        private double getMinLimitInvestAmount() {
            return mFund.minInvestLimit;
        }

        /**
         * 获取最高投资金额
         */
        private double getMaxLimitInvestAmount() {
            return mFund.maxInvestLimit;
        }
    }

    public static class InvestFundSuccessFragment extends BaseFragment {

        private String mMessage;
        private ShareInfo mShareInfo;

        public InvestFundSuccessFragment init(String message, ShareInfo shareInfo) {
            Bundle arguments = new Bundle();
            arguments.putString(KEY_CN_RECHARGE_SUCCESS_MESSAGE, message);
            arguments.putParcelable(CommonProxyActivity.KEY_SHARE_INFO_OBJECT, shareInfo);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mMessage = getArguments().getString(KEY_CN_RECHARGE_SUCCESS_MESSAGE, "");
            mShareInfo = safeGet(() -> getArguments().getParcelable(CommonProxyActivity.KEY_SHARE_INFO_OBJECT), null);
            return inflater.inflate(R.layout.frag_invest_fund_success, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this,findToolbar(this),R.drawable.ic_close_light);
            v_setText(this, R.id.label_message, mMessage);
            v_setClick(view, R.id.btn_done, v -> {
                goBack(this);
            });

            View shareSection = v_findView(this, R.id.section_share);
            ShareInfo shareInfo = mShareInfo;
            if (shareInfo != null) {
                resetSharePlatformSection(shareSection, shareInfo);
            }
            v_setVisibility(shareSection, shareInfo != null ? View.VISIBLE : View.GONE);
        }

        private void resetSharePlatformSection(View shareSection, ShareInfo shareInfo) {
            Context ctx = shareSection.getContext();
            LinearLayout sharePlatformList = v_findView(shareSection, R.id.list_share_platform);
            sharePlatformList.removeAllViews();

            SharePlatform[] platforms = {SharePlatform.WX, SharePlatform.WX_CIRCLE, SharePlatform.QQ, SharePlatform.SINA};
            Stream.of(platforms)
                    .forEach(platform -> {
                        LinearLayout cell = new LinearLayout(ctx);
                        cell.setOrientation(LinearLayout.VERTICAL);
                        cell.setGravity(Gravity.CENTER_HORIZONTAL);

                        ImageView iconImage = new ImageView(ctx);
                        iconImage.setImageResource(platform.iconResId);
                        cell.addView(iconImage, new LinearLayout.LayoutParams(-2, -2));

                        TextView titleLabel = new TextView(ctx);
                        titleLabel.setTextSize(12);
                        titleLabel.setTextColor(SignalColorHolder.TEXT_GREY_COLOR);
                        titleLabel.setText(platform.title);
                        cell.addView(titleLabel, apply(new LinearLayout.LayoutParams(-2, -2), params -> {
                            params.topMargin = dp2px(10);
                        }));

                        sharePlatformList.addView(cell, apply(new LinearLayout.LayoutParams(-2, -2), params -> {
                            params.leftMargin = dp2px(16);
                            params.rightMargin = dp2px(16);
                        }));

                        v_setClick(cell, v -> {
                            if (getActivity() != null && getActivity() instanceof UMShareHandlerProtocol) {
                                UMShareHandlerProtocol protocol = (UMShareHandlerProtocol) getActivity();
                                protocol.onPerformShare(shareInfo, platform);
                            }
                        });
                    });
        }
    }
}
