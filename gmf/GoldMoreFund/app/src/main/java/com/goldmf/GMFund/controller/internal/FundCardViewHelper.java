package com.goldmf.GMFund.controller.internal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.Shape;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.StringPair;
import com.goldmf.GMFund.extension.FlagExtension;
import com.goldmf.GMFund.extension.ViewGroupExtension;
import com.goldmf.GMFund.model.CommonDefine.PlaceHolder;
import com.goldmf.GMFund.model.Fund;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.FundBrief.Fund_Status;
import com.goldmf.GMFund.model.FundBrief.Fund_Type;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.util.FormatUtil;
import com.goldmf.GMFund.util.SecondUtil;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action2;
import rx.functions.Func0;
import yale.extension.common.shape.RoundCornerShape;
import yale.extension.rx.RXFragment;

import static com.goldmf.GMFund.controller.internal.ActivityNavigation.*;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_SEP_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RISE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getIncomeTextColor;
import static com.goldmf.GMFund.extension.FlagExtension.hasFlag;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setRoundBackgroundColor;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageResource;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setInvisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_setProgress;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_updateLayoutParams;
import static com.goldmf.GMFund.extension.ViewGroupExtension.v_forEach;
import static com.goldmf.GMFund.util.FormatUtil.formatBigNumber;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatRatio;
import static com.goldmf.GMFund.util.FormatUtil.formatRemainingTimeOverMonth;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;
import static com.goldmf.GMFund.util.FormatUtil.formateRemainingDays;

/**
 * Created by yale on 15/11/7.
 */
public class FundCardViewHelper {
    public static final int FLAG_USE_INVEST_STYLE = 1 << 1;
    public static final int FLAG_USE_MANAGE_STYLE = 1 << 2;
    public static final int FLAG_NO_MARGIN = 1 << 3;
    public static final int FLAG_BACKGROUND_PURE_WHITE = 1 << 4;
    public static final int FLAG_SHOW_SEP_LINE = 1 << 5;
    public static final int FLAG_SHOW_INCOME_DETAIL = 1 << 6;

    public static Observable createObservableUpdateListTimer(WeakReference<RXFragment> vcRef, LinearLayout container, int flags) {
        boolean[] needTimer = {false};

        ViewGroupExtension.v_forEach(container, (idx, child) -> {
            FundCardViewModel vm = (FundCardViewModel) child.getTag();
            if (vm != null && vm.fundStatus == Fund_Status.Booking) {
                needTimer[0] = true;
            }
        });

        if (needTimer[0]) {
            return Observable.interval(1, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(ignored -> {
                        RXFragment vc = vcRef.get();
                        if (vc != null && vc.getUserVisibleHint() && vc.getView() != null) {
                            updateFundCardListView(container, flags);
                        }
                    });
        }
        return Observable.empty();
    }

    public static FundCardViewModel createViewModel(FundBrief brief) {
        return new FundCardViewModel(brief);
    }

    public static void resetFundCardListViewWithFundBrief(LinearLayout container, List<FundBrief> items, int flags) {
        resetFundCardListView(container, () -> Stream.of(items).map(it -> createViewModel(it)).collect(Collectors.toList()), flags);
    }

    public static void resetFundCardListViewWithViewModel(LinearLayout container, List<FundCardViewModel> items, int flags) {
        resetFundCardListView(container, () -> items, flags);
    }

    public static void resetFundCardListView(LinearLayout container, Func0<List<FundCardViewModel>> itemsGetter, int flags) {
        Context ctx = container.getContext();
        List<FundCardViewModel> items = safeGet(() -> itemsGetter.call(), Collections.<FundCardViewModel>emptyList());
        int childCount = container.getChildCount();
        int itemCount = items.size();
        int[] index = {0};

        Stream.of(items)
                .forEach(item -> {
                    boolean needToNewCell = index[0] >= childCount;
                    View cell = needToNewCell ? createCell(ctx, container) : container.getChildAt(index[0]);
                    FundCardViewHelper.afterCreateCell(cell, item);
                    FundCardViewHelper.updateCell(cell, item, flags);
                    cell.setTag(item);
                    index[0]++;
                    if (needToNewCell) {
                        container.addView(cell);
                    }
                });

        int deprecatedCellCount = childCount - itemCount;
        while (deprecatedCellCount > 0) {
            container.removeView(container.getChildAt(container.getChildCount() - 1));
            deprecatedCellCount--;
        }
    }

    public static void updateFundCardListView(LinearLayout container, int flags) {
        updateFundCardListView(container, flags, null);
    }

    public static void updateFundCardListView(LinearLayout container, int flags, Action2<View, FundCardViewModel> afterUpdateCell) {
        v_forEach(container, (idx, cell) -> {
            FundCardViewModel vm = (FundCardViewModel) cell.getTag();
            if (vm != null) {
                if (vm.fundStatus == Fund_Status.Review || vm.fundStatus == Fund_Status.Booking) {
                    vm.update();
                    updateCell(cell, vm, flags);
                    if (afterUpdateCell != null) {
                        afterUpdateCell.call(cell, vm);
                    }
                }
            }
        });
    }

    public static View createCell(Context ctx, ViewGroup parent) {
        return LayoutInflater.from(ctx).inflate(R.layout.cell_fund_card, parent, false);
    }

    public static void afterCreateCell(View cell, FundCardViewModel vm) {
        Context ctx = cell.getContext();
        v_setClick(cell, v -> {
            showActivity(ctx, an_FundDetailPage(vm.raw.index));
        });

        View extraSection = cell.findViewById(R.id.card_normal).findViewById(R.id.section_extra);
        v_setClick(extraSection, v -> {
            showActivity(ctx, an_FundTradePage(vm.raw.index, vm.raw.name, "", 0));
        });
    }

    public static void updateCell(View cell, FundCardViewModel vm, int flags) {
        boolean useInvestStyle = (flags & FLAG_USE_INVEST_STYLE) != 0;
        boolean useManageStyle = (flags & FLAG_USE_MANAGE_STYLE) != 0;
        boolean noMargin = (flags & FLAG_NO_MARGIN) != 0;
        boolean backgroundPureWhite = (flags & FLAG_BACKGROUND_PURE_WHITE) != 0;
        boolean showSepLine = (flags & FLAG_SHOW_SEP_LINE) != 0;

        View detailCard = v_findView(cell, R.id.card_detail);
        View normalCard = v_findView(cell, R.id.card_normal);
        View investedCard = v_findView(cell, R.id.card_invested);
        View[] cards = {detailCard, normalCard, investedCard};
        Stream.of(cards)
                .limit(2)
                .forEach(card -> {
                    v_updateLayoutParams(card, ViewGroup.MarginLayoutParams.class, params -> {
                        if (noMargin) {
                            params.setMargins(0, 0, 0, 0);
                        } else {
                            params.setMargins(dp2px(10), dp2px(5), dp2px(10), dp2px(5));
                        }
                    });
                });
        Stream.of(cards)
                .forEach(card -> {
                    final int pressedColor = 0xFFEEEEEE;
                    final int normalColor = WHITE_COLOR;
                    StateListDrawable background = new StateListDrawable();
                    if (backgroundPureWhite) {
                        background.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressedColor));
                        background.addState(new int[]{}, new ColorDrawable(normalColor));
                    } else {
                        background.addState(new int[]{android.R.attr.state_pressed}, new ShapeDrawable(new RoundCornerShape(pressedColor, dp2px(4)).border(LINE_SEP_COLOR, dp2px(0.5f))));
                        background.addState(new int[]{}, new ShapeDrawable(new RoundCornerShape(normalColor, dp2px(4)).border(LINE_SEP_COLOR, dp2px(0.5f))));
                    }
                    card.setBackgroundDrawable(background);
                });
        v_findView(cell, R.id.line_sep).setVisibility(showSepLine ? View.VISIBLE : View.GONE);

        if (useInvestStyle) {
            _updateInvestStyleCell(cell, vm);
            return;
        }

        if (useManageStyle) {
            _updateManageStyleCell(cell, vm);
            return;
        }

        if (vm.isUnknownType || vm.isUnknownStatus) {
            _updateUnknownCell(cell, vm);
            return;
        }

        switch (vm.fundStatus) {
            case Fund_Status.Review:
                _updateReviewCell(cell, vm);
                break;
            case Fund_Status.Booking: {
                _updateBookingCell(cell, vm);
                break;
            }
            case Fund_Status.Capital: {
                _updateRaisingCell(cell, vm);
                break;
            }
            case Fund_Status.LockIn: {
                _updateLockedCell(cell, vm, flags);
                break;
            }
            case Fund_Status.Stop: {
                _updateStopCell(cell, vm);
                break;
            }
            default:
        }
    }

    private static void _updateUnknownCell(View cell, FundCardViewModel vm) {
        v_setVisible(cell, R.id.card_normal);
        v_setGone(cell, R.id.card_detail);
        v_setGone(cell, R.id.card_invested);
        cell = v_findView(cell, R.id.card_normal);

        v_setImageUri(cell, R.id.img_icon, vm.fundIconURL);
        v_setImageResource(cell, R.id.img_fund_tag, 0);
        v_setText(cell, R.id.label_name, vm.fundName);
        if (vm.isUnknownType) {
            v_setText(cell, R.id.label_duration, "未知类型，请更新操盘侠APP");
        } else if (vm.isUnknownStatus) {
            v_setText(cell, R.id.label_duration, "未知状态，请更新操盘侠APP");
        } else {
            v_setText(cell, R.id.label_duration, "");
        }
        v_setGone(cell, R.id.label_income_hint);
        v_setGone(cell, R.id.label_income_or_position);
        v_setGone(cell, R.id.progress_position);
    }

    private static void _updateReviewCell(View cell, FundCardViewModel vm) {
        v_setGone(cell, R.id.card_normal);
        v_setVisible(cell, R.id.card_detail);
        v_setGone(cell, R.id.card_invested);
        cell = v_findView(cell, R.id.card_detail);
        v_setImageUri(cell, R.id.img_icon, vm.fundIconURL);

        v_setImageResource(cell, R.id.img_fund_tag, vm.fundImgTag);

        v_setText(cell, R.id.label_name, vm.fundName);
        v_setText(cell, R.id.label_duration, vm.fundDuration);
        v_setGone(cell, R.id.progress_position);
        v_setText(cell, R.id.label_income_or_position, anyMatch(vm.fundInnerType, Fund_Type.Bonus) ? vm.expectAnnualFundIncomeRatio : vm.recentYearFundIncomeRatio);
        v_setVisibility(cell, R.id.label_income_or_position, vm.fundPresentType == Fund_Type.Charitable ? View.GONE : View.VISIBLE);

        v_setText(cell, R.id.label_income_hint, anyMatch(vm.fundInnerType, Fund_Type.Bonus) ? vm.expectAnnualFundIncomeRatioHint : vm.recentYearFundIncomeRatioHint);
        v_setVisibility(cell, R.id.label_income_hint, vm.fundPresentType == Fund_Type.Charitable ? View.GONE : View.VISIBLE);

        v_setText(cell, R.id.label_detail, vm.reviewLabel);
        v_setVisible(cell, R.id.label_detail);

        v_setProgress(cell, R.id.progress_detail, vm.raisedProgressInt, 10000);

        v_setInvisible(cell, R.id.label_detail_left);
        v_setInvisible(cell, R.id.label_detail_right);
    }

    private static void _updateBookingCell(View cell, FundCardViewModel vm) {
        v_setGone(cell, R.id.card_normal);
        v_setVisible(cell, R.id.card_detail);
        v_setGone(cell, R.id.card_invested);
        cell = v_findView(cell, R.id.card_detail);
        v_setImageUri(cell, R.id.img_icon, vm.fundIconURL);

        v_setImageResource(cell, R.id.img_fund_tag, vm.fundImgTag);

        v_setText(cell, R.id.label_name, vm.fundName);
        v_setText(cell, R.id.label_duration, vm.fundDuration);
        v_setGone(cell, R.id.progress_position);
        v_setText(cell, R.id.label_income_or_position, anyMatch(vm.fundInnerType, Fund_Type.Bonus) ? vm.expectAnnualFundIncomeRatio : vm.recentYearFundIncomeRatio);
        v_setVisibility(cell, R.id.label_income_or_position, vm.fundPresentType == Fund_Type.Charitable ? View.GONE : View.VISIBLE);

        v_setText(cell, R.id.label_income_hint, anyMatch(vm.fundInnerType, Fund_Type.Bonus) ? vm.expectAnnualFundIncomeRatioHint : vm.recentYearFundIncomeRatioHint);
        v_setVisibility(cell, R.id.label_income_hint, vm.fundPresentType == Fund_Type.Charitable ? View.GONE : View.VISIBLE);

        v_setText(cell, R.id.label_detail, vm.openTime);
        v_setVisible(cell, R.id.label_detail);

        v_setProgress(cell, R.id.progress_detail, vm.raisedProgressInt, 10000);

        v_setInvisible(cell, R.id.label_detail_left);
        v_setInvisible(cell, R.id.label_detail_right);
    }

    private static void _updateRaisingCell(View cell, FundCardViewModel vm) {
        v_setGone(cell, R.id.card_normal);
        v_setVisible(cell, R.id.card_detail);
        v_setGone(cell, R.id.card_invested);
        cell = v_findView(cell, R.id.card_detail);
        v_setImageUri(cell, R.id.img_icon, vm.fundIconURL);

        v_setImageResource(cell, R.id.img_fund_tag, vm.fundImgTag);
        v_setText(cell, R.id.label_name, vm.fundName);
        v_setText(cell, R.id.label_duration, vm.fundDuration);
        v_setGone(cell, R.id.progress_position);

        v_setText(cell, R.id.label_income_or_position, anyMatch(vm.fundInnerType, Fund_Type.Bonus) ? vm.expectAnnualFundIncomeRatio : vm.recentYearFundIncomeRatio);
        v_setVisibility(cell, R.id.label_income_or_position, vm.fundPresentType == Fund_Type.Charitable ? View.GONE : View.VISIBLE);
        v_setText(cell, R.id.label_income_hint, anyMatch(vm.fundInnerType, Fund_Type.Bonus) ? vm.expectAnnualFundIncomeRatioHint : vm.recentYearFundIncomeRatioHint);
        v_setVisibility(cell, R.id.label_income_hint, vm.fundPresentType == Fund_Type.Charitable ? View.GONE : View.VISIBLE);
        v_setProgress(cell, R.id.progress_detail, vm.raisedProgressInt, 10000);
        v_setText(cell, R.id.label_detail_right, anyMatch(vm.fundInnerType, Fund_Type.Porfolio) ? vm.raisedAmountRatio : vm.fundRemainingAmount);
        v_setText(cell, R.id.label_detail_left, vm.raisedAmount);
        v_setInvisible(cell, R.id.label_detail);
        v_setVisible(cell, R.id.label_detail_left);
        v_setVisible(cell, R.id.label_detail_right);
    }

    private static void _updateLockedCell(View cell, FundCardViewModel vm, int flags) {
        boolean showIncomeDetail = hasFlag(flags, FLAG_SHOW_INCOME_DETAIL);

        v_setVisible(cell, R.id.card_normal);
        v_setGone(cell, R.id.card_detail);
        v_setGone(cell, R.id.card_invested);
        cell = v_findView(cell, R.id.card_normal);
        v_setImageUri(cell, R.id.img_icon, vm.fundIconURL);

        v_setImageResource(cell, R.id.img_fund_tag, vm.fundImgTag);
        v_setText(cell, R.id.label_name, vm.fundName);
        v_setText(cell, R.id.label_duration, vm.fundDuration);
        if (anyMatch(vm.fundInnerType, Fund_Type.Bonus)) {
            v_setText(cell, R.id.label_income_or_position, vm.currentAnnualFundIncomeRatio);
            v_setText(cell, R.id.label_income_hint, vm.currentAnnualFundIncomeRatioHint);
            v_setVisible(cell, R.id.label_income_hint);
            v_setGone(cell, R.id.progress_position);
        } else {
            if (vm.showFundIncome) {
                v_setText(cell, R.id.label_income_or_position, vm.currentFundIncomeRatio);
                v_setText(cell, R.id.label_income_hint, vm.currentFundIncomeRatioHint);
                v_setVisible(cell, R.id.label_income_hint);
                v_setGone(cell, R.id.progress_position);
            } else {
                v_setText(cell, R.id.label_income_or_position, vm.currentPositionText);
                v_setProgress(cell, R.id.progress_position, vm.currentPositionInt, 100);
                v_setVisible(cell, R.id.progress_position);
                v_setGone(cell, R.id.label_income_hint);
            }
        }

        View extraSection = v_findView(cell, R.id.section_extra);
        v_setVisibility(extraSection, showIncomeDetail ? View.VISIBLE : View.GONE);
        if (showIncomeDetail) {
            int extraBGColor = cell.getResources().getColor(R.color.gmf_sep_Line);
            extraSection.setBackgroundDrawable(new ShapeDrawable(new Shape() {
                final RectF rect = new RectF();
                final Path path = new Path();
                final int RADIUS = dp2px(4);
                final float[] RADII = {0, 0, 0, 0, RADIUS, RADIUS, RADIUS, RADIUS};

                @Override
                public void draw(Canvas canvas, Paint paint) {
                    path.reset();
                    rect.set(0, 0, canvas.getWidth(), canvas.getHeight());
                    path.addRoundRect(rect, RADII, Path.Direction.CW);
                    paint.setColor(extraBGColor);
                    paint.setStyle(Paint.Style.FILL);
                    rect.inset(dp2px(0.5f), dp2px(0.5f));
                    canvas.drawPath(path, paint);
                }
            }));
            v_setText(extraSection, R.id.label_total_capital, vm.extraLocked.totalCapital);
            v_setText(extraSection, R.id.label_total_income, vm.extraLocked.totalIncome);
            v_setText(extraSection, R.id.label_today_income, vm.extraLocked.todayIncome);
        }
    }

    private static void _updateStopCell(View cell, FundCardViewModel vm) {
        v_setVisible(cell, R.id.card_normal);
        v_setGone(cell, R.id.card_detail);
        v_setGone(cell, R.id.card_invested);
        cell = v_findView(cell, R.id.card_normal);
        v_setImageUri(cell, R.id.img_icon, vm.fundIconURL);

        v_setText(cell, R.id.label_name, vm.fundName);
        v_setText(cell, R.id.label_duration, vm.fundDuration);
        v_setImageResource(cell, R.id.img_fund_tag, R.mipmap.ic_tag_fund_stop);

        if (anyMatch(vm.fundInnerType, Fund_Type.Bonus)) {
            v_setText(cell, R.id.label_income_or_position, vm.finalPersonalAnnualIncomeRatio);
            v_setText(cell, R.id.label_income_hint, vm.finalAnnualFundIncomeRatioHint);
            v_setVisible(cell, R.id.label_income_hint);
            v_setGone(cell, R.id.progress_position);
        } else {
            v_setText(cell, R.id.label_income_or_position, vm.finalFundIncomeRatio);
            v_setText(cell, R.id.label_income_hint, vm.finalFundIncomeRatioHint);
            v_setVisible(cell, R.id.label_income_hint);
            v_setGone(cell, R.id.progress_position);
        }

        v_setGone(cell, R.id.section_extra);
    }

    private static void _updateInvestStyleCell(View cell, FundCardViewModel vm) {
        boolean isStop = anyMatch(vm.fundStatus, Fund_Status.Stop);
        boolean notStop = !isStop;
        boolean isBonusInnerType = anyMatch(vm.fundInnerType, Fund_Type.Bonus);

        v_setGone(cell, R.id.card_normal);
        v_setGone(cell, R.id.card_detail);
        v_setVisible(cell, R.id.card_invested);
        cell = v_findView(cell, R.id.card_invested);

        v_setText(cell, R.id.label_left_big, vm.fundName);
        v_setText(cell, R.id.label_medium_big, vm.personalInvestedAmount);

        if (vm.isUnknownType || vm.isUnknownStatus) {
            v_setText(cell, R.id.label_left_small, String.format(Locale.getDefault(), "未知%s,请更新操盘侠APP", vm.isUnknownType ? "类型" : "状态"));
            v_setText(cell, R.id.label_right_big, PlaceHolder.NULL_VALUE);
            v_setText(cell, R.id.label_right_small, PlaceHolder.NULL_VALUE);
        } else {
            v_setText(cell, R.id.label_left_small, notStop ? vm.remainingDay : vm.stopDate);
            v_setText(cell, R.id.label_right_big, notStop ? vm.expectPersonalIncome : vm.finalPersonalIncome);
            TextView smallRightLabel = v_findView(cell, R.id.label_right_small);
            if (notStop) {
                v_setText(smallRightLabel, isBonusInnerType ? vm.expectPersonalAnnualIncomeRatio : vm.expectPersonalIncomeRatio);
            } else {
                v_setText(smallRightLabel, isBonusInnerType ? vm.finalPersonalAnnualIncomeRatio : vm.finalPersonalIncomeRatio);
            }
            smallRightLabel.setCompoundDrawablesWithIntrinsicBounds(isBonusInnerType ? R.mipmap.ic_annual_income : 0, 0, 0, 0);
        }
    }

    private static void _updateManageStyleCell(View cell, FundCardViewModel vm) {
        v_setGone(cell, R.id.card_normal);
        v_setGone(cell, R.id.card_detail);
        v_setVisible(cell, R.id.card_invested);
        cell = v_findView(cell, R.id.card_invested);

        v_setText(cell, R.id.label_left_big, vm.fundName);
        v_setText(cell, R.id.label_medium_big, vm.managedAmount);
        v_setText(cell, R.id.label_right_big, vm.takenSharing);
        v_setText(cell, R.id.label_right_small, vm.totalSharing);
        TextView smallRightLabel = v_findView(cell, R.id.label_right_small);
        smallRightLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        if (vm.isUnknownType || vm.isUnknownStatus) {
            v_setText(cell, R.id.label_left_small, String.format(Locale.getDefault(), "未知%s,请更新操盘侠APP", vm.isUnknownType ? "类型" : "状态"));
        } else {
            v_setText(cell, R.id.label_left_small, vm.stopDate);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static class FundCardViewModel {
        public FundBrief raw;
        public int moneyType;
        public int fundPresentType;
        public int fundInnerType;
        public int fundStatus;
        //        public boolean hasInvested;
        public boolean showFundIncome;
        public boolean isUnknownType;
        public boolean isUnknownStatus;

        public String fundIconURL;
        public CharSequence fundName;
        public CharSequence fundDuration;
        public CharSequence expectAnnualFundIncomeRatio;
        public CharSequence expectAnnualFundIncomeRatioHint;
        public CharSequence currentAnnualFundIncomeRatio;
        public CharSequence currentAnnualFundIncomeRatioHint;
        public CharSequence finalAnnualFundIncomeRatio;
        public CharSequence finalAnnualFundIncomeRatioHint;
        public CharSequence recentYearFundIncomeRatio;
        public CharSequence recentYearFundIncomeRatioHint;
        public CharSequence currentFundIncomeRatio;
        public CharSequence currentFundIncomeRatioHint;
        public CharSequence finalFundIncomeRatio;
        public CharSequence finalFundIncomeRatioHint;
        public CharSequence hint;
        public int raisedProgressInt;                   //10000的max
        public CharSequence currentPositionText;
        public int currentPositionInt;
        public CharSequence raisedAmount;
        public CharSequence raisedAmountRatio;

        public CharSequence remainingRunningDay;
        public CharSequence stopDate;
        public CharSequence remainingDay;               //到停止stop需要的天
        public CharSequence personalInvestedAmountBig;
        public CharSequence personalInvestedAmount;
        public CharSequence reviewLabel;
        public CharSequence openTime;
        public CharSequence fundRemainingAmount;
        public CharSequence expectPersonalIncome;
        public CharSequence expectPersonalIncomeRatio;
        public CharSequence expectPersonalAnnualIncomeRatio;
        public CharSequence finalPersonalIncome;
        public CharSequence finalPersonalIncomeRatio;
        public CharSequence finalPersonalAnnualIncomeRatio;
        public CharSequence takenSharing;
        public CharSequence totalSharing;
        public CharSequence managedAmount;
        public int fundImgTag;  //角标

        public ExtraLocked extraLocked;

        /**
         * update的时候不能修改类型
         */
        public void update() {
            this.openTime = computeOpenTime();
        }

        public FundCardViewModel(FundBrief raw) {
            this.raw = raw;
            this.extraLocked = new ExtraLocked(raw);
            this.fundStatus = safeGet(() -> raw.status, Fund_Status.Review);
            this.fundPresentType = safeGet(() -> raw.type, Fund_Type.Porfolio);
            this.fundInnerType = safeGet(() -> raw.innerType, Fund_Type.Porfolio);
            this.isUnknownType = Fund_Type.isUnknown(fundPresentType) || Fund_Type.isUnknown(fundInnerType);
            this.isUnknownStatus = Fund_Status.isUnknown(fundStatus);

            this.moneyType = safeGet(() -> raw.moneyType, Money_Type.CN);
            this.showFundIncome = safeGet(() -> raw.incomeVisible, false);

            this.fundIconURL = safeGet(() -> raw.fundIcon, "");
            this.fundName = safeGet(() -> raw.name, PlaceHolder.NULL_VALUE);
            this.fundDuration = computeFundDuration();
            this.expectAnnualFundIncomeRatio = safeGet(() -> {
                        double extra = safeGet(() -> raw.preferential.preferentialRatio, 0D);
                        double min = raw.expectedMinAnnualYield + extra;
                        double max = raw.expectedMinAnnualYield + raw.expectedMaxAnnualYield + extra;

                        if (min == max) {
                            return setColor("100%＋" + formatRatio(min, false, 0, 2), TEXT_RED_COLOR);
                        } else {
                            return setColor(concatNoBreak("100%＋", formatRatio(min, false, 0, 2).replace("%", ""), "~", formatRatio(max, false, 0, 2)), TEXT_RED_COLOR);
                        }
                    },
                    setColor(PlaceHolder.NULL_VALUE, TEXT_RED_COLOR));

            this.expectAnnualFundIncomeRatioHint = "本金＋预期收益(年化)";
            this.currentAnnualFundIncomeRatio = computeIncomeRatioText(computeUserAnnualIncomeRatio());
            this.currentAnnualFundIncomeRatioHint = "当前收益(年化)";
            this.finalAnnualFundIncomeRatio = showFundIncome ?
                    computeIncomeRatioText(safeGet(() -> computeUserAnnualIncomeRatio(), null)) :
                    setColor("仅投资人可见", TEXT_GREY_COLOR);
            this.finalAnnualFundIncomeRatioHint = "最终收益(年化)";
            this.recentYearFundIncomeRatio = safeGet(() -> {
                if (raw.expectedMinAnnualYield <= 0.0) {
                    return formatRatio(raw.capitalEnsureRatio, false, 0, 2);
                }
                return formatRatio(raw.capitalEnsureRatio, false, 0, 2) + "+" + formatRatio(raw.expectedMinAnnualYield, false, 0, 2);
            }, "");
            this.recentYearFundIncomeRatioHint = safeGet(() -> (raw.expectedMinAnnualYield <= 0.0) ? "本金保障" : "本金+保底收益(年化)", "");
            this.currentFundIncomeRatio = computeIncomeRatioText(safeGet(() -> raw.currentIncomeRatioOrNull, null));
            this.currentFundIncomeRatioHint = "当前收益";
            this.finalFundIncomeRatio = showFundIncome ? computeIncomeRatioText(safeGet(() -> raw.currentIncomeRatioOrNull, null)) : setColor("仅投资人可见", TEXT_GREY_COLOR);
            this.finalFundIncomeRatioHint = "最终收益";

            this.raisedProgressInt = safeGet(() -> (int) (raw.raisedCapital * 10000 / raw.targetCapital), 0);
            this.currentPositionText = setColor(safeGet(() -> "仓位" + formatRatio(raw.currentPositionOrNull, false, 0, 2), "仓位" + PlaceHolder.NULL_VALUE), TEXT_BLACK_COLOR);
            this.currentPositionInt = safeGet(() -> (int) (raw.currentPositionOrNull * 100), 0);

            this.raisedAmount = safeGet(
                    () -> "已投资 " + formatBigNumber(raw.raisedCapital, false, 0, 2) + " " + Money_Type.getUnit(moneyType),
                    "已投资 " + PlaceHolder.NULL_VALUE + " " + Money_Type.getUnit(moneyType));

            SpannableStringBuilder overString = setRoundBackgroundColor(setFontSize(" 超额 ", sp2px(11)), TEXT_WHITE_COLOR, TEXT_RED_COLOR, dp2px(2));
            boolean over = raw.raisedCapital >= raw.targetCapital;
            String raisedRatio = formatRatio(formatRatio(raw.raisedCapital / raw.targetCapital, 0.5, 2), false, 2);

            raisedAmountRatio = safeGet(
                    () -> over ? (concatNoBreak(setColor(raisedRatio, TEXT_RED_COLOR), " ", overString)) : raisedRatio,
                    "已投资 " + PlaceHolder.NULL_VALUE + " " + Money_Type.getUnit(moneyType));

            this.remainingRunningDay = safeGet(() -> formateRemainingDays(raw.remainingDays()))
                    .def(PlaceHolder.NULL_VALUE + "天")
                    .get();
            this.remainingDay = safeGet(() -> raw.stopDayStr())
                    .def(PlaceHolder.NULL_VALUE + "天")
                    .get();
            this.stopDate = safeGet(() -> formatSecond(raw.stopTime, "yyyy/MM/dd"))
                    .def(PlaceHolder.NULL_VALUE)
                    .get();

            this.personalInvestedAmountBig = safeGet(() -> formatBigNumber(raw.investOrNull.investMoney, false, 0, 2, true))
                    .def(PlaceHolder.NULL_VALUE)
                    .get();

            this.personalInvestedAmount = safeGet(() -> formatMoney(raw.investOrNull.investMoney, false, 0))
                    .def(PlaceHolder.NULL_VALUE)
                    .get();

            this.fundRemainingAmount = safeGet(() -> {
                if (raw.targetCapital - raw.raisedCapital <= 0) {
                    return concatNoBreak(" ", setRoundBackgroundColor(setFontSize(" 满额 ", sp2px(11)), TEXT_WHITE_COLOR, TEXT_RED_COLOR, dp2px(2)));
                }
                return "还可投 " + formatBigNumber(raw.targetCapital - raw.raisedCapital, false, 0, 2) + " " + Money_Type.getUnit(moneyType);
            })
                    .def("")
                    .get();

            this.expectPersonalIncome = safeGet(() -> setColor(formatMoney(raw.investOrNull.totalIncome, false, 2), getIncomeTextColor(raw.investOrNull.totalIncome, RISE_COLOR)))
                    .def(setColor("--", TEXT_BLACK_COLOR))
                    .get();
            this.expectPersonalIncomeRatio = safeGet(() -> setColor(formatRatio(raw.investOrNull.totalIncomeRatio, true, 2), getIncomeTextColor(raw.investOrNull.totalIncomeRatio, RISE_COLOR)))
                    .def(setColor("--", TEXT_BLACK_COLOR))
                    .get();
            this.expectPersonalAnnualIncomeRatio = safeGet(() -> {
                Double annualIncome = computeUserAnnualIncomeRatio();
                return setColor(formatRatio(annualIncome, true, 2), getIncomeTextColor(annualIncome, RISE_COLOR));
            })
                    .def(setColor("--", TEXT_BLACK_COLOR))
                    .get();
            this.finalPersonalIncome = expectPersonalIncome;
            this.finalPersonalIncomeRatio = expectPersonalIncomeRatio;
            this.finalPersonalAnnualIncomeRatio = expectPersonalAnnualIncomeRatio;
            this.takenSharing = setColor(formatBigNumber(safeGet(() -> raw.traderInvestMementOrNull.extractedShare, 0D), false, 2), TEXT_RED_COLOR);
            this.totalSharing = setColor(formatBigNumber(safeGet(() -> raw.traderInvestMementOrNull.totalShare, 0D), false, 2), TEXT_RED_COLOR);
            this.managedAmount = safeGet(() -> formatMoney(raw.raisedCapital, false, 0), PlaceHolder.NULL_VALUE);

            this.fundImgTag = safeGet(() -> {
                if (anyMatch(fundStatus, Fund_Status.Stop))
                    return R.mipmap.ic_tag_fund_stop;
                else if (anyMatch(fundInnerType, Fund_Type.WuYo))
                    return R.mipmap.ic_tag_fund_wuyou;
                else if (anyMatch(fundInnerType, Fund_Type.WenJian))
                    return R.mipmap.ic_tag_fund_wenyin;
                else if (anyMatch(fundInnerType, Fund_Type.Porfolio))
                    return R.mipmap.ic_tag_fund_jinqu;

                return 0;
            }, 0);

            this.reviewLabel = setColor("审核中", TEXT_GREY_COLOR);
            this.update();
        }

        private Double computeUserAnnualIncomeRatio() {
            return safeGet(() -> {
                if (anyMatch(fundInnerType, Fund_Type.Bonus)) {
                    double preferentialRatio = safeGet(() -> raw.preferential.preferentialRatio, 0D);
                    return raw.expectedMinAnnualYield + opt(raw.floatingAnnualYieldOrNull).or(0D) + preferentialRatio;
                } else {
                    return raw.currentIncomeAnnualRatioOrNull;
                }
            }, 0D);
        }

        private CharSequence computeFundDuration() {
            return safeGet(() -> {
                if (anyMatch(raw.innerType, Fund_Type.Bonus) || raw.userProfitSharingRatio == null) {
                    StringBuilder sb = new StringBuilder();
                    StringPair[] pairs = formatRemainingTimeOverMonth(raw.stopTime - raw.startTime);
                    for (StringPair pair : pairs) {
                        sb.append(pair.first).append(pair.second);
                    }
                    return sb.toString();
                } else {
                    return concatNoBreak(setColor(String.format(Locale.getDefault(), "%s收益分成", formatRatio(raw.userProfitSharingRatio, false, 0, 2)), TEXT_RED_COLOR), String.format(Locale.getDefault(), " • T+%d", raw.tradingDay));
                }
            }, "");
        }

        private CharSequence computeIncomeRatioText(@Nullable Double income) {
            if (income == null) {
                return setColor(PlaceHolder.NULL_VALUE, TEXT_GREY_COLOR);
            }

            return safeGet(() -> setColor(formatRatio(income, false, 0, 2), getIncomeTextColor(income)),
                    setColor(PlaceHolder.NULL_VALUE, TEXT_BLACK_COLOR));
        }

        private CharSequence computeOpenTime() {
            return safeGet(() -> {
                SpannableStringBuilder sb = new SpannableStringBuilder();

                double second = raw.beginFundraisingTime - SecondUtil.currentSecond() + 15;//特别处理15s
                if (second > 0) {
                    StringPair[] pairs = FormatUtil.formatRemainingTimeOverDay(second);

                    for (StringPair pair : pairs) {
                        sb = concatNoBreak(sb, pair.first, pair.second);
                    }
                    sb = setColor(concatNoBreak(sb, " 后开放投资"), TEXT_GREY_COLOR);
                    return sb;
                } else {
                    return setColor("即将开放投资", TEXT_GREY_COLOR);
                }

            }, setColor("即将开放投资", TEXT_GREY_COLOR));
        }
    }

    public static class ExtraLocked {
        public CharSequence totalCapital;
        public CharSequence totalIncome;
        public CharSequence todayIncome;

        public ExtraLocked(FundBrief raw) {
            this.totalCapital = safeGet(() -> concat("总资产", setFontSize(setColor(formatMoney(raw.totalCapital(), false, 2), TEXT_BLACK_COLOR), sp2px(14))), "");
            this.totalIncome = safeGet(() -> concat("总盈亏", setFontSize(setColor(formatMoney(raw.currentIncomeOrNull(), false, 2), getIncomeTextColor(raw.currentIncomeOrNull())), sp2px(14))), "");
            this.todayIncome = safeGet(() -> concat("当日盈亏", setFontSize(setColor(formatMoney(raw.dayIncome(), false, 2), getIncomeTextColor(raw.dayIncome())), sp2px(14))), "");
        }
    }
}
