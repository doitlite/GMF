package com.goldmf.GMFund.controller.internal;

import java.util.regex.Pattern;

/**
 * Created by yale on 16/2/16.
 */
public class RegexPatternHolder {
    private RegexPatternHolder() {
    }

    public static Pattern MATCH_URL = Pattern.compile("(((http|ftp|https)://)?(([0-9a-z_-]+\\.)+(com|gov|net|org|edu|int|mil|cn|me)(:[0-9]+)?((/([~0-9a-zA-Z#\\+%@\\./_\\-\\(\\)]+))?(\\?[0-9a-zA-Z#\\+%@/&\\[\\];=_\\-\\(\\)\\.]+)?)?))");

    public static Pattern MATCH_URL_ENTIRE = Pattern.compile("^(((http|ftp|https)://)?(([0-9a-z_-]+\\.)+(com|gov|net|org|edu|int|mil|cn|me)(:[0-9]+)?((/([~0-9a-zA-Z#\\+%@\\./_\\-\\(\\)]+))?(\\?[0-9a-zA-Z#\\+%@/&\\[\\];=_\\-\\(\\)\\.]+)?)?))$");

    public static Pattern MATCH_VA_PAIR_ENTIRE = Pattern.compile("^([a-z|A-Z|0-1|_]+=[^&]*)(&[a-z|A-Z|0-1|_]+=[^&]*)*$");

    public static Pattern MATCH_SINGLE_PAIR = Pattern.compile("([a-z|A-Z|0-1|_]+)=([^&]*)");

    public static Pattern MATCH_START_WITH_HTTP = Pattern.compile("^(https?://).*");

    public static Pattern MATCH_LAN_ADDRESS_ENTIRE = Pattern.compile("(^192\\.168\\.[0-9]{1,3}\\.[0-9]{1,3}$)|(^127\\.0\\.0\\.1$)|(^localhost$)|(^188.166.76.220$)");

    public static Pattern MATCH_DOUBLE_VALUE_ENTIRE = Pattern.compile("^[0-9]+(\\.[0-9]+)?$");
}
