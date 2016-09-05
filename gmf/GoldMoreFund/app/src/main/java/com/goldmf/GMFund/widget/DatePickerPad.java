package com.goldmf.GMFund.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.goldmf.GMFund.R;

import java.util.Arrays;

/**
 * Created by yale on 15/8/17.
 */
public class DatePickerPad extends FrameLayout {
    public DatePickerPad(Context context) {
        this(context, null);
    }

    public DatePickerPad(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DatePickerPad(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.date_picker_pad, this);

        StringFlow flow = (StringFlow) findViewById(R.id.flow_date);
        flow.setDataSet(Arrays.asList("2015.08.04 周五", "2015.08.07 周一", "2015.08.08 周二", "2015.08.09 周三", "2015.08.10 周四", "2015.08.11 周五"));
    }
}
