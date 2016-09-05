package com.goldmf.GMFund.extension;

import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.functions.Action1;
import rx.functions.Func1;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by yale on 15/9/1.
 */
public class ListExtension {
    private ListExtension() {
    }

    public static <T> List<List<T>> splitFromList(List<T> dataSet, int expectChunkSize) {
        int totalSize = dataSet.size();

        ArrayList<List<T>> ret = new ArrayList<>(totalSize / expectChunkSize + 1);
        for (int i = 0; i < totalSize; i += expectChunkSize) {
            int chunkSize = max(Math.min(expectChunkSize, totalSize - i), 0);
            ret.add(new ArrayList<>(dataSet.subList(i, i + chunkSize)));
        }
        return ret;
    }

    public static <T> List<T> subList(List<T> raw, int start, int end) {
        if (raw != null) {
            int count = raw.size();
            start = max(start, 0);
            end = min(end, count);
            if (start < end) {
                return raw.subList(start, end);
            }
        }
        return Collections.emptyList();
    }

    @Nullable
    public static <T> T getFromList(List<T> raw, int position) {
        if (raw != null && position >= 0 && position < raw.size()) {
            return raw.get(position);
        }
        return null;
    }

    @Nullable
    public static <T> T findFirstFromList(List<T> raw) {
        return getFromList(raw, 0);
    }

    @Nullable
    public static <T> T findLastFromList(List<T> raw) {
        if (raw != null) {
            return getFromList(raw, raw.size() - 1);
        }

        return null;
    }
}
