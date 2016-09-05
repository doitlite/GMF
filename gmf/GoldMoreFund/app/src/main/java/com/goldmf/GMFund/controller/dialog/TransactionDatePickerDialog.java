package com.goldmf.GMFund.controller.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewTreeObserver;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.widget.CalendarPad;
import com.goldmf.GMFund.widget.CalendarPad.CalendarPageInfo;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static com.goldmf.GMFund.extension.CalendarExtension.getDate;
import static com.goldmf.GMFund.extension.CalendarExtension.getTimeInSecond;
import static com.goldmf.GMFund.extension.CalendarExtension.setDate;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;

/**
 * Created by yale on 15/8/20.
 */
public class TransactionDatePickerDialog extends BasicDialog {

    private CalendarPad mCalendarPad;
    private FundBrief mFund;

    public TransactionDatePickerDialog(Context context, FundBrief fund) {
        super(context, R.style.GMFDialog);
        mFund = fund;
        mCalendarPad = new CalendarPad(context);
        setContentView(mCalendarPad);
        getWindow().setLayout(-1, -2);
        getWindow().setGravity(Gravity.BOTTOM);

        // bind child views
        v_setClick(this, R.id.btn_close, this::animateDismiss);

        getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getWindow().getDecorView().setTranslationY(getWindow().getDecorView().getHeight());
                animateShow();
                getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });

//        performFetchTransactionTimeList();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            animateDismiss();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public CalendarPad getCalendarPad() {
        return mCalendarPad;
    }

//    private void performFetchTransactionTimeList() {
//        requestFetchTransactionTimeList(mFund)
//                .map(data -> isSuccess(data) ? map(data, fromLongListToCalendarPageList(data.data)) : castToList(data, CalendarPageInfo.class))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(data -> {
//                    if (isSuccess(data)) {
//                        mCalendarPad.setData(data.data);
//                        mCalendarPad.setLoading(false);
//                    } else {
//                        showToast(getContext(), "请求失败");
//                        animateDismiss();
//                    }
//                });
//    }

//    private static Observable<MResultsInfo<List<Long>>> requestFetchTransactionTimeList(final FundBrief source) {
//        final String monthRange = getCombinedMonthString(source);
//        return Observable.create(subscriber -> StockManager.getInstance().getTradeDate(source.index, monthRange, ObservableExtension.createObservableListMResult(subscriber)));
//    }

    private static String getCombinedMonthString(FundBrief source) {
        StringBuilder sb = new StringBuilder();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(source.startTime * 1000);
        int[] startDate = getDate(calendar);
        calendar.setTimeInMillis(source.stopTime * 1000);
        int[] endDate = getDate(calendar);

        int minYear = startDate[0];
        int maxYear = endDate[0];
        for (int currentYear = minYear; currentYear <= maxYear; currentYear++) {
            int currentMonth = currentYear == minYear ? startDate[1] : 0;
            int maxMonth = currentYear == maxYear ? endDate[1] : 11;
            do {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(getTimeInSecond(setDate(calendar, currentYear, currentMonth, 1)));
                currentMonth++;
            } while (currentMonth <= maxMonth);
        }

        return sb.toString();
    }

    private void animateShow() {
        ObjectAnimator.ofFloat(getWindow().getDecorView(), "translationY", 0).setDuration(200).start();
    }

    public void animateDismiss() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(getWindow().getDecorView(), "translationY", getWindow().getDecorView().getHeight());
        animator.setDuration(200);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                dismiss();
            }
        });
        animator.start();
    }

    private List<CalendarPageInfo> fromLongListToCalendarPageList(List<Long> dates) {
        Calendar calendar = Calendar.getInstance();
        LinkedList<CalendarPageInfo> ret = new LinkedList<>();
        CalendarPageInfo item = new CalendarPageInfo();
        boolean isInit = false;
        int[] tmp_days = new int[255];
        int current_idx = 0;
        for (long date : dates) {
            calendar.setTimeInMillis(date * 1000);
            int[] dateArray = getDate(calendar);
            if (!isInit) {
                current_idx = 0;
                item.year = dateArray[0];
                item.month = dateArray[1];
                item.name = String.valueOf((item.month + 1) + "月");
                tmp_days[current_idx] = dateArray[2];
                isInit = true;
            } else {
                int latestDay = tmp_days[current_idx];
                if (latestDay < dateArray[2]) {
                    tmp_days[++current_idx] = dateArray[2];
                } else {
                    item.days = Arrays.copyOf(tmp_days, current_idx + 1);
                    ret.add(item);
                    item = new CalendarPageInfo();
                    current_idx = 0;
                    item.year = dateArray[0];
                    item.month = dateArray[1];
                    item.name = String.valueOf((item.month + 1) + "月");
                    tmp_days[current_idx] = dateArray[2];
                }
            }
        }

        //item初始化完毕但是没有放进List
        if (isInit) {
            item.days = Arrays.copyOf(tmp_days, current_idx + 1);
            ret.add(item);
        }

        return ret;
    }
}
