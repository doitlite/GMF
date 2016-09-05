package com.goldmf.GMFund.manager;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by cupide on 15/11/28.
 */
public class BaseManager {

    private Map<String, ArrayList<EqualWeakReference<OnKeyListener>>> listeners = new HashMap<>();

    public interface OnKeyListener {
        void onKey(String key, Map<String, Object> object);
    }

    public final void addListener(String key, OnKeyListener listener) {

        if (key != null && listener != null) {
            ArrayList<EqualWeakReference<OnKeyListener>> val = listeners.get(key);
            if (val == null) {
                val = new ArrayList<>();
                listeners.put(key, val);
            }
            val.add(new EqualWeakReference<>(listener));
        }
    }

    public final void removeListener(OnKeyListener listener) {
        for (ArrayList<EqualWeakReference<OnKeyListener>> array : listeners.values()) {
            array.remove(listener);
        }

        Iterator<Map.Entry<String, ArrayList<EqualWeakReference<OnKeyListener>>>> iter = listeners.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, ArrayList<EqualWeakReference<OnKeyListener>>> entry = iter.next();
            ArrayList val = entry.getValue();

            if (val.contains(listener)) {
                val.remove(listener);
            }

            if (val.size() == 0) {
                iter.remove();
            }
        }
    }

    class EqualWeakReference<T> extends WeakReference<T> {

        public EqualWeakReference(T r) {
            super(r);
        }

        public EqualWeakReference(T r, ReferenceQueue q) {
            super(r, q);
        }

        @Override
        public boolean equals(Object o) {
            Object temp = o;
            if (o instanceof EqualWeakReference) {
                temp = ((EqualWeakReference) o).get();
            }
            return get() != null && get().equals(temp);
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    public final void fire(String key, HashMap<String, Object> object) {

        if (key != null) {
            ArrayList<EqualWeakReference<OnKeyListener>> val = listeners.get(key);
            if (val != null) {
                for (WeakReference<OnKeyListener> reference : val) {
                    if (reference.get() != null) {

                        handler.post(() -> {
                            if (reference.get() != null) {
                                reference.get().onKey(key, object);
                            }
                        });
                    }
                }
            }
        }
    }
}
