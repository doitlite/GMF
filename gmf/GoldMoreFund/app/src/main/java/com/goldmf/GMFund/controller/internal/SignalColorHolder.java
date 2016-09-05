package com.goldmf.GMFund.controller.internal;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;

import com.goldmf.GMFund.R;

/**
 * Created by yale on 15/8/27.
 */
@SuppressWarnings("deprecation")
public class SignalColorHolder {
    private SignalColorHolder() {
    }

    public static int RED_COLOR = 0;
    public static int BLACK_COLOR = 0;
    public static int GREEN_COLOR = 0;
    public static int BLUE_COLOR = 0;
    public static int WHITE_COLOR = 0;
    public static int GREY_COLOR = 0;
    public static int LIGHT_GREY_COLOR = 0;
    public static int YELLOW_COLOR = 0;
    public static int ORANGE_COLOR = 0;
    public static int PURPLE_COLOR = 0;

    public static int STATUS_BAR_BLACK = 0;

    public static int LINE_SEP_COLOR = 0;
    public static int LINE_BORDER_COLOR = 0;

    public static int TEXT_WHITE_COLOR = 0;
    public static int TEXT_RED_COLOR = 0;
    public static int TEXT_GREEN_COLOR = 0;
    public static int TEXT_BLUE_COLOR = 0;
    public static int TEXT_BLACK_COLOR = 0;
    public static int TEXT_GREY_COLOR = 0;
    public static int TEXT_GREY_LIGHT_COLOR = 0;
    public static int TEXT_PURPLE_COLOR = 0;
    public static int TEXT_GOLD_COLOR = 0;
    public static int TEXT_SILVER_COLOR = 0;
    public static int TEXT_COPPER_COLOR = 0;
    public static int TEXT_IRON_COLOR = 0;

    public static int RISE_COLOR = 0;
    public static int FALL_COLOR = 0;
    public static int BUY_COLOR = 0;
    public static int SELL_COLOR = 0;

    public static int LINE_YELLOW_COLOR = 0;
    public static int LINE_BLUE_COLOR = 0;
    public static int LINE_PURPLE_COLOR = 0;
    public static int LINE_BACKGROUND_COLOR = 0;

    public static void init(Context context) {
        Resources res = context.getResources();

        RED_COLOR = res.getColor(R.color.gmf_red);
        BLACK_COLOR = res.getColor(R.color.gmf_black);
        GREEN_COLOR = res.getColor(R.color.gmf_green);
        BLUE_COLOR = res.getColor(R.color.gmf_blue);
        WHITE_COLOR = res.getColor(R.color.gmf_white);
        GREY_COLOR = res.getColor(R.color.gmf_grey);
        LIGHT_GREY_COLOR = res.getColor(R.color.gmf_light_grey);
        YELLOW_COLOR = res.getColor(R.color.gmf_yellow);
        ORANGE_COLOR = res.getColor(R.color.gmf_orange);
        PURPLE_COLOR = res.getColor(R.color.gmf_purple);

        STATUS_BAR_BLACK = res.getColor(R.color.gmf_status_bar_black);

        LINE_SEP_COLOR = res.getColor(R.color.gmf_sep_Line);
        LINE_SEP_COLOR = res.getColor(R.color.gmf_border_line);

        TEXT_WHITE_COLOR = res.getColor(R.color.gmf_text_white);
        TEXT_RED_COLOR = res.getColor(R.color.gmf_text_red);
        TEXT_BLUE_COLOR = res.getColor(R.color.gmf_text_blue);
        TEXT_GREEN_COLOR = res.getColor(R.color.gmf_text_green);
        TEXT_BLACK_COLOR = res.getColor(R.color.gmf_text_black);
        TEXT_GREY_COLOR = res.getColor(R.color.gmf_text_grey);
        TEXT_GREY_LIGHT_COLOR = res.getColor(R.color.gmf_text_light_grey);
        TEXT_PURPLE_COLOR = res.getColor(R.color.gmf_text_purple);
        TEXT_GOLD_COLOR = res.getColor(R.color.gmf_text_gold);
        TEXT_SILVER_COLOR = res.getColor(R.color.gmf_text_silver);
        TEXT_COPPER_COLOR = res.getColor(R.color.gmf_text_copper);
        TEXT_IRON_COLOR = res.getColor(R.color.gmf_text_iron);

        RISE_COLOR = res.getColor(R.color.gmf_text_red);
        FALL_COLOR = res.getColor(R.color.gmf_text_green);
        BUY_COLOR = res.getColor(R.color.gmf_blue);
        SELL_COLOR = res.getColor(R.color.gmf_orange);

        LINE_YELLOW_COLOR = res.getColor(R.color.gmf_line_yellow);
        LINE_BLUE_COLOR = res.getColor(R.color.gmf_line_blue);
        LINE_PURPLE_COLOR = res.getColor(R.color.gmf_line_purple);
        LINE_BACKGROUND_COLOR = res.getColor(R.color.gmf_line_grey);
    }

    public static int getIncomeTextColor(@Nullable Double income) {
        return getIncomeTextColor(income, RISE_COLOR);
    }

    public static int getIncomeTextColor(@Nullable Double income, int defaultColor) {
        if (income == null || income == 0)
            return defaultColor;
        else if (income > 0)
            return RISE_COLOR;
        else
            return FALL_COLOR;
    }

    public static int getIncomeBgColor(@Nullable Double income) {
        if (income == null || income >= 0) {
            return RISE_COLOR;
        } else {
            return FALL_COLOR;
        }
    }

    public static int getNetValueTextColor(@Nullable Double netValue) {
        if (netValue == null || netValue >= 1)
            return RISE_COLOR;
        else
            return FALL_COLOR;
    }

    public static int getFiveOrderPriceColor(@Nullable Double price, @Nullable Double prevClosePrice) {
        if (price == null || prevClosePrice == null)
            return TEXT_GREY_COLOR;

        if (price.equals(prevClosePrice))
            return TEXT_GREY_COLOR;
        if (price > prevClosePrice)
            return RISE_COLOR;
        else
            return FALL_COLOR;
    }

    public static int getAwardTextColor(@Nullable Double award) {
        if (award == null || award > 0)
            return TEXT_RED_COLOR;
        else
            return TEXT_BLACK_COLOR;
    }
}
