package com.goldmf.GMFund.extension;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.goldmf.GMFund.BuildConfig;
import com.goldmf.GMFund.util.Hourglass;
import com.goldmf.GMFund.widget.fresco.DefaultZoomableController;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;

import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import yale.extension.common.Optional;
import yale.extension.common.function.SafeAction0;
import yale.extension.common.function.SafeAction1;
import yale.extension.common.function.SafeFunc0;

/**
 * Created by yale on 16/3/24.
 */
public class ObjectExtension {
    private ObjectExtension() {
    }

    public static <T> String toString(@Nullable String sepSymbol, T... targets) {
        return toString(sepSymbol, it -> it.toString(), targets);
    }

    public static <T> String toString(@Nullable String sepSymbol, Func1<T, String> toStringFunc, T... targets) {
        sepSymbol = sepSymbol != null ? sepSymbol : "";

        if (targets != null && targets.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (T target : targets) {
                sb.append(toStringFunc.call(target)).append(sepSymbol);
            }
            return sb.substring(0, sb.length() - sepSymbol.length());
        }
        return "";
    }

    public static <T> T apply(T input, Action1<T> operation) {
        operation.call(input);
        return input;
    }

    public static boolean notNull(Object... targets) {
        for (Object target : targets) {
            if (target == null)
                return false;
        }

        return true;
    }


    public static void safeCall(SafeAction0 operation) {
        try {
            if (operation != null) {
                operation.call();
            }
        } catch (Exception ignored) {
            if (BuildConfig.DEBUG) {
                ignored.printStackTrace();
            }
        } catch (Error e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public static <T> T safeGet(SafeFunc0<T> getFunc, T defValue) {
        try {
            if (getFunc == null) {
                return defValue;
            }
            T ret = getFunc.call();
            if (ret instanceof CharSequence) {
                return TextUtils.isEmpty((CharSequence) ret) ? defValue : ret;
            } else {
                return ret == null ? defValue : ret;
            }
        } catch (NullPointerException ignored) {
            return defValue;
        } catch (ClassCastException ignored) {
            return defValue;
        } catch (NumberFormatException ignored) {
            return defValue;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return defValue;
        } catch (Error e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            return defValue;
        }
    }

    public static void traceCostTime(String tag, SafeAction0 runnable) {
        tag = tag == null ? "Unknown" : tag;
        Hourglass hourglass = new Hourglass();
        hourglass.start();
        safeCall(runnable);
        Logger.e("RunnableWithTag:%s costTimeInMills:%d", tag, hourglass.peek());
        hourglass.stop();
    }

    public static <R> R traceCostTime(String tag, SafeFunc0<R> runnable) {
        tag = tag == null ? "Unknown" : tag;
        Hourglass hourglass = new Hourglass();
        hourglass.start();
        R ret = safeGet(runnable, null);
        Logger.e("RunnableWithTag:%s costTimeInMills:%d", tag, hourglass.peek());
        hourglass.stop();
        return ret;
    }

    public static <T> SafeGetter<T> safeGet(SafeFunc0<T> getFunc) {
        return new SafeGetter<>(getFunc);
    }

    public static <T> Optional<T> opt(@Nullable T value) {
        return Optional.of(value);
    }

    public static <T> Optional<T> opt(@Nullable WeakReference<T> value) {
        return Optional.of(value == null ? null : value.get());
    }

    public static <T> Optional<T> opt(@NonNull Optional<T> value) {
        return value;
    }

    public static class SafeGetter<T> {
        private SafeFunc0<T> getFunc;
        private T defValue;

        public SafeGetter(SafeFunc0<T> getFunc) {
            this.getFunc = getFunc;
        }

        public SafeGetter<T> def(T defValue) {
            this.defValue = defValue;
            return this;
        }

        public T get() {
            return safeGet(getFunc, defValue);
        }
    }
}
