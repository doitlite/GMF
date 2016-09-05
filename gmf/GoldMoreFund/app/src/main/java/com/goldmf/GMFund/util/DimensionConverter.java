package com.goldmf.GMFund.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by yale on 15/7/21.
 */
public class DimensionConverter {
    private DimensionConverter() {
    }

    public static float dp2px(Context context, float dp) {
        Resources resources = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static float sp2px(Context context, float sp) {
        Resources resources = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.getDisplayMetrics());
    }

    public static float px2dp(Context context, float px) {
        Resources resources = context.getResources();
        return px / resources.getDisplayMetrics().density;
    }

}
