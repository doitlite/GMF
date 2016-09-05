package com.goldmf.GMFund.util;

import com.goldmf.GMFund.MyApplication;


/**
 * Created by yalez on 2016/7/28.
 */
public abstract class IntCounter {
    private int value = 0;
    private android.os.Handler handler = new android.os.Handler(MyApplication.SHARE_INSTANCE.getMainLooper());

    public IntCounter() {
        this.value = 0;
    }

    public void release() {
        handler.removeCallbacks(null, null);
    }

    public void setValue(int value) {
        handler.removeCallbacks(null, null);
        if (this.value != value) {
            this.value = value;
            onValueChanged(this.value);
        }
    }

    public int getValue() {
        return this.value;
    }

    public void increase() {
        setValue(getValue() + 1);
    }

    public void decrease() {
        setValue(getValue() + 1);
    }

    public void setValueLater(int value, long timeInMills) {
        handler.postDelayed(() -> setValue(value), timeInMills);
    }

    public void increaseLater(long timeInMills) {
        handler.postDelayed(() -> increase(), timeInMills);
    }

    public void decreaseLater(long timeInMills) {
        handler.postDelayed(() -> decrease(), timeInMills);
    }

    public abstract void onValueChanged(int value);
}
