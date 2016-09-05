package com.goldmf.GMFund.extension;

import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.annimon.stream.Stream;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.manager.ISafeModel;
import com.goldmf.GMFund.model.GMFMatch;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.PageArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by yale on 15/8/22.
 */
public class MResultExtension {
    private MResultExtension() {
    }

    public static final int GMF_CODE_OK = 0;
    public static final int GMF_CODE_NEED_LOGIN = 10000;
    // 邀请手机号未注册
    public static final int GMF_CODE_NO_REGISTER = 4022011;
    // 邀请手机号号码错误
    public static final int GMF_CODE_ERROR_PHONE = 5022010;

    public static boolean isSuccess(MResults.MResultsInfo... dataSet) {
        for (MResults.MResultsInfo data : dataSet) {
            if (!data.isSuccess || data.errCode != GMF_CODE_OK) {
                return false;
            }

            if (data.data != null && data.data instanceof ISafeModel && !((ISafeModel) data.data).isValid()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSuccess(List<MResults.MResultsInfo> dataSet) {
        for (MResults.MResultsInfo data : dataSet) {
            if (!data.isSuccess || data.errCode != GMF_CODE_OK) {
                return false;
            }

            if (data.data != null && data.data instanceof ISafeModel && !((ISafeModel) data.data).isValid()) {
                return false;
            }
        }
        return true;
    }

    public static <T> MResults.MResultsInfo<T> map(MResults.MResultsInfo data, T containData) {
        data.data = containData;
        return data;
    }

    public static <T> MResults.MResultsInfo<T> cast(MResults.MResultsInfo data, Class<T> clazz) {
        data.data = null;
        return data;
    }

    public static <T extends PageArray.PageItemIndex> MResults.MResultsInfo<CommandPageArray<T>> castToCommandPageArray(MResults.MResultsInfo data, Class<T> clazz) {
        data.data = null;
        return data;
    }

    @SafeVarargs
    public static Observable<List<MResults.MResultsInfo>> zipToList(Observable<? extends MResults.MResultsInfo>... observables) {
        int count = observables.length;
        if (count == 0) {
            return Observable.empty();
        } else if (count == 1) {
            return observables[0].map(it -> Arrays.asList(it));
        } else if (count == 2) {
            return Observable.zip(observables[0],
                    observables[1],
                    (o1, o2) -> Arrays.asList(o1, o2));
        } else if (count == 3) {
            return Observable.zip(observables[0],
                    observables[1], observables[2],
                    (o1, o2, o3) -> Arrays.asList(o1, o2, o3));
        } else if (count == 4) {
            return Observable.zip(observables[0],
                    observables[1],
                    observables[2],
                    observables[3],
                    (o1, o2, o3, o4) -> Arrays.asList(o1, o2, o3, o4));
        } else if (count == 5) {
            return Observable.zip(observables[0],
                    observables[1],
                    observables[2],
                    observables[3],
                    observables[4],
                    (o1, o2, o3, o4, o5) -> Arrays.asList(o1, o2, o3, o4, o5));
        } else if (count == 6) {
            return Observable.zip(observables[0],
                    observables[1],
                    observables[2],
                    observables[3],
                    observables[4],
                    observables[5],
                    (o1, o2, o3, o4, o5, o6) -> Arrays.asList(o1, o2, o3, o4, o5, o6));
        } else if (count == 7) {
            return Observable.zip(observables[0],
                    observables[1],
                    observables[2],
                    observables[3],
                    observables[4],
                    observables[5],
                    observables[6],
                    (o1, o2, o3, o4, o5, o6, o7) -> Arrays.asList(o1, o2, o3, o4, o5, o6, o7));
        } else if (count == 8) {
            return Observable.zip(observables[0],
                    observables[1],
                    observables[2],
                    observables[3],
                    observables[4],
                    observables[5],
                    observables[6],
                    observables[7],
                    (o1, o2, o3, o4, o5, o6, o7, o8) -> Arrays.asList(o1, o2, o3, o4, o5, o6, o7, o8));
        } else if (count == 9) {
            return Observable.zip(observables[0],
                    observables[1],
                    observables[2],
                    observables[3],
                    observables[4],
                    observables[5],
                    observables[6],
                    observables[7],
                    observables[8],
                    (o1, o2, o3, o4, o5, o6, o7, o8, o9) -> Arrays.asList(o1, o2, o3, o4, o5, o6, o7, o8, o9));
        }


        throw new UnsupportedOperationException();
    }

    public static CharSequence getErrorMessage(MResults.MResultsInfo... dataSet) {
        CharSequence result = "网络好像出了点问题";

        if (dataSet != null) {
            result = Stream.of(dataSet)
                    .filter(it -> it != null)
                    .filter(it -> !it.isSuccess)
                    .filter(it -> !TextUtils.isEmpty(it.msg))
                    .map(it -> it.msg)
                    .findFirst()
                    .orElse(result.toString());
        }

        return result;
    }

    public static CharSequence getErrorMessage(List<MResults.MResultsInfo> dataSet) {
        CharSequence result = "网络好像出了点问题";

        if (dataSet != null) {
            result = Stream.of(dataSet)
                    .filter(it -> it != null)
                    .filter(it -> !it.isSuccess)
                    .filter(it -> !TextUtils.isEmpty(it.msg))
                    .map(it -> it.msg)
                    .findFirst()
                    .orElse(result.toString());
        }

        return result;
    }

    public static List<Pair<Integer, CharSequence>> getErrorMessageList(MResults.MResultsInfo... dataSet) {
        List<Pair<Integer, CharSequence>> list = new ArrayList<>();
        CharSequence result = "网络好像出了点问题";
        int errorCode = -1;
        for (MResults.MResultsInfo data : dataSet) {
            if (!TextUtils.isEmpty(data.msg)) {
                Pair<Integer, CharSequence> pair = new Pair<>(data.errCode, data.msg);
                list.add(pair);
            }
        }
        if (list.isEmpty()) {
            Pair<Integer, CharSequence> pair = new Pair<>(errorCode, result);
            list.add(pair);
        }

        return list;
    }

    public static <T> MResults<T> createObservableMResult(final Subscriber<? super MResults.MResultsInfo<T>> subscriber) {
        return createObservableMResult(subscriber, null);
    }

    public static <T> MResults<T> createObservableMResult(final Subscriber<? super MResults.MResultsInfo<T>> subscriber, Action1<? super MResults.MResultsInfo<T>> doOnCall) {
        return result -> {
            if (doOnCall != null)
                doOnCall.call(result);
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(result);
            }
            if (!result.hasNext) {
                subscriber.onCompleted();
            }
        };
    }

    public static <T> MResults<List<T>> createObservableListMResult(final Subscriber<? super MResults.MResultsInfo<List<T>>> subscriber) {
        return result -> {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(result);
            }
            if (!result.hasNext) {
                subscriber.onCompleted();
            }
        };
    }

    public static <T extends PageArray.PageItemIndex> MResults<CommandPageArray<T>> createObservablePageArrayMResult(final Subscriber<? super MResults.MResultsInfo<CommandPageArray<T>>> subscriber) {
        return result -> {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(result);
            }
            if (!result.hasNext) {
                subscriber.onCompleted();
            }
        };
    }

    public static Observable<Integer> createObservableCountDown(int count, long intervalTimeInMills) {
        return Observable.create(sub -> {
            int counter = count;
            while (counter > 1 && !sub.isUnsubscribed()) {
                sub.onNext(counter--);
                try {
                    Thread.sleep(intervalTimeInMills);
                } catch (InterruptedException ignored) {
                }
            }

            sub.onCompleted();
        }).subscribeOn(Schedulers.newThread()).cast(Integer.class);
    }
}
