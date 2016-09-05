package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.annimon.stream.Stream;
import com.goldmf.GMFund.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import rx.functions.Action1;

import static com.goldmf.GMFund.extension.CalendarExtension.*;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_preDraw;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setInvisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;

/**
 * Created by yale on 15/8/4.
 */
public class CalendarPad extends RelativeLayout {
    private StringFlow mMonthFlow;
    private CalendarView mCalendarView;
    private OnDatePickListener mOnDatePickListener = OnDatePickListener.NULL;

    public CalendarPad(Context context) {
        this(context, null);
    }

    public CalendarPad(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarPad(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.calendar_pad, this, true);

        mMonthFlow = (StringFlow) findViewById(R.id.flow_date);
        mCalendarView = (CalendarView) findViewById(R.id.calendar);
        v_preDraw(mCalendarView, true, new Action1<View>() {
            @Override
            public void call(View view) {
                addSepLine();
            }
        });
        setLoading(true);
    }

    public void setOnDatePickListener(OnDatePickListener listener) {
        mOnDatePickListener = listener == null ? OnDatePickListener.NULL : listener;
    }

    private void addSepLine() {
        final int height = mCalendarView.getHeight();
        View view = new View(getContext());
        view.setBackgroundColor(getResources().getColor(R.color.gmf_sep_Line));

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dp2px(this, 1), height);
        params.addRule(CENTER_VERTICAL);
        params.addRule(ALIGN_LEFT, R.id.calendar);

        ((ViewGroup) findViewById(R.id.section_content)).addView(view, params);
    }

    public void setLoading(boolean isLoading) {
        if (isLoading) {
            v_setInvisible(this, R.id.section_content);
            v_setVisible(this, R.id.progress);
        } else {
            v_setVisible(this, R.id.section_content);
            v_setInvisible(this, R.id.progress);
        }
    }

    public void setData(final List<CalendarPageInfo> calendarPageList) {
        if (calendarPageList.isEmpty()) return;

        // initialize monthFlow
        {
            ArrayList<String> dataSet = new ArrayList<>(calendarPageList.size());
            for (CalendarPageInfo month : calendarPageList) {
                dataSet.add(month.name.toString());
            }
            mMonthFlow.setDataSet(dataSet);
            mMonthFlow.setOnItemSelectedListener((index, item) -> {
                CalendarPageInfo pageInfo = calendarPageList.get(index);
                Calendar calendar = setDate(Calendar.getInstance(), pageInfo.year, pageInfo.month, 1);
                mCalendarView.setCalendar(calendar);
            });
        }

        // initialize monthButton
        v_setClick(this, R.id.btn_month, v -> {
            int selectedPosition = mMonthFlow.getSelectedPosition();
            CalendarPageInfo pageInfo = calendarPageList.get(selectedPosition);
            mOnDatePickListener.onPickMonth(pageInfo.year, pageInfo.month);
        });

        // initialize calendar
        {
            final ColorStateList hasOrderTextColor = new ColorStateList(new int[][]{{android.R.attr.state_enabled}, {android.R.attr.state_pressed}}, new int[]{0xFF333333, 0xFFFFFFFF});
            final ColorStateList noOrderTextColor = new ColorStateList(new int[][]{{android.R.attr.state_enabled}}, new int[]{0xFFCCCCCC});
            final ColorStateList hasOrderBackgroundColor = new ColorStateList(new int[][]{{android.R.attr.state_enabled}, {android.R.attr.state_pressed}}, new int[]{0, 0xFF000000});
            final ColorStateList noOrderBackgroundColor = new ColorStateList(new int[][]{{android.R.attr.state_enabled}}, new int[]{0});

            mCalendarView.setDelegate(new CalendarView.Delegate() {
                @Override
                public ColorStateList getTextColor(int year, int month, int dayInMonth) {
                    return hasOrder(year, month, dayInMonth, calendarPageList) ? hasOrderTextColor : noOrderTextColor;
                }

                @Override
                public ColorStateList getBackgroundColor(int year, int month, int dayInMonth) {
                    return hasOrder(year, month, dayInMonth, calendarPageList) ? hasOrderBackgroundColor : noOrderBackgroundColor;
                }
            });
            mCalendarView.setOnItemClickListener((year, month, dayInMonth) -> {
                if (hasOrder(year, month, dayInMonth, calendarPageList))
                    mOnDatePickListener.onPickDate(year, month, dayInMonth);
            });
            CalendarPageInfo pageInfo = Stream.of(calendarPageList).findFirst().get();
            mCalendarView.setCalendar(setDate(Calendar.getInstance(), pageInfo.year, pageInfo.month, pageInfo.days[0]));
        }
    }

    private boolean hasOrder(int yeah, int month, int dayInMonth, List<CalendarPageInfo> monthList) {
        for (CalendarPageInfo m : monthList) {
            if (m.year == yeah && m.month == month) {
                for (int d : m.days) {
                    if (d == dayInMonth) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    public static class CalendarPageInfo {
        public CharSequence name;
        public int year;
        public int month;
        public int[] days;
    }

    public interface OnDatePickListener {
        void onPickMonth(int year, int month);

        void onPickDate(int year, int month, int day);

        OnDatePickListener NULL = new OnDatePickListener() {
            @Override
            public void onPickMonth(int year, int month) {
            }

            @Override
            public void onPickDate(int year, int month, int day) {
            }
        };
    }
}
