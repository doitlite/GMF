package com.goldmf.GMFund.extension;

import android.text.Editable;
import android.text.TextUtils;
import android.widget.TextView;

import com.goldmf.GMFund.util.FormatUtil;

import rx.functions.Func1;

/**
 * Created by yale on 15/9/8.
 */
public class StringExtension {

    private StringExtension() {
    }

    public static boolean isEmpty(CharSequence... targets) {
        return !notEmpty(targets);
    }

    public static boolean notEmpty(CharSequence... targets) {
        for (CharSequence target : targets) {
            if (TextUtils.isEmpty(target))
                return false;
        }

        return true;
    }

    public static boolean anyMatch(String target, String... values) {
        for (String value : values) {
            if (value.equals(target)) return true;
        }
        return false;
    }

    public static boolean anyContain(String target, String... values) {
        for (String value : values) {
            if (target.contains(value)) return true;
        }
        return false;
    }

    public static boolean notMatch(String target, String... values) {
        return !anyMatch(target, values);
    }

    public static String map(TextView textView, Func1<String, String> mapper) {
        return map(textView.getText().toString(), mapper);
    }

    public static String map(Editable editable, Func1<String, String> mapper) {
        return map(editable.toString(), mapper);
    }

    public static String map(String str, Func1<String, String> mapper) {
        return mapper.call(str);
    }

    public static Func1<String, String> formatMoneyTransformer(boolean symbol, int scale) {
        return str -> {
            if (TextUtils.isEmpty(str))
                return str;

            String trimStr = str.trim();
            if (isNumberString(trimStr)) {
                double value = Double.valueOf(trimStr);
                return FormatUtil.formatMoney(value, symbol, scale);
            }

            return str;
        };
    }

    public static Func1<String, String> formatMoneyTransformer(boolean symbol, int minScale, int maxScale) {
        return str -> {
            if (TextUtils.isEmpty(str))
                return str;

            String trimStr = str.trim();
            if (isNumberString(trimStr)) {
                double value = Double.valueOf(trimStr);
                return FormatUtil.formatMoney(value, symbol, minScale, maxScale);
            }

            return str;
        };
    }

    public static Func1<String, String> normalMoneyTransformer() {
        return str -> {
            if (TextUtils.isEmpty(str)) return str;

            String trimStr = str.trim();
            trimStr = trimStr.replaceAll(",", "");
            if (isNumberString(trimStr)) {
                return trimStr;
            }

            return str;
        };
    }

    public static Func1<String, String> formatBankCardNoTransformer() {
        return str -> {
            if (TextUtils.isEmpty(str))
                return str;

            String trimStr = str.replaceAll(" ", "");
            if (isNumberString(trimStr)) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < trimStr.length(); i++) {
                    char aChar = trimStr.charAt(i);
                    sb.append(aChar);
                    if (i % 4 == 3) {
                        sb.append(" ");
                    }
                }
                return sb.toString();
            }

            return str;
        };
    }

    public static Func1<String, String> normalBankCardNoTransformer() {
        return str -> {
            if (TextUtils.isEmpty(str))
                return str;

            return str.replaceAll(" ", "");
        };
    }

    public static Func1<String, String> encryptBankCardNoTransformer() {
        return str -> {
            if (TextUtils.isEmpty(str))
                return str;

            String trimStr = str.replaceAll(" ", "");
            if (isNumberString(trimStr) && trimStr.length() >= 16) {
                String sb = trimStr.substring(0, 4)
                        + " **** **** "
                        + trimStr.substring(trimStr.length() - 4, trimStr.length());
                return sb;
            }

            return str;
        };
    }

    public static Func1<String, String> encryptPhoneNumberTransformer() {
        return str -> {
            String trimStr = str.replaceAll(" ", "");
            if (TextUtils.isEmpty(trimStr) || trimStr.length() != 11) return str;

            String sb = trimStr.substring(0, 3)
                    + " **** "
                    + trimStr.substring(trimStr.length() - 4, trimStr.length());
            return sb;
        };
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean isNumberString(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

}
