package yale.extension.common.function;

import java.util.concurrent.Callable;

import rx.functions.Function;

/**
 * Created by yale on 16/4/21.
 */
public interface SafeFunc1<T, R> extends Function {
    R call(T arg) throws Exception;
}
