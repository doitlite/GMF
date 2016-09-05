package com.goldmf.GMFund.util;

import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.UUID;

import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;

/**
 * Created by yalez on 2016/6/28.
 */
public class GlobalVariableDic {

    private static GlobalVariableDic sInstance = new GlobalVariableDic();
    private HashMap<String, Object> mVarDic = new HashMap<>();

    public static GlobalVariableDic shareInstance() {
        return sInstance;
    }

    private GlobalVariableDic() {
    }

    public String generateUniqueKey() {
        return UUID.randomUUID().toString();
    }

    public void update(String key, Object value) {
        if (mVarDic.containsKey(key)) {
            mVarDic.remove(key);
        }
        mVarDic.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <R> R get(String key) {
        Object value = mVarDic.get(key);
        return safeGet(() -> (R) value, null);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <R> R pop(String key) {
        Object value = mVarDic.get(key);
        mVarDic.remove(key);
        return safeGet(() -> (R) value, null);
    }

    public void remove(String key) {
        mVarDic.remove(key);
    }
}
