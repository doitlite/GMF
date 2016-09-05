package com.goldmf.GMFund.widget;

import android.content.Context;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Created by yale on 15/8/3.
 */
public class MenuListWindow extends PopupWindow {
    public MenuListWindow(Context context) {
        super(260, 120);
        TextView contentView = new TextView(context);
        contentView.setText("你好啊");
        setContentView(contentView);
    }
}
