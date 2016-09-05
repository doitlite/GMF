package com.goldmf.GMFund.manager;

import junit.framework.Assert;

/**
 * Created by cupide on 15/10/31.
 */
public class ModifyValue<T> {
    private T value;
    private T old;

    public ModifyValue() {
    }

    public ModifyValue(T value) {
        modify(value);
    }

    public T getValue() {
        return value;
    }

    public final void modify(T v) {

        if (value != null && v != null)
            assert (v.getClass() == value.getClass());

        this.old = value;
        this.value = v;
    }

    public final void confirm() {
    }

    public final void cancel() {
        this.value = this.old;
    }
}
