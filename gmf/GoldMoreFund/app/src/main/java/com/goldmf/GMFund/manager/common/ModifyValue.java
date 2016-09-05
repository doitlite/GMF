package com.goldmf.GMFund.manager.common;

/**
 * Created by cupide on 15/10/12.
 */
public class ModifyValue<T> {

    public T v;
    private T old;

    public ModifyValue(T obj){

        this.v = obj;
    }

    public final void modify(T obj){

        this.old = this.v;
        this.v = obj;
    }

    public final void confirm(){

    }

    public final void cancel(){
        this.v = this.old;
    }
}
