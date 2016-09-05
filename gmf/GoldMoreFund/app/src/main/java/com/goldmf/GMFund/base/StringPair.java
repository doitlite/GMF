package com.goldmf.GMFund.base;

import android.util.Pair;

import com.goldmf.GMFund.extension.ObjectExtension;

import static com.goldmf.GMFund.extension.ObjectExtension.opt;

/**
 * Created by yale on 15/11/12.
 */
public class StringPair extends Pair<String, String> {
    public StringPair(String first, String second) {
        super(first, second);
    }

    public static StringPair create(String first, String second) {
        return new StringPair(first, second);
    }

    @Override
    public String toString() {
        return opt(first).or("") + opt(second).or("");
    }
}
