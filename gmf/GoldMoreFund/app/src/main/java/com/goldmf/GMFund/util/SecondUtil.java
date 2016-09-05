package com.goldmf.GMFund.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by cupide on 15/8/21.
 */
public class SecondUtil {

    private static long diffTime = 0;

    public static long currentSecond(){
        return System.currentTimeMillis()/1000 + diffTime;
    }

    public static Date currentDate(){
        return new Date(System.currentTimeMillis() + diffTime*1000);
    }

    public static void setServerTime(long serverTime){

        diffTime = serverTime - System.currentTimeMillis()/1000;
    }

    private static Calendar calendar = Calendar.getInstance();

    public static Calendar currentCalendar(){
        calendar.setTimeInMillis(SecondUtil.currentSecond() * 1000);
        return calendar;
    }

    public static String currentWeek() {
        String[] weekOfDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekOfDays[w];
    }
}
