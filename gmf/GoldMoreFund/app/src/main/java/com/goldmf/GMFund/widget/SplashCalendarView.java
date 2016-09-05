package com.goldmf.GMFund.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.util.SecondUtil;

import java.util.Calendar;
import java.util.Random;

import static com.goldmf.GMFund.extension.ViewExtension.v_findView;

/**
 * Created by cupide on 16/4/13.
 */
public class SplashCalendarView extends RelativeLayout {

    private TextView mDayLabel;     //日期
    private TextView mWeekNameLabel;    //星期
    private TextView mCalendarLabel;    //年月
    private TextView mInfosLabel;    //信息

    private String strInfos;

    public SplashCalendarView(Context context) {
        this(context, null);
    }

    public SplashCalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SplashCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.widget_calendar_view, this);

        // bind child views
        mDayLabel = v_findView(this, R.id.label_text_day);
        mWeekNameLabel = v_findView(this, R.id.label_text_week);
        mCalendarLabel = v_findView(this, R.id.label_text_calendar);
        mInfosLabel = v_findView(this, R.id.label_text_infos);

        if(strInfos != null){
            mInfosLabel.setText(strInfos);
        }

        mDayLabel.setText(String.valueOf(SecondUtil.currentCalendar().get(Calendar.DAY_OF_MONTH)));
        mWeekNameLabel.setText(SecondUtil.currentWeek());
        String strCalendar = SecondUtil.currentCalendar().get(Calendar.YEAR) + "年";
        if(SecondUtil.currentCalendar().get(Calendar.MONTH) < 10){
            strCalendar += " ";
        }
        strCalendar += (SecondUtil.currentCalendar().get(Calendar.MONTH)+1) + "月";

        mCalendarLabel.setText(strCalendar);

    }

    public void setInfos(String infos){
        strInfos = infos;
        if(mInfosLabel != null){
            mInfosLabel.setText(strInfos);
        }
    }
}