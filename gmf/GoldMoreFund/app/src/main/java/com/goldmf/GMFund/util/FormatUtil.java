package com.goldmf.GMFund.util;

import android.text.SpannableStringBuilder;

import com.goldmf.GMFund.base.StringPair;
import com.goldmf.GMFund.controller.internal.SignalColorHolder;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;

/**
 * Created by cupide on 15/8/6.
 */
public class FormatUtil {

    public static double formatRatio(Double ratio, double sep, int scale){
        double temp = Math.pow(10, scale) * 100;
        return (ratio*temp > sep*temp ? Math.floor(ratio*temp) : Math.ceil(ratio*temp))/temp;
    }

    public static String formatNumber(Double number, boolean symbol, int scale) {
        if (number == null) {
            return "--";
        }

        DecimalFormat df = new DecimalFormat(appendScale("###########0", scale));
        df.setRoundingMode(RoundingMode.HALF_UP);
        String str = df.format(number);
        if (symbol && number > 0)
            return "+" + str;
        return str;
    }

    public static String formatMoney(Integer money, boolean symbol, int scale) {
        if (money == null)
            return "--";
        DecimalFormat df = new DecimalFormat(appendScale("###,###,###,##0", scale));
        df.setRoundingMode(RoundingMode.HALF_UP);
        String str = df.format(money);
        if (symbol && money > 0)
            return "+" + str;
        return str;
    }

    public static String formatMoney(Long money, boolean symbol, int scale) {
        if (money == null)
            return "--";
        DecimalFormat df = new DecimalFormat(appendScale("###,###,###,##0", scale));
        df.setRoundingMode(RoundingMode.HALF_UP);
        String str = df.format(money);
        if (symbol && money > 0)
            return "+" + str;
        return str;
    }

    public static String formatMoney(Float money, boolean symbol, int scale) {
        if (money == null) {
            return "--";
        }
        DecimalFormat df = new DecimalFormat(appendScale("###,###,###,##0", scale));
        df.setRoundingMode(RoundingMode.HALF_UP);
        String str = df.format(money);
        if (symbol && money > 0)
            return "+" + str;
        return str;
    }

    public static String formatMoney(Double money, boolean symbol, int scale) {
        if (money == null) {
            return "--";
        }

        DecimalFormat df = new DecimalFormat(appendScale("###,###,###,##0", scale));
        df.setRoundingMode(RoundingMode.HALF_UP);
        String str = df.format(money);
        if (symbol && money > 0)
            return "+" + str;
        return str;
    }


    private static String appendScale(String source, int scale) {
        if (scale <= 0)
            return source;
        else {
            StringBuilder sb = new StringBuilder(source).append(".");
            for (int i = 0; i < scale; i++) {
                sb.append(0);
            }
            return sb.toString();
        }
    }

    public static String formatMoney(Double money, boolean symbol, int minScale, int maxScale) {
        if (money == null) {
            return "--";
        }

        StringBuilder patternBuilder = new StringBuilder("###,###,###,##0.");
        for (int i = 0; i < minScale; i++) {
            patternBuilder.append("0");
        }
        for (int i = 0; i < maxScale - minScale; i++) {
            patternBuilder.append("#");
        }

        DecimalFormat df = new DecimalFormat(patternBuilder.toString());
        df.setRoundingMode(RoundingMode.HALF_UP);
        String str = df.format(money);
        if (symbol && money > 0)
            return "+" + str;
        return str;
    }

    public static String formatRatio(Double ratio, boolean symbol, int scale) {
        if (ratio == null)
            return "-%";
        DecimalFormat df;
        if (scale == 0) {
            df = new DecimalFormat("0%");
        } else {
            StringBuilder sb = new StringBuilder("0").append(".");
            for (int i = 0; i < scale; i++) {
                sb.append("0");
            }
            sb.append("%");
            df = new DecimalFormat(sb.toString());
        }
        df.setRoundingMode(RoundingMode.HALF_UP);
        String str = df.format(ratio);
        if (symbol && ratio > 0)
            return "+" + str;
        return str;
    }

    public static String formatRatio(Double ratio, boolean symbol, int minScale, int maxScale) {
        if (ratio == null)
            return "-%";

        StringBuilder patternBuilder = new StringBuilder("0.");
        for (int i = 0; i < minScale; i++) {
            patternBuilder.append("0");
        }
        for (int i = 0; i < maxScale - minScale; i++) {
            patternBuilder.append("#");
        }

        patternBuilder.append("%");

        DecimalFormat df = new DecimalFormat(patternBuilder.toString());
        df.setRoundingMode(RoundingMode.HALF_UP);
        if (symbol && ratio > 0) {
            return "+" + df.format(ratio);
        } else {
            return df.format(ratio);
        }
    }

    public static String computeBigNumberUnit(Long bigNumber) {
        if (bigNumber == null)
            return "--";

        if (bigNumber >= 100000000) {
            return "亿";
        } else if (bigNumber >= 10000) {
            return "万";
        }
        return "";
    }

    public static String computeBigNumberUnit(Double bigNumber) {
        if (bigNumber == null)
            return "--";

        if (bigNumber >= 100000000) {
            return "亿";
        } else if (bigNumber >= 10000) {
            return "万";
        }
        return "";
    }

    public static String computeBiggerNumberUnit(Double bigNumber) {
        if (bigNumber == null)
            return "--";

        if (bigNumber >= 1000000000) {
            return "亿";
        } else if (bigNumber >= 10000) {
            return "万";
        }
        return "";
    }

    public static String formatBigNumber(Long bigNumber, boolean symbol, int scale) {
        return formatBigNumber(bigNumber, symbol, scale, scale, true);
    }

    public static String formatBigNumber(Long bigNumber, boolean symbol, int minScale, int maxScale) {
        return formatBigNumber(bigNumber, symbol, minScale, maxScale, true);
    }

    public static String formatBigNumber(Long bigNumber, boolean symbol, int minScale, int maxScale, boolean appendUnit) {
        if (bigNumber == null)
            return "--";

        long absBigNumber = Math.abs(bigNumber);
        if (absBigNumber >= 1000000000) {
            return getFormatBigNumber(symbol, bigNumber, 100000000, minScale, maxScale, appendUnit ? "亿" : "");
        }
        if (absBigNumber >= 100000) {
            return getFormatBigNumber(symbol, bigNumber, 10000, minScale, maxScale, appendUnit ? "万" : "");
        } else {
            return getFormatBigNumber(symbol, bigNumber, 1, minScale, maxScale, "");
        }
    }

    public static String formatBigNumber(Double bigNumber, boolean symbol, int scale) {
        return formatBigNumber(bigNumber, symbol, scale, scale, true);
    }

    public static String formatBigNumber(Double bigNumber, boolean symbol, int minScale, int maxScale) {
        return formatBigNumber(bigNumber, symbol, minScale, maxScale, true);
    }

    public static String formatBigNumber(Double bigNumber, boolean symbol, int minScale, int maxScale, boolean appendUnit) {
        if (bigNumber == null)
            return "--";


        double absBigNumber = Math.abs(bigNumber);
        if (absBigNumber >= 1000000000) {
            return getFormatBigNumber(symbol, bigNumber, 100000000, minScale, maxScale, appendUnit ? "亿" : "");
        }
        if (absBigNumber >= 100000) {
            return getFormatBigNumber(symbol, bigNumber, 10000, minScale, maxScale, appendUnit ? "万" : "");
        } else {
            return getFormatBigNumber(symbol, bigNumber, 1, minScale, maxScale, "");
        }
    }

    public static String formatBigNumber(Double bigNumber, long divider, int scale) {
        if (bigNumber == null)
            return "--";
        return getFormatBigNumber(bigNumber, divider, scale);
    }

    public static String formatBiggerNumber(Double biggerNumber, boolean symbol, int minScale, int maxScale, boolean appendUnit) {
        if (biggerNumber == null)
            return "--";

        double absBiggerNumber = Math.abs(biggerNumber);
        if (absBiggerNumber >= 1000000000) {
            return getFormatBiggerNumber(symbol, biggerNumber, 100000000, minScale, maxScale, appendUnit ? " 亿" : "");
        }
        return getFormatBiggerNumber(symbol, biggerNumber, 1, minScale, maxScale, "");
    }

    public static String formatStockCount(Long count, int minScale, int maxScale, boolean appendUnit) {
        if (count == null)
            return "--";

        if (count >= 1000) {
            return getFormatBigNumber(false, count, 1000, minScale, maxScale, "K");
        } else if (count >= 1000000) {
            return getFormatBigNumber(false, count, 1000000, minScale, maxScale, "M");
        } else {
            return count + "";
        }
    }

    private static String getFormatBigNumber(Double bigNumber, long divider, int minScale) {
        StringBuilder sb = new StringBuilder();
        StringBuilder patternBuilder = new StringBuilder("#.");
        for (int i = 0; i < minScale; i++) {
            patternBuilder.append("#");
        }
        DecimalFormat df = new DecimalFormat(patternBuilder.toString());
        df.setRoundingMode(RoundingMode.HALF_UP);
        sb.append(df.format(bigNumber / divider));
        return sb.toString();
    }

    private static String getFormatBigNumber(boolean symbol, long bigNumber, long divider, int minScale, int maxScale, String unit) {
        StringBuilder sb = new StringBuilder();
        if (symbol) {
            sb.append(bigNumber > 0 ? "+" : "");
        }

        StringBuilder patternBuilder = new StringBuilder("###,###,###,##0.");
        for (int i = 0; i < minScale; i++) {
            patternBuilder.append("0");
        }
        for (int i = 0; i < maxScale - minScale; i++) {
            patternBuilder.append("#");
        }

        DecimalFormat df = new DecimalFormat(patternBuilder.toString());
        df.setRoundingMode(RoundingMode.HALF_UP);
        sb.append(df.format((double) bigNumber / divider));
        sb.append(unit);

        return sb.toString();
    }


    private static String getFormatBigNumber(boolean symbol, double bigNumber, long divider, int minScale, int maxScale, String unit) {
        StringBuilder sb = new StringBuilder();
        if (symbol) {
            sb.append(bigNumber > 0 ? "+" : "");
        }
        StringBuilder patternBuilder = new StringBuilder("###,###,###,##0");
        if (minScale > 0 || maxScale > 0) {
            patternBuilder.append(".");
        }
        for (int i = 0; i < minScale; i++) {
            patternBuilder.append("0");
        }
        for (int i = 0; i < maxScale - minScale; i++) {
            patternBuilder.append("#");
        }

        DecimalFormat df = new DecimalFormat(patternBuilder.toString());
        df.setRoundingMode(RoundingMode.HALF_UP);
        sb.append(df.format(bigNumber / divider));
        sb.append(unit);

        return sb.toString();
    }

    private static String getFormatBiggerNumber(boolean symbol, double bigNumber, long divider, int minScale, int maxScale, String unit) {
        StringBuilder sb = new StringBuilder();
        if (symbol) {
            sb.append(bigNumber > 0 ? "+" : "");
        }

        StringBuilder patternBuilder = new StringBuilder("###,###,###,##0");
        if (minScale > 0 || maxScale > 0) {
            patternBuilder.append(".");
        }
        for (int i = 0; i < minScale; i++) {
            patternBuilder.append("0");
        }
        for (int i = 0; i < maxScale - minScale; i++) {
            patternBuilder.append("#");
        }

        DecimalFormat df = new DecimalFormat(patternBuilder.toString());
        df.setRoundingMode(RoundingMode.HALF_UP);
        sb.append(df.format(bigNumber / divider));
        sb.append(unit);

        return sb.toString();
    }

    public static String formatSecond() {
        return formatSecond(SecondUtil.currentSecond());
    }

    public static String formatSecond(String format) {
        return formatSecond(SecondUtil.currentSecond(), format);
    }

    public static String formatSecond(long second) {
        return formatSecond(second, "yyyy-MM-dd HH:mm:ss");
    }

    public static String formatSecond(long second, String format) {
        Date timeDate = new Date(second * 1000);
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        return formatter.format(timeDate);
    }

    public static long formatSecond(String timeDate, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        try {
            Date date = formatter.parse(timeDate);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static String getWeek(long second) {
        String[] weekOfDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(second * 1000));
        int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekOfDays[w];
    }

    public static String formateRemainingDays(double remainingDays) {

        //天、小时、一小时内、--
        if (remainingDays >= 1) {
            return String.format("%d天", (int) (remainingDays));
        } else if (remainingDays >= 1.0f / 24) {
            return String.format("%d小时", (int) (remainingDays * 24));
        } else {
            return "少于1小时";
        }
    }

    public static String formateRunningDays(double runningDays) {

        if (runningDays >= 1) {
            return String.format("%d天", (int) (runningDays));
        } else if (runningDays >= 1.0f / 24) {
            return String.format("%d小时", (int) (runningDays * 24));
        } else {
            return "少于1小时";
        }
    }


    public static String formatTimeByNow(long second) {

        long currentSecond = SecondUtil.currentSecond();

        long distSecond = currentSecond - second;
        if (distSecond > 0) {
            //过去的时间
            if (distSecond < 60) {
                return "刚刚";
            } else if (distSecond < (30 * 60)) {
                return String.format("%d分钟前", distSecond / 60 + 1);
            } else {

                Calendar date = Calendar.getInstance();
                date.setTime(new Date(second * 1000));

                Calendar current = Calendar.getInstance();
                current.setTime(new Date(currentSecond * 1000));

                Calendar yesterday = Calendar.getInstance();
                yesterday.setTime(new Date((currentSecond - 24 * 3600) * 1000));

                if (date.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR)
                        && date.get(Calendar.MONTH) == yesterday.get(Calendar.MONTH)
                        && date.get(Calendar.DAY_OF_MONTH) == yesterday.get(Calendar.DAY_OF_MONTH)) {
                    return formatSecond(second, "昨天 HH:mm");
                }

                if (date.get(Calendar.YEAR) == current.get(Calendar.YEAR)) {
                    if (date.get(Calendar.WEEK_OF_YEAR) == current.get(Calendar.WEEK_OF_YEAR)) {
                        //本周
                        if (date.get(Calendar.DAY_OF_WEEK) == current.get(Calendar.DAY_OF_WEEK)) {
                            return formatSecond(second, "今天 HH:mm");
                        } else {
                            return formatSecond(second, getWeek(second) + " HH:mm");
                        }
                    } else {
                        return formatSecond(second, "MM月dd日");
                    }
                }

                return formatSecond(second, "yyyy.MM.dd");
            }
        } else {
            //未来的时间
            return formatSecond(second, "yyyy.MM.dd");
        }
    }

    public static StringPair[] formatRemainingTimeOverMonth(double timeInSecond) {
        int days = Math.max((int) (timeInSecond / (60 * 60 * 24)), 0);
        int month = days / 30;
        if (days % 30 >= 20)
            month++;
        int year = Math.max(month / 12, 0);
        if (year > 0) {
            if (month % 12 != 0) {
                return new StringPair[]{StringPair.create("" + year, "年"), StringPair.create("" + month % 12, "个月")};
            } else {
                return new StringPair[]{StringPair.create("" + year, "年")};
            }
        }

        return new StringPair[]{StringPair.create("" + month, "个月")};
    }

    public static StringPair[] formatRemainingTimeOverDay(double timeInSecond) {

        if(timeInSecond < 0)
            timeInSecond = 0;

        int hours = (int) (timeInSecond / (60 * 60));
        int minutes = (int) ((timeInSecond - hours * 60 * 60) / 60);
        int seconds = (int) (timeInSecond % 60);

        ArrayList<StringPair> list = new ArrayList<>();
        list.add(StringPair.create(String.format("%02d", hours), "小时 "));
        list.add(StringPair.create(String.format("%02d", minutes), "分钟 "));
        list.add(StringPair.create(String.format("%02d", seconds), "秒 "));

        StringPair[] ret = new StringPair[list.size()];
        return list.toArray(ret);
    }

    public static String ESCAPE_NIL_STRING(String string) {
        if (string == null || string.length() == 0)
            return "";
        return string;
    }

    public static String ESCAPE_EMPTY_STRING(String string1, String string2) {
        if (string1 == null || string1.length() == 0)
            return ESCAPE_NIL_STRING(string2);
        return string1;
    }

    public static SpannableStringBuilder formatStockCode(String source, String input) {
        SpannableStringBuilder sb = new SpannableStringBuilder(source);
        sb = setColor(sb, TEXT_GREY_COLOR);

        if (source.contains(input)) {
            int index = source.indexOf(input);
            sb.replace(index, index + input.length(), setColor(input, SignalColorHolder.RED_COLOR));
        }
        return sb;
    }
}
