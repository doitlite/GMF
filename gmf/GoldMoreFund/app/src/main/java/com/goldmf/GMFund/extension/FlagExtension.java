package com.goldmf.GMFund.extension;

/**
 * Created by yalez on 2016/7/16.
 */
public class FlagExtension {
    private FlagExtension() {
    }

    public static boolean hasFlag(int flags, int flag) {
        return (flags & flag) != 0;
    }
}
