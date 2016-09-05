package com.goldmf.GMFund.extension;

import java.util.Calendar;

/**
 * Created by yale on 15/8/26.
 */
public class CalendarExtension {
    public static Calendar setDate(Calendar calendar, int year, int month, int day) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Calendar setYear(Calendar calendar, int year) {
        calendar.set(Calendar.YEAR, year);
        return calendar;
    }

    public static Calendar setMonth(Calendar calendar, int month) {
        calendar.set(Calendar.MONTH, month);
        return calendar;
    }

    public static Calendar setDay(Calendar calendar, int day) {
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar;
    }

    public static Calendar addYear(Calendar calendar, int year) {
        calendar.add(Calendar.YEAR, year);
        return calendar;
    }

    public static Calendar addMonth(Calendar calendar, int month) {
        calendar.add(Calendar.MONTH, month);
        return calendar;
    }

    public static Calendar addDay(Calendar calendar, int day) {
        calendar.add(Calendar.DAY_OF_MONTH, day);
        return calendar;
    }

    public static long getTimeInSecond(Calendar calendar) {
        return calendar.getTimeInMillis() / 1000;
    }

    public static int[] getDate(Calendar calendar) {
        int[] ret = new int[3];
        ret[0] = calendar.get(Calendar.YEAR);
        ret[1] = calendar.get(Calendar.MONTH);
        ret[2] = calendar.get(Calendar.DAY_OF_MONTH);
        return ret;
    }
}
