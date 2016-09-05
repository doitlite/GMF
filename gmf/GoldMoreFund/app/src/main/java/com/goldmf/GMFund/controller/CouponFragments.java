package com.goldmf.GMFund.controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.business.CouponController;
import com.goldmf.GMFund.controller.internal.SignalColorHolder;
import com.goldmf.GMFund.manager.fortune.Coupon;
import com.goldmf.GMFund.model.CommonDefine;
import com.goldmf.GMFund.model.CommonDefine.PlaceHolder;
import com.goldmf.GMFund.widget.BasicCell;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import rx.functions.Action1;
import yale.extension.common.shape.RoundCornerShape;

import static com.goldmf.GMFund.controller.FragmentStackActivity.*;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WebViewPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LIGHT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_SEP_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLUE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.extension.FlagExtension.hasFlag;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.ObjectExtension.apply;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setClickEvent;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setRelativeFontSize;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setStyle;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_preDraw;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setTextWithFitBound;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.extension.ViewExtension.v_updateLayoutParams;
import static com.goldmf.GMFund.extension.ViewGroupExtension.v_forEach;
import static com.goldmf.GMFund.model.CommonDefine.PlaceHolder.NULL_VALUE;
import static com.goldmf.GMFund.model.FundBrief.Money_Type;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;

/**
 * Created by yalez on 2016/7/12.
 */
public class CouponFragments {
    private CouponFragments() {
    }

    public static class CouponListFragment extends SimpleFragment {
        public static final int FLAG_SHOW_PASSED_LIST = 1;
        public static final int FLAG_HIDE_RULE = 1 << 1;
        public static final int FLAG_HIDE_EXCHANGE_BTN = 1 << 2;
        public static final int FLAG_HIDE_UNAVAILABLE_CELL = 1 << 3;
        public static final int FLAG_USE_BLACK_THEME = 1 << 4;
        public static final int FLAG_SHOW_USED_TITLE = 1 << 5;
        public static final int FLAG_SHOW_AVAILABLE_TITLE = 1 << 6;
        public static final int FLAG_SET_SELECTABLE = 1 << 7;
        public static final int FLAG_HAS_SELECTED_POS = 1 << 8;
        public static final int MASK_SELECTED_COUPON_POS = 0xFF000000;
        public static final int OFFSET_SELECTED_COUPON_POS = 24;

        private int mFlags = 0;
        private List<Coupon> mPassedList;
        private boolean mDataExpired = false;

        CouponListFragment init(int flags, LinkedList<Coupon> list) {
            Bundle arguments = new Bundle();
            arguments.putInt(CommonProxyActivity.KEY_FLAGS_INT, flags);
            arguments.putSerializable(CommonProxyActivity.KEY_COUPON_LIST_OIBJECT, list);
            setArguments(arguments);
            return this;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mFlags = getArguments().getInt(CommonProxyActivity.KEY_FLAGS_INT);
            mPassedList = safeGet(() -> (List<Coupon>) getArguments().getSerializable(CommonProxyActivity.KEY_COUPON_LIST_OIBJECT), Collections.<Coupon>emptyList());
            return inflater.inflate(R.layout.frag_coupon_list, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            boolean isUseBlackTheme = hasFlag(mFlags, FLAG_USE_BLACK_THEME);
            setStatusBarBackgroundColor(this, isUseBlackTheme ? STATUS_BAR_BLACK : RED_COLOR);
            findToolbar(this).setBackgroundColor(isUseBlackTheme ? STATUS_BAR_BLACK : RED_COLOR);
            setupBackButton(this, findToolbar(this));

            boolean isHideExchangeButton = hasFlag(mFlags, FLAG_HIDE_EXCHANGE_BTN);
            v_setClick(this, R.id.btn_exchange, v -> {
                showActivity(this, an_WebViewPage(CommonDefine.H5URL_COUPON_CODE()));
                mDataExpired = true;
            });
            v_setVisibility(this, R.id.btn_exchange, isHideExchangeButton ? View.GONE : View.VISIBLE);

            boolean isShowUsedTitle = hasFlag(mFlags, FLAG_SHOW_USED_TITLE);
            boolean isShowAvailableTitle = hasFlag(mFlags, FLAG_SHOW_AVAILABLE_TITLE);
            if (isShowUsedTitle) {
                int unavailableCount = safeGet(() -> mPassedList.size(), 0);
                String title = String.format(Locale.getDefault(), "已使用和过期投资红包(%d)", unavailableCount);
                updateTitle(title);
            } else if (isShowAvailableTitle) {
                updateTitle("可用投资红包");
            } else {
                updateTitle("投资红包");
            }

            setOnSwipeRefreshListener(() -> fetchData(false));
            v_setClick(mReloadSection, v -> fetchData(true));
            boolean isShowPassedList = hasFlag(mFlags, FLAG_SHOW_PASSED_LIST);
            setSwipeRefreshable(!isShowPassedList);

            View emptySpan = v_findView(mContentSection, R.id.span_empty);
            apply(emptySpan, it -> it.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(WHITE_COLOR, dp2px(4)).border(SignalColorHolder.LINE_SEP_COLOR, dp2px(0.5f)))));

            TextView labelRule = v_findView(this, R.id.label_rule);
            boolean hideRuleLabel = hasFlag(mFlags, FLAG_HIDE_RULE);
            if (!hideRuleLabel) {
                apply(labelRule, it -> {
                    it.setMovementMethod(LinkMovementMethod.getInstance());
                    Action1<View> clickEvent = v -> {
                        showActivity(this, an_WebViewPage(CommonDefine.H5URL_COUPON_RULE()));
                    };
                    CharSequence text = setStyle(concatNoBreak("没有更多可用券了 ", setColor(setClickEvent("了解使用规则 >", clickEvent), TEXT_BLUE_COLOR)), Typeface.BOLD);
                    it.setText(text);
                });
            }
            v_setVisibility(labelRule, hideRuleLabel ? View.GONE : View.VISIBLE);

            if (isShowPassedList) {
                resetContentSection(mPassedList);
            } else {
                fetchData(true);
            }
            mDataExpired = false;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (getUserVisibleHint() && getView() != null) {
                if (mDataExpired) {
                    mDataExpired = false;
                    boolean isShowPassedList = hasFlag(mFlags, FLAG_SHOW_PASSED_LIST);
                    if (isShowPassedList) {
                        resetContentSection(mPassedList);
                    } else {
                        fetchData(false);
                    }
                }
            }
        }

        private void fetchData(boolean reload) {
            consumeEventMRUpdateUI(CouponController.fetchCouponList(), reload)
                    .onNextSuccess(response -> {
                        resetContentSection(response.data);
                    })
                    .done();
        }

        private void resetContentSection(List<Coupon> raw) {
            List<CouponVM> items;
            boolean isShowPassedList = (mFlags & FLAG_SHOW_PASSED_LIST) != 0;
            if (isShowPassedList) {
                items = Stream.of(raw).map(it -> new CouponVM(it)).collect(Collectors.toList());
            } else {
                items = Stream.of(raw)
                        .filter(it -> !CouponVM.isDepricated(it) && !CouponVM.isUsed(it))
                        .map(it -> new CouponVM(it))
                        .collect(Collectors.toList());
            }

            LinearLayout list = v_findView(mContentSection, R.id.list);
            Context ctx = list.getContext();
            list.removeAllViews();
            if (!items.isEmpty()) {
                boolean isSelectable = hasFlag(mFlags, FLAG_SET_SELECTABLE);
                Drawable tickBitmap = getResources().getDrawable(R.drawable.ic_coupon_tick);
                tickBitmap.setBounds(0, 0, tickBitmap.getIntrinsicWidth(), tickBitmap.getIntrinsicHeight());
                Stream.of(items)
                        .forEach(vm -> {
                            View cell = LayoutInflater.from(ctx).inflate(R.layout.cell_coupon_list, list, false);
                            StateListDrawable drawable = new StateListDrawable();
                            if (isSelectable) {
                                drawable.addState(new int[]{android.R.attr.state_selected}, new ShapeDrawable(new Shape() {
                                    Path path = new Path();
                                    RectF rect = new RectF();
                                    int RADIUS = dp2px(4);

                                    @Override
                                    public void draw(Canvas canvas, Paint paint) {
                                        path.reset();
                                        rect.set(0, 0, canvas.getWidth(), canvas.getHeight());
                                        rect.inset(dp2px(2), dp2px(2));

                                        path.addRoundRect(rect, RADIUS, RADIUS, Path.Direction.CW);
                                        paint.setStrokeWidth(dp2px(2));
                                        paint.setStyle(Paint.Style.FILL);
                                        paint.setColor(WHITE_COLOR);
                                        canvas.drawPath(path, paint);
                                        paint.setStyle(Paint.Style.STROKE);
                                        paint.setColor(RED_COLOR);
                                        canvas.drawPath(path, paint);

                                        path.reset();
                                        rect.left = rect.right - dp2px(24);
                                        rect.top = rect.bottom - dp2px(24);
                                        paint.setStyle(Paint.Style.FILL);
                                        path.addRoundRect(rect, new float[]{RADIUS, RADIUS, 0, 0, RADIUS, RADIUS, 0, 0}, Path.Direction.CW);
                                        canvas.drawPath(path, paint);

                                        canvas.translate(rect.left, rect.top);
                                        canvas.translate((rect.width() - tickBitmap.getIntrinsicWidth()) / 2, (rect.height() - tickBitmap.getIntrinsicHeight()) / 2);
                                        tickBitmap.draw(canvas);
                                    }
                                }));
                            }
                            drawable.addState(new int[0], new ShapeDrawable(new RoundCornerShape(WHITE_COLOR, dp2px(4)).border(LINE_SEP_COLOR, dp2px(1))));
                            cell.setBackgroundDrawable(drawable);

                            v_updateLayoutParams(cell, LinearLayout.LayoutParams.class, params -> {
                                params.leftMargin = dp2px(10);
                                params.rightMargin = dp2px(10);
                                params.topMargin = dp2px(16);
                            });

                            View sepLine = v_findView(cell, R.id.line_sep);
                            sepLine.setBackgroundDrawable(new ShapeDrawable(new Shape() {
                                final int DIAMETER = dp2px(4);
                                final int RADIUS = DIAMETER >> 1;

                                @Override
                                public void draw(Canvas canvas, Paint paint) {
                                    int height = canvas.getHeight();
                                    int circleCount = height / RADIUS;
                                    if (circleCount > 0) {
                                        paint.setColor(LIGHT_GREY_COLOR);
                                        for (int i = 0; i < circleCount; i = i + 2) {
                                            canvas.drawCircle(RADIUS, i * DIAMETER + RADIUS, RADIUS, paint);
                                        }
                                    }
                                }
                            }));
                            v_preDraw(cell, true, v -> {
                                v_updateLayoutParams(sepLine, params -> {
                                    params.height = cell.getMeasuredHeight() - dp2px(16);
                                    sepLine.requestLayout();
                                });
                            });

                            v_setTextWithFitBound(cell, R.id.label_amount, vm.amount);
                            v_setText(cell, R.id.label_type, vm.type);
                            v_setText(cell, R.id.label_limit, vm.limit);
                            v_setText(cell, R.id.label_date, vm.date);

                            list.addView(cell);

                            if (isSelectable) {
                                v_setClick(cell, v -> {
                                    v_forEach(list, (pos, child) -> {
                                        boolean isCurrentChild = (v == child);
                                        child.setSelected(isCurrentChild);
                                    });
                                    goBack(this);
                                    NotificationCenter.selectCouponSubject.onNext(vm.raw);
                                });
                            }
                        });
                if (isSelectable) {
                    boolean hasSelectedPos = hasFlag(mFlags, FLAG_HAS_SELECTED_POS);
                    if (hasSelectedPos) {
                        int selectedPos = (mFlags & MASK_SELECTED_COUPON_POS) >> OFFSET_SELECTED_COUPON_POS;
                        list.getChildAt(selectedPos).setSelected(true);
                    }
                }
            }
            v_setVisibility(mContentSection, R.id.span_empty, items.isEmpty() ? View.VISIBLE : View.GONE);

            boolean hideUnavailableCell = hasFlag(mFlags, FLAG_HIDE_UNAVAILABLE_CELL);
            BasicCell unavailableCell = v_findView(mContentSection, R.id.cell_unavailable);
            LinkedList<Coupon> unavailableCoupons = new LinkedList<>(Stream.of(raw)
                    .filter(it -> CouponVM.isDepricated(it) || CouponVM.isUsed(it))
                    .collect(Collectors.toList()));
            if (!hideUnavailableCell) {
                v_setText(unavailableCell.getTitleLabel(), String.format(Locale.getDefault(), "已使用和过期投资红包(%d)", unavailableCoupons.size()));
                v_setClick(unavailableCell, v -> {
                    int flags = FLAG_SHOW_PASSED_LIST |
                            FLAG_HIDE_EXCHANGE_BTN |
                            FLAG_HIDE_RULE |
                            FLAG_HIDE_UNAVAILABLE_CELL |
                            FLAG_SHOW_USED_TITLE;
                    CouponListFragment fragment = new CouponListFragment().init(flags, unavailableCoupons);
                    fragment.mPassedList = unavailableCoupons;
                    pushFragment(this, fragment);
                });
            }
            v_setVisibility(unavailableCell, (hideUnavailableCell || unavailableCoupons.isEmpty()) ? View.GONE : View.VISIBLE);
        }
    }

    private static class CouponVM {
        private Coupon raw;
        private CharSequence amount;
        private CharSequence type;
        private CharSequence limit;
        private CharSequence date;
        private boolean isDeprecated;
        private boolean isUsed;

        @SuppressWarnings("ConstantConditions")
        public CouponVM(Coupon raw) {
            boolean isUnknownStatus = raw == null || Coupon.isUnknownStatus(raw.status);
            boolean isUnknownType = raw == null || Coupon.isUnknownType(raw.type);

            this.raw = raw;
            this.isDeprecated = isDepricated(raw);
            this.isUsed = isUsed(raw);
            this.type = safeGet(() -> raw.title, PlaceHolder.NULL_VALUE);
            this.type = setColor(this.type, (isUsed || isDeprecated) ? TEXT_GREY_COLOR : TEXT_BLACK_COLOR);

            if (isDeprecated) {
                this.date = setColor(safeGet(() -> setStyle(formatSecond(raw.stopTime, "yyyy.MM.dd"), Typeface.BOLD) + " 过期", PlaceHolder.NULL_VALUE), TEXT_RED_COLOR);
            } else if (isUsed) {
                this.date = setColor(safeGet(() -> setStyle(formatSecond(raw.usedTime, "yyyy.MM.dd"), Typeface.BOLD) + " 使用", PlaceHolder.NULL_VALUE), TEXT_RED_COLOR);
            } else {
                this.date = setColor(safeGet(() -> {
                    CharSequence startTime = setStyle(formatSecond(raw.startTime, "yyyy.MM.dd"), Typeface.BOLD);
                    CharSequence endTime = setStyle(formatSecond(raw.stopTime, "yyyy.MM.dd"), Typeface.BOLD);
                    return String.format(Locale.getDefault(), "%s 至 %s", startTime, endTime);
                }, PlaceHolder.NULL_VALUE), TEXT_GREY_COLOR);
            }

            if (isUnknownStatus || isUnknownType) {
                this.date = "";
                this.amount = NULL_VALUE;
                this.limit = String.format(Locale.getDefault(), "未知红包%s，请更新操盘侠APP", isUnknownStatus ? "类型" : "状态");
            } else {
                this.amount = concatNoBreak(setStyle(formatMoney(raw.amount, false, 0, 2), Typeface.BOLD), setRelativeFontSize(Money_Type.getUnit(raw.moneyType), 20f / 32));
                this.limit = safeGet(() -> raw.content, PlaceHolder.NULL_VALUE);
            }
            this.amount = setColor(this.amount, (isDeprecated || isUsed) ? TEXT_GREY_COLOR : TEXT_RED_COLOR);
        }

        public static boolean isDepricated(Coupon raw) {
            return safeGet(() -> anyMatch(raw.status, Coupon.Coupon_Status_over), false);
        }

        public static boolean isUsed(Coupon raw) {
            return safeGet(() -> anyMatch(raw.status, Coupon.Coupon_Status_used), false);
        }
    }
}
