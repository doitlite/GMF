package com.goldmf.GMFund.controller;

import android.util.SparseArray;

import com.orhanobut.logger.Logger;

import rx.Subscription;

/**
 * Created by yale on 16/3/21.
 */
public class SubscriptionManager {
    public enum LIFE_PERIOD {
        CREATE_DESTROY,
        START_STOP,
    }

    private SparseArray<SparseArray<Subscription>> mSubCompositionArray = new SparseArray<>();

    public SubscriptionManager() {
    }

    public boolean contain(LIFE_PERIOD period, int key) {
        SparseArray<Subscription> array = mSubCompositionArray.get(period.ordinal());
        if (array != null) {
            Subscription sub = array.get(key);
            if (sub != null) {
                return !sub.isUnsubscribed();
            }
        }

        return false;
    }

    public void subscribe(LIFE_PERIOD period, int key, Subscription subscription) {
        SparseArray<Subscription> array = mSubCompositionArray.get(period.ordinal());
        if (array == null) {
            array = new SparseArray<>();
            mSubCompositionArray.append(period.ordinal(), array);
        }

        Subscription cache = array.get(key);
        if (cache != null) {
            cache.unsubscribe();
        }

        array.append(key, subscription);
    }

    public void unsubscribe(LIFE_PERIOD period, int key) {
        SparseArray<Subscription> array = mSubCompositionArray.get(period.ordinal());
        if (array != null) {
            Subscription sub = array.get(key);
            if (sub != null) {
                sub.unsubscribe();
            }

            array.delete(period.ordinal());
        }
    }

    public void unsubscribe(LIFE_PERIOD period) {
        SparseArray<Subscription> array = mSubCompositionArray.get(period.ordinal());
        if (array != null) {
            int size = array.size();
            for (int i = 0; i < size; i++) {
                Subscription sub = array.valueAt(i);
                sub.unsubscribe();
            }
            array.clear();
        }
    }

    public void unsubscribe() {
        int size = mSubCompositionArray.size();
        for (int i = 0; i < size; i++) {
            SparseArray<Subscription> array = mSubCompositionArray.valueAt(i);
            int size2 = array.size();
            for (int j = 0; j < size2; j++) {
                Subscription sub = array.valueAt(j);
                sub.unsubscribe();
            }
            array.clear();
        }
    }
}
