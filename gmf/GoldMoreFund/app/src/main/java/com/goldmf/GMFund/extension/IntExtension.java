package com.goldmf.GMFund.extension;

import yale.extension.common.Range;

/**
 * Created by yale on 16/4/13.
 */
public class IntExtension {
    public static boolean containOption(int value, int option) {
        return (value & option) >= option;
    }

    public static boolean inRange(Integer target, Integer min, Integer max) {
        if (target == null)
            return false;

        min = min == null ? Integer.MIN_VALUE : min;
        max = max == null ? Integer.MAX_VALUE : max;

        return target >= min && target <= max;

    }

    public static boolean anyMatch(Integer target, Integer... values) {
        for (Integer value : values) {
            if (value.equals(target)) return true;
        }
        return false;
    }

    public static boolean notMatch(Integer target, Integer... values) {
        return !anyMatch(target, values);
    }
}
