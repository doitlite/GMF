package com.goldmf.GMFund.protocol.base;

import com.goldmf.GMFund.base.MResults;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import yale.extension.common.Optional;

/**
 * Created by yale on 16/3/22.
 */
public class PageArrayHelper {
    private PageArrayHelper() {
    }

    public static <T extends PageArray.PageItemIndex> List<T> getData(Optional<CommandPageArray<T>> array) {
        return getData(array.orNull());
    }

    public static <T extends PageArray.PageItemIndex> List<T> getData(CommandPageArray<T> array) {
        if (array != null && array.data() != null) {
            return array.data();
        }

        return Collections.emptyList();
    }

    public static <T extends PageArray.PageItemIndex> boolean hasMoreData(Optional<CommandPageArray<T>> array) {
        return hasMoreData(array.orNull());
    }

    public static <T extends PageArray.PageItemIndex> boolean hasMoreData(CommandPageArray<T> array) {
        if (array == null || array.getPage() == null) {
            return false;
        }

        return array.getPage().getMore();
    }

    public static <T extends PageArray.PageItemIndex> boolean hasData(Optional<CommandPageArray<T>> array) {
        return hasData(array.orNull());
    }

    public static <T extends PageArray.PageItemIndex> boolean hasData(CommandPageArray<T> array) {
        if (array == null || array.data() == null) {
            return false;
        }

        return !array.data().isEmpty();
    }

    public static <T extends PageArray.PageItemIndex> boolean isEmpty(Optional<CommandPageArray<T>> array) {
        return !hasData(array);
    }

    public static <T extends PageArray.PageItemIndex> boolean isEmpty(CommandPageArray<T> array) {
        return !hasData(array);
    }


    public static <T extends PageArray.PageItemIndex> Observable<MResults.MResultsInfo<CommandPageArray<T>>> getNextPage(CommandPageArray<T> pageArray) {
        return Observable.create(sub -> pageArray.getNextPage(result -> {
            if (!sub.isUnsubscribed()) {
                sub.onNext(result);
            }
            sub.onCompleted();
        }));
    }

    public static <T extends PageArray.PageItemIndex> Observable<MResults.MResultsInfo<CommandPageArray<T>>> getPreviousPage(CommandPageArray<T> pageArray) {
        return Observable.create(sub -> pageArray.getPrePage(result -> {
            if (!sub.isUnsubscribed()) {
                sub.onNext(result);
            }
            sub.onCompleted();
        }));
    }
}
