package com.goldmf.GMFund.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.IResultReceiver;

import com.esotericsoftware.kryo.NotNull;

import java.math.BigDecimal;

/**
 * Created by yale on 16/3/18.
 */
@SuppressWarnings("ConstantConditions")
public class NumberUtil {
    private NumberUtil() {
    }

    public static boolean hasDecimal(Double number) {
        if (number == null) {
            return false;
        }

        return new BigDecimal(number).setScale(0, BigDecimal.ROUND_DOWN).doubleValue() != number;
    }

    public static Integer toInteger(String str, @NonNull Integer defaultValue) {
        try {
            return Integer.valueOf(str);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    @Nullable
    public static Integer toNullableInteger(String str) {
        return toInteger(str, null);
    }

    public static Long toLong(String str, @NonNull Long defaultValue) {
        try {
            return Long.valueOf(str);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    @Nullable
    public static Long toNullableLong(String str) {
        return toLong(str, null);
    }


    public static Float toFloat(String str, @NonNull Float defaultValue) {
        try {
            return Float.valueOf(str);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    @Nullable
    public static Float toNullableFloat(String str) {
        return toFloat(str, null);
    }


    public static Double toDouble(String str, @NonNull Double defaultValue) {
        try {
            return Double.valueOf(str);
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    @Nullable
    public static Double toNullableDouble(String str) {
        return toDouble(str, null);
    }
}
